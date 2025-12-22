package com.portalperiodistico.article_service.domain.observer.impl;

import com.portalperiodistico.article_service.domain.entity.Article;
import com.portalperiodistico.article_service.domain.observer.ArticleStateChangeObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Observador que registra auditoría de cambios de estado de artículos
 *
 * Patrón de Diseño: Observer Pattern
 *
 * Este observador mantiene un registro de auditoría de todos los cambios
 * de estado, útil para:
 * - Trazabilidad y compliance
 * - Análisis de tiempos de aprobación
 * - Resolución de disputas
 * - Métricas y reportes
 *
 * Implementación actual: Logs en consola (mock)
 * Implementación futura: Persistir en tabla de auditoría o enviar a sistema de logging externo
 */
@Component
@Slf4j
public class AuditLogObserver implements ArticleStateChangeObserver {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    /**
     * Registra el cambio de estado en el log de auditoría
     *
     * Información registrada:
     * - Timestamp del cambio
     * - ID y título del artículo
     * - Estado anterior y nuevo
     * - Mensaje descriptivo
     *
     * @param article Artículo que cambió de estado
     * @param oldStateName Estado anterior
     * @param newStateName Nuevo estado
     * @param message Mensaje del cambio
     */
    @Override
    public void onStateChange(Article article, String oldStateName, String newStateName, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);

        // TODO: Implementar persistencia en tabla de auditoría
        // auditRepository.save(new AuditLog(timestamp, article.getId(), oldState, newState, message));

        log.info("[AUDIT] {} | Artículo ID: {} | Título: '{}' | {} → {} | {}",
                timestamp,
                article.getIdArticle(),
                article.getTitle(),
                oldStateName,
                newStateName,
                message);

        // Ejemplo de implementación futura:
        // AuditLog auditLog = AuditLog.builder()
        //     .timestamp(LocalDateTime.now())
        //     .articleId(article.getIdArticle())
        //     .articleTitle(article.getTitle())
        //     .oldState(oldStateName)
        //     .newState(newStateName)
        //     .message(message)
        //     .build();
        // auditRepository.save(auditLog);
    }
}
