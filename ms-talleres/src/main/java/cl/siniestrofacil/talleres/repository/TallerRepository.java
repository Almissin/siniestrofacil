package cl.siniestrofacil.talleres.repository;

import cl.siniestrofacil.talleres.model.Taller;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TallerRepository extends JpaRepository<Taller, Long> {

    boolean existsByNombreIgnoreCase(String nombre);

    boolean existsByNombreIgnoreCaseAndIdNot(String nombre, Long id);

    boolean existsByRut(String rut);

    List<Taller> findAllByOrderByIdAsc();

    List<Taller> findByActivoTrueOrderByIdAsc();
}
