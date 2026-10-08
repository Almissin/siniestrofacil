package cl.siniestrofacil.denuncias.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/** Referencia a una fotografia (URL). La imagen no se guarda en este servicio. */
public record FotografiaRequest(

        @Schema(example = "https://fotos.siniestrofacil.cl/ABCD12/parachoque.jpg")
        @NotBlank(message = "la url de la fotografia es obligatoria")
        @Size(max = 500, message = "la url no puede superar 500 caracteres")
        @Pattern(regexp = "^https?://.+", message = "la url debe comenzar con http:// o https://")
        String url,

        @Schema(example = "parachoque trasero")
        @Size(max = 150, message = "la descripcion de la fotografia no puede superar 150 caracteres")
        String descripcion
) {
}
