package com.portalperiodistico.article_service.service.interfaces;

import java.math.BigDecimal;

/**
 * Interfaz para obtener información de roles (peso de aprobación e ID)
 *
 * Principios SOLID aplicados:
 * - Dependency Inversion Principle (DIP): Abstracción sobre RoleRepository,
 *   permitiendo que los controllers no dependan directamente de repositorios
 * - Interface Segregation Principle (ISP): Interfaz pequeña y focalizada
 *   en obtención de datos de roles
 *
 * Esta abstracción permite cambiar la fuente de datos de roles (base de datos, caché, servicio externo)
 * sin afectar a los consumidores.
 */
public interface RoleWeightProvider {

    /**
     * Obtiene el peso de aprobación de un rol
     *
     * El peso de aprobación determina qué porcentaje contribuye el rol
     * al proceso de aprobación de un artículo
     *
     * @param roleName Nombre del rol (ej: "Editor", "Jefe de Redacción")
     * @return Peso de aprobación del rol (ej: 30.00, 50.00)
     * @throws RuntimeException si el rol no existe
     */
    BigDecimal getRoleApprovalWeight(String roleName);

    /**
     * Obtiene el ID de un rol por su nombre
     *
     * @param roleName Nombre del rol
     * @return ID del rol
     * @throws RuntimeException si el rol no existe
     */
    Integer getRoleId(String roleName);
}
