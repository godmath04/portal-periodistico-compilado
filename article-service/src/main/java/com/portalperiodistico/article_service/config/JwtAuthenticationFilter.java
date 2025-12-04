package com.portalperiodistico.article_service.config;

import com.portalperiodistico.article_service.security.UserPrincipal;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.SecretKey;
import java.io.IOException;
import java.util.List;

/**
 * Filtro JWT para el article-service.
 * Extrae la informacion del JWT sin consultar la base de datos.
 */
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Value("${jwt.secret}")
    private String secretKey;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. Obtener la cabecera 'Authorization'
        final String authHeader = request.getHeader("Authorization");

        // 2. Verificar formato "Bearer <token>"
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            // 3. Extraer el token
            String jwt = authHeader.substring(7);

            // 4. Parsear el JWT y extraer los claims
            byte[] keyBytes = Decoders.BASE64.decode(secretKey);
            SecretKey key = Keys.hmacShaKeyFor(keyBytes);

            Claims claims = Jwts.parser()
                    .verifyWith(key)
                    .build()
                    .parseSignedClaims(jwt)
                    .getPayload();

            // 5. Extraer datos del usuario desde los claims
            String username = claims.getSubject();
            Integer userId = claims.get("userId", Integer.class);
            List<String> roles = claims.get("roles", List.class);

            // 6. Solo autenticar si no esta ya autenticado
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // 7. Crear el UserPrincipal con los datos del JWT
                UserPrincipal userPrincipal = UserPrincipal.create(userId, username, roles);

                // 8. Crear el token de autenticacion
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userPrincipal,
                        null,
                        userPrincipal.getAuthorities()
                );

                // 9. Establecer en el contexto de seguridad
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }

            filterChain.doFilter(request, response);

        } catch (Exception e) {
            logger.warn("No se pudo procesar el token JWT: " + e.getMessage());
            filterChain.doFilter(request, response);
        }
    }
}