package com.portalperiodistico.article_service.service.impl;

import com.portalperiodistico.article_service.domain.entity.Role;
import com.portalperiodistico.article_service.domain.repository.RoleRepository;
import com.portalperiodistico.article_service.service.interfaces.RoleWeightProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

/**
 * Implementación del proveedor de información de roles
 *
 * Principios SOLID aplicados:
 * - Dependency Inversion Principle (DIP): Implementa la interfaz RoleWeightProvider,
 *   permitiendo que los controllers dependan de la abstracción en lugar del repositorio
 * - Interface Segregation Principle (ISP): Interfaz pequeña y focalizada
 *
 * Esta clase encapsula el acceso al RoleRepository, evitando que los controllers
 * dependan directamente de repositorios (violación de DIP).
 */
@Service
@RequiredArgsConstructor
public class RoleWeightProviderImpl implements RoleWeightProvider {

    private final RoleRepository roleRepository;

    /**
     * Obtiene el peso de aprobación de un rol desde la base de datos
     *
     * @param roleName Nombre del rol
     * @return Peso de aprobación del rol
     * @throws RuntimeException si el rol no existe
     */
    @Override
    public BigDecimal getRoleApprovalWeight(String roleName) {
        return roleRepository.findByRoleName(roleName)
                .map(Role::getApprovalWeight)
                .orElseThrow(() -> new RuntimeException("Rol '" + roleName + "' no encontrado en la base de datos"));
    }

    /**
     * Obtiene el ID de un rol desde la base de datos
     *
     * @param roleName Nombre del rol
     * @return ID del rol
     * @throws RuntimeException si el rol no existe
     */
    @Override
    public Integer getRoleId(String roleName) {
        return roleRepository.findByRoleName(roleName)
                .map(Role::getRoleId)
                .orElseThrow(() -> new RuntimeException("Rol '" + roleName + "' no encontrado en la base de datos"));
    }
}
