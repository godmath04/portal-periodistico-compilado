package com.portalperiodistico.auth_service.dto;

import lombok.Data;
import java.util.Set;

@Data
public class AdminUserRequest {
    private String username;
    private String email;
    private String password;
    private String firstName;
    private String lastName;
    private Boolean active;
    private Set<String> roles;
}