package com.portalperiodistico.article_service.domain.state.impl;

import com.portalperiodistico.article_service.domain.entity.Article;
import com.portalperiodistico.article_service.domain.state.ArticleState;
import com.portalperiodistico.article_service.domain.state.ArticleStateTransition;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Estado: En revisión
 *
 * El artículo está siendo revisado por editores/jefes de redacción.
 * En este estado el artículo NO puede editarse y está esperando aprobaciones.
 *
 * Transiciones permitidas:
 * - Aprobar → Suma peso de aprobación
 *   - Si llega a 100% → "Publicado"
 *   - Si no llega a 100% → Permanece "En revision"
 * - Rechazar → "Observado"
 * - No puede enviarse a revisión (ya está en revisión)
 * - No puede editarse (está en proceso de revisión)
 *
 * Principio Open/Closed aplicado: Este estado encapsula la lógica de aprobación.
 * Cambios en el umbral de aprobación o nuevas reglas se manejan aquí sin afectar otros estados.
 */
@Component
public class EnRevisionState implements ArticleState {

    private static final BigDecimal APPROVAL_THRESHOLD = BigDecimal.valueOf(100);

    @Override
    public String getStateName() {
        return "En revision";
    }

    @Override
    public boolean canEdit() {
        return false; // Los artículos en revisión no pueden editarse
    }

    @Override
    public boolean canSendToReview() {
        return false; // Ya está en revisión, no puede reenviarse
    }

    /**
     * Procesa una aprobación sumando el peso del rol aprobador
     *
     * Si el porcentaje acumulado llega a 100%, el artículo se publica.
     * Si no llega a 100%, permanece en revisión esperando más aprobaciones.
     *
     * @param article Artículo a aprobar
     * @param approvalWeight Peso de aprobación del rol (ej: 30.00, 50.00)
     * @return Transición con el nuevo estado y porcentaje
     */
    @Override
    public ArticleStateTransition processApproval(Article article, BigDecimal approvalWeight) {
        // Sumar el porcentaje de aprobación
        BigDecimal newPercentage = article.getCurrentApprovalPercentage().add(approvalWeight);
        article.setCurrentApprovalPercentage(newPercentage);

        // Verificar si alcanzó el 100% para publicar
        if (newPercentage.compareTo(APPROVAL_THRESHOLD) >= 0) {
            // Transición a Publicado
            return new ArticleStateTransition(
                    "Publicado",
                    newPercentage,
                    "El artículo ha alcanzado el " + newPercentage + "% de aprobación y ha sido PUBLICADO."
            );
        } else {
            // Mantener en revisión
            return new ArticleStateTransition(
                    "En revision",
                    newPercentage,
                    "Progreso actual: " + newPercentage + "%. El artículo permanece en revisión."
            );
        }
    }

    /**
     * Procesa un rechazo cambiando el estado a "Observado"
     *
     * Cuando un revisor rechaza el artículo, este pasa a estado Observado
     * para que el autor pueda realizar correcciones.
     *
     * @param article Artículo a rechazar
     * @return Transición al estado Observado
     */
    @Override
    public ArticleStateTransition processRejection(Article article) {
        // Transición a Observado
        return new ArticleStateTransition(
                "Observado",
                article.getCurrentApprovalPercentage(), // El porcentaje no cambia
                "El artículo ha sido rechazado y marcado como Observado. " +
                        "El autor debe realizar las correcciones necesarias."
        );
    }
}
