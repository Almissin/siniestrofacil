package cl.siniestrofacil.usuarios.repository;

import cl.siniestrofacil.usuarios.model.NombreRol;
import cl.siniestrofacil.usuarios.model.Rol;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface RolRepository extends JpaRepository<Rol, Long> {

    Optional<Rol> findByNombre(NombreRol nombre);
}
