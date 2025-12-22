package com.portalperiodistico.article_service.domain.observer;

import com.portalperiodistico.article_service.domain.entity.Article;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

/**
 * Notificador centralizado de cambios de estado de artículos
 *
 * Patrón de Diseño: Observer Pattern (Subject/Notifier)
 *
 * Esta clase actúa como el "Subject" en el patrón Observer:
 * - Mantiene la lista de observadores registrados
 * - Notifica a todos los observadores cuando ocurre un cambio
 *
 * Ventajas:
 * - Centraliza la notificación a múltiples observadores
 * - Los observadores se registran automáticamente vía inyección de dependencias (Spring)
 * - Fácil agregar/quitar observadores sin modificar código de transiciones
 */
@Component
@RequiredArgsConstructor
public class ArticleStateChangeNotifier {

    // Spring inyecta automáticamente TODOS los beans que implementan ArticleStateChangeObserver
    private final List<ArticleStateChangeObserver> observers;

    /**
     * Notifica a todos los observadores registrados sobre un cambio de estado
     *
     * Este método es llamado por ApprovalServiceImpl después de que un artículo
     * cambia de estado, asegurando que todos los observadores sean notificados.
     *
     * @param article Artículo que cambió de estado
     * @param oldStateName Estado anterior
     * @param newStateName Nuevo estado
     * @param message Mensaje descriptivo del cambio
     */
    public void notifyStateChange(Article article, String oldStateName, String newStateName, String message) {
        // Notificar a cada observador registrado
        observers.forEach(observer ->
                observer.onStateChange(article, oldStateName, newStateName, message)
        );
    }

    /**
     * Retorna el número de observadores registrados
     *
     * Útil para debugging y validación de configuración
     *
     * @return Cantidad de observadores activos
     */
    public int getObserverCount() {
        return observers.size();
    }
}
