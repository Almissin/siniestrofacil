package cl.siniestrofacil.usuarios.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger: el boton "Authorize" permite pegar el token JWT obtenido en POST /auth/login.
 */
@Configuration
@OpenAPIDefinition(info = @Info(
        title = "SiniestroFacil - ms-usuarios",
        version = "1.0.0",
        description = "Autenticacion con JWT, perfil de usuario y administracion de usuarios y roles"))
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT",
        description = "Pegue solo el token (sin la palabra Bearer)")
public class OpenApiConfig {
}
