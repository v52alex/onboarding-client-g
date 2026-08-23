# Project status

```yaml
project_workflow:
  type: new-development
  status: releasable
  methodology: scrum
  owner: Alexis
  last_reviewed: 2026-08-22
  rationale: "Sprint 5 completó regresión automatizada y E2E remoto para auditoría asíncrona confiable."
```

## Sprint actual

- Sprint: 5 — Auditoría asíncrona y outbox confiable.
- El publicador reclama eventos pendientes, los entrega a RabbitMQ y registra
  publicación o reintento con backoff; el `eventId` preserva idempotencia
  extremo a extremo.
- Flyway V3 persiste revisión y eventos operativos; V4–V6 habilitan publicación,
  replay idempotente y recuperación de eventos pendientes.
- Los endpoints `/api/v1/case-management/**` exigen el rol Keycloak
  `onboarding-manager`; el onboarding del prospecto permanece público.
- La API entrega búsqueda paginada, expediente agregado, autoasignación y
  aprobación/rechazo con reglas de transición.
- La suite Java pasó 37 pruebas; incluye `401`, `403`, ciclo operativo, motivo
  obligatorio para rechazo y publicación/reintento del outbox.
- La imagen fue empaquetada, reconstruida y desplegada en `windows-docker`;
  Flyway confirmó la versión 6 sobre MySQL 8.4.
- RabbitMQ confirmó la publicación de los eventos y Audit Service quedó
  saludable mediante el túnel SSH local; el E2E remoto completó KYC, documento,
  OTP, contrato y su línea de tiempo de auditoría.
