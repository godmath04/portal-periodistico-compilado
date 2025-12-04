package com.portalperiodistico.article_service.domain.repository;

import com.portalperiodistico.article_service.domain.entity.ArticleApproval;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ArticleApprovalRepository extends JpaRepository<ArticleApproval, Long> {

    // Buscar todas las aprobaciones de un articulo
    List<ArticleApproval> findAllByArticle_IdArticle(Long articleId);

    // Verificar si un rol ya aprobo/rechazo un articulo especifico
    Optional<ArticleApproval> findByArticle_IdArticleAndRoleId(Long articleId, Integer roleId);

    // Contar aprobaciones de un articulo con estado APPROVED
    long countByArticle_IdArticleAndStatus(Long articleId, String status);

    // Obtener todas las aprobaciones de un articulo ordenadas por fecha (más reciente primero)
    List<ArticleApproval> findByArticle_IdArticleOrderByTimestampDesc(Long articleId);
}