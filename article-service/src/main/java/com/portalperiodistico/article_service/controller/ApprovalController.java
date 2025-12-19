package com.portalperiodistico.article_service.controller;

import com.portalperiodistico.article_service.domain.dto.ApprovalRequest;
import com.portalperiodistico.article_service.domain.dto.ApprovalResponse;
import com.portalperiodistico.article_service.security.UserPrincipal;
import com.portalperiodistico.article_service.service.ApprovalService;
import com.portalperiodistico.article_service.service.interfaces.RoleWeightProvider;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Controlador REST para operaciones de aprobación/rechazo de artículos
 *
 * Principios SOLID aplicados:
 * - Dependency Inversion Principle (DIP): Depende de RoleWeightProvider (interfaz),
 *   no de RoleRepository (implementación concreta)
 *
 * Antes: Controller dependía directamente de RoleRepository
 * Ahora: Controller depende de RoleWeightProvider (abstracción)
 */
@RestController
@RequestMapping("/api/v1/approvals")
@RequiredArgsConstructor
public class ApprovalController {

    private final ApprovalService approvalService;
    private final RoleWeightProvider roleWeightProvider;

    /**
     * Aprobar o rechazar un articulo
     * POST /api/v1/approvals
     * Requiere autenticacion
     */
    @PostMapping
    public ResponseEntity<?> processApproval(
            @Valid @RequestBody ApprovalRequest request,
            @AuthenticationPrincipal UserPrincipal authenticatedUser) {

        if (authenticatedUser == null) {
            return new ResponseEntity<>("Usuario no autenticado", HttpStatus.UNAUTHORIZED);
        }

        // Extraer roles del usuario (vienen del JWT)
        List<String> userRoles = authenticatedUser.getAuthorities().stream()
                .map(authority -> authority.getAuthority().replace("ROLE_", ""))
                .collect(Collectors.toList());

        // Por ahora, tomamos el primer rol del usuario
        // En el futuro, podriamos permitir que el usuario elija con cual rol aprobar
        if (userRoles.isEmpty()) {
            return new ResponseEntity<>("El usuario no tiene roles asignados", HttpStatus.FORBIDDEN);
        }

        String roleName = userRoles.get(0);

        // Obtener información del rol usando el proveedor de roles
        BigDecimal roleWeight = roleWeightProvider.getRoleApprovalWeight(roleName);
        Integer roleId = roleWeightProvider.getRoleId(roleName);

        // Procesar la aprobación
        ApprovalResponse response = approvalService.processApproval(
                request,
                authenticatedUser.getUserId(),
                authenticatedUser.getUsername(),
                roleId,
                roleName,
                roleWeight
        );

        return ResponseEntity.ok(response);
    }

    /**
     * Obtener historial de aprobaciones de un articulo
     * GET /api/v1/approvals/article/{articleId}
     * Requiere autenticacion
     */
    @GetMapping("/article/{articleId}")
    public ResponseEntity<?> getApprovalHistory(@PathVariable Long articleId) {
        try {
            List<ApprovalResponse> history = approvalService.getApprovalHistory(articleId);
            return ResponseEntity.ok(history);
        } catch (RuntimeException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(Map.of("error", e.getMessage()));
        }
    }
}