package cl.siniestrofacil.talleres.repository;

import cl.siniestrofacil.talleres.model.Asignacion;
import cl.siniestrofacil.talleres.model.EstadoAsignacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AsignacionRepository extends JpaRepository<Asignacion, Long> {

    boolean existsByFolioDenuncia(String folioDenuncia);

    Optional<Asignacion> findByFolioDenuncia(String folioDenuncia);

    /** Navega la relacion Asignacion -> Taller -> id. */
    long countByTallerIdAndEstado(Long tallerId, EstadoAsignacion estado);

    boolean existsByTallerId(Long tallerId);

    List<Asignacion> findByTallerIdOrderByIdDesc(Long tallerId);

    List<Asignacion> findAllByOrderByIdDesc();
}
