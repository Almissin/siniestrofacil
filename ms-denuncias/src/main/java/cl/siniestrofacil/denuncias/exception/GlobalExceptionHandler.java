package cl.siniestrofacil.denuncias.exception;

import cl.siniestrofacil.denuncias.dto.ErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import tools.jackson.databind.DatabindException;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // errores de las anotaciones de validacion del request (@NotBlank, @Pattern, etc.)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> manejarValidacion(MethodArgumentNotValidException ex,
                                                           HttpServletRequest request) {

        // juntar los mensajes por campo, ordenados para que la respuesta sea estable
        Map<String, String> errores = new TreeMap<>();
        for (FieldError fieldError : ex.getBindingResult().getFieldErrors()) {
            errores.merge(fieldError.getField(), fieldError.getDefaultMessage(),
                    (actual, nuevo) -> actual + "; " + nuevo);
        }

        log.warn("validacion fallida en {} {}: {}", request.getMethod(), request.getRequestURI(), errores);

        return construir(HttpStatus.BAD_REQUEST, "la denuncia tiene datos invalidos", request, errores);
    }

    // json mal formado o valores que no se pueden convertir (ej: fecha con formato incorrecto)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> manejarJsonInvalido(HttpMessageNotReadableException ex,
                                                             HttpServletRequest request) {

        String mensaje = "el cuerpo de la peticion no es un json valido";
        Map<String, String> errores = null;

        // si el error viene de un campo puntual, indicar cual
        if (ex.getCause() instanceof DatabindException databindException
                && !databindException.getPath().isEmpty()) {
            String campo = databindException.getPath().getLast().getPropertyName();
            mensaje = "el campo " + campo + " tiene un formato invalido";
            if ("fechaSiniestro".equals(campo)) {
                errores = Map.of(campo, "la fecha debe tener formato AAAA-MM-DD");
            } else {
                errores = Map.of(campo, "formato invalido");
            }
        }

        log.warn("peticion ilegible en {} {}: {}", request.getMethod(), request.getRequestURI(), mensaje);

        return construir(HttpStatus.BAD_REQUEST, mensaje, request, errores);
    }

    // folio inexistente
    @ExceptionHandler(DenunciaNoEncontradaException.class)
    public ResponseEntity<ErrorResponse> manejarNoEncontrada(DenunciaNoEncontradaException ex,
                                                             HttpServletRequest request) {

        log.warn("{} ({} {})", ex.getMessage(), request.getMethod(), request.getRequestURI());

        return construir(HttpStatus.NOT_FOUND, ex.getMessage(), request, null);
    }

    private ResponseEntity<ErrorResponse> construir(HttpStatus status, String mensaje,
                                                    HttpServletRequest request, Map<String, String> errores) {
        ErrorResponse body = new ErrorResponse(LocalDateTime.now(), status.value(), status.getReasonPhrase(),
                mensaje, request.getRequestURI(), errores);
        return ResponseEntity.status(status).body(body);
    }
}
