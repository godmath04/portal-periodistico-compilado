package com.portalperiodistico.article_service.domain.observer;

import com.portalperiodistico.article_service.domain.entity.Article;

/**
 * Interfaz para observadores de cambios de estado de artículos
 *
 * Patrón de Diseño: Observer Pattern
 *
 * Este patrón permite que múltiples componentes del sistema sean notificados
 * cuando un artículo cambia de estado, sin acoplar el código de transición
 * de estados con las acciones que deben ejecutarse.
 *
 * Ventajas del Observer Pattern:
 * - Desacoplamiento: Los estados no conocen a los observadores
 * - Extensibilidad: Se pueden agregar nuevos observadores sin modificar estados
 * - Open/Closed Principle: Abierto para extensión, cerrado para modificación
 *
 * Ejemplos de uso:
 * - Enviar notificaciones por email al autor
 * - Registrar auditoría de cambios
 * - Actualizar caché
 * - Enviar eventos a sistemas externos
 * - Publicar en colas de mensajes (Kafka, RabbitMQ)
 */
public interface ArticleStateChangeObserver {

    /**
     * Notifica que un artículo ha cambiado de estado
     *
     * @param article Artículo que cambió de estado
     * @param oldStateName Nombre del estado anterior
     * @param newStateName Nombre del nuevo estado
     * @param message Mensaje descriptivo del cambio
     */
    void onStateChange(Article article, String oldStateName, String newStateName, String message);
}
