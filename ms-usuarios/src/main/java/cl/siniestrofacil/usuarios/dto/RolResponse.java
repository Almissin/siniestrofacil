package cl.siniestrofacil.usuarios.dto;

import cl.siniestrofacil.usuarios.model.NombreRol;
import cl.siniestrofacil.usuarios.model.Rol;

public record RolResponse(Long id, NombreRol nombre, String descripcion) {

    public static RolResponse from(Rol rol) {
        return new RolResponse(rol.getId(), rol.getNombre(), rol.getDescripcion());
    }
}
