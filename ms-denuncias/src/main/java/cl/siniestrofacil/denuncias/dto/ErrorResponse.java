package cl.siniestrofacil.denuncias.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.Map;

// respuesta comun para todos los errores de la api
@Getter
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    private LocalDateTime timestamp;
    private int status;
    private String error;
    private String mensaje;
    private String path;

    // detalle por campo, solo para errores de validacion
    private Map<String, String> errores;
}
