-- Script de creacion de la base de datos (PostgreSQL)

-- Convenciones de diseño del esquema:
-- - Nombres en minusculas con snake_case (para tablas, columnas, indices y restricciones).
-- - Claves primarias auto-incrementales con GENERATED ALWAYS AS IDENTITY.
-- - Importes monetarios con NUMERIC(10,2).
-- - Valores booleanos nativos (BOOLEAN).
-- - Restricciones CHECK y UNIQUE explicitas en el esquema.
--
-- Fuente: docs/entregables/entregable-2/der.md y docs/frontend/entidades.json (32 tablas: 25 del modelo + 7 catalogos).

DROP SCHEMA IF EXISTS public CASCADE;
CREATE SCHEMA public;

-- ============================================================
-- Usuarios y acceso (RF01-RF05, RN17)
-- ============================================================

CREATE TABLE rol (
    id_rol  INTEGER GENERATED ALWAYS AS IDENTITY,
    nombre  VARCHAR(30) NOT NULL,
    CONSTRAINT pk_rol PRIMARY KEY (id_rol),
    CONSTRAINT uk_rol_nombre UNIQUE (nombre)
);

-- ============================================================
-- Tablas catalogo (id + nombre)
-- Reemplazan a los enums: agregar o cambiar un valor es un INSERT/UPDATE
-- y no requiere modificar ni recompilar el codigo.
-- ============================================================

-- Estados de alumnos y docentes (ACTIVO, INACTIVO)
CREATE TABLE estado_persona (
    id_estado_persona INTEGER GENERATED ALWAYS AS IDENTITY,
    nombre VARCHAR(30) NOT NULL,
    CONSTRAINT pk_estado_persona PRIMARY KEY (id_estado_persona),
    CONSTRAINT uk_estado_persona_nombre UNIQUE (nombre)
);

-- Estados de una matricula (PENDIENTE, ACTIVA, CANCELADA)
CREATE TABLE estado_matricula (
    id_estado_matricula INTEGER GENERATED ALWAYS AS IDENTITY,
    nombre VARCHAR(30) NOT NULL,
    CONSTRAINT pk_estado_matricula PRIMARY KEY (id_estado_matricula),
    CONSTRAINT uk_estado_matricula_nombre UNIQUE (nombre)
);

-- Estados de una cuota (PENDIENTE, PAGADA, VENCIDA, ANULADA)
CREATE TABLE estado_cuota (
    id_estado_cuota INTEGER GENERATED ALWAYS AS IDENTITY,
    nombre VARCHAR(30) NOT NULL,
    CONSTRAINT pk_estado_cuota PRIMARY KEY (id_estado_cuota),
    CONSTRAINT uk_estado_cuota_nombre UNIQUE (nombre)
);

-- Tipos de cuota (MENSUAL, SEGURO)
CREATE TABLE tipo_cuota (
    id_tipo_cuota INTEGER GENERATED ALWAYS AS IDENTITY,
    nombre VARCHAR(30) NOT NULL,
    CONSTRAINT pk_tipo_cuota PRIMARY KEY (id_tipo_cuota),
    CONSTRAINT uk_tipo_cuota_nombre UNIQUE (nombre)
);

-- Medios de pago (EFECTIVO, TRANSFERENCIA, TARJETA, OTRO)
CREATE TABLE medio_pago (
    id_medio_pago INTEGER GENERATED ALWAYS AS IDENTITY,
    nombre VARCHAR(30) NOT NULL,
    CONSTRAINT pk_medio_pago PRIMARY KEY (id_medio_pago),
    CONSTRAINT uk_medio_pago_nombre UNIQUE (nombre)
);

-- Tipos de evaluacion (PRIMER_PARCIAL, SEGUNDO_PARCIAL, ...)
CREATE TABLE tipo_evaluacion (
    id_tipo_evaluacion INTEGER GENERATED ALWAYS AS IDENTITY,
    nombre VARCHAR(30) NOT NULL,
    CONSTRAINT pk_tipo_evaluacion PRIMARY KEY (id_tipo_evaluacion),
    CONSTRAINT uk_tipo_evaluacion_nombre UNIQUE (nombre)
);

-- Estados de asistencia (PRESENTE, AUSENTE, TARDANZA, JUSTIFICADO)
CREATE TABLE estado_asistencia (
    id_estado_asistencia INTEGER GENERATED ALWAYS AS IDENTITY,
    nombre VARCHAR(30) NOT NULL,
    CONSTRAINT pk_estado_asistencia PRIMARY KEY (id_estado_asistencia),
    CONSTRAINT uk_estado_asistencia_nombre UNIQUE (nombre)
);

-- ============================================================
-- Carreras y planes de estudio (RF47-RF48)
-- ============================================================

CREATE TABLE carrera (
    id_carrera     INTEGER GENERATED ALWAYS AS IDENTITY,
    nombre         VARCHAR(100) NOT NULL,
    duracion_anios INTEGER      NOT NULL,
    activa         BOOLEAN      NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_carrera PRIMARY KEY (id_carrera),
    CONSTRAINT uk_carrera_nombre UNIQUE (nombre),
    CONSTRAINT ck_carrera_duracion CHECK (duracion_anios > 0)
);

CREATE TABLE plan_estudio (
    id_plan       INTEGER GENERATED ALWAYS AS IDENTITY,
    id_carrera    INTEGER      NOT NULL,
    nombre        VARCHAR(100) NOT NULL,
    anio_vigencia INTEGER      NOT NULL,
    CONSTRAINT pk_plan_estudio PRIMARY KEY (id_plan),
    CONSTRAINT fk_plan_carrera FOREIGN KEY (id_carrera) REFERENCES carrera (id_carrera),
    CONSTRAINT uk_plan_carrera_nombre UNIQUE (id_carrera, nombre),
    CONSTRAINT ck_plan_anio CHECK (anio_vigencia >= 1900)
);

-- ============================================================
-- Alumnos y docentes (RF06-RF13, RN13)
-- ============================================================

CREATE TABLE alumno (
    id_alumno        INTEGER GENERATED ALWAYS AS IDENTITY,
    dni              VARCHAR(10)  NOT NULL,
    apellido         VARCHAR(60)  NOT NULL,
    nombre           VARCHAR(60)  NOT NULL,
    fecha_nacimiento DATE         NOT NULL,
    domicilio        VARCHAR(120),
    telefono         VARCHAR(20),
    email            VARCHAR(100) NOT NULL,
    fecha_ingreso    DATE         NOT NULL DEFAULT CURRENT_DATE,
    id_estado_persona INTEGER     NOT NULL,
    id_plan          INTEGER      NOT NULL,
    CONSTRAINT pk_alumno PRIMARY KEY (id_alumno),
    CONSTRAINT uk_alumno_dni UNIQUE (dni),
    CONSTRAINT fk_alumno_plan FOREIGN KEY (id_plan) REFERENCES plan_estudio (id_plan),
    CONSTRAINT fk_alumno_estado FOREIGN KEY (id_estado_persona) REFERENCES estado_persona (id_estado_persona),
    CONSTRAINT ck_alumno_nacimiento CHECK (fecha_nacimiento < fecha_ingreso)
);

CREATE TABLE docente (
    id_docente INTEGER GENERATED ALWAYS AS IDENTITY,
    dni        VARCHAR(10)  NOT NULL,
    apellido   VARCHAR(60)  NOT NULL,
    nombre     VARCHAR(60)  NOT NULL,
    telefono   VARCHAR(20),
    email      VARCHAR(100) NOT NULL,
    titulo     VARCHAR(100),
    id_estado_persona INTEGER   NOT NULL,
    CONSTRAINT pk_docente PRIMARY KEY (id_docente),
    CONSTRAINT uk_docente_dni UNIQUE (dni),
    CONSTRAINT fk_docente_estado FOREIGN KEY (id_estado_persona) REFERENCES estado_persona (id_estado_persona)
);

CREATE TABLE usuario (
    id_usuario    INTEGER GENERATED ALWAYS AS IDENTITY,
    username      VARCHAR(50)  NOT NULL,
    password_hash VARCHAR(100) NOT NULL,   -- BCrypt (RNF01)
    activo        BOOLEAN      NOT NULL DEFAULT TRUE,
    id_rol        INTEGER      NOT NULL,   -- un unico rol por usuario (RN17)
    id_alumno     INTEGER,
    id_docente    INTEGER,
    CONSTRAINT pk_usuario PRIMARY KEY (id_usuario),
    CONSTRAINT uk_usuario_username UNIQUE (username),
    CONSTRAINT uk_usuario_alumno UNIQUE (id_alumno),
    CONSTRAINT uk_usuario_docente UNIQUE (id_docente),
    CONSTRAINT fk_usuario_rol FOREIGN KEY (id_rol) REFERENCES rol (id_rol),
    CONSTRAINT fk_usuario_alumno FOREIGN KEY (id_alumno) REFERENCES alumno (id_alumno),
    CONSTRAINT fk_usuario_docente FOREIGN KEY (id_docente) REFERENCES docente (id_docente),
    CONSTRAINT ck_usuario_persona CHECK (id_alumno IS NULL OR id_docente IS NULL)
);

-- ============================================================
-- Ciclos, cursos y materias (RF14-RF21)
-- ============================================================

CREATE TABLE ciclo_lectivo (
    id_ciclo     INTEGER GENERATED ALWAYS AS IDENTITY,
    anio         INTEGER NOT NULL,
    nombre       VARCHAR(60) NOT NULL,
    estado       VARCHAR(15) NOT NULL DEFAULT 'PLANIFICADO',
    fecha_inicio DATE    NOT NULL,
    fecha_fin    DATE    NOT NULL,
    CONSTRAINT pk_ciclo_lectivo PRIMARY KEY (id_ciclo),
    CONSTRAINT uk_ciclo_anio UNIQUE (anio),
    CONSTRAINT ck_ciclo_estado CHECK (estado IN ('ACTIVO', 'CERRADO', 'PLANIFICADO')),
    CONSTRAINT ck_ciclo_fechas CHECK (fecha_fin > fecha_inicio)
);

CREATE TABLE curso (
    id_curso         INTEGER GENERATED ALWAYS AS IDENTITY,
    nombre           VARCHAR(30) NOT NULL,
    turno            VARCHAR(10) NOT NULL DEFAULT 'MANANA',
    capacidad_maxima INTEGER     NOT NULL,   -- RN03
    id_ciclo         INTEGER     NOT NULL,
    id_carrera       INTEGER     NOT NULL,
    CONSTRAINT pk_curso PRIMARY KEY (id_curso),
    CONSTRAINT fk_curso_ciclo FOREIGN KEY (id_ciclo) REFERENCES ciclo_lectivo (id_ciclo),
    CONSTRAINT fk_curso_carrera FOREIGN KEY (id_carrera) REFERENCES carrera (id_carrera),
    CONSTRAINT uk_curso_nombre UNIQUE (nombre, id_ciclo, id_carrera),
    CONSTRAINT ck_curso_capacidad CHECK (capacidad_maxima > 0),
    CONSTRAINT ck_curso_turno CHECK (turno IN ('MANANA', 'TARDE', 'NOCHE'))
);

CREATE TABLE materia (
    id_materia INTEGER GENERATED ALWAYS AS IDENTITY,
    nombre     VARCHAR(80) NOT NULL,
    activa     BOOLEAN     NOT NULL DEFAULT TRUE,
    CONSTRAINT pk_materia PRIMARY KEY (id_materia),
    CONSTRAINT uk_materia_nombre UNIQUE (nombre)
);

CREATE TABLE plan_materia (
    id_plan    INTEGER NOT NULL,
    id_materia INTEGER NOT NULL,
    anio_plan  INTEGER NOT NULL,
    CONSTRAINT pk_plan_materia PRIMARY KEY (id_plan, id_materia),
    CONSTRAINT fk_plan_materia_plan FOREIGN KEY (id_plan) REFERENCES plan_estudio (id_plan),
    CONSTRAINT fk_plan_materia_materia FOREIGN KEY (id_materia) REFERENCES materia (id_materia),
    CONSTRAINT ck_plan_materia_anio CHECK (anio_plan > 0)
);

CREATE TABLE correlativa (
    id_plan              INTEGER NOT NULL,
    id_materia           INTEGER NOT NULL,
    id_materia_requerida INTEGER NOT NULL,
    CONSTRAINT pk_correlativa PRIMARY KEY (id_plan, id_materia, id_materia_requerida),
    CONSTRAINT fk_correlativa_materia FOREIGN KEY (id_plan, id_materia)
        REFERENCES plan_materia (id_plan, id_materia),
    CONSTRAINT fk_correlativa_requerida FOREIGN KEY (id_plan, id_materia_requerida)
        REFERENCES plan_materia (id_plan, id_materia),
    CONSTRAINT ck_correlativa_distinta CHECK (id_materia <> id_materia_requerida)
);

-- Asignacion de docente a una materia dentro de un curso (RF18-RF19, RN11)
CREATE TABLE curso_materia (
    id_curso_materia INTEGER GENERATED ALWAYS AS IDENTITY,
    id_curso         INTEGER NOT NULL,
    id_materia       INTEGER NOT NULL,
    id_docente       INTEGER NOT NULL,
    rol_docente      VARCHAR(20) NOT NULL DEFAULT 'TITULAR',
    CONSTRAINT pk_curso_materia PRIMARY KEY (id_curso_materia),
    CONSTRAINT fk_curso_materia_curso FOREIGN KEY (id_curso) REFERENCES curso (id_curso),
    CONSTRAINT fk_curso_materia_materia FOREIGN KEY (id_materia) REFERENCES materia (id_materia),
    CONSTRAINT fk_curso_materia_docente FOREIGN KEY (id_docente) REFERENCES docente (id_docente),
    CONSTRAINT uk_curso_materia_docente UNIQUE (id_curso, id_materia, id_docente),
    CONSTRAINT ck_curso_materia_rol CHECK (rol_docente IN ('TITULAR', 'ADJUNTO', 'JTP'))
);

-- ============================================================
-- Inscripciones y matriculas (RF22-RF27)
-- ============================================================

CREATE TABLE inscripcion (
    id_inscripcion INTEGER GENERATED ALWAYS AS IDENTITY,
    id_alumno      INTEGER NOT NULL,
    id_curso       INTEGER NOT NULL,
    id_ciclo       INTEGER NOT NULL,
    fecha          DATE    NOT NULL DEFAULT CURRENT_DATE,
    activa         BOOLEAN NOT NULL DEFAULT TRUE,   -- FALSE = anulada (RF24)
    CONSTRAINT pk_inscripcion PRIMARY KEY (id_inscripcion),
    CONSTRAINT fk_inscripcion_alumno FOREIGN KEY (id_alumno) REFERENCES alumno (id_alumno),
    CONSTRAINT fk_inscripcion_curso FOREIGN KEY (id_curso) REFERENCES curso (id_curso),
    CONSTRAINT fk_inscripcion_ciclo FOREIGN KEY (id_ciclo) REFERENCES ciclo_lectivo (id_ciclo)
);
-- RN01 y RN02: unicidad solo entre inscripciones activas, asi una anulada
-- no impide volver a inscribirse.
CREATE UNIQUE INDEX uk_inscripcion_alumno_curso_ciclo
    ON inscripcion (id_alumno, id_curso, id_ciclo) WHERE activa;
CREATE UNIQUE INDEX uk_inscripcion_alumno_ciclo
    ON inscripcion (id_alumno, id_ciclo) WHERE activa;

CREATE TABLE matricula (
    id_matricula INTEGER GENERATED ALWAYS AS IDENTITY,
    id_alumno    INTEGER       NOT NULL,
    id_ciclo     INTEGER       NOT NULL,
    fecha        DATE          NOT NULL DEFAULT CURRENT_DATE,
    importe      NUMERIC(10,2),
    id_estado_matricula INTEGER NOT NULL,
    CONSTRAINT pk_matricula PRIMARY KEY (id_matricula),
    CONSTRAINT fk_matricula_alumno FOREIGN KEY (id_alumno) REFERENCES alumno (id_alumno),
    CONSTRAINT fk_matricula_ciclo FOREIGN KEY (id_ciclo) REFERENCES ciclo_lectivo (id_ciclo),
    CONSTRAINT uk_matricula_alumno_ciclo UNIQUE (id_alumno, id_ciclo),   -- RN05
    CONSTRAINT fk_matricula_estado FOREIGN KEY (id_estado_matricula) REFERENCES estado_matricula (id_estado_matricula),
    CONSTRAINT ck_matricula_importe CHECK (importe IS NULL OR importe >= 0)
);

-- ============================================================
-- Cuotas y pagos (RF28-RF35, RF46)
-- ============================================================

CREATE TABLE cuota (
    id_cuota          INTEGER GENERATED ALWAYS AS IDENTITY,
    id_matricula      INTEGER       NOT NULL,   -- RN06: no hay cuotas sueltas
    numero            INTEGER       NOT NULL,
    id_tipo_cuota     INTEGER       NOT NULL,
    fecha_vencimiento DATE          NOT NULL,
    importe           NUMERIC(10,2) NOT NULL,
    id_estado_cuota   INTEGER       NOT NULL,
    CONSTRAINT pk_cuota PRIMARY KEY (id_cuota),
    CONSTRAINT fk_cuota_matricula FOREIGN KEY (id_matricula) REFERENCES matricula (id_matricula),
    CONSTRAINT uk_cuota UNIQUE (id_matricula, numero, id_tipo_cuota),
    CONSTRAINT ck_cuota_importe CHECK (importe > 0),
    CONSTRAINT ck_cuota_numero CHECK (numero BETWEEN 1 AND 12),
    CONSTRAINT fk_cuota_tipo FOREIGN KEY (id_tipo_cuota) REFERENCES tipo_cuota (id_tipo_cuota),
    CONSTRAINT fk_cuota_estado FOREIGN KEY (id_estado_cuota) REFERENCES estado_cuota (id_estado_cuota)
    -- RN18 (12 cuotas CUOTA_MENSUAL y 3 SEGURO_CUATRIMESTRAL por matricula): validado en el Service.
);

CREATE TABLE pago (
    id_pago    INTEGER GENERATED ALWAYS AS IDENTITY,
    id_cuota   INTEGER       NOT NULL,
    id_usuario INTEGER       NOT NULL,   -- quien registro el pago (RNF12)
    fecha      DATE          NOT NULL DEFAULT CURRENT_DATE,
    importe    NUMERIC(10,2) NOT NULL,
    id_medio_pago INTEGER    NOT NULL,
    comprobante VARCHAR(11)  NOT NULL,
    CONSTRAINT pk_pago PRIMARY KEY (id_pago),
    CONSTRAINT uk_pago_cuota UNIQUE (id_cuota),   -- RN08: una cuota, un unico pago
    CONSTRAINT uk_pago_comprobante UNIQUE (comprobante),
    CONSTRAINT fk_pago_cuota FOREIGN KEY (id_cuota) REFERENCES cuota (id_cuota),
    CONSTRAINT fk_pago_usuario FOREIGN KEY (id_usuario) REFERENCES usuario (id_usuario),
    CONSTRAINT ck_pago_importe CHECK (importe > 0),
    CONSTRAINT fk_pago_medio FOREIGN KEY (id_medio_pago) REFERENCES medio_pago (id_medio_pago)
);

-- ============================================================
-- Evaluaciones, calificaciones y asistencias (RF36-RF45)
-- ============================================================

CREATE TABLE evaluacion (
    id_evaluacion    INTEGER GENERATED ALWAYS AS IDENTITY,
    id_curso_materia INTEGER      NOT NULL,
    fecha            DATE         NOT NULL,
    id_tipo_evaluacion INTEGER    NOT NULL,
    descripcion      VARCHAR(200),
    CONSTRAINT pk_evaluacion PRIMARY KEY (id_evaluacion),
    CONSTRAINT fk_evaluacion_curso_materia FOREIGN KEY (id_curso_materia)
        REFERENCES curso_materia (id_curso_materia),
    CONSTRAINT fk_evaluacion_tipo FOREIGN KEY (id_tipo_evaluacion) REFERENCES tipo_evaluacion (id_tipo_evaluacion)
);

CREATE TABLE calificacion (
    id_calificacion INTEGER GENERATED ALWAYS AS IDENTITY,
    id_evaluacion   INTEGER NOT NULL,
    id_alumno       INTEGER NOT NULL,
    nota            INTEGER NOT NULL,
    CONSTRAINT pk_calificacion PRIMARY KEY (id_calificacion),
    CONSTRAINT fk_calificacion_evaluacion FOREIGN KEY (id_evaluacion) REFERENCES evaluacion (id_evaluacion),
    CONSTRAINT fk_calificacion_alumno FOREIGN KEY (id_alumno) REFERENCES alumno (id_alumno),
    CONSTRAINT uk_calificacion UNIQUE (id_evaluacion, id_alumno),
    CONSTRAINT ck_calificacion_nota CHECK (nota BETWEEN 1 AND 10)   -- RN14
);

CREATE TABLE asistencia (
    id_asistencia INTEGER GENERATED ALWAYS AS IDENTITY,
    id_alumno     INTEGER     NOT NULL,
    id_curso      INTEGER     NOT NULL,
    id_materia    INTEGER     NOT NULL,
    fecha         DATE        NOT NULL,
    id_estado_asistencia INTEGER NOT NULL,
    CONSTRAINT pk_asistencia PRIMARY KEY (id_asistencia),
    CONSTRAINT fk_asistencia_alumno FOREIGN KEY (id_alumno) REFERENCES alumno (id_alumno),
    CONSTRAINT fk_asistencia_curso FOREIGN KEY (id_curso) REFERENCES curso (id_curso),
    CONSTRAINT fk_asistencia_materia FOREIGN KEY (id_materia) REFERENCES materia (id_materia),
    CONSTRAINT uk_asistencia UNIQUE (id_alumno, id_curso, id_materia, fecha),   -- RN15
    CONSTRAINT fk_asistencia_estado FOREIGN KEY (id_estado_asistencia) REFERENCES estado_asistencia (id_estado_asistencia)
);

-- ============================================================
-- Mesas de examen y declaracion jurada (RF49-RF52)
-- ============================================================

CREATE TABLE mesa_examen (
    id_mesa    INTEGER GENERATED ALWAYS AS IDENTITY,
    id_materia INTEGER NOT NULL,
    id_docente INTEGER NOT NULL,
    fecha      DATE    NOT NULL,
    cupo       INTEGER NOT NULL,
    tribunal   VARCHAR(150),
    turno      VARCHAR(10) DEFAULT 'MANANA',
    CONSTRAINT pk_mesa_examen PRIMARY KEY (id_mesa),
    CONSTRAINT fk_mesa_materia FOREIGN KEY (id_materia) REFERENCES materia (id_materia),
    CONSTRAINT fk_mesa_docente FOREIGN KEY (id_docente) REFERENCES docente (id_docente),
    CONSTRAINT ck_mesa_cupo CHECK (cupo > 0),
    CONSTRAINT ck_mesa_turno CHECK (turno IS NULL OR turno IN ('MANANA', 'TARDE', 'NOCHE'))
);

CREATE TABLE inscripcion_mesa (
    id_inscripcion_mesa INTEGER GENERATED ALWAYS AS IDENTITY,
    id_mesa             INTEGER NOT NULL,
    id_alumno           INTEGER NOT NULL,
    fecha               DATE    NOT NULL DEFAULT CURRENT_DATE,
    activa              BOOLEAN NOT NULL DEFAULT TRUE,   -- FALSE = baja (RF50)
    CONSTRAINT pk_inscripcion_mesa PRIMARY KEY (id_inscripcion_mesa),
    CONSTRAINT fk_insc_mesa_mesa FOREIGN KEY (id_mesa) REFERENCES mesa_examen (id_mesa),
    CONSTRAINT fk_insc_mesa_alumno FOREIGN KEY (id_alumno) REFERENCES alumno (id_alumno)
);
-- RN19: una sola inscripcion activa por alumno y mesa
CREATE UNIQUE INDEX uk_inscripcion_mesa_activa
    ON inscripcion_mesa (id_mesa, id_alumno) WHERE activa;

CREATE TABLE declaracion_jurada (
    id_declaracion INTEGER GENERATED ALWAYS AS IDENTITY,
    id_alumno      INTEGER NOT NULL,
    id_ciclo       INTEGER NOT NULL,
    fecha          DATE    NOT NULL DEFAULT CURRENT_DATE,
    CONSTRAINT pk_declaracion_jurada PRIMARY KEY (id_declaracion),
    CONSTRAINT fk_dj_alumno FOREIGN KEY (id_alumno) REFERENCES alumno (id_alumno),
    CONSTRAINT fk_dj_ciclo FOREIGN KEY (id_ciclo) REFERENCES ciclo_lectivo (id_ciclo),
    CONSTRAINT uk_dj_alumno_ciclo UNIQUE (id_alumno, id_ciclo)   -- RN20: una por ciclo
);

CREATE TABLE declaracion_materia (
    id_declaracion INTEGER NOT NULL,
    id_materia     INTEGER NOT NULL,
    CONSTRAINT pk_declaracion_materia PRIMARY KEY (id_declaracion, id_materia),
    CONSTRAINT fk_dm_declaracion FOREIGN KEY (id_declaracion) REFERENCES declaracion_jurada (id_declaracion),
    CONSTRAINT fk_dm_materia FOREIGN KEY (id_materia) REFERENCES materia (id_materia)
);

-- ============================================================
-- Solicitudes y documentacion de alumnos
-- ============================================================

CREATE TABLE solicitud (
    id_solicitud     INTEGER GENERATED ALWAYS AS IDENTITY,
    id_alumno        INTEGER      NOT NULL,
    tipo             VARCHAR(30)  NOT NULL,
    fecha_solicitud  DATE         NOT NULL DEFAULT CURRENT_DATE,
    motivo           VARCHAR(255) NOT NULL,
    observaciones    VARCHAR(255),
    estado           VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    fecha_resolucion DATE,
    respuesta        VARCHAR(255),
    CONSTRAINT pk_solicitud PRIMARY KEY (id_solicitud),
    CONSTRAINT fk_solicitud_alumno FOREIGN KEY (id_alumno) REFERENCES alumno (id_alumno),
    CONSTRAINT ck_solicitud_tipo CHECK (tipo IN ('CONSTANCIA', 'PASANTIA', 'TITULO_INTERMEDIO', 'ACADEMICA', 'CAMBIO_CARRERA', 'REINCORPORACION')),
    CONSTRAINT ck_solicitud_estado CHECK (estado IN ('PENDIENTE', 'EN_PROCESO', 'APROBADA', 'RECHAZADA'))
);

CREATE TABLE documentacion (
    id_documento  INTEGER GENERATED ALWAYS AS IDENTITY,
    id_alumno     INTEGER      NOT NULL,
    tipo          VARCHAR(30)  NOT NULL,
    obligatorio   BOOLEAN      NOT NULL DEFAULT TRUE,
    estado        VARCHAR(20)  NOT NULL DEFAULT 'PENDIENTE',
    fecha_entrega DATE,
    observaciones VARCHAR(255),
    CONSTRAINT pk_documentacion PRIMARY KEY (id_documento),
    CONSTRAINT fk_documentacion_alumno FOREIGN KEY (id_alumno) REFERENCES alumno (id_alumno),
    CONSTRAINT ck_documentacion_tipo CHECK (tipo IN ('DNI', 'TITULO_SECUNDARIO', 'APTO_MEDICO', 'FOTOS')),
    CONSTRAINT ck_documentacion_estado CHECK (estado IN ('ENTREGADO', 'PENDIENTE', 'RECHAZADO'))
);

-- ============================================================
-- Indices para las claves foraneas mas consultadas (RNF06)
-- (PostgreSQL no los crea solo; las UNIQUE ya tienen el suyo)
-- ============================================================

CREATE INDEX ix_alumno_plan ON alumno (id_plan);
CREATE INDEX ix_alumno_apellido ON alumno (apellido, nombre);
CREATE INDEX ix_usuario_rol ON usuario (id_rol);
CREATE INDEX ix_curso_ciclo ON curso (id_ciclo);
CREATE INDEX ix_curso_carrera ON curso (id_carrera);
CREATE INDEX ix_curso_materia_docente ON curso_materia (id_docente);
CREATE INDEX ix_inscripcion_curso ON inscripcion (id_curso);
CREATE INDEX ix_matricula_ciclo ON matricula (id_ciclo);
CREATE INDEX ix_cuota_estado_venc ON cuota (id_estado_cuota, fecha_vencimiento);   -- job de vencidas (RF30)
CREATE INDEX ix_pago_usuario ON pago (id_usuario);
CREATE INDEX ix_pago_fecha ON pago (fecha);
CREATE INDEX ix_evaluacion_curso_materia ON evaluacion (id_curso_materia);
CREATE INDEX ix_calificacion_alumno ON calificacion (id_alumno);
CREATE INDEX ix_asistencia_curso ON asistencia (id_curso, fecha);
CREATE INDEX ix_asistencia_materia ON asistencia (id_materia);
CREATE INDEX ix_mesa_materia ON mesa_examen (id_materia);
CREATE INDEX ix_solicitud_alumno ON solicitud (id_alumno);
CREATE INDEX ix_documentacion_alumno ON documentacion (id_alumno);
