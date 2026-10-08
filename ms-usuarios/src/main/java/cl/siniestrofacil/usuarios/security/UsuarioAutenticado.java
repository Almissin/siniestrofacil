package cl.siniestrofacil.usuarios.security;

/**
 * Datos del usuario que viajan dentro del token JWT.
 * Los demas microservicios leen estos mismos datos (id, rut, rol, tallerId) sin consultar ms-usuarios.
 */
public record UsuarioAutenticado(Long id, String email, String rut, String rol, Long tallerId) {
}
