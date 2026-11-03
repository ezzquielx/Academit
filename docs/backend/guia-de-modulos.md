# Guía de Módulos del Backend - Academit

> Referencia técnica oficial para el desarrollo e implementación de la API REST de **Academit**.

## 1. Cómo Usar Este Documento

Cada integrante toma un módulo según el orden de dependencias y crea su rama de trabajo. **Cada módulo ya tiene un prompt completo en [docs/backend/prompts/](prompts/) (índice y tabla de olas en su [README](prompts/README.md)): alcanza con pegar el contenido del prompt del módulo en una sesión nueva y el agente hace el resto.** Los prompts dedicados se mantienen a mano y los CRUD se generan con `tools/prompts-modulos.py`.

Si preferís armar el prompt a mano, la instrucción clásica sigue siendo:

> *"Implementá el módulo `<NOMBRE>` siguiendo docs/backend/guia-de-modulos.md (secciones 2, 3 y la ficha del módulo). Rama `feature/back-<modulo>`. Sin push ni merge."*

Al finalizar la implementación, se actualiza la fila correspondiente al módulo en `docs/estado-del-proyecto.md`.

## 2. Reglas Comunes de Implementación

Todas las capas del backend deben respetar rigurosamente estas convenciones:
1. **Nombres de campo exactos:** Respetar sin alteraciones el contrato (`entidades.json` y `endpoints.csv`).
2. **Campos calculados:** Exclusivos en response DTOs; jamás se persisten en base de datos.
3. **Estructura de errores:** `ProblemDetail` (RFC 7807) extendido con `message` y `errors: [{ campo, mensaje }]`.
4. **Seguridad y roles:** Validación JWT en filtro; 401 si falta token, 403 si el rol no figura en `x-roles`.
5. **Sin paginación en servidor:** Los listados devuelven colecciones completas (`List<T>`) directamente.
6. **Estados de persona:** `ACTIVO` e `INACTIVO`. La condición "regular" es académica y por materia.
7. **Estado de pago de matrícula:** Dinámico (`AL_DIA`, `EN_MORA`, `PENDIENTE`); se anulan cuotas pendientes al cancelar.
8. **Comprobante de pago:** Formato `AAAA-NNNNNN` emitido por el backend a partir del año y número de pago.
9. **Baja lógica:** Actualización de estado sin borrado físico en entidades con registros históricos asociados.
10. **Mensajes de validación:** Claros y en español en todas las anotaciones de Bean Validation.
11. **Flujo de Git:** Commits convencionales y Pull Requests según `docs/flujo-de-trabajo.md`.
12. **Módulo de referencia:** `UsuarioController` y `UsuarioService` como ejemplo canónico de arquitectura.

## 3. Plantilla de un Módulo (Pasos de Desarrollo)

Para cada módulo, seguir sistemáticamente este ciclo de desarrollo en capas:
1. **DTOs:** Crear `RequestDTO` con Bean Validation (`@NotNull`, `@NotBlank`, `@Size`, etc.) y `ResponseDTO` con campos calculados.
2. **Service:** Definir interfaz en `service/` e implementar en `service/impl/` con validaciones de negocio transaccionales (`@Transactional`).
3. **Controller:** Anotar con `@RestController`, mapear ruta base, declarar endpoints exactos y anotar seguridad por rol.
4. **Tests Unitarios e Integración:** Cobertura obligatoria con Mockito y `@WebMvcTest`:
   - Caso feliz (200 / 201).
   - Validación de entrada fallida (400 Bad Request).
   - Control de acceso por rol (403 Forbidden).
   - Recurso inexistente (404 Not Found).
5. **Verificación local:** Ejecutar `./gradlew test -q` y arrancar con `spring.jpa.hibernate.ddl-auto=validate`.
6. **Entrega:** Declarar endpoints completados sobre el total y documentar dudas abiertas. No inventar endpoints fuera de contrato.

## 4. Orden de Desarrollo y Dependencias por Olas

Los módulos de una misma ola se pueden desarrollar en paralelo:

| Ola | Módulos | Dependencias Previas |
| :---: | :--- | :--- |
| **Base** | Autenticación y Seguridad, Usuarios | Configuración inicial y Spring Security (Hecho) |
| **Ola 1** | Ciclos Lectivos, Carreras, Planes de Estudio, Materias | Seguridad y Usuarios |
| **Ola 2** | Alumnos, Docentes | Ciclos Lectivos, Carreras y Planes de Estudio |
| **Ola 3** | Cursos (Comisiones), Asignaciones Docentes | Ciclos Lectivos, Materias y Docentes |
| **Ola 4** | Inscripciones a Cursos | Alumnos, Cursos y Ciclos Lectivos |
| **Ola 5** | Matrículas Anuales, Cuotas y Aranceles | Alumnos y Ciclos Lectivos |
| **Ola 6** | Pagos y Tarea de Cuotas Vencidas | Matrículas, Cuotas y Alumnos |
| **Ola 7** | Evaluaciones, Calificaciones, Asistencias, Mesas de Examen | Cursos, Materias, Docentes y Alumnos |
| **Ola 8** | Declaraciones Juradas, Solicitudes, Documentación | Alumnos, Planes de Estudio y Carreras |

## 5. Fichas por Módulo

### 5.1 Módulo: Autenticación y Sesión (`auth`)
- **Estado actual:** Hecho | **Rama sugerida:** `feature/back-auth` | **Ola:** 0
- **Entidades y tablas:** `Usuario` (`usuario`), `Rol` (`rol`)
- **Dependencias:** Ninguna (Infraestructura base)
- **Campos calculados:** Ninguno
- **Reglas de negocio aplicables:** RF01, RF02, RF05, RN17 (JWT Bearer, logout, expiración, 401/403 estandarizados)
- **Endpoints pendientes (0 de 3 pendientes; 3 listos):**
  - *Todos los endpoints implementados.*
- **Preguntas abiertas:** Ninguna (implementado y verificado).

### 5.2 Módulo: Gestión de Usuarios (`usuarios`)
- **Estado actual:** Hecho | **Rama sugerida:** `feature/back-usuarios` | **Ola:** 0
- **Entidades y tablas:** `Usuario` (`usuario`), `Rol` (`rol`)
- **Dependencias:** `auth`
- **Campos calculados:** Ninguno
- **Reglas de negocio aplicables:** RF03-RF05, RN17 (CRUD administrativo, hash BCrypt, perfil propio)
- **Endpoints pendientes (0 de 6 pendientes; 6 listos):**
  - *Todos los endpoints implementados.*
- **Preguntas abiertas:** Ninguna (implementado y verificado como módulo modelo).

### 5.3 Módulo: Carreras (`carreras`)
- **Estado actual:** Parcial | **Rama sugerida:** `feature/back-carreras` | **Ola:** 1
- **Entidades y tablas:** `Carrera` (`carrera`)
- **Dependencias:** `auth`, `usuarios`
- **Campos calculados:** Ninguno
- **Reglas de negocio aplicables:** RF17, RN17 (código único, baja lógica)
- **Endpoints pendientes (7 de 7 pendientes; 0 listos):**
  - `GET /api/carreras` [ADMINISTRADOR, ADMINISTRATIVO]
  - `GET /api/carreras/{id}` [ADMINISTRADOR, ADMINISTRATIVO]
  - `POST /api/carreras` [ADMINISTRADOR]
  - `PUT /api/carreras/{id}` [ADMINISTRADOR]
  - `DELETE /api/carreras/{id}` [ADMINISTRADOR]
  - `GET /api/carreras/{id}/planes` [ADMINISTRADOR, ADMINISTRATIVO]
  - `GET /api/carreras/{id}/cursos` [ADMINISTRADOR, ADMINISTRATIVO]
- **Preguntas abiertas:** Confirmar si baja de carrera inhabilita nuevos planes.

### 5.4 Módulo: Ciclos Lectivos (`ciclos-lectivos`)
- **Estado actual:** Parcial | **Rama sugerida:** `feature/back-ciclos-lectivos` | **Ola:** 1
- **Entidades y tablas:** `CicloLectivo` (`ciclo_lectivo`)
- **Dependencias:** `auth`, `usuarios`
- **Campos calculados:** Ninguno
- **Reglas de negocio aplicables:** RF20, RN04 (vigencia del ciclo y no superposición)
- **Endpoints pendientes (3 de 3 pendientes; 0 listos):**
  - `GET /api/ciclos-lectivos` [ADMINISTRADOR, ADMINISTRATIVO]
  - `POST /api/ciclos-lectivos` [ADMINISTRADOR]
  - `PUT /api/ciclos-lectivos/{id}` [ADMINISTRADOR]
- **Preguntas abiertas:** Validar si solo un ciclo puede estar vigente a la vez.

### 5.5 Módulo: Materias (`materias`)
- **Estado actual:** Parcial | **Rama sugerida:** `feature/back-materias` | **Ola:** 1
- **Entidades y tablas:** `Materia` (`materia`), `Correlatividad` (`correlatividad`)
- **Dependencias:** `planes-estudio`
- **Campos calculados:** `carreraNombre`, `planNombre`
- **Reglas de negocio aplicables:** RF17, RF18, RN17 (correlatividades regulares y aprobadas)
- **Endpoints pendientes (3 de 3 pendientes; 0 listos):**
  - `GET /api/materias` [ADMINISTRADOR, DOCENTE, ALUMNO]
  - `POST /api/materias` [ADMINISTRADOR]
  - `PUT /api/materias/{id}` [ADMINISTRADOR]
- **Preguntas abiertas:** Grafo de correlativas acíclico validado en backend.

### 5.6 Módulo: Planes de Estudio (`planes-estudio`)
- **Estado actual:** Parcial | **Rama sugerida:** `feature/back-planes-estudio` | **Ola:** 1
- **Entidades y tablas:** `PlanEstudio` (`plan_estudio`)
- **Dependencias:** `carreras`
- **Campos calculados:** `carreraNombre`
- **Reglas de negocio aplicables:** RF17, RN17 (resolución ministerial, año de vigencia)
- **Endpoints pendientes (2 de 2 pendientes; 0 listos):**
  - `GET /api/planes-estudio` [ADMINISTRADOR, ADMINISTRATIVO]
  - `GET /api/planes-estudio/{id}/materias` [ALUMNO, DOCENTE, ADMINISTRATIVO, ADMINISTRADOR]
- **Preguntas abiertas:** Verificar si un plan cerrado permite cursadas activas.

### 5.7 Módulo: Alumnos (`alumnos`)
- **Estado actual:** Parcial | **Rama sugerida:** `feature/back-alumnos` | **Ola:** 2
- **Entidades y tablas:** `Alumno` (`alumno`), `EstadoPersonaEnum`
- **Dependencias:** `ciclos-lectivos`, `carreras`, `planes-estudio`
- **Campos calculados:** `carrera`, `planNombre`, `cursoNombre`
- **Reglas de negocio aplicables:** RF06-RF09, RF34, RN04 (estado ACTIVO/INACTIVO, DNI único, baja lógica)
- **Endpoints pendientes (7 de 7 pendientes; 0 listos):**
  - `GET /api/alumnos` [ADMINISTRADOR, ADMINISTRATIVO]
  - `GET /api/alumnos/{id}` [ADMINISTRADOR, ADMINISTRATIVO]
  - `POST /api/alumnos` [ADMINISTRADOR, ADMINISTRATIVO]
  - `PUT /api/alumnos/{id}` [ADMINISTRADOR, ADMINISTRATIVO]
  - `DELETE /api/alumnos/{id}` [ADMINISTRADOR]
  - `GET /api/alumnos/{id}/situacion-academica` [ALUMNO, ADMINISTRADOR, ADMINISTRATIVO]
  - `GET /api/alumnos/{id}/situacion-administrativa` [ADMINISTRATIVO, ADMINISTRADOR, ALUMNO]
- **Preguntas abiertas:** Cálculo consolidado de situación académica y administrativa.

### 5.8 Módulo: Docentes (`docentes`)
- **Estado actual:** Parcial | **Rama sugerida:** `feature/back-docentes` | **Ola:** 2
- **Entidades y tablas:** `Docente` (`docente`), `EstadoPersonaEnum`
- **Dependencias:** `auth`, `usuarios`
- **Campos calculados:** Ninguno
- **Reglas de negocio aplicables:** RF10-RF13, RN11, RN12 (legajo único, estado ACTIVO/INACTIVO)
- **Endpoints pendientes (4 de 4 pendientes; 0 listos):**
  - `GET /api/docentes` [ADMINISTRADOR, ADMINISTRATIVO]
  - `POST /api/docentes` [ADMINISTRADOR]
  - `PUT /api/docentes/{id}` [ADMINISTRADOR]
  - `DELETE /api/docentes/{id}` [ADMINISTRADOR]
- **Preguntas abiertas:** Revisar si docente inactivo retiene histórico de cursos.

### 5.9 Módulo: Asignaciones Docentes (`asignaciones`)
- **Estado actual:** Parcial | **Rama sugerida:** `feature/back-asignaciones` | **Ola:** 3
- **Entidades y tablas:** `CursoMateria` (`curso_materia`), `RolDocenteEnum`
- **Dependencias:** `cursos`, `materias`, `docentes`
- **Campos calculados:** `docenteNombre`, `materiaNombre`, `cursoNombre`
- **Reglas de negocio aplicables:** RF19, RN11, RN12 (rol titular/adjunto, unicidad curso-materia-docente)
- **Endpoints pendientes (3 de 3 pendientes; 0 listos):**
  - `GET /api/asignaciones` [ADMINISTRADOR]
  - `POST /api/asignaciones` [ADMINISTRADOR]
  - `DELETE /api/asignaciones/{id}` [ADMINISTRADOR]
- **Preguntas abiertas:** Permitir múltiples docentes con distinto rol en la misma comisión.

### 5.10 Módulo: Cursos (Comisiones) (`cursos`)
- **Estado actual:** Parcial | **Rama sugerida:** `feature/back-cursos` | **Ola:** 3
- **Entidades y tablas:** `Curso` (`curso`), `TurnoEnum`
- **Dependencias:** `ciclos-lectivos`, `carreras`
- **Campos calculados:** `inscriptos`, `cuposDisponibles`, `carreraNombre`, `planNombre`
- **Reglas de negocio aplicables:** RF14-RF16, RN03, RN04 (cupo máximo, turno y ciclo activo)
- **Endpoints pendientes (3 de 3 pendientes; 0 listos):**
  - `GET /api/cursos` [ADMINISTRADOR, DOCENTE, ADMINISTRATIVO]
  - `POST /api/cursos` [ADMINISTRADOR]
  - `PUT /api/cursos/{id}` [ADMINISTRADOR]
- **Preguntas abiertas:** Validar si la capacidad máxima puede reducirse con alumnos inscriptos.

### 5.11 Módulo: Inscripciones a Cursos (`inscripciones`)
- **Estado actual:** Parcial | **Rama sugerida:** `feature/back-inscripciones` | **Ola:** 4
- **Entidades y tablas:** `Inscripcion` (`inscripcion`)
- **Dependencias:** `alumnos`, `cursos`, `ciclos-lectivos`
- **Campos calculados:** `alumnoNombre`, `cursoNombre`
- **Reglas de negocio aplicables:** RF22-RF24, RN01, RN02, RN03, RN04 (control de cupo estricto, una por ciclo/curso)
- **Endpoints pendientes (3 de 3 pendientes; 0 listos):**
  - `GET /api/inscripciones` [ADMINISTRADOR, ADMINISTRATIVO]
  - `POST /api/inscripciones` [ADMINISTRADOR, ADMINISTRATIVO]
  - `DELETE /api/inscripciones/{id}` [ADMINISTRADOR, ADMINISTRATIVO]
- **Preguntas abiertas:** Restricción atómica de concurrencia al validar cupo.

### 5.12 Módulo: Cuotas y Aranceles (`cuotas`)
- **Estado actual:** Hecho | **Rama sugerida:** `feature/back-cuotas` | **Ola:** 5
- **Entidades y tablas:** `Cuota` (`cuota`), `EstadoCuotaEnum`, `TipoCuotaEnum`
- **Dependencias:** `matriculas`
- **Campos calculados:** `idAlumno`, `alumnoNombre`, `concepto`, `mes`, `anio`, `fechaPago`, `estado` (VENCIDA dinámica)
- **Reglas de negocio aplicables:** RF28-RF31, RN06, RN07, RN18 (12 cuotas mensuales + 3 seguros, vencimiento)
- **Endpoints pendientes (0 de 2 pendientes; 2 listos):**
  - *Todos los endpoints implementados.*
- **Preguntas abiertas:** Tarea programada para persistir el estado VENCIDA (resuelta en `feature/back-cuotas-vencidas`).

### 5.13 Módulo: Matrículas Anuales (`matriculas`)
- **Estado actual:** Hecho | **Rama sugerida:** `feature/back-matriculas` | **Ola:** 5
- **Entidades y tablas:** `Matricula` (`matricula`), `EstadoMatriculaEnum`
- **Dependencias:** `alumnos`, `ciclos-lectivos`
- **Campos calculados:** `alumnoNombre`, `carreraNombre`, `cicloLectivoNombre`, `estadoPago`
- **Reglas de negocio aplicables:** RF25-RF27, RN05, RN18 (1 por alumno/ciclo, estadoPago calculado, anulación de cuotas pendientes)
- **Endpoints pendientes (0 de 4 pendientes; 4 listos):**
  - *Todos los endpoints implementados.*
- **Preguntas abiertas:** Regeneración de cuotas si la matrícula se reactiva; importes y vencimientos de las cuotas generadas (ver decisiones abiertas en `docs/estado-del-proyecto.md`).

### 5.14 Módulo: Pagos y Comprobantes (`pagos`)
- **Estado actual:** Parcial | **Rama sugerida:** `feature/back-pagos` | **Ola:** 6
- **Entidades y tablas:** `Pago` (`pago`), `MedioPagoEnum`
- **Dependencias:** `matriculas`, `cuotas`, `alumnos`
- **Campos calculados:** `alumnoNombre`, `cuotaConcepto`
- **Reglas de negocio aplicables:** RF32, RF33, RN08, RN09, RN10 (pago total obligatorio, comprobante AAAA-NNNNNN)
- **Endpoints pendientes (2 de 2 pendientes; 0 listos):**
  - `GET /api/pagos` [ADMINISTRATIVO, ADMINISTRADOR, ALUMNO]
  - `POST /api/pagos` [ADMINISTRATIVO, ADMINISTRADOR]
- **Preguntas abiertas:** Validación de medio de pago contra catálogo institucional.

### 5.15 Módulo: Asistencias (`asistencias`)
- **Estado actual:** Parcial | **Rama sugerida:** `feature/back-asistencias` | **Ola:** 7
- **Entidades y tablas:** `Asistencia` (`asistencia`), `EstadoAsistenciaEnum`
- **Dependencias:** `cursos`, `materias`, `alumnos`, `docentes`
- **Campos calculados:** `alumnoNombre`, `materiaNombre`
- **Reglas de negocio aplicables:** RF41-RF44, RN11, RN12, RN13 (toma por materia, estados PRESENT/AUSENT/TARDANZA/JUSTIF)
- **Endpoints pendientes (2 de 2 pendientes; 0 listos):**
  - `GET /api/asistencias` [DOCENTE, ALUMNO, ADMINISTRADOR]
  - `POST /api/asistencias/lote` [DOCENTE]
- **Preguntas abiertas:** Cálculo de regularidad académica: mínimo 75% de asistencias.

### 5.16 Módulo: Calificaciones (`calificaciones`)
- **Estado actual:** Parcial | **Rama sugerida:** `feature/back-calificaciones` | **Ola:** 7
- **Entidades y tablas:** `Calificacion` (`calificacion`)
- **Dependencias:** `evaluaciones`, `alumnos`, `docentes`
- **Campos calculados:** `condicion`, `alumnoNombre`, `materiaNombre`
- **Reglas de negocio aplicables:** RF38-RF40, RN11, RN14, RN21 (escala 1 a 10, nota >= 4 aprobado, solo docente asignado)
- **Endpoints pendientes (2 de 2 pendientes; 0 listos):**
  - `GET /api/calificaciones` [ALUMNO, DOCENTE, ADMINISTRADOR]
  - `POST /api/calificaciones` [DOCENTE, ADMINISTRADOR]
- **Preguntas abiertas:** Permitir recalificación con auditoría de usuario modificador.

### 5.17 Módulo: Evaluaciones (`evaluaciones`)
- **Estado actual:** Parcial | **Rama sugerida:** `feature/back-evaluaciones` | **Ola:** 7
- **Entidades y tablas:** `Evaluacion` (`evaluacion`), `TipoEvaluacionEnum`
- **Dependencias:** `cursos`, `materias`, `docentes`
- **Campos calculados:** `materiaNombre`, `cursoNombre`
- **Reglas de negocio aplicables:** RF36, RF37, RN11 (solo docente asignado a la cátedra puede crear evaluación)
- **Endpoints pendientes (3 de 3 pendientes; 0 listos):**
  - `GET /api/evaluaciones` [DOCENTE, ADMINISTRADOR]
  - `POST /api/evaluaciones` [DOCENTE]
  - `POST /api/evaluaciones/{idEvaluacion}/calificaciones` [DOCENTE]
- **Preguntas abiertas:** Fecha de evaluación dentro del rango del ciclo lectivo.

### 5.18 Módulo: Mesas de Examen (`mesas-examen`)
- **Estado actual:** Parcial | **Rama sugerida:** `feature/back-mesas-examen` | **Ola:** 7
- **Entidades y tablas:** `MesaExamen` (`mesa_examen`), `InscripcionMesa` (`inscripcion_mesa`)
- **Dependencias:** `materias`, `ciclos-lectivos`, `docentes`, `alumnos`
- **Campos calculados:** `inscriptos`, `cuposDisponibles`, `materiaNombre`, `carreraNombre`
- **Reglas de negocio aplicables:** RF45-RF49, RN15 (inscripción previa, verificación de regularidad y tribunal docente)
- **Endpoints pendientes (5 de 5 pendientes; 0 listos):**
  - `GET /api/mesas-examen` [ALUMNO, DOCENTE, ADMINISTRATIVO, ADMINISTRADOR]
  - `POST /api/mesas-examen/{id}/inscripciones` [ALUMNO]
  - `DELETE /api/mesas-examen/{id}/inscripciones/{idInscripcion}` [ALUMNO]
  - `POST /api/mesas-examen` [ADMINISTRADOR]
  - `PUT /api/mesas-examen/{id}` [ADMINISTRADOR]
- **Preguntas abiertas:** Límite de tiempo para darse de baja de la mesa de examen.

### 5.19 Módulo: Declaraciones Juradas (`declaraciones-juradas`)
- **Estado actual:** Parcial | **Rama sugerida:** `feature/back-declaraciones-juradas` | **Ola:** 8
- **Entidades y tablas:** `DeclaracionJurada` (`declaracion_jurada`), `DeclaracionMateria`
- **Dependencias:** `alumnos`, `ciclos-lectivos`, `materias`
- **Campos calculados:** `alumnoNombre`, `cicloNombre`, `materiasNombres`
- **Reglas de negocio aplicables:** RF50-RF52, RN16 (presentación anual, verificación estricta de correlatividades)
- **Endpoints pendientes (2 de 2 pendientes; 0 listos):**
  - `GET /api/declaraciones-juradas` [ALUMNO, ADMINISTRADOR, ADMINISTRATIVO]
  - `POST /api/declaraciones-juradas` [ALUMNO]
- **Preguntas abiertas:** Plazo límite de recepción de DDJJ en el ciclo.

### 5.20 Módulo: Documentación y Legajos (`documentacion`)
- **Estado actual:** Parcial | **Rama sugerida:** `feature/back-documentacion` | **Ola:** 8
- **Entidades y tablas:** `Documentacion` (`documentacion`), `TipoDocumentacionEnum`
- **Dependencias:** `alumnos`
- **Campos calculados:** `alumnoNombre`
- **Reglas de negocio aplicables:** RF56-RF58, RN17 (verificación física/digital, estados PENDIENTE/ENTREGADO)
- **Endpoints pendientes (2 de 2 pendientes; 0 listos):**
  - `GET /api/documentacion` [ALUMNO, ADMINISTRATIVO, ADMINISTRADOR]
  - `PUT /api/documentacion/{id}` [ADMINISTRATIVO, ADMINISTRADOR]
- **Preguntas abiertas:** Almacenamiento de adjuntos o solo estado de verificación en legajo.

### 5.21 Módulo: Solicitudes y Trámites (`solicitudes`)
- **Estado actual:** Parcial | **Rama sugerida:** `feature/back-solicitudes` | **Ola:** 8
- **Entidades y tablas:** `Solicitud` (`solicitud`), `TipoSolicitudEnum`
- **Dependencias:** `alumnos`, `usuarios`
- **Campos calculados:** `alumnoNombre`
- **Reglas de negocio aplicables:** RF53-RF55, RN17 (resolución administrativa, motivos y estados PENDIENTE/APROBADA/RECHAZADA)
- **Endpoints pendientes (3 de 3 pendientes; 0 listos):**
  - `GET /api/solicitudes` [ADMINISTRATIVO, ADMINISTRADOR, ALUMNO]
  - `POST /api/solicitudes` [ALUMNO]
  - `PATCH /api/solicitudes/{id}` [ADMINISTRATIVO, ADMINISTRADOR]
- **Preguntas abiertas:** Notificación o registro de resolución formal por administrativo.

## 6. Lo que Falta y No es Código

Las tareas pendientes correspondientes a portada institucional, actualización de diagramas UML/DER, especificaciones de análisis y preparación de la defensa oral se gestionan en:
- [Entregables y Tareas Pendientes (No Código)](../pendientes.md)
