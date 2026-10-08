package cl.siniestrofacil.usuarios.config;

import cl.siniestrofacil.usuarios.model.NombreRol;
import cl.siniestrofacil.usuarios.model.Rol;
import cl.siniestrofacil.usuarios.model.Usuario;
import cl.siniestrofacil.usuarios.repository.RolRepository;
import cl.siniestrofacil.usuarios.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Al iniciar crea los 3 roles y un usuario de demostracion por rol (solo si no existen).
 * Es idempotente: se puede reiniciar el servicio sin duplicar datos.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.datos-demo.habilitado:true}")
    private boolean datosDemo;

    @Override
    @Transactional
    public void run(String... args) {
        Rol admin = crearRol(NombreRol.ADMIN, "Administra usuarios, polizas y talleres");
        Rol asegurado = crearRol(NombreRol.ASEGURADO, "Registra y consulta sus propias denuncias");
        Rol taller = crearRol(NombreRol.TALLER, "Consulta denuncias asignadas y confirma recepcion del vehiculo");

        if (!datosDemo) {
            return;
        }
        crearUsuario("11111111-1", "Administrador", "SiniestroFacil",
                "admin@siniestrofacil.cl", "Admin123!", admin, null);
        crearUsuario("12345678-9", "Ana", "Asegurada",
                "asegurado@siniestrofacil.cl", "Asegurado123!", asegurado, null);
        crearUsuario("22222222-2", "Taller", "Central",
                "taller@siniestrofacil.cl", "Taller123!", taller, 1L);
    }

    private Rol crearRol(NombreRol nombre, String descripcion) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> rolRepository.save(new Rol(nombre, descripcion)));
    }

    private void crearUsuario(String rut, String nombre, String apellido, String email,
                              String password, Rol rol, Long tallerId) {
        if (usuarioRepository.existsByEmail(email)) {
            return;
        }
        Usuario u = new Usuario();
        u.setRut(rut);
        u.setNombre(nombre);
        u.setApellido(apellido);
        u.setEmail(email);
        u.setPassword(passwordEncoder.encode(password));
        u.setRol(rol);
        u.setTallerId(tallerId);
        u.setActivo(true);
        usuarioRepository.save(u);
        log.info("Usuario de demostracion creado: {} ({})", email, rol.getNombre());
    }
}
