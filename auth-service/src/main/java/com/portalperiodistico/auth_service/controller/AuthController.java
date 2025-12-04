package com.portalperiodistico.auth_service.controller;

import com.portalperiodistico.auth_service.domain.entity.Role;
import com.portalperiodistico.auth_service.domain.entity.User;
import com.portalperiodistico.auth_service.domain.repository.RoleRepository;
import com.portalperiodistico.auth_service.domain.repository.UserRepository;
import com.portalperiodistico.auth_service.dto.AuthRequest;
import com.portalperiodistico.auth_service.dto.AuthResponse;
import com.portalperiodistico.auth_service.dto.RegisterRequest;
import com.portalperiodistico.auth_service.service.JwtService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails; // Importante
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.BadCredentialsException;
import java.util.Map;

import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/auth")
public class AuthController {

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private JwtService jwtService;

    /**
     * Endpoint para iniciar sesión (VERSIÓN ESTÁNDAR)
     * POST /auth/login
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody AuthRequest request) {
        try {
            //0. Verificar si el usuario esta activo antes de la autenticacion
            User user = userRepository.findByUsername(request.getUsername())
                    .orElse(null);
            if (user != null && !user.isActive())
            {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                                .body(Map.of("message", "Tu cuenta ha sido desactivada por un administrador"));
            }

            // 1. Autenticar al usuario.
            //    Esto usa UserDetailsServiceImpl y el PasswordEncoder
            //    para verificar la contraseña.
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            request.getUsername(),
                            request.getPassword()
                    )
            );

            // 2. Si la autenticación fue exitosa, el objeto 'authentication'
            //    contiene el UserDetails que creó nuestro servicio.
            UserDetails userDetails = (UserDetails) authentication.getPrincipal();

            // 3. Generar el token JWT usando ese UserDetails
            String jwt = jwtService.generateToken(userDetails);

            // 4. Devolver el token
            return ResponseEntity.ok(AuthResponse.builder()
                    .token(jwt)
                    .username(userDetails.getUsername())
                    .build());
        } catch (BadCredentialsException e) {
            System.out.println("CAUGHT BAD CREDENTIAL EXCEPTION: " + e.getMessage());
            // Manejar usuario/contraseña incorrectos
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(java.util.Map.of("message", "Usuario o contraseña incorrectos"));
        }
    }

    /**
     * Endpoint para registrar un nuevo usuario
     * POST /auth/register
     */
    @PostMapping("/register")
    public ResponseEntity<?> registerUser(@RequestBody RegisterRequest request) {

        if (userRepository.existsByUsername(request.getUsername())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: El nombre de usuario ya existe.");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("Error: El email ya está en uso.");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(true);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        Role reporteroRole = roleRepository.findByRoleName("Reportero")
                .orElseThrow(() -> new RuntimeException("Error: Rol 'Reportero' no encontrado."));

        user.setRoles(Set.of(reporteroRole));
        userRepository.save(user);

        return ResponseEntity.status(HttpStatus.CREATED).body("Usuario registrado exitosamente.");
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