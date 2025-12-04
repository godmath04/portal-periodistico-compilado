package com.portalperiodistico.article_service.domain.repository;

import com.portalperiodistico.article_service.domain.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ArticleRepository extends JpaRepository<Article, Long> {
    List<Article> findAllByAuthorId(Integer authorId);
    List<Article> findAllByArticleStatus_StatusName(String statusName);
}