package cl.siniestrofacil.denuncias.repository;

import cl.siniestrofacil.denuncias.model.Denuncia;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

// acceso a la tabla denuncias (save, findById, etc. vienen incluidos)
@Repository
public interface DenunciaRepository extends JpaRepository<Denuncia, String> {

    // contar denuncias cuyo folio empieza con un prefijo (ej. "SF-2026-")
    long countByFolioStartingWith(String prefijo);
}
