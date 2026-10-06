package cl.siniestrofacil.talleres.service;

import cl.siniestrofacil.talleres.model.Taller;

/** Un taller junto con la cantidad de asignaciones activas (cupos ocupados). */
public record CupoTaller(Taller taller, long ocupados) {

    public long libres() {
        return taller.getCupoMaximo() - ocupados;
    }
}
