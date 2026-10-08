package cl.siniestrofacil.usuarios.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Datos que un usuario puede modificar de su propio perfil. */
public record ActualizarPerfilRequest(

        @Schema(example = "Camila")
        @NotBlank(message = "el nombre es obligatorio")
        @Size(max = 80, message = "el nombre no puede superar 80 caracteres")
        String nombre,

        @Schema(example = "Rojas Perez")
        @NotBlank(message = "el apellido es obligatorio")
        @Size(max = 80, message = "el apellido no puede superar 80 caracteres")
        String apellido,

        @Schema(example = "+56987654321")
        @Pattern(regexp = Validaciones.TELEFONO, message = Validaciones.TELEFONO_MENSAJE)
        String telefono
) {
}
