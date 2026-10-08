package cl.siniestrofacil.talleres.security;

/**
 * Datos del usuario que vienen dentro del token JWT emitido por ms-usuarios.
 */
public record UsuarioAutenticado(Long id, String email, String rut, String rol, Long tallerId) {

    public boolean esAdmin() {
        return "ADMIN".equals(rol);
    }

    public boolean esTaller() {
        return "TALLER".equals(rol);
    }
}
