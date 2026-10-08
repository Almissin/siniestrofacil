package cl.siniestrofacil.usuarios.exception;

/** HTTP 401 - email o contrasena incorrectos (mensaje generico para no revelar cual fallo). */
public class CredencialesInvalidasException extends RuntimeException {

    public CredencialesInvalidasException() {
        super("email o contrasena incorrectos");
    }
}
