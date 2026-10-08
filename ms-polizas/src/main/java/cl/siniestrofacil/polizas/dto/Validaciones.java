package cl.siniestrofacil.polizas.dto;

/** Expresiones regulares compartidas por los DTO. */
public final class Validaciones {

    /** Numero de poliza: POL- seguido de 3 a 6 digitos (POL-001). */
    public static final String NUMERO_POLIZA = "^POL-\\d{3,6}$";
    public static final String NUMERO_POLIZA_MENSAJE = "el numero de poliza debe tener formato POL-001";

    /** RUT chileno sin puntos y con guion. */
    public static final String RUT = "^\\d{7,8}-[\\dkK]$";
    public static final String RUT_MENSAJE = "el rut debe tener formato 12345678-9 (sin puntos y con guion)";

    /** Patente chilena: AA1234 (antigua) o ABCD12 (nueva), solo mayusculas. Igual que en ms-denuncias. */
    public static final String PATENTE = "^[A-Z]{2}\\d{4}$|^[A-Z]{4}\\d{2}$";
    public static final String PATENTE_MENSAJE = "formato de patente invalido";

    private Validaciones() {
    }
}
