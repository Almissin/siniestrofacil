package cl.siniestrofacil.polizas.dto;

import cl.siniestrofacil.polizas.model.Cobertura;

public record CoberturaResponse(Long id, String nombre, Long montoMaximo) {

    public static CoberturaResponse from(Cobertura c) {
        return new CoberturaResponse(c.getId(), c.getNombre(), c.getMontoMaximo());
    }
}
