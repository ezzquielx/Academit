# Diccionario de Datos — Academit

> Documento generado automáticamente a partir de `database/01-schema.sql` mediante `tools/diccionario-datos.py`.
> Modela las 32 tablas del esquema relacional institucional en PostgreSQL 16+.

Para ver el diagrama gráfico entidad-relación y sus cardinalidades, consultá el [Modelo Relacional](modelo-relacional.md).
Los datos maestros y registros de desarrollo se describen en [database/README.md](../../database/README.md) y [02-datos-iniciales.sql](../../database/02-datos-iniciales.sql).

---

## Usuarios y Roles

### Tabla `rol`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_rol** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **nombre** | `VARCHAR(30)` | NO | NO | — | SÍ | — | — |

### Tabla `usuario`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_usuario** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **username** | `VARCHAR(50)` | NO | NO | — | SÍ | — | — |
| **password_hash** | `VARCHAR(100)` | NO | NO | — | NO | — | — |
| **activo** | `BOOLEAN` | NO | NO | — | NO | — | — |
| **id_rol** | `INTEGER` | NO | NO | `rol(id_rol)` | NO | — | — |
| **id_alumno** | `INTEGER` | SÍ | NO | `alumno(id_alumno)` | SÍ | `id_alumno IS NULL OR id_docente IS NULL` | — |
| **id_docente** | `INTEGER` | SÍ | NO | `docente(id_docente)` | SÍ | — | — |

## Catálogos

### Tabla `estado_persona`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_estado_persona** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **nombre** | `VARCHAR(30)` | NO | NO | — | SÍ | — | — |

### Tabla `estado_matricula`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_estado_matricula** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **nombre** | `VARCHAR(30)` | NO | NO | — | SÍ | — | — |

### Tabla `estado_cuota`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_estado_cuota** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **nombre** | `VARCHAR(30)` | NO | NO | — | SÍ | — | — |

### Tabla `tipo_cuota`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_tipo_cuota** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **nombre** | `VARCHAR(30)` | NO | NO | — | SÍ | — | — |

### Tabla `medio_pago`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_medio_pago** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **nombre** | `VARCHAR(30)` | NO | NO | — | SÍ | — | — |

### Tabla `tipo_evaluacion`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_tipo_evaluacion** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **nombre** | `VARCHAR(30)` | NO | NO | — | SÍ | — | — |

### Tabla `estado_asistencia`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_estado_asistencia** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **nombre** | `VARCHAR(30)` | NO | NO | — | SÍ | — | — |

## Estructura Académica

### Tabla `carrera`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_carrera** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **codigo** | `VARCHAR(20)` | NO | NO | — | SÍ | — | — |
| **nombre** | `VARCHAR(100)` | NO | NO | — | SÍ | — | — |
| **titulo_otorgado** | `VARCHAR(150)` | NO | NO | — | NO | — | — |
| **duracion_anios** | `INTEGER` | NO | NO | — | NO | `duracion_anios > 0` | — |
| **resolucion** | `VARCHAR(100)` | SÍ | NO | — | NO | — | — |
| **estado** | `VARCHAR(10)` | NO | NO | — | NO | `estado IN ('ACTIVA', 'INACTIVA')` | — |

### Tabla `plan_estudio`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_plan** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **id_carrera** | `INTEGER` | NO | NO | `carrera(id_carrera)` | SÍ | — | — |
| **codigo** | `VARCHAR(50)` | NO | NO | — | SÍ | — | — |
| **nombre** | `VARCHAR(100)` | NO | NO | — | SÍ | — | — |
| **anio_aprobacion** | `INTEGER` | NO | NO | — | NO | `anio_aprobacion >= 1900` | — |
| **estado** | `VARCHAR(10)` | NO | NO | — | NO | `estado IN ('VIGENTE', 'CERRADO')` | — |

### Tabla `materia`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_materia** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **codigo** | `VARCHAR(40)` | NO | NO | — | NO | — | — |
| **nombre** | `VARCHAR(80)` | NO | NO | — | SÍ | — | — |
| **regimen** | `VARCHAR(15)` | NO | NO | — | NO | `regimen IN ('ANUAL', 'CUATRIMESTRAL')` | — |
| **horas_semanales** | `INTEGER` | SÍ | NO | — | NO | `horas_semanales IS NULL OR horas_semanales > 0` | — |
| **activa** | `BOOLEAN` | NO | NO | — | NO | — | — |

### Tabla `plan_materia`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_plan** | `INTEGER` | NO | SÍ | `plan_estudio(id_plan)` | NO | — | — |
| **id_materia** | `INTEGER` | NO | SÍ | `materia(id_materia)` | NO | — | — |
| **anio_plan** | `INTEGER` | NO | NO | — | NO | `anio_plan > 0` | — |

### Tabla `correlativa`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_plan** | `INTEGER` | NO | SÍ | `plan_materia(id_plan, id_materia)` | NO | — | — |
| **id_materia** | `INTEGER` | NO | SÍ | `plan_materia(id_plan, id_materia)` | NO | `id_materia <> id_materia_requerida` | — |
| **id_materia_requerida** | `INTEGER` | NO | SÍ | `plan_materia(id_plan, id_materia)` | NO | — | — |

### Tabla `ciclo_lectivo`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_ciclo** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **anio** | `INTEGER` | NO | NO | — | SÍ | — | — |
| **nombre** | `VARCHAR(60)` | NO | NO | — | NO | — | — |
| **estado** | `VARCHAR(15)` | NO | NO | — | NO | `estado IN ('ACTIVO', 'CERRADO', 'PLANIFICADO')` | — |
| **fecha_inicio** | `DATE` | NO | NO | — | NO | — | — |
| **fecha_fin** | `DATE` | NO | NO | — | NO | `fecha_fin > fecha_inicio` | — |

### Tabla `curso`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_curso** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **codigo** | `VARCHAR(40)` | NO | NO | — | SÍ | — | — |
| **nombre** | `VARCHAR(30)` | NO | NO | — | SÍ | — | — |
| **turno** | `VARCHAR(10)` | NO | NO | — | NO | `turno IN ('MANANA', 'TARDE', 'NOCHE')` | — |
| **capacidad_maxima** | `INTEGER` | NO | NO | — | NO | `capacidad_maxima > 0` | — |
| **aula** | `VARCHAR(100)` | SÍ | NO | — | NO | — | — |
| **id_ciclo** | `INTEGER` | NO | NO | `ciclo_lectivo(id_ciclo)` | SÍ | — | — |
| **id_carrera** | `INTEGER` | NO | NO | `carrera(id_carrera)` | SÍ | — | — |
| **id_plan** | `INTEGER` | NO | NO | `plan_estudio(id_plan)` | NO | — | — |

## Personas y Asignaciones

### Tabla `alumno`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_alumno** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **dni** | `VARCHAR(10)` | NO | NO | — | SÍ | — | — |
| **apellido** | `VARCHAR(60)` | NO | NO | — | NO | — | — |
| **nombre** | `VARCHAR(60)` | NO | NO | — | NO | — | — |
| **fecha_nacimiento** | `DATE` | NO | NO | — | NO | `fecha_nacimiento < fecha_ingreso` | — |
| **domicilio** | `VARCHAR(120)` | SÍ | NO | — | NO | — | — |
| **telefono** | `VARCHAR(20)` | SÍ | NO | — | NO | — | — |
| **email** | `VARCHAR(100)` | NO | NO | — | NO | — | — |
| **fecha_ingreso** | `DATE` | NO | NO | — | NO | — | — |
| **id_estado_persona** | `INTEGER` | NO | NO | `estado_persona(id_estado_persona)` | NO | — | — |
| **id_plan** | `INTEGER` | NO | NO | `plan_estudio(id_plan)` | NO | — | — |

### Tabla `docente`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_docente** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **dni** | `VARCHAR(10)` | NO | NO | — | SÍ | — | — |
| **apellido** | `VARCHAR(60)` | NO | NO | — | NO | — | — |
| **nombre** | `VARCHAR(60)` | NO | NO | — | NO | — | — |
| **telefono** | `VARCHAR(20)` | SÍ | NO | — | NO | — | — |
| **email** | `VARCHAR(100)` | NO | NO | — | NO | — | — |
| **titulo** | `VARCHAR(100)` | SÍ | NO | — | NO | — | — |
| **id_estado_persona** | `INTEGER` | NO | NO | `estado_persona(id_estado_persona)` | NO | — | — |

### Tabla `curso_materia`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_curso_materia** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **id_curso** | `INTEGER` | NO | NO | `curso(id_curso)` | SÍ | — | — |
| **id_materia** | `INTEGER` | NO | NO | `materia(id_materia)` | SÍ | — | — |
| **id_docente** | `INTEGER` | NO | NO | `docente(id_docente)` | SÍ | — | — |
| **rol_docente** | `VARCHAR(20)` | NO | NO | — | NO | `rol_docente IN ('TITULAR', 'ADJUNTO', 'JTP')` | — |

## Inscripciones, Matrículas y Aranceles

### Tabla `inscripcion`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_inscripcion** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **id_alumno** | `INTEGER` | NO | NO | `alumno(id_alumno)` | NO | — | — |
| **id_curso** | `INTEGER` | NO | NO | `curso(id_curso)` | NO | — | — |
| **id_ciclo** | `INTEGER` | NO | NO | `ciclo_lectivo(id_ciclo)` | NO | — | — |
| **fecha** | `DATE` | NO | NO | — | NO | — | — |
| **activa** | `BOOLEAN` | NO | NO | — | NO | — | — |

### Tabla `matricula`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_matricula** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **id_alumno** | `INTEGER` | NO | NO | `alumno(id_alumno)` | SÍ | — | — |
| **id_ciclo** | `INTEGER` | NO | NO | `ciclo_lectivo(id_ciclo)` | SÍ | — | — |
| **fecha** | `DATE` | NO | NO | — | NO | — | — |
| **importe** | `NUMERIC(10` | SÍ | NO | — | NO | `importe IS NULL OR importe >= 0` | — |
| **id_estado_matricula** | `INTEGER` | NO | NO | `estado_matricula(id_estado_matricula)` | NO | — | — |

### Tabla `cuota`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_cuota** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **id_matricula** | `INTEGER` | NO | NO | `matricula(id_matricula)` | SÍ | — | — |
| **numero** | `INTEGER` | NO | NO | — | SÍ | `numero BETWEEN 1 AND 12` | — |
| **id_tipo_cuota** | `INTEGER` | NO | NO | `tipo_cuota(id_tipo_cuota)` | SÍ | — | — |
| **fecha_vencimiento** | `DATE` | NO | NO | — | NO | — | — |
| **importe** | `NUMERIC(10` | NO | NO | — | NO | `importe > 0` | — |
| **id_estado_cuota** | `INTEGER` | NO | NO | `estado_cuota(id_estado_cuota)` | NO | — | — |

### Tabla `pago`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_pago** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **id_cuota** | `INTEGER` | NO | NO | `cuota(id_cuota)` | SÍ | — | — |
| **id_usuario** | `INTEGER` | NO | NO | `usuario(id_usuario)` | NO | — | — |
| **fecha** | `DATE` | NO | NO | — | NO | — | — |
| **importe** | `NUMERIC(10` | NO | NO | — | NO | `importe > 0` | — |
| **id_medio_pago** | `INTEGER` | NO | NO | `medio_pago(id_medio_pago)` | NO | — | — |
| **comprobante** | `VARCHAR(11)` | NO | NO | — | SÍ | — | — |

## Desempeño Académico

### Tabla `evaluacion`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_evaluacion** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **id_curso_materia** | `INTEGER` | NO | NO | `curso_materia(id_curso_materia)` | NO | — | — |
| **fecha** | `DATE` | NO | NO | — | NO | — | — |
| **id_tipo_evaluacion** | `INTEGER` | NO | NO | `tipo_evaluacion(id_tipo_evaluacion)` | NO | — | — |
| **descripcion** | `VARCHAR(200)` | SÍ | NO | — | NO | — | — |

### Tabla `calificacion`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_calificacion** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **id_evaluacion** | `INTEGER` | NO | NO | `evaluacion(id_evaluacion)` | SÍ | — | — |
| **id_alumno** | `INTEGER` | NO | NO | `alumno(id_alumno)` | SÍ | — | — |
| **nota** | `INTEGER` | NO | NO | — | NO | `nota BETWEEN 1 AND 10` | — |
| **observacion** | `VARCHAR(100)` | SÍ | NO | — | NO | — | — |

### Tabla `asistencia`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_asistencia** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **id_alumno** | `INTEGER` | NO | NO | `alumno(id_alumno)` | SÍ | — | — |
| **id_curso** | `INTEGER` | NO | NO | `curso(id_curso)` | SÍ | — | — |
| **id_materia** | `INTEGER` | NO | NO | `materia(id_materia)` | SÍ | — | — |
| **fecha** | `DATE` | NO | NO | — | SÍ | — | — |
| **id_estado_asistencia** | `INTEGER` | NO | NO | `estado_asistencia(id_estado_asistencia)` | NO | — | — |

### Tabla `mesa_examen`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_mesa** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **id_materia** | `INTEGER` | NO | NO | `materia(id_materia)` | NO | — | — |
| **id_docente** | `INTEGER` | SÍ | NO | `docente(id_docente)` | NO | — | — |
| **fecha** | `DATE` | NO | NO | — | NO | — | — |
| **cupo** | `INTEGER` | NO | NO | — | NO | `cupo > 0` | — |
| **tribunal** | `VARCHAR(150)` | SÍ | NO | — | NO | — | — |
| **turno** | `VARCHAR(10)` | SÍ | NO | — | NO | `turno IS NULL OR turno IN ('MANANA', 'TARDE', 'NOCHE')` | — |
| **hora** | `VARCHAR(8)` | SÍ | NO | — | NO | — | — |
| **aula** | `VARCHAR(50)` | SÍ | NO | — | NO | — | — |
| **estado** | `VARCHAR(20)` | SÍ | NO | — | NO | `estado IN ('ABIERTA', 'CERRADA', 'FINALIZADA')` | — |

### Tabla `inscripcion_mesa`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_inscripcion_mesa** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **id_mesa** | `INTEGER` | NO | NO | `mesa_examen(id_mesa)` | NO | — | — |
| **id_alumno** | `INTEGER` | NO | NO | `alumno(id_alumno)` | NO | — | — |
| **fecha** | `DATE` | NO | NO | — | NO | — | — |
| **activa** | `BOOLEAN` | NO | NO | — | NO | — | — |

## Trámites y Declaraciones

### Tabla `declaracion_jurada`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_declaracion** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **id_alumno** | `INTEGER` | NO | NO | `alumno(id_alumno)` | SÍ | — | — |
| **id_ciclo** | `INTEGER` | NO | NO | `ciclo_lectivo(id_ciclo)` | SÍ | — | — |
| **fecha** | `DATE` | NO | NO | — | NO | — | — |

### Tabla `declaracion_materia`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_declaracion** | `INTEGER` | NO | SÍ | `declaracion_jurada(id_declaracion)` | NO | — | — |
| **id_materia** | `INTEGER` | NO | SÍ | `materia(id_materia)` | NO | — | — |

### Tabla `solicitud`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_solicitud** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **id_alumno** | `INTEGER` | NO | NO | `alumno(id_alumno)` | NO | — | — |
| **tipo** | `VARCHAR(30)` | NO | NO | — | NO | `tipo IN ('CONSTANCIA', 'PASANTIA', 'TITULO_INTERMEDIO', 'ACADEMICA', 'CAMBIO_CARRERA', 'REINCORPORACION')` | — |
| **fecha_solicitud** | `DATE` | NO | NO | — | NO | — | — |
| **motivo** | `VARCHAR(255)` | NO | NO | — | NO | — | — |
| **observaciones** | `VARCHAR(255)` | SÍ | NO | — | NO | — | — |
| **estado** | `VARCHAR(20)` | NO | NO | — | NO | `estado IN ('PENDIENTE', 'EN_PROCESO', 'APROBADA', 'RECHAZADA')` | — |
| **fecha_resolucion** | `DATE` | SÍ | NO | — | NO | — | — |
| **respuesta** | `VARCHAR(255)` | SÍ | NO | — | NO | — | — |

### Tabla `documentacion`

| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |
| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |
| **id_documento** | `INTEGER (IDENTITY)` | NO | SÍ | — | NO | — | — |
| **id_alumno** | `INTEGER` | NO | NO | `alumno(id_alumno)` | NO | — | — |
| **tipo** | `VARCHAR(30)` | NO | NO | — | NO | `tipo IN ('DNI', 'TITULO_SECUNDARIO', 'APTO_MEDICO', 'FOTOS')` | — |
| **obligatorio** | `BOOLEAN` | NO | NO | — | NO | — | — |
| **estado** | `VARCHAR(20)` | NO | NO | — | NO | `estado IN ('ENTREGADO', 'PENDIENTE', 'RECHAZADO')` | — |
| **fecha_entrega** | `DATE` | SÍ | NO | — | NO | — | — |
| **observaciones** | `VARCHAR(255)` | SÍ | NO | — | NO | — | — |
