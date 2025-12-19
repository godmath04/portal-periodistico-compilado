package com.portalperiodistico.suggestion_service.service.impl;

import com.portalperiodistico.suggestion_service.domain.entity.PromptConfig;
import com.portalperiodistico.suggestion_service.domain.repository.PromptConfigRepository;
import com.portalperiodistico.suggestion_service.service.interfaces.AIProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Implementación del proveedor de IA usando Google Gemini
 *
 * Principios SOLID aplicados:
 * - Dependency Inversion Principle (DIP): Implementa la interfaz AIProvider,
 *   permitiendo que los consumidores dependan de la abstracción
 * - Dependency Inversion Principle (DIP): Inyecta RestTemplate en lugar de crearlo
 *   directamente, mejorando la testabilidad
 * - Open/Closed Principle (OCP): Si se requiere otro proveedor de IA (OpenAI, Claude),
 *   solo se crea una nueva implementación de AIProvider sin modificar código existente
 *
 * Esta clase reemplaza a GeminiService, ahora con inyección de dependencias adecuada.
 */
@Service
@RequiredArgsConstructor
public class GeminiAIProvider implements AIProvider {

    private final RestTemplate restTemplate; // Inyectado, no creado manualmente
    private final PromptConfigRepository promptConfigRepository;

    @Value("${google.gemini.api-key}")
    private String apiKey;

    private static final String GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=";

    // Prompt por defecto (respaldo si no hay nada en la base de datos)
    private static final String DEFAULT_PROMPT = "Actúa como un editor de noticias en Ecuador. " +
            "Tengo estas tendencias actuales: {TRENDS}. " +
            "Dame 3 sugerencias de artículos creativos para mi portal periodístico basados en ellas. " +
            "Devuélveme SOLO un arreglo JSON de strings con los títulos sugeridos, sin formato markdown ni explicaciones. " +
            "Ejemplo: [\"Titulo 1\", \"Titulo 2\"]";

    /**
     * Genera sugerencias de artículos usando la API de Google Gemini
     *
     * @param trends Lista de tendencias actuales obtenidas del TrendsProvider
     * @return JSON con sugerencias de títulos de artículos
     */
    @Override
    public String getSuggestions(List<String> trends) {
        String url = GEMINI_URL + apiKey;

        // 1. Obtener el prompt de la base de datos (o usar el default)
        String promptTemplate = getPromptTemplate();

        // 2. Reemplazar el placeholder {TRENDS} con las tendencias reales
        String prompt = promptTemplate.replace("{TRENDS}", String.join(", ", trends));

        // 3. Construir el cuerpo del JSON para Gemini
        // Estructura: { "contents": [{ "parts": [{ "text": "PROMPT" }] }] }
        Map<String, String> part = new HashMap<>();
        part.put("text", prompt);

        Map<String, Object> content = new HashMap<>();
        content.put("parts", List.of(part));

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("contents", List.of(content));

        // 4. Hacer la petición HTTP POST
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity = new HttpEntity<>(requestBody, headers);

        try {
            ResponseEntity<Map> response = restTemplate.postForEntity(url, entity, Map.class);

            // 5. Extraer el texto de la respuesta
            Map<String, Object> body = response.getBody();
            if (body == null) return "[\"Error: Respuesta vacía\"]";

            List<Map> candidates = (List<Map>) body.get("candidates");
            if (candidates == null || candidates.isEmpty()) return "[\"Error: Sin candidatos\"]";

            Map contentResponse = (Map) candidates.get(0).get("content");
            List<Map> parts = (List<Map>) contentResponse.get("parts");
            String text = (String) parts.get(0).get("text");

            return text; // Devuelve el JSON generado por la IA

        } catch (Exception e) {
            e.printStackTrace();
            // Devolvemos un JSON válido de error para que el frontend no se rompa
            return "[\"Error generando sugerencias con IA: " + e.getMessage() + "\"]";
        }
    }

    /**
     * Obtiene el template de prompt desde la base de datos
     *
     * @return Template de prompt configurado, o el DEFAULT_PROMPT si no existe en BD
     */
    private String getPromptTemplate() {
        // Buscamos el ID 1. Si no existe, usamos el DEFAULT_PROMPT.
        return promptConfigRepository.findById(1L)
                .map(PromptConfig::getPromptTemplate)
                .orElse(DEFAULT_PROMPT);
    }
}
