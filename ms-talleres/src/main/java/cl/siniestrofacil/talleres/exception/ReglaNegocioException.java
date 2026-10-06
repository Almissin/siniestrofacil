package cl.siniestrofacil.talleres.exception;

/** HTTP 400 - la operacion no cumple una regla de negocio. */
public class ReglaNegocioException extends RuntimeException {

    public ReglaNegocioException(String mensaje) {
        super(mensaje);
    }
}
