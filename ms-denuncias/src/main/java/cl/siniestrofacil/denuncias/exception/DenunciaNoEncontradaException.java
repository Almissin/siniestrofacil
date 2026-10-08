package cl.siniestrofacil.denuncias.exception;

/** HTTP 404 - el folio no existe. */
public class DenunciaNoEncontradaException extends RuntimeException {

    public DenunciaNoEncontradaException(String folio) {
        super("no existe una denuncia con folio " + folio);
    }
}
