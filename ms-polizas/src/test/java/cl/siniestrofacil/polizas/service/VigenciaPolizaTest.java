package cl.siniestrofacil.polizas.service;

import cl.siniestrofacil.polizas.model.Poliza;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;

/** Pruebas unitarias de la regla de vigencia (sin Spring ni base de datos). */
class VigenciaPolizaTest {

    private static final LocalDate INICIO = LocalDate.of(2026, 1, 1);
    private static final LocalDate TERMINO = LocalDate.of(2027, 1, 1);

    @Test
    void polizaActivaDentroDelPeriodoEstaVigente() {
        ResultadoVigencia r = PolizaService.evaluarVigencia(poliza(true), LocalDate.of(2026, 10, 5));

        assertThat(r.vigente()).isTrue();
        assertThat(r.motivo()).contains("vigente hasta el 01-01-2027");
    }

    @Test
    void elPrimerYUltimoDiaTambienSonVigentes() {
        assertThat(PolizaService.evaluarVigencia(poliza(true), INICIO).vigente()).isTrue();
        assertThat(PolizaService.evaluarVigencia(poliza(true), TERMINO).vigente()).isTrue();
    }

    @Test
    void polizaVencidaNoEstaVigente() {
        ResultadoVigencia r = PolizaService.evaluarVigencia(poliza(true), TERMINO.plusDays(1));

        assertThat(r.vigente()).isFalse();
        assertThat(r.motivo()).contains("vencio");
    }

    @Test
    void polizaQueAunNoComienzaNoEstaVigente() {
        ResultadoVigencia r = PolizaService.evaluarVigencia(poliza(true), INICIO.minusDays(1));

        assertThat(r.vigente()).isFalse();
        assertThat(r.motivo()).contains("aun no inicia");
    }

    @Test
    void polizaAnuladaNoEstaVigenteAunqueLasFechasCalcen() {
        ResultadoVigencia r = PolizaService.evaluarVigencia(poliza(false), LocalDate.of(2026, 10, 5));

        assertThat(r.vigente()).isFalse();
        assertThat(r.motivo()).contains("anulada");
    }

    private Poliza poliza(boolean activa) {
        Poliza p = new Poliza();
        p.setNumero("POL-100");
        p.setFechaInicio(INICIO);
        p.setFechaTermino(TERMINO);
        p.setActiva(activa);
        return p;
    }
}
