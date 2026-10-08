package cl.siniestrofacil.polizas.repository;

import cl.siniestrofacil.polizas.model.Poliza;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PolizaRepository extends JpaRepository<Poliza, Long> {

    Optional<Poliza> findByNumero(String numero);

    boolean existsByNumero(String numero);

    List<Poliza> findAllByOrderByNumeroAsc();

    List<Poliza> findByRutAseguradoOrderByNumeroAsc(String rutAsegurado);
}
