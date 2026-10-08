package cl.siniestrofacil.usuarios.dto;

import cl.siniestrofacil.usuarios.model.NombreRol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/** Datos que un ADMIN puede modificar de cualquier usuario. */
public record ActualizarUsuarioRequest(

        @Schema(example = "Taller")
        @NotBlank(message = "el nombre es obligatorio")
        @Size(max = 80, message = "el nombre no puede superar 80 caracteres")
        String nombre,

        @Schema(example = "Automotriz Norte Ltda")
        @NotBlank(message = "el apellido es obligatorio")
        @Size(max = 80, message = "el apellido no puede superar 80 caracteres")
        String apellido,

        @Schema(example = "+56223456789")
        @Pattern(regexp = Validaciones.TELEFONO, message = Validaciones.TELEFONO_MENSAJE)
        String telefono,

        @Schema(example = "TALLER")
        @NotNull(message = "el rol es obligatorio (ADMIN, ASEGURADO o TALLER)")
        NombreRol rol,

        @Schema(example = "2", description = "Obligatorio solo para el rol TALLER")
        @Positive(message = "el tallerId debe ser positivo")
        Long tallerId
) {
}
