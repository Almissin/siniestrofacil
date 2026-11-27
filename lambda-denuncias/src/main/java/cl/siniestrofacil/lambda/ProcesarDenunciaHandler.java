package cl.siniestrofacil.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * lambda consumidora de la cola sqs siniestrofacil-denuncias
 * por cada denuncia valida la poliza asigna el taller o rechaza y guarda el resultado en ms-denuncias
 * si algo falla lanza excepcion y sqs reintenta el mensaje hasta enviarlo a la cola de errores
 */
public class ProcesarDenunciaHandler implements RequestHandler<Map<String, Object>, String> {

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final HttpClient HTTP = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(5)).build();

    @Override
    @SuppressWarnings("unchecked")
    public String handleRequest(Map<String, Object> evento, Context contexto) {
        List<Map<String, Object>> registros = (List<Map<String, Object>>) evento.getOrDefault("Records", List.of());
        for (Map<String, Object> registro : registros) {
            procesar(String.valueOf(registro.get("body")), contexto);
        }
        return "procesados " + registros.size();
    }

    // procesar un mensaje de la cola con los datos de una denuncia
    private void procesar(String cuerpo, Context contexto) {
        try {
            JsonNode d = JSON.readTree(cuerpo);
            String folio = d.path("folio").asText();
            String poliza = d.path("numeroPoliza").asText();
            String patente = d.path("patente").asText();
            LocalDate fecha = LocalDate.parse(d.path("fechaSiniestro").asText());
            log(contexto, "[CONSUMIDOR] mensaje recibido desde sqs folio " + folio + " poliza " + poliza + " patente " + patente);

            ValidadorPoliza.Resultado r = ValidadorPoliza.evaluar(poliza, patente, fecha);
            log(contexto, "[CONSUMIDOR] folio " + folio + " resultado " + r.estado()
                    + (r.motivo() != null ? " motivo " + r.motivo() : " taller " + r.nombreTaller()));

            int codigo = enviarResultado(folio, r);
            if (codigo == 200) {
                log(contexto, "[CONSUMIDOR] folio " + folio + " actualizado en ms-denuncias");
            } else if (codigo == 409) {
                log(contexto, "[CONSUMIDOR] folio " + folio + " ya estaba procesado, se descarta el mensaje repetido");
            } else {
                throw new IllegalStateException("ms-denuncias respondio " + codigo + " para el folio " + folio);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new IllegalStateException("no se pudo procesar el mensaje " + cuerpo, e);
        }
    }

    // llamar a patch denuncias folio resultado con un token de administrador de corta duracion
    private int enviarResultado(String folio, ValidadorPoliza.Resultado r) throws Exception {
        Map<String, Object> cuerpo = new LinkedHashMap<>();
        cuerpo.put("estado", r.estado());
        cuerpo.put("motivo", r.motivo());
        cuerpo.put("tallerId", r.tallerId());
        cuerpo.put("nombreTaller", r.nombreTaller());

        String token = Jwt.crear(Map.of("sub", "lambda-procesar-denuncia", "uid", 0, "rol", "ADMIN"),
                Config.jwtSecret(), 300, System.currentTimeMillis() / 1000);

        HttpRequest peticion = HttpRequest.newBuilder()
                .uri(URI.create(Config.apiUrl() + "/denuncias/" + folio + "/resultado"))
                .timeout(Duration.ofSeconds(25))
                .header("Content-Type", "application/json")
                .header("Authorization", "Bearer " + token)
                .method("PATCH", HttpRequest.BodyPublishers.ofString(JSON.writeValueAsString(cuerpo)))
                .build();
        return HTTP.send(peticion, HttpResponse.BodyHandlers.ofString()).statusCode();
    }

    private static void log(Context contexto, String mensaje) {
        if (contexto != null && contexto.getLogger() != null) {
            contexto.getLogger().log(mensaje + "\n");
        } else {
            System.out.println(mensaje);
        }
    }
}
