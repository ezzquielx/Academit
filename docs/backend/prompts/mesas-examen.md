Contexto: Academit, backend Spring Boot (Java 21, Gradle). Implementá el módulo «Mesas de examen» según el contrato del frontend.

Skills: si existen academit-backend-modulo, academit-reglas-negocio y academit-base-datos, cargalas; si no, seguí docs/backend/guia-de-modulos.md (secciones 2 y 3 y la ficha del módulo).
Rama: feature/back-mesas-examen desde develop. Si dependés de un módulo que todavía no se integró, partí de la rama de ese módulo y decilo en el resumen. Commits según docs/flujo-de-trabajo.md, uno por punto, con "Refs:" y los RF y RN de mesas de examen del análisis (docs/entregables/entregable-1-analisis.md). Sin push ni merge.

Ahorro de tokens: del contrato leé solo las filas del módulo con rg (docs/frontend/endpoints.csv, su entidad en docs/frontend/entidades.json y sus x-roles en docs/frontend/openapi.yaml); tomá como modelo de estilo los módulos de usuarios y auth, leídos por fragmentos; no pegues código en tu respuesta; tests UNA vez al final.

Endpoints del contrato (verificá con endpoints.csv):
- GET /api/mesas-examen
- POST /api/mesas-examen
- PUT /api/mesas-examen/{id}
- POST /api/mesas-examen/{id}/inscripciones
- DELETE /api/mesas-examen/{id}/inscripciones/{idInscripcion}

Depende de: materias, alumnos y asignaciones (usá los datos de prueba mientras tanto).

Reglas específicas:
1. Mesa: materia, fecha, cupo, turno (MANANA, TARDE, NOCHE) y tribunal como texto, según entidades.json. Crear y editar según x-roles (normalmente ADMINISTRADOR).
2. Inscripción de un alumno: el alumno debe estar ACTIVO; una sola inscripción activa por alumno y mesa (409 si duplica; permitir reinscribir tras anular); cupo de la mesa (inscriptos activos < cupo, 409 si está llena); la mesa debe ser futura.
3. DELETE de inscripción: baja lógica (activa = false) que libera el cupo. El alumno solo anula la suya.
4. Si el análisis define un cierre de inscripción (en la maqueta se menciona 48 horas antes de la mesa) o exige que el alumno tenga regularizada la materia, aplicalo detrás de un método aislado; si no está definido, registralo como decisión abierta y no lo apliques.
5. Campos calculados del contrato (por ejemplo cupos disponibles y nombre de la materia) se calculan, no se persisten. El alumno ve las mesas y sus inscripciones.

Tests obligatorios (además de 200 o 201, 400, 401, 403 y 404 por endpoint):
- Mesa llena: 409; tras anular una inscripción, se puede inscribir otro alumno.
- Inscripción duplicada: 409. Mesa pasada: 400 o 409 con mensaje.
- Alumno intentando crear o editar una mesa: 403. Alumno anulando la inscripción de otro: 403.

Decisiones a registrar si el análisis no las define:
- Cierre de inscripción a mesas
- Requisito de regularidad para rendir

Pasos comunes, en este orden: DTOs con los nombres exactos del contrato (campos calculados solo en response, Bean Validation en español); service con las reglas; controller con TODOS los endpoints del módulo (mismas rutas y métodos que endpoints.csv, arrays completos sin paginación, permisos por rol según x-roles: 401 sin sesión, 403 con rol no permitido, errores como ProblemDetail con message y errors); tests (service con Mockito, controller con @WebMvcTest).

Verificación (una vez): nix-shell --run "cd backend && ./gradlew test -q | tail -30"; la aplicación arranca con ddl-auto=validate contra la base con el esquema y los datos cargados (db-start y db-reset del nix-shell; si no hay PostgreSQL, decilo y seguí).
Cierre: actualizá la fila del módulo en docs/estado-del-proyecto.md (python3 tools/estado-proyecto.py regenera las cifras) y tildá la tarea en docs/pendientes.md. Sin modificar el frontend, listá qué pantallas del módulo ya se pueden probar con VITE_USE_MOCK=false y cualquier diferencia entre lo implementado y el contrato.
No inventes endpoints ni campos. No toques frontend/ ni database/ sin avisarlo en el resumen. Lo ambiguo va a docs/estado-del-proyecto.md como decisión abierta y seguí.
Entrega: resumen de máximo 6 líneas: endpoints implementados sobre el total del módulo, reglas cubiertas, tests y dudas abiertas.
