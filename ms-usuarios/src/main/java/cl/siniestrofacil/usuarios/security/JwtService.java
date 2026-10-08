package cl.siniestrofacil.usuarios.security;

import cl.siniestrofacil.usuarios.model.Usuario;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.Optional;

/**
 * Genera y valida tokens JWT firmados con HMAC-SHA256.
 * La clave (jwt.secret) debe ser la misma en todos los microservicios para que validen el token.
 */
@Service
public class JwtService {

    public static final String EMISOR = "ms-usuarios";

    private final SecretKey clave;
    private final long expiracionMs;

    public JwtService(@Value("${jwt.secret}") String secreto,
                      @Value("${jwt.expiracion-ms}") long expiracionMs) {
        this.clave = Keys.hmacShaKeyFor(secreto.getBytes(StandardCharsets.UTF_8));
        this.expiracionMs = expiracionMs;
    }

    public String generarToken(Usuario usuario) {
        Date ahora = new Date();
        JwtBuilder builder = Jwts.builder()
                .issuer(EMISOR)
                .subject(usuario.getEmail())
                .claim("uid", usuario.getId())
                .claim("rut", usuario.getRut())
                .claim("rol", usuario.getRol().getNombre().name())
                .issuedAt(ahora)
                .expiration(new Date(ahora.getTime() + expiracionMs));
        if (usuario.getTallerId() != null) {
            builder.claim("tallerId", usuario.getTallerId());
        }
        return builder.signWith(clave).compact();
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

    public long getExpiracionSegundos() {
        return expiracionMs / 1000;
    }

    private static Long aLong(Number n) {
        return n == null ? null : n.longValue();
    }
}
