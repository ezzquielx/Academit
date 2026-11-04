# Auditoría automática de UI

Generada con `node tools/auditar-ui.mjs` contra http://127.0.0.1:5199 (VITE_USE_MOCK=true).
Anchos: 360, 768, 1024, 1440 px. Roles: ADMINISTRADOR, ADMINISTRATIVO, DOCENTE, ALUMNO, ALUMNO_NUEVO.

## Resultado actual

| Rol | Ruta | Ancho | Problema |
| --- | --- | ---: | --- |
| — | — | — | Sin problemas detectados |

Total de hallazgos: 0.

## Antes / después

| Rol | Ruta | Ancho | Hallazgo inicial | Estado |
| --- | --- | ---: | --- | --- |
| ADMINISTRADOR | /solicitudes/bandeja | 360 | Botones de filtro/estado fuera del viewport | Resuelto: `.btn-group` se apila a ancho completo en móvil |
| ADMINISTRADOR | /administrativo/cuotas | 360 | Botones de filtro fuera del viewport | Resuelto: idem filtros de cuotas |
| ADMINISTRATIVO | /solicitudes/bandeja | 360 | Botones de filtro/estado fuera del viewport | Resuelto: idem bandeja |
| ADMINISTRATIVO | /administrativo/cuotas | 360 | Botones de filtro fuera del viewport | Resuelto: idem cuotas |
| DOCENTE | /docencia/mis-cursos | 360 | Acciones de la tarjeta fuera del viewport | Resuelto: filas de acciones con `flex-wrap` |
| DOCENTE | /docencia/mis-cursos | 768 | Acciones de la tarjeta fuera del viewport | Resuelto: idem |
| DOCENTE | /docencia/mis-cursos | 1024 | Acciones de la tarjeta fuera del viewport | Resuelto: idem |
| ALUMNO_NUEVO | /registro | 1440 | El alumno registrado no podía iniciar sesión en mock | Resuelto: el registro mock crea alumno + usuario con `idAlumno` |

## Pendientes que el script no puede resolver

- **Aprobar/rechazar declaraciones juradas y descargar PDF:** no hay endpoint en el contrato;
  el botón de descarga queda deshabilitado con nota (ver `docs/pendientes.md`).
- **`idAlumno` en la sesión real:** `UsuarioSesionDTO` no lo expone, así que con `VITE_USE_MOCK=false`
  el frontend no puede identificar al alumno logueado y muestra el aviso de legajo (ver `docs/pendientes.md`).
