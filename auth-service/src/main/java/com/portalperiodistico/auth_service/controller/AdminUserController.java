package com.portalperiodistico.auth_service.controller;

import com.portalperiodistico.auth_service.dto.AdminUserRequest;
import com.portalperiodistico.auth_service.dto.UserResponse;
import com.portalperiodistico.auth_service.service.interfaces.UserManagementService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para gestión administrativa de usuarios
 *
 * Principios SOLID aplicados:
 * - Dependency Inversion Principle (DIP): Depende de la interfaz UserManagementService,
 *   no de implementaciones concretas
 *
 * Antes: Controller dependía de la clase concreta AdminUserService
 * Ahora: Controller depende de UserManagementService (abstracción)
 */
@RestController
@RequestMapping("/admin/users")
@RequiredArgsConstructor
public class AdminUserController {

    private final UserManagementService userManagementService;

    @GetMapping
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(userManagementService.getAllUsers());
    }

    @PostMapping
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<UserResponse> create(@RequestBody AdminUserRequest request) {
        return ResponseEntity.ok(userManagementService.createUser(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<UserResponse> update(@PathVariable Integer id, @RequestBody AdminUserRequest request) {
        return ResponseEntity.ok(userManagementService.updateUser(id, request));
    }

    @DeleteMapping("/{id}") // Soft Delete
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        userManagementService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}