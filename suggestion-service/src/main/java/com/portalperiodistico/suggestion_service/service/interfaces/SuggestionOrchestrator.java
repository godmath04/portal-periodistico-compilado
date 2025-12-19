package com.portalperiodistico.suggestion_service.service.interfaces;

/**
 * Interfaz para orquestador de generación de sugerencias
 *
 * Principios SOLID aplicados:
 * - Dependency Inversion Principle (DIP): Los controllers dependerán de esta abstracción
 * - Interface Segregation Principle (ISP): Interfaz pequeña y focalizada
 *
 * Este orquestador coordina la obtención de tendencias y la generación de sugerencias con IA,
 * encapsulando la lógica de coordinación entre TrendsProvider y AIProvider.
 * Esto simplifica el controller y permite cambiar la lógica de orquestación sin afectar
 * la capa de presentación.
 */
public interface SuggestionOrchestrator {

    /**
     * Genera sugerencias de artículos combinando tendencias actuales e inteligencia artificial
     *
     * Este método orquesta el flujo completo:
     * 1. Obtiene las tendencias actuales del TrendsProvider
     * 2. Solicita sugerencias al AIProvider basadas en esas tendencias
     * 3. Retorna el resultado en formato JSON
     *
     * @return JSON con sugerencias de títulos de artículos
     *         Ejemplo: ["Título 1", "Título 2", "Título 3"]
     */
    String generateSuggestions();
}
