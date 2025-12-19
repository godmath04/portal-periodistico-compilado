package com.portalperiodistico.suggestion_service.service.impl;

import com.portalperiodistico.suggestion_service.service.interfaces.TrendsProvider;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Implementación del proveedor de tendencias usando RSS de Google Trends
 *
 * Principios SOLID aplicados:
 * - Dependency Inversion Principle (DIP): Implementa la interfaz TrendsProvider,
 *   permitiendo que los consumidores dependan de la abstracción
 * - Open/Closed Principle (OCP): Si se requiere otra fuente de tendencias
 *   (Twitter API, Reddit, etc.), solo se crea una nueva implementación de
 *   TrendsProvider sin modificar código existente
 *
 * Esta clase reemplaza a RssService, ahora siguiendo los principios SOLID.
 */
@Service
public class RssTrendsProvider implements TrendsProvider {

    private static final String GOOGLE_TRENDS_URL = "https://trends.google.com/trending/rss?geo=EC";

    /**
     * Obtiene las tendencias actuales desde el feed RSS de Google Trends Ecuador
     *
     * @return Lista de títulos de tendencias actuales (máximo 10)
     *         En caso de error, retorna una lista con un mensaje de error
     */
    @Override
    public List<String> getActualTrends() {
        try {
            URL feedUrl = new URL(GOOGLE_TRENDS_URL);

            // ROME se encarga de leer el XML y convertirlo a objetos Java
            SyndFeedInput input = new SyndFeedInput();
            SyndFeed feed = input.build(new XmlReader(feedUrl));

            // Extraemos solo los títulos de las entradas y limitamos a 10 para no saturar
            return feed.getEntries().stream()
                    .map(SyndEntry::getTitle)
                    .limit(10)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            e.printStackTrace();
            return List.of("Error obteniendo noticias: " + e.getMessage());
        }
    }
}
