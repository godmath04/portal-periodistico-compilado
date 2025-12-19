package com.portalperiodistico.suggestion_service.service.impl;

import com.portalperiodistico.suggestion_service.service.interfaces.AIProvider;
import com.portalperiodistico.suggestion_service.service.interfaces.SuggestionOrchestrator;
import com.portalperiodistico.suggestion_service.service.interfaces.TrendsProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Implementación del orquestador de generación de sugerencias
 *
 * Principios SOLID aplicados:
 * - Dependency Inversion Principle (DIP): Depende de abstracciones (TrendsProvider, AIProvider)
 *   en lugar de implementaciones concretas
 * - Open/Closed Principle (OCP): Si cambia la lógica de orquestación, solo se modifica esta clase
 * - Interface Segregation Principle (ISP): Implementa una interfaz simple y focalizada
 *
 * Esta clase coordina la obtención de tendencias y la generación de sugerencias con IA,
 * desacoplando el controller de los detalles de implementación.
 */
@Service
@RequiredArgsConstructor
public class SuggestionOrchestratorImpl implements SuggestionOrchestrator {

    private final TrendsProvider trendsProvider;
    private final AIProvider aiProvider;

    /**
     * Genera sugerencias de artículos orquestando TrendsProvider y AIProvider
     *
     * Flujo:
     * 1. Obtiene las tendencias actuales del proveedor configurado (RSS, API, etc.)
     * 2. Envía las tendencias al proveedor de IA configurado (Gemini, OpenAI, etc.)
     * 3. Retorna las sugerencias generadas en formato JSON
     *
     * @return JSON con sugerencias de títulos de artículos
     */
    @Override
    public String generateSuggestions() {
        // 1. Obtener tendencias actuales
        List<String> trends = trendsProvider.getActualTrends();

        // 2. Generar sugerencias con IA basadas en las tendencias
        String suggestions = aiProvider.getSuggestions(trends);

        return suggestions;
    }
}
