Contexto: Academit, backend Spring Boot (Java 21, Gradle). Implementá el módulo «Matrículas y cuotas» según el contrato del frontend.

Skills: si existen academit-backend-modulo, academit-reglas-negocio y academit-base-datos, cargalas; si no, seguí docs/backend/guia-de-modulos.md (secciones 2 y 3 y la ficha del módulo).
Rama: feature/back-matriculas-cuotas desde develop. Si dependés de un módulo que todavía no se integró, partí de la rama de ese módulo y decilo en el resumen. Commits según docs/flujo-de-trabajo.md, uno por punto, con "Refs: RF28, RF46, RN18". Sin push ni merge.

Ahorro de tokens: del contrato leé solo las filas del módulo con rg (docs/frontend/endpoints.csv, su entidad en docs/frontend/entidades.json y sus x-roles en docs/frontend/openapi.yaml); tomá como modelo de estilo los módulos de usuarios y auth, leídos por fragmentos; no pegues código en tu respuesta; tests UNA vez al final.

Endpoints del contrato (verificá con endpoints.csv):
- GET /api/matriculas
- PUT /api/matriculas/{id}
- PATCH /api/matriculas/{id}
- POST /api/matriculas/{id}/cuotas (genera las cuotas)
- GET /api/cuotas
- PATCH /api/cuotas/{id}/anular

Depende de: alumnos y ciclos lectivos (usá los datos de prueba mientras no estén integrados).

Reglas específicas:
1. Una matrícula por alumno y ciclo (restricción única): ante duplicado, 409. Estados guardados de la matrícula: PENDIENTE, ACTIVA, CANCELADA.
2. POST /api/matriculas/{id}/cuotas genera 12 cuotas CUOTA_MENSUAL (numero 1 a 12) y 3 seguros SEGURO_CUATRIMESTRAL (numero 1 a 3) (RN18). Es idempotente por matrícula: si ya existen, 409. Solo para matrículas ACTIVA. Todo en una transacción. Tomá los importes y vencimientos de lo que defina el análisis o la matrícula; si no están definidos, usá un criterio simple y explícito (mensuales el día 10 de cada mes del ciclo; seguros cada cuatro meses) y registralo como decisión abierta.
3. Matricula.estadoPago NO se guarda: el DTO lo calcula con las cuotas. EN_MORA si hay al menos una VENCIDA; AL_DIA si todas las ya vencidas (o todas) están PAGADA; PENDIENTE en el resto.
4. El estado VENCIDA de una cuota se resuelve así: si está PENDIENTE y su vencimiento es anterior a hoy, el response la devuelve como VENCIDA aunque la tarea programada todavía no la haya persistido (de eso se ocupa el módulo cuotas-vencidas; no crees el scheduler acá). Una cuota que vence hoy NO está vencida.
5. Cancelar una matrícula (PATCH o PUT con estado CANCELADA) pasa a ANULADA sus cuotas PENDIENTE o VENCIDA; las PAGADA no se tocan.
6. PATCH /api/cuotas/{id}/anular: solo ADMINISTRADOR; no se puede anular una cuota PAGADA (409). Cuota tiene idMatricula; idAlumno es un campo calculado.
7. Permisos según x-roles; el alumno ve solo sus matrículas y cuotas.

Tests obligatorios (además de 200 o 201, 400, 401, 403 y 404 por endpoint):
- Generación crea exactamente 15 cuotas con la numeración correcta; segunda llamada: 409; matrícula no ACTIVA: 409.
- estadoPago en los tres casos (EN_MORA, AL_DIA, PENDIENTE), con un reloj inyectable (Clock) para no depender de la fecha real.
- Cancelar anula las pendientes y vencidas y deja intactas las pagadas.
- Anular una cuota pagada: 409; anular siendo ADMINISTRATIVO: 403.

Decisiones a registrar si el análisis no las define:
- Importe y fechas de vencimiento de cuotas y seguros
- Quién puede pasar una matrícula de PENDIENTE a ACTIVA

Pasos comunes, en este orden: DTOs con los nombres exactos del contrato (campos calculados solo en response, Bean Validation en español); service con las reglas; controller con TODOS los endpoints del módulo (mismas rutas y métodos que endpoints.csv, arrays completos sin paginación, permisos por rol según x-roles: 401 sin sesión, 403 con rol no permitido, errores como ProblemDetail con message y errors); tests (service con Mockito, controller con @WebMvcTest).

Verificación (una vez): nix-shell --run "cd backend && ./gradlew test -q | tail -30"; la aplicación arranca con ddl-auto=validate contra la base con el esquema y los datos cargados (db-start y db-reset del nix-shell; si no hay PostgreSQL, decilo y seguí).
Cierre: actualizá la fila del módulo en docs/estado-del-proyecto.md (python3 tools/estado-proyecto.py regenera las cifras) y tildá la tarea en docs/pendientes.md. Sin modificar el frontend, listá qué pantallas del módulo ya se pueden probar con VITE_USE_MOCK=false y cualquier diferencia entre lo implementado y el contrato.
No inventes endpoints ni campos. No toques frontend/ ni database/ sin avisarlo en el resumen. Lo ambiguo va a docs/estado-del-proyecto.md como decisión abierta y seguí.
Entrega: resumen de máximo 6 líneas: endpoints implementados sobre el total del módulo, reglas cubiertas, tests y dudas abiertas.
