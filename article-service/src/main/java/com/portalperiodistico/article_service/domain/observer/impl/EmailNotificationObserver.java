package com.portalperiodistico.article_service.domain.observer.impl;

import com.portalperiodistico.article_service.domain.entity.Article;
import com.portalperiodistico.article_service.domain.observer.ArticleStateChangeObserver;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Observador que envía notificaciones por email cuando un artículo cambia de estado
 *
 * Patrón de Diseño: Observer Pattern
 *
 * Este observador implementa la lógica de envío de emails sin que el código
 * de transición de estados conozca esta funcionalidad.
 *
 * Implementación actual: Solo registra logs (mock)
 * Implementación futura: Integrar con servicio de email (SendGrid, AWS SES, etc.)
 */
@Component
@Slf4j
public class EmailNotificationObserver implements ArticleStateChangeObserver {

    /**
     * Envía notificación por email al autor del artículo
     *
     * Escenarios de notificación:
     * - Borrador → En revisión: Confirmar al autor que se envió a revisión
     * - En revisión → Publicado: Felicitar al autor por la publicación
     * - En revisión → Observado: Notificar rechazo y solicitar correcciones
     *
     * @param article Artículo que cambió de estado
     * @param oldStateName Estado anterior
     * @param newStateName Nuevo estado
     * @param message Mensaje del cambio
     */
    @Override
    public void onStateChange(Article article, String oldStateName, String newStateName, String message) {
        // TODO: Implementar envío real de email con servicio de correo
        // Por ahora, solo registramos en logs

        log.info("=== NOTIFICACIÓN EMAIL (MOCK) ===");
        log.info("Destinatario: Autor del artículo ID {}", article.getAuthorId());
        log.info("Asunto: Tu artículo '{}' cambió de estado", article.getTitle());
        log.info("Estado anterior: {}", oldStateName);
        log.info("Estado nuevo: {}", newStateName);
        log.info("Mensaje: {}", message);
        log.info("================================");

        // Ejemplo de implementación futura:
        // emailService.sendEmail(
        //     to: getUserEmail(article.getAuthorId()),
        //     subject: "Tu artículo '" + article.getTitle() + "' cambió de estado",
        //     body: buildEmailBody(article, oldStateName, newStateName, message)
        // );
    }
}
