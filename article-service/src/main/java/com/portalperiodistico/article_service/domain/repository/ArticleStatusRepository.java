package com.portalperiodistico.article_service.domain.repository;

import com.portalperiodistico.article_service.domain.entity.ArticleStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ArticleStatusRepository extends JpaRepository<ArticleStatus, Integer> {
    Optional<ArticleStatus> findByStatusName(String statusName);
}