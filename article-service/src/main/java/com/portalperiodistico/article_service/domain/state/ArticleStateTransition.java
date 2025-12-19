package com.portalperiodistico.article_service.domain.state;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * DTO que representa el resultado de una transición de estado
 *
 * Encapsula la información necesaria después de procesar una aprobación o rechazo:
 * - Nuevo nombre de estado al que debe transicionar el artículo
 * - Nuevo porcentaje de aprobación acumulado
 * - Mensaje descriptivo de la transición para el usuario
 *
 * Este DTO facilita la comunicación entre el State Pattern y los servicios,
 * permitiendo retornar toda la información de la transición de forma inmutable.
 */
@Getter
@AllArgsConstructor
public class ArticleStateTransition {

    /**
     * Nombre del nuevo estado al que debe transicionar el artículo
     * Ejemplos: "Borrador", "En revision", "Publicado", "Observado"
     */
    private final String newStateName;

    /**
     * Nuevo porcentaje de aprobación acumulado
     * Rango: 0.00 a 100.00
     */
    private final BigDecimal newPercentage;

    /**
     * Mensaje descriptivo de la transición para mostrar al usuario
     * Ejemplo: "Artículo aprobado por Editor. Progreso actual: 70%"
     */
    private final String message;
}
