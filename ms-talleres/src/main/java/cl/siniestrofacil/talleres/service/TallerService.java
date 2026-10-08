package cl.siniestrofacil.talleres.service;

import cl.siniestrofacil.talleres.dto.TallerRequest;
import cl.siniestrofacil.talleres.dto.TallerResponse;
import cl.siniestrofacil.talleres.exception.ConflictoException;
import cl.siniestrofacil.talleres.exception.RecursoDuplicadoException;
import cl.siniestrofacil.talleres.exception.RecursoNoEncontradoException;
import cl.siniestrofacil.talleres.exception.ReglaNegocioException;
import cl.siniestrofacil.talleres.model.EstadoAsignacion;
import cl.siniestrofacil.talleres.model.Taller;
import cl.siniestrofacil.talleres.repository.AsignacionRepository;
import cl.siniestrofacil.talleres.repository.TallerRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Gestion del listado de talleres en convenio.
 */
@Service
@RequiredArgsConstructor
public class TallerService {

    private final TallerRepository tallerRepository;
    private final AsignacionRepository asignacionRepository;

    @Transactional(readOnly = true)
    public List<TallerResponse> listar(boolean soloActivos) {
        List<Taller> talleres = soloActivos
                ? tallerRepository.findByActivoTrueOrderByIdAsc()
                : tallerRepository.findAllByOrderByIdAsc();
        return talleres.stream().map(this::aResponse).toList();
    }

    /** Talleres activos que todavia tienen al menos un cupo libre. */
    @Transactional(readOnly = true)
    public List<TallerResponse> listarDisponibles() {
        return tallerRepository.findByActivoTrueOrderByIdAsc().stream()
                .map(this::aResponse)
                .filter(t -> t.cuposDisponibles() > 0)
                .toList();
    }

    @Transactional(readOnly = true)
    public TallerResponse obtener(Long id) {
        return aResponse(buscar(id));
    }

    @Transactional
    public TallerResponse crear(TallerRequest req) {
        String rut = normalizarRut(req.rut());
        if (tallerRepository.existsByNombreIgnoreCase(req.nombre().trim())) {
            throw new RecursoDuplicadoException("ya existe un taller con el nombre " + req.nombre().trim());
        }
        if (tallerRepository.existsByRut(rut)) {
            throw new RecursoDuplicadoException("ya existe un taller con el rut " + rut);
        }
        Taller taller = new Taller();
        taller.setRut(rut);
        copiarDatos(taller, req);
        taller.setActivo(true);
        return aResponse(tallerRepository.save(taller));
    }

    @Transactional
    public TallerResponse actualizar(Long id, TallerRequest req) {
        Taller taller = buscar(id);
        String rut = normalizarRut(req.rut());

        if (tallerRepository.existsByNombreIgnoreCaseAndIdNot(req.nombre().trim(), id)) {
            throw new RecursoDuplicadoException("ya existe un taller con el nombre " + req.nombre().trim());
        }
        if (!rut.equals(taller.getRut()) && tallerRepository.existsByRut(rut)) {
            throw new RecursoDuplicadoException("ya existe un taller con el rut " + rut);
        }
        long ocupados = ocupados(taller);
        if (req.cupoMaximo() < ocupados) {
            throw new ReglaNegocioException("el cupo maximo no puede ser menor a los " + ocupados
                    + " vehiculos que el taller tiene asignados actualmente");
        }
        taller.setRut(rut);
        copiarDatos(taller, req);
        return aResponse(taller);
    }

    @Transactional
    public TallerResponse cambiarEstado(Long id, boolean activo) {
        Taller taller = buscar(id);
        taller.setActivo(activo);
        return aResponse(taller);
    }

    /** Solo se elimina un taller sin historial; si ya tuvo asignaciones se debe desactivar. */
    @Transactional
    public void eliminar(Long id) {
        Taller taller = buscar(id);
        if (asignacionRepository.existsByTallerId(id)) {
            throw new ConflictoException("el taller tiene asignaciones registradas; desactivelo en lugar de eliminarlo");
        }
        tallerRepository.delete(taller);
    }

    // ======================= APOYO =======================

    Taller buscar(Long id) {
        return tallerRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("no existe un taller con id " + id));
    }

    long ocupados(Taller taller) {
        return asignacionRepository.countByTallerIdAndEstado(taller.getId(), EstadoAsignacion.ASIGNADA);
    }

    private TallerResponse aResponse(Taller taller) {
        return TallerResponse.from(taller, ocupados(taller));
    }

    private void copiarDatos(Taller taller, TallerRequest req) {
        taller.setNombre(req.nombre().trim());
        taller.setDireccion(req.direccion().trim());
        taller.setComuna(req.comuna().trim());
        taller.setTelefono(req.telefono());
        taller.setEmail(req.email() == null ? null : req.email().trim().toLowerCase(Locale.ROOT));
        taller.setCupoMaximo(req.cupoMaximo());
    }

    private static String normalizarRut(String rut) {
        return rut.trim().toUpperCase(Locale.ROOT);
    }
}
