Contexto: Academit, backend Spring Boot (Java 21, Gradle). Implementá el módulo «Tarea programada de cuotas vencidas» según el contrato del frontend.

Skills: si existen academit-backend-modulo, academit-reglas-negocio y academit-base-datos, cargalas; si no, seguí docs/backend/guia-de-modulos.md (secciones 2 y 3 y la ficha del módulo).
Rama: feature/back-cuotas-vencidas desde develop. Si dependés de un módulo que todavía no se integró, partí de la rama de ese módulo y decilo en el resumen. Commits según docs/flujo-de-trabajo.md, uno por punto, con "Refs: RF30, RN07". Sin push ni merge.

Ahorro de tokens: del contrato leé solo las filas del módulo con rg (docs/frontend/endpoints.csv, su entidad en docs/frontend/entidades.json y sus x-roles en docs/frontend/openapi.yaml); tomá como modelo de estilo los módulos de usuarios y auth, leídos por fragmentos; no pegues código en tu respuesta; tests UNA vez al final.

Endpoints del contrato (verificá con endpoints.csv):
- Sin endpoints nuevos: es una tarea programada interna

Depende de: matrículas y cuotas (la entidad Cuota y su repository ya existen).

Reglas específicas:
1. Un componente con @Scheduled que una vez por día pasa a VENCIDA toda cuota PENDIENTE cuyo vencimiento sea anterior a hoy. Una cuota que vence hoy no está vencida. No toques cuotas PAGADA ni ANULADA.
2. Habilitá @EnableScheduling en una clase de configuración. El cron y la zona horaria van configurables en application.properties (por ejemplo academit.cuotas.cron con valor por defecto diario de madrugada y zona America/Argentina/Tucuman).
3. La lógica vive en un método de servicio idempotente y transaccional (marcarVencidas) que devuelve la cantidad actualizada y la registra en el log; el @Scheduled solo lo invoca. Usá la consulta del repository que ya existe para esto (RF30) o creala si falta.
4. Inyectá un Clock para poder probar con fechas fijas. No crees endpoints; el cálculo dinámico de VENCIDA en las respuestas lo hace el módulo de matrículas y cuotas.
5. Ejecución al arrancar: no la agregues salvo que el análisis lo pida.

Tests obligatorios (además de 200 o 201, 400, 401, 403 y 404 por endpoint):
- Con Clock fijo: una cuota PENDIENTE vencida ayer pasa a VENCIDA; una que vence hoy no; PAGADA y ANULADA no cambian.
- Ejecutar dos veces seguidas no cambia nada más (idempotente) y devuelve 0 la segunda vez.
- El método del servicio es transaccional y el @Scheduled lo invoca (verificalo con Mockito).

Pasos comunes, en este orden: DTOs con los nombres exactos del contrato (campos calculados solo en response, Bean Validation en español); service con las reglas; controller con TODOS los endpoints del módulo (mismas rutas y métodos que endpoints.csv, arrays completos sin paginación, permisos por rol según x-roles: 401 sin sesión, 403 con rol no permitido, errores como ProblemDetail con message y errors); tests (service con Mockito, controller con @WebMvcTest).

Verificación (una vez): nix-shell --run "cd backend && ./gradlew test -q | tail -30"; la aplicación arranca con ddl-auto=validate contra la base con el esquema y los datos cargados (db-start y db-reset del nix-shell; si no hay PostgreSQL, decilo y seguí).
Cierre: actualizá la fila del módulo en docs/estado-del-proyecto.md (python3 tools/estado-proyecto.py regenera las cifras) y tildá la tarea en docs/pendientes.md. Sin modificar el frontend, listá qué pantallas del módulo ya se pueden probar con VITE_USE_MOCK=false y cualquier diferencia entre lo implementado y el contrato.
No inventes endpoints ni campos. No toques frontend/ ni database/ sin avisarlo en el resumen. Lo ambiguo va a docs/estado-del-proyecto.md como decisión abierta y seguí.
Entrega: resumen de máximo 6 líneas: endpoints implementados sobre el total del módulo, reglas cubiertas, tests y dudas abiertas.
