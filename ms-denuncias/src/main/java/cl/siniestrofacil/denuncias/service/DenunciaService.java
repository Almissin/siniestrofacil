package cl.siniestrofacil.denuncias.service;

import cl.siniestrofacil.denuncias.dto.DenunciaRequest;
import cl.siniestrofacil.denuncias.model.Denuncia;
import cl.siniestrofacil.denuncias.repository.DenunciaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.Year;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class DenunciaService {

    // guardar denuncias en MySQL
    private final DenunciaRepository denunciaRepository;

    // synchronized evita que dos peticiones simultaneas obtengan el mismo folio
    public synchronized Denuncia registrar(DenunciaRequest request) {
        Denuncia denuncia = new Denuncia();

        // generar folio con prefijo y correlativo a partir de lo guardado en la base de datos
        String prefijo = String.format("SF-%d-", Year.now().getValue());
        long correlativo = denunciaRepository.countByFolioStartingWith(prefijo) + 1;
        denuncia.setFolio(String.format("%s%06d", prefijo, correlativo));

        // copiar datos recibidos desde la peticion
        denuncia.setPatente(request.getPatente());
        denuncia.setRutAsegurado(request.getRutAsegurado());
        denuncia.setNumeroPoliza(request.getNumeroPoliza());
        denuncia.setFechaSiniestro(request.getFechaSiniestro());
        denuncia.setDescripcion(request.getDescripcion());

        // dejar estado inicial a la espera del proceso asincrono
        denuncia.setEstado("RECIBIDA");
        denuncia.setFechaRegistro(LocalDateTime.now());

        Denuncia guardada = denunciaRepository.save(denuncia);
        log.info("denuncia registrada folio={} patente={} poliza={}",
                guardada.getFolio(), guardada.getPatente(), guardada.getNumeroPoliza());
        return guardada;
    }

    // buscar denuncia por folio
    public Optional<Denuncia> buscarPorFolio(String folio) {
        return denunciaRepository.findById(folio);
    }
}
