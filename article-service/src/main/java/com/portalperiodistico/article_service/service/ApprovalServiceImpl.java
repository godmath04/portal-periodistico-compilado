package com.portalperiodistico.article_service.service;

import com.portalperiodistico.article_service.domain.dto.ApprovalRequest;
import com.portalperiodistico.article_service.domain.dto.ApprovalResponse;
import com.portalperiodistico.article_service.domain.entity.Article;
import com.portalperiodistico.article_service.domain.entity.ArticleApproval;
import com.portalperiodistico.article_service.domain.entity.ArticleStatus;
import com.portalperiodistico.article_service.domain.repository.ArticleApprovalRepository;
import com.portalperiodistico.article_service.domain.repository.ArticleRepository;
import com.portalperiodistico.article_service.domain.repository.ArticleStatusRepository;
import com.portalperiodistico.article_service.domain.observer.ArticleStateChangeNotifier;
import com.portalperiodistico.article_service.domain.state.ArticleState;
import com.portalperiodistico.article_service.domain.state.ArticleStateFactory;
import com.portalperiodistico.article_service.domain.state.ArticleStateTransition;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de aprobaciones usando State Pattern
 *
 * Principios SOLID aplicados:
 * - Open/Closed Principle (OCP): Usa State Pattern para transiciones de estado,
 *   permitiendo agregar nuevos estados sin modificar este código
 * - Dependency Inversion Principle (DIP): Depende de ArticleState (abstracción)
 *
 * Antes: Lógica de transiciones hardcodeada con if/else y strings
 * Ahora: State Pattern encapsula la lógica de cada estado
 */
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ArticleRepository articleRepository;
    private final ArticleApprovalRepository approvalRepository;
    private final ArticleStatusRepository articleStatusRepository;
    private final ArticleStateFactory articleStateFactory;
    private final ArticleStateChangeNotifier stateChangeNotifier;

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

        // 4. Procesar usando State Pattern
        // Obtener el estado actual del artículo
        String currentStateName = article.getArticleStatus().getStatusName();
        ArticleState currentState = articleStateFactory.getState(currentStateName);

        // Delegar la transición al estado correspondiente
        ArticleStateTransition transition;
        if (APPROVAL_REJECTED.equals(request.getStatus())) {
            transition = currentState.processRejection(article);
        } else {
            transition = currentState.processApproval(article, approvalWeight);
        }

        // 5. Aplicar la transición retornada por el estado
        ArticleStatus newStatus = articleStatusRepository.findByStatusName(transition.getNewStateName())
                .orElseThrow(() -> new RuntimeException("Estado '" + transition.getNewStateName() + "' no encontrado en la BD"));

        article.setArticleStatus(newStatus);
        articleRepository.save(article);

        // 5.1. Notificar a todos los observadores sobre el cambio de estado (Observer Pattern)
        stateChangeNotifier.notifyStateChange(article, currentStateName, transition.getNewStateName(), transition.getMessage());

        // 6. Construir la respuesta usando los datos de la transición
        return new ApprovalResponse(
                article.getIdArticle(),
                article.getTitle(),
                approverUsername,
                roleName,
                approvalWeight,
                request.getStatus(),
                transition.getNewPercentage(),
                transition.getNewStateName(),
                transition.getMessage()
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