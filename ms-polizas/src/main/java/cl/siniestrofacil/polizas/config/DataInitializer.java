package cl.siniestrofacil.polizas.config;

import cl.siniestrofacil.polizas.model.Cobertura;
import cl.siniestrofacil.polizas.model.Poliza;
import cl.siniestrofacil.polizas.repository.PolizaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

/**
 * Si la tabla esta vacia, crea 3 polizas de demostracion para el asegurado 12345678-9
 * (el usuario asegurado@siniestrofacil.cl de ms-usuarios): una vigente, una vencida y una anulada.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final PolizaRepository polizaRepository;

    @Value("${app.datos-demo.habilitado:true}")
    private boolean datosDemo;

    @Override
    @Transactional
    public void run(String... args) {
        if (!datosDemo || polizaRepository.count() > 0) {
            return;
        }
        crear("POL-001", "ABCD12", LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1), true,
                new Cobertura("Danos propios", 15_000_000L),
                new Cobertura("Responsabilidad civil", 10_000_000L),
                new Cobertura("Robo", 12_000_000L));
        crear("POL-002", "AB1234", LocalDate.of(2024, 6, 1), LocalDate.of(2025, 6, 1), true,
                new Cobertura("Responsabilidad civil", 8_000_000L));
        crear("POL-003", "BCDF34", LocalDate.of(2026, 1, 1), LocalDate.of(2027, 1, 1), false,
                new Cobertura("Danos propios", 9_000_000L));
    }

    private void crear(String numero, String patente, LocalDate inicio, LocalDate termino,
                       boolean activa, Cobertura... coberturas) {
        Poliza p = new Poliza();
        p.setNumero(numero);
        p.setRutAsegurado("12345678-9");
        p.setPatente(patente);
        p.setFechaInicio(inicio);
        p.setFechaTermino(termino);
        p.setActiva(activa);
        for (Cobertura c : coberturas) {
            p.agregarCobertura(c);
        }
        polizaRepository.save(p);
        log.info("Poliza de demostracion creada: {} (activa={}, termino={})", numero, activa, termino);
    }
}
