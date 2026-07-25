# Reporte de pruebas

Fecha de ejecución: 2026-07-25  
Rama: `dev`  
Runtime: Java 21.0.6  
Build: Spring Boot 3.5.16 / Maven

## Resultado

| Métrica | Resultado |
|---|---:|
| Pruebas ejecutadas | 17 |
| Pruebas unitarias | 14 |
| Pruebas de integración | 3 |
| Fallos | 0 |
| Errores | 0 |
| Omitidas | 0 |
| Build | Exitoso |

Comando utilizado:

```bash
mvn clean verify
```

También se validó la sintaxis de los archivos JSON:

```bash
jq empty postman/collection.json src/main/resources/interactions/onboarding.json
```

## Cobertura funcional

| Componente | Tipo | Casos verificados |
|---|---|---|
| `OnboardingOrchestrator` | Unitario | Inicio, transición exitosa, acción inválida, caso completado y caso inexistente |
| `WorkflowCatalog` | Unitario | Carga válida, destino inexistente, claves duplicadas y workflow desconocido |
| `IdentityVerificationHandler` | Unitario parametrizado | `VERIFIED`, `REJECTED` y `MANUAL_REVIEW` |
| `JsonSectionActionHandler` | Unitario | Persistencia de sección sin eliminar datos existentes |
| `NoOpBiometricVerificationAdapter` | Unitario | Enrutamiento seguro a revisión manual |
| API REST | Integración | Creación, consulta de interacción y respuesta `409` para una acción inválida |
| Flujo completo | Integración | Las nueve acciones avanzan el caso hasta `COMPLETED` |
| Persistencia | Integración | Flyway, JPA, H2 y versionado optimista |

## Archivos de prueba

- `src/test/java/com/v52alex/onboarding/application/OnboardingOrchestratorTest.java`
- `src/test/java/com/v52alex/onboarding/application/WorkflowCatalogTest.java`
- `src/test/java/com/v52alex/onboarding/application/handlers/IdentityVerificationHandlerTest.java`
- `src/test/java/com/v52alex/onboarding/application/handlers/JsonSectionActionHandlerTest.java`
- `src/test/java/com/v52alex/onboarding/integration/biometric/NoOpBiometricVerificationAdapterTest.java`
- `src/test/java/com/v52alex/onboarding/OnboardingApiIntegrationTest.java`
- `src/test/java/com/v52alex/onboarding/OnboardingFlowIntegrationTest.java`

## Colección de API

`postman/collection.json` contiene:

- Health check.
- Creación del caso y almacenamiento automático de `caseId`.
- Consulta de la interacción actual.
- Las nueve acciones del onboarding en orden.
- Verificaciones Postman para estado HTTP, revisión biométrica manual y finalización.

La variable `baseUrl` tiene como valor inicial `http://localhost:8080`.

## Alcance

Este reporte confirma comportamiento y empaquetado, pero no representa cobertura
porcentual de líneas o ramas. No se agregó JaCoCo porque el objetivo actual era
incorporar pruebas útiles sin añadir herramientas antes de definir un quality gate
de CI/CD.

