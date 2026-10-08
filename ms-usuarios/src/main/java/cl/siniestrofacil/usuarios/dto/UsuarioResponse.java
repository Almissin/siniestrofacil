package cl.siniestrofacil.usuarios.dto;

import cl.siniestrofacil.usuarios.model.NombreRol;
import cl.siniestrofacil.usuarios.model.Usuario;

import java.time.LocalDateTime;

/** Datos publicos de un usuario (nunca incluye la contrasena). */
public record UsuarioResponse(
        Long id,
        String rut,
        String nombre,
        String apellido,
        String email,
        String telefono,
        NombreRol rol,
        Long tallerId,
        boolean activo,
        LocalDateTime fechaCreacion
) {
    public static UsuarioResponse from(Usuario u) {
        return new UsuarioResponse(
                u.getId(),
                u.getRut(),
                u.getNombre(),
                u.getApellido(),
                u.getEmail(),
                u.getTelefono(),
                u.getRol().getNombre(),
                u.getTallerId(),
                u.isActivo(),
                u.getFechaCreacion()
        );
    }
}
