package cl.siniestrofacil.usuarios.repository;

import cl.siniestrofacil.usuarios.model.NombreRol;
import cl.siniestrofacil.usuarios.model.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Long> {

    Optional<Usuario> findByEmail(String email);

    boolean existsByEmail(String email);

    boolean existsByRut(String rut);

    /** Navega la relacion Usuario -> Rol -> nombre. */
    List<Usuario> findByRolNombreOrderByIdAsc(NombreRol nombre);

    List<Usuario> findAllByOrderByIdAsc();
}
