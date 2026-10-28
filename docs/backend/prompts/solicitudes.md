Contexto: Academit, backend Spring Boot (Java 21, Gradle). Implementá el módulo «solicitudes» según el contrato del frontend.

Skills: si existen academit-backend-modulo, academit-reglas-negocio y academit-base-datos, cargalas; si no, seguí docs/backend/guia-de-modulos.md (secciones 2 y 3 y la ficha del módulo).
Rama: feature/back-solicitudes desde develop. Si dependés de un módulo que todavía no se integró, partí de la rama de ese módulo y decilo en el resumen. Commits según docs/flujo-de-trabajo.md, uno por punto, con "Refs: RF56, RF57". Sin push ni merge.

Ahorro de tokens: del contrato leé solo las filas del módulo con rg (docs/frontend/endpoints.csv, su entidad en docs/frontend/entidades.json y sus x-roles en docs/frontend/openapi.yaml); tomá como modelo de estilo los módulos de usuarios y auth, leídos por fragmentos; no pegues código en tu respuesta; tests UNA vez al final.

Endpoints del contrato (verificá con endpoints.csv):
- GET /api/solicitudes
- POST /api/solicitudes
- PATCH /api/solicitudes/{id}

Depende de: alumnos

Reglas específicas:
- Tipos y estados EXACTOS de docs/frontend/entidades.json: no inventar códigos ni valores.
- El alumno crea solicitudes propias y consulta solo las suyas.
- PATCH /api/solicitudes/{id} resuelve (estado, respuesta y fechaResolucion obligatorios al resolver) y es de ADMINISTRATIVO o ADMINISTRADOR.
- La declaración jurada NO es un tipo de solicitud (módulo propio en openapi.yaml).
- El efecto de resolver un cambio de carrera sobre el plan del alumno: si el análisis no lo define, dejalo como decisión abierta en docs/estado-del-proyecto.md y seguí.

Tests obligatorios: por endpoint, 200 o 201, 400 por validación, 401 sin sesión, 403 por rol y 404; y un test de service por cada regla que lance excepción.

Pasos comunes, en este orden: DTOs con los nombres exactos del contrato (campos calculados solo en response, Bean Validation en español); service con las reglas; controller con TODOS los endpoints del módulo (mismas rutas y métodos que endpoints.csv, arrays completos sin paginación, permisos por rol según x-roles: 401 sin sesión, 403 con rol no permitido, errores como ProblemDetail con message y errors); tests (service con Mockito, controller con @WebMvcTest).

Verificación (una vez): nix-shell --run "cd backend && ./gradlew test -q | tail -30"; la aplicación arranca con ddl-auto=validate contra la base con el esquema y los datos cargados (db-start y db-reset del nix-shell; si no hay PostgreSQL, decilo y seguí).
Cierre: actualizá la fila del módulo en docs/estado-del-proyecto.md (python3 tools/estado-proyecto.py regenera las cifras) y tildá la tarea en docs/pendientes.md. Sin modificar el frontend, listá qué pantallas del módulo ya se pueden probar con VITE_USE_MOCK=false y cualquier diferencia entre lo implementado y el contrato.
No inventes endpoints ni campos. No toques frontend/ ni database/ sin avisarlo en el resumen. Lo ambiguo va a docs/estado-del-proyecto.md como decisión abierta y seguí.
Entrega: resumen de máximo 6 líneas: endpoints implementados sobre el total del módulo, reglas cubiertas, tests y dudas abiertas.
