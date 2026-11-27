package cl.siniestrofacil.lambda;

import java.time.LocalDate;
import java.util.Map;

/**
 * decidir si una denuncia se acepta y a que taller va, o si se rechaza y por que
 * replica las polizas y el taller de demostracion que crean ms-polizas y ms-talleres
 * porque esos microservicios corren en docker local y la lambda en aws no puede llegar a ellos
 */
public final class ValidadorPoliza {

    record Poliza(String patente, LocalDate inicio, LocalDate termino, boolean activa) {
    }

    record Resultado(String estado, String motivo, Long tallerId, String nombreTaller) {

        static Resultado rechazar(String motivo) {
            return new Resultado("RECHAZADA", motivo, null, null);
        }

        static Resultado asignar(long tallerId, String nombreTaller) {
            return new Resultado("TALLER_ASIGNADO", null, tallerId, nombreTaller);
        }
    }

    private static final Map<String, Poliza> POLIZAS = Map.of(
            "POL-001", new Poliza("ABCD12", LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1), true),
            "POL-002", new Poliza("AB1234", LocalDate.of(2024, 6, 1), LocalDate.of(2025, 6, 1), true),
            "POL-003", new Poliza("BCDF34", LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1), false));

    private static final long TALLER_ID = 1L;
    private static final String TALLER_NOMBRE = "Taller Central";

    private ValidadorPoliza() {
    }

    // aplicar las reglas de negocio en el mismo orden que las revisaria un liquidador
    public static Resultado evaluar(String numeroPoliza, String patente, LocalDate fechaSiniestro) {
        Poliza poliza = POLIZAS.get(numeroPoliza);
        if (poliza == null) {
            return Resultado.rechazar("la poliza " + numeroPoliza + " no existe");
        }
        if (!poliza.activa()) {
            return Resultado.rechazar("la poliza " + numeroPoliza + " esta anulada");
        }
        if (!poliza.patente().equalsIgnoreCase(patente)) {
            return Resultado.rechazar("la patente " + patente + " no corresponde a la poliza " + numeroPoliza);
        }
        if (fechaSiniestro.isBefore(poliza.inicio()) || !fechaSiniestro.isBefore(poliza.termino())) {
            return Resultado.rechazar("la poliza " + numeroPoliza + " no estaba vigente el " + fechaSiniestro);
        }
        return Resultado.asignar(TALLER_ID, TALLER_NOMBRE);
    }
}
