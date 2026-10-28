Contexto: Academit, backend Spring Boot (Java 21, Gradle). Implementá el módulo «Inscripciones» según el contrato del frontend.

Skills: si existen academit-backend-modulo, academit-reglas-negocio y academit-base-datos, cargalas; si no, seguí docs/backend/guia-de-modulos.md (secciones 2 y 3 y la ficha del módulo).
Rama: feature/back-inscripciones desde develop. Si dependés de un módulo que todavía no se integró, partí de la rama de ese módulo y decilo en el resumen. Commits según docs/flujo-de-trabajo.md, uno por punto, con "Refs: RF22, RN02, RN03". Sin push ni merge.

Ahorro de tokens: del contrato leé solo las filas del módulo con rg (docs/frontend/endpoints.csv, su entidad en docs/frontend/entidades.json y sus x-roles en docs/frontend/openapi.yaml); tomá como modelo de estilo los módulos de usuarios y auth, leídos por fragmentos; no pegues código en tu respuesta; tests UNA vez al final.

Endpoints del contrato (verificá con endpoints.csv):
- GET /api/inscripciones
- POST /api/inscripciones
- DELETE /api/inscripciones/{id} (anula la inscripción)

Depende de: cursos, alumnos y ciclos lectivos (mientras no estén integrados, desarrollá contra los datos de prueba de database/02-datos-iniciales.sql).

Reglas específicas:
1. Una sola inscripción ACTIVA por alumno y ciclo lectivo (RN02). Validala en el service ANTES de que salte la restricción única de la base y respondé 409 con un mensaje claro. El alumno debe poder reinscribirse después de anular (índice único parcial solo sobre activas).
2. Cupo del curso (RN03): inscripciones activas del curso < capacidad máxima. Si no hay cupo, 409. Evitá la condición de carrera: leé el curso con bloqueo (@Lock PESSIMISTIC_WRITE) o validá y guardá dentro de la misma transacción.
3. El curso debe pertenecer al ciclo indicado y el alumno debe estar ACTIVO; si no, 400 o 409 con mensaje.
4. DELETE no borra la fila: marca la inscripción como anulada (activa = false) y libera el cupo.
5. Campos calculados del response según el contrato (por ejemplo nombres de alumno y curso); no los persistas.
6. Permisos según x-roles: crear y anular lo hacen ADMINISTRADOR y ADMINISTRATIVO; el alumno ve solo las suyas (comparar con el id del token); el docente ve las inscripciones de sus cursos asignados.

Tests obligatorios (además de 200 o 201, 400, 401, 403 y 404 por endpoint):
- Cupo lleno: 409; con una anulación previa, la siguiente inscripción entra.
- Segunda inscripción activa del mismo alumno en el mismo ciclo: 409; reinscripción tras anular: 201.
- Alumno INACTIVO: rechazado. Curso de otro ciclo: rechazado.
- ALUMNO intentando POST: 403; ALUMNO pidiendo inscripciones ajenas: 403 o lista vacía según el contrato.

Pasos comunes, en este orden: DTOs con los nombres exactos del contrato (campos calculados solo en response, Bean Validation en español); service con las reglas; controller con TODOS los endpoints del módulo (mismas rutas y métodos que endpoints.csv, arrays completos sin paginación, permisos por rol según x-roles: 401 sin sesión, 403 con rol no permitido, errores como ProblemDetail con message y errors); tests (service con Mockito, controller con @WebMvcTest).

Verificación (una vez): nix-shell --run "cd backend && ./gradlew test -q | tail -30"; la aplicación arranca con ddl-auto=validate contra la base con el esquema y los datos cargados (db-start y db-reset del nix-shell; si no hay PostgreSQL, decilo y seguí).
Cierre: actualizá la fila del módulo en docs/estado-del-proyecto.md (python3 tools/estado-proyecto.py regenera las cifras) y tildá la tarea en docs/pendientes.md. Sin modificar el frontend, listá qué pantallas del módulo ya se pueden probar con VITE_USE_MOCK=false y cualquier diferencia entre lo implementado y el contrato.
No inventes endpoints ni campos. No toques frontend/ ni database/ sin avisarlo en el resumen. Lo ambiguo va a docs/estado-del-proyecto.md como decisión abierta y seguí.
Entrega: resumen de máximo 6 líneas: endpoints implementados sobre el total del módulo, reglas cubiertas, tests y dudas abiertas.
