package com.portalperiodistico.article_service.service;

import com.portalperiodistico.article_service.domain.dto.ApprovalRequest;
import com.portalperiodistico.article_service.domain.dto.ApprovalResponse;
import com.portalperiodistico.article_service.domain.entity.Article;
import com.portalperiodistico.article_service.domain.entity.ArticleApproval;
import com.portalperiodistico.article_service.domain.entity.ArticleStatus;
import com.portalperiodistico.article_service.domain.repository.ArticleApprovalRepository;
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
public class ApprovalServiceImpl implements ApprovalService {

    private final ArticleRepository articleRepository;
    private final ArticleApprovalRepository approvalRepository;
    private final ArticleStatusRepository articleStatusRepository;

    private static final String STATUS_DRAFT = "Borrador";
    private static final String STATUS_IN_REVIEW = "En revision";
    private static final String STATUS_PUBLISHED = "Publicado";
    private static final String STATUS_REJECTED = "Observado";

    private static final String APPROVAL_APPROVED = "APPROVED";
    private static final String APPROVAL_REJECTED = "REJECTED";

    @Override
    @Transactional
    public ApprovalResponse processApproval(
            ApprovalRequest request,
            Integer approverUserId,
            String approverUsername,
            Integer roleId,
            String roleName,
            BigDecimal approvalWeight
    ) {

        // 1. Buscar el articulo
        Article article = articleRepository.findById(request.getArticleId())
                .orElseThrow(() -> new RuntimeException("Articulo no encontrado con ID: " + request.getArticleId()));

        // 2. Verificar que el rol no haya aprobado/rechazado ya este articulo
        approvalRepository.findByArticle_IdArticleAndRoleId(request.getArticleId(), roleId)
                .ifPresent(existing -> {
                    throw new RuntimeException("El rol '" + roleName + "' ya ha revisado este articulo previamente");
                });

        // 3. Crear el registro de aprobacion
        ArticleApproval approval = new ArticleApproval();
        approval.setArticle(article);
        approval.setApproverUserId(approverUserId);
        approval.setApproverUsername(approverUsername);
        approval.setRoleId(roleId);
        approval.setRoleName(roleName);
        approval.setApprovalWeight(approvalWeight);
        approval.setStatus(request.getStatus());
        approval.setComments(request.getComments());

        approvalRepository.save(approval);

        // 4. Procesar segun el estado
        String newArticleStatus;
        BigDecimal newPercentage;
        String message;

        if (APPROVAL_REJECTED.equals(request.getStatus())) {
            // Si es RECHAZADO, el articulo pasa a "Observado"
            newArticleStatus = STATUS_REJECTED;
            newPercentage = article.getCurrentApprovalPercentage(); // No cambia el porcentaje
            message = "Articulo rechazado por " + roleName + ". El articulo ha sido marcado como Observado.";

        } else {
            // Si es APROBADO, sumar el porcentaje
            newPercentage = article.getCurrentApprovalPercentage().add(approvalWeight);
            article.setCurrentApprovalPercentage(newPercentage);

            if (newPercentage.compareTo(BigDecimal.valueOf(100)) >= 0) {
                // Si llega al 100%, publicar
                newArticleStatus = STATUS_PUBLISHED;
                message = "Articulo aprobado por " + roleName + ". El articulo ha alcanzado el 100% y ha sido PUBLICADO.";
            } else {
                // Si no llega al 100%, mantener en revision
                newArticleStatus = STATUS_IN_REVIEW;
                message = "Articulo aprobado por " + roleName + ". Progreso actual: " + newPercentage + "%";
            }
        }

        // 5. Actualizar el estado del articulo
        ArticleStatus status = articleStatusRepository.findByStatusName(newArticleStatus)
                .orElseThrow(() -> new RuntimeException("Estado '" + newArticleStatus + "' no encontrado en la BD"));

        article.setArticleStatus(status);
        articleRepository.save(article);

        // 6. Construir la respuesta
        return new ApprovalResponse(
                article.getIdArticle(),
                article.getTitle(),
                approverUsername,
                roleName,
                approvalWeight,
                request.getStatus(),
                newPercentage,
                newArticleStatus,
                message
        );
    }
    @Override
    @Transactional(readOnly = true)
    public List<ApprovalResponse> getApprovalHistory(Long articleId) {

        // 1. Verificar que el articulo existe
        Article article = articleRepository.findById(articleId)
                .orElseThrow(() -> new RuntimeException("Articulo no encontrado con ID: " + articleId));

        // 2. Obtener todas las aprobaciones del articulo ordenadas por fecha
        List<ArticleApproval> approvals = approvalRepository.findByArticle_IdArticleOrderByTimestampDesc(articleId);

        // 3. Mapear a DTOs
        return approvals.stream()
                .map(approval -> new ApprovalResponse(
                        article.getIdArticle(),
                        article.getTitle(),
                        approval.getApproverUsername(), // No tenemos username en la entidad
                        approval.getRoleName(),
                        approval.getApprovalWeight(),
                        approval.getStatus(),
                        article.getCurrentApprovalPercentage(),
                        article.getArticleStatus().getStatusName(),
                        approval.getComments() != null ? approval.getComments() : "Sin comentarios"
                ))
                .collect(Collectors.toList());
    }
}