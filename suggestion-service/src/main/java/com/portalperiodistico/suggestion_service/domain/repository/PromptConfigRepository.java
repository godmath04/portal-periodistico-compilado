package com.portalperiodistico.suggestion_service.domain.repository;

import com.portalperiodistico.suggestion_service.domain.entity.PromptConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface PromptConfigRepository
        extends JpaRepository<PromptConfig, Long> {
}