package com.portalperiodistico.article_service.domain.state.impl;

import com.portalperiodistico.article_service.domain.entity.Article;
import com.portalperiodistico.article_service.domain.state.ArticleState;
import com.portalperiodistico.article_service.domain.state.ArticleStateTransition;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Estado: Borrador
 *
 * El artículo está siendo redactado por el autor.
 * En este estado el artículo puede ser editado libremente y enviado a revisión.
 *
 * Transiciones permitidas:
 * - Puede enviarse a revisión → "En revision"
 * - No puede ser aprobado (aún no está en revisión)
 * - No puede ser rechazado (aún no está en revisión)
 *
 * Principio Open/Closed aplicado: Este estado encapsula su comportamiento.
 * Para agregar un nuevo estado, solo se crea una nueva clase sin modificar esta.
 */
@Component
public class BorradorState implements ArticleState {

    @Override
    public String getStateName() {
        return "Borrador";
    }

    @Override
    public boolean canEdit() {
        return true; // Los borradores pueden editarse libremente
    }

    @Override
    public boolean canSendToReview() {
        return true; // Los borradores pueden enviarse a revisión
    }

    /**
     * No se puede aprobar un artículo que está en Borrador
     *
     * @throws IllegalStateException siempre, ya que los borradores no están en proceso de aprobación
     */
    @Override
    public ArticleStateTransition processApproval(Article article, BigDecimal approvalWeight) {
        throw new IllegalStateException("No se puede aprobar un artículo en estado Borrador. " +
                "Primero debe enviarse a revisión.");
    }

    /**
     * No se puede rechazar un artículo que está en Borrador
     *
     * @throws IllegalStateException siempre, ya que los borradores no están en proceso de revisión
     */
    @Override
    public ArticleStateTransition processRejection(Article article) {
        throw new IllegalStateException("No se puede rechazar un artículo en estado Borrador. " +
                "Primero debe enviarse a revisión.");
    }
}
