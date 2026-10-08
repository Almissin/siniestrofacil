package cl.siniestrofacil.denuncias.dto;

import cl.siniestrofacil.denuncias.model.Fotografia;

public record FotografiaResponse(Long id, String url, String descripcion) {

    public static FotografiaResponse from(Fotografia f) {
        return new FotografiaResponse(f.getId(), f.getUrl(), f.getDescripcion());
    }
}
