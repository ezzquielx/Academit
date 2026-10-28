Contexto: Academit, backend Spring Boot (Java 21, Gradle). Implementá el módulo «Pagos» según el contrato del frontend.

Skills: si existen academit-backend-modulo, academit-reglas-negocio y academit-base-datos, cargalas; si no, seguí docs/backend/guia-de-modulos.md (secciones 2 y 3 y la ficha del módulo).
Rama: feature/back-comprobante-pago desde develop. Si dependés de un módulo que todavía no se integró, partí de la rama de ese módulo y decilo en el resumen. Commits según docs/flujo-de-trabajo.md, uno por punto, con "Refs: RF32, RN08, RN09". Sin push ni merge.

Ahorro de tokens: del contrato leé solo las filas del módulo con rg (docs/frontend/endpoints.csv, su entidad en docs/frontend/entidades.json y sus x-roles en docs/frontend/openapi.yaml); tomá como modelo de estilo los módulos de usuarios y auth, leídos por fragmentos; no pegues código en tu respuesta; tests UNA vez al final.

Endpoints del contrato (verificá con endpoints.csv):
- GET /api/pagos
- POST /api/pagos

Depende de: matrículas y cuotas (necesita cuotas existentes; mientras tanto, usá las del dato de prueba).

Reglas específicas:
1. POST registra el pago de UNA cuota. El pago es siempre por el importe completo (RN09): si el request trae importe y no coincide con el de la cuota, 400 con mensaje claro. No hay pagos parciales.
2. Rechazá con 409 si la cuota ya está PAGADA o ANULADA, o si su matrícula está CANCELADA. Un pago por cuota: capturá la violación de la restricción única de id_cuota y devolvé 409.
3. Todo en una sola transacción: guardar el pago, pasar la cuota a PAGADA y asignar el comprobante. Registrá el usuario que cobra (el del token) y la fecha de hoy.
4. Pago.comprobante: formato AAAA-NNNNNN (año del pago y id_pago con 6 dígitos, por ejemplo 2026-000123), único, generado por el backend después de obtener el id; es de solo lectura para el cliente.
5. Medios de pago válidos: EFECTIVO, TRANSFERENCIA, TARJETA, OTRO. Campos desnormalizados del contrato (idAlumno, concepto) se calculan, no se persisten.
6. Permisos según x-roles: registrar pagos lo hacen ADMINISTRADOR y ADMINISTRATIVO; el alumno ve solo sus pagos.

Tests obligatorios (además de 200 o 201, 400, 401, 403 y 404 por endpoint):
- Importe distinto del de la cuota: 400 y la cuota sigue sin pagar.
- Pago completo: 201, cuota PAGADA y comprobante que cumple el patrón ^\d{4}-\d{6}$.
- Segundo pago de la misma cuota: 409. Cuota ANULADA: 409.
- ALUMNO haciendo POST: 403. Dos pagos seguidos generan comprobantes distintos y correlativos.

Pasos comunes, en este orden: DTOs con los nombres exactos del contrato (campos calculados solo en response, Bean Validation en español); service con las reglas; controller con TODOS los endpoints del módulo (mismas rutas y métodos que endpoints.csv, arrays completos sin paginación, permisos por rol según x-roles: 401 sin sesión, 403 con rol no permitido, errores como ProblemDetail con message y errors); tests (service con Mockito, controller con @WebMvcTest).

Verificación (una vez): nix-shell --run "cd backend && ./gradlew test -q | tail -30"; la aplicación arranca con ddl-auto=validate contra la base con el esquema y los datos cargados (db-start y db-reset del nix-shell; si no hay PostgreSQL, decilo y seguí).
Cierre: actualizá la fila del módulo en docs/estado-del-proyecto.md (python3 tools/estado-proyecto.py regenera las cifras) y tildá la tarea en docs/pendientes.md. Sin modificar el frontend, listá qué pantallas del módulo ya se pueden probar con VITE_USE_MOCK=false y cualquier diferencia entre lo implementado y el contrato.
No inventes endpoints ni campos. No toques frontend/ ni database/ sin avisarlo en el resumen. Lo ambiguo va a docs/estado-del-proyecto.md como decisión abierta y seguí.
Entrega: resumen de máximo 6 líneas: endpoints implementados sobre el total del módulo, reglas cubiertas, tests y dudas abiertas.
