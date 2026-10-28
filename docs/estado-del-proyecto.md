# Estado Real del Proyecto y Plan de Distribución - Academit

> **Fecha de relevamiento:** Octubre 2026 | **Entidades:** 35 | **Repositories:** 32 | **Tests backend:** 120/120 OK.

## 1. Estado por Módulo

| Módulo | Entidad y Repository | Service | Controller | Tests | Estado |
| --- | --- | --- | --- | --- | --- |
| **Base de datos** | 25 tablas alineadas | N/A | N/A | Scripts SQL verificados | **Hecho** |
| **Entidades JPA** | 35 entidades, 32 repositorios | N/A | N/A | Compilación JPA OK | **Hecho** |
| **Seguridad y Usuarios** | `Usuario`, `Rol` / Repos | `AuthService`, `UsuarioService` | `Auth`, `Perfil`, `Usuario` | 46 tests unit/integ | **Hecho** |
| **Alumnos** | `Alumno`, `EstadoPersona` / Repos | Pendiente | Pendiente | Pendiente | **Parcial** |
| **Docentes** | `Docente` / `DocenteRepository` | Pendiente | Pendiente | Pendiente | **Parcial** |
| **Carreras** | `Carrera` / `CarreraRepository` | Pendiente | Pendiente | Pendiente | **Parcial** |
| **Materias y Planes** | `Materia`, `PlanEstudio`, `Correlativa` | Pendiente | Pendiente | Pendiente | **Parcial** |
| **Cursos y Ciclos** | `Curso`, `CicloLectivo` / Repos | `CicloLectivoService` hecho; Cursos pendiente | `CicloLectivoController` hecho; Cursos pendiente | 15 tests del módulo ciclos | **Parcial** |
| **Asignaciones** | `CursoMateria` / Repo | Pendiente | Pendiente | Pendiente | **Parcial** |
| **Inscripciones** | `Inscripcion` / Repo | Pendiente | Pendiente | Pendiente | **Parcial** |
| **Matrículas y Cuotas** | `Matricula`, `Cuota`, `EstadoCuota` | `MatriculaService` y `CuotaService` hechos | `MatriculaController` y `CuotaController` hechos | 54 tests de matrículas y cuotas | **Hecho** |
| **Pagos** | `Pago`, `MedioPago` / Repos | `PagoService` hecho | `PagoController` hecho | 24 tests de pagos | **Hecho** |
| **Evaluaciones y Calificaciones** | `Evaluacion`, `Calificacion` / Repos | Pendiente | Pendiente | Pendiente | **Parcial** |
| **Asistencia** | `Asistencia`, `EstadoAsistencia` | Pendiente | Pendiente | Pendiente | **Parcial** |
| **Mesas de Examen** | `MesaExamen`, `InscripcionMesa` | Pendiente | Pendiente | Pendiente | **Parcial** |
| **Declaración Jurada** | `DeclaracionJurada`, `DeclaracionMateria` | Pendiente | Pendiente | Pendiente | **Parcial** |
| **Solicitudes** | `Solicitud`, `TipoSolicitud` | Pendiente | Pendiente | Pendiente | **Parcial** |
| **Documentación** | `Documentacion`, `TipoDocumento` | Pendiente | Pendiente | Pendiente | **Parcial** |
| **Cambio de Carrera** | Gestionado vía `Solicitud` + `Plan` | Pendiente | Pendiente | Pendiente | **Pendiente** |

## 2. Endpoints del Contrato vs Implementados

- **Total en contrato frontend:** 71 endpoints (68 de negocio + 3 de autenticación inicial).
- **Implementados en backend:** 22 endpoints (20 del contrato + `GET /api/usuarios/{id}` y `GET /api/auth/me`). Cobertura: **28.2 %**.
- **Pendientes de implementar:** 51 endpoints.

### Endpoints implementados (20 del contrato)
- `POST /api/auth/login`, `GET /api/auth/me`, `POST /api/auth/logout`, `POST /api/auth/cambiar-password`
- `GET /api/usuarios`, `GET /api/usuarios/{id}`, `POST /api/usuarios`, `PUT /api/usuarios/{id}`, `DELETE /api/usuarios/{id}`
- `GET /api/usuarios/me`, `PUT /api/usuarios/me`
- `GET /api/ciclos-lectivos`, `POST /api/ciclos-lectivos`, `PUT /api/ciclos-lectivos/{id}`
- `GET /api/matriculas`, `PUT /api/matriculas/{id}`, `PATCH /api/matriculas/{id}`, `POST /api/matriculas/{id}/cuotas`
- `GET /api/cuotas`, `PATCH /api/cuotas/{id}/anular`
- `GET /api/pagos`, `POST /api/pagos`

### Endpoints faltantes (51) por módulo
- **alumnos (7):** `GET /api/alumnos`, `GET /api/alumnos/{id}`, `POST /api/alumnos`, `PUT /api/alumnos/{id}`, `DELETE /api/alumnos/{id}`, `GET /api/alumnos/{id}/situacion-academica`, `GET /api/alumnos/{id}/situacion-administrativa`
- **asignaciones (3):** `GET /api/asignaciones`, `POST /api/asignaciones`, `DELETE /api/asignaciones/{id}`
- **asistencias (2):** `GET /api/asistencias`, `POST /api/asistencias/lote`
- **calificaciones (2):** `GET /api/calificaciones`, `POST /api/calificaciones`
- **carreras (7):** `GET /api/carreras`, `GET /api/carreras/{id}`, `POST /api/carreras`, `PUT /api/carreras/{id}`, `DELETE /api/carreras/{id}`, `GET /api/carreras/{id}/planes`, `GET /api/carreras/{id}/cursos`
- **cursos (3):** `GET /api/cursos`, `POST /api/cursos`, `PUT /api/cursos/{id}`
- **declaraciones-juradas (2):** `GET /api/declaraciones-juradas`, `POST /api/declaraciones-juradas`
- **docentes (4):** `GET /api/docentes`, `POST /api/docentes`, `PUT /api/docentes/{id}`, `DELETE /api/docentes/{id}`
- **documentacion (2):** `GET /api/documentacion`, `PUT /api/documentacion/{id}`
- **evaluaciones (3):** `GET /api/evaluaciones`, `POST /api/evaluaciones`, `POST /api/evaluaciones/{idEvaluacion}/calificaciones`
- **inscripciones (3):** `GET /api/inscripciones`, `POST /api/inscripciones`, `DELETE /api/inscripciones/{id}`
- **materias (3):** `GET /api/materias`, `POST /api/materias`, `PUT /api/materias/{id}`
- **mesas-examen (5):** `GET /api/mesas-examen`, `POST /api/mesas-examen/{id}/inscripciones`, `DELETE /api/mesas-examen/{id}/inscripciones/{idInscripcion}`, `POST /api/mesas-examen`, `PUT /api/mesas-examen/{id}`
- **planes-estudio (2):** `GET /api/planes-estudio`, `GET /api/planes-estudio/{id}/materias`
- **solicitudes (3):** `GET /api/solicitudes`, `POST /api/solicitudes`, `PATCH /api/solicitudes/{id}`

## 3. Reglas de Negocio en Backend y su Estado

1. **Cupo del Curso (RN03 - RF14, RF22):** Validar `inscriptos < cupo` antes de confirmar inscripción. *Estado: Pendiente (columna existe en BD; falta validación en Service).*
2. **Inscripción Única por Ciclo (RN02 - RF22):** Un alumno solo puede registrar una inscripción activa por ciclo lectivo. *Estado: Parcial (restringido en BD por UK; falta validación amigable en Service).*
3. **Generación de 12 Cuotas y 3 Seguros (RN18 - RF28, RF46):** Generación automática de 12 cuotas mensuales regulares y 3 seguros cuatrimestrales por matrícula activa. *Estado: Hecho en `feature/back-matriculas` (idempotente, 409 si ya existen o la matrícula no está ACTIVA).*
4. **Pago Completo de Cuota (RN09 - RF32):** Rechazar pagos parciales; el importe debe cancelar la cuota completa y pasar su estado a PAGADA. *Estado: Hecho en `feature/back-pagos` (400 si el importe no coincide, la cuota queda PAGADA en la misma transacción).*
5. **Marcado de Cuotas Vencidas (RN07 - RF30):** Marcar cuotas impagas como VENCIDAS al superar la fecha límite. *Estado: Parcial (el response de cuotas ya expone VENCIDA de forma dinámica según la fecha; falta la tarea programada que persista el cambio en `feature/back-cuotas-vencidas`).*
6. **Calificaciones 1 a 10 con Aprobado >= 4 (RN14, RN21 - RF38, RF40):** Restricción de escala y condición de aprobación. *Estado: Parcial (CHECK 1-10 en BD; falta regla de aprobación en Service).*
7. **Carga Exclusiva por Docente Asignado (RN11, RN12 - RF36, RF38, RF41):** Solo el docente vinculado a la cátedra (`curso_materia`) puede registrar notas y asistencia. *Estado: Pendiente.*
8. **Permisos y Control de Acceso por Rol (RN17 - RF05):** Respuestas 401 y 403 estandarizadas en ProblemDetail. *Estado: Hecho en SecurityConfig y Usuarios; Pendiente de extender a nuevos controllers.*

## 4. Decisiones Tomadas por el Grupo

1. **Estado de Alumno y de Docente:** ACTIVO e INACTIVO (la baja lógica pasa de BAJA a INACTIVO). Se elimina REGULAR como estado: "regular" es una condición por materia, calculada con asistencia (mínimo 75 %) y notas, y no se guarda.
   - *Motivo:* Unifica el ciclo de vida de personas en el padrón y evita persistir una condición académica volátil que depende de cada cursada.
2. **Matricula.estadoPago no se guarda:** se calcula al devolver la matrícula a partir de sus cuotas. EN_MORA si hay al menos una cuota VENCIDA; AL_DIA si todas las cuotas ya vencidas (o todas) están PAGADA; PENDIENTE en el resto. Matricula.estado (PENDIENTE, ACTIVA, CANCELADA) sí se guarda; al cancelar una matrícula sus cuotas pendientes pasan a ANULADA.
   - *Motivo:* Elimina inconsistencias entre la cuenta corriente de cuotas y la cabecera de matrícula.
3. **Sin paginación en el servidor por ahora:** los listados devuelven arrays completos. Si hiciera falta, se agregan ?page y ?size opcionales y un header X-Total-Count sin cambiar la forma del array.
   - *Motivo:* Simplifica el desarrollo inicial con el volumen de datos actual y permite filtrado/ordenamiento ágil en cliente.
4. **Pago.comprobante:** columna única con formato AAAA-NNNNNN (año y número correlativo de 6 dígitos), generada por el backend al registrar el pago a partir del id_pago (ejemplo 2026-000123); el frontend solo la muestra.
   - *Motivo:* Garantiza numeración oficial única, auditable y correlativa administrada exclusivamente por el servidor.

### Estado de Aplicación de Decisiones
- **Ya aplicado e integrado en `develop`:**
  - 4a (Estados de Personas): Base de datos (`01-schema.sql` y `02-datos-iniciales.sql`), backend (`Alumno`, `Docente`), contrato (`entidades.json`, `openapi.yaml`) y frontend (`mocks/data.js` y vistas). Resuelto en `refactor/aplicar-decisiones-estado`, integrado en `develop`.
  - 4b (Matrícula y Estado de Pago): Backend (`MatriculaResponseDTO`, `MatriculaService`), contrato (`AL_DIA`, `EN_MORA`, `PENDIENTE` calculado) y frontend (`AdminMatriculasPage`, mocks). Resuelto en `refactor/aplicar-decisiones-estado`, integrado en `develop`.
  - 4c (Colecciones Completas): Controllers implementados devuelven colecciones completas; frontend preparado para arrays directos. Resuelto en `fix/back-contrato-errores-auth`, integrado en `develop`.
  - 4d (Comprobante de Pago): Columna `pago.comprobante VARCHAR(11)` en BD, DTO y generador AAAA-NNNNNN en backend, contrato y pantallas. Resuelto en `refactor/aplicar-decisiones-estado`, integrado en `develop`.

### Decisiones Abiertas de los Módulos Nuevos

Registro de criterios que el análisis no define. Se implementaron como están descriptos y siguen confirmables:

- **Matrículas - importes de cuotas y seguros (RF28, RN18):** si `matricula.importe` está definido, cada cuota mensual es `importe / 12` (2 decimales, redondeo HALF_UP) y cada seguro equivale a un cuarto de la mensual; sin importe se usan **$12000,00** y **$3000,00** (proporción de los datos de prueba).
- **Matrículas - fechas de vencimiento:** mensuales el día **10 de enero a diciembre** del año del ciclo lectivo; seguros el día 10 de **marzo, julio y noviembre** del mismo año (coincide con el mock del frontend; los datos de prueba generan las mensuales a partir del mes de inicio del ciclo).
- **Matrículas - PENDIENTE a ACTIVA:** único punto de cambio `PUT /api/matriculas/{id}`, permitido a ADMINISTRATIVO y ADMINISTRADOR. El contrato no incluye `POST /api/matriculas`, por lo que no hay alta de matrículas desde la API.
- **Matrículas - carrera de la matrícula:** la tabla `matricula` no tiene `id_carrera`; `idCarrera` y `carreraNombre` se derivan del plan de estudios del alumno.
- **Cuotas - campos sin columna en la base:** `concepto`, `mes`, `anio` y la fecha de pago se calculan en el response (`cuota` solo guarda número, tipo, vencimiento e importe) y `tipo` se expone como `MENSUAL` aunque el catálogo de la base guarda `CUOTA_MENSUAL`.
- **Pagos - medio de pago:** el contrato enumera `TARJETA_CREDITO` y `TARJETA_DEBITO` pero el catálogo de la base guarda `TARJETA` (y el frontend envía `TARJETA`); se aceptan ambos y se resuelve por nombre con ese fallback. `medioPago` opcional: sin valor se registra `EFECTIVO`.
- **Pagos - estado del pago:** la tabla `pago` no tiene columna `estado`; todo pago registrado queda `ACREDITADO` en el response (`PENDIENTE`/`RECHAZADO` quedan reservados para un flujo futuro).

### Estado de Desajustes de Base de Datos (`docs/estado/desajustes-db.md`)

- Tablas `solicitud` y `documentacion`: Creadas en BD y mapeadas en JPA. Resuelto en `feature/db-alinear-contrato`, integrado en `develop`.
- Asignación con roles docentes: Columna `rol_docente` y unicidad `(id_curso, id_materia, id_docente)`. Resuelto en `feature/db-alinear-contrato`, integrado en `develop`.
- Asistencia por materia: Columna `id_materia` y unicidad compuesta. Resuelto en `feature/db-alinear-contrato`, integrado en `develop`.
- Turno en curso y mesa: Columnas `turno` y `tribunal`. Resuelto en `feature/db-alinear-contrato`, integrado en `develop`.
- Tipos de cuota: `CUOTA_MENSUAL` y `SEGURO_CUATRIMESTRAL`. Resuelto en `feature/db-alinear-contrato`, integrado en `develop`.

## 5. Trabajo Restante y Reparto por Ramas

1. **Fase Previa - Integración de Esquema, Contrato y Decisiones:**
   - **Estado:** Completada e integrada en `develop`. Comprende `fix/back-contrato-errores-auth`, `feature/db-alinear-contrato`, `refactor/aplicar-decisiones-estado` y `docs/ordenar-documentacion`.
2. **Fase 1 - Maestros Base (Trabajo en PARALELO):**
   - `feature/back-alumnos`: Service, Controller y Tests para `/api/alumnos` (CRUD y situaciones).
   - `feature/back-docentes`: Service, Controller y Tests para `/api/docentes` (CRUD).
   - `feature/back-carreras-planes`: Controller y Service para `/api/carreras`, `/api/materias` y `/api/planes-estudio`.
   - `feature/back-cursos-ciclos`: Controller y Service para `/api/cursos` y `/api/ciclos-lectivos`.
3. **Fase 2 - Asignaciones e Inscripciones (Depende de Fase 1, en PARALELO):**
   - `feature/back-asignaciones`: Controller y Service para `/api/asignaciones` con roles docentes.
   - `feature/back-inscripciones`: Controller y Service para `/api/inscripciones` con validación de cupo (RN03) e inscripción única (RN02).
4. **Fase 3 - Aranceles y Pagos (Depende de Fase 1 y 2):**
   - `feature/back-matriculas` (hecha): `/api/matriculas`, generación de 12+3 cuotas (RN18) y `estadoPago` calculado (4b).
   - `feature/back-cuotas` (hecha): `/api/cuotas`, listado enriquecido con `idAlumno`/`alumnoNombre`/`fechaPago` y anulación de cuotas (409 si está pagada).
   - `feature/back-pagos` (hecha): `/api/pagos`, validación de pago completo (RN09) y comprobante AAAA-NNNNNN (4d).
5. **Fase 4 - Académico (Depende de Fase 1 y 2, en PARALELO):**
   - `feature/back-asistencias`: `/api/asistencias`, control de docente asignado (RN12).
   - `feature/back-calificaciones`: `/api/evaluaciones` y `/api/calificaciones`, escala 1-10 y docente asignado (RN11, RN14).
   - `feature/back-mesas-examen`: `/api/mesas-examen` e inscripciones a examen.
   - `feature/back-declaraciones-juradas`: `/api/declaraciones-juradas` con verificación de correlativas.
6. **Fase 5 - Trámites y Mantenimiento (PARALELO):**
   - `feature/back-solicitudes-documentacion`: `/api/solicitudes` y `/api/documentacion`.
   - `feature/back-cuotas-vencidas`: tarea programada para marcado de cuotas vencidas (RN07, RF30).

## 6. Checklist de Integración Frontend vs Backend

- [ ] Configurar `frontend/.env` con `VITE_USE_MOCK=false` y proxy Vite apuntando a `http://localhost:8080`.
- [x] `Login.jsx` (`POST /api/auth/login`): recepción y almacenamiento de JWT, redirección por rol.
- [x] `Layout.jsx` / `Perfil`: `GET /api/auth/me`, `GET /api/usuarios/me`, `POST /api/auth/logout`.
- [x] `GestionUsuariosPage.jsx`: CRUD de usuarios con roles y validaciones.
- [ ] `GestionAlumnosPage.jsx`: listado, filtros, alta/edición y fichas de situación académica/administrativa.
- [ ] `GestionDocentesPage.jsx`: gestión de docentes y visualización de asignaciones.
- [ ] `GestionCarrerasPage.jsx` / `GestionPlanEstudiosPage.jsx`: carreras, materias y malla curricular con correlativas.
- [ ] `GestionCursosPage.jsx` / `GestionAsignacionesPage.jsx`: comisiones, ciclos y asignación docente.
- [ ] `GestionInscripcionesPage.jsx`: inscripción con control de cupo y rechazo de duplicados.
- [ ] `AdminMatriculasPage.jsx` / `AdminCuotasPage.jsx`: alta de matrícula, generación de cuotas y cálculo de mora.
- [ ] `AdminPagosPage.jsx`: cobro de cuota completa y emisión de comprobante AAAA-NNNNNN.
- [ ] `CalificacionesDocentePage.jsx`: registro de notas (1-10) por docente asignado.
- [ ] `AsistenciasDocentePage.jsx`: toma de asistencia por materia.
- [ ] `MesasExamenPage.jsx`: mesas y actas de examen.
- [ ] `DeclaracionJuradaPage.jsx`: presentación anual de materias a cursar.
- [ ] `BandejaSolicitudesPage.jsx` / `DocumentacionPage.jsx`: trámites y legajos digitales.
- [ ] Validación OpenAPI: verificación de schemas JSON con `docs/frontend/openapi.yaml`.

## 7. Documentación Pendiente de la Entrega

1. **Portada Institucional:** Integrantes del grupo, profesor/a, ayudante, asignatura y fecha de entrega.
2. **Análisis de Requisitos (`docs/entregables/entregable-1-analisis.md`):** Incorporar RF y RN formalizados para Solicitudes y Documentación.
3. **Diagramas UML y DER (`docs/entregables/entregable-2/`):**
   - DER actualizado: incorporar tablas `solicitud`, `documentacion`, `rol_docente`, `asistencia.id_materia`, `turno` y `pago.comprobante`.
   - Diagrama de Clases: actualizar con nuevas entidades y estado INACTIVO.
   - Diagramas de Secuencia: actualizar inscripción, pago con comprobante, calificación y asistencia.
4. **Modelo Relacional y Diccionario de Datos:** Documentar tipos de datos, PK, FK y restricciones CHECK actualizadas.
5. **Revisión del Enunciado:** Revisar enunciado de la cátedra para verificar cobertura de todos los puntos solicitados.
