package com.portalperiodistico.auth_service.controller;

import com.portalperiodistico.auth_service.dto.AdminUserRequest;
import com.portalperiodistico.auth_service.dto.UserResponse;
import com.portalperiodistico.auth_service.service.AdminUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private AdminUserService adminUserService;

    @GetMapping
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<List<UserResponse>> getAll() {
        return ResponseEntity.ok(adminUserService.getAllUsers());
    }

    @PostMapping
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<UserResponse> create(@RequestBody AdminUserRequest request) {
        return ResponseEntity.ok(adminUserService.createUser(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<UserResponse> update(@PathVariable Integer id, @RequestBody AdminUserRequest request) {
        return ResponseEntity.ok(adminUserService.updateUser(id, request));
    }

    @DeleteMapping("/{id}") // Soft Delete
    @PreAuthorize("hasRole('Administrador')")
    public ResponseEntity<Void> delete(@PathVariable Integer id) {
        adminUserService.deleteUser(id);
        return ResponseEntity.noContent().build();
    }
}