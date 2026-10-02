package cl.siniestrofacil.denuncias.exception;

// lanzar cuando no existe una denuncia con el folio buscado
public class DenunciaNoEncontradaException extends RuntimeException {

    public DenunciaNoEncontradaException(String folio) {
        super("no existe una denuncia con folio " + folio);
    }
}
