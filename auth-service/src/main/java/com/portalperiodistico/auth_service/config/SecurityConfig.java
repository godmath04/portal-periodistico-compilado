package com.portalperiodistico.auth_service.config;

import com.portalperiodistico.auth_service.service.UserDetailsServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;

@Configuration
@EnableWebSecurity // Activa la seguridad web de Spring
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private UserDetailsServiceImpl userDetailsService;

    @Autowired
    private JwtAuthenticationFilter jwtAuthFilter;

    @Autowired
    private CorsConfigurationSource corsConfigurationSource;

    // --- BEAN 1: PasswordEncoder ---
    // Define el encriptador
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // --- BEAN 2: AuthenticationProvider ---
    // Le dice a Spring que use nuestro UserDetailsServiceImpl y
    // nuestro PasswordEncoder para verificar usuarios.
    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService); // Nuestro servicio de usuarios
        authProvider.setPasswordEncoder(passwordEncoder()); // Nuestro encriptador
        return authProvider;
    }

    // --- BEAN 3: AuthenticationManager ---
    // El gestor de autenticación, lo necesitaremos en el controlador
    // para procesar el login.
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    // --- BEAN 4: SecurityFilterChain (El "Firewall") ---
    // Aquí definimos qué rutas son públicas y cuáles están protegidas.
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource))
                .csrf(csrf -> csrf.disable()) // Deshabilitamos CSRF (común en APIs REST)

                // Hacemos que la API sea "STATELESS" (sin estado).
                // No se crearán sesiones de usuario en el servidor.
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

                .authorizeHttpRequests(authz -> authz
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Definimos nuestras rutas públicas (login, registro, etc.)
                        // Por ahora, dejaremos /auth/** como público
                        .requestMatchers("/auth/**").permitAll()

                        // Todas las demás peticiones deben estar autenticadas
                        .anyRequest().authenticated()
                )

                // Usamos el proveedor de autenticación que definimos y filtro de JWT
                .authenticationProvider(authenticationProvider())
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}