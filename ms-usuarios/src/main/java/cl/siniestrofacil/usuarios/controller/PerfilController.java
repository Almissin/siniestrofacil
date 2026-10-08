package cl.siniestrofacil.usuarios.controller;

import cl.siniestrofacil.usuarios.dto.ActualizarPerfilRequest;
import cl.siniestrofacil.usuarios.dto.CambiarPasswordRequest;
import cl.siniestrofacil.usuarios.dto.UsuarioResponse;
import cl.siniestrofacil.usuarios.security.UsuarioAutenticado;
import cl.siniestrofacil.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/** Perfil del usuario autenticado (cualquier rol). El id se toma del token, nunca de la URL. */
@RestController
@RequestMapping("/usuarios/me")
@RequiredArgsConstructor
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "2. Mi perfil", description = "Perfil del usuario autenticado (ADMIN, ASEGURADO o TALLER)")
public class PerfilController {

    private final UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Ver mi perfil")
    public UsuarioResponse verPerfil(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado actual) {
        return usuarioService.obtenerPerfil(actual.id());
    }

    @PutMapping
    @Operation(summary = "Actualizar mi perfil", description = "Modifica nombre, apellido y telefono")
    public UsuarioResponse actualizarPerfil(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado actual,
                                            @Valid @RequestBody ActualizarPerfilRequest request) {
        return usuarioService.actualizarPerfil(actual.id(), request);
    }

    @PutMapping("/password")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "Cambiar mi contrasena")
    public void cambiarPassword(@Parameter(hidden = true) @AuthenticationPrincipal UsuarioAutenticado actual,
                                @Valid @RequestBody CambiarPasswordRequest request) {
        usuarioService.cambiarPassword(actual.id(), request);
    }
}
