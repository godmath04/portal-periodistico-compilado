package com.portalperiodistico.article_service.service;

import com.portalperiodistico.article_service.domain.dto.ArticleCreateRequest;
import com.portalperiodistico.article_service.domain.dto.ArticleDto;
import com.portalperiodistico.article_service.domain.dto.ArticleStatusDto;
import com.portalperiodistico.article_service.domain.dto.ArticleUpdateRequest;
import com.portalperiodistico.article_service.domain.dto.AuthorDto;
import com.portalperiodistico.article_service.domain.entity.Article;
import com.portalperiodistico.article_service.domain.entity.ArticleStatus;
import com.portalperiodistico.article_service.domain.repository.ArticleRepository;
import com.portalperiodistico.article_service.domain.repository.ArticleStatusRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArticleServiceImpl implements ArticleService {

    private final ArticleRepository articleRepository;
    private final ArticleStatusRepository articleStatusRepository;

    private static final String STATUS_DRAFT = "Borrador";
    private static final String STATUS_PUBLISHED = "Publicado";
    private static final String STATUS_IN_REVIEW = "En revision";
    private static final String STATUS_REJECTED = "Observado";

    @Override
    @Transactional
    public ArticleDto createDraftArticle(ArticleCreateRequest createRequest, Integer authenticatedUserId) {

        // 1. Buscar el estado "Borrador"
        ArticleStatus draftStatus = articleStatusRepository.findByStatusName(STATUS_DRAFT)
                .orElseThrow(() -> new RuntimeException("Estado '" + STATUS_DRAFT + "' no configurado en la BD"));

        // 2. Crear el nuevo articulo
        Article newArticle = new Article();
        newArticle.setTitle(createRequest.getTitle());
        newArticle.setContent(createRequest.getContent());
        newArticle.setAuthorId(authenticatedUserId);
        newArticle.setArticleStatus(draftStatus);
        newArticle.setCurrentApprovalPercentage(BigDecimal.ZERO);

        // 3. Guardar el articulo
        Article savedArticle = articleRepository.save(newArticle);

        // 4. Convertir a DTO y retornar
        return mapToArticleDto(savedArticle);
    }
    @Override
    @Transactional
    public ArticleDto updateArticle(Long articleId, ArticleUpdateRequest updateRequest, Integer authenticatedUserId) {

        // 1. Buscar el articulo
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Articulo no encontrado con ID: " + articleId));

        // 2. Verificar que el usuario sea el autor
        if (!article.getAuthorId().equals(authenticatedUserId)) {
            throw new RuntimeException("No tienes permiso para editar este articulo. Solo el autor puede editarlo.");
        }

        // 3. Verificar que el articulo este en estado editable
        String currentStatus = article.getArticleStatus().getStatusName();
        if (!STATUS_DRAFT.equals(currentStatus) && !STATUS_REJECTED.equals(currentStatus)) {
            throw new RuntimeException("Solo se pueden editar articulos en estado 'Borrador' u 'Observado'. Estado actual: " + currentStatus);
        }

        // 4. Actualizar los campos
        article.setTitle(updateRequest.getTitle());
        article.setContent(updateRequest.getContent());

        // 5. Si el articulo estaba Observado, al editarlo vuelve a Borrador y resetear porcentaje
        if (STATUS_REJECTED.equals(currentStatus)) {
            ArticleStatus draftStatus = articleStatusRepository.findByStatusName(STATUS_DRAFT)
                    .orElseThrow(() -> new RuntimeException("Estado 'Borrador' no encontrado"));
            article.setArticleStatus(draftStatus);
            article.setCurrentApprovalPercentage(BigDecimal.ZERO);
        }

        // 6. Guardar cambios
        Article updatedArticle = articleRepository.save(article);

        // 7. Retornar DTO
        return mapToArticleDto(updatedArticle);
    }
    @Override
    @Transactional
    public void deleteArticle(Long articleId, Integer authenticatedUserId) {

        // 1. Buscar el articulo
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Articulo no encontrado con ID: " + articleId));

        // 2. Verificar que el usuario sea el autor
        if (!article.getAuthorId().equals(authenticatedUserId)) {
            throw new RuntimeException("No tienes permiso para eliminar este articulo. Solo el autor puede eliminarlo.");
        }

        // 3. Verificar que el articulo este en estado eliminable
        String currentStatus = article.getArticleStatus().getStatusName();
        if (!STATUS_DRAFT.equals(currentStatus) && !STATUS_REJECTED.equals(currentStatus)) {
            throw new RuntimeException("Solo se pueden eliminar articulos en estado 'Borrador' u 'Observado'. Estado actual: " + currentStatus);
        }

        // 4. Eliminar el articulo
        articleRepository.delete(article);
    }



    @Override
    @Transactional(readOnly = true)
    public List<ArticleDto> getAllPublishedArticles() {
        List<Article> publishedArticles = articleRepository.findAllByArticleStatus_StatusName(STATUS_PUBLISHED);
        return publishedArticles.stream()
                .map(this::mapToArticleDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleDto> getArticlesByAuthor(Integer authorId) {
        List<Article> authorArticles = articleRepository.findAllByAuthorId(authorId);

        return authorArticles.stream()
                .map(this::mapToArticleDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public ArticleDto getArticleById(Long articleId) {
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado con ID: " + articleId));

        return mapToArticleDto(article);
    }

    @Override
    @Transactional
    public ArticleDto sendArticleToReview(Long articleId, Integer authenticatedUserId) {

        // 1. Buscar el artículo
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Artículo no encontrado con ID: " + articleId));

        // 2. Verificar que el usuario sea el autor
        if (!article.getAuthorId().equals(authenticatedUserId)) {
            throw new RuntimeException("No tienes permiso para enviar este artículo a revisión. Solo el autor puede hacerlo.");
        }

        // 3. Verificar que el artículo esté en estado "Borrador"
        String currentStatus = article.getArticleStatus().getStatusName();
        if (!STATUS_DRAFT.equals(currentStatus)) {
            throw new RuntimeException("Solo se pueden enviar a revisión artículos en estado 'Borrador'. Estado actual: " + currentStatus);
        }

        // 4. Cambiar estado a "En revision"
        ArticleStatus inReviewStatus = articleStatusRepository.findByStatusName(STATUS_IN_REVIEW)
                .orElseThrow(() -> new RuntimeException("Estado 'En revision' no encontrado"));

        article.setArticleStatus(inReviewStatus);

        // 5. Guardar cambios
        Article updatedArticle = articleRepository.save(article);

        // 6. Retornar DTO
        return mapToArticleDto(updatedArticle);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ArticleDto> getPendingArticles() {

        // Buscar todos los artículos en estado "En revision"
        List<Article> pendingArticles = articleRepository.findAllByArticleStatus_StatusName(STATUS_IN_REVIEW);

        // Mapear a DTOs
        return pendingArticles.stream()
                .map(this::mapToArticleDto)
                .collect(Collectors.toList());
    }

    // --- MAPPERS PRIVADOS ---

    private ArticleDto mapToArticleDto(Article article) {
        return new ArticleDto(
                article.getIdArticle(),
                article.getTitle(),
                article.getContent(),
                article.getCreatedAt(),
                article.getUpdatedAt(),
                mapToAuthorDto(article.getAuthorId()),
                mapToStatusDto(article.getArticleStatus())
        );
    }

    private AuthorDto mapToAuthorDto(Integer authorId) {
        return new AuthorDto(
                authorId,
                null,  // username
                null,  // firstName
                null   // lastName
        );
    }

    private ArticleStatusDto mapToStatusDto(ArticleStatus status) {
        return new ArticleStatusDto(
                status.getIdArticleStatus(),
                status.getStatusName()
        );
    }
}