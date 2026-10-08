package cl.siniestrofacil.usuarios.security;

import cl.siniestrofacil.usuarios.model.NombreRol;
import cl.siniestrofacil.usuarios.model.Rol;
import cl.siniestrofacil.usuarios.model.Usuario;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Pruebas unitarias del token JWT (sin Spring, sin base de datos). */
class JwtServiceTest {

    private static final String CLAVE = "clave-de-prueba-para-los-tests-de-ms-usuarios-2026";

    private final JwtService jwtService = new JwtService(CLAVE, 3_600_000L);

    @Test
    void generaYValidaTokenConLosDatosDelUsuario() {
        String token = jwtService.generarToken(usuarioTaller());

        Optional<UsuarioAutenticado> resultado = jwtService.validar(token);

        assertThat(resultado).isPresent();
        UsuarioAutenticado u = resultado.get();
        assertThat(u.id()).isEqualTo(7L);
        assertThat(u.email()).isEqualTo("taller@test.cl");
        assertThat(u.rut()).isEqualTo("22222222-2");
        assertThat(u.rol()).isEqualTo("TALLER");
        assertThat(u.tallerId()).isEqualTo(3L);
    }

    @Test
    void rechazaTokenAlterado() {
        String token = jwtService.generarToken(usuarioTaller());

        //assertThat(jwtService.validar(token + "x")).isEmpty();
        // cambiar el ultimo caracter de la firma para alterar el token
        char ultimo = token.charAt(token.length() - 1);
        String alterado = token.substring(0, token.length() - 1) + (ultimo == 'A' ? 'B' : 'A');

        assertThat(jwtService.validar(alterado)).isEmpty();
    }

    @Test
    void rechazaTokenFirmadoConOtraClave() {
        JwtService otro = new JwtService("otra-clave-distinta-de-al-menos-32-caracteres-xyz", 3_600_000L);
        String token = otro.generarToken(usuarioTaller());

        assertThat(jwtService.validar(token)).isEmpty();
    }

    @Test
    void rechazaTokenExpirado() {
        JwtService expirado = new JwtService(CLAVE, -1_000L);
        String token = expirado.generarToken(usuarioTaller());

        assertThat(jwtService.validar(token)).isEmpty();
    }

    private Usuario usuarioTaller() {
        Usuario u = new Usuario();
        u.setId(7L);
        u.setEmail("taller@test.cl");
        u.setRut("22222222-2");
        u.setRol(new Rol(NombreRol.TALLER, "Taller"));
        u.setTallerId(3L);
        return u;
    }
}
