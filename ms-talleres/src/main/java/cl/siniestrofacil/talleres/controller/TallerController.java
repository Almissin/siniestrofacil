package cl.siniestrofacil.talleres.controller;

import cl.siniestrofacil.talleres.dto.CambiarEstadoRequest;
import cl.siniestrofacil.talleres.dto.TallerRequest;
import cl.siniestrofacil.talleres.dto.TallerResponse;
import cl.siniestrofacil.talleres.service.TallerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.net.URI;
import java.util.List;

/** Listado de talleres en convenio. Consultar: cualquier usuario autenticado. Modificar: solo ADMIN. */
@RestController
@RequestMapping("/talleres")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "1. Talleres", description = "Gestion del listado de talleres en convenio")
public class TallerController {

    private final TallerService tallerService;

    @GetMapping
    @Operation(summary = "Listar talleres", description = "Incluye cupos ocupados y disponibles. Filtro opcional soloActivos")
    public List<TallerResponse> listar(@RequestParam(defaultValue = "false") boolean soloActivos) {
        return tallerService.listar(soloActivos);
    }

    @GetMapping("/disponibles")
    @Operation(summary = "Listar talleres disponibles", description = "Talleres activos con al menos un cupo libre")
    public List<TallerResponse> listarDisponibles() {
        return tallerService.listarDisponibles();
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener taller por id")
    public TallerResponse obtener(@PathVariable Long id) {
        return tallerService.obtener(id);
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Crear taller (ADMIN)")
    public ResponseEntity<TallerResponse> crear(@Valid @RequestBody TallerRequest request) {
        TallerResponse creado = tallerService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Actualizar taller (ADMIN)")
    public TallerResponse actualizar(@PathVariable Long id, @Valid @RequestBody TallerRequest request) {
        return tallerService.actualizar(id, request);
    }

    @PatchMapping("/{id}/estado")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Activar o desactivar taller (ADMIN)", description = "Un taller inactivo no recibe nuevas asignaciones")
    public TallerResponse cambiarEstado(@PathVariable Long id, @Valid @RequestBody CambiarEstadoRequest request) {
        return tallerService.cambiarEstado(id, request.activo());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar taller (ADMIN)", description = "Solo si nunca tuvo asignaciones; si no, se debe desactivar")
    public void eliminar(@PathVariable Long id) {
        tallerService.eliminar(id);
    }
}
