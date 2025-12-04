package com.portalperiodistico.auth_service.service;

import com.portalperiodistico.auth_service.domain.entity.Role;
import com.portalperiodistico.auth_service.domain.entity.User;
import com.portalperiodistico.auth_service.domain.repository.RoleRepository;
import com.portalperiodistico.auth_service.domain.repository.UserRepository;
import com.portalperiodistico.auth_service.dto.AdminUserRequest;
import com.portalperiodistico.auth_service.dto.UserResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
public class AdminUserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private RoleRepository roleRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    // Listar todos
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream().map(this::mapToDTO).collect(Collectors.toList());
    }

    // Crear Usuario (Con roles específicos)
    public UserResponse createUser(AdminUserRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) throw new RuntimeException("Usuario existe");
        if (userRepository.existsByEmail(request.getEmail())) throw new RuntimeException("Email existe");

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

    // Editar
    public UserResponse updateUser(Integer id, AdminUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("No encontrado"));

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

    // Soft Delete
    public void deleteUser(Integer id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("No encontrado"));
        user.setActive(false);
        userRepository.save(user);
    }

    // Helpers
    private void assignRoles(User user, Set<String> roleNames) {
        Set<Role> roles = new HashSet<>();
        for (String roleName : roleNames) {
            Role role = roleRepository.findByRoleName(roleName)
                    .orElseThrow(() -> new RuntimeException("Rol no encontrado: " + roleName));
            roles.add(role);
        }
        user.setRoles(roles);
    }

    private UserResponse mapToDTO(User user) {
        return UserResponse.builder()
                .userId(user.getUserId())
                .username(user.getUsername())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .isActive(user.isActive())
                .roles(user.getRoles().stream().map(Role::getRoleName).collect(Collectors.toSet()))
                .build();
    }
}