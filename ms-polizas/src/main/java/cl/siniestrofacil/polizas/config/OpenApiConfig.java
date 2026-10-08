package cl.siniestrofacil.polizas.config;

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
        title = "SiniestroFacil - ms-polizas",
        version = "1.0.0",
        description = "Gestion del listado de polizas y verificacion de su vigencia"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Token obtenido en POST /auth/login de ms-usuarios (pegue solo el token, sin la palabra Bearer)")
public class OpenApiConfig {
}
