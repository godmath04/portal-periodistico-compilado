package com.portalperiodistico.suggestion_service.controller;

import com.portalperiodistico.suggestion_service.service.GeminiService;
import com.portalperiodistico.suggestion_service.service.RssService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {

    @Autowired
    private RssService rssService;

    @Autowired
    private GeminiService geminiService;

    @GetMapping("/generate")
    public ResponseEntity<String> generateSuggestions() {
        // 1. Obtener tendencias (Gratis)
        List<String> trends = rssService.getActualTrends();

        // 2. Preguntar a la IA (Gratis)
        String iaResponse = geminiService.getSuggestions(trends);

        // 3. Devolver resultado (Se devuelve como String crudo porque la IA ya manda JSON)
        return ResponseEntity.ok(iaResponse);
    }
}