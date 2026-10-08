package cl.siniestrofacil.talleres.service;

import cl.siniestrofacil.talleres.model.Taller;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

/** Pruebas unitarias de la regla de seleccion de taller (sin Spring ni base de datos). */
class SeleccionTallerTest {

    @Test
    void eligeElTallerConMasCuposLibres() {
        Taller central = taller(1L, 3, true);
        Taller norte = taller(2L, 5, true);

        Optional<Taller> elegido = AsignacionService.seleccionarTaller(List.of(
                new CupoTaller(central, 1),   // 2 libres
                new CupoTaller(norte, 1)));   // 4 libres

        assertThat(elegido).contains(norte);
    }

    @Test
    void enEmpateEligeElDeMenorId() {
        Taller central = taller(1L, 3, true);
        Taller norte = taller(2L, 3, true);

        Optional<Taller> elegido = AsignacionService.seleccionarTaller(List.of(
                new CupoTaller(norte, 0),
                new CupoTaller(central, 0)));

        assertThat(elegido).contains(central);
    }

    @Test
    void ignoraTalleresSinCupo() {
        Taller lleno = taller(1L, 2, true);
        Taller conCupo = taller(2L, 1, true);

        Optional<Taller> elegido = AsignacionService.seleccionarTaller(List.of(
                new CupoTaller(lleno, 2),
                new CupoTaller(conCupo, 0)));

        assertThat(elegido).contains(conCupo);
    }

    @Test
    void ignoraTalleresInactivos() {
        Taller inactivo = taller(1L, 10, false);

        assertThat(AsignacionService.seleccionarTaller(List.of(new CupoTaller(inactivo, 0)))).isEmpty();
    }

    @Test
    void sinTalleresDisponiblesNoEligeNinguno() {
        Taller lleno = taller(1L, 1, true);

        assertThat(AsignacionService.seleccionarTaller(List.of(new CupoTaller(lleno, 1)))).isEmpty();
        assertThat(AsignacionService.seleccionarTaller(List.of())).isEmpty();
    }

    private Taller taller(Long id, int cupo, boolean activo) {
        Taller t = new Taller();
        t.setId(id);
        t.setNombre("Taller " + id);
        t.setCupoMaximo(cupo);
        t.setActivo(activo);
        return t;
    }
}
