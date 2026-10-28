Contexto: Academit, backend Spring Boot (Java 21, Gradle). Implementá el módulo «Declaraciones juradas» según el contrato del frontend.

Skills: si existen academit-backend-modulo, academit-reglas-negocio y academit-base-datos, cargalas; si no, seguí docs/backend/guia-de-modulos.md (secciones 2 y 3 y la ficha del módulo).
Rama: feature/back-declaraciones-juradas desde develop. Si dependés de un módulo que todavía no se integró, partí de la rama de ese módulo y decilo en el resumen. Commits según docs/flujo-de-trabajo.md, uno por punto, con "Refs:" y los RF y RN de declaración jurada del análisis (docs/entregables/entregable-1-analisis.md). Sin push ni merge.

Ahorro de tokens: del contrato leé solo las filas del módulo con rg (docs/frontend/endpoints.csv, su entidad en docs/frontend/entidades.json y sus x-roles en docs/frontend/openapi.yaml); tomá como modelo de estilo los módulos de usuarios y auth, leídos por fragmentos; no pegues código en tu respuesta; tests UNA vez al final.

Endpoints del contrato (verificá con endpoints.csv):
- GET /api/declaraciones-juradas
- POST /api/declaraciones-juradas

Depende de: alumnos, planes de estudio, materias y calificaciones (para verificar correlativas).

Reglas específicas:
1. La declaración jurada es la presentación de las materias que el alumno cursará en un ciclo: tiene alumno, ciclo, fecha y una lista de materias. Es su propia entidad; no es un tipo de Solicitud.
2. Una declaración por alumno y ciclo: si ya existe, 409 (o reemplazo, solo si el análisis lo dice).
3. Cada materia debe pertenecer al plan de estudios del alumno y estar activa; si no, 400 con errors por materia.
4. Correlativas: para cada materia, todas sus correlativas (tabla correlativa del plan) deben estar aprobadas por el alumno. Aislá la definición de «materia aprobada» detrás de un único método. Si el análisis no la define, implementá la regla mínima (el alumno tiene notas cargadas en la materia y todas son >= 4) y registrá la decisión abierta.
5. El ALUMNO presenta solo la suya (comparar con el id del token); ADMINISTRADOR y ADMINISTRATIVO consultan según x-roles.

Tests obligatorios (además de 200 o 201, 400, 401, 403 y 404 por endpoint):
- Materia fuera del plan: 400. Materia con correlativa no aprobada: 400 con la materia indicada.
- Segunda declaración del mismo ciclo: 409.
- Alumno presentando por otro: 403. Declaración válida: 201 con las materias guardadas.

Decisiones a registrar si el análisis no las define:
- Definición de «materia aprobada»
- Si se permite reemplazar la declaración dentro del ciclo

Pasos comunes, en este orden: DTOs con los nombres exactos del contrato (campos calculados solo en response, Bean Validation en español); service con las reglas; controller con TODOS los endpoints del módulo (mismas rutas y métodos que endpoints.csv, arrays completos sin paginación, permisos por rol según x-roles: 401 sin sesión, 403 con rol no permitido, errores como ProblemDetail con message y errors); tests (service con Mockito, controller con @WebMvcTest).

Verificación (una vez): nix-shell --run "cd backend && ./gradlew test -q | tail -30"; la aplicación arranca con ddl-auto=validate contra la base con el esquema y los datos cargados (db-start y db-reset del nix-shell; si no hay PostgreSQL, decilo y seguí).
Cierre: actualizá la fila del módulo en docs/estado-del-proyecto.md (python3 tools/estado-proyecto.py regenera las cifras) y tildá la tarea en docs/pendientes.md. Sin modificar el frontend, listá qué pantallas del módulo ya se pueden probar con VITE_USE_MOCK=false y cualquier diferencia entre lo implementado y el contrato.
No inventes endpoints ni campos. No toques frontend/ ni database/ sin avisarlo en el resumen. Lo ambiguo va a docs/estado-del-proyecto.md como decisión abierta y seguí.
Entrega: resumen de máximo 6 líneas: endpoints implementados sobre el total del módulo, reglas cubiertas, tests y dudas abiertas.
