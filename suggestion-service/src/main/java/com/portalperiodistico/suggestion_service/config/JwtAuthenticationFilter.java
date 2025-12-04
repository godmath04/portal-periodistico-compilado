package com.portalperiodistico.suggestion_service.config; // 1. Ajustado al paquete real donde tienes el archivo

import com.portalperiodistico.suggestion_service.service.JwtService; // 2. Import necesario
import com.portalperiodistico.suggestion_service.security.UserPrincipal; // 3. Import necesario
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            jwt = authHeader.substring(7);
            username = jwtService.extractUsername(jwt);

            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                if (jwtService.isTokenValid(jwt)) {
                    // Extract roles from token
                    Claims claims = jwtService.extractClaim(jwt, claims1 -> claims1);
                    List<GrantedAuthority> authorities = new ArrayList<>();

                    Object rolesObj = claims.get("roles");
                    if (rolesObj instanceof List<?>) {
                        List<?> roles = (List<?>) rolesObj;
                        for (Object role : roles) {
                            if (role instanceof String) {
                                authorities.add(new SimpleGrantedAuthority((String) role));
                            }
                        }
                    }
                    System.out.println("Extracted authorities: " + authorities);

                    UserPrincipal userPrincipal = new UserPrincipal(username, authorities);

                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userPrincipal,
                            null,
                            authorities
                    );
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            System.out.println("Token inválido o expirado");
            System.out.println("Error details; " + e.getClass().getName() + " - " + e.getMessage());
            e.printStackTrace();
        }

        filterChain.doFilter(request, response);
    }
}