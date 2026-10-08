package cl.siniestrofacil.polizas.exception;

/** HTTP 409 - ya existe un registro con ese dato unico (email o rut). */
public class RecursoDuplicadoException extends RuntimeException {

    public RecursoDuplicadoException(String mensaje) {
        super(mensaje);
    }
}
