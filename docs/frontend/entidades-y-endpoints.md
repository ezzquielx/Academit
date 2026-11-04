# Relevamiento de Entidades y Endpoints de la Maqueta Frontend

> **Documento de Auditoría y Especificación de Contratos**  
> **Proyecto:** Academit - Sistema de Gestión del Instituto  
> **Ubicación:** `docs/frontend/entidades-y-endpoints.md`  
> **Alcance:** Auditoría arquitectónica del frontend (`frontend/src/`) y alineación con el Diseño Entidad-Relación (DER).

---

## Resumen Ejecutivo

- **Total de Entidades Identificadas:** 22
- **Total de Endpoints en el Contrato:** 74
- **Endpoints Implementados en API (`real`):** 74 (100.0%)
- **Endpoints Inferidos / Convención REST (`inferido`):** 0 (0.0%)
- **Endpoints Faltantes / Brechas (`falta`):** 0 (0.0%)

---

## 1. Inventario de Entidades de Dominio

A continuación se presentan las entidades del frontend categorizando sus campos en persistidos y calculados, enums y relaciones:

### 1.1 Entidad: `Alumno`
**Descripción:** Estudiante inscripto en el instituto educativo.

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idAlumno` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador único del alumno (PK). |
| `legajo` | **PERSISTIDO** | string | Sí | - | Código identificatorio institucional (ej. ALU-1001). |
| `nombre` | **PERSISTIDO** | string | Sí | - | Nombres del estudiante. |
| `apellido` | **PERSISTIDO** | string | Sí | - | Apellidos del estudiante. |
| `dni` | **PERSISTIDO** | string | Sí | - | Documento Nacional de Identidad único. |
| `email` | **PERSISTIDO** | string | Sí | - | Correo electrónico de contacto. |
| `telefono` | **PERSISTIDO** | string | No | - | Teléfono de contacto. |
| `domicilio` | **PERSISTIDO** | string | No | - | Domicilio residencial del estudiante. |
| `fechaNacimiento` | **PERSISTIDO** | string (YYYY-MM-DD) | Sí | - | Fecha de nacimiento. |
| `idCarrera` | **PERSISTIDO** | number (Integer) | Sí | - | FK a la Carrera en la que está inscripto. |
| `idPlan` | **PERSISTIDO** | number (Integer) | Sí | - | FK al Plan de Estudio asignado. |
| `idCurso` | **PERSISTIDO** | number (Integer) | No | - | FK al curso/comisión principal asignada. |
| `cicloLectivo` | **PERSISTIDO** | number (Integer) | Sí | - | Año lectivo de ingreso o cursada activa. |
| `estado` | **PERSISTIDO** | string (Enum) | Sí | ACTIVO, INACTIVO | Condición institucional activa o inactiva. |
| `estadoMatricula` | **PERSISTIDO** | string (Enum) | Sí | ACTIVA, PENDIENTE, CANCELADA | Estado de la matrícula para el ciclo lectivo actual. |
| `idMatricula` | **PERSISTIDO** | number (Integer) | No | - | FK a la matrícula del ciclo actual. |
| `carrera` | *CALCULADO* | string | No | - | Nombre desnormalizado de la carrera. |
| `planNombre` | *CALCULADO* | string | No | - | Nombre desnormalizado del plan curricular. |
| `cursoNombre` | *CALCULADO* | string | No | - | Nombre del curso asignado. |

**Relaciones:**
- Con `Carrera` (M:1): `idCarrera` -> `idCarrera`
- Con `PlanEstudio` (M:1): `idPlan` -> `idPlan`
- Con `Curso` (M:1): `idCurso` -> `idCurso`
- Con `Matricula` (1:1): `idMatricula` -> `idMatricula`

**Pantallas que la consumen:** `GestionAlumnosPage.jsx, GestionInscripcionesPage.jsx, AdminSituacionAlumnoPage.jsx`

---

### 1.2 Entidad: `Docente`
**Descripción:** Profesor o docente a cargo de cátedras y cursos.

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idDocente` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador único del docente (PK). |
| `legajo` | **PERSISTIDO** | string | Sí | - | Código identificatorio del docente (ej. DOC-201). |
| `nombre` | **PERSISTIDO** | string | Sí | - | Nombres del docente. |
| `apellido` | **PERSISTIDO** | string | Sí | - | Apellidos del docente. |
| `dni` | **PERSISTIDO** | string | Sí | - | Documento Nacional de Identidad. |
| `email` | **PERSISTIDO** | string | Sí | - | Correo institucional o personal. |
| `telefono` | **PERSISTIDO** | string | No | - | Teléfono de contacto. |
| `titulo` | **PERSISTIDO** | string | Sí | - | Título habilitante o especialidad académica. |
| `estado` | **PERSISTIDO** | string (Enum) | Sí | ACTIVO, INACTIVO | Estado laboral/académico del docente. |

**Relaciones:**
- Con `Asignacion` (1:N): `idDocente` -> `idDocente`

**Pantallas que la consumen:** `GestionDocentesPage.jsx, GestionAsignacionesPage.jsx`

---

### 1.3 Entidad: `Carrera`
**Descripción:** Titulación académica dictada por el instituto.

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idCarrera` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador único de la carrera (PK). |
| `codigo` | **PERSISTIDO** | string | Sí | - | Código identificador (ej. TSD, IA, REDES). |
| `nombre` | **PERSISTIDO** | string | Sí | - | Nombre oficial de la carrera. |
| `tituloOtorgado` | **PERSISTIDO** | string | Sí | - | Título con validez nacional que expide. |
| `duracionAnios` | **PERSISTIDO** | number (Integer) | Sí | - | Duración teórica de la carrera en años. |
| `resolucion` | **PERSISTIDO** | string | No | - | Resolución ministerial habilitante. |
| `estado` | **PERSISTIDO** | string (Enum) | Sí | ACTIVA, INACTIVA | Estado de oferta académica de la carrera. |

**Relaciones:**
- Con `PlanEstudio` (1:N): `idCarrera` -> `idCarrera`
- Con `Curso` (1:N): `idCarrera` -> `idCarrera`

**Pantallas que la consumen:** `GestionCarrerasPage.jsx, GestionAlumnosPage.jsx`

---

### 1.4 Entidad: `PlanEstudio`
**Descripción:** Estructura curricular y materias que componen una carrera.

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idPlan` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador único del plan (PK). |
| `idCarrera` | **PERSISTIDO** | number (Integer) | Sí | - | FK a la Carrera. |
| `codigo` | **PERSISTIDO** | string | Sí | - | Código del plan (ej. PLAN-2023). |
| `nombre` | **PERSISTIDO** | string | Sí | - | Nombre descriptivo del plan. |
| `anioAprobacion` | **PERSISTIDO** | number (Integer) | Sí | - | Año de entrada en vigencia. |
| `cantidadMaterias` | **PERSISTIDO** | number (Integer) | Sí | - | Cantidad total de materias que lo integran. |
| `estado` | **PERSISTIDO** | string (Enum) | Sí | VIGENTE, CERRADO | Estado de vigencia del plan de estudios. |
| `carreraNombre` | *CALCULADO* | string | No | - | Nombre desnormalizado de la carrera. |

**Relaciones:**
- Con `Carrera` (M:1): `idCarrera` -> `idCarrera`
- Con `Materia` (1:N): `idPlan` -> `idPlan`

**Pantallas que la consumen:** `GestionPlanEstudiosPage.jsx, PlanEstudiosAlumnoPage.jsx`

---

### 1.5 Entidad: `Materia`
**Descripción:** Asignatura curricular dictada dentro de un plan de estudio.

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idMateria` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador único de la materia (PK). |
| `codigo` | **PERSISTIDO** | string | Sí | - | Código curricular (ej. PROG-1). |
| `nombre` | **PERSISTIDO** | string | Sí | - | Nombre de la materia. |
| `idCarrera` | **PERSISTIDO** | number (Integer) | Sí | - | FK a la Carrera. |
| `idPlan` | **PERSISTIDO** | number (Integer) | Sí | - | FK al Plan de Estudio. |
| `anio` | **PERSISTIDO** | number (Integer) | Sí | - | Año de cursada (1°, 2°, 3° año). |
| `regimen` | **PERSISTIDO** | string (Enum) | Sí | ANUAL, CUATRIMESTRAL | Régimen de dictado. |
| `horasSemanales` | **PERSISTIDO** | number (Integer) | Sí | - | Carga horaria semanal. |
| `correlativas` | **PERSISTIDO** | array of numbers | No | - | IDs de materias correlativas previas requeridas. |
| `carreraNombre` | *CALCULADO* | string | No | - | Nombre de la carrera asociada. |
| `planNombre` | *CALCULADO* | string | No | - | Nombre del plan de estudio asociado. |

**Relaciones:**
- Con `Carrera` (M:1): `idCarrera` -> `idCarrera`
- Con `PlanEstudio` (M:1): `idPlan` -> `idPlan`

**Pantallas que la consumen:** `GestionMateriasPage.jsx, PlanEstudiosAlumnoPage.jsx`

---

### 1.6 Entidad: `Curso`
**Descripción:** Comisión y división física/horaria donde se cursan materias.

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idCurso` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador único del curso (PK). |
| `codigo` | **PERSISTIDO** | string | Sí | - | Código de la comisión (ej. 1A-TM-2026). |
| `nombre` | **PERSISTIDO** | string | Sí | - | Nombre descriptivo de la comisión. |
| `idCarrera` | **PERSISTIDO** | number (Integer) | Sí | - | FK a la Carrera. |
| `idPlan` | **PERSISTIDO** | number (Integer) | Sí | - | FK al Plan de Estudio. |
| `turno` | **PERSISTIDO** | string (Enum) | Sí | MANANA, TARDE, NOCHE | Turno de cursada (sin tildes). |
| `capacidadMaxima` | **PERSISTIDO** | number (Integer) | Sí | - | Límite de capacidad física o aula. |
| `cicloLectivo` | **PERSISTIDO** | number (Integer) | Sí | - | Año lectivo correspondiente. |
| `aula` | **PERSISTIDO** | string | No | - | Aula física asignada. |
| `inscriptos` | *CALCULADO* | number (Integer) | Sí | - | Contador de alumnos matriculados confirmados. |
| `cuposDisponibles` | *CALCULADO* | number (Integer) | Sí | - | Cupos disponibles (capacidadMaxima - inscriptos). |
| `carreraNombre` | *CALCULADO* | string | No | - | Nombre desnormalizado de la carrera. |
| `planNombre` | *CALCULADO* | string | No | - | Nombre desnormalizado del plan. |

**Relaciones:**
- Con `Carrera` (M:1): `idCarrera` -> `idCarrera`
- Con `PlanEstudio` (M:1): `idPlan` -> `idPlan`

**Pantallas que la consumen:** `GestionCursosPage.jsx, GestionInscripcionesPage.jsx`

---

### 1.7 Entidad: `CicloLectivo`
**Descripción:** Período académico anual de actividades del instituto.

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idCiclo` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador único del ciclo (PK). |
| `anio` | **PERSISTIDO** | number (Integer) | Sí | - | Año calendario (ej. 2026). |
| `nombre` | **PERSISTIDO** | string | Sí | - | Denominación (ej. Ciclo Lectivo 2026). |
| `fechaInicio` | **PERSISTIDO** | string (YYYY-MM-DD) | Sí | - | Fecha de inicio del ciclo. |
| `fechaFin` | **PERSISTIDO** | string (YYYY-MM-DD) | Sí | - | Fecha de cierre del ciclo. |
| `estado` | **PERSISTIDO** | string (Enum) | Sí | ACTIVO, CERRADO, PLANIFICADO | Estado operativo del ciclo lectivo. |

**Relaciones:**
- Con `Inscripcion` (1:N): `idCiclo` -> `cicloLectivo`

**Pantallas que la consumen:** `GestionCiclosLectivosPage.jsx`

---

### 1.8 Entidad: `Asignacion`
**Descripción:** Asignación docente a un curso y materia específicos.

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idAsignacion` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador de la asignación (PK). |
| `idDocente` | **PERSISTIDO** | number (Integer) | Sí | - | FK al Docente asignado. |
| `idMateria` | **PERSISTIDO** | number (Integer) | Sí | - | FK a la Materia dictada. |
| `idCurso` | **PERSISTIDO** | number (Integer) | Sí | - | FK al Curso / Comisión. |
| `rolDocente` | **PERSISTIDO** | string (Enum) | Sí | Titular, Adjunto, JTP, Ayudante | Cargo docente en la cátedra. |
| `docenteNombre` | *CALCULADO* | string | No | - | Nombre completo del docente. |
| `materiaNombre` | *CALCULADO* | string | No | - | Nombre de la materia. |
| `cursoNombre` | *CALCULADO* | string | No | - | Nombre del curso. |

**Relaciones:**
- Con `Docente` (M:1): `idDocente` -> `idDocente`
- Con `Materia` (M:1): `idMateria` -> `idMateria`
- Con `Curso` (M:1): `idCurso` -> `idCurso`

**Pantallas que la consumen:** `GestionAsignacionesPage.jsx`

---

### 1.9 Entidad: `Inscripcion`
**Descripción:** Inscripción de un estudiante a una comisión/curso anual.

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idInscripcion` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador único de inscripción (PK). |
| `idAlumno` | **PERSISTIDO** | number (Integer) | Sí | - | FK al Alumno inscripto. |
| `idCurso` | **PERSISTIDO** | number (Integer) | Sí | - | FK al Curso/Comisión. |
| `cicloLectivo` | **PERSISTIDO** | number (Integer) | Sí | - | Año lectivo cursado. |
| `fechaInscripcion` | **PERSISTIDO** | string (YYYY-MM-DD) | Sí | - | Fecha de registro de la inscripción. |
| `estado` | **PERSISTIDO** | string (Enum) | Sí | CONFIRMADA, CANCELADA | Estado de la inscripción. |
| `alumnoNombre` | *CALCULADO* | string | No | - | Nombre completo del alumno. |
| `cursoNombre` | *CALCULADO* | string | No | - | Nombre del curso inscripto. |

**Relaciones:**
- Con `Alumno` (M:1): `idAlumno` -> `idAlumno`
- Con `Curso` (M:1): `idCurso` -> `idCurso`

**Pantallas que la consumen:** `GestionInscripcionesPage.jsx`

---

### 1.10 Entidad: `Matricula`
**Descripción:** Matrícula anual institucional habilitante de cursada.

> **Nota de Dominio:** Matricula.estadoPago no se guarda: es un campo calculado de solo lectura que el backend genera a partir de las cuotas asociadas (EN_MORA, AL_DIA, PENDIENTE).

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idMatricula` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador de la matrícula (PK). |
| `idAlumno` | **PERSISTIDO** | number (Integer) | Sí | - | FK al Alumno matriculado. |
| `idCarrera` | **PERSISTIDO** | number (Integer) | Sí | - | FK a la Carrera matriculada. |
| `idCiclo` | **PERSISTIDO** | number (Integer) | Sí | - | FK al Ciclo Lectivo correspondiente. |
| `cicloLectivo` | **PERSISTIDO** | number (Integer) | Sí | - | Año lectivo de la matrícula. |
| `fecha` | **PERSISTIDO** | string (YYYY-MM-DD) | Sí | - | Fecha de emisión de la matrícula. |
| `estado` | **PERSISTIDO** | string (Enum) | Sí | ACTIVA, PENDIENTE, CANCELADA | Estado académico de la matrícula. |
| `alumnoNombre` | *CALCULADO* | string | No | - | Nombre completo del alumno. |
| `carreraNombre` | *CALCULADO* | string | No | - | Nombre de la carrera matriculada. |
| `cicloLectivoNombre` | *CALCULADO* | string | No | - | Nombre del ciclo lectivo. |
| `estadoPago` | *CALCULADO* | string (Enum) | No | AL_DIA, EN_MORA, PENDIENTE | Estado financiero calculado a partir de las cuotas asociadas (EN_MORA si hay cuota vencida, AL_DIA si están pagadas, PENDIENTE en el resto). Solo lectura. |

**Relaciones:**
- Con `Alumno` (M:1): `idAlumno` -> `idAlumno`
- Con `Carrera` (M:1): `idCarrera` -> `idCarrera`
- Con `CicloLectivo` (M:1): `idCiclo` -> `idCiclo`

**Pantallas que la consumen:** `AdminMatriculasPage.jsx, CancelarMatriculaPage.jsx`

---

### 1.11 Entidad: `Cuota`
**Descripción:** Obligación arancelaria mensual o seguro cuatrimestral emitida para un alumno.

> **Nota de Dominio:** `idAlumno` pasa a ser campo calculado derivado de la matrícula vinculada (`idMatricula` -> `Matricula.idAlumno`).

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idCuota` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador único de la cuota (PK). |
| `idMatricula` | **PERSISTIDO** | number (Integer) | Sí | - | FK a la Matrícula del ciclo. |
| `concepto` | **PERSISTIDO** | string | Sí | - | Descripción arancelaria (ej. Cuota 1 - Enero 2026). |
| `mes` | **PERSISTIDO** | number (Integer) | Sí | - | Número de mes correspondiente (1 a 12). |
| `anio` | **PERSISTIDO** | number (Integer) | Sí | - | Año lectivo de facturación. |
| `fechaVencimiento` | **PERSISTIDO** | string (YYYY-MM-DD) | Sí | - | Fecha límite de pago. |
| `importe` | **PERSISTIDO** | number (Float) | Sí | - | Monto a abonar en pesos argentinos. |
| `tipo` | **PERSISTIDO** | string (Enum) | Sí | MENSUAL, SEGURO_CUATRIMESTRAL | Concepto arancelario. |
| `estado` | **PERSISTIDO** | string (Enum) | Sí | PENDIENTE, PAGADA, VENCIDA, ANULADA | Estado arancelario de la cuota. |
| `fechaPago` | **PERSISTIDO** | string (YYYY-MM-DD) | No | - | Fecha de cancelación efectiva. |
| `idAlumno` | *CALCULADO* | number (Integer) | No | - | Campo calculado derivado de la matrícula (idMatricula -> Matricula.idAlumno). |
| `alumnoNombre` | *CALCULADO* | string | No | - | Nombre completo del alumno derivado de matrícula. |

**Relaciones:**
- Con `Matricula` (M:1): `idMatricula` -> `idMatricula`

**Pantallas que la consumen:** `AdminCuotasPage.jsx, AdminPagosPage.jsx`

---

### 1.12 Entidad: `Pago`
**Descripción:** Comprobante de recaudación arancelaria emitido por el instituto.

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idPago` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador único del pago (PK). |
| `idCuota` | **PERSISTIDO** | number (Integer) | No | - | FK a la Cuota cancelada (null en matrícula u otros). |
| `idAlumno` | **PERSISTIDO** | number (Integer) | Sí | - | FK al Alumno que realiza el pago. |
| `concepto` | **PERSISTIDO** | string | Sí | - | Concepto cancelado. |
| `importe` | **PERSISTIDO** | number (Float) | Sí | - | Monto total cancelado (exacto según RN09). |
| `fechaPago` | **PERSISTIDO** | string (YYYY-MM-DD) | Sí | - | Fecha de recepción del pago. |
| `medioPago` | **PERSISTIDO** | string (Enum) | Sí | EFECTIVO, TRANSFERENCIA, TARJETA_CREDITO, TARJETA_DEBITO, OTRO | Canal de cobranza utilizado. |
| `comprobante` | **PERSISTIDO** | string | Sí | - | Número de comprobante oficial AAAA-NNNNNN generado por el backend al registrar el pago (solo lectura). |
| `estado` | **PERSISTIDO** | string (Enum) | Sí | ACREDITADO, PENDIENTE, RECHAZADO | Estado contable de acreditación. |
| `alumnoNombre` | *CALCULADO* | string | No | - | Nombre completo del alumno pagador. |
| `cuotaConcepto` | *CALCULADO* | string | No | - | Detalle de la cuota cancelada. |

**Relaciones:**
- Con `Cuota` (M:1): `idCuota` -> `idCuota`
- Con `Alumno` (M:1): `idAlumno` -> `idAlumno`

**Pantallas que la consumen:** `AdminPagosPage.jsx, SituacionAcademicaArancelariaPage.jsx`

---

### 1.13 Entidad: `Evaluacion`
**Descripción:** Instancia evaluativa planificada por el docente titular de cátedra.

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idEvaluacion` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador único de la evaluación (PK). |
| `idMateria` | **PERSISTIDO** | number (Integer) | Sí | - | FK a la Materia evaluada. |
| `idCurso` | **PERSISTIDO** | number (Integer) | Sí | - | FK al Curso / Comisión evaluada. |
| `fecha` | **PERSISTIDO** | string (YYYY-MM-DD) | Sí | - | Fecha planificada de la evaluación. |
| `tipo` | **PERSISTIDO** | string (Enum) | Sí | PARCIAL, TRABAJO_PRACTICO | Tipo de examen. |
| `descripcion` | **PERSISTIDO** | string | Sí | - | Temario y alcance del examen. |
| `ponderacion` | **PERSISTIDO** | string | No | - | Porcentaje en la nota final (ej. 30%). |
| `materiaNombre` | *CALCULADO* | string | No | - | Nombre de la materia evaluada. |
| `cursoNombre` | *CALCULADO* | string | No | - | Nombre de la comisión evaluada. |

**Relaciones:**
- Con `Materia` (M:1): `idMateria` -> `idMateria`
- Con `Curso` (M:1): `idCurso` -> `idCurso`

**Pantallas que la consumen:** `EvaluacionesDocentePage.jsx, CalificacionesDocentePage.jsx`

---

### 1.14 Entidad: `Calificacion`
**Descripción:** Nota numérica obtenida por un alumno en una instancia evaluativa.

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idCalificacion` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador de la calificación (PK). |
| `idAlumno` | **PERSISTIDO** | number (Integer) | Sí | - | FK al Alumno calificado. |
| `idMateria` | **PERSISTIDO** | number (Integer) | Sí | - | FK a la Materia evaluada. |
| `evaluacionNombre` | **PERSISTIDO** | string | Sí | - | Nombre de la evaluación asociada. |
| `tipo` | **PERSISTIDO** | string (Enum) | Sí | PARCIAL, TRABAJO_PRACTICO | Tipo de evaluación evaluada. |
| `nota` | **PERSISTIDO** | number (Integer, 1-10) | Sí | - | Calificación numérica obtenida (1 a 10). |
| `fecha` | **PERSISTIDO** | string (YYYY-MM-DD) | Sí | - | Fecha de registro de la nota. |
| `observacion` | **PERSISTIDO** | string | No | - | Devolución pedagógica del docente. |
| `condicion` | *CALCULADO* | string (Enum) | Sí | APROBADO, DESAPROBADO | Condición calculada a partir de nota (nota >= 4 ? APROBADO : DESAPROBADO). |
| `alumnoNombre` | *CALCULADO* | string | No | - | Nombre completo del alumno. |
| `materiaNombre` | *CALCULADO* | string | No | - | Nombre de la materia evaluada. |

**Relaciones:**
- Con `Alumno` (M:1): `idAlumno` -> `idAlumno`
- Con `Materia` (M:1): `idMateria` -> `idMateria`

**Pantallas que la consumen:** `CalificacionesDocentePage.jsx, CalificacionesAlumnoPage.jsx`

---

### 1.15 Entidad: `Asistencia`
**Descripción:** Presentismo diario de clase tomado por cátedra.

> **Nota de Dominio:** Asistencia se registra por alumno, curso, materia y fecha.

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idAsistencia` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador del registro de asistencia (PK). |
| `idAlumno` | **PERSISTIDO** | number (Integer) | Sí | - | FK al Alumno presente o ausente. |
| `idMateria` | **PERSISTIDO** | number (Integer) | Sí | - | FK a la Materia cursada. |
| `idCurso` | **PERSISTIDO** | number (Integer) | Sí | - | FK al Curso / Comisión dictada. |
| `fecha` | **PERSISTIDO** | string (YYYY-MM-DD) | Sí | - | Fecha de la clase dictada. |
| `estado` | **PERSISTIDO** | string (Enum) | Sí | PRESENTE, AUSENTE, TARDANZA, JUSTIFICADO | Estado de asistencia registrado. |
| `alumnoNombre` | *CALCULADO* | string | No | - | Nombre completo del alumno. |
| `materiaNombre` | *CALCULADO* | string | No | - | Nombre de la materia dictada. |

**Relaciones:**
- Con `Alumno` (M:1): `idAlumno` -> `idAlumno`
- Con `Materia` (M:1): `idMateria` -> `idMateria`
- Con `Curso` (M:1): `idCurso` -> `idCurso`

**Pantallas que la consumen:** `AsistenciasDocentePage.jsx, AsistenciasAlumnoPage.jsx`

---

### 1.16 Entidad: `MesaExamen`
**Descripción:** Mesa examinadora de examen final institucional.

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idMesa` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador de la mesa de examen (PK). |
| `idMateria` | **PERSISTIDO** | number (Integer) | Sí | - | FK a la Materia a rendir. |
| `idCarrera` | **PERSISTIDO** | number (Integer) | Sí | - | FK a la Carrera. |
| `fecha` | **PERSISTIDO** | string (YYYY-MM-DD) | Sí | - | Fecha de la mesa examinadora. |
| `hora` | **PERSISTIDO** | string (HH:mm) | Sí | - | Hora fijada de inicio. |
| `turno` | **PERSISTIDO** | string (Enum) | Sí | MANANA, TARDE, NOCHE | Turno de la mesa (sin tildes). |
| `tribunal` | **PERSISTIDO** | string | Sí | - | Nombres de los docentes del tribunal evaluador. |
| `aula` | **PERSISTIDO** | string | Sí | - | Aula física asignada para el examen. |
| `cupoMaximo` | **PERSISTIDO** | number (Integer) | Sí | - | Capacidad máxima de inscriptos. |
| `alumnosInscriptosIds` | **PERSISTIDO** | array of numbers | Sí | - | Lista de IDs de alumnos inscriptos. |
| `estado` | **PERSISTIDO** | string (Enum) | Sí | ABIERTA, CERRADA, FINALIZADA | Estado de la mesa. |
| `inscriptos` | *CALCULADO* | number (Integer) | Sí | - | Contador de alumnos inscriptos. |
| `cuposDisponibles` | *CALCULADO* | number (Integer) | Sí | - | Cupos disponibles (cupoMaximo - inscriptos). |
| `materiaNombre` | *CALCULADO* | string | No | - | Nombre de la materia evaluada. |
| `carreraNombre` | *CALCULADO* | string | No | - | Nombre de la carrera asociada. |

**Relaciones:**
- Con `Materia` (M:1): `idMateria` -> `idMateria`
- Con `Carrera` (M:1): `idCarrera` -> `idCarrera`

**Pantallas que la consumen:** `GestionMesasPage.jsx, MesasExamenPage.jsx, InscripcionMesasPage.jsx`

---

### 1.17 Entidad: `InscripcionMesa`
**Descripción:** Inscripción individual de un alumno para rendir examen final.

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idInscripcionMesa` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador único de inscripción a mesa (PK). |
| `idMesa` | **PERSISTIDO** | number (Integer) | Sí | - | FK a la Mesa de Examen. |
| `idAlumno` | **PERSISTIDO** | number (Integer) | Sí | - | FK al Alumno inscripto. |
| `fechaInscripcion` | **PERSISTIDO** | string (YYYY-MM-DD) | Sí | - | Fecha de registro de la inscripción. |
| `estado` | **PERSISTIDO** | string (Enum) | Sí | CONFIRMADA, CANCELADA | Estado de la inscripción a mesa. |
| `alumnoNombre` | *CALCULADO* | string | No | - | Nombre completo del alumno. |
| `materiaNombre` | *CALCULADO* | string | No | - | Nombre de la materia a rendir. |

**Relaciones:**
- Con `MesaExamen` (M:1): `idMesa` -> `idMesa`
- Con `Alumno` (M:1): `idAlumno` -> `idAlumno`

**Pantallas que la consumen:** `InscripcionMesasPage.jsx`

---

### 1.18 Entidad: `Solicitud`
**Descripción:** Trámite administrativo o académico solicitado por el alumno.

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idSolicitud` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador único de la solicitud (PK). |
| `idAlumno` | **PERSISTIDO** | number (Integer) | Sí | - | FK al Alumno solicitante. |
| `tipo` | **PERSISTIDO** | string (Enum) | Sí | CONSTANCIA, PASANTIA, TITULO_INTERMEDIO, ACADEMICA, CAMBIO_CARRERA, REINCORPORACION | Tipo de trámite institucional (sin tildes ni espacios). |
| `fechaSolicitud` | **PERSISTIDO** | string (YYYY-MM-DD) | Sí | - | Fecha de presentación del trámite. |
| `motivo` | **PERSISTIDO** | string | Sí | - | Fundamentación o motivo del pedido. |
| `observaciones` | **PERSISTIDO** | string | No | - | Información complementaria del alumno. |
| `estado` | **PERSISTIDO** | string (Enum) | Sí | PENDIENTE, EN_PROCESO, APROBADA, RECHAZADA | Estado administrativo de resolución. |
| `fechaResolucion` | **PERSISTIDO** | string (YYYY-MM-DD) | No | - | Fecha en la que fue resuelta. |
| `respuesta` | **PERSISTIDO** | string | No | - | Dictamen de secretaría académica. |
| `alumnoNombre` | *CALCULADO* | string | No | - | Nombre completo del alumno solicitante. |

**Relaciones:**
- Con `Alumno` (M:1): `idAlumno` -> `idAlumno`

**Pantallas que la consumen:** `BandejaSolicitudesPage.jsx, ConstanciasPage.jsx, PasantiasPage.jsx, SolicitudTituloPage.jsx, SolicitudesAcademicasPage.jsx, CambioCarreraPage.jsx, ReincorporacionPage.jsx`

---

### 1.19 Entidad: `DeclaracionJurada`
**Descripción:** Declaración jurada de asignaturas que el estudiante cursará en el ciclo lectivo bajo compromiso de correlatividad (RF51).

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idDeclaracion` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador único de la declaración jurada (PK). |
| `idAlumno` | **PERSISTIDO** | number (Integer) | Sí | - | FK al Alumno firmante. |
| `idCiclo` | **PERSISTIDO** | number (Integer) | Sí | - | FK al Ciclo Lectivo correspondiente. |
| `fecha` | **PERSISTIDO** | string (YYYY-MM-DD) | Sí | - | Fecha de presentación de la declaración jurada. |
| `materiasDeclaradasIds` | **PERSISTIDO** | array of numbers | Sí | - | Lista de IDs de materias que se compromete a cursar. |
| `estado` | **PERSISTIDO** | string (Enum) | Sí | VALIDADA, PENDIENTE, RECHAZADA | Estado de validación reglamentaria de la DJ. |
| `observaciones` | **PERSISTIDO** | string | No | - | Observaciones o fundamentación de correlatividades. |
| `alumnoNombre` | *CALCULADO* | string | No | - | Nombre completo del alumno. |
| `cicloNombre` | *CALCULADO* | string | No | - | Nombre del ciclo lectivo declarado. |
| `materiasNombres` | *CALCULADO* | array of strings | No | - | Nombres de las materias declaradas derivadas de IDs. |

**Relaciones:**
- Con `Alumno` (M:1): `idAlumno` -> `idAlumno`
- Con `CicloLectivo` (M:1): `idCiclo` -> `idCiclo`

**Pantallas que la consumen:** `DeclaracionJuradaPage.jsx`

---

### 1.20 Entidad: `Documentacion`
**Descripción:** Requisito documental obligatorio o probatorio para el legajo estudiantil.

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idDocumento` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador único del requisito documental (PK). |
| `idAlumno` | **PERSISTIDO** | number (Integer) | Sí | - | FK al Alumno. |
| `tipo` | **PERSISTIDO** | string (Enum) | Sí | DNI, TITULO_SECUNDARIO, APTO_MEDICO, FOTOS | Tipo de documento exigido (sin tildes ni espacios). |
| `obligatorio` | **PERSISTIDO** | boolean | Sí | - | Indica si es indispensable para cursar. |
| `estado` | **PERSISTIDO** | string (Enum) | Sí | ENTREGADO, PENDIENTE, RECHAZADO | Estado de verificación del requisito. |
| `fechaEntrega` | **PERSISTIDO** | string (YYYY-MM-DD) | No | - | Fecha de recepción física o digital. |
| `observaciones` | **PERSISTIDO** | string | No | - | Notas de secretaría (legalizaciones, vencimientos). |
| `alumnoNombre` | *CALCULADO* | string | No | - | Nombre completo del alumno. |

**Relaciones:**
- Con `Alumno` (M:1): `idAlumno` -> `idAlumno`

**Pantallas que la consumen:** `DocumentacionPage.jsx`

---

### 1.21 Entidad: `Usuario`
**Descripción:** Cuenta de usuario del sistema con credenciales y perfil.

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idUsuario` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador único del usuario (PK). |
| `username` | **PERSISTIDO** | string | Sí | - | Nombre de usuario único para acceso. |
| `nombre` | **PERSISTIDO** | string | Sí | - | Nombre y apellido visible. |
| `email` | **PERSISTIDO** | string | Sí | - | Correo institucional o personal. |
| `telefono` | **PERSISTIDO** | string | No | - | Teléfono de contacto. |
| `domicilio` | **PERSISTIDO** | string | No | - | Domicilio de residencia. |
| `rol` | **PERSISTIDO** | string (Enum) | Sí | ADMINISTRADOR, ADMINISTRATIVO, DOCENTE, ALUMNO | Rol operativo y nivel de acceso en el sistema. |
| `estado` | **PERSISTIDO** | string (Enum) | Sí | ACTIVO, INACTIVO | Estado de habilitación de la cuenta. |
| `ultimoAcceso` | **PERSISTIDO** | string (YYYY-MM-DD HH:mm) | No | - | Última fecha y hora de sesión registrada. |

**Relaciones:**
- Con `Rol` (M:1): `rol` -> `nombre`

**Pantallas que la consumen:** `GestionUsuariosPage.jsx, MisDatosPage.jsx, Login.jsx`

---

### 1.22 Entidad: `Rol`
**Descripción:** Rol de seguridad y autorización del sistema.

| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |
| :--- | :---: | :--- | :---: | :--- | :--- |
| `idRol` | **PERSISTIDO** | number (Integer) | Sí | - | Identificador del rol (PK). |
| `nombre` | **PERSISTIDO** | string (Enum) | Sí | ADMINISTRADOR, ADMINISTRATIVO, DOCENTE, ALUMNO | Nombre del rol de usuario. |
| `descripcion` | **PERSISTIDO** | string | No | - | Alcance de permisos del rol en la aplicación. |

**Relaciones:**
- Con `Usuario` (1:N): `nombre` -> `rol`

**Pantallas que la consumen:** `GestionUsuariosPage.jsx, Login.jsx, MisDatosPage.jsx`

---

## 2. Inventario de Endpoints REST

Tabla exhaustiva con la totalidad de los contratos de comunicación consumidos o requeridos por el frontend:

| Método | Ruta | Descripción | Roles Permitidos | Pantallas Consumidoras | Función API | Códigos HTTP | Estado |
| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :---: |
| `GET` | `/api/alumnos` | Obtiene el listado completo de alumnos. | ADMINISTRADOR, ADMINISTRATIVO | GestionAlumnosPage.jsx, GestionInscripcionesPage.jsx, AdminDeudasPage.jsx, AdminPagosPage.jsx, AdminSituacionAlumnoPage.jsx, AsistenciasDocentePage.jsx, CalificacionesDocentePage.jsx, InicioPage.jsx | `getAlumnos` | 200, 401, 403 | **real** |
| `GET` | `/api/alumnos/{id}` | Obtiene un alumno por su ID. | ADMINISTRADOR, ADMINISTRATIVO | GestionAlumnosPage.jsx | `getAlumnoById` | 200, 401, 403, 404 | **real** |
| `POST` | `/api/alumnos` | Crea un nuevo alumno en el sistema. | ADMINISTRADOR, ADMINISTRATIVO | GestionAlumnosPage.jsx | `createAlumno` | 201, 400, 401, 403, 409 | **real** |
| `PUT` | `/api/alumnos/{id}` | Actualiza los datos de un alumno existente. | ADMINISTRADOR, ADMINISTRATIVO | GestionAlumnosPage.jsx | `updateAlumno` | 200, 400, 401, 403, 404 | **real** |
| `DELETE` | `/api/alumnos/{id}` | Realiza la baja lógica de un alumno. | ADMINISTRADOR | GestionAlumnosPage.jsx | `deleteAlumno` | 200, 204, 401, 403, 404 | **real** |
| `GET` | `/api/alumnos/{id}/situacion-academica` | Obtiene la situación académica consolidada del alumno (materias cursadas, regularizadas y notas finales). | ALUMNO, ADMINISTRADOR, ADMINISTRATIVO | SituacionAcademicaArancelariaPage.jsx | `getSituacionAcademica` | 200, 401, 403, 404 | **real** |
| `GET` | `/api/alumnos/{id}/situacion-administrativa` | Obtiene la situación arancelaria y administrativa del alumno (matrícula, cuotas, deudas y pagos). | ADMINISTRATIVO, ADMINISTRADOR, ALUMNO | AdminSituacionAlumnoPage.jsx, SituacionAcademicaArancelariaPage.jsx | `getSituacionAdministrativa` | 200, 401, 403, 404 | **real** |
| `GET` | `/api/docentes` | Obtiene el padrón completo de docentes institucionales. | ADMINISTRADOR, ADMINISTRATIVO | GestionDocentesPage.jsx, GestionAsignacionesPage.jsx | `getDocentes` | 200, 401, 403 | **real** |
| `POST` | `/api/docentes` | Registra un nuevo docente en el cuerpo académico. | ADMINISTRADOR | GestionDocentesPage.jsx | `createDocente` | 201, 400, 401, 403, 409 | **real** |
| `PUT` | `/api/docentes/{id}` | Actualiza los datos de legajo o contacto de un docente. | ADMINISTRADOR | GestionDocentesPage.jsx | `updateDocente` | 200, 400, 401, 403, 404 | **real** |
| `DELETE` | `/api/docentes/{id}` | Realiza la baja lógica de un docente. | ADMINISTRADOR | GestionDocentesPage.jsx | `deleteDocente` | 200, 204, 401, 403, 404 | **real** |
| `GET` | `/api/carreras` | Obtiene el listado de carreras institucionales. | ADMINISTRADOR, ADMINISTRATIVO | GestionCarrerasPage.jsx, GestionAlumnosPage.jsx, GestionCursosPage.jsx, GestionPlanEstudiosPage.jsx | `getCarreras` | 200, 401, 403 | **real** |
| `GET` | `/api/carreras/{id}` | Obtiene los detalles de una carrera específica por su identificador. | ADMINISTRADOR, ADMINISTRATIVO | GestionCarrerasPage.jsx | `getCarreraById` | 200, 401, 403, 404 | **real** |
| `POST` | `/api/carreras` | Registra una nueva carrera en la oferta académica. | ADMINISTRADOR | GestionCarrerasPage.jsx | `createCarrera` | 201, 400, 401, 403, 409 | **real** |
| `PUT` | `/api/carreras/{id}` | Actualiza la información de una carrera existente. | ADMINISTRADOR | GestionCarrerasPage.jsx | `updateCarrera` | 200, 400, 401, 403, 404 | **real** |
| `DELETE` | `/api/carreras/{id}` | Realiza la baja lógica de una carrera académica. | ADMINISTRADOR | GestionCarrerasPage.jsx | `deleteCarrera` | 200, 204, 401, 403, 404 | **real** |
| `GET` | `/api/carreras/{id}/planes` | Obtiene los planes de estudio correspondientes a una carrera. | ADMINISTRADOR, ADMINISTRATIVO | GestionCarrerasPage.jsx | `getCarreraPlanes` | 200, 401, 403, 404 | **real** |
| `GET` | `/api/carreras/{id}/cursos` | Obtiene los cursos o comisiones vinculados a una carrera. | ADMINISTRADOR, ADMINISTRATIVO | GestionCarrerasPage.jsx | `getCarreraCursos` | 200, 401, 403, 404 | **real** |
| `GET` | `/api/planes-estudio` | Obtiene todos los planes de estudio del instituto. | ADMINISTRADOR, ADMINISTRATIVO | GestionPlanEstudiosPage.jsx, GestionAlumnosPage.jsx | `getPlanesEstudio` | 200, 401, 403 | **real** |
| `GET` | `/api/planes-estudio/{id}/materias` | Obtiene las materias de un plan de estudio estructuradas con año y correlativas. | ALUMNO, DOCENTE, ADMINISTRATIVO, ADMINISTRADOR | PlanEstudiosAlumnoPage.jsx, GestionPlanEstudiosPage.jsx | `getMateriasDePlan` | 200, 401, 403, 404 | **real** |
| `GET` | `/api/materias` | Obtiene el catálogo de materias curriculares, con filtros opcionales (idCarrera, idPlan). | ADMINISTRADOR, DOCENTE, ALUMNO | GestionMateriasPage.jsx, GestionPlanEstudiosPage.jsx, GestionAsignacionesPage.jsx, GestionMesasPage.jsx, PlanEstudiosAlumnoPage.jsx, AsistenciasDocentePage.jsx, CalificacionesDocentePage.jsx, EvaluacionesDocentePage.jsx, DeclaracionJuradaPage.jsx | `getMaterias` | 200, 401, 403 | **real** |
| `POST` | `/api/materias` | Registra una nueva materia en el plan de estudios. | ADMINISTRADOR | GestionMateriasPage.jsx | `createMateria` | 201, 400, 401, 403, 409 | **real** |
| `PUT` | `/api/materias/{id}` | Actualiza los datos o régimen de correlatividades de una materia. | ADMINISTRADOR | GestionMateriasPage.jsx, GestionPlanEstudiosPage.jsx | `updateMateria` | 200, 400, 401, 403, 404 | **real** |
| `GET` | `/api/cursos` | Obtiene el listado de comisiones y cursos habilitados. | ADMINISTRADOR, DOCENTE, ADMINISTRATIVO | GestionCursosPage.jsx, GestionInscripcionesPage.jsx, GestionAsignacionesPage.jsx, MisCursosPage.jsx, AsistenciasDocentePage.jsx, CalificacionesDocentePage.jsx, EvaluacionesDocentePage.jsx | `getCursos` | 200, 401, 403 | **real** |
| `POST` | `/api/cursos` | Registra una nueva comisión / curso con control de cupos y carrera asociada. | ADMINISTRADOR | GestionCursosPage.jsx | `createCurso` | 201, 400, 401, 403, 409 | **real** |
| `PUT` | `/api/cursos/{id}` | Actualiza los datos de un curso o comisión. | ADMINISTRADOR | GestionCursosPage.jsx | `updateCurso` | 200, 400, 401, 403, 404 | **real** |
| `GET` | `/api/ciclos-lectivos` | Obtiene el catálogo de ciclos lectivos. | ADMINISTRADOR, ADMINISTRATIVO | GestionCiclosLectivosPage.jsx, GestionInscripcionesPage.jsx | `getCiclosLectivos` | 200, 401, 403 | **real** |
| `POST` | `/api/ciclos-lectivos` | Registra un nuevo ciclo lectivo anual. | ADMINISTRADOR | GestionCiclosLectivosPage.jsx | `createCicloLectivo` | 201, 400, 401, 403, 409 | **real** |
| `PUT` | `/api/ciclos-lectivos/{id}` | Actualiza los datos o estado de un ciclo lectivo existente. | ADMINISTRADOR | GestionCiclosLectivosPage.jsx | `updateCicloLectivo` | 200, 400, 401, 403, 404 | **real** |
| `GET` | `/api/asignaciones` | Obtiene el listado de asignaciones docentes a cursos y materias. | ADMINISTRADOR, ADMINISTRATIVO, DOCENTE | GestionAsignacionesPage.jsx, MisCursosPage.jsx | `getAsignaciones` | 200, 401, 403 | **real** |
| `POST` | `/api/asignaciones` | Registra una nueva asignación docente a una cátedra y curso. | ADMINISTRADOR, ADMINISTRATIVO | GestionAsignacionesPage.jsx | `createAsignacion` | 201, 400, 401, 403, 409 | **real** |
| `DELETE` | `/api/asignaciones/{id}` | Elimina una asignación docente existente. | ADMINISTRADOR, ADMINISTRATIVO | GestionAsignacionesPage.jsx | `deleteAsignacion` | 200, 204, 401, 403, 404 | **real** |
| `GET` | `/api/inscripciones` | Obtiene el registro de inscripciones de alumnos a comisiones y cursos. | ADMINISTRADOR, ADMINISTRATIVO | GestionInscripcionesPage.jsx | `getInscripciones` | 200, 401, 403 | **real** |
| `POST` | `/api/inscripciones` | Realiza la inscripción de un alumno a una comisión/curso con control de cupos disponibles. | ADMINISTRADOR, ADMINISTRATIVO | GestionInscripcionesPage.jsx | `createInscripcion` | 201, 400, 401, 403, 409 | **real** |
| `DELETE` | `/api/inscripciones/{id}` | Cancela una inscripción de un alumno y libera el cupo correspondiente en la comisión. | ADMINISTRADOR, ADMINISTRATIVO | GestionInscripcionesPage.jsx | `cancelarInscripcion` | 200, 204, 401, 403, 404 | **real** |
| `GET` | `/api/matriculas` | Obtiene el padrón de matrículas anuales registradas. | ADMINISTRADOR, ADMINISTRATIVO | AdminMatriculasPage.jsx, CancelarMatriculaPage.jsx | `getMatriculas` | 200, 401, 403 | **real** |
| `PUT` | `/api/matriculas/{id}` | Actualiza el estado o información de una matrícula anual. | ADMINISTRATIVO, ADMINISTRADOR | AdminMatriculasPage.jsx | `updateMatricula` | 200, 400, 401, 403, 404 | **real** |
| `PATCH` | `/api/matriculas/{id}` | Registra la cancelación definitiva de matrícula de un alumno (RF27). | ALUMNO, ADMINISTRATIVO, ADMINISTRADOR | CancelarMatriculaPage.jsx | `cancelarMatricula` | 200, 400, 401, 403, 404 | **real** |
| `POST` | `/api/matriculas/{id}/cuotas` | Genera el plan de 12 cuotas mensuales y 3 seguros cuatrimestrales para una matrícula individual. | ADMINISTRATIVO, ADMINISTRADOR | AdminMatriculasPage.jsx | `generarCuotas` | 200, 401, 403, 404, 409 | **real** |
| `GET` | `/api/cuotas` | Enriquecer cuota con campos calculados derivados de la matrícula (idAlumno, alumnoNombre) | ADMINISTRATIVO, ADMINISTRADOR, ALUMNO | AdminCuotasPage.jsx, AdminDeudasPage.jsx, AdminPagosPage.jsx, SituacionAcademicaArancelariaPage.jsx | `getCuotas` | 200, 401, 403 | **real** |
| `PATCH` | `/api/cuotas/{id}/anular` | Anula una cuota administrativa (solo para administradores). | ADMINISTRADOR | AdminCuotasPage.jsx | `anularCuota` | 200, 401, 403, 404, 409 | **real** |
| `GET` | `/api/pagos` | Obtiene el historial de pagos registrados con filtros opcionales. | ADMINISTRATIVO, ADMINISTRADOR, ALUMNO | AdminPagosPage.jsx, SituacionAcademicaArancelariaPage.jsx | `getPagos` | 200, 401, 403 | **real** |
| `POST` | `/api/pagos` | Registra un nuevo pago arancelario por el importe total de la cuota (RN09). | ADMINISTRATIVO, ADMINISTRADOR | AdminPagosPage.jsx | `registrarPago` | 201, 400, 401, 403, 409 | **real** |
| `GET` | `/api/evaluaciones` | Obtiene el listado de evaluaciones planificadas por cátedra (parciales, trabajos prácticos). | DOCENTE, ADMINISTRADOR | EvaluacionesDocentePage.jsx, CalificacionesDocentePage.jsx | `getEvaluaciones` | 200, 401, 403 | **real** |
| `POST` | `/api/evaluaciones` | Registra una nueva evaluación creada por un docente (parcial o TP integrador). | DOCENTE | EvaluacionesDocentePage.jsx | `createEvaluacion` | 201, 400, 401, 403, 409 | **real** |
| `POST` | `/api/evaluaciones/{idEvaluacion}/calificaciones` | Guarda las calificaciones de una evaluación en lote (atómico, todo o nada). | DOCENTE | CalificacionesDocentePage.jsx | `guardarCalificaciones` | 200, 400, 401, 403, 404 | **real** |
| `GET` | `/api/calificaciones` | Obtiene el listado de calificaciones según filtros opcionales (idAlumno, tipo, idMateria). | ALUMNO, DOCENTE, ADMINISTRADOR | CalificacionesDocentePage.jsx, CalificacionesAlumnoPage.jsx, SituacionAcademicaArancelariaPage.jsx | `getCalificaciones` | 200, 401, 403 | **real** |
| `POST` | `/api/calificaciones` | Registra o actualiza la calificación individual de un alumno. | DOCENTE, ADMINISTRADOR | CalificacionesDocentePage.jsx | `saveCalificacion` | 200, 400, 401, 403 | **real** |
| `GET` | `/api/asistencias` | Obtiene el registro de asistencias con filtros opcionales (idAlumno, idCurso, fecha). | DOCENTE, ALUMNO, ADMINISTRADOR | AsistenciasDocentePage.jsx, AsistenciasAlumnoPage.jsx | `getAsistencias` | 200, 401, 403 | **real** |
| `POST` | `/api/asistencias/lote` | Registra o actualiza la asistencia de una lista de alumnos en lote. | DOCENTE | AsistenciasDocentePage.jsx | `registrarAsistencias` | 200, 400, 401, 403 | **real** |
| `GET` | `/api/mesas-examen` | Obtiene el listado de mesas de examen final programadas. | ALUMNO, DOCENTE, ADMINISTRATIVO, ADMINISTRADOR | MesasExamenPage.jsx, InscripcionMesasPage.jsx, GestionMesasPage.jsx | `getMesasExamen` | 200, 401, 403 | **real** |
| `POST` | `/api/mesas-examen/{id}/inscripciones` | Inscribe a un alumno en una mesa de examen final (RF49). | ALUMNO | InscripcionMesasPage.jsx | `inscribirseMesa` | 200, 401, 403, 404, 409 | **real** |
| `DELETE` | `/api/mesas-examen/{id}/inscripciones/{idInscripcion}` | Da de baja la inscripción a una mesa de examen final (RF50). | ALUMNO | InscripcionMesasPage.jsx | `darseDeBajaMesa` | 200, 401, 403, 404 | **real** |
| `POST` | `/api/mesas-examen` | Programa una nueva mesa de examen final en secretaría académica. | ADMINISTRADOR | GestionMesasPage.jsx | `createMesa` | 201, 400, 401, 403, 409 | **real** |
| `PUT` | `/api/mesas-examen/{id}` | Actualiza los datos o estado de una mesa de examen final existente. | ADMINISTRADOR | GestionMesasPage.jsx | `updateMesa` | 200, 400, 401, 403, 404 | **real** |
| `GET` | `/api/solicitudes` | Obtiene el listado de solicitudes y trámites institucionales con filtros opcionales. | ADMINISTRATIVO, ADMINISTRADOR, ALUMNO | BandejaSolicitudesPage.jsx, ConstanciasPage.jsx, InicioPage.jsx, PasantiasPage.jsx, ReincorporacionPage.jsx, SolicitudTituloPage.jsx, SolicitudesAcademicasPage.jsx, CambioCarreraPage.jsx | `getSolicitudes` | 200, 401, 403 | **real** |
| `POST` | `/api/solicitudes` | Genera una nueva solicitud o trámite del estudiante (constancias, DDJJ, pasantías, título, académicas). | ALUMNO | CambioCarreraPage.jsx, ConstanciasPage.jsx, PasantiasPage.jsx, ReincorporacionPage.jsx, SolicitudTituloPage.jsx, SolicitudesAcademicasPage.jsx | `createSolicitud` | 201, 400, 401, 403 | **real** |
| `PATCH` | `/api/solicitudes/{id}` | Resuelve una solicitud en la bandeja administrativa (APROBADA o RECHAZADA). | ADMINISTRATIVO, ADMINISTRADOR | BandejaSolicitudesPage.jsx | `resolverSolicitud` | 200, 400, 401, 403, 404 | **real** |
| `GET` | `/api/declaraciones-juradas` | Obtiene el listado de declaraciones juradas de materias a cursar. | ALUMNO, ADMINISTRADOR, ADMINISTRATIVO | DeclaracionJuradaPage.jsx | `getDeclaracionesJuradas` | 200, 401, 403 | **real** |
| `POST` | `/api/declaraciones-juradas` | Presenta una nueva declaración jurada de materias (RF51). | ALUMNO | DeclaracionJuradaPage.jsx | `createDeclaracionJurada` | 201, 400, 401, 403 | **real** |
| `GET` | `/api/documentacion` | Obtiene el estado de presentación de la documentación requerida para el legajo. | ALUMNO, ADMINISTRATIVO, ADMINISTRADOR | DocumentacionPage.jsx | `getDocumentacion` | 200, 401, 403 | **real** |
| `PUT` | `/api/documentacion/{id}` | Actualiza el estado de entrega o verificación de un requisito documental. | ADMINISTRATIVO, ADMINISTRADOR | DocumentacionPage.jsx | `updateDocumentacion` | 200, 400, 401, 403, 404 | **real** |
| `POST` | `/api/auth/login` | Inicia sesión con usuario y contraseña. | ALUMNO, DOCENTE, ADMINISTRATIVO, ADMINISTRADOR | Login.jsx | `loginApi` | 200, 400, 401, 404 | **real** |
| `POST` | `/api/auth/logout` | Cierra la sesión activa en el sistema. | ALUMNO, DOCENTE, ADMINISTRATIVO, ADMINISTRADOR | Topbar.jsx | `logoutApi` | 200, 401 | **real** |
| `POST` | `/api/auth/cambiar-password` | Modifica la contraseña del usuario en sesión. | ALUMNO, DOCENTE, ADMINISTRATIVO, ADMINISTRADOR | CambiarPasswordPage.jsx | `cambiarPasswordApi` | 200, 400, 401 | **real** |
| `POST` | `/api/auth/registro` | Registra a un nuevo alumno en el sistema. | ALUMNO, DOCENTE, ADMINISTRATIVO, ADMINISTRADOR | Registro.jsx | `registroApi` | 201, 400, 409 | **real** |
| `GET` | `/api/auth/carreras` | Obtiene las carreras disponibles para aspirantes y nuevos estudiantes (público). | ALUMNO, DOCENTE, ADMINISTRATIVO, ADMINISTRADOR | Registro.jsx | `getOfertaCarrerasApi` | 200 | **real** |
| `GET` | `/api/auth/planes-estudio` | Obtiene los planes de estudio disponibles para aspirantes y nuevos estudiantes (público). | ALUMNO, DOCENTE, ADMINISTRATIVO, ADMINISTRADOR | Registro.jsx | `getOfertaPlanesApi` | 200 | **real** |
| `GET` | `/api/usuarios` | Obtiene el listado de cuentas de usuario y roles del sistema. | ADMINISTRADOR | GestionUsuariosPage.jsx | `getUsuarios` | 200, 401, 403 | **real** |
| `POST` | `/api/usuarios` | Registra un nuevo usuario con credenciales y asignación de rol. | ADMINISTRADOR | GestionUsuariosPage.jsx | `createUsuario` | 201, 400, 401, 403, 409 | **real** |
| `PUT` | `/api/usuarios/{id}` | Actualiza los datos de perfil o rol de un usuario existente. | ADMINISTRADOR | GestionUsuariosPage.jsx | `updateUsuario` | 200, 400, 401, 403, 404 | **real** |
| `DELETE` | `/api/usuarios/{id}` | Realiza la baja lógica de un usuario del sistema. | ADMINISTRADOR | GestionUsuariosPage.jsx | `deleteUsuario` | 200, 204, 401, 403, 404 | **real** |
| `GET` | `/api/usuarios/me` | Obtiene los datos del perfil del usuario en sesión. | ALUMNO, DOCENTE, ADMINISTRATIVO, ADMINISTRADOR | MisDatosPage.jsx | `getMiPerfil` | 200, 401 | **real** |
| `PUT` | `/api/usuarios/me` | Actualiza los datos de contacto y filiación del perfil en sesión. | ALUMNO, DOCENTE, ADMINISTRATIVO, ADMINISTRADOR | MisDatosPage.jsx | `actualizarMiPerfil` | 200, 400, 401 | **real** |

