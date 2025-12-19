package com.portalperiodistico.auth_service.service.impl;

import com.portalperiodistico.auth_service.domain.entity.Role;
import com.portalperiodistico.auth_service.domain.entity.User;
import com.portalperiodistico.auth_service.domain.repository.RoleRepository;
import com.portalperiodistico.auth_service.domain.repository.UserRepository;
import com.portalperiodistico.auth_service.dto.AdminUserRequest;
import com.portalperiodistico.auth_service.dto.UserResponse;
import com.portalperiodistico.auth_service.service.interfaces.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Implementación del servicio de gestión de usuarios para administradores
 *
 * Principios SOLID aplicados:
 * - Dependency Inversion Principle (DIP): Implementa la interfaz UserManagementService,
 *   permitiendo que los controllers dependan de la abstracción
 * - Interface Segregation Principle (ISP): Interfaz focalizada solo en gestión administrativa
 *   de usuarios, segregada de autenticación
 *
 * Esta clase reemplaza a AdminUserService, ahora implementando la interfaz UserManagementService
 * para mejorar el diseño según principios SOLID.
 */
@Service
@RequiredArgsConstructor
public class AdminUserServiceImpl implements UserManagementService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    /**
     * Obtiene todos los usuarios del sistema
     *
     * @return Lista de usuarios con sus datos básicos y roles
     */
    @Override
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToDTO)
                .collect(Collectors.toList());
    }

    /**
     * Crea un nuevo usuario con roles específicos
     *
     * @param request Datos del usuario a crear (incluye roles)
     * @return Datos del usuario creado
     * @throws RuntimeException si el username o email ya existen
     */
    @Override
    public UserResponse createUser(AdminUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("Usuario existe");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email existe");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setActive(true);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        assignRoles(user, request.getRoles());

        return mapToDTO(userRepository.save(user));
    }

    /**
     * Actualiza los datos de un usuario existente
     *
     * @param id ID del usuario a actualizar
     * @param request Nuevos datos del usuario
     * @return Datos actualizados del usuario
     * @throws RuntimeException si el usuario no existe
     */
    @Override
    public UserResponse updateUser(Integer id, AdminUserRequest request) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No encontrado"));

        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());

        // Solo cambiar password si viene en el request
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        if (request.getRoles() != null && !request.getRoles().isEmpty()) {
            assignRoles(user, request.getRoles());
        }

        if (request.getActive() != null) {
            user.setActive(request.getActive());
        }

        return mapToDTO(userRepository.save(user));
    }

    /**
     * Elimina (desactiva) un usuario del sistema
     *
     * Se realiza una eliminación lógica (soft delete) cambiando isActive a false
     *
     * @param id ID del usuario a desactivar
     * @throws RuntimeException si el usuario no existe
     */
    @Override
    public void deleteUser(Integer id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("No encontrado"));
        user.setActive(false);
        userRepository.save(user);
    }

    /**
     * Asigna roles a un usuario
     *
     * @param user Usuario al que asignar roles
     * @param roleNames Nombres de los roles a asignar
     * @throws RuntimeException si algún rol no existe
     */
    private void assignRoles(User user, Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByRoleName(roleName)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + roleName));
            roles.add(role);
        }
        user.setRoles(roles);
    }

    /**
     * Convierte una entidad User a DTO UserResponse
     *
     * @param user Entidad User a convertir
     * @return DTO con datos del usuario
     */
    private UserResponse mapToDTO(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isActive(user.isActive())
                .roles(user.getRoles().stream()
                        .map(Role::getRoleName)
                        .collect(Collectors.toSet()))
                .build();
    }
}
