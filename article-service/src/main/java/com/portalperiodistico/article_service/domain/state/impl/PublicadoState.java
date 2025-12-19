package com.portalperiodistico.article_service.domain.state.impl;

import com.portalperiodistico.article_service.domain.entity.Article;
import com.portalperiodistico.article_service.domain.state.ArticleState;
import com.portalperiodistico.article_service.domain.state.ArticleStateTransition;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Estado: Publicado
 *
 * El artículo está publicado y visible para todos los usuarios.
 * Alcanzó el 100% de aprobación y no puede modificarse.
 *
 * Transiciones permitidas:
 * - Ninguna (es un estado final)
 * - No puede editarse (ya está publicado)
 * - No puede enviarse a revisión (ya está publicado)
 * - No puede aprobarse (ya está aprobado al 100%)
 * - No puede rechazarse (ya está publicado)
 *
 * Principio Open/Closed aplicado: Este estado encapsula el comportamiento final.
 * Si en el futuro se requiere despublicar artículos, se agrega esa lógica aquí
 * sin modificar otros estados.
 */
@Component
public class PublicadoState implements ArticleState {

    @Override
    public String getStateName() {
        return "Publicado";
    }

    @Override
    public boolean canEdit() {
        return false; // Los artículos publicados no pueden editarse
    }

    @Override
    public boolean canSendToReview() {
        return false; // Los artículos publicados no pueden reenviarse a revisión
    }

    /**
     * No se puede aprobar un artículo que ya está publicado
     *
     * @throws IllegalStateException siempre, ya que el artículo ya está aprobado al 100%
     */
    @Override
    public ArticleStateTransition processApproval(Article article, BigDecimal approvalWeight) {
        throw new IllegalStateException("No se puede aprobar un artículo ya publicado. " +
                "El artículo ya alcanzó el 100% de aprobación.");
    }

    /**
     * No se puede rechazar un artículo que ya está publicado
     *
     * @throws IllegalStateException siempre, ya que el artículo ya fue aprobado y publicado
     */
    @Override
    public ArticleStateTransition processRejection(Article article) {
        throw new IllegalStateException("No se puede rechazar un artículo ya publicado. " +
                "El artículo ya fue aprobado y está visible públicamente.");
    }
}
