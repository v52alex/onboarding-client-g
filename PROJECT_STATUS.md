# Project status

```yaml
project_workflow:
  type: new-development
  status: releasable
  methodology: scrum
  owner: Alexis
  last_reviewed: 2026-08-07
  rationale: "Sprint 4 incorpora gestión operativa protegida por Keycloak sobre los casos completados."
```

## Sprint actual

- Sprint: 4 — Bandeja, asignación y decisión operativa.
- Flyway V3 persiste revisión y eventos de auditoría, y migra casos completados
  existentes a `PENDING`.
- Los endpoints `/api/v1/case-management/**` exigen el rol Keycloak
  `case-manager`; el onboarding del prospecto permanece público.
- La API entrega búsqueda paginada, expediente agregado, autoasignación y
  aprobación/rechazo con reglas de transición.
- La suite Java pasó 35 pruebas; incluye `401`, `403`, ciclo operativo y motivo
  obligatorio para rechazo.
- La imagen fue empaquetada, reconstruida y desplegada en `windows-docker`;
  Flyway confirmó la versión 3 sobre MySQL 8.4.
