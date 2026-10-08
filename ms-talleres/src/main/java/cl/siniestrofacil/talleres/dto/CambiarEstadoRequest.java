package cl.siniestrofacil.talleres.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequest(

        @Schema(example = "false", description = "true = activo, false = no recibe nuevas asignaciones")
        @NotNull(message = "el campo activo es obligatorio")
        Boolean activo
) {
}
