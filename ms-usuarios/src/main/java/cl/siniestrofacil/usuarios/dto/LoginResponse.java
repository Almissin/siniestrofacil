package cl.siniestrofacil.usuarios.dto;

/** Respuesta del login: token JWT y datos basicos del usuario. */
public record LoginResponse(
        String token,
        String tipo,
        long expiraEnSegundos,
        UsuarioResponse usuario
) {
}
