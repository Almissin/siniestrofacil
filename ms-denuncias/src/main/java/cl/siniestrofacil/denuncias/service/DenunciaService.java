package cl.siniestrofacil.denuncias.service;

import cl.siniestrofacil.denuncias.dto.DenunciaRequest;
import cl.siniestrofacil.denuncias.dto.DenunciaResponse;
import cl.siniestrofacil.denuncias.dto.ResultadoRequest;
import cl.siniestrofacil.denuncias.exception.ConflictoException;
import cl.siniestrofacil.denuncias.exception.DenunciaNoEncontradaException;
import cl.siniestrofacil.denuncias.exception.ReglaNegocioException;
import cl.siniestrofacil.denuncias.model.Denuncia;
import cl.siniestrofacil.denuncias.model.EstadoDenuncia;
import cl.siniestrofacil.denuncias.model.Fotografia;
import cl.siniestrofacil.denuncias.repository.DenunciaRepository;
import cl.siniestrofacil.denuncias.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.Year;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Registro y consulta de denuncias.
 * El registro es inmediato y entrega el folio: NO llama a ms-polizas ni a ms-talleres,
 * porque el caso exige que el ingreso de la denuncia y la asignacion del taller sean procesos independientes.
 */
@Service
@RequiredArgsConstructor
public class DenunciaService {

    private static final String PREFIJO = "SF-";

    private final DenunciaRepository denunciaRepository;
    private final Clock clock;

    // ======================= ASEGURADO =======================

    /** Registra la denuncia, genera el folio y la deja en estado RECIBIDA. */
    @Transactional
    public DenunciaResponse registrar(DenunciaRequest req, UsuarioAutenticado usuario) {
        int anio = Year.now(clock).getValue();
        String prefijoAnio = PREFIJO + anio + "-";
        Optional<String> ultimo = denunciaRepository.findTopByFolioStartingWithOrderByFolioDesc(prefijoAnio)
                .map(Denuncia::getFolio);

        Denuncia denuncia = new Denuncia();
        denuncia.setFolio(siguienteFolio(anio, ultimo));
        denuncia.setPatente(req.patente());
        denuncia.setRutAsegurado(usuario.rut());
        denuncia.setNumeroPoliza(req.numeroPoliza().trim().toUpperCase(Locale.ROOT));
        denuncia.setFechaSiniestro(req.fechaSiniestro());
        denuncia.setDescripcion(req.descripcion().trim());
        denuncia.setEstado(EstadoDenuncia.RECIBIDA);
        if (req.fotografias() != null) {
            req.fotografias().forEach(f -> denuncia.agregarFotografia(new Fotografia(f.url().trim(), f.descripcion())));
        }
        return DenunciaResponse.from(denunciaRepository.save(denuncia));
    }

    /**
     * Folio SF-AAAA-NNNNNN. Continua el correlativo del ultimo folio del anio guardado en la BD,
     * asi no se repite al reiniciar el servicio. Estatico para probarlo con pruebas unitarias.
     */
    public static String siguienteFolio(int anio, Optional<String> ultimoFolioDelAnio) {
        int siguiente = ultimoFolioDelAnio
                .map(f -> Integer.parseInt(f.substring(f.lastIndexOf('-') + 1)) + 1)
                .orElse(1);
        return String.format("%s%d-%06d", PREFIJO, anio, siguiente);
    }

    @Transactional(readOnly = true)
    public List<DenunciaResponse> listarMisDenuncias(UsuarioAutenticado usuario) {
        return denunciaRepository.findByRutAseguradoOrderByFechaRegistroDesc(usuario.rut()).stream()
                .map(DenunciaResponse::from).toList();
    }

    // ======================= TALLER =======================

    /** El taller consulta unicamente las denuncias que le fueron asignadas (tallerId del token). */
    @Transactional(readOnly = true)
    public List<DenunciaResponse> listarAsignadas(UsuarioAutenticado usuario) {
        if (usuario.tallerId() == null) {
            throw new ReglaNegocioException("el usuario no tiene un taller asociado");
        }
        return denunciaRepository.findByTallerIdOrderByFechaRegistroDesc(usuario.tallerId()).stream()
                .map(DenunciaResponse::from).toList();
    }

    /** El taller asignado confirma que recibio el vehiculo. */
    @Transactional
    public DenunciaResponse confirmarRecepcion(String folio, UsuarioAutenticado usuario) {
        Denuncia denuncia = buscar(folio);
        if (!denuncia.getEstado().admiteRecepcion()) {
            throw new ConflictoException("no se puede confirmar la recepcion: la denuncia " + denuncia.getFolio()
                    + " esta en estado " + denuncia.getEstado());
        }
        if (usuario.tallerId() == null || !usuario.tallerId().equals(denuncia.getTallerId())) {
            throw new AccessDeniedException("la denuncia " + denuncia.getFolio() + " no esta asignada a su taller");
        }
        denuncia.setEstado(EstadoDenuncia.VEHICULO_RECIBIDO);
        denuncia.setFechaRecepcion(LocalDateTime.now(clock));
        return DenunciaResponse.from(denuncia);
    }

    // ======================= ADMIN / PROCESAMIENTO =======================

    @Transactional(readOnly = true)
    public List<DenunciaResponse> listar(EstadoDenuncia estado) {
        List<Denuncia> denuncias = (estado == null)
                ? denunciaRepository.findAllByOrderByFechaRegistroDesc()
                : denunciaRepository.findByEstadoOrderByFechaRegistroDesc(estado);
        return denuncias.stream().map(DenunciaResponse::from).toList();
    }

    /**
     * Guarda el resultado del procesamiento asincrono.
     * En la EP3 lo llamara la AWS Lambda despues de consultar ms-polizas y ms-talleres.
     */
    @Transactional
    public DenunciaResponse registrarResultado(String folio, ResultadoRequest req) {
        Denuncia denuncia = buscar(folio);
        if (!denuncia.getEstado().admiteResultado()) {
            throw new ConflictoException("la denuncia " + denuncia.getFolio()
                    + " ya fue procesada (estado " + denuncia.getEstado() + ")");
        }

        switch (req.estado()) {
            case RECHAZADA -> {
                if (req.motivo() == null || req.motivo().isBlank()) {
                    throw new ReglaNegocioException("para rechazar la denuncia se debe indicar el motivo");
                }
                denuncia.setMotivoRechazo(req.motivo().trim());
            }
            case TALLER_ASIGNADO -> {
                if (req.tallerId() == null || req.nombreTaller() == null || req.nombreTaller().isBlank()) {
                    throw new ReglaNegocioException("para asignar taller se debe indicar tallerId y nombreTaller");
                }
                denuncia.setTallerId(req.tallerId());
                denuncia.setNombreTaller(req.nombreTaller().trim());
            }
            default -> throw new ReglaNegocioException("el resultado solo puede ser RECHAZADA o TALLER_ASIGNADO");
        }
        denuncia.setEstado(req.estado());
        denuncia.setFechaResultado(LocalDateTime.now(clock));
        return DenunciaResponse.from(denuncia);
    }

    // ======================= CONSULTA POR FOLIO =======================

    /** ADMIN ve todas; ASEGURADO solo las suyas; TALLER solo las asignadas a su taller. */
    @Transactional(readOnly = true)
    public DenunciaResponse obtener(String folio, UsuarioAutenticado usuario) {
        Denuncia denuncia = buscar(folio);
        boolean permitido = usuario.esAdmin()
                || (usuario.esAsegurado() && denuncia.getRutAsegurado().equalsIgnoreCase(usuario.rut()))
                || (usuario.esTaller() && usuario.tallerId() != null && usuario.tallerId().equals(denuncia.getTallerId()));
        if (!permitido) {
            throw new AccessDeniedException("la denuncia " + denuncia.getFolio() + " no pertenece al usuario");
        }
        return DenunciaResponse.from(denuncia);
    }

    private Denuncia buscar(String folio) {
        String f = folio.trim().toUpperCase(Locale.ROOT);
        return denunciaRepository.findById(f).orElseThrow(() -> new DenunciaNoEncontradaException(f));
    }
}
