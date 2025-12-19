package com.portalperiodistico.suggestion_service.controller;

import com.portalperiodistico.suggestion_service.service.interfaces.SuggestionOrchestrator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controlador REST para generación de sugerencias de artículos
 *
 * Principios SOLID aplicados:
 * - Dependency Inversion Principle (DIP): Depende de la interfaz SuggestionOrchestrator,
 *   no de implementaciones concretas. Esto permite cambiar la implementación sin modificar
 *   el controller.
 * - Open/Closed Principle (OCP): Cerrado para modificación, abierto para extensión.
 *   Nuevas funcionalidades se agregan creando nuevas implementaciones, no modificando este código.
 *
 * Antes: Controller dependía directamente de RssService y GeminiService (acoplamiento alto)
 * Ahora: Controller depende de SuggestionOrchestrator (abstracción), que coordina la lógica
 */
@RestController
@RequestMapping("/api/suggestions")
@RequiredArgsConstructor
public class SuggestionController {

    private final SuggestionOrchestrator suggestionOrchestrator;

    /**
     * Genera sugerencias de artículos basadas en tendencias actuales
     *
     * Este endpoint orquesta la obtención de tendencias y la generación de sugerencias con IA.
     * Toda la lógica está encapsulada en SuggestionOrchestrator.
     *
     * @return ResponseEntity con JSON de sugerencias de títulos de artículos
     *         Ejemplo: ["Título 1", "Título 2", "Título 3"]
     */
    @GetMapping("/generate")
    public ResponseEntity<String> generateSuggestions() {
        // El orquestador maneja toda la lógica de coordinación
        String suggestions = suggestionOrchestrator.generateSuggestions();
        return ResponseEntity.ok(suggestions);
    }
}