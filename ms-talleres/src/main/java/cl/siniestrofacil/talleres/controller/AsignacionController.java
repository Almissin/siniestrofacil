package cl.siniestrofacil.talleres.controller;

import cl.siniestrofacil.talleres.dto.AsignacionRequest;
import cl.siniestrofacil.talleres.dto.AsignacionResponse;
import cl.siniestrofacil.talleres.security.UsuarioAutenticado;
import cl.siniestrofacil.talleres.service.AsignacionService;
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
@RequestMapping("/asignaciones")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "2. Asignaciones", description = "Asignacion de talleres a denuncias con poliza vigente")
public class AsignacionController {

    private final AsignacionService asignacionService;

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Asignar taller a una denuncia (ADMIN)",
            description = "Elige el taller activo con mas cupos libres. En la EP3 lo llamara la AWS Lambda")
    public ResponseEntity<AsignacionResponse> asignar(@Valid @RequestBody AsignacionRequest request) {
        AsignacionResponse creada = asignacionService.asignar(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/asignaciones/denuncia/{folio}").buildAndExpand(creada.folioDenuncia()).toUri();
        return ResponseEntity.created(ubicacion).body(creada);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Listar todas las asignaciones (ADMIN)")
    public List<AsignacionResponse> listar() {
        return asignacionService.listar();
    }

    @GetMapping("/mi-taller")
    @PreAuthorize("hasRole('TALLER')")
    @Operation(summary = "Asignaciones de mi taller (TALLER)", description = "El taller se toma del token")
    public List<AsignacionResponse> listarDeMiTaller(
            @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return asignacionService.listarDeMiTaller(usuario);
    }

    @GetMapping("/denuncia/{folio}")
    @PreAuthorize("hasAnyRole('ADMIN', 'TALLER')")
    @Operation(summary = "Consultar la asignacion de una denuncia", description = "Un TALLER solo ve las de su taller")
    public AsignacionResponse buscarPorFolio(@PathVariable String folio,
                                             @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return asignacionService.buscarPorFolio(folio, usuario);
    }

    @PatchMapping("/{id}/finalizar")
    @PreAuthorize("hasAnyRole('ADMIN', 'TALLER')")
    @Operation(summary = "Finalizar asignacion y liberar el cupo", description = "Un TALLER solo finaliza las de su taller")
    public AsignacionResponse finalizar(@PathVariable Long id,
                                        @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado usuario) {
        return asignacionService.finalizar(id, usuario);
    }
}
