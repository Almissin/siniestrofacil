package cl.siniestrofacil.denuncias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.util.List;

/**
 * Datos de una nueva denuncia. Version 2: el rut del asegurado ya NO viene en el cuerpo,
 * se toma del token JWT; y se agregan las referencias a fotografias.
 */
public record DenunciaRequest(

        @Schema(example = "ABCD12")
        @NotBlank(message = "la patente es obligatoria")
        @Pattern(regexp = "^[A-Z]{2}\\d{4}$|^[A-Z]{4}\\d{2}$", message = "formato de patente invalido")
        String patente,

        @Schema(example = "POL-001")
        @NotBlank(message = "el numero de poliza es obligatorio")
        String numeroPoliza,

        @Schema(example = "2026-09-28")
        @NotNull(message = "la fecha del siniestro es obligatoria")
        @PastOrPresent(message = "la fecha del siniestro no puede ser futura")
        LocalDate fechaSiniestro,

        @Schema(example = "choque por alcance en semaforo")
        @NotBlank(message = "la descripcion es obligatoria")
        @Size(max = 500, message = "la descripcion no puede superar 500 caracteres")
        String descripcion,

        @Schema(description = "Referencias a las fotografias del siniestro (opcional, maximo 10)")
        @Size(max = 10, message = "no se pueden adjuntar mas de 10 fotografias")
        List<@Valid FotografiaRequest> fotografias
) {
}
