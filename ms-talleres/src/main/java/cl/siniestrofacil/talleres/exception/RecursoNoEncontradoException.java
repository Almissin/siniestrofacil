package cl.siniestrofacil.talleres.exception;

/** HTTP 404 - el recurso solicitado no existe. */
public class RecursoNoEncontradoException extends RuntimeException {

    public RecursoNoEncontradoException(String mensaje) {
        super(mensaje);
    }
}
