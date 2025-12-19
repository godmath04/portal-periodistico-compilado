package com.portalperiodistico.auth_service.controller;

import com.portalperiodistico.auth_service.domain.entity.User;
import com.portalperiodistico.auth_service.domain.repository.UserRepository;
import com.portalperiodistico.auth_service.dto.AuthRequest;
import com.portalperiodistico.auth_service.dto.AuthResponse;
import com.portalperiodistico.auth_service.dto.RegisterRequest;
import com.portalperiodistico.auth_service.service.interfaces.AuthenticationService;

import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.Map;

/**
 * Controlador REST para autenticación de usuarios
 *
 * Principios SOLID aplicados:
 * - Dependency Inversion Principle (DIP): Depende de la interfaz AuthenticationService,
 *   no de implementaciones concretas ni de repositorios
 * - Open/Closed Principle (OCP): La lógica de autenticación está encapsulada en el servicio,
 *   permitiendo extensión sin modificar el controller
 *
 * Antes: Controller dependía directamente de repositorios y contenía lógica de negocio
 * Ahora: Controller delega toda la lógica a AuthenticationService (abstracción)
 */
@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;
    private final UserRepository userRepository; // Solo para getUserBasicInfo (endpoint de lectura simple)

    /**
     * Endpoint para iniciar sesión
     *
     * Delega toda la lógica de autenticación al AuthenticationService
     *
     * @param request Datos de autenticación (username y password)
     * @return ResponseEntity con el token JWT y datos del usuario
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            // Delegar toda la lógica al servicio de autenticación
            AuthResponse response = authenticationService.login(request);
            return ResponseEntity.ok(response);
        } catch (BadCredentialsException e) {
            System.out.println("CAUGHT BAD CREDENTIAL EXCEPTION: " + e.getMessage());
            // Manejar usuario/contraseña incorrectos
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(Map.of("message", "Usuario o contraseña incorrectos"));
        } catch (ResponseStatusException e) {
            // Manejar cuenta desactivada
            return ResponseEntity.status(e.getStatusCode())
                    .body(Map.of("message", e.getReason()));
        }
    }

    /**
     * Endpoint para registrar un nuevo usuario
     *
     * Delega toda la lógica de registro al AuthenticationService
     *
     * @param request Datos del nuevo usuario
     * @return ResponseEntity con mensaje de confirmación
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {
        try {
            // Delegar toda la lógica al servicio de autenticación
            String message = authenticationService.registerUser(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(message);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        }
    }

    /**
     * Obtener informacion basica
     * GET /auth/users/{id}
     */
    @GetMapping("/users/{id}")
    public ResponseEntity<?>
    getUserBasicInfo(@PathVariable Integer id) {
        User user = userRepository.findById(id).orElse(null);
        if (user == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> userInfo = Map.of(
                "userId", user.getUserId(),
                "username", user.getUsername(),
                "firstName", user.getFirstName() != null ? user.getFirstName() : "",
                "lastName", user.getLastName() != null ? user.getLastName() : ""
        );
        return ResponseEntity.ok(userInfo);
    }

}