package com.portalperiodistico.auth_service.service.interfaces;

import io.jsonwebtoken.Claims;

import java.util.function.Function;

/**
 * Interfaz para extracción de claims (datos) de un token
 *
 * Principios SOLID aplicados:
 * - Interface Segregation Principle (ISP): Segregada de TokenProvider para que
 *   los consumidores que solo necesitan extraer datos no dependan de métodos
 *   de generación/validación
 * - Dependency Inversion Principle (DIP): Abstracción sobre extracción de claims
 *
 * Esta segregación permite que los componentes que solo leen información del token
 * (como filtros de seguridad) no dependan de la interfaz completa de generación.
 */
public interface TokenClaimsExtractor {

    /**
     * Extrae el username (subject) del token
     *
     * @param token Token del cual extraer el username
     * @return Username contenido en el token
     */
    String extractUsername(String token);

    /**
     * Extrae un claim específico del token usando una función extractora
     *
     * @param token Token del cual extraer el claim
     * @param claimsResolver Función que extrae el claim deseado de los Claims
     * @param <T> Tipo del claim a extraer
     * @return Valor del claim extraído
     */
    <T> T extractClaim(String token, Function<Claims, T> claimsResolver);
}
