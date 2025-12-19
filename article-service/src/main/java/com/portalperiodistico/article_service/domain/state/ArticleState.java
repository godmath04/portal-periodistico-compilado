package com.portalperiodistico.article_service.domain.state;

import com.portalperiodistico.article_service.domain.entity.Article;

import java.math.BigDecimal;

/**
 * Interfaz para el patrón State de artículos
 *
 * Principios SOLID aplicados:
 * - Open/Closed Principle (OCP): Permite agregar nuevos estados sin modificar código existente
 * - Liskov Substitution Principle (LSP): Todas las implementaciones pueden sustituirse entre sí
 * - Interface Segregation Principle (ISP): Interfaz focalizada en comportamiento de estados
 *
 * El patrón State permite que un artículo cambie su comportamiento según su estado actual,
 * encapsulando la lógica de transiciones en cada estado concreto.
 *
 * Estados soportados:
 * - Borrador: Artículo en creación, puede editarse y enviarse a revisión
 * - En revisión: Artículo en proceso de aprobación, no puede editarse
 * - Publicado: Artículo aprobado al 100%, visible públicamente, no puede modificarse
 * - Observado: Artículo rechazado, puede editarse y reenviarse a revisión
 */
public interface ArticleState {

    /**
     * Obtiene el nombre del estado
     *
     * @return Nombre del estado ("Borrador", "En revision", "Publicado", "Observado")
     */
    String getStateName();

    /**
     * Verifica si el artículo puede ser editado en este estado
     *
     * @return true si el artículo es editable, false en caso contrario
     */
    boolean canEdit();

    /**
     * Verifica si el artículo puede ser enviado a revisión desde este estado
     *
     * @return true si puede enviarse a revisión, false en caso contrario
     */
    boolean canSendToReview();

    /**
     * Procesa una aprobación en este estado
     *
     * Cada estado maneja la aprobación de forma diferente:
     * - Borrador/Observado/Publicado: No se puede aprobar (IllegalStateException)
     * - En revisión: Suma el peso y verifica si alcanza 100% para publicar
     *
     * @param article Artículo a aprobar
     * @param approvalWeight Peso de aprobación del rol que aprueba
     * @return Transición con el nuevo estado, porcentaje y mensaje
     * @throws IllegalStateException si el estado no permite aprobaciones
     */
    ArticleStateTransition processApproval(Article article, BigDecimal approvalWeight);

    /**
     * Procesa un rechazo en este estado
     *
     * Cada estado maneja el rechazo de forma diferente:
     * - Borrador/Observado/Publicado: No se puede rechazar (IllegalStateException)
     * - En revisión: Cambia a estado "Observado"
     *
     * @param article Artículo a rechazar
     * @return Transición con el nuevo estado y mensaje
     * @throws IllegalStateException si el estado no permite rechazos
     */
    ArticleStateTransition processRejection(Article article);
}
