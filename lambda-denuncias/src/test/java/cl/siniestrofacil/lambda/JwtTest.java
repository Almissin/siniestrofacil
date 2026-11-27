package cl.siniestrofacil.lambda;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtTest {

    private static final String CLAVE = Config.CLAVE_POR_DEFECTO;
    private static final long AHORA = 1_800_000_000L;

    @Test
    void validaUnTokenRecienCreado() {
        String token = Jwt.crear(Map.of("sub", "admin@siniestrofacil.cl", "rol", "ADMIN"), CLAVE, 300, AHORA);
        var claims = Jwt.validar(token, CLAVE, AHORA + 10);
        assertTrue(claims.isPresent());
        assertEquals("ADMIN", claims.get().get("rol"));
    }

    @Test
    void rechazaUnTokenVencido() {
        String token = Jwt.crear(Map.of("sub", "a"), CLAVE, 300, AHORA);
        assertTrue(Jwt.validar(token, CLAVE, AHORA + 301).isEmpty());
    }

    @Test
    void rechazaUnTokenFirmadoConOtraClave() {
        String token = Jwt.crear(Map.of("sub", "a"), "otra-clave-distinta-con-mas-de-treinta-y-dos-caracteres", 300, AHORA);
        assertTrue(Jwt.validar(token, CLAVE, AHORA).isEmpty());
    }

    @Test
    void rechazaTokenAlteradoONulo() {
        String token = Jwt.crear(Map.of("sub", "a", "rol", "ASEGURADO"), CLAVE, 300, AHORA);
        String alterado = token.substring(0, token.length() - 2) + "xx";
        assertTrue(Jwt.validar(alterado, CLAVE, AHORA).isEmpty());
        assertTrue(Jwt.validar(null, CLAVE, AHORA).isEmpty());
        assertTrue(Jwt.validar("no.es.jwt", CLAVE, AHORA).isEmpty());
    }
}
