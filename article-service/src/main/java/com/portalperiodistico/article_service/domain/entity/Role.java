package com.portalperiodistico.article_service.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
@Entity
@Table(name = "[ROLE]")
public class Role {

    @Id
    @Column(name = "RoleID")
    private Integer roleId;

    @Column(name = "RoleName", nullable = false, length = 100)
    private String roleName;

    @Column(name = "Description", length = 255)
    private String description;

    @Column(name = "ApprovalWeight", nullable = false, precision = 5, scale = 2)
    private BigDecimal approvalWeight;
}