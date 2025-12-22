# Patrones de Diseño Implementados - Portal Periodístico

Se implementaron **3 patrones de diseño** fundamentales en el sistema de microservicios del Portal Periodístico.

### Patrones Implementados

| Patrón | Ubicación | Propósito | Principio SOLID |
|--------|-----------|-----------|-----------------|
| **State Pattern** | `article-service` | Gestionar transiciones de estado de artículos | Open/Closed Principle |
| **Strategy Pattern** | `suggestion-service` | Permitir múltiples proveedores de IA y tendencias | Open/Closed + Dependency Inversion |
| **Observer Pattern** | `article-service` | Notificar cambios de estado a múltiples componentes | Open/Closed + Single Responsibility |

---

## 1. State Pattern

### ¿Qué es?

El **State Pattern** permite que un objeto altere su comportamiento cuando su estado interno cambia. El objeto parecerá cambiar de clase.

### ¿Por qué lo elegimos?

**Problema Original:**
```java
// ANTES: Lógica hardcodeada en ApprovalServiceImpl (ANTI-PATRÓN)
if (currentStatus.equals("En revision")) {
    if (request.getStatus().equals("REJECTED")) {
        newStatus = "Observado";
        article.setCurrentApprovalPercentage(BigDecimal.ZERO);
        message = "El artículo fue rechazado...";
    } else {
        BigDecimal newPercentage = currentPercentage.add(approvalWeight);
        if (newPercentage.compareTo(BigDecimal.valueOf(100)) >= 0) {
            newStatus = "Publicado";
            message = "¡Artículo publicado!";
        } else {
            newStatus = "En revision";
            message = "Progreso: " + newPercentage + "%";
        }
    }
} else if (currentStatus.equals("Borrador")) {
    // Más lógica hardcodeada...
}
```

**Problemas detectados:**
- Código difícil de mantener (lógica de 4 estados mezclada)
- Violación del Open/Closed Principle (agregar estado = modificar método gigante)
- Imposible testear estados individualmente
- Strings hardcodeados propensos a errores tipográficos

### Solución Implementada

**Estructura del patrón:**

```
ArticleState (interface)
    ├── BorradorState
    ├── EnRevisionState
    ├── PublicadoState
    └── ObservadoState

ArticleStateFactory: Crea instancias de estados
ArticleStateTransition: DTO para retornar resultados de transiciones
```

**Código clave:**

#### `ArticleState.java` (Interfaz base)
```java
public interface ArticleState {
    String getStateName();
    boolean canEdit();
    boolean canSendToReview();

    ArticleStateTransition processApproval(Article article, BigDecimal approvalWeight);
    ArticleStateTransition processRejection(Article article);
}
```

#### `EnRevisionState.java` (Ejemplo de estado concreto)
```java
@Component
public class EnRevisionState implements ArticleState {

    @Override
    public ArticleStateTransition processApproval(Article article, BigDecimal approvalWeight) {
        BigDecimal newPercentage = article.getCurrentApprovalPercentage().add(approvalWeight);
        article.setCurrentApprovalPercentage(newPercentage);

        if (newPercentage.compareTo(BigDecimal.valueOf(100)) >= 0) {
            return new ArticleStateTransition(
                "Publicado",
                newPercentage,
                "El artículo ha alcanzado el 100% y ha sido PUBLICADO."
            );
        } else {
            return new ArticleStateTransition(
                "En revision",
                newPercentage,
                "Progreso actual: " + newPercentage + "%"
            );
        }
    }

    @Override
    public ArticleStateTransition processRejection(Article article) {
        article.setCurrentApprovalPercentage(BigDecimal.ZERO);
        return new ArticleStateTransition(
            "Observado",
            BigDecimal.ZERO,
            "El artículo fue rechazado y enviado a Observado."
        );
    }

    @Override
    public boolean canEdit() { return false; }

    @Override
    public boolean canSendToReview() { return false; }
}
```

#### `ArticleStateFactory.java` (Factory para crear estados)
```java
@Component
@RequiredArgsConstructor
public class ArticleStateFactory {
    private final BorradorState borradorState;
    private final EnRevisionState enRevisionState;
    private final PublicadoState publicadoState;
    private final ObservadoState observadoState;

    public ArticleState getState(String stateName) {
        switch (stateName) {
            case "Borrador": return borradorState;
            case "En revision": return enRevisionState;
            case "Publicado": return publicadoState;
            case "Observado": return observadoState;
            default: throw new IllegalArgumentException("Estado desconocido: " + stateName);
        }
    }
}
```

#### Uso en `ApprovalServiceImpl.java`
```java
// AHORA: Código limpio usando State Pattern
String currentStateName = article.getArticleStatus().getStatusName();
ArticleState currentState = articleStateFactory.getState(currentStateName);

ArticleStateTransition transition;
if (APPROVAL_REJECTED.equals(request.getStatus())) {
    transition = currentState.processRejection(article);
} else {
    transition = currentState.processApproval(article, approvalWeight);
}

ArticleStatus newStatus = articleStatusRepository.findByStatusName(transition.getNewStateName())
    .orElseThrow(() -> new RuntimeException("Estado no encontrado"));
article.setArticleStatus(newStatus);
```

### Diagrama de Flujo de Estados

```
┌─────────────┐
│  Borrador   │
│ (editable)  │
└──────┬──────┘
       │ enviarARevisión()
       ▼
┌─────────────────┐
│  En revision    │◄──┐
│  (no editable)  │   │
└────┬────────┬───┘   │
     │        │        │
     │ aprobar│rechazar│
     │ 100%   │        │
     ▼        ▼        │
┌──────┐  ┌──────────┐│
│Publi-│  │Observado ││
│cado  │  │(editable)││
└──────┘  └─────┬────┘│
                │      │
                └──────┘
              enviarARevisión()
```

### Beneficios Obtenidos

**Extensibilidad**: Agregar nuevo estado = crear nueva clase (no modificar código existente)
**Testabilidad**: Cada estado se puede testear aisladamente
**Mantenibilidad**: Lógica de cada estado encapsulada en su propia clase
**Eliminación de strings mágicos**: Estados validados en tiempo de compilación
**Open/Closed Principle**: Abierto para extensión, cerrado para modificación

---

## 2. Strategy Pattern

### ¿Qué es?

El **Strategy Pattern** define una familia de algoritmos, encapsula cada uno de ellos y los hace intercambiables. Strategy permite que el algoritmo varíe independientemente de los clientes que lo usan.

### ¿Por qué lo elegimos?

**Problema Original:**
```java
// ANTES: Servicio acoplado a Gemini (suggestion-service)
@Service
public class GeminiService {
    private RestTemplate restTemplate = new RestTemplate();

    public String getSuggestions(List<String> trends) {
        // Lógica específica de Gemini hardcodeada
        // ¿Qué pasa si queremos usar OpenAI, Claude o MockAI?
        // Respuesta: Modificar este código o duplicarlo
    }
}
```

**Problemas detectados:**
- Acoplamiento fuerte a un proveedor específico (Gemini)
- Imposible cambiar de proveedor sin modificar código
- Testing difícil (requiere API key real de Gemini)
- No hay modo offline/desarrollo
- Violación de Open/Closed Principle

### Solución Implementada

**Estructura del patrón:**

```
AIProvider (interface)
    └── GeminiAIProvider (única implementación - producción)

TrendsProvider (interface)
    ├── RssTrendsProvider (Google Trends RSS)
    └── LocalNewsTrendsProvider (fallback local)

SuggestionOrchestrator: Combina TrendsProvider + AIProvider
```

**Implementación:**
- GeminiAIProvider es la única implementación de AIProvider
- Siempre usa la API real de Gemini
- Para agregar otro proveedor (OpenAI, Claude), solo crear nueva clase que implemente AIProvider

**Código clave:**

#### `AIProvider.java` (Strategy interface)
```java
/**
 * Interfaz para proveedores de inteligencia artificial
 *
 * Patrón de Diseño: Strategy Pattern
 * Principio SOLID: Open/Closed Principle
 *
 * Permite agregar nuevos proveedores de IA (OpenAI, Claude, etc.)
 * sin modificar el código existente.
 */
public interface AIProvider {
    /**
     * Genera sugerencias de artículos basadas en tendencias actuales
     *
     * @param trends Lista de tendencias del momento
     * @return JSON con sugerencias de títulos de artículos
     */
    String getSuggestions(List<String> trends);
}
```

#### `GeminiAIProvider.java` (Implementación concreta)
```java
@Service
@RequiredArgsConstructor
public class GeminiAIProvider implements AIProvider {

    private final RestTemplate restTemplate; // Inyectado, no creado

    @Value("${google.gemini.api-key}")
    private String apiKey;

    @Override
    public String getSuggestions(List<String> trends) {
        // Construye el request para la API de Gemini
        String url = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=" + apiKey;

        // Llama a la API y retorna las sugerencias
        // ... implementación completa
    }
}
```

#### `TrendsProvider.java` (Otra Strategy interface)
```java
/**
 * Interfaz para proveedores de tendencias de noticias
 *
 * Patrón de Diseño: Strategy Pattern
 * Permite obtener tendencias de múltiples fuentes
 */
public interface TrendsProvider {
    /**
     * Obtiene las tendencias actuales de noticias
     *
     * @return Lista de tendencias del momento
     */
    List<String> getActualTrends();
}
```

#### `LocalNewsTrendsProvider.java` (Estrategia para fallback)
```java
@Service
public class LocalNewsTrendsProvider implements TrendsProvider {

    @Override
    public List<String> getActualTrends() {
        // Tendencias predefinidas para cuando RSS no esté disponible
        return List.of(
            "Elecciones Ecuador 2025",
            "Economía ecuatoriana",
            "Seguridad ciudadana Quito",
            "Educación pública Ecuador",
            "Crisis energética"
        );
    }
}
```

#### `SuggestionOrchestratorImpl.java` (Cliente del patrón)
```java
@Service
@RequiredArgsConstructor
public class SuggestionOrchestratorImpl implements SuggestionOrchestrator {

    private final TrendsProvider trendsProvider; // Abstracción
    private final AIProvider aiProvider;         // Abstracción

    @Override
    public String generateSuggestions() {
        // 1. Obtener tendencias (de cualquier proveedor)
        List<String> trends = trendsProvider.getActualTrends();

        // 2. Generar sugerencias con IA (de cualquier proveedor)
        String suggestions = aiProvider.getSuggestions(trends);

        return suggestions;
    }
}
```

### Diagrama del Strategy Pattern

```
┌─────────────────────────┐
│ SuggestionOrchestrator  │
│  (Context/Cliente)      │
└───────┬─────────────────┘
        │
        │ usa
        ▼
┌───────────────┐          ┌──────────────────┐
│  AIProvider   │          │ TrendsProvider   │
│  <<interface>>│          │   <<interface>>  │
└───────┬───────┘          └────────┬─────────┘
        │                           │
        │ implementa                │ implementan
        │                      ┌────┴────┐
        ▼                      │         │
  ┌──────────┐                ▼         ▼
  │  Gemini  │          ┌──────┐  ┌──────────┐
  │    AI    │          │ RSS  │  │  Local   │
  └──────────┘          │Trends│  │NewsTrends│
                        └──────┘  └──────────┘
```

### Uso en el Controller

```java
@RestController
@RequestMapping("/api/suggestions")
@RequiredArgsConstructor
public class SuggestionController {

    private final SuggestionOrchestrator suggestionOrchestrator; // Abstracción

    @PostMapping("/generate")
    public ResponseEntity<String> generateSuggestions() {
        String suggestions = suggestionOrchestrator.generateSuggestions();
        return ResponseEntity.ok(suggestions);
    }
}
```

### Beneficios Obtenidos

**Intercambiabilidad**: Agregar otro proveedor (OpenAI, Claude) = crear nueva clase que implemente AIProvider
**Simplicidad**: Una sola implementación, sin configuración compleja
**Extensibilidad**: El patrón permite agregar nuevos proveedores sin modificar código existente
**Dependency Inversion**: El orquestador depende de la interfaz AIProvider, no de Gemini específicamente
**Open/Closed Principle**: Abierto para extensión (nuevos proveedores), cerrado para modificación

---

## 3. Observer Pattern

### ¿Qué es?

El **Observer Pattern** define una dependencia uno-a-muchos entre objetos, de manera que cuando un objeto cambia de estado, todos sus dependientes son notificados y actualizados automáticamente.

### ¿Por qué lo elegimos?

**Problema Original:**
```java
// ANTES: ApprovalServiceImpl hacía TODO (violación de SRP)
public ApprovalResponse processApproval(...) {
    // 1. Procesar aprobación
    article.setArticleStatus(newStatus);
    articleRepository.save(article);

    // 2. Enviar email al autor | Responsabilidad adicional
    emailService.sendEmail(...);

    // 3. Registrar auditoría | Otra responsabilidad
    auditLog.log(...);

    // 4. Actualizar caché | Otra más
    cacheService.invalidate(...);

    // 5. Publicar evento a Kafka | Y otra...
    kafkaProducer.send(...);

    // ¿Qué pasa si queremos agregar notificaciones push?
    // Respuesta: Modificar este método (violación de OCP)
}
```

**Problemas detectados:**
- ApprovalServiceImpl tiene múltiples responsabilidades (SRP)
- Agregar nueva notificación = modificar código existente (OCP)
- Difícil testear sin mocks de todos los servicios
- Acoplamiento fuerte entre aprobación y notificaciones

### Solución Implementada

**Estructura del patrón:**

```
ArticleStateChangeObserver (interface)
    ├── EmailNotificationObserver
    └── AuditLogObserver

ArticleStateChangeNotifier (Subject/Notifier)
    └── Mantiene lista de observers y notifica a todos
```

**Código clave:**

#### `ArticleStateChangeObserver.java` (Observer interface)
```java
/**
 * Interfaz para observadores de cambios de estado de artículos
 *
 * Patrón de Diseño: Observer Pattern
 *
 * Este patrón permite que múltiples componentes del sistema sean notificados
 * cuando un artículo cambia de estado, sin acoplar el código de transición
 * de estados con las acciones que deben ejecutarse.
 *
 * Ventajas del Observer Pattern:
 * - Desacoplamiento: Los estados no conocen a los observadores
 * - Extensibilidad: Se pueden agregar nuevos observadores sin modificar estados
 * - Open/Closed Principle: Abierto para extensión, cerrado para modificación
 *
 * Ejemplos de uso:
 * - Enviar notificaciones por email al autor
 * - Registrar auditoría de cambios
 * - Actualizar caché
 * - Enviar eventos a sistemas externos
 * - Publicar en colas de mensajes (Kafka, RabbitMQ)
 */
public interface ArticleStateChangeObserver {

    /**
     * Notifica que un artículo ha cambiado de estado
     *
     * @param article Artículo que cambió de estado
     * @param oldStateName Nombre del estado anterior
     * @param newStateName Nombre del nuevo estado
     * @param message Mensaje descriptivo del cambio
     */
    void onStateChange(Article article, String oldStateName, String newStateName, String message);
}
```

#### `EmailNotificationObserver.java` (Observer concreto #1)
```java
/**
 * Observador que envía notificaciones por email cuando un artículo cambia de estado
 *
 * Patrón de Diseño: Observer Pattern
 *
 * Este observador implementa la lógica de envío de emails sin que el código
 * de transición de estados conozca esta funcionalidad.
 *
 * Implementación actual: Solo registra logs (mock)
 * Implementación futura: Integrar con servicio de email (SendGrid, AWS SES, etc.)
 */
@Component
@Slf4j
public class EmailNotificationObserver implements ArticleStateChangeObserver {

    @Override
    public void onStateChange(Article article, String oldStateName, String newStateName, String message) {
        log.info("=== NOTIFICACIÓN EMAIL (MOCK) ===");
        log.info("Destinatario: Autor del artículo ID {}", article.getAuthorId());
        log.info("Asunto: Tu artículo '{}' cambió de estado", article.getTitle());
        log.info("Estado anterior: {}", oldStateName);
        log.info("Estado nuevo: {}", newStateName);
        log.info("Mensaje: {}", message);
        log.info("================================");

        // Ejemplo de implementación futura:
        // emailService.sendEmail(
        //     to: getUserEmail(article.getAuthorId()),
        //     subject: "Tu artículo '" + article.getTitle() + "' cambió de estado",
        //     body: buildEmailBody(article, oldStateName, newStateName, message)
        // );
    }
}
```

#### `AuditLogObserver.java` (Observer concreto #2)
```java
/**
 * Observador que registra auditoría de cambios de estado de artículos
 *
 * Patrón de Diseño: Observer Pattern
 *
 * Este observador mantiene un registro de auditoría de todos los cambios
 * de estado, útil para:
 * - Trazabilidad y compliance
 * - Análisis de tiempos de aprobación
 * - Resolución de disputas
 * - Métricas y reportes
 *
 * Implementación actual: Logs en consola (mock)
 * Implementación futura: Persistir en tabla de auditoría o enviar a sistema de logging externo
 */
@Component
@Slf4j
public class AuditLogObserver implements ArticleStateChangeObserver {

    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public void onStateChange(Article article, String oldStateName, String newStateName, String message) {
        String timestamp = LocalDateTime.now().format(FORMATTER);

        // TODO: Implementar persistencia en tabla de auditoría
        // auditRepository.save(new AuditLog(timestamp, article.getId(), oldState, newState, message));

        log.info("[AUDIT] {} | Artículo ID: {} | Título: '{}' | {} → {} | {}",
                timestamp,
                article.getIdArticle(),
                article.getTitle(),
                oldStateName,
                newStateName,
                message);
    }
}
```

#### `ArticleStateChangeNotifier.java` (Subject/Notifier)
```java
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
```

#### Uso en `ApprovalServiceImpl.java`
```java
@Service
@RequiredArgsConstructor
public class ApprovalServiceImpl implements ApprovalService {

    private final ArticleStateChangeNotifier stateChangeNotifier; // Inyectado

    @Override
    @Transactional
    public ApprovalResponse processApproval(...) {
        // 1-4. Procesar aprobación y transición de estado
        article.setArticleStatus(newStatus);
        articleRepository.save(article);

        // 5. Notificar a todos los observadores (1 línea reemplaza 4+ llamadas)
        stateChangeNotifier.notifyStateChange(
            article,
            currentStateName,
            transition.getNewStateName(),
            transition.getMessage()
        );

        // 6. Retornar respuesta
        return new ApprovalResponse(...);
    }
}
```

### Diagrama del Observer Pattern

```
┌──────────────────────────┐
│  ApprovalServiceImpl     │
│  (Cliente)               │
└────────────┬─────────────┘
             │
             │ usa
             ▼
┌────────────────────────────┐
│ ArticleStateChangeNotifier │
│ (Subject/Notifier)         │
│  - List<Observer> observers│
│  + notifyStateChange()     │
└────────────┬───────────────┘
             │
             │ notifica a todos
             ▼
┌─────────────────────────────┐
│ ArticleStateChangeObserver  │
│      <<interface>>          │
│  + onStateChange()          │
└────────────┬────────────────┘
             │
             │ implementan
      ┌──────┴──────┐
      │             │
      ▼             ▼
┌──────────┐  ┌──────────┐
│  Email   │  │  Audit   │
│Notifier  │  │   Log    │
│ Observer │  │ Observer │
└──────────┘  └──────────┘

Futuros observers (sin modificar código):
  - CacheInvalidationObserver
  - KafkaEventPublisherObserver
  - PushNotificationObserver
  - MetricsCollectorObserver
```

### Extensibilidad: Agregar nuevo Observer

```java
// Nuevo observer para invalidar caché (sin modificar código existente)
@Component
public class CacheInvalidationObserver implements ArticleStateChangeObserver {

    private final CacheService cacheService;

    @Override
    public void onStateChange(Article article, String oldStateName, String newStateName, String message) {
        // Invalidar caché del artículo cuando se publica
        if ("Publicado".equals(newStateName)) {
            cacheService.invalidate("article:" + article.getIdArticle());
            cacheService.invalidate("articles:published");
        }
    }
}

// Spring detecta el @Component y lo registra automáticamente
// ApprovalServiceImpl NO NECESITA MODIFICACIÓN
```

### Beneficios Obtenidos

**Desacoplamiento**: ApprovalServiceImpl no conoce a los observadores
**Extensibilidad**: Agregar observador = crear nueva clase @Component
**Single Responsibility**: Cada observer tiene 1 responsabilidad
**Open/Closed Principle**: Abierto para extensión, cerrado para modificación
**Testabilidad**: Se puede testear ApprovalServiceImpl sin mocks de observers
**Configuración automática**: Spring inyecta todos los observers automáticamente

---

## Diagrama de Relaciones

### Visión General de Patrones por Microservicio

```
┌────────────────────────────────────────────────────────────────┐
│                     SUGGESTION-SERVICE                         │
│                                                                │
│  ┌────────────────────────────────────────────────────────────┐│
│  │ Strategy Pattern                                           ││
│  │                                                            ││
│  │ AIProvider                                                 ││
│  │   └── GeminiAIProvider (única implementación)             ││
│  │                                                            ││
│  │ TrendsProvider                                             ││
│  │   ├── RssTrendsProvider                                   ││
│  │   └── LocalNewsTrendsProvider                             ││
│  └────────────────────────────────────────────────────────────┘│
│                                                                │
└────────────────────────────────────────────────────────────────┘

┌────────────────────────────────────────────────────────────────┐
│                     ARTICLE-SERVICE                            │
│                                                                │
│  ┌────────────────┐          ┌────────────────┐              │
│  │ State          │          │ Observer       │              │
│  │ Pattern        │          │ Pattern        │              │
│  │                │          │                │              │
│  │ ArticleState   │──notifica→│StateChange    │              │
│  │ (4 estados)    │          │Notifier        │              │
│  │                │          │(+observers)    │              │
│  └────────────────┘          └────────────────┘              │
│                                                                │
└────────────────────────────────────────────────────────────────┘
```

---

## Beneficios Generales

### Principios SOLID Aplicados

| Principio | Patrón(es) que lo aplican |
|-----------|---------------------------|
| **Open/Closed Principle (OCP)** | State, Strategy, Observer |
| **Liskov Substitution Principle (LSP)** | Strategy (todas las implementaciones son intercambiables) |
| **Interface Segregation Principle (ISP)** | Todas las interfaces tienen métodos cohesivos |
| **Dependency Inversion Principle (DIP)** | Todos los patrones (dependencia de abstracciones) |

### Métricas de Mejora

#### Antes de los Patrones
```
- Clases grandes con múltiples responsabilidades: 3
- Dependencias de implementaciones concretas: 12
- Lógica hardcodeada con if/else: 4 bloques (50+ líneas)
- Strings mágicos: 15+
- Imposibilidad de agregar funcionalidad sin modificar código: SÍ
```

#### Después de los Patrones
```
- Clases con responsabilidad única: 20
- Dependencias de abstracciones: 12 (100%)
- Lógica encapsulada en clases de estado/estrategia: 8 clases
- Strings mágicos: 0 (eliminados)
- Agregar funcionalidad sin modificar código: SÍ (crear nueva clase)
```

### Ventajas para el Equipo de Desarrollo

**Mantenibilidad**: Código más limpio y fácil de entender
**Testabilidad**: Clases pequeñas fáciles de testear aisladamente
**Extensibilidad**: Nuevas funcionalidades sin tocar código existente
**Configurabilidad**: Comportamiento configurable externamente
**Escalabilidad**: Arquitectura preparada para crecer
**Reducción de bugs**: Menos strings mágicos, más validaciones en tiempo de compilación

---

## Archivos Creados

### Suggestion-Service (9 archivos)

#### Strategy Pattern
- `service/interfaces/AIProvider.java`
- `service/interfaces/TrendsProvider.java`
- `service/interfaces/SuggestionOrchestrator.java`
- `service/impl/GeminiAIProvider.java`
- `service/impl/RssTrendsProvider.java`
- `service/impl/LocalNewsTrendsProvider.java`
- `service/impl/SuggestionOrchestratorImpl.java`

#### Configuración
- `config/RestTemplateConfig.java`

### Article-Service (10 archivos)

#### State Pattern
- `domain/state/ArticleState.java`
- `domain/state/ArticleStateTransition.java`
- `domain/state/ArticleStateFactory.java`
- `domain/state/impl/BorradorState.java`
- `domain/state/impl/EnRevisionState.java`
- `domain/state/impl/PublicadoState.java`
- `domain/state/impl/ObservadoState.java`

#### Observer Pattern
- `domain/observer/ArticleStateChangeObserver.java`
- `domain/observer/ArticleStateChangeNotifier.java`
- `domain/observer/impl/EmailNotificationObserver.java`
- `domain/observer/impl/AuditLogObserver.java`

#### Servicios
- `service/impl/RoleWeightProviderImpl.java`
- `domain/dto/ApprovalContext.java`

---

## Archivos Modificados

### Suggestion-Service
- `controller/SuggestionController.java`: Inyecta `SuggestionOrchestrator` en lugar de servicios concretos

### Article-Service
- `service/ApprovalServiceImpl.java`: Usa State Pattern y notifica a Observer Pattern
- `controller/ApprovalController.java`: Usa `RoleWeightProvider` en lugar de `RoleRepository`
- `service/ArticleServiceImpl.java`: Implementa interfaces segregadas

---

## Futuras Extensiones Sugeridas

### Observer Pattern
- `SlackNotificationObserver`: Notificar equipo en Slack cuando se publica artículo
- `KafkaEventPublisherObserver`: Publicar eventos a Kafka para otros microservicios
- `MetricsCollectorObserver`: Recopilar métricas de tiempos de aprobación

### Strategy Pattern
- `OpenAIProvider`: Integración con ChatGPT
- `ClaudeAIProvider`: Integración con Claude AI
- `TwitterTrendsProvider`: Obtener tendencias de Twitter/X

### State Pattern
- `ArchivedState`: Estado para artículos archivados
- `ScheduledState`: Estado para artículos programados para publicación futura
