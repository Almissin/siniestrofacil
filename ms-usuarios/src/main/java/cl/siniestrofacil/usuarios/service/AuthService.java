package cl.siniestrofacil.usuarios.service;

import cl.siniestrofacil.usuarios.dto.*;
import cl.siniestrofacil.usuarios.exception.CredencialesInvalidasException;
import cl.siniestrofacil.usuarios.exception.UsuarioInactivoException;
import cl.siniestrofacil.usuarios.model.NombreRol;
import cl.siniestrofacil.usuarios.model.Usuario;
import cl.siniestrofacil.usuarios.repository.UsuarioRepository;
import cl.siniestrofacil.usuarios.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Registro publico de asegurados e inicio de sesion (emision del token JWT).
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private static final String TIPO_TOKEN = "Bearer";

    private final UsuarioRepository usuarioRepository;
    private final UsuarioService usuarioService;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    /** El registro publico siempre crea un ASEGURADO: los roles ADMIN y TALLER solo los asigna un ADMIN. */
    @Transactional
    public UsuarioResponse registrar(RegistroRequest req) {
        return usuarioService.crear(new CrearUsuarioRequest(
                req.rut(), req.nombre(), req.apellido(), req.email(),
                req.password(), req.telefono(), NombreRol.ASEGURADO, null));
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest req) {
        Usuario usuario = usuarioRepository.findByEmail(UsuarioService.normalizarEmail(req.email()))
                .orElseThrow(CredencialesInvalidasException::new);

        if (!passwordEncoder.matches(req.password(), usuario.getPassword())) {
            throw new CredencialesInvalidasException();
        }
        if (!usuario.isActivo()) {
            throw new UsuarioInactivoException();
        }
        return new LoginResponse(jwtService.generarToken(usuario), TIPO_TOKEN,
                jwtService.getExpiracionSegundos(), UsuarioResponse.from(usuario));
    }
}
