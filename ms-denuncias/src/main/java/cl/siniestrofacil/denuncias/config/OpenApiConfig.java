package cl.siniestrofacil.denuncias.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger: el boton "Authorize" permite pegar el token JWT obtenido en POST /auth/login de ms-usuarios.
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "SiniestroFacil - ms-denuncias",
        version = "2.0.0",
        description = "Registro y consulta de denuncias de siniestros vehiculares, resultado del procesamiento y recepcion del vehiculo"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token obtenido en POST /auth/login de ms-usuarios (pegue solo el token, sin la palabra Bearer)")
public class OpenApiConfig {
}
