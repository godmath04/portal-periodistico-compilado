package com.portalperiodistico.article_service.domain.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ArticleUpdateRequest {

    @NotBlank(message = "El titulo es requerido")
    private String title;

    @NotBlank(message = "El contenido es requerido")
    private String content;
}