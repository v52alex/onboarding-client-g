# Project status

```yaml
project_workflow:
  type: new-development
  status: releasable
  methodology: scrum
  owner: Alexis
  last_reviewed: 2026-08-07
  rationale: "El orquestador está en validación de contrato e integración end-to-end con us-onboarding-ang y content-service."
```

## Sprint actual

- Sprint: 3 — KYC documental e integración de contenido.
- El contrato KYC exige una referencia documental persistida.
- La referencia debe pertenecer al caso, estar disponible y apuntar a Content Service.
- El contrato OpenAPI documenta la carga multipart usada por el frontend.
- La suite Java pasó 32 pruebas y el smoke remoto confirmó rechazo sin documento
  (`400`), carga `AVAILABLE` y evaluación KYC `APPROVED` con documento del caso.
- Cambio previo al cierre: workflow v2 añade `request-otp` y reemplaza la captura
  directa de contacto por un desafío OTP dummy ligado al caso.
- La nueva imagen fue desplegada en `windows-docker`; el smoke remoto pasó
  destino inválido `400`, código incorrecto `400` y código `123456` `VERIFIED`.
