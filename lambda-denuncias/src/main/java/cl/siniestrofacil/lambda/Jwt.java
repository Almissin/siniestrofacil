package cl.siniestrofacil.lambda;

import com.fasterxml.jackson.databind.ObjectMapper;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

/**
 * crear y validar tokens jwt firmados con hmac compatibles con los de ms-usuarios
 * ms-usuarios firma con jjwt y elige hs384 por el largo de la clave, por eso se aceptan hs256 hs384 y hs512
 */
public final class Jwt {

    public static final String EMISOR = "ms-usuarios";

    private static final ObjectMapper JSON = new ObjectMapper();
    private static final Base64.Encoder B64 = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder B64D = Base64.getUrlDecoder();
    private static final Map<String, String> ALGORITMOS = Map.of(
            "HS256", "HmacSHA256", "HS384", "HmacSHA384", "HS512", "HmacSHA512");

    private Jwt() {
    }

    // crear un token hs384 con los claims indicados que vence en los segundos dados
    public static String crear(Map<String, Object> claims, String secreto, long segundosVigencia, long ahoraSegundos) {
        try {
            Map<String, Object> cabecera = new LinkedHashMap<>();
            cabecera.put("alg", "HS384");
            Map<String, Object> cuerpo = new LinkedHashMap<>(claims);
            cuerpo.put("iss", EMISOR);
            cuerpo.put("iat", ahoraSegundos);
            cuerpo.put("exp", ahoraSegundos + segundosVigencia);
            String datos = B64.encodeToString(JSON.writeValueAsBytes(cabecera)) + "."
                    + B64.encodeToString(JSON.writeValueAsBytes(cuerpo));
            return datos + "." + B64.encodeToString(firmar(datos, secreto, "HmacSHA384"));
        } catch (Exception e) {
            throw new IllegalStateException("no se pudo crear el token", e);
        }
    }

    // validar firma emisor y vencimiento y devolver los claims si el token es valido
    @SuppressWarnings("unchecked")
    public static Optional<Map<String, Object>> validar(String token, String secreto, long ahoraSegundos) {
        try {
            if (token == null) {
                return Optional.empty();
            }
            String[] partes = token.trim().split("\\.");
            if (partes.length != 3) {
                return Optional.empty();
            }
            Map<String, Object> cabecera = JSON.readValue(B64D.decode(partes[0]), Map.class);
            String algoritmo = ALGORITMOS.get(String.valueOf(cabecera.get("alg")));
            if (algoritmo == null) {
                return Optional.empty();
            }
            byte[] esperada = firmar(partes[0] + "." + partes[1], secreto, algoritmo);
            if (!MessageDigest.isEqual(esperada, B64D.decode(partes[2]))) {
                return Optional.empty();
            }
            Map<String, Object> claims = JSON.readValue(B64D.decode(partes[1]), Map.class);
            if (!EMISOR.equals(claims.get("iss"))) {
                return Optional.empty();
            }
            Object exp = claims.get("exp");
            if (!(exp instanceof Number n) || n.longValue() <= ahoraSegundos) {
                return Optional.empty();
            }
            return Optional.of(claims);
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    // calcular la firma hmac de los datos con la clave compartida
    private static byte[] firmar(String datos, String secreto, String algoritmo) throws Exception {
        Mac mac = Mac.getInstance(algoritmo);
        mac.init(new SecretKeySpec(secreto.getBytes(StandardCharsets.UTF_8), algoritmo));
        return mac.doFinal(datos.getBytes(StandardCharsets.UTF_8));
    }
}
