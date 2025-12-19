package com.portalperiodistico.article_service.service.interfaces;

import com.portalperiodistico.article_service.domain.dto.ArticleDto;

/**
 * Interfaz para operaciones de flujo de trabajo (workflow) sobre artículos
 *
 * Principios SOLID aplicados:
 * - Interface Segregation Principle (ISP): Segregada de operaciones de comando y consulta
 * - Dependency Inversion Principle (DIP): Abstracción para transiciones de estado
 *
 * Esta interfaz encapsula las transiciones de estado del artículo,
 * permitiendo futura extensión con nuevos flujos de trabajo sin modificar el código existente.
 */
public interface ArticleWorkflowService {

    /**
     * Envía un artículo a revisión
     *
     * Cambia el estado del artículo de "Borrador" a "En revision"
     * Solo el autor puede enviar su artículo a revisión
     *
     * @param articleId ID del artículo
     * @param authenticatedUserId ID del usuario autenticado (debe ser el autor)
     * @return Artículo con estado actualizado a "En revision"
     * @throws RuntimeException si el usuario no es el autor o el artículo no está en estado "Borrador"
     */
    ArticleDto sendArticleToReview(Long articleId, Integer authenticatedUserId);
}
