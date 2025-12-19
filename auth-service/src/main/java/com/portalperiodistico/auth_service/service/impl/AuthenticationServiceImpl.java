package com.portalperiodistico.auth_service.service.impl;

import com.portalperiodistico.auth_service.domain.entity.Role;
import com.portalperiodistico.auth_service.domain.entity.User;
import com.portalperiodistico.auth_service.domain.repository.RoleRepository;
import com.portalperiodistico.auth_service.domain.repository.UserRepository;
import com.portalperiodistico.auth_service.dto.AuthRequest;
import com.portalperiodistico.auth_service.dto.AuthResponse;
import com.portalperiodistico.auth_service.dto.RegisterRequest;
import com.portalperiodistico.auth_service.service.interfaces.AuthenticationService;
import com.portalperiodistico.auth_service.service.interfaces.TokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.Set;

/**
 * Implementación del servicio de autenticación de usuarios
 *
 * Principios SOLID aplicados:
 * - Dependency Inversion Principle (DIP): Depende de interfaces (TokenProvider, UserRepository, etc.)
 *   en lugar de implementaciones concretas
 * - Interface Segregation Principle (ISP): Implementa una interfaz focalizada en autenticación
 * - Open/Closed Principle (OCP): La lógica de autenticación está encapsulada, permitiendo
 *   extensión sin modificación del controller
 *
 * Esta clase encapsula la lógica de login y registro que anteriormente estaba en AuthController,
 * mejorando la separación de responsabilidades según SOLID.
 */
@Service
@RequiredArgsConstructor
public class AuthenticationServiceImpl implements AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final TokenProvider tokenProvider;

    /**
     * Autentica un usuario y genera un token JWT
     *
     * @param request Datos de autenticación (username y password)
     * @return AuthResponse con el token JWT y datos del usuario
     * @throws BadCredentialsException si las credenciales son inválidas
     * @throws ResponseStatusException si la cuenta está desactivada
     */
    @Override
    public AuthResponse login(AuthRequest request) {
        // 0. Verificar si el usuario está activo antes de la autenticación
        User user = userRepository.findByUsername(request.getUsername())
                .orElse(null);
        if (user != null && !user.isActive()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Tu cuenta ha sido desactivada por un administrador"
            );
        }

        // 1. Autenticar al usuario
        // Esto usa UserDetailsServiceImpl y el PasswordEncoder para verificar la contraseña
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );

        // 2. Si la autenticación fue exitosa, el objeto 'authentication'
        // contiene el UserDetails que creó nuestro servicio
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();

        // 3. Generar el token JWT usando ese UserDetails
        String jwt = tokenProvider.generateToken(userDetails);

        // 4. Devolver el token
        return AuthResponse.builder()
                .token(jwt)
                .username(userDetails.getUsername())
                .build();
    }

    /**
     * Registra un nuevo usuario en el sistema
     *
     * El usuario registrado recibe automáticamente el rol "Reportero"
     *
     * @param request Datos del nuevo usuario
     * @return Mensaje de confirmación del registro
     * @throws RuntimeException si el username o email ya existen
     * @throws RuntimeException si el rol "Reportero" no existe en la base de datos
     */
    @Override
    public String registerUser(RegisterRequest request) {
        // Validar que el username no exista
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Error: El nombre de usuario ya existe.");
        }

        // Validar que el email no exista
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Error: El email ya está en uso.");
        }

        // Crear el nuevo usuario
        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(true);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        // Asignar rol por defecto: Reportero
        Role reporteroRole = roleRepository.findByRoleName("Reportero")
                .orElseThrow(() -> new RuntimeException("Error: Rol 'Reportero' no encontrado."));

        user.setRoles(Set.of(reporteroRole));

        // Guardar usuario
        userRepository.save(user);

        return "Usuario registrado exitosamente.";
    }
}
