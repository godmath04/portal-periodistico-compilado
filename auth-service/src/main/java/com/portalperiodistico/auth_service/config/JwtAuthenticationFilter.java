package com.portalperiodistico.auth_service.config;

import com.portalperiodistico.auth_service.service.interfaces.TokenClaimsExtractor;
import com.portalperiodistico.auth_service.service.interfaces.TokenProvider;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * Filtro de autenticación JWT para Spring Security
 *
 * Principios SOLID aplicados:
 * - Dependency Inversion Principle (DIP): Depende de interfaces (TokenClaimsExtractor, TokenProvider)
 *   en lugar de implementaciones concretas
 * - Interface Segregation Principle (ISP): Usa interfaces segregadas según la necesidad
 *   (extractUsername usa TokenClaimsExtractor, validación usa TokenProvider)
 *
 * Antes: Filtro dependía de la clase concreta JwtService
 * Ahora: Filtro depende de abstracciones segregadas según su uso
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    // Inyección por constructor (inmutable)
    private final TokenClaimsExtractor tokenClaimsExtractor;
    private final TokenProvider tokenProvider;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Obtener la cabecera 'Authorization'
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // 2. Comprobar si la cabecera existe y tiene el formato "Bearer <token>"
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response); // Si no hay token, pasa al siguiente filtro
            return;
        }

        // 3. Extraer el token (sin el prefijo "Bearer ")
        jwt = authHeader.substring(7);

        try {
            // 4. Extraer el username del token usando TokenClaimsExtractor
            username = tokenClaimsExtractor.extractUsername(jwt);

            // 5. Validar el token
            //    Comprobamos que el username no sea nulo Y que el usuario no esté ya autenticado
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Cargamos los detalles del usuario desde la BD
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Si el token es válido usando TokenProvider...
                if (tokenProvider.validateToken(jwt, userDetails)) {
                    // ...creamos una autenticación para Spring Security
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null, // No pasamos credenciales (contraseña)
                            userDetails.getAuthorities()
                    );
                    authToken.setDetails(
                            new WebAuthenticationDetailsSource().buildDetails(request)
                    );

                    // 6. Establecemos al usuario como AUTENTICADO en el contexto de seguridad
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
            // 7. Continuar la cadena de filtros
            filterChain.doFilter(request, response);

        } catch (Exception e) {
            // Si el token está expirado o es inválido, simplemente no lo autenticamos
            // y continuamos. Spring Security lo manejará como "no autenticado".
            logger.warn("No se pudo procesar el token JWT: " + e.getMessage());
            filterChain.doFilter(request, response);
        }
    }
}