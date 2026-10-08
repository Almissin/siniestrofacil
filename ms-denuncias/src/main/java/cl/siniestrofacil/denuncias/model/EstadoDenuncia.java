package cl.siniestrofacil.denuncias.model;

/**
 * Ciclo de vida de una denuncia:
 * RECIBIDA -> RECHAZADA (poliza no vigente: no continua a la asignacion)
 * RECIBIDA -> TALLER_ASIGNADO -> VEHICULO_RECIBIDO
 */
public enum EstadoDenuncia {
    RECIBIDA,
    RECHAZADA,
    TALLER_ASIGNADO,
    VEHICULO_RECIBIDO;

    /** Solo una denuncia recien recibida puede registrar el resultado de la validacion. */
    public boolean admiteResultado() {
        return this == RECIBIDA;
    }

    /** Solo una denuncia con taller asignado puede confirmar la recepcion del vehiculo. */
    public boolean admiteRecepcion() {
        return this == TALLER_ASIGNADO;
    }
}
