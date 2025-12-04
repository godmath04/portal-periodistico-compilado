package com.portalperiodistico.article_service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleStatusDto {

    private Integer idArticleStatus;
    private String statusName;
}