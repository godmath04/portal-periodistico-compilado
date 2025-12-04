package com.portalperiodistico.suggestion_service.service;

import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.io.SyndFeedInput;
import com.rometools.rome.io.XmlReader;
import org.springframework.stereotype.Service;

import java.net.URL;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RssService {
    private static final String GOOGLE_TRENDS_URL = "https://trends.google.com/trending/rss?geo=EC";

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