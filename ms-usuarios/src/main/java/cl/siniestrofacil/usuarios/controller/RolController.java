package cl.siniestrofacil.usuarios.controller;

import cl.siniestrofacil.usuarios.dto.RolResponse;
import cl.siniestrofacil.usuarios.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/roles")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
@SecurityRequirement(name = "bearerAuth")
@Tag(name = "4. Roles", description = "Solo rol ADMIN")
public class RolController {

    private final UsuarioService usuarioService;

    @GetMapping
    @Operation(summary = "Listar roles del sistema")
    public List<RolResponse> listar() {
        return usuarioService.listarRoles();
    }
}
