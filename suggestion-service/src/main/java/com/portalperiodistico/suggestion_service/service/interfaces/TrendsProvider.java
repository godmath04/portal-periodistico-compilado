package com.portalperiodistico.suggestion_service.service.interfaces;

import java.util.List;

/**
 * Interfaz para proveedores de tendencias actuales
 *
 * Principios SOLID aplicados:
 * - Dependency Inversion Principle (DIP): Abstracción sobre diferentes fuentes de tendencias
 * - Open/Closed Principle (OCP): Permite agregar nuevas fuentes de tendencias
 *   (Twitter API, Reddit, Google News, etc.) sin modificar código existente
 * - Interface Segregation Principle (ISP): Interfaz pequeña y focalizada con un solo método
 *
 * Esta abstracción permite cambiar la fuente de tendencias (de RSS a API de Twitter,
 * por ejemplo) sin afectar el resto del sistema.
 */
public interface TrendsProvider {

    /**
     * Obtiene las tendencias actuales desde la fuente configurada
     *
     * @return Lista de strings con los temas/tendencias actuales
     *         Ejemplo: ["Elecciones Ecuador", "Copa América", "Tecnología 5G"]
     */
    List<String> getActualTrends();
}
