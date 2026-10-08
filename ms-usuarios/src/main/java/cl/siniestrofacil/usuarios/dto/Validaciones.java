package cl.siniestrofacil.usuarios.dto;

/** Expresiones regulares compartidas por los DTO. */
public final class Validaciones {

    /** RUT chileno sin puntos y con guion: 12345678-9 o 1234567-K. */
    public static final String RUT = "^\\d{7,8}-[\\dkK]$";
    public static final String RUT_MENSAJE = "el rut debe tener formato 12345678-9 (sin puntos y con guion)";

    /** Telefono opcional: 8 a 12 digitos, con + inicial opcional. */
    public static final String TELEFONO = "^\\+?\\d{8,12}$";
    public static final String TELEFONO_MENSAJE = "el telefono debe tener entre 8 y 12 digitos (ej: +56912345678)";

    private Validaciones() {
    }
}
