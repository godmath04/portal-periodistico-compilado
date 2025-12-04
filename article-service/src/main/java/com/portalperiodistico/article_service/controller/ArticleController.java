package com.portalperiodistico.article_service.controller;

import com.portalperiodistico.article_service.domain.dto.ArticleCreateRequest;
import com.portalperiodistico.article_service.domain.dto.ArticleDto;
import com.portalperiodistico.article_service.domain.dto.ArticleUpdateRequest;
import com.portalperiodistico.article_service.domain.entity.Article;
import com.portalperiodistico.article_service.security.UserPrincipal;
import com.portalperiodistico.article_service.service.ArticleService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/articles")
@RequiredArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    /**
     * Crear un nuevo articulo (Borrador)
     * POST /api/v1/articles
     * Requiere autenticacion
     */
    @PostMapping
    public ResponseEntity<ArticleDto> createDraftArticle(
            @Valid @RequestBody ArticleCreateRequest createRequest,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {

        if (authenticatedUser == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        ArticleDto createdArticle = articleService.createDraftArticle(
                createRequest,
                authenticatedUser.getUserId()
        );

        return new ResponseEntity<>(createdArticle, HttpStatus.CREATED);
    }

    /**
     * Actualizar un articulo existente
     * PUT /api/v1/articles/{id}
     * Requiere autenticacion
     */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateArticle(
            @PathVariable Long id,
            @Valid @RequestBody ArticleUpdateRequest updateRequest,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {

        if (authenticatedUser == null) {
            return new ResponseEntity<>("Usuario no autenticado", HttpStatus.UNAUTHORIZED);
        }

        try {
            ArticleDto updatedArticle = articleService.updateArticle(
                    id,
                    updateRequest,
                    authenticatedUser.getUserId()
            );
            return ResponseEntity.ok(updatedArticle);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Eliminar un articulo
     * DELETE /api/v1/articles/{id}
     * Requiere autenticacion
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteArticle(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {

        if (authenticatedUser == null) {
            return new ResponseEntity<>("Usuario no autenticado", HttpStatus.UNAUTHORIZED);
        }

        try {
            articleService.deleteArticle(id, authenticatedUser.getUserId());
            return ResponseEntity.ok("Articulo eliminado exitosamente");
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Obtener todos los articulos publicados
     * GET /api/v1/articles
     * Publico (no requiere autenticacion)
     */
    @GetMapping
    public ResponseEntity<List<ArticleDto>> getAllPublishedArticles() {
        List<ArticleDto> articles = articleService.getAllPublishedArticles();
        return ResponseEntity.ok(articles);
    }

    /**
     * Obtener articulos por autor
     * GET /api/v1/articles/author/{authorId}
     * Publico (no requiere autenticacion)
     */
    @GetMapping("/author/{authorId}")
    public ResponseEntity<List<ArticleDto>> getArticlesByAuthor(@PathVariable Integer authorId) {
        List<ArticleDto> articles = articleService.getArticlesByAuthor(authorId);
        return ResponseEntity.ok(articles);
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getArticleById(@PathVariable Long id) {
        try {
            ArticleDto article = articleService.getArticleById(id);
            return ResponseEntity.ok(article);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }

    /**
     * Enviar un artículo a revisión
     * PUT /api/v1/articles/{id}/send-to-review
     * Requiere autenticación
     */
    @PutMapping("/{id}/send-to-review")
    public ResponseEntity<?> sendArticleToReview(
            @PathVariable Long id,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {

        if (authenticatedUser == null) {
            return new ResponseEntity<>("Usuario no autenticado", HttpStatus.UNAUTHORIZED);
        }

        try {
            ArticleDto updatedArticle = articleService.sendArticleToReview(
                    id,
                    authenticatedUser.getUserId()
            );
            return ResponseEntity.ok(updatedArticle);
        } catch (RuntimeException e) {
            return new ResponseEntity<>(e.getMessage(), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Obtener artículos pendientes de aprobación (estado "En revision")
     * GET /api/v1/articles/pending
     * Requiere autenticación
     */
    @GetMapping("/pending")
    public ResponseEntity<List<ArticleDto>> getPendingArticles(
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {

        if (authenticatedUser == null) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        List<ArticleDto> pendingArticles = articleService.getPendingArticles();
        return ResponseEntity.ok(pendingArticles);
    }
}