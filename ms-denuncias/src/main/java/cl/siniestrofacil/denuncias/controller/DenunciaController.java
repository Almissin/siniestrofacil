package cl.siniestrofacil.denuncias.controller;

import cl.siniestrofacil.denuncias.dto.DenunciaRequest;
import cl.siniestrofacil.denuncias.dto.DenunciaResponse;
import cl.siniestrofacil.denuncias.dto.ResultadoRequest;
import cl.siniestrofacil.denuncias.model.EstadoDenuncia;
import cl.siniestrofacil.denuncias.security.UsuarioAutenticado;
import cl.siniestrofacil.denuncias.service.DenunciaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/denuncias")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "Denuncias", description = "Registro y consulta de denuncias de siniestros vehiculares")
public class DenunciaController {

    private final DenunciaService denunciaService;

    @PostMapping
    @PreAuthorize("hasRole('ASEGURADO')")
    @Operation(summary = "Registrar denuncia (ASEGURADO)",
            description = "Entrega de inmediato el folio y deja la denuncia en estado RECIBIDA. El rut se toma del token")
    public ResponseEntity<DenunciaResponse> registrar(@Valid @RequestBody DenunciaRequest request,
                                                      @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        DenunciaResponse creada = denunciaService.registrar(request, usuario);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{folio}").buildAndExpand(creada.folio()).toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @GetMapping("/mis-denuncias")
    @PreAuthorize("hasRole('ASEGURADO')")
    @Operation(summary = "Mis denuncias (ASEGURADO)")
    public List<DenunciaResponse> listarMisDenuncias(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return denunciaService.listarMisDenuncias(usuario);
    }

    @GetMapping("/asignadas")
    @PreAuthorize("hasRole('TALLER')")
    @Operation(summary = "Denuncias asignadas a mi taller (TALLER)", description = "El taller se toma del token")
    public List<DenunciaResponse> listarAsignadas(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return denunciaService.listarAsignadas(usuario);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todas las denuncias (ADMIN)", description = "Filtro opcional por estado")
    public List<DenunciaResponse> listar(@RequestParam(required = false) EstadoDenuncia estado) {
        return denunciaService.listar(estado);
    }

    @GetMapping("/{folio}")
    @Operation(summary = "Consultar denuncia por folio",
            description = "ADMIN ve todas; ASEGURADO solo las suyas; TALLER solo las asignadas a su taller")
    public DenunciaResponse obtener(@PathVariable String folio,
                                    @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return denunciaService.obtener(folio, usuario);
    }

    @PatchMapping("/{folio}/resultado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Registrar resultado del procesamiento (ADMIN)",
            description = "RECHAZADA (poliza no vigente) o TALLER_ASIGNADO. En la EP3 lo llamara la AWS Lambda")
    public DenunciaResponse registrarResultado(@PathVariable String folio, @Valid @RequestBody ResultadoRequest request) {
        return denunciaService.registrarResultado(folio, request);
    }

    @PatchMapping("/{folio}/recepcion")
    @PreAuthorize("hasRole('TALLER')")
    @Operation(summary = "Confirmar recepcion del vehiculo (TALLER asignado)")
    public DenunciaResponse confirmarRecepcion(@PathVariable String folio,
                                               @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return denunciaService.confirmarRecepcion(folio, usuario);
    }
}
