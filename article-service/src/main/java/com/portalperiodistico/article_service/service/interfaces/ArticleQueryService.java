package com.portalperiodistico.article_service.service.interfaces;

import com.portalperiodistico.article_service.domain.dto.ArticleDto;

import java.util.List;

/**
 * Interfaz para operaciones de consulta (lectura) sobre artículos
 *
 * Principios SOLID aplicados:
 * - Interface Segregation Principle (ISP): Segregada de operaciones de comando y workflow
 * - Dependency Inversion Principle (DIP): Abstracción para operaciones de lectura
 *
 * Esta interfaz implementa el patrón CQRS (Command Query Responsibility Segregation)
 * separando las operaciones de lectura de las de escritura.
 */
public interface ArticleQueryService {

    /**
     * Obtiene todos los artículos publicados
     *
     * Solo retorna artículos en estado "Publicado"
     *
     * @return Lista de artículos publicados
     */
    List<ArticleDto> getAllPublishedArticles();

    /**
     * Obtiene todos los artículos de un autor específico
     *
     * @param authorId ID del autor
     * @return Lista de artículos del autor (en cualquier estado)
     */
    List<ArticleDto> getArticlesByAuthor(Integer authorId);

    /**
     * Obtiene un artículo específico por su ID
     *
     * @param articleId ID del artículo
     * @return Artículo encontrado
     * @throws RuntimeException si el artículo no existe
     */
    ArticleDto getArticleById(Long articleId);

    /**
     * Obtiene todos los artículos pendientes de revisión
     *
     * Retorna artículos en estado "En revision"
     *
     * @return Lista de artículos en revisión
     */
    List<ArticleDto> getPendingArticles();
}
