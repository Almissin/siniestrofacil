package cl.siniestrofacil.denuncias.dto;

import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

// generar gett
@Getter
// generar sett
@Setter
public class DenunciaRequest {

    // validar patente con formato chileno antiguo o nuevo
    @NotBlank(message = "la patente es obligatoria")
    @Pattern(regexp = "^[A-Z]{2}\\d{4}$|^[A-Z]{4}\\d{2}$", message = "formato de patente invalido")
    private String patente;

    @NotBlank(message = "el rut del asegurado es obligatorio")
    private String rutAsegurado;

    @NotBlank(message = "el numero de poliza es obligatorio")
    private String numeroPoliza;

    // validar que la fecha no sea futura
    @NotNull(message = "la fecha del siniestro es obligatoria")
    @PastOrPresent(message = "la fecha del siniestro no puede ser futura")
    private LocalDate fechaSiniestro;

    // limitar largo de la descripcion
    @NotBlank(message = "la descripcion es obligatoria")
    @Size(max = 500, message = "la descripcion no puede superar 500 caracteres")
    private String descripcion;
}
