package cl.siniestrofacil.talleres.dto;

import cl.siniestrofacil.talleres.model.Taller;

import java.time.LocalDateTime;

/** Taller con el calculo de cupos ocupados y disponibles. */
public record TallerResponse(
        Long id,
        String nombre,
        String rut,
        String direccion,
        String comuna,
        String telefono,
        String email,
        int cupoMaximo,
        long cuposOcupados,
        long cuposDisponibles,
        boolean activo,
        LocalDateTime fechaCreacion
) {
    public static TallerResponse from(Taller t, long ocupados) {
        return new TallerResponse(
                t.getId(), t.getNombre(), t.getRut(), t.getDireccion(), t.getComuna(),
                t.getTelefono(), t.getEmail(), t.getCupoMaximo(), ocupados,
                Math.max(0, t.getCupoMaximo() - ocupados), t.isActivo(), t.getFechaCreacion());
    }
}
