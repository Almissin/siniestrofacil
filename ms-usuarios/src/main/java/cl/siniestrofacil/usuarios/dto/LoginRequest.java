package cl.siniestrofacil.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(

        @Schema(example = "admin@siniestrofacil.cl")
        @NotBlank(message = "el email es obligatorio")
        @Email(message = "el email no tiene un formato valido")
        String email,

        @Schema(example = "Admin123!")
        @NotBlank(message = "la contrasena es obligatoria")
        String password
) {
}
