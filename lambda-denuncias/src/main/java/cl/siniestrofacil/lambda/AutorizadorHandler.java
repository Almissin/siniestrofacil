package cl.siniestrofacil.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * autorizador lambda del http api gateway con respuestas simples
 * revisa el jwt antes de que la peticion llegue a ms-denuncias y la rechaza si falta o no es valido
 */
public class AutorizadorHandler implements RequestHandler<Map<String, Object>, Map<String, Object>> {

    private static final String PREFIJO = "Bearer ";

    private final String secreto;

    public AutorizadorHandler() {
        this(Config.jwtSecret());
    }

    AutorizadorHandler(String secreto) {
        this.secreto = secreto;
    }

    @Override
    public Map<String, Object> handleRequest(Map<String, Object> evento, Context contexto) {
        String token = extraerToken(evento);
        Optional<Map<String, Object>> claims = Jwt.validar(token, secreto, System.currentTimeMillis() / 1000);

        Map<String, Object> respuesta = new LinkedHashMap<>();
        respuesta.put("isAuthorized", claims.isPresent());
        if (claims.isPresent()) {
            // pasar el usuario y el rol al backend como contexto del autorizador
            respuesta.put("context", Map.of(
                    "usuario", String.valueOf(claims.get().get("sub")),
                    "rol", String.valueOf(claims.get().get("rol"))));
            log(contexto, "[AUTORIZADOR] acceso permitido a " + claims.get().get("sub") + " rol " + claims.get().get("rol"));
        } else {
            log(contexto, "[AUTORIZADOR] acceso denegado, token ausente invalido o vencido");
        }
        return respuesta;
    }

    // leer el header authorization del evento payload 2.0 de api gateway
    @SuppressWarnings("unchecked")
    static String extraerToken(Map<String, Object> evento) {
        Object headers = evento == null ? null : evento.get("headers");
        if (!(headers instanceof Map<?, ?> mapa)) {
            return null;
        }
        Object valor = null;
        for (Map.Entry<?, ?> e : ((Map<Object, Object>) mapa).entrySet()) {
            if ("authorization".equalsIgnoreCase(String.valueOf(e.getKey()))) {
                valor = e.getValue();
            }
        }
        if (valor == null) {
            return null;
        }
        String texto = valor.toString().trim();
        return texto.startsWith(PREFIJO) ? texto.substring(PREFIJO.length()).trim() : texto;
    }

    private static void log(Context contexto, String mensaje) {
        if (contexto != null && contexto.getLogger() != null) {
            contexto.getLogger().log(mensaje + "\n");
        } else {
            System.out.println(mensaje);
        }
    }
}
