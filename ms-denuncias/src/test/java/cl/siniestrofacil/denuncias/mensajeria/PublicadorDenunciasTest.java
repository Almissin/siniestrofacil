package cl.siniestrofacil.denuncias.mensajeria;

import cl.siniestrofacil.denuncias.dto.DenunciaResponse;
import cl.siniestrofacil.denuncias.model.EstadoDenuncia;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;

class PublicadorDenunciasTest {

    // crear una denuncia de ejemplo con los campos que viajan en el mensaje
    private static DenunciaResponse denuncia(String poliza) {
        return new DenunciaResponse("SF-2026-000001", "ABCD12", "12345678-9", poliza,
                LocalDate.of(2026, 11, 20), "choque", EstadoDenuncia.RECIBIDA, null,
                List.of(), null, null, null, null, null);
    }

    @Test
    void armaElMensajeConLosDatosParaLaLambda() {
        String json = PublicadorDenuncias.aJson(denuncia("POL-001"));
        assertEquals("{\"folio\":\"SF-2026-000001\",\"patente\":\"ABCD12\",\"numeroPoliza\":\"POL-001\","
                + "\"rutAsegurado\":\"12345678-9\",\"fechaSiniestro\":\"2026-11-20\"}", json);
    }

    @Test
    void escapaLasComillasDelNumeroDePoliza() {
        String json = PublicadorDenuncias.aJson(denuncia("POL\"X"));
        assertEquals(true, json.contains("\"numeroPoliza\":\"POL\\\"X\""));
    }

    @Test
    void sinUrlDeColaNoPublicaNiFalla() {
        PublicadorDenuncias publicador = new PublicadorDenuncias("");
        assertDoesNotThrow(() -> publicador.publicar(denuncia("POL-001")));
    }
}
