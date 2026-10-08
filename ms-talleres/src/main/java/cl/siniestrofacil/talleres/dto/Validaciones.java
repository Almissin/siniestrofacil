package cl.siniestrofacil.talleres.dto;

/** Expresiones regulares compartidas por los DTO. */
public final class Validaciones {

    /** RUT chileno sin puntos y con guion: 76123456-7 o 7612345-K. */
    public static final String RUT = "^\\d{7,8}-[\\dkK]$";
    public static final String RUT_MENSAJE = "el rut debe tener formato 76123456-7 (sin puntos y con guion)";

    /** Telefono opcional: 8 a 12 digitos, con + inicial opcional. */
    public static final String TELEFONO = "^\\+?\\d{8,12}$";
    public static final String TELEFONO_MENSAJE = "el telefono debe tener entre 8 y 12 digitos (ej: +56223456789)";

    /** Folio entregado por ms-denuncias: SF-AAAA-NNNNNN. */
    public static final String FOLIO = "^SF-\\d{4}-\\d{6}$";
    public static final String FOLIO_MENSAJE = "el folio debe tener formato SF-AAAA-NNNNNN";

    private Validaciones() {
    }
}
