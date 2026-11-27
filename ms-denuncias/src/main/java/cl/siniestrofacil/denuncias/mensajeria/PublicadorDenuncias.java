package cl.siniestrofacil.denuncias.mensajeria;

import cl.siniestrofacil.denuncias.dto.DenunciaResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import software.amazon.awssdk.services.sqs.SqsClient;
import software.amazon.awssdk.services.sqs.model.SendMessageRequest;

/**
 * publicar cada denuncia recien registrada en la cola sqs de procesamiento
 * la lambda procesarDenuncia lee la cola para validar la poliza y asignar el taller
 * sin url configurada no publica nada, asi funciona igual en local y en docker swarm
 */
@Component
public class PublicadorDenuncias {

    private static final Logger log = LoggerFactory.getLogger(PublicadorDenuncias.class);

    private final String urlCola;
    private SqsClient clienteSqs;

    public PublicadorDenuncias(@Value("${sqs.denuncias.url:}") String urlCola) {
        this.urlCola = urlCola;
    }

    // enviar la denuncia a la cola sin bloquear la respuesta al asegurado si sqs falla
    public void publicar(DenunciaResponse denuncia) {
        if (urlCola == null || urlCola.isBlank()) {
            log.info("[PRODUCTOR] cola sqs no configurada, se omite la publicacion del folio {}", denuncia.folio());
            return;
        }
        String mensaje = aJson(denuncia);
        try {
            String idMensaje = cliente().sendMessage(SendMessageRequest.builder()
                            .queueUrl(urlCola)
                            .messageBody(mensaje)
                            .build())
                    .messageId();
            log.info("[PRODUCTOR] folio {} publicado en sqs con messageId {}", denuncia.folio(), idMensaje);
        } catch (RuntimeException e) {
            log.error("[PRODUCTOR] no se pudo publicar el folio {} en sqs", denuncia.folio(), e);
        }
    }

    // crear el cliente una sola vez y solo cuando se necesita
    private synchronized SqsClient cliente() {
        if (clienteSqs == null) {
            clienteSqs = SqsClient.create();
        }
        return clienteSqs;
    }

    // armar el mensaje con los datos que necesita la lambda para validar la poliza
    static String aJson(DenunciaResponse d) {
        return "{"
                + "\"folio\":\"" + escapar(d.folio()) + "\","
                + "\"patente\":\"" + escapar(d.patente()) + "\","
                + "\"numeroPoliza\":\"" + escapar(d.numeroPoliza()) + "\","
                + "\"rutAsegurado\":\"" + escapar(d.rutAsegurado()) + "\","
                + "\"fechaSiniestro\":\"" + d.fechaSiniestro() + "\""
                + "}";
    }

    // escapar comillas y barras para que el json sea valido
    private static String escapar(String texto) {
        if (texto == null) {
            return "";
        }
        return texto.replace("\\", "\\\\").replace("\"", "\\\"");
    }
}
