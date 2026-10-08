package cl.siniestrofacil.denuncias.dto;

import cl.siniestrofacil.denuncias.model.Denuncia;
import cl.siniestrofacil.denuncias.model.EstadoDenuncia;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public record DenunciaResponse(
        String folio,
        String patente,
        String rutAsegurado,
        String numeroPoliza,
        LocalDate fechaSiniestro,
        String descripcion,
        EstadoDenuncia estado,
        LocalDateTime fechaRegistro,
        List<FotografiaResponse> fotografias,
        Long tallerId,
        String nombreTaller,
        String motivoRechazo,
        LocalDateTime fechaResultado,
        LocalDateTime fechaRecepcion
) {
    public static DenunciaResponse from(Denuncia d) {
        return new DenunciaResponse(
                d.getFolio(), d.getPatente(), d.getRutAsegurado(), d.getNumeroPoliza(),
                d.getFechaSiniestro(), d.getDescripcion(), d.getEstado(), d.getFechaRegistro(),
                d.getFotografias().stream().map(FotografiaResponse::from).toList(),
                d.getTallerId(), d.getNombreTaller(), d.getMotivoRechazo(),
                d.getFechaResultado(), d.getFechaRecepcion());
    }
}
