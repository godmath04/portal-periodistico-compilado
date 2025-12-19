package com.portalperiodistico.suggestion_service.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * Configuración de RestTemplate como bean de Spring
 *
 * Principio SOLID aplicado: Dependency Inversion Principle (DIP)
 *
 * En lugar de crear instancias de RestTemplate directamente en los servicios
 * (acoplamiento), se configura como un bean para que Spring lo inyecte.
 * Esto permite:
 * - Mejor testabilidad (se puede mockear fácilmente)
 * - Configuración centralizada
 * - Reutilización de la misma instancia
 */
@Configuration
public class RestTemplateConfig {

    /**
     * Crea y configura un bean de RestTemplate
     *
     * @return Instancia configurada de RestTemplate para realizar peticiones HTTP
     */
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
