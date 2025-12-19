package com.portalperiodistico.auth_service.service.interfaces;

import com.portalperiodistico.auth_service.dto.AdminUserRequest;
import com.portalperiodistico.auth_service.dto.UserResponse;

import java.util.List;

/**
 * Interfaz para gestión de usuarios por administradores
 *
 * Principios SOLID aplicados:
 * - Dependency Inversion Principle (DIP): Los controllers dependerán de esta abstracción
 * - Interface Segregation Principle (ISP): Interfaz focalizada solo en gestión administrativa
 *   de usuarios, segregada de autenticación
 *
 * Esta interfaz encapsula las operaciones CRUD de usuarios que solo pueden realizar
 * los administradores del sistema.
 */
public interface UserManagementService {

    /**
     * Obtiene todos los usuarios del sistema
     *
     * @return Lista de usuarios con sus datos básicos
     */
    List<UserResponse> getAllUsers();

    /**
     * Crea un nuevo usuario con roles específicos
     *
     * Solo los administradores pueden usar este método para crear usuarios con cualquier rol
     *
     * @param request Datos del usuario a crear (incluye roles)
     * @return Datos del usuario creado
     * @throws RuntimeException si el username ya existe
     */
    UserResponse createUser(AdminUserRequest request);

    /**
     * Actualiza los datos de un usuario existente
     *
     * Permite modificar username, email, nombre, apellido, estado activo y roles
     *
     * @param id ID del usuario a actualizar
     * @param request Nuevos datos del usuario
     * @return Datos actualizados del usuario
     * @throws RuntimeException si el usuario no existe
     */
    UserResponse updateUser(Integer id, AdminUserRequest request);

    /**
     * Elimina (desactiva) un usuario del sistema
     *
     * Se realiza una eliminación lógica (soft delete) cambiando isActive a false
     *
     * @param id ID del usuario a desactivar
     * @throws RuntimeException si el usuario no existe
     */
    void deleteUser(Integer id);
}
