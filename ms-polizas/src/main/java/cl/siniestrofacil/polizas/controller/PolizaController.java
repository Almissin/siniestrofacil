package cl.siniestrofacil.polizas.controller;

import cl.siniestrofacil.polizas.dto.*;
import cl.siniestrofacil.polizas.security.UsuarioAutenticado;
import cl.siniestrofacil.polizas.service.PolizaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/polizas")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Polizas", description = "Gestion del listado de polizas y verificacion de vigencia")
public class PolizaController {

    private final PolizaService polizaService;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar polizas (ADMIN)", description = "Filtro opcional por rut del asegurado")
    public List<PolizaResponse> listar(@RequestParam(required = false) String rut) {
        return polizaService.listar(rut);
    }

    @GetMapping("/mis-polizas")
    @PreAuthorize("hasRole('ASEGURADO')")
    @Operation(summary = "Mis polizas (ASEGURADO)", description = "El rut se toma del token")
    public List<PolizaResponse> listarMisPolizas(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return polizaService.listarMisPolizas(usuario);
    }

    @GetMapping("/{numero}")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASEGURADO')")
    @Operation(summary = "Obtener poliza", description = "Un ASEGURADO solo ve sus propias polizas")
    public PolizaResponse obtener(@PathVariable String numero,
                                  @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return polizaService.obtener(numero, usuario);
    }

    @GetMapping("/{numero}/vigencia")
    @PreAuthorize("hasAnyRole('ADMIN', 'ASEGURADO')")
    @Operation(summary = "Verificar vigencia de la poliza",
            description = "Responde si la poliza esta vigente hoy y el motivo. En la EP3 lo llamara la AWS Lambda")
    public VigenciaResponse consultarVigencia(@PathVariable String numero,
                                              @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return polizaService.consultarVigencia(numero, usuario);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear poliza con sus coberturas (ADMIN)")
    public ResponseEntity<PolizaResponse> crear(@Valid @RequestBody PolizaRequest request) {
        PolizaResponse creada = polizaService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{numero}").buildAndExpand(creada.numero()).toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @PutMapping("/{numero}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar poliza (ADMIN)", description = "Las coberturas se reemplazan por las enviadas")
    public PolizaResponse actualizar(@PathVariable String numero, @Valid @RequestBody ActualizarPolizaRequest request) {
        return polizaService.actualizar(numero, request);
    }

    @PatchMapping("/{numero}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar o anular poliza (ADMIN)", description = "Una poliza anulada deja de estar vigente")
    public PolizaResponse cambiarEstado(@PathVariable String numero, @Valid @RequestBody CambiarEstadoRequest request) {
        return polizaService.cambiarEstado(numero, request.activa());
    }

    @DeleteMapping("/{numero}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar poliza (ADMIN)", description = "Elimina tambien sus coberturas")
    public void eliminar(@PathVariable String numero) {
        polizaService.eliminar(numero);
    }
}
