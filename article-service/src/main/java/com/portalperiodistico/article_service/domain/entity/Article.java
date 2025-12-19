package com.portalperiodistico.article_service.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "[ARTICLE]")
public class Article {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "[ArticleID]")
    private Long idArticle;

    @Column(name = "[Title]", nullable = false, length = 255)
    private String title;

    @Column(name = "[Content]", nullable = false, columnDefinition = "TEXT")
    private String content;

    @CreationTimestamp
    @Column(name = "[CreatedAt]", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "[UpdatedAt]")
    private LocalDateTime updatedAt;

    @Column(name = "[AuthorID]", nullable = false)
    private Integer authorId;

    @Column(name = "[CurrentApprovalPercentage]", precision = 5, scale = 2)
    private BigDecimal currentApprovalPercentage = BigDecimal.ZERO;

    // Relacion con ArticleStatus
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "[IdArticleStatus]", referencedColumnName = "[IdArticleStatus]", nullable = false)
    private ArticleStatus articleStatus;
}