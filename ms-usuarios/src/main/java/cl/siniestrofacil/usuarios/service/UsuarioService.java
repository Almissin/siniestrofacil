package cl.siniestrofacil.usuarios.service;

import cl.siniestrofacil.usuarios.dto.*;
import cl.siniestrofacil.usuarios.exception.RecursoDuplicadoException;
import cl.siniestrofacil.usuarios.exception.RecursoNoEncontradoException;
import cl.siniestrofacil.usuarios.exception.ReglaNegocioException;
import cl.siniestrofacil.usuarios.model.NombreRol;
import cl.siniestrofacil.usuarios.model.Rol;
import cl.siniestrofacil.usuarios.model.Usuario;
import cl.siniestrofacil.usuarios.repository.RolRepository;
import cl.siniestrofacil.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Locale;

/**
 * Logica de negocio de usuarios: perfil propio y administracion (ADMIN).
 */
@Service
@RequiredArgsConstructor
public class UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    // ======================= PERFIL PROPIO =======================

    @Transactional(readOnly = true)
    public UsuarioResponse obtenerPerfil(Long idUsuario) {
        return UsuarioResponse.from(buscarUsuario(idUsuario));
    }

    @Transactional
    public UsuarioResponse actualizarPerfil(Long idUsuario, ActualizarPerfilRequest req) {
        Usuario usuario = buscarUsuario(idUsuario);
        usuario.setNombre(req.nombre().trim());
        usuario.setApellido(req.apellido().trim());
        usuario.setTelefono(req.telefono());
        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public void cambiarPassword(Long idUsuario, CambiarPasswordRequest req) {
        Usuario usuario = buscarUsuario(idUsuario);
        if (!passwordEncoder.matches(req.passwordActual(), usuario.getPassword())) {
            throw new ReglaNegocioException("la contrasena actual no es correcta");
        }
        if (req.passwordActual().equals(req.passwordNueva())) {
            throw new ReglaNegocioException("la contrasena nueva debe ser distinta a la actual");
        }
        usuario.setPassword(passwordEncoder.encode(req.passwordNueva()));
    }

    // ======================= ADMINISTRACION =======================

    @Transactional(readOnly = true)
    public List<UsuarioResponse> listar(NombreRol rol) {
        List<Usuario> usuarios = (rol == null)
                ? usuarioRepository.findAllByOrderByIdAsc()
                : usuarioRepository.findByRolNombreOrderByIdAsc(rol);
        return usuarios.stream().map(UsuarioResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public UsuarioResponse obtener(Long id) {
        return UsuarioResponse.from(buscarUsuario(id));
    }

    /** Crea un usuario con cualquier rol. Tambien lo usa el registro publico (siempre con rol ASEGURADO). */
    @Transactional
    public UsuarioResponse crear(CrearUsuarioRequest req) {
        String email = normalizarEmail(req.email());
        String rut = normalizarRut(req.rut());

        if (usuarioRepository.existsByEmail(email)) {
            throw new RecursoDuplicadoException("ya existe un usuario con el email " + email);
        }
        if (usuarioRepository.existsByRut(rut)) {
            throw new RecursoDuplicadoException("ya existe un usuario con el rut " + rut);
        }
        validarTaller(req.rol(), req.tallerId());

        Usuario usuario = new Usuario();
        usuario.setRut(rut);
        usuario.setNombre(req.nombre().trim());
        usuario.setApellido(req.apellido().trim());
        usuario.setEmail(email);
        usuario.setPassword(passwordEncoder.encode(req.password()));
        usuario.setTelefono(req.telefono());
        usuario.setRol(buscarRol(req.rol()));
        usuario.setTallerId(req.tallerId());
        usuario.setActivo(true);
        return UsuarioResponse.from(usuarioRepository.save(usuario));
    }

    @Transactional
    public UsuarioResponse actualizar(Long id, ActualizarUsuarioRequest req, Long idAdminActual) {
        Usuario usuario = buscarUsuario(id);
        if (id.equals(idAdminActual) && req.rol() != NombreRol.ADMIN) {
            throw new ReglaNegocioException("no puede quitarse a si mismo el rol ADMIN");
        }
        validarTaller(req.rol(), req.tallerId());

        usuario.setNombre(req.nombre().trim());
        usuario.setApellido(req.apellido().trim());
        usuario.setTelefono(req.telefono());
        usuario.setRol(buscarRol(req.rol()));
        usuario.setTallerId(req.tallerId());
        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public UsuarioResponse cambiarEstado(Long id, boolean activo, Long idAdminActual) {
        if (id.equals(idAdminActual) && !activo) {
            throw new ReglaNegocioException("no puede desactivar su propia cuenta");
        }
        Usuario usuario = buscarUsuario(id);
        usuario.setActivo(activo);
        return UsuarioResponse.from(usuario);
    }

    @Transactional
    public void eliminar(Long id, Long idAdminActual) {
        if (id.equals(idAdminActual)) {
            throw new ReglaNegocioException("no puede eliminar su propia cuenta");
        }
        usuarioRepository.delete(buscarUsuario(id));
    }

    @Transactional(readOnly = true)
    public List<RolResponse> listarRoles() {
        return rolRepository.findAll().stream().map(RolResponse::from).toList();
    }

    // ======================= APOYO =======================

    private Usuario buscarUsuario(Long id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new RecursoNoEncontradoException("no existe un usuario con id " + id));
    }

    private Rol buscarRol(NombreRol nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseThrow(() -> new RecursoNoEncontradoException("no existe el rol " + nombre));
    }

    /** Un usuario TALLER debe estar asociado a un taller; los demas roles no. */
    private void validarTaller(NombreRol rol, Long tallerId) {
        if (rol == NombreRol.TALLER && tallerId == null) {
            throw new ReglaNegocioException("un usuario con rol TALLER debe indicar el tallerId");
        }
        if (rol != NombreRol.TALLER && tallerId != null) {
            throw new ReglaNegocioException("solo los usuarios con rol TALLER pueden tener tallerId");
        }
    }

    static String normalizarEmail(String email) {
        return email.trim().toLowerCase(Locale.ROOT);
    }

    static String normalizarRut(String rut) {
        return rut.trim().toUpperCase(Locale.ROOT);
    }
}
