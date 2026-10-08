package cl.siniestrofacil.usuarios.controller;

import cl.siniestrofacil.usuarios.dto.LoginRequest;
import cl.siniestrofacil.usuarios.dto.LoginResponse;
import cl.siniestrofacil.usuarios.dto.RegistroRequest;
import cl.siniestrofacil.usuarios.dto.UsuarioResponse;
import cl.siniestrofacil.usuarios.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@Tag(name = "1. Autenticacion", description = "Registro de asegurados e inicio de sesion (publico)")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/registro")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "Registrar asegurado", description = "Crea una cuenta con rol ASEGURADO")
    @ApiResponse(responseCode = "201", description = "Cuenta creada")
    @ApiResponse(responseCode = "400", description = "Datos invalidos")
    @ApiResponse(responseCode = "409", description = "Email o rut ya registrado")
    public UsuarioResponse registrar(@Valid @RequestBody RegistroRequest request) {
        return authService.registrar(request);
    }

    @PostMapping("/login")
    @Operation(summary = "Iniciar sesion", description = "Devuelve un token JWT para usar en el header Authorization")
    @ApiResponse(responseCode = "200", description = "Login correcto, incluye el token")
    @ApiResponse(responseCode = "401", description = "Email o contrasena incorrectos")
    @ApiResponse(responseCode = "403", description = "Cuenta desactivada")
    public LoginResponse login(@Valid @RequestBody LoginRequest request) {
        return authService.login(request);
    }
}
