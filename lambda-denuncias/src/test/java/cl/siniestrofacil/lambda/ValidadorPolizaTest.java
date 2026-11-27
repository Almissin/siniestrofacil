package cl.siniestrofacil.lambda;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ValidadorPolizaTest {

    private static final LocalDate FECHA = LocalDate.of(2026, 11, 20);

    @Test
    void polizaVigenteAsignaElTallerCentral() {
        var r = ValidadorPoliza.evaluar("POL-001", "ABCD12", FECHA);
        assertEquals("TALLER_ASIGNADO", r.estado());
        assertEquals(1L, r.tallerId());
        assertEquals("Taller Central", r.nombreTaller());
    }

    @Test
    void polizaVencidaSeRechaza() {
        var r = ValidadorPoliza.evaluar("POL-002", "AB1234", FECHA);
        assertEquals("RECHAZADA", r.estado());
        assertTrue(r.motivo().contains("no estaba vigente"));
    }

    @Test
    void polizaAnuladaSeRechaza() {
        assertTrue(ValidadorPoliza.evaluar("POL-003", "BCDF34", FECHA).motivo().contains("anulada"));
    }

    @Test
    void polizaInexistenteOPatenteDistintaSeRechazan() {
        assertTrue(ValidadorPoliza.evaluar("POL-999", "ABCD12", FECHA).motivo().contains("no existe"));
        assertTrue(ValidadorPoliza.evaluar("POL-001", "ZZZZ99", FECHA).motivo().contains("no corresponde"));
    }
}
