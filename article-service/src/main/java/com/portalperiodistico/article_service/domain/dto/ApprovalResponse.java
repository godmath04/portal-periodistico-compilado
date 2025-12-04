package com.portalperiodistico.article_service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ApprovalResponse {

    private Long articleId;
    private String articleTitle;
    private String approverUsername;
    private String roleName;
    private BigDecimal approvalWeight;
    private String status; // APPROVED o REJECTED
    private BigDecimal currentApprovalPercentage;
    private String articleStatus; // Borrador, En revision, Publicado, Observado
    private String message;
}