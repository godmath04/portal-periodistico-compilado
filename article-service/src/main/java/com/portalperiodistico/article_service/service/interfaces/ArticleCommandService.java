package com.portalperiodistico.article_service.service.interfaces;

import com.portalperiodistico.article_service.domain.dto.ArticleCreateRequest;
import com.portalperiodistico.article_service.domain.dto.ArticleDto;
import com.portalperiodistico.article_service.domain.dto.ArticleUpdateRequest;

/**
 * Interfaz para operaciones de comando (escritura) sobre artículos
 *
 * Principios SOLID aplicados:
 * - Interface Segregation Principle (ISP): Segregada de operaciones de consulta y workflow
 * - Dependency Inversion Principle (DIP): Abstracción para operaciones de escritura
 *
 * Esta interfaz implementa el patrón CQRS (Command Query Responsibility Segregation)
 * separando las operaciones de escritura de las de lectura para mejor diseño y escalabilidad.
 */
public interface ArticleCommandService {

    /**
     * Crea un nuevo artículo en estado "Borrador"
     *
     * @param createRequest Datos del artículo a crear
     * @param authenticatedUserId ID del usuario autenticado (autor)
     * @return Artículo creado en estado Borrador
     */
    ArticleDto createDraftArticle(ArticleCreateRequest createRequest, Integer authenticatedUserId);

    /**
     * Actualiza un artículo existente
     *
     * Solo el autor puede actualizar su artículo y solo si está en estado "Borrador" u "Observado"
     *
     * @param articleId ID del artículo a actualizar
     * @param updateRequest Nuevos datos del artículo
     * @param authenticatedUserId ID del usuario autenticado
     * @return Artículo actualizado
     * @throws RuntimeException si el usuario no es el autor o el artículo no está en estado editable
     */
    ArticleDto updateArticle(Long articleId, ArticleUpdateRequest updateRequest, Integer authenticatedUserId);

    /**
     * Elimina un artículo
     *
     * Solo el autor puede eliminar su artículo
     *
     * @param articleId ID del artículo a eliminar
     * @param authenticatedUserId ID del usuario autenticado
     * @throws RuntimeException si el usuario no es el autor
     */
    void deleteArticle(Long articleId, Integer authenticatedUserId);
}
