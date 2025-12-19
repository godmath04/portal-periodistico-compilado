package com.portalperiodistico.article_service.domain.state;

import com.portalperiodistico.article_service.domain.state.impl.BorradorState;
import com.portalperiodistico.article_service.domain.state.impl.EnRevisionState;
import com.portalperiodistico.article_service.domain.state.impl.ObservadoState;
import com.portalperiodistico.article_service.domain.state.impl.PublicadoState;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * Factory para obtener instancias de estados de artículos
 *
 * Principios SOLID aplicados:
 * - Open/Closed Principle (OCP): Centraliza la creación de estados. Para agregar un nuevo estado,
 *   solo se agrega un nuevo caso aquí sin modificar la lógica de negocio
 * - Dependency Inversion Principle (DIP): Los consumidores dependen de ArticleState (interfaz),
 *   no de las implementaciones concretas
 *
 * Este factory permite obtener la instancia correcta del estado según el nombre,
 * facilitando el patrón State en los servicios.
 */
@Component
@RequiredArgsConstructor
public class ArticleStateFactory {

    // Inyección de todos los estados disponibles
    private final BorradorState borradorState;
    private final EnRevisionState enRevisionState;
    private final PublicadoState publicadoState;
    private final ObservadoState observadoState;

    /**
     * Obtiene la instancia del estado correspondiente al nombre
     *
     * @param stateName Nombre del estado ("Borrador", "En revision", "Publicado", "Observado")
     * @return Instancia del estado correspondiente
     * @throws IllegalArgumentException si el nombre del estado no es reconocido
     */
    public ArticleState getState(String stateName) {
        switch (stateName) {
            case "Borrador":
                return borradorState;
            case "En revision":
                return enRevisionState;
            case "Publicado":
                return publicadoState;
            case "Observado":
                return observadoState;
            default:
                throw new IllegalArgumentException("Estado desconocido: " + stateName + ". " +
                        "Estados válidos: Borrador, En revision, Publicado, Observado");
        }
    }
}
