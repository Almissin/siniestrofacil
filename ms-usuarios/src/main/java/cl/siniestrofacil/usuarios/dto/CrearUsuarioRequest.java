package cl.siniestrofacil.usuarios.dto;

import cl.siniestrofacil.usuarios.model.NombreRol;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;

/** Datos para que un ADMIN cree un usuario con cualquier rol. */
public record CrearUsuarioRequest(

        @Schema(example = "76543210-3")
        @NotBlank(message = "el rut es obligatorio")
        @Pattern(regexp = Validaciones.RUT, message = Validaciones.RUT_MENSAJE)
        String rut,

        @Schema(example = "Taller")
        @NotBlank(message = "el nombre es obligatorio")
        @Size(max = 80, message = "el nombre no puede superar 80 caracteres")
        String nombre,

        @Schema(example = "Automotriz Norte")
        @NotBlank(message = "el apellido es obligatorio")
        @Size(max = 80, message = "el apellido no puede superar 80 caracteres")
        String apellido,

        @Schema(example = "contacto@tallernorte.cl")
        @NotBlank(message = "el email es obligatorio")
        @Email(message = "el email no tiene un formato valido")
        @Size(max = 120, message = "el email no puede superar 120 caracteres")
        String email,

        @Schema(example = "TallerNorte2026!")
        @NotBlank(message = "la contrasena es obligatoria")
        @Size(min = 8, max = 60, message = "la contrasena debe tener entre 8 y 60 caracteres")
        String password,

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
