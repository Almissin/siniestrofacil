package cl.siniestrofacil.usuarios.controller;

import cl.siniestrofacil.usuarios.dto.*;
import cl.siniestrofacil.usuarios.model.NombreRol;
import cl.siniestrofacil.usuarios.security.UsuarioAutenticado;
import cl.siniestrofacil.usuarios.service.UsuarioService;
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

/** Administracion de usuarios: exclusivo del rol ADMIN. */
@RestController
@RequestMapping("/usuarios")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "3. Administracion de usuarios", description = "Solo rol ADMIN")
public class UsuarioAdminController {

    private final UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Listar usuarios", description = "Filtro opcional por rol")
    public List<UsuarioResponse> listar(@RequestParam(required = false) NombreRol rol) {
        return usuarioService.listar(rol);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtener usuario por id")
    public UsuarioResponse obtener(@PathVariable Long id) {
        return usuarioService.obtener(id);
    }

    @PostMapping
    @Operation(summary = "Crear usuario", description = "Permite crear usuarios ADMIN, ASEGURADO o TALLER")
    public ResponseEntity<UsuarioResponse> crear(@Valid @RequestBody CrearUsuarioRequest request) {
        UsuarioResponse creado = usuarioService.crear(request);
        URI ubicacion = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(creado.id()).toUri();
        return ResponseEntity.created(ubicacion).body(creado);
    }

    @PutMapping("/{id}")
    @Operation(summary = "Actualizar usuario", description = "Modifica datos, rol y taller asociado")
    public UsuarioResponse actualizar(@PathVariable Long id,
                                      @Valid @RequestBody ActualizarUsuarioRequest request,
                                      @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado admin) {
        return usuarioService.actualizar(id, request, admin.id());
    }

    @PatchMapping("/{id}/estado")
    @Operation(summary = "Activar o desactivar usuario", description = "Un usuario desactivado no puede iniciar sesion")
    public UsuarioResponse cambiarEstado(@PathVariable Long id,
                                         @Valid @RequestBody CambiarEstadoRequest request,
                                         @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado admin) {
        return usuarioService.cambiarEstado(id, request.activo(), admin.id());
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Eliminar usuario")
    public void eliminar(@PathVariable Long id,
                         @Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado admin) {
        usuarioService.eliminar(id, admin.id());
    }
}
