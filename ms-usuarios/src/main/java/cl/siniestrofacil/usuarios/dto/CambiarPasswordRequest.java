package cl.siniestrofacil.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record CambiarPasswordRequest(

        @Schema(example = "Asegurado123!")
        @NotBlank(message = "la contrasena actual es obligatoria")
        String passwordActual,

        @Schema(example = "NuevaClave2026!")
        @NotBlank(message = "la contrasena nueva es obligatoria")
        @Size(min = 8, max = 60, message = "la contrasena nueva debe tener entre 8 y 60 caracteres")
        String passwordNueva
) {
}
