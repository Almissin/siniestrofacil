package cl.siniestrofacil.denuncias.exception;

/** HTTP 409 - la operacion choca con el estado actual (sin cupo, ya finalizada, taller con asignaciones). */
public class ConflictoException extends RuntimeException {

    public ConflictoException(String mensaje) {
        super(mensaje);
    }
}
