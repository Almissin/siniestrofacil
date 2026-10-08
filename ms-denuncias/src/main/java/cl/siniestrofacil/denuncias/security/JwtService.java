package cl.siniestrofacil.denuncias.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

/**
 * Valida los tokens JWT emitidos por ms-usuarios (este servicio NO genera tokens).
 * La clave (jwt.secret) debe ser la misma que en ms-usuarios.
 */
@Service
public class JwtService {

    public static final String EMISOR = "ms-usuarios";

    private final SecretKey clave;

    public JwtService(@Value("${jwt.secret}") String secreto) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
    }

    /** Devuelve los datos del usuario si el token es valido; vacio si esta alterado, vencido o mal formado. */
    public Optional<UsuarioAutenticado> validar(String token) {
        try {
            Claims c = Jwts.parser()
                    .verifyWith(clave)
                    .requireIssuer(EMISOR)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return Optional.of(new UsuarioAutenticado(
                    aLong(c.get("uid", Number.class)),
                    c.getSubject(),
                    c.get("rut", String.class),
                    c.get("rol", String.class),
                    aLong(c.get("tallerId", Number.class))));
        } catch (JwtException | IllegalArgumentException e) {
            return Optional.empty();
        }
    }

    private static Long aLong(Number n) {
        return n == null ? null : n.longValue();
    }
}
