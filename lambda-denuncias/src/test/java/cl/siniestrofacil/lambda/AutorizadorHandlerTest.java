package cl.siniestrofacil.lambda;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class AutorizadorHandlerTest {

    private final AutorizadorHandler autorizador = new AutorizadorHandler(Config.CLAVE_POR_DEFECTO);

    @Test
    void permiteUnTokenValido() {
        String token = Jwt.crear(Map.of("sub", "asegurado@siniestrofacil.cl", "rol", "ASEGURADO"),
                Config.CLAVE_POR_DEFECTO, 300, System.currentTimeMillis() / 1000);
        var respuesta = autorizador.handleRequest(Map.of("headers", Map.of("authorization", "Bearer " + token)), null);
        assertEquals(true, respuesta.get("isAuthorized"));
    }

    @Test
    void niegaSinHeaderOConTokenFalso() {
        assertEquals(false, autorizador.handleRequest(Map.of("headers", Map.of()), null).get("isAuthorized"));
        assertEquals(false, autorizador.handleRequest(
                Map.of("headers", Map.of("authorization", "Bearer abc.def.ghi")), null).get("isAuthorized"));
    }
}
