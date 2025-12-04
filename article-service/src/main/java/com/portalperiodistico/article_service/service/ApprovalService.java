package com.portalperiodistico.article_service.service;

import com.portalperiodistico.article_service.domain.dto.ApprovalRequest;
import com.portalperiodistico.article_service.domain.dto.ApprovalResponse;
import java.util.List;
public interface ApprovalService {

    /**
     * Procesa la aprobacion o rechazo de un articulo por parte de un usuario con un rol especifico.
     *
     * @param request Datos de la aprobacion (articleId, status, comments)
     * @param approverUserId ID del usuario que aprueba/rechaza (del JWT)
     * @param approverUsername Username del usuario (del JWT)
     * @param roleId ID del rol del usuario (del JWT)
     * @param roleName Nombre del rol (del JWT)
     * @param approvalWeight Peso de aprobacion del rol (del JWT o de BD)
     * @return Respuesta con el estado actualizado del articulo
     */
    ApprovalResponse processApproval(
            ApprovalRequest request,
            Integer approverUserId,
            String approverUsername,
            Integer roleId,
            String roleName,
            java.math.BigDecimal approvalWeight
    );

    /**
     * Obtiene el historial de aprobaciones de un articulo.
     *
     * @param articleId ID del articulo
     * @return Lista de respuestas con el historial de aprobaciones
     */
    List<ApprovalResponse> getApprovalHistory(Long articleId);
}