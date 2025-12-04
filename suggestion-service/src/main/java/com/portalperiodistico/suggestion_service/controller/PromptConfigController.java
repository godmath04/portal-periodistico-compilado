package com.portalperiodistico.suggestion_service.controller;

import com.portalperiodistico.suggestion_service.domain.dto.PromptConfigDto;
import com.portalperiodistico.suggestion_service.domain.dto.UpdatePromptRequestDto; // Import correcto
import com.portalperiodistico.suggestion_service.domain.entity.PromptConfig;
import com.portalperiodistico.suggestion_service.domain.repository.PromptConfigRepository; // Ajusté el paquete del repo al estándar
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/admin/prompt-config")
public class PromptConfigController {

    @Autowired
    private PromptConfigRepository promptConfigRepository;


    @GetMapping("/test")
    public ResponseEntity<Map<String, String>>
    testAuth( @RequestHeader(value = "Authorization", required = false) String authHeader
    ) {
        Map<String, String> response = new HashMap<>();
        response.put("authHeader", authHeader != null ? authHeader : "NULL");
        response.put("message", "Token received");
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasAuthority('Administrador')")
    public ResponseEntity<PromptConfigDto> getPromptConfig() {
        PromptConfig config = promptConfigRepository.findById(1L)
                .orElseGet(this::createDefaultConfig);

        String fechaString = (config.getUpdatedAt() != null)
                ? config.getUpdatedAt().toString()
                : null;

        PromptConfigDto dto = new PromptConfigDto(
                config.getIdPromptConfig(),
                config.getPromptTemplate(),
                fechaString,
                config.getUpdatedBy()
        );

        return ResponseEntity.ok(dto);
    }

    @PutMapping
    @PreAuthorize("hasAuthority('Administrador')")
    public ResponseEntity<PromptConfigDto> updatePromptConfig(
            // CORRECCIÓN AQUÍ: Usamos UpdatePromptRequestDto (con Dto al final)
            @RequestBody UpdatePromptRequestDto request,
            @RequestHeader(value = "X-User-Id", required = false) String userId) {

        PromptConfig config = promptConfigRepository.findById(1L)
                .orElseGet(() -> new PromptConfig());

        // Ahora sí funcionará getPromptTemplate() porque la clase es correcta
        config.setPromptTemplate(request.getPromptTemplate());
        config.setUpdatedBy(userId != null ? userId : "Sistema");
        config.setUpdatedAt(LocalDateTime.now());

        PromptConfig saved = promptConfigRepository.save(config);

        String fechaString = (saved.getUpdatedAt() != null)
                ? saved.getUpdatedAt().toString()
                : LocalDateTime.now().toString();

        PromptConfigDto dto = new PromptConfigDto(
                saved.getIdPromptConfig(),
                saved.getPromptTemplate(),
                fechaString,
                saved.getUpdatedBy()
        );

        return ResponseEntity.ok(dto);
    }

    private PromptConfig createDefaultConfig() {
        PromptConfig config = new PromptConfig();
        config.setPromptTemplate("Actúa como un editor...");
        config.setUpdatedAt(LocalDateTime.now());
        config.setUpdatedBy("Sistema");
        return promptConfigRepository.save(config);
    }
}