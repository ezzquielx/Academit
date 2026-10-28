Contexto: Academit, backend Spring Boot (Java 21, Gradle). Implementá el módulo «planes-estudio y materias» según el contrato del frontend.

Skills: si existen academit-backend-modulo, academit-reglas-negocio y academit-base-datos, cargalas; si no, seguí docs/backend/guia-de-modulos.md (secciones 2 y 3 y la ficha del módulo).
Rama: feature/back-planes-materias desde develop. Si dependés de un módulo que todavía no se integró, partí de la rama de ese módulo y decilo en el resumen. Commits según docs/flujo-de-trabajo.md, uno por punto, con "Refs: RF47, RF48, RN24, RN22". Sin push ni merge.

Ahorro de tokens: del contrato leé solo las filas del módulo con rg (docs/frontend/endpoints.csv, su entidad en docs/frontend/entidades.json y sus x-roles en docs/frontend/openapi.yaml); tomá como modelo de estilo los módulos de usuarios y auth, leídos por fragmentos; no pegues código en tu respuesta; tests UNA vez al final.

Endpoints del contrato (verificá con endpoints.csv):
- GET /api/planes-estudio
- GET /api/planes-estudio/{id}/materias
- GET /api/materias
- POST /api/materias
- PUT /api/materias/{id}

Depende de: carreras

Reglas específicas:
- Módulo doble: listados del plan (endpoints del contrato más arriba) y alta/edición de materias con POST y PUT /api/materias.
- GET /api/planes-estudio/{id}/materias devuelve las materias del plan con anioPlan y sus correlativas.
- Nombre de materia único; baja lógica (activa=false) si tiene historial: nunca DELETE físico.
- Una correlativa no puede apuntar a la misma materia y solo puede vincular materias del mismo plan (RN24).
- Las correlativas no forman un ciclo (circularidad no permitida).
- El plan pertenece a una única carrera (RN22).

Tests obligatorios: por endpoint, 200 o 201, 400 por validación, 401 sin sesión, 403 por rol y 404; y un test de service por cada regla que lance excepción.

Pasos comunes, en este orden: DTOs con los nombres exactos del contrato (campos calculados solo en response, Bean Validation en español); service con las reglas; controller con TODOS los endpoints del módulo (mismas rutas y métodos que endpoints.csv, arrays completos sin paginación, permisos por rol según x-roles: 401 sin sesión, 403 con rol no permitido, errores como ProblemDetail con message y errors); tests (service con Mockito, controller con @WebMvcTest).

Verificación (una vez): nix-shell --run "cd backend && ./gradlew test -q | tail -30"; la aplicación arranca con ddl-auto=validate contra la base con el esquema y los datos cargados (db-start y db-reset del nix-shell; si no hay PostgreSQL, decilo y seguí).
Cierre: actualizá la fila del módulo en docs/estado-del-proyecto.md (python3 tools/estado-proyecto.py regenera las cifras) y tildá la tarea en docs/pendientes.md. Sin modificar el frontend, listá qué pantallas del módulo ya se pueden probar con VITE_USE_MOCK=false y cualquier diferencia entre lo implementado y el contrato.
No inventes endpoints ni campos. No toques frontend/ ni database/ sin avisarlo en el resumen. Lo ambiguo va a docs/estado-del-proyecto.md como decisión abierta y seguí.
Entrega: resumen de máximo 6 líneas: endpoints implementados sobre el total del módulo, reglas cubiertas, tests y dudas abiertas.
