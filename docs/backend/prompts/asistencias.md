Contexto: Academit, backend Spring Boot (Java 21, Gradle). Implementá el módulo «Asistencias» según el contrato del frontend.

Skills: si existen academit-backend-modulo, academit-reglas-negocio y academit-base-datos, cargalas; si no, seguí docs/backend/guia-de-modulos.md (secciones 2 y 3 y la ficha del módulo).
Rama: feature/back-asistencias desde develop. Si dependés de un módulo que todavía no se integró, partí de la rama de ese módulo y decilo en el resumen. Commits según docs/flujo-de-trabajo.md, uno por punto, con "Refs: RF41, RN12". Sin push ni merge.

Ahorro de tokens: del contrato leé solo las filas del módulo con rg (docs/frontend/endpoints.csv, su entidad en docs/frontend/entidades.json y sus x-roles en docs/frontend/openapi.yaml); tomá como modelo de estilo los módulos de usuarios y auth, leídos por fragmentos; no pegues código en tu respuesta; tests UNA vez al final.

Endpoints del contrato (verificá con endpoints.csv):
- GET /api/asistencias
- POST /api/asistencias/lote

Depende de: asignaciones e inscripciones. Reutilizá el componente «docente asignado a la cátedra» del módulo calificaciones; si todavía no está integrado, coordinalo con quien lo hace o creálo igual con el mismo nombre y avisalo.

Reglas específicas:
1. La asistencia se registra por alumno, curso, materia y fecha (clave única de los cuatro). El lote recibe un arreglo de registros del mismo curso, materia y fecha.
2. Solo el docente asignado a esa cátedra (curso_materia) registra asistencia (RN12); si no, 403. ADMINISTRADOR según x-roles.
3. Estados válidos: PRESENTE, AUSENTE, TARDANZA y JUSTIFICADO. La fecha no puede ser futura. Cada alumno del lote debe estar inscripto en el curso.
4. Si ya existe un registro para el mismo alumno, curso, materia y fecha, actualizalo (upsert) y documentalo. El lote es atómico: si un ítem es inválido, 400 con errors por posición y no se guarda nada.
5. Exponé un método reutilizable que calcule el porcentaje de asistencia por alumno y materia y la condición REGULAR (asistencia >= 75 % y sin notas desaprobadas sin recuperar). Lo va a usar GET /api/alumnos/{id}/situacion-academica. La condición se calcula, nunca se guarda.
6. El alumno consulta solo la suya; el docente, las de sus cátedras.

Tests obligatorios (además de 200 o 201, 400, 401, 403 y 404 por endpoint):
- Docente no asignado: 403. Fecha futura: 400. Estado inválido: 400.
- Lote con un alumno no inscripto: 400 y nada guardado.
- Registrar dos veces el mismo alumno, curso, materia y fecha: queda un solo registro.
- Cálculo del porcentaje: 3 presentes de 4 clases = 75 % → regular; 2 de 4 = 50 % → no regular.

Pasos comunes, en este orden: DTOs con los nombres exactos del contrato (campos calculados solo en response, Bean Validation en español); service con las reglas; controller con TODOS los endpoints del módulo (mismas rutas y métodos que endpoints.csv, arrays completos sin paginación, permisos por rol según x-roles: 401 sin sesión, 403 con rol no permitido, errores como ProblemDetail con message y errors); tests (service con Mockito, controller con @WebMvcTest).

Verificación (una vez): nix-shell --run "cd backend && ./gradlew test -q | tail -30"; la aplicación arranca con ddl-auto=validate contra la base con el esquema y los datos cargados (db-start y db-reset del nix-shell; si no hay PostgreSQL, decilo y seguí).
Cierre: actualizá la fila del módulo en docs/estado-del-proyecto.md (python3 tools/estado-proyecto.py regenera las cifras) y tildá la tarea en docs/pendientes.md. Sin modificar el frontend, listá qué pantallas del módulo ya se pueden probar con VITE_USE_MOCK=false y cualquier diferencia entre lo implementado y el contrato.
No inventes endpoints ni campos. No toques frontend/ ni database/ sin avisarlo en el resumen. Lo ambiguo va a docs/estado-del-proyecto.md como decisión abierta y seguí.
Entrega: resumen de máximo 6 líneas: endpoints implementados sobre el total del módulo, reglas cubiertas, tests y dudas abiertas.
