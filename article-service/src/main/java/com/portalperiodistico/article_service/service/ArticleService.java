package com.portalperiodistico.article_service.service;

import com.portalperiodistico.article_service.domain.dto.ArticleCreateRequest;
import com.portalperiodistico.article_service.domain.dto.ArticleDto;
import com.portalperiodistico.article_service.domain.dto.ArticleUpdateRequest;

import java.util.List;

public interface ArticleService {

    /**
     * Crea un nuevo artículo. El artículo se guarda inicialmente como "Borrador".
     *
     * @param createRequest El DTO con los datos del artículo (título, contenido).
     * @param authenticatedUserId El ID del usuario autenticado (autor).
     * @return El DTO del artículo creado.
     */
    ArticleDto createDraftArticle(ArticleCreateRequest createRequest, Integer authenticatedUserId);

    /**
     * Actualiza un articulo existente.
     * Solo se pueden editar articulos en estado "Borrador" u "Observado".
     */
    ArticleDto updateArticle(Long articleId, ArticleUpdateRequest updateRequest, Integer authenticatedUserId);
    /**
     * Elimina un articulo.
     * Solo se pueden eliminar articulos en estado "Borrador" u "Observado".
     */
    void deleteArticle(Long articleId, Integer authenticatedUserId);


    /**
     * Obtiene todos los artículos públicos (estado "Publicado").
     *
     * @return Lista de DTOs de artículos publicados.
     */
    List<ArticleDto> getAllPublishedArticles();

    /**
     * Obtiene los artículos escritos por un autor específico.
     *
     * @param authorId El ID del autor.
     * @return Lista de DTOs de artículos de ese autor.
     */
    List<ArticleDto> getArticlesByAuthor(Integer authorId);

    /**
     * Obtiene un artículo por su ID.
     *
     * @param articleId El ID del artículo.
     * @return El DTO del artículo encontrado.
     * @throws RuntimeException si el artículo no existe.
     */
    ArticleDto getArticleById(Long articleId);

    /**
     * Envía un artículo a revisión.
     * Solo se pueden enviar artículos en estado "Borrador".
     *
     * @param articleId El ID del artículo.
     * @param authenticatedUserId El ID del usuario autenticado (debe ser el autor).
     * @return El DTO del artículo actualizado.
     */
    ArticleDto sendArticleToReview(Long articleId, Integer authenticatedUserId);

    /**
     * Obtiene todos los artículos en estado "En revisión" (pendientes de aprobación).
     *
     * @return Lista de DTOs de artículos pendientes.
     */
    List<ArticleDto> getPendingArticles();

}