# Refactorización SOLID - Portal Periodístico Microservicios

En este README se detalla la refactorización de los tres microservicios del Portal Periodístico para implementar los principios SOLID (Open/Closed, Liskov Substitution, Interface Segregation, Dependency Inversion), excluyendo Single Responsibility Principle

## Principios SOLID Aplicados

### 1. Dependency Inversion Principle (DIP)
**Antes**: Controllers y servicios dependían de implementaciones concretas (repositorios, clases concretas)
**Ahora**: Controllers y servicios dependen de abstracciones (interfaces)

**Beneficios**:
- Mejor testabilidad (fácil de mockear)
- Flexibilidad para cambiar implementaciones
- Desacoplamiento entre capas

### 2. Open/Closed Principle (OCP)
**Antes**: Agregar nuevas funcionalidades requería modificar código existente
**Ahora**: Se pueden agregar nuevas funcionalidades extendiendo (creando nuevas clases) sin modificar código existente

**Implementaciones**:
- **State Pattern** para estados de artículos
- **Interfaces** para proveedores de IA y tendencias
- **Abstracciones** para servicios de autenticación

### 3. Interface Segregation Principle (ISP)
**Antes**: Interfaces grandes con múltiples responsabilidades
**Ahora**: Interfaces pequeñas y focalizadas

**Ejemplos**:
- `ArticleService` segregado en `ArticleCommandService`, `ArticleQueryService`, `ArticleWorkflowService`
- `JwtService` segregado en `TokenProvider` + `TokenClaimsExtractor`
- Servicios con interfaces de un solo propósito

### 4. Liskov Substitution Principle (LSP)
**Aplicado**: Todas las implementaciones respetan los contratos de sus interfaces
**Documentado**: Contratos de `UserPrincipal` consistentes entre servicios

---

## Suggestion-Service

### Cambios Realizados

#### Interfaces Creadas

1. **`AIProvider`** (`service/interfaces/AIProvider.java`)
   - Abstracción para proveedores de inteligencia artificial
   - Permite cambiar de Gemini a OpenAI, Claude, etc. sin modificar código
   - Principio aplicado: DIP, OCP

2. **`TrendsProvider`** (`service/interfaces/TrendsProvider.java`)
   - Abstracción para proveedores de tendencias
   - Permite cambiar de RSS a Twitter API, Reddit, etc.
   - Principio aplicado: DIP, OCP

3. **`SuggestionOrchestrator`** (`service/interfaces/SuggestionOrchestrator.java`)
   - Orquestador que coordina TrendsProvider + AIProvider
   - Principio aplicado: DIP, ISP

#### Implementaciones Creadas

4. **`RestTemplateConfig`** (`config/RestTemplateConfig.java`)
   - Bean de RestTemplate para inyección de dependencias
   - Principio aplicado: DIP

5. **`GeminiAIProvider`** (`service/impl/GeminiAIProvider.java`)
   - Implementación concreta de AIProvider usando Gemini
   - Renombrado desde `GeminiService`
   - Ahora inyecta RestTemplate en lugar de crearlo

6. **`RssTrendsProvider`** (`service/impl/RssTrendsProvider.java`)
   - Implementación concreta de TrendsProvider usando RSS
   - Renombrado desde `RssService`

7. **`SuggestionOrchestratorImpl`** (`service/impl/SuggestionOrchestratorImpl.java`)
   - Implementación del orquestador
   - Encapsula la lógica de coordinación

#### Archivos Modificados

8. **`SuggestionController`**
   - **Antes**: Dependía de `RssService` y `GeminiService` (concretos)
   - **Ahora**: Depende de `SuggestionOrchestrator` (interfaz)
   - Código simplificado de 8 líneas a 2 líneas en el endpoint

### Violaciones SOLID Resueltas

   **DIP**: Controller ya no depende de implementaciones concretas
   **OCP**: Nuevos proveedores de IA/tendencias se agregan sin modificar código
   **ISP**: Interfaces pequeñas (1 método cada una)

---

## Auth-Service

### Cambios Realizados

#### Interfaces Creadas

1. **`AuthenticationService`** (`service/interfaces/AuthenticationService.java`)
   - Operaciones de autenticación (login, register)
   - Principio aplicado: DIP, ISP

2. **`TokenProvider`** (`service/interfaces/TokenProvider.java`)
   - Generación y validación de tokens
   - Principio aplicado: ISP (segregada de extracción)

3. **`TokenClaimsExtractor`** (`service/interfaces/TokenClaimsExtractor.java`)
   - Extracción de claims del token
   - Principio aplicado: ISP (segregada de generación/validación)

4. **`UserManagementService`** (`service/interfaces/UserManagementService.java`)
   - Gestión administrativa de usuarios
   - Principio aplicado: DIP, ISP

#### Implementaciones Creadas

5. **`JwtTokenProvider`** (`service/impl/JwtTokenProvider.java`)
   - Renombrado desde `JwtService`
   - Implementa `TokenProvider` + `TokenClaimsExtractor`

6. **`AuthenticationServiceImpl`** (`service/impl/AuthenticationServiceImpl.java`)
   - Lógica de login/register extraída de `AuthController`
   - Encapsula toda la lógica de autenticación

7. **`AdminUserServiceImpl`** (`service/impl/AdminUserServiceImpl.java`)
   - Renombrado desde `AdminUserService`
   - Implementa `UserManagementService`

#### Archivos Modificados

8. **`AuthController`**
   - **Antes**: Dependía de `UserRepository`, `RoleRepository`, `JwtService`, `PasswordEncoder` (4 dependencias concretas)
   - **Ahora**: Depende de `AuthenticationService` (1 interfaz)
   - Login y Register delegados al servicio

9. **`AdminUserController`**
   - **Antes**: Dependía de `AdminUserService` (concreto)
   - **Ahora**: Depende de `UserManagementService` (interfaz)

10. **`JwtAuthenticationFilter`**
    - **Antes**: Dependía de `JwtService` (concreto)
    - **Ahora**: Depende de `TokenClaimsExtractor` + `TokenProvider` (interfaces segregadas)

### Violaciones SOLID Resueltas

   **DIP**: Controllers no dependen de repositorios ni clases concretas
   **OCP**: Lógica de autenticación encapsulada, extensible sin modificar controller
   **ISP**: JwtService segregado en 2 interfaces focalizadas
   **LSP**: Implementaciones consistentes

---

## Article-Service

### Cambios Realizados

#### Interfaces Creadas

1. **`ArticleCommandService`** (`service/interfaces/ArticleCommandService.java`)
   - Operaciones de escritura (create, update, delete)
   - Principio aplicado: ISP (CQRS pattern)

2. **`ArticleQueryService`** (`service/interfaces/ArticleQueryService.java`)
   - Operaciones de lectura (getAll, getById, etc.)
   - Principio aplicado: ISP (CQRS pattern)

3. **`ArticleWorkflowService`** (`service/interfaces/ArticleWorkflowService.java`)
   - Operaciones de workflow (sendToReview)
   - Principio aplicado: ISP

4. **`RoleWeightProvider`** (`service/interfaces/RoleWeightProvider.java`)
   - Abstracción sobre información de roles
   - Principio aplicado: DIP

#### State Pattern Implementado

5. **`ArticleState`** (`domain/state/ArticleState.java`)
   - Interfaz para el patrón State
   - Define comportamiento de cada estado
   - Principio aplicado: OCP, LSP

6. **`ArticleStateTransition`** (`domain/state/ArticleStateTransition.java`)
   - DTO que representa el resultado de una transición

7. **`BorradorState`** (`domain/state/impl/BorradorState.java`)
   - Estado inicial: puede editarse y enviarse a revisión

8. **`EnRevisionState`** (`domain/state/impl/EnRevisionState.java`)
   - Estado en revisión: procesa aprobaciones/rechazos
   - Lógica de transición a Publicado (100%) u Observado (rechazo)

9. **`PublicadoState`** (`domain/state/impl/PublicadoState.java`)
   - Estado final: no puede modificarse

10. **`ObservadoState`** (`domain/state/impl/ObservadoState.java`)
    - Estado de rechazo: puede editarse y reenviarse

11. **`ArticleStateFactory`** (`domain/state/ArticleStateFactory.java`)
    - Factory para obtener instancias de estados
    - Centraliza creación de estados

#### Clases de Soporte

12. **`RoleWeightProviderImpl`** (`service/impl/RoleWeightProviderImpl.java`)
    - Implementación que encapsula acceso a RoleRepository

13. **`ApprovalContext`** (`domain/dto/ApprovalContext.java`)
    - DTO que reduce parámetros de `processApproval` de 6 a 2

#### Archivos Modificados

14. **`ApprovalServiceImpl`**
    - **Antes**: Lógica de transiciones hardcodeada con if/else y strings (líneas 74-94)
    - **Ahora**: Usa State Pattern, delega transiciones a cada estado
    - Código más limpio y extensible

15. **`ApprovalController`**
    - **Antes**: Dependía de `RoleRepository` (concreto)
    - **Ahora**: Depende de `RoleWeightProvider` (interfaz)

### Violaciones SOLID Resueltas

   **DIP**: Controllers no dependen de repositorios
   **OCP**: State Pattern permite agregar nuevos estados sin modificar código existente
   **ISP**: ArticleService segregado en 3 interfaces, ApprovalContext reduce parámetros
   **LSP**: Estados pueden sustituirse polimórficamente

### Diagrama de Estados (State Pattern)

```
[Borrador]
    ↓ (sendToReview)
[En revisión]
    ├─→ APROBADO → Si < 100%: [En revisión]
    │            → Si ≥ 100%: [Publicado] ✓
    └─→ RECHAZADO → [Observado]
                    ↓ (edit + sendToReview)
                [En revisión] (reinicia con 0%)
```

---

## Archivos Creados

### Suggestion-Service (7 archivos)
- `config/RestTemplateConfig.java`
- `service/interfaces/AIProvider.java`
- `service/interfaces/TrendsProvider.java`
- `service/interfaces/SuggestionOrchestrator.java`
- `service/impl/GeminiAIProvider.java`
- `service/impl/RssTrendsProvider.java`
- `service/impl/SuggestionOrchestratorImpl.java`

### Auth-Service (7 archivos)
- `service/interfaces/AuthenticationService.java`
- `service/interfaces/TokenProvider.java`
- `service/interfaces/TokenClaimsExtractor.java`
- `service/interfaces/UserManagementService.java`
- `service/impl/JwtTokenProvider.java`
- `service/impl/AuthenticationServiceImpl.java`
- `service/impl/AdminUserServiceImpl.java`

### Article-Service (13 archivos)
- `service/interfaces/ArticleCommandService.java`
- `service/interfaces/ArticleQueryService.java`
- `service/interfaces/ArticleWorkflowService.java`
- `service/interfaces/RoleWeightProvider.java`
- `domain/state/ArticleState.java`
- `domain/state/ArticleStateTransition.java`
- `domain/state/impl/BorradorState.java`
- `domain/state/impl/EnRevisionState.java`
- `domain/state/impl/PublicadoState.java`
- `domain/state/impl/ObservadoState.java`
- `domain/state/ArticleStateFactory.java`
- `service/impl/RoleWeightProviderImpl.java`
- `domain/dto/ApprovalContext.java`

**Total: 27 archivos nuevos**

---

## Archivos Modificados

### Suggestion-Service (1 archivo)
- `controller/SuggestionController.java`

### Auth-Service (3 archivos)
- `controller/AuthController.java`
- `controller/AdminUserController.java`
- `config/JwtAuthenticationFilter.java`

### Article-Service (2 archivos)
- `service/ApprovalServiceImpl.java`
- `controller/ApprovalController.java`

**Total: 6 archivos modificados**

---

## Beneficios Obtenidos

### 1. Mantenibilidad
- **Código más limpio**: Responsabilidades claras y segregadas
- **Fácil de entender**: Interfaces pequeñas con nombres descriptivos
- **Documentación**: Todos los archivos tienen comentarios en español explicando principios SOLID aplicados

### 2. Testabilidad
- **Fácil de mockear**: Interfaces permiten crear mocks fácilmente
- **Inyección de dependencias**: RestTemplate, servicios inyectados en lugar de creados
- **Aislamiento**: Lógica de negocio separada de controllers

### 3. Extensibilidad
- **Nuevos proveedores de IA**: Solo crear nueva clase implementando `AIProvider`
- **Nuevos estados de artículo**: Solo crear nueva clase implementando `ArticleState`
- **Nuevas fuentes de tendencias**: Solo crear nueva clase implementando `TrendsProvider`
- **No se modifica código existente** (principio OCP)

### 4. Flexibilidad
- **Cambiar implementaciones**: De Gemini a OpenAI, de RSS a Twitter API, etc.
- **Sin impacto en consumidores**: Controllers no se modifican

### 5. Desacoplamiento
- **Controllers no dependen de repositorios**
- **Servicios dependen de abstracciones**
- **Capas bien definidas**


## Notas Importantes

### Compatibilidad
- **Todos los endpoints existentes funcionan igual**
- **No se modificó la lógica de negocio**
- **100% compatible con el frontend existente**

### Migraciones
- **No se requieren migraciones de base de datos**
- **Solo cambios a nivel de código (refactorización)**

### Archivos Antiguos
Los siguientes archivos se mantuvieron para compatibilidad pero ya no se usan directamente:
- `GeminiService.java` → Reemplazado por `GeminiAIProvider`
- `RssService.java` → Reemplazado por `RssTrendsProvider`
- `JwtService.java` → Reemplazado por `JwtTokenProvider`
- `AdminUserService.java` → Reemplazado por `AdminUserServiceImpl`

Posiblemente estos archivos sean eliminados al generar el deploy y validar todas las funciones.

