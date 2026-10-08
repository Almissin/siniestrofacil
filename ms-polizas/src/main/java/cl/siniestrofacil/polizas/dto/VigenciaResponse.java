package cl.siniestrofacil.polizas.dto;

import java.time.LocalDate;

/**
 * Respuesta de GET /polizas/{numero}/vigencia.
 * En la EP3 la consumira la AWS Lambda para decidir si la denuncia continua a la asignacion de taller.
 */
public record VigenciaResponse(
        String numeroPoliza,
        String rutAsegurado,
        boolean vigente,
        String motivo,
        LocalDate fechaInicio,
        LocalDate fechaTermino,
        LocalDate fechaConsulta
) {
}
