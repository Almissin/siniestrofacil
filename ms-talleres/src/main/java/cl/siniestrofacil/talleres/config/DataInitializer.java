package cl.siniestrofacil.talleres.config;

import cl.siniestrofacil.talleres.model.Taller;
import cl.siniestrofacil.talleres.repository.TallerRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Si la tabla esta vacia, crea 2 talleres de demostracion.
 * Taller Central queda con id 1: es el taller del usuario taller@siniestrofacil.cl de ms-usuarios.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final TallerRepository tallerRepository;

    @Value("${app.datos-demo.habilitado:true}")
    private boolean datosDemo;

    @Override
    @Transactional
    public void run(String... args) {
        if (!datosDemo || tallerRepository.count() > 0) {
            return;
        }
        crear("Taller Central", "76111111-1", "Av. Providencia 1234", "Providencia",
                "+56222223333", "contacto@tallercentral.cl", 3);
        crear("Automotriz Norte", "76222222-2", "Av. Independencia 2500", "Independencia",
                "+56224445555", "contacto@automotriznorte.cl", 2);
    }

    private void crear(String nombre, String rut, String direccion, String comuna,
                       String telefono, String email, int cupo) {
        Taller t = new Taller();
        t.setNombre(nombre);
        t.setRut(rut);
        t.setDireccion(direccion);
        t.setComuna(comuna);
        t.setTelefono(telefono);
        t.setEmail(email);
        t.setCupoMaximo(cupo);
        t.setActivo(true);
        tallerRepository.save(t);
        log.info("Taller de demostracion creado: {} (cupo {})", nombre, cupo);
    }
}
