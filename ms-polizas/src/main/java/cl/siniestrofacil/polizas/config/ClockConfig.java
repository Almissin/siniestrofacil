package cl.siniestrofacil.polizas.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/** Reloj con zona horaria de Chile: la vigencia se evalua con la fecha local de Santiago. */
@Configuration
public class ClockConfig {

    @Bean
    public Clock clock() {
        return Clock.system(ZoneId.of("America/Santiago"));
    }
}
