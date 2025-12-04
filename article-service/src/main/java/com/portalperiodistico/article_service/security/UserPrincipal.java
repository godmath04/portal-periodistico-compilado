package com.portalperiodistico.article_service.security;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Clase que representa al usuario autenticado en el article-service.
 * Solo contiene los datos esenciales extraidos del JWT.
 */
@Getter
@AllArgsConstructor
public class UserPrincipal implements UserDetails {

    private Integer userId;
    private String username;
    private Collection<? extends GrantedAuthority> authorities;

    /**
     * Constructor conveniente para crear desde el JWT
     */
    public static UserPrincipal create(Integer userId, String username, List<String> roles) {
        List<GrantedAuthority> authorities = roles.stream()
                .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                .collect(Collectors.toList());

        return new UserPrincipal(userId, username, authorities);
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return null; // No necesitamos la contrasena aqui
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }
}