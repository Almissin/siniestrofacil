package cl.siniestrofacil.denuncias.service;

import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Pruebas unitarias del formato y correlativo del folio. */
class FolioTest {

    @Test
    void primerFolioDelAnio() {
        assertThat(DenunciaService.siguienteFolio(2026, Optional.empty())).isEqualTo("SF-2026-000001");
    }

    @Test
    void continuaElCorrelativoDelUltimoFolio() {
        assertThat(DenunciaService.siguienteFolio(2026, Optional.of("SF-2026-000041"))).isEqualTo("SF-2026-000042");
    }

    @Test
    void cumpleElFormatoSfAnioSeisDigitos() {
        assertThat(DenunciaService.siguienteFolio(2027, Optional.of("SF-2027-000999")))
                .matches("^SF-\\d{4}-\\d{6}$")
                .isEqualTo("SF-2027-001000");
    }
}
