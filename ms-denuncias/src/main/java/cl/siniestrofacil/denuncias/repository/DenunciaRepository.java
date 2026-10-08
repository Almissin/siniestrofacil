package cl.siniestrofacil.denuncias.repository;

import cl.siniestrofacil.denuncias.model.Denuncia;
import cl.siniestrofacil.denuncias.model.EstadoDenuncia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface DenunciaRepository extends JpaRepository<Denuncia, String> {

    /** Ultimo folio del anio (ej: prefijo "SF-2026-"), para continuar el correlativo. */
    Optional<Denuncia> findTopByFolioStartingWithOrderByFolioDesc(String prefijo);

    List<Denuncia> findByRutAseguradoOrderByFechaRegistroDesc(String rutAsegurado);

    List<Denuncia> findByTallerIdOrderByFechaRegistroDesc(Long tallerId);

    List<Denuncia> findByEstadoOrderByFechaRegistroDesc(EstadoDenuncia estado);

    List<Denuncia> findAllByOrderByFechaRegistroDesc();
}
