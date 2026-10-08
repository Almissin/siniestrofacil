package cl.siniestrofacil.polizas;

import cl.siniestrofacil.polizas.repository.PolizaRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/** Levanta el contexto completo con H2 en memoria (no necesita MySQL). */
@SpringBootTest
class MsPolizasApplicationTests {

    @Autowired
    private PolizaRepository polizaRepository;

    @Test
    void contextoLevantaYCreaPolizasDeDemostracion() {
        assertThat(polizaRepository.count()).isEqualTo(3);
        assertThat(polizaRepository.findByNumero("POL-001"))
                .hasValueSatisfying(p -> assertThat(p.getCoberturas()).hasSize(3));
    }
}
