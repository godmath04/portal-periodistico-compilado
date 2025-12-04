package com.portalperiodistico.article_service.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "[ARTICLE_APPROVAL]")
public class ArticleApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "[ArticleApprovalID]")
    private Long articleApprovalId;

    // Relacion con Article
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "[ArticleID]", referencedColumnName = "[IdArticle]", nullable = false)
    private Article article;

    // ID del usuario que aprobo/rechazo (no FK, viene del JWT)
    @Column(name = "[ApproverUserID]", nullable = false)
    private Integer approverUserId;

    // Nombre de usuario del aprobador (para referencia)
    @Column(name = "[ApproverUsername]", length = 100)
    private String approverUsername;

    // ID del rol del aprobador (no FK, viene del JWT)
    @Column(name = "[RoleID]", nullable = false)
    private Integer roleId;

    // Nombre del rol (para referencia)
    @Column(name = "[RoleName]", nullable = false, length = 100)
    private String roleName;

    // Peso/porcentaje que aporta este rol
    @Column(name = "[ApprovalWeight]", nullable = false, precision = 5, scale = 2)
    private BigDecimal approvalWeight;

    // Estado: APPROVED o REJECTED
    @Column(name = "[Status]", nullable = false, length = 20)
    private String status;

    // Comentarios opcionales
    @Column(name = "[Comments]", columnDefinition = "TEXT")
    private String comments;

    @CreationTimestamp
    @Column(name = "[Timestamp]", nullable = false, updatable = false)
    private LocalDateTime timestamp;
}