package com.portalperiodistico.article_service.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalRequest {

    @NotNull(message = "El ID del articulo es requerido")
    private Long articleId;

    @NotNull(message = "El estado es requerido")
    @Pattern(regexp = "APPROVED|REJECTED", message = "El estado debe ser APPROVED o REJECTED")
    private String status;

    private String comments; // Opcional
}