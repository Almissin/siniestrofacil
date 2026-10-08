package cl.siniestrofacil.polizas.service;

import cl.siniestrofacil.polizas.dto.*;
import cl.siniestrofacil.polizas.exception.RecursoDuplicadoException;
import cl.siniestrofacil.polizas.exception.RecursoNoEncontradoException;
import cl.siniestrofacil.polizas.exception.ReglaNegocioException;
import cl.siniestrofacil.polizas.model.Cobertura;
import cl.siniestrofacil.polizas.model.Poliza;
import cl.siniestrofacil.polizas.repository.PolizaRepository;
import cl.siniestrofacil.polizas.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

/**
 * Gestion del listado de polizas y verificacion de su vigencia.
 */
@Service
@RequiredArgsConstructor
public class PolizaService {

    private static final DateTimeFormatter FORMATO = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private final PolizaRepository polizaRepository;
    private final Clock clock;

    // ======================= CONSULTAS =======================

    @Transactional(readOnly = true)
    public List<PolizaResponse> listar(String rut) {
        List<Poliza> polizas = (rut == null || rut.isBlank())
                ? polizaRepository.findAllByOrderByNumeroAsc()
                : polizaRepository.findByRutAseguradoOrderByNumeroAsc(normalizarRut(rut));
        return polizas.stream().map(this::aResponse).toList();
    }

    /** El asegurado ve solo sus polizas: el rut se toma del token. */
    @Transactional(readOnly = true)
    public List<PolizaResponse> listarMisPolizas(UsuarioAutenticado usuario) {
        return polizaRepository.findByRutAseguradoOrderByNumeroAsc(usuario.rut()).stream()
                .map(this::aResponse).toList();
    }

    @Transactional(readOnly = true)
    public PolizaResponse obtener(String numero, UsuarioAutenticado usuario) {
        Poliza poliza = buscar(numero);
        verificarAcceso(poliza, usuario);
        return aResponse(poliza);
    }

    /** Verifica si la poliza esta vigente hoy. En la EP3 lo llamara la AWS Lambda antes de asignar taller. */
    @Transactional(readOnly = true)
    public VigenciaResponse consultarVigencia(String numero, UsuarioAutenticado usuario) {
        Poliza poliza = buscar(numero);
        verificarAcceso(poliza, usuario);
        LocalDate hoy = LocalDate.now(clock);
        ResultadoVigencia resultado = evaluarVigencia(poliza, hoy);
        return new VigenciaResponse(poliza.getNumero(), poliza.getRutAsegurado(), resultado.vigente(),
                resultado.motivo(), poliza.getFechaInicio(), poliza.getFechaTermino(), hoy);
    }

    // ======================= ADMINISTRACION =======================

    @Transactional
    public PolizaResponse crear(PolizaRequest req) {
        String numero = normalizarNumero(req.numero());
        if (polizaRepository.existsByNumero(numero)) {
            throw new RecursoDuplicadoException("ya existe una poliza con el numero " + numero);
        }
        validarFechas(req.fechaInicio(), req.fechaTermino());

        Poliza poliza = new Poliza();
        poliza.setNumero(numero);
        poliza.setRutAsegurado(normalizarRut(req.rutAsegurado()));
        poliza.setPatente(req.patente());
        poliza.setFechaInicio(req.fechaInicio());
        poliza.setFechaTermino(req.fechaTermino());
        poliza.setActiva(true);
        req.coberturas().forEach(c -> poliza.agregarCobertura(new Cobertura(c.nombre().trim(), c.montoMaximo())));
        return aResponse(polizaRepository.save(poliza));
    }

    @Transactional
    public PolizaResponse actualizar(String numero, ActualizarPolizaRequest req) {
        Poliza poliza = buscar(numero);
        validarFechas(req.fechaInicio(), req.fechaTermino());

        poliza.setRutAsegurado(normalizarRut(req.rutAsegurado()));
        poliza.setPatente(req.patente());
        poliza.setFechaInicio(req.fechaInicio());
        poliza.setFechaTermino(req.fechaTermino());
        // orphanRemoval elimina de la BD las coberturas que se quitan de la lista
        poliza.limpiarCoberturas();
        req.coberturas().forEach(c -> poliza.agregarCobertura(new Cobertura(c.nombre().trim(), c.montoMaximo())));
        return aResponse(polizaRepository.saveAndFlush(poliza));
    }

    @Transactional
    public PolizaResponse cambiarEstado(String numero, boolean activa) {
        Poliza poliza = buscar(numero);
        poliza.setActiva(activa);
        return aResponse(poliza);
    }

    @Transactional
    public void eliminar(String numero) {
        polizaRepository.delete(buscar(numero));
    }

    // ======================= REGLA DE VIGENCIA =======================

    /**
     * Una poliza esta vigente si esta activa y la fecha consultada esta entre su inicio y su termino (ambos incluidos).
     * Es estatica y sin base de datos para probarla con pruebas unitarias.
     */
    public static ResultadoVigencia evaluarVigencia(Poliza poliza, LocalDate fecha) {
        if (!poliza.isActiva()) {
            return new ResultadoVigencia(false, "la poliza se encuentra anulada");
        }
        if (fecha.isBefore(poliza.getFechaInicio())) {
            return new ResultadoVigencia(false,
                    "la poliza aun no inicia su vigencia (comienza el " + FORMATO.format(poliza.getFechaInicio()) + ")");
        }
        if (fecha.isAfter(poliza.getFechaTermino())) {
            return new ResultadoVigencia(false,
                    "la poliza vencio el " + FORMATO.format(poliza.getFechaTermino()));
        }
        return new ResultadoVigencia(true,
                "la poliza esta vigente hasta el " + FORMATO.format(poliza.getFechaTermino()));
    }

    // ======================= APOYO =======================

    private Poliza buscar(String numero) {
        String n = normalizarNumero(numero);
        return polizaRepository.findByNumero(n)
                .orElseThrow(() -> new RecursoNoEncontradoException("no existe una poliza con numero " + n));
    }

    /** Un ASEGURADO solo puede consultar sus propias polizas; un TALLER no consulta polizas. */
    private void verificarAcceso(Poliza poliza, UsuarioAutenticado usuario) {
        if (usuario.esAdmin()) {
            return;
        }
        if (usuario.esAsegurado() && poliza.getRutAsegurado().equalsIgnoreCase(usuario.rut())) {
            return;
        }
        throw new AccessDeniedException("la poliza " + poliza.getNumero() + " no pertenece al usuario");
    }

    private void validarFechas(LocalDate inicio, LocalDate termino) {
        if (!termino.isAfter(inicio)) {
            throw new ReglaNegocioException("la fecha de termino debe ser posterior a la fecha de inicio");
        }
    }

    private PolizaResponse aResponse(Poliza poliza) {
        return PolizaResponse.from(poliza, evaluarVigencia(poliza, LocalDate.now(clock)));
    }

    private static String normalizarNumero(String numero) {
        return numero.trim().toUpperCase(Locale.ROOT);
    }

    private static String normalizarRut(String rut) {
        return rut.trim().toUpperCase(Locale.ROOT);
    }
}
