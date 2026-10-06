package cl.siniestrofacil.talleres.dto;

import cl.siniestrofacil.talleres.model.Asignacion;
import cl.siniestrofacil.talleres.model.EstadoAsignacion;

import java.time.LocalDateTime;

public record AsignacionResponse(
        Long id,
        String folioDenuncia,
        Long tallerId,
        String nombreTaller,
        String direccionTaller,
        String comunaTaller,
        EstadoAsignacion estado,
        LocalDateTime fechaAsignacion,
        LocalDateTime fechaFinalizacion
) {
    public static AsignacionResponse from(Asignacion a) {
        return new AsignacionResponse(
                a.getId(), a.getFolioDenuncia(),
                a.getTaller().getId(), a.getTaller().getNombre(),
                a.getTaller().getDireccion(), a.getTaller().getComuna(),
                a.getEstado(), a.getFechaAsignacion(), a.getFechaFinalizacion());
    }
}
