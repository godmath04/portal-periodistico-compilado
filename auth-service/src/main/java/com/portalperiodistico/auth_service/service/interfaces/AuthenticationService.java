package com.portalperiodistico.auth_service.service.interfaces;

import com.portalperiodistico.auth_service.dto.AuthRequest;
import com.portalperiodistico.auth_service.dto.AuthResponse;
import com.portalperiodistico.auth_service.dto.RegisterRequest;

/**
 * Interfaz para operaciones de autenticación de usuarios
 *
 * Principios SOLID aplicados:
 * - Dependency Inversion Principle (DIP): Los controllers dependerán de esta abstracción,
 *   no de implementaciones concretas
 * - Interface Segregation Principle (ISP): Interfaz focalizada en autenticación
 *   (login y registro), segregada de otras responsabilidades
 *
 * Esta interfaz encapsula toda la lógica de autenticación, permitiendo:
 * - Testabilidad mejorada (se puede mockear fácilmente)
 * - Flexibilidad para cambiar implementaciones (OAuth, LDAP, etc.)
 * - Desacoplamiento entre controller y lógica de negocio
 */
public interface AuthenticationService {

    /**
     * Autentica un usuario y genera un token JWT
     *
     * @param request Datos de autenticación (username y password)
     * @return AuthResponse con el token JWT y datos del usuario
     * @throws org.springframework.security.authentication.BadCredentialsException si las credenciales son inválidas
     */
    AuthResponse login(AuthRequest request);

    /**
     * Registra un nuevo usuario en el sistema
     *
     * El usuario registrado recibe automáticamente el rol "Reportero"
     *
     * @param request Datos del nuevo usuario (username, email, password, nombre, apellido)
     * @return Mensaje de confirmación del registro
     * @throws RuntimeException si el username o email ya existen
     */
    String registerUser(RegisterRequest request);
}
