package cl.siniestrofacil.polizas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;

public record CambiarEstadoRequest(

        @Schema(example = "false", description = "true = activa, false = anulada (deja de estar vigente)")
        @NotNull(message = "el campo activa es obligatorio")
        Boolean activa
) {
}
