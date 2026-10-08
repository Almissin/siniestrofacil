package cl.siniestrofacil.usuarios.exception;

/** HTTP 403 - la cuenta existe pero fue desactivada por un administrador. */
public class UsuarioInactivoException extends RuntimeException {

    public UsuarioInactivoException() {
        super("la cuenta se encuentra desactivada, contacte al administrador");
    }
}
