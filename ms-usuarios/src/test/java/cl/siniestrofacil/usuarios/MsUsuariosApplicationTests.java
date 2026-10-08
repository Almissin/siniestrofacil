package cl.siniestrofacil.usuarios;

import cl.siniestrofacil.usuarios.repository.RolRepository;
import cl.siniestrofacil.usuarios.repository.UsuarioRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.assertj.core.api.Assertions.assertThat;

/** Levanta el contexto completo con H2 en memoria (no necesita MySQL). */
@SpringBootTest
class MsUsuariosApplicationTests {

    @Autowired
    private RolRepository rolRepository;

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Test
    void contextoLevantaYCreaDatosIniciales() {
        assertThat(rolRepository.count()).isEqualTo(3);
        assertThat(usuarioRepository.existsByEmail("admin@siniestrofacil.cl")).isTrue();
    }
}
