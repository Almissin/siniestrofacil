package cl.siniestrofacil.talleres;

import cl.siniestrofacil.talleres.repository.TallerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/** Levanta el contexto completo con H2 en memoria (no necesita MySQL). */
@SpringBootTest
class MsTalleresApplicationTests {

    @Autowired
    private TallerRepository tallerRepository;

    @Test
    void contextoLevantaYCreaTalleresDeDemostracion() {
        assertThat(tallerRepository.count()).isEqualTo(2);
        assertThat(tallerRepository.existsByNombreIgnoreCase("Taller Central")).isTrue();
    }
}
