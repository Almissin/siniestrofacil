package cl.siniestrofacil.denuncias.dto;

import cl.siniestrofacil.denuncias.model.EstadoDenuncia;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

/**
 * Resultado del procesamiento asincrono (validar poliza + asignar taller).
 * En la EP3 lo enviara la AWS Lambda; en la EP2 se envia desde Postman con el token del ADMIN.
 * RECHAZADA requiere motivo. TALLER_ASIGNADO requiere tallerId y nombreTaller.
 */
public record ResultadoRequest(

        @Schema(example = "TALLER_ASIGNADO", allowableValues = {"RECHAZADA", "TALLER_ASIGNADO"})
        @NotNull(message = "el estado es obligatorio (RECHAZADA o TALLER_ASIGNADO)")
        EstadoDenuncia estado,

        @Schema(example = "la poliza vencio el 01-06-2025", description = "Obligatorio si el estado es RECHAZADA")
        @Size(max = 255, message = "el motivo no puede superar 255 caracteres")
        String motivo,

        @Schema(example = "1", description = "Obligatorio si el estado es TALLER_ASIGNADO")
        @Positive(message = "el tallerId debe ser positivo")
        Long tallerId,

        @Schema(example = "Taller Central", description = "Obligatorio si el estado es TALLER_ASIGNADO")
        @Size(max = 100, message = "el nombre del taller no puede superar 100 caracteres")
        String nombreTaller
) {
}
