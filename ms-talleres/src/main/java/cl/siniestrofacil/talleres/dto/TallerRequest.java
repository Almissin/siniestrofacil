package cl.siniestrofacil.talleres.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/** Datos para crear o actualizar un taller en convenio (solo ADMIN). */
public record TallerRequest(

        @Schema(example = "Taller Los Andes")
        @NotBlank(message = "el nombre es obligatorio")
        @Size(max = 100, message = "el nombre no puede superar 100 caracteres")
        String nombre,

        @Schema(example = "76444444-4")
        @NotBlank(message = "el rut es obligatorio")
        @Pattern(regexp = Validaciones.RUT, message = Validaciones.RUT_MENSAJE)
        String rut,

        @Schema(example = "Av. Vicuna Mackenna 4500")
        @NotBlank(message = "la direccion es obligatoria")
        @Size(max = 150, message = "la direccion no puede superar 150 caracteres")
        String direccion,

        @Schema(example = "San Joaquin")
        @NotBlank(message = "la comuna es obligatoria")
        @Size(max = 60, message = "la comuna no puede superar 60 caracteres")
        String comuna,

        @Schema(example = "+56225554444")
        @Pattern(regexp = Validaciones.TELEFONO, message = Validaciones.TELEFONO_MENSAJE)
        String telefono,

        @Schema(example = "contacto@tallerlosandes.cl")
        @Email(message = "el email no tiene un formato valido")
        @Size(max = 120, message = "el email no puede superar 120 caracteres")
        String email,

        @Schema(example = "4", description = "Vehiculos que puede atender al mismo tiempo")
        @NotNull(message = "el cupo maximo es obligatorio")
        @Min(value = 1, message = "el cupo maximo debe ser al menos 1")
        @Max(value = 50, message = "el cupo maximo no puede superar 50")
        Integer cupoMaximo
) {
}
