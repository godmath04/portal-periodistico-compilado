package com.portalperiodistico.suggestion_service.service.impl;

import com.portalperiodistico.suggestion_service.service.interfaces.TrendsProvider;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;

/**
 * Implementación alternativa de TrendsProvider usando tendencias locales predefinidas
 *
 * Patrón de Diseño: Strategy Pattern
 *
 * Esta implementación permite usar tendencias predefinidas sin depender de fuentes externas:
 * - Para desarrollo local offline
 * - Para pruebas con datos controlados
 * - Para fallback si RSS falla
 *
 * Ventaja del Strategy Pattern:
 * Se puede cambiar entre RssTrendsProvider, LocalNewsTrendsProvider o TwitterTrendsProvider
 * sin modificar el código del SuggestionOrchestrator, solo cambiando la configuración
 */
@Service
@Profile("local") // Solo se activa en perfil 'local'
public class LocalNewsTrendsProvider implements TrendsProvider {

    /**
     * Retorna tendencias locales predefinidas para Ecuador
     *
     * @return Lista de tendencias locales
     */
    @Override
    public List<String> getActualTrends() {
        // Tendencias predefinidas para Ecuador
        return Arrays.asList(
                "Elecciones Presidenciales Ecuador 2025",
                "Economía ecuatoriana: perspectivas",
                "Seguridad ciudadana en Quito",
                "Exportaciones de banano y cacao",
                "Turismo en Galápagos",
                "Educación digital en Ecuador",
                "Salud pública: vacunación COVID",
                "Deportes: Selección Ecuador",
                "Cultura: Festival de cine latinoamericano",
                "Tecnología: Startups ecuatorianas"
        );
    }
}
