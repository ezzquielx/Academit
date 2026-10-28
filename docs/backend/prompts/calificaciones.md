Contexto: Academit, backend Spring Boot (Java 21, Gradle). Implementá el módulo «Evaluaciones y calificaciones» según el contrato del frontend.

Skills: si existen academit-backend-modulo, academit-reglas-negocio y academit-base-datos, cargalas; si no, seguí docs/backend/guia-de-modulos.md (secciones 2 y 3 y la ficha del módulo).
Rama: feature/back-calificaciones desde develop. Si dependés de un módulo que todavía no se integró, partí de la rama de ese módulo y decilo en el resumen. Commits según docs/flujo-de-trabajo.md, uno por punto, con "Refs: RF36, RF38, RF40, RN11, RN14, RN21". Sin push ni merge.

Ahorro de tokens: del contrato leé solo las filas del módulo con rg (docs/frontend/endpoints.csv, su entidad en docs/frontend/entidades.json y sus x-roles en docs/frontend/openapi.yaml); tomá como modelo de estilo los módulos de usuarios y auth, leídos por fragmentos; no pegues código en tu respuesta; tests UNA vez al final.

Endpoints del contrato (verificá con endpoints.csv):
- GET /api/evaluaciones
- POST /api/evaluaciones
- POST /api/evaluaciones/{idEvaluacion}/calificaciones
- GET /api/calificaciones
- POST /api/calificaciones

Depende de: asignaciones, inscripciones y cursos (usá los datos de prueba mientras tanto). Este módulo crea el componente reutilizable «docente asignado a la cátedra» que también usa asistencias: si ya existe, reutilizalo; si no, creálo en un lugar neutral (por ejemplo un servicio AsignacionDocenteService) y avisalo.

Reglas específicas:
1. Solo el docente asignado a la cátedra (curso_materia, con rol TITULAR, ADJUNTO o JTP) puede crear evaluaciones y cargar calificaciones de ese curso y materia (RN11). Verificalo comparando el docente del usuario del token con las asignaciones; si no está asignado, 403. ADMINISTRADOR según x-roles.
2. Nota: entero de 1 a 10 (RN14); fuera de rango, 400. Aprobado = nota >= 4 (RN21), calculado en el response, no persistido.
3. Una calificación por evaluación y alumno (restricción única): si ya existe, actualizala (upsert) y documentalo, o devolvé 409 si el contrato lo indica. El alumno debe estar inscripto en el curso, si no 400.
4. Evaluación: tipo PARCIAL o TRABAJO_PRACTICO y ponderación según el contrato (en la maqueta llega como texto, por ejemplo «40%»: respetá lo que diga entidades.json). Fecha obligatoria.
5. El alumno ve solo sus calificaciones; el docente, las de sus cátedras.

Tests obligatorios (además de 200 o 201, 400, 401, 403 y 404 por endpoint):
- Docente no asignado: 403 al crear evaluación y al cargar notas.
- Nota 0 y 11: 400. Nota 3: aprobado false. Nota 4: aprobado true.
- Alumno no inscripto en el curso: 400.
- ALUMNO haciendo POST: 403; ALUMNO consultando notas ajenas: 403 o vacío según contrato.

Decisiones a registrar si el análisis no las define:
- Cómo se calcula la nota final de una materia y cuándo está aprobada (lo necesitan situacion-academica y declaración jurada)

Pasos comunes, en este orden: DTOs con los nombres exactos del contrato (campos calculados solo en response, Bean Validation en español); service con las reglas; controller con TODOS los endpoints del módulo (mismas rutas y métodos que endpoints.csv, arrays completos sin paginación, permisos por rol según x-roles: 401 sin sesión, 403 con rol no permitido, errores como ProblemDetail con message y errors); tests (service con Mockito, controller con @WebMvcTest).

Verificación (una vez): nix-shell --run "cd backend && ./gradlew test -q | tail -30"; la aplicación arranca con ddl-auto=validate contra la base con el esquema y los datos cargados (db-start y db-reset del nix-shell; si no hay PostgreSQL, decilo y seguí).
Cierre: actualizá la fila del módulo en docs/estado-del-proyecto.md (python3 tools/estado-proyecto.py regenera las cifras) y tildá la tarea en docs/pendientes.md. Sin modificar el frontend, listá qué pantallas del módulo ya se pueden probar con VITE_USE_MOCK=false y cualquier diferencia entre lo implementado y el contrato.
No inventes endpoints ni campos. No toques frontend/ ni database/ sin avisarlo en el resumen. Lo ambiguo va a docs/estado-del-proyecto.md como decisión abierta y seguí.
Entrega: resumen de máximo 6 líneas: endpoints implementados sobre el total del módulo, reglas cubiertas, tests y dudas abiertas.
