package cl.siniestrofacil.polizas.dto;

import cl.siniestrofacil.polizas.model.Poliza;
import cl.siniestrofacil.polizas.service.ResultadoVigencia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record PolizaResponse(
        Long id,
        String numero,
        String rutAsegurado,
        String patente,
        LocalDate fechaInicio,
        LocalDate fechaTermino,
        boolean activa,
        boolean vigente,
        List<CoberturaResponse> coberturas,
        LocalDateTime fechaCreacion
) {
    public static PolizaResponse from(Poliza p, ResultadoVigencia vigencia) {
        return new PolizaResponse(
                p.getId(), p.getNumero(), p.getRutAsegurado(), p.getPatente(),
                p.getFechaInicio(), p.getFechaTermino(), p.isActiva(), vigencia.vigente(),
                p.getCoberturas().stream().map(CoberturaResponse::from).toList(),
                p.getFechaCreacion());
    }
}
