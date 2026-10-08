package cl.siniestrofacil.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos para que un asegurado cree su propia cuenta. */
public record RegistroRequest(

        @Schema(example = "15678234-K")
        @NotBlank(message = "el rut es obligatorio")
        @Pattern(regexp = Validaciones.RUT, message = Validaciones.RUT_MENSAJE)
        String rut,

        @Schema(example = "Camila")
        @NotBlank(message = "el nombre es obligatorio")
        @Size(max = 80, message = "el nombre no puede superar 80 caracteres")
        String nombre,

        @Schema(example = "Rojas")
        @NotBlank(message = "el apellido es obligatorio")
        @Size(max = 80, message = "el apellido no puede superar 80 caracteres")
        String apellido,

        @Schema(example = "camila.rojas@correo.cl")
        @NotBlank(message = "el email es obligatorio")
        @Email(message = "el email no tiene un formato valido")
        @Size(max = 120, message = "el email no puede superar 120 caracteres")
        String email,

        @Schema(example = "Camila2026!")
        @NotBlank(message = "la contrasena es obligatoria")
        @Size(min = 8, max = 60, message = "la contrasena debe tener entre 8 y 60 caracteres")
        String password,

        @Schema(example = "+56912345678")
        @Pattern(regexp = Validaciones.TELEFONO, message = Validaciones.TELEFONO_MENSAJE)
        String telefono
) {
}
