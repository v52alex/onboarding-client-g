# Project status

```yaml
project_workflow:
  type: new-development
  status: in-validation
  methodology: scrum
  owner: Alexis
  last_reviewed: 2026-08-22
  rationale: "Sprint 5 está desplegado y en validación integrada con auditoría asíncrona confiable."
```

## Sprint actual

- Sprint: 5 — Auditoría asíncrona y outbox confiable.
- El publicador reclama eventos pendientes, los entrega a RabbitMQ y registra
  publicación o reintento con backoff; el `eventId` preserva idempotencia
  extremo a extremo.
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
- La suite Java pasó 37 pruebas. RabbitMQ confirmó la publicación de los eventos
  y Audit Service quedó saludable mediante el túnel SSH local.
