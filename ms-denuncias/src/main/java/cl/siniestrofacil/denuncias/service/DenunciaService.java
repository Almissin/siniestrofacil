package cl.siniestrofacil.denuncias.service;

import cl.siniestrofacil.denuncias.dto.DenunciaRequest;
import cl.siniestrofacil.denuncias.model.Denuncia;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;

@Slf4j
@Service
public class DenunciaService {

    // guardar denuncias en memoria mientras no exista base de datos
    private final Map<String, Denuncia> denuncias = new ConcurrentHashMap<>();

    // llevar contador para el folio correlativo
    private final AtomicLong contador = new AtomicLong();

    public Denuncia registrar(DenunciaRequest request) {
        Denuncia denuncia = new Denuncia();

        // generar folio con prefijo y correlativo
        denuncia.setFolio(String.format("SF-%d-%06d", Year.now().getValue(), contador.incrementAndGet()));

        // copiar datos recibidos desde la peticion
        denuncia.setPatente(request.getPatente());
        denuncia.setRutAsegurado(request.getRutAsegurado());
        denuncia.setNumeroPoliza(request.getNumeroPoliza());
        denuncia.setFechaSiniestro(request.getFechaSiniestro());
        denuncia.setDescripcion(request.getDescripcion());

        // dejar estado inicial a la espera del proceso asincrono
        denuncia.setEstado("RECIBIDA");
        denuncia.setFechaRegistro(LocalDateTime.now());

        denuncias.put(denuncia.getFolio(), denuncia);
        log.info("denuncia registrada folio={} patente={} poliza={}",
                denuncia.getFolio(), denuncia.getPatente(), denuncia.getNumeroPoliza());
        return denuncia;
    }

    // buscar denuncia por folio
    public Optional<Denuncia> buscarPorFolio(String folio) {
        return Optional.ofNullable(denuncias.get(folio));
    }
}