package cl.siniestrofacil.denuncias.controller;

import cl.siniestrofacil.denuncias.dto.DenunciaRequest;
import cl.siniestrofacil.denuncias.exception.DenunciaNoEncontradaException;
import cl.siniestrofacil.denuncias.model.Denuncia;
import cl.siniestrofacil.denuncias.service.DenunciaService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/denuncias")
@RequiredArgsConstructor
public class DenunciaController {

    private final DenunciaService denunciaService;

    // registrar denuncia y responder folio inmediato
    @PostMapping
    public ResponseEntity<Denuncia> registrar(@Valid @RequestBody DenunciaRequest request) {
        Denuncia denuncia = denunciaService.registrar(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(denuncia);
    }

    // buscar y mostrar detalles por folio
    @GetMapping("/{folio}")
    public ResponseEntity<Denuncia> buscarPorFolio(@PathVariable String folio) {

        // pedir al servicio la denuncia con ese folio
        var resultado = denunciaService.buscarPorFolio(folio);

        // responder 200 si existe y 404 con mensaje si no existe
        if (resultado.isPresent()) {
            return ResponseEntity.ok(resultado.get());
        } else {
            throw new DenunciaNoEncontradaException(folio);
        }
    }
}