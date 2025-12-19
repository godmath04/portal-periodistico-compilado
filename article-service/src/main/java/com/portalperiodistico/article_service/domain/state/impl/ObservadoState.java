package com.portalperiodistico.article_service.domain.state.impl;

import com.portalperiodistico.article_service.domain.entity.Article;
import com.portalperiodistico.article_service.domain.state.ArticleState;
import com.portalperiodistico.article_service.domain.state.ArticleStateTransition;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Estado: Observado
 *
 * El artículo fue rechazado durante el proceso de revisión y necesita correcciones.
 * En este estado el artículo puede editarse y reenviarse a revisión.
 *
 * Transiciones permitidas:
 * - Puede editarse (para realizar correcciones)
 * - Puede enviarse nuevamente a revisión → "En revision" (con porcentaje reiniciado a 0%)
 * - No puede aprobarse (debe estar en revisión para ser aprobado)
 * - No puede rechazarse (ya está rechazado)
 *
 * Principio Open/Closed aplicado: Este estado encapsula el comportamiento de rechazo.
 * Futuras extensiones (como límite de rechazos) se agregan aquí sin afectar otros estados.
 */
@Component
public class ObservadoState implements ArticleState {

    @Override
    public String getStateName() {
        return "Observado";
    }

    @Override
    public boolean canEdit() {
        return true; // Los artículos observados pueden editarse para correcciones
    }

    @Override
    public boolean canSendToReview() {
        return true; // Los artículos observados pueden reenviarse a revisión después de correcciones
    }

    /**
     * No se puede aprobar un artículo que está en Observado
     *
     * El artículo debe editarse y reenviarse a revisión primero
     *
     * @throws IllegalStateException siempre, ya que los artículos observados no están en revisión
     */
    @Override
    public ArticleStateTransition processApproval(Article article, BigDecimal approvalWeight) {
        throw new IllegalStateException("No se puede aprobar un artículo en estado Observado. " +
                "El autor debe editar el artículo y enviarlo nuevamente a revisión.");
    }

    /**
     * No se puede rechazar un artículo que ya está en Observado
     *
     * @throws IllegalStateException siempre, ya que el artículo ya fue rechazado
     */
    @Override
    public ArticleStateTransition processRejection(Article article) {
        throw new IllegalStateException("El artículo ya está en estado Observado (rechazado). " +
                "No puede rechazarse nuevamente.");
    }
}
