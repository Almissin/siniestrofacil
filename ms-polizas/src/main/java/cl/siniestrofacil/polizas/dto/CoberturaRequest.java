package cl.siniestrofacil.polizas.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;

public record CoberturaRequest(

        @Schema(example = "Danos propios")
        @NotBlank(message = "el nombre de la cobertura es obligatorio")
        @Size(max = 80, message = "el nombre de la cobertura no puede superar 80 caracteres")
        String nombre,

        @Schema(example = "15000000", description = "Monto maximo cubierto en pesos chilenos")
        @NotNull(message = "el monto maximo es obligatorio")
        @Positive(message = "el monto maximo debe ser mayor a 0")
        Long montoMaximo
) {
}
