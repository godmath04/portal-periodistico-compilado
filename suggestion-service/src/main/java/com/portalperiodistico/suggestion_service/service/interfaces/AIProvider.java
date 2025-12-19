package com.portalperiodistico.suggestion_service.service.interfaces;

import java.util.List;

/**
 * Interfaz para proveedores de inteligencia artificial
 *
 * Principios SOLID aplicados:
 * - Dependency Inversion Principle (DIP): Los controllers dependerán de esta abstracción,
 *   no de implementaciones concretas
 * - Open/Closed Principle (OCP): Permite agregar nuevos proveedores de IA
 *   (OpenAI, Claude, Cohere, etc.) sin modificar el código existente
 * - Interface Segregation Principle (ISP): Interfaz pequeña y focalizada con un solo método
 *
 * Esta abstracción permite cambiar de proveedor de IA (de Gemini a OpenAI, por ejemplo)
 * sin afectar el resto del sistema.
 */
public interface AIProvider {

    /**
     * Genera sugerencias de artículos basadas en tendencias actuales
     *
     * @param trends Lista de tendencias actuales (temas populares, noticias del momento, etc.)
     * @return JSON con sugerencias de títulos de artículos en formato String
     *         Ejemplo: ["Título 1", "Título 2", "Título 3"]
     */
    String getSuggestions(List<String> trends);
}
