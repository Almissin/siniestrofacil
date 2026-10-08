package cl.siniestrofacil.polizas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

/** Datos para crear una poliza (solo ADMIN). */
public record PolizaRequest(

        @Schema(example = "POL-010")
        @NotBlank(message = "el numero de poliza es obligatorio")
        @Pattern(regexp = Validaciones.NUMERO_POLIZA, message = Validaciones.NUMERO_POLIZA_MENSAJE)
        String numero,

        @Schema(example = "12345678-9")
        @NotBlank(message = "el rut del asegurado es obligatorio")
        @Pattern(regexp = Validaciones.RUT, message = Validaciones.RUT_MENSAJE)
        String rutAsegurado,

        @Schema(example = "BCDF34")
        @NotBlank(message = "la patente es obligatoria")
        @Pattern(regexp = Validaciones.PATENTE, message = Validaciones.PATENTE_MENSAJE)
        String patente,

        @Schema(example = "2026-01-01")
        @NotNull(message = "la fecha de inicio es obligatoria")
        LocalDate fechaInicio,

        @Schema(example = "2027-01-01")
        @NotNull(message = "la fecha de termino es obligatoria")
        LocalDate fechaTermino,

        @NotEmpty(message = "la poliza debe tener al menos una cobertura")
        @Size(max = 10, message = "la poliza no puede tener mas de 10 coberturas")
        List<@Valid CoberturaRequest> coberturas
) {
}
