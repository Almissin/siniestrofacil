package cl.siniestrofacil.talleres.service;

import cl.siniestrofacil.talleres.dto.AsignacionRequest;
import cl.siniestrofacil.talleres.dto.AsignacionResponse;
import cl.siniestrofacil.talleres.exception.ConflictoException;
import cl.siniestrofacil.talleres.exception.RecursoDuplicadoException;
import cl.siniestrofacil.talleres.exception.RecursoNoEncontradoException;
import cl.siniestrofacil.talleres.exception.ReglaNegocioException;
import cl.siniestrofacil.talleres.model.Asignacion;
import cl.siniestrofacil.talleres.model.EstadoAsignacion;
import cl.siniestrofacil.talleres.model.Taller;
import cl.siniestrofacil.talleres.repository.AsignacionRepository;
import cl.siniestrofacil.talleres.repository.TallerRepository;
import cl.siniestrofacil.talleres.security.UsuarioAutenticado;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

/**
 * Asignacion de talleres a denuncias.
 * En la EP3 este servicio sera llamado por la AWS Lambda que procesa la cola SQS,
 * solo cuando la poliza de la denuncia esta vigente.
 */
@Service
@RequiredArgsConstructor
public class AsignacionService {

    private final AsignacionRepository asignacionRepository;
    private final TallerRepository tallerRepository;
    private final TallerService tallerService;

    /** Asigna el taller activo con mas cupos libres. Si no hay cupo, la denuncia debe procesarse mas tarde. */
    @Transactional
    public AsignacionResponse asignar(AsignacionRequest req) {
        String folio = req.folioDenuncia().trim();
        if (asignacionRepository.existsByFolioDenuncia(folio)) {
            throw new RecursoDuplicadoException("la denuncia " + folio + " ya tiene un taller asignado");
        }

        List<CupoTaller> candidatos = tallerRepository.findByActivoTrueOrderByIdAsc().stream()
                .map(t -> new CupoTaller(t, tallerService.ocupados(t)))
                .toList();

        Taller elegido = seleccionarTaller(candidatos)
                .orElseThrow(() -> new ConflictoException(
                        "no hay talleres con cupo disponible; la denuncia debe procesarse mas tarde"));

        Asignacion asignacion = new Asignacion();
        asignacion.setFolioDenuncia(folio);
        asignacion.setTaller(elegido);
        asignacion.setEstado(EstadoAsignacion.ASIGNADA);
        return AsignacionResponse.from(asignacionRepository.save(asignacion));
    }

    /**
     * Regla de seleccion: el taller con mas cupos libres; en empate, el de menor id.
     * Es estatica y sin base de datos para poder probarla con pruebas unitarias.
     */
    public static Optional<Taller> seleccionarTaller(List<CupoTaller> candidatos) {
        return candidatos.stream()
                .filter(c -> c.taller().isActivo() && c.libres() > 0)
                .max(Comparator.comparingLong(CupoTaller::libres)
                        .thenComparing(c -> c.taller().getId(), Comparator.reverseOrder()))
                .map(CupoTaller::taller);
    }

    @Transactional(readOnly = true)
    public List<AsignacionResponse> listar() {
        return asignacionRepository.findAllByOrderByIdDesc().stream().map(AsignacionResponse::from).toList();
    }

    /** El taller ve solo sus asignaciones (el tallerId viene del token). */
    @Transactional(readOnly = true)
    public List<AsignacionResponse> listarDeMiTaller(UsuarioAutenticado usuario) {
        if (usuario.tallerId() == null) {
            throw new ReglaNegocioException("el usuario no tiene un taller asociado");
        }
        return asignacionRepository.findByTallerIdOrderByIdDesc(usuario.tallerId()).stream()
                .map(AsignacionResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public AsignacionResponse buscarPorFolio(String folio, UsuarioAutenticado usuario) {
        Asignacion asignacion = asignacionRepository.findByFolioDenuncia(folio)
                .orElseThrow(() -> new RecursoNoEncontradoException(
                        "la denuncia " + folio + " no tiene taller asignado"));
        verificarAcceso(asignacion, usuario);
        return AsignacionResponse.from(asignacion);
    }

    /** El taller termina el caso y libera el cupo. */
    @Transactional
    public AsignacionResponse finalizar(Long id, UsuarioAutenticado usuario) {
        Asignacion asignacion = asignacionRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("no existe una asignacion con id " + id));
        verificarAcceso(asignacion, usuario);
        if (asignacion.getEstado() == EstadoAsignacion.FINALIZADA) {
            throw new ConflictoException("la asignacion " + id + " ya fue finalizada");
        }
        asignacion.setEstado(EstadoAsignacion.FINALIZADA);
        asignacion.setFechaFinalizacion(LocalDateTime.now());
        return AsignacionResponse.from(asignacion);
    }

    /** Un usuario TALLER solo puede ver o modificar asignaciones de su propio taller. */
    private void verificarAcceso(Asignacion asignacion, UsuarioAutenticado usuario) {
        if (usuario.esTaller() && !asignacion.getTaller().getId().equals(usuario.tallerId())) {
            throw new AccessDeniedException("la asignacion pertenece a otro taller");
        }
    }
}
