package com.portalperiodistico.suggestion_service.domain.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import java.time.LocalDateTime;

@Entity
@Table(name = "PROMPT_CONFIG")
@Getter
@Setter
public class PromptConfig {

    @Id
    @GeneratedValue(strategy =
            GenerationType.IDENTITY)
    @Column(name = "IdPromptConfig")
    private Long idPromptConfig;

    @Column(name = "PromptTemplate", nullable
            = false, columnDefinition = "TEXT")
    private String promptTemplate;

    @Column(name = "UpdatedAt")
    private LocalDateTime updatedAt;

    @Column(name = "UpdatedBy")
    private String updatedBy;

    @PreUpdate
    protected void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}