package cl.siniestrofacil.denuncias.model;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

/** Pruebas del ciclo de vida de la denuncia. */
class EstadoDenunciaTest {

    @Test
    void soloUnaDenunciaRecibidaAdmiteResultado() {
        assertThat(EstadoDenuncia.RECIBIDA.admiteResultado()).isTrue();
        assertThat(EstadoDenuncia.RECHAZADA.admiteResultado()).isFalse();
        assertThat(EstadoDenuncia.TALLER_ASIGNADO.admiteResultado()).isFalse();
        assertThat(EstadoDenuncia.VEHICULO_RECIBIDO.admiteResultado()).isFalse();
    }

    @Test
    void soloUnaDenunciaConTallerAsignadoAdmiteRecepcion() {
        assertThat(EstadoDenuncia.TALLER_ASIGNADO.admiteRecepcion()).isTrue();
        assertThat(EstadoDenuncia.RECIBIDA.admiteRecepcion()).isFalse();
        assertThat(EstadoDenuncia.RECHAZADA.admiteRecepcion()).isFalse();
        assertThat(EstadoDenuncia.VEHICULO_RECIBIDO.admiteRecepcion()).isFalse();
    }
}
