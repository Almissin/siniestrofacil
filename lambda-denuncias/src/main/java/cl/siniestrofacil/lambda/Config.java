package cl.siniestrofacil.lambda;

/**
 * leer la configuracion de las lambdas desde variables de entorno
 */
final class Config {

    static final String CLAVE_POR_DEFECTO = "siniestrofacil-clave-secreta-desarrollo-jvy0101-2026";

    private Config() {
    }

    // usar la misma clave jwt que ms-usuarios y ms-denuncias
    static String jwtSecret() {
        String valor = System.getenv("JWT_SECRET");
        return valor == null || valor.isBlank() ? CLAVE_POR_DEFECTO : valor;
    }

    // obtener la url base del api gateway donde esta ms-denuncias
    static String apiUrl() {
        String valor = System.getenv("API_URL");
        if (valor == null || valor.isBlank()) {
            throw new IllegalStateException("falta la variable de entorno API_URL");
        }
        return valor.endsWith("/") ? valor.substring(0, valor.length() - 1) : valor;
    }
}
