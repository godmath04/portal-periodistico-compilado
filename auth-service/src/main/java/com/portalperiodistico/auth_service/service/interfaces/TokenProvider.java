package com.portalperiodistico.auth_service.service.interfaces;

import org.springframework.security.core.userdetails.UserDetails;

/**
 * Interfaz para generación y validación de tokens de autenticación
 *
 * Principios SOLID aplicados:
 * - Interface Segregation Principle (ISP): Interfaz pequeña y focalizada solo en
 *   generación y validación de tokens (separada de extracción de claims)
 * - Dependency Inversion Principle (DIP): Abstracción sobre la tecnología de tokens
 *   (puede ser JWT, OAuth, etc.)
 *
 * Esta interfaz permite cambiar la implementación de tokens (de JWT a otro formato)
 * sin afectar a los consumidores.
 */
public interface TokenProvider {

    /**
     * Genera un token de autenticación para el usuario
     *
     * @param userDetails Detalles del usuario autenticado
     * @return Token de autenticación generado (por ejemplo, JWT)
     */
    String generateToken(UserDetails userDetails);

    /**
     * Valida si un token es válido para el usuario dado
     *
     * @param token Token a validar
     * @param userDetails Detalles del usuario
     * @return true si el token es válido, false en caso contrario
     */
    boolean validateToken(String token, UserDetails userDetails);
}
