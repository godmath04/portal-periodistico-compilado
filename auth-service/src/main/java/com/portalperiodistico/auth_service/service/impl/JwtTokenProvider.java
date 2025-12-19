package com.portalperiodistico.auth_service.service.impl;

import com.portalperiodistico.auth_service.security.UserPrincipal;
import com.portalperiodistico.auth_service.service.interfaces.TokenClaimsExtractor;
import com.portalperiodistico.auth_service.service.interfaces.TokenProvider;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Implementación del proveedor de tokens JWT
 *
 * Principios SOLID aplicados:
 * - Interface Segregation Principle (ISP): Implementa TokenProvider y TokenClaimsExtractor
 *   por separado, permitiendo a los consumidores depender solo de lo que necesitan
 * - Dependency Inversion Principle (DIP): Los consumidores dependen de las interfaces,
 *   no de esta implementación concreta
 *
 * Esta clase reemplaza a JwtService, ahora implementando interfaces segregadas para
 * mejorar el diseño según SOLID.
 */
@Service
public class JwtTokenProvider implements TokenProvider, TokenClaimsExtractor {

    @Value("${application.security.jwt.secret-key}")
    private String secretKey;

    @Value("${application.security.jwt.expiration}")
    private long jwtExpiration;

    /**
     * Genera un token JWT incluyendo userId y roles
     *
     * @param userDetails Detalles del usuario autenticado
     * @return Token JWT generado
     */
    @Override
    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();

        // Si el UserDetails es nuestro UserPrincipal, extraemos userId y roles
        if (userDetails instanceof UserPrincipal) {
            UserPrincipal userPrincipal = (UserPrincipal) userDetails;
            claims.put("userId", userPrincipal.getUserId());

            List<String> roles = userPrincipal.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .map(authority -> authority.replace("ROLE_", ""))
                    .collect(Collectors.toList());
            claims.put("roles", roles);
        }

        return buildToken(claims, userDetails, jwtExpiration);
    }

    /**
     * Valida si un token es válido para el usuario dado
     *
     * @param token Token a validar
     * @param userDetails Detalles del usuario
     * @return true si el token es válido, false en caso contrario
     */
    @Override
    public boolean validateToken(String token, UserDetails userDetails) {
        final String username = extractUsername(token);
        return (username.equals(userDetails.getUsername())) && !isTokenExpired(token);
    }

    /**
     * Extrae el username (subject) del token
     *
     * @param token Token del cual extraer el username
     * @return Username contenido en el token
     */
    @Override
    public String extractUsername(String token) {
        return extractClaim(token, Claims::getSubject);
    }

    /**
     * Extrae un claim específico del token usando una función extractora
     *
     * @param token Token del cual extraer el claim
     * @param claimsResolver Función que extrae el claim deseado de los Claims
     * @param <T> Tipo del claim a extraer
     * @return Valor del claim extraído
     */
    @Override
    public <T> T extractClaim(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = extractAllClaims(token);
        return claimsResolver.apply(claims);
    }

    /**
     * Construye el token JWT con claims, subject y expiración
     *
     * @param extraClaims Claims adicionales (userId, roles, etc.)
     * @param userDetails Detalles del usuario (username se usa como subject)
     * @param expiration Tiempo de expiración en milisegundos
     * @return Token JWT construido
     */
    private String buildToken(Map<String, Object> extraClaims, UserDetails userDetails, long expiration) {
        return Jwts.builder()
                .setClaims(extraClaims)
                .setSubject(userDetails.getUsername())
                .setIssuedAt(new Date(System.currentTimeMillis()))
                .setExpiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    /**
     * Verifica si el token ha expirado
     *
     * @param token Token a verificar
     * @return true si el token ha expirado, false en caso contrario
     */
    private boolean isTokenExpired(String token) {
        try {
            Date expiration = extractExpiration(token);
            return expiration.before(new Date());
        } catch (io.jsonwebtoken.ExpiredJwtException e) {
            return true;
        }
    }

    /**
     * Extrae la fecha de expiración del token
     *
     * @param token Token del cual extraer la expiración
     * @return Fecha de expiración
     */
    private Date extractExpiration(String token) {
        return extractClaim(token, Claims::getExpiration);
    }

    /**
     * Extrae todos los claims del token
     *
     * @param token Token a parsear
     * @return Claims contenidos en el token
     */
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .setSigningKey(getSigningKey())
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    /**
     * Obtiene la clave de firma para el token
     *
     * @return Clave secreta para firmar/validar tokens
     */
    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
