package cl.siniestrofacil.talleres.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

/** Pide asignar un taller disponible a una denuncia con poliza vigente. */
public record AsignacionRequest(

        @Schema(example = "SF-2026-000001")
        @NotBlank(message = "el folio de la denuncia es obligatorio")
        @Pattern(regexp = Validaciones.FOLIO, message = Validaciones.FOLIO_MENSAJE)
        String folioDenuncia
) {
}
