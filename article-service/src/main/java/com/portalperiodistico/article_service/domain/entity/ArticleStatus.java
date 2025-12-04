package com.portalperiodistico.article_service.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "[ARTICLE_STATUS]")
public class ArticleStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "[IdArticleStatus]")
    private Integer idArticleStatus;

    @Column(name = "[StatusName]", nullable = false, length = 50)
    private String statusName;

    // Relación: Un estado puede tener muchos artículos
    // 'mappedBy' apunta al nombre del CAMPO en la clase Article
    @OneToMany(mappedBy = "articleStatus")
    private Set<Article> articles;
}