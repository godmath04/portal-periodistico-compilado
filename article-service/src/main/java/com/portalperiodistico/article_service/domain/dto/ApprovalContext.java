package com.portalperiodistico.article_service.domain.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;

/**
 * DTO que encapsula el contexto de aprobación
 *
 * Principio SOLID aplicado:
 * - Interface Segregation Principle (ISP): Reduce la cantidad de parámetros en
 *   el método processApproval de 6 parámetros individuales a un solo objeto
 *
 * Antes: ApprovalService.processApproval tenía 6 parámetros separados
 * Ahora: ApprovalService.processApproval recibe ApprovalRequest + ApprovalContext
 *
 * Este DTO agrupa toda la información del aprobador y su rol, mejorando la
 * legibilidad y mantenibilidad del código.
 */
@Getter
@AllArgsConstructor
@Builder
public class ApprovalContext {

    /**
     * ID del usuario que está aprobando/rechazando
     */
    private final Integer approverUserId;

    /**
     * Username del usuario que está aprobando/rechazando
     */
    private final String approverUsername;

    /**
     * ID del rol del aprobador
     */
    private final Integer roleId;

    /**
     * Nombre del rol del aprobador (ej: "Editor", "Jefe de Redacción")
     */
    private final String roleName;

    /**
     * Peso de aprobación del rol (ej: 30.00, 50.00)
     */
    private final BigDecimal approvalWeight;
}
