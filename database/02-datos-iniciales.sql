-- Datos iniciales y de prueba (PostgreSQL)
--
-- Ejecutar DESPUES de 01-schema.sql sobre la base vacia.
-- Usuarios de prueba (contrasena de todos: Academit123, guardada con BCrypt):
--   admin | administrativo | docente1 | alumno1
-- Solo para desarrollo: no usar estos usuarios en un entorno real.

-- Roles (RF04, RN17)
INSERT INTO rol (nombre) VALUES ('ADMINISTRADOR'), ('ADMINISTRATIVO'), ('DOCENTE'), ('ALUMNO');

-- Catalogos (reemplazan a los enums; se pueden ampliar con un INSERT)
INSERT INTO estado_persona (nombre) VALUES ('ACTIVO'), ('INACTIVO');
INSERT INTO estado_matricula (nombre) VALUES ('PENDIENTE'), ('ACTIVA'), ('CANCELADA');
INSERT INTO estado_cuota (nombre) VALUES ('PENDIENTE'), ('PAGADA'), ('VENCIDA'), ('ANULADA');
INSERT INTO tipo_cuota (nombre) VALUES ('CUOTA_MENSUAL'), ('SEGURO_CUATRIMESTRAL');
INSERT INTO medio_pago (nombre) VALUES ('EFECTIVO'), ('TRANSFERENCIA'), ('TARJETA'), ('OTRO');
INSERT INTO tipo_evaluacion (nombre) VALUES
    ('PRIMER_PARCIAL'), ('SEGUNDO_PARCIAL'), ('TRABAJO_PRACTICO'), ('EXAMEN_FINAL'), ('RECUPERATORIO');
INSERT INTO estado_asistencia (nombre) VALUES ('PRESENTE'), ('AUSENTE'), ('TARDANZA'), ('JUSTIFICADO');

-- Carrera, plan y materias (RF47)
INSERT INTO carrera (nombre, duracion_anios)
VALUES ('Tecnicatura Universitaria en Programación', 3);

INSERT INTO plan_estudio (id_carrera, nombre, anio_vigencia)
SELECT id_carrera, 'Plan 2024', 2024 FROM carrera
WHERE nombre = 'Tecnicatura Universitaria en Programación';

INSERT INTO materia (nombre) VALUES
    ('Programación I'), ('Matemática'), ('Arquitectura de Computadoras'),
    ('Programación II'), ('Bases de Datos'), ('Sistemas Operativos');

INSERT INTO plan_materia (id_plan, id_materia, anio_plan)
SELECT p.id_plan, m.id_materia, v.anio
FROM plan_estudio p
JOIN (VALUES ('Programación I', 1), ('Matemática', 1), ('Arquitectura de Computadoras', 1),
             ('Programación II', 2), ('Bases de Datos', 2), ('Sistemas Operativos', 2)
     ) AS v(nombre, anio) ON TRUE
JOIN materia m ON m.nombre = v.nombre
WHERE p.nombre = 'Plan 2024';

INSERT INTO correlativa (id_plan, id_materia, id_materia_requerida)
SELECT p.id_plan, m.id_materia, r.id_materia
FROM plan_estudio p
JOIN (VALUES ('Programación II', 'Programación I'),
             ('Bases de Datos', 'Programación I'),
             ('Sistemas Operativos', 'Arquitectura de Computadoras')
     ) AS v(materia, requerida) ON TRUE
JOIN materia m ON m.nombre = v.materia
JOIN materia r ON r.nombre = v.requerida
WHERE p.nombre = 'Plan 2024';

-- Ciclo lectivo, docentes y alumnos (RF06, RF10, RF20)
INSERT INTO ciclo_lectivo (anio, nombre, estado, fecha_inicio, fecha_fin)
VALUES (2026, 'Ciclo Lectivo 2026', 'ACTIVO', '2026-03-02', '2026-12-18'),
       (2027, 'Ciclo Lectivo 2027', 'PLANIFICADO', '2027-03-01', '2027-12-17');

INSERT INTO docente (dni, apellido, nombre, telefono, email, titulo, id_estado_persona)
SELECT v.dni, v.apellido, v.nombre, v.tel, v.email, v.titulo, e.id_estado_persona
FROM (VALUES ('25111222', 'Pérez', 'Laura', '3815000001', 'laura.perez@example.com', 'Ingeniera en Informática'),
             ('27333444', 'Gómez', 'Carlos', '3815000002', 'carlos.gomez@example.com', 'Licenciado en Matemática')
     ) AS v(dni, apellido, nombre, tel, email, titulo)
JOIN estado_persona e ON e.nombre = 'ACTIVO';

INSERT INTO alumno (dni, apellido, nombre, fecha_nacimiento, domicilio, telefono, email, id_plan, id_estado_persona)
SELECT v.dni, v.apellido, v.nombre, v.nac::date, v.dom, v.tel, v.email, p.id_plan, e.id_estado_persona
FROM plan_estudio p, estado_persona e,
     (VALUES ('44111222', 'Rojas', 'Lucía', '2003-05-14', 'Calle Falsa 123', '3815100001', 'lucia.rojas@example.com'),
             ('44333444', 'Díaz', 'Mateo', '2002-11-02', 'Calle Falsa 456', '3815100002', 'mateo.diaz@example.com'),
             ('44555666', 'Silva', 'Camila', '2004-01-27', 'Calle Falsa 789', '3815100003', 'camila.silva@example.com')
     ) AS v(dni, apellido, nombre, nac, dom, tel, email)
WHERE p.nombre = 'Plan 2024' AND e.nombre = 'ACTIVO';

-- Usuarios de prueba, uno por rol (RF01, RF03)
INSERT INTO usuario (username, password_hash, id_rol, id_alumno, id_docente)
SELECT v.usr, '$2b$10$6KLhDtEtvnNTo1sGZJo1wu/.H3xpna6fkSlFCJqwI/cK/iBCzpOG6', r.id_rol,
       (SELECT id_alumno FROM alumno WHERE dni = v.dni_alumno),
       (SELECT id_docente FROM docente WHERE dni = v.dni_docente)
FROM (VALUES ('admin', 'ADMINISTRADOR', NULL, NULL),
             ('administrativo', 'ADMINISTRATIVO', NULL, NULL),
             ('docente1', 'DOCENTE', NULL, '25111222'),
             ('alumno1', 'ALUMNO', '44111222', NULL)
     ) AS v(usr, rol, dni_alumno, dni_docente)
JOIN rol r ON r.nombre = v.rol;

-- Cursos y docentes asignados (RF14, RF18, RF19)
INSERT INTO curso (nombre, turno, capacidad_maxima, id_ciclo, id_carrera)
SELECT v.nombre, v.turno, v.capacidad, ci.id_ciclo, ca.id_carrera
FROM ciclo_lectivo ci, carrera ca,
     (VALUES ('1° Año A', 'MANANA', 30), ('2° Año A', 'NOCHE', 25)) AS v(nombre, turno, capacidad)
WHERE ci.anio = 2026 AND ca.nombre = 'Tecnicatura Universitaria en Programación';

INSERT INTO curso_materia (id_curso, id_materia, id_docente, rol_docente)
SELECT c.id_curso, m.id_materia, d.id_docente, v.rol
FROM (VALUES ('1° Año A', 'Programación I', '25111222', 'TITULAR'),
             ('1° Año A', 'Programación I', '27333444', 'ADJUNTO'),
             ('1° Año A', 'Matemática', '27333444', 'TITULAR'),
             ('1° Año A', 'Arquitectura de Computadoras', '27333444', 'TITULAR'),
             ('2° Año A', 'Programación II', '25111222', 'TITULAR'),
             ('2° Año A', 'Bases de Datos', '25111222', 'TITULAR'),
             ('2° Año A', 'Sistemas Operativos', '27333444', 'TITULAR')
     ) AS v(curso, materia, dni, rol)
JOIN curso c ON c.nombre = v.curso
JOIN materia m ON m.nombre = v.materia
JOIN docente d ON d.dni = v.dni;

-- Inscripciones y matriculas (RF22, RF25). Camila queda sin inscribir para probar.
INSERT INTO inscripcion (id_alumno, id_curso, id_ciclo)
SELECT a.id_alumno, c.id_curso, c.id_ciclo
FROM alumno a JOIN curso c ON c.nombre = '1° Año A'
WHERE a.dni IN ('44111222', '44333444');

INSERT INTO matricula (id_alumno, id_ciclo, importe, id_estado_matricula)
SELECT a.id_alumno, ci.id_ciclo, v.importe, em.id_estado_matricula
FROM (VALUES ('44111222', 15000.00, 'ACTIVA'), ('44333444', 15000.00, 'PENDIENTE')) AS v(dni, importe, estado)
JOIN alumno a ON a.dni = v.dni
JOIN ciclo_lectivo ci ON ci.anio = 2026
JOIN estado_matricula em ON em.nombre = v.estado;

-- Cuotas de la matricula de Lucia: 12 mensuales + 3 seguros (RF28, RF46, RN18).
-- Las pendientes con vencimiento pasado sirven para probar el job de vencidas (RF30).
INSERT INTO cuota (id_matricula, numero, id_tipo_cuota, fecha_vencimiento, importe, id_estado_cuota)
SELECT m.id_matricula, n, t.id_tipo_cuota,
       (DATE '2026-03-10' + ((n - 1) * INTERVAL '1 month'))::date, 12000.00, e.id_estado_cuota
FROM matricula m
JOIN alumno a ON a.id_alumno = m.id_alumno
CROSS JOIN generate_series(1, 12) AS n
JOIN tipo_cuota t ON t.nombre = 'CUOTA_MENSUAL'
JOIN estado_cuota e ON e.nombre = CASE WHEN n <= 6 THEN 'PAGADA' ELSE 'PENDIENTE' END
WHERE a.dni = '44111222';

INSERT INTO cuota (id_matricula, numero, id_tipo_cuota, fecha_vencimiento, importe, id_estado_cuota)
SELECT m.id_matricula, n, t.id_tipo_cuota,
       (DATE '2026-03-31' + ((n - 1) * INTERVAL '4 months'))::date, 3000.00, e.id_estado_cuota
FROM matricula m
JOIN alumno a ON a.id_alumno = m.id_alumno
CROSS JOIN generate_series(1, 3) AS n
JOIN tipo_cuota t ON t.nombre = 'SEGURO_CUATRIMESTRAL'
JOIN estado_cuota e ON e.nombre = CASE WHEN n = 1 THEN 'PAGADA' ELSE 'PENDIENTE' END
WHERE a.dni = '44111222';

-- Pagos de las cuotas pagadas (RF32, RN08-RN10)
INSERT INTO pago (id_cuota, id_usuario, fecha, importe, id_medio_pago, comprobante)
SELECT c.id_cuota, u.id_usuario, c.fecha_vencimiento, c.importe, mp.id_medio_pago,
       '2026-' || LPAD((ROW_NUMBER() OVER (ORDER BY c.id_cuota))::text, 6, '0')
FROM cuota c
JOIN estado_cuota e ON e.id_estado_cuota = c.id_estado_cuota AND e.nombre = 'PAGADA'
JOIN usuario u ON u.username = 'administrativo'
JOIN medio_pago mp ON mp.nombre = 'EFECTIVO';

-- Evaluacion, notas y asistencias (RF36-RF41)
INSERT INTO evaluacion (id_curso_materia, fecha, id_tipo_evaluacion, descripcion)
SELECT cm.id_curso_materia, DATE '2026-05-20', te.id_tipo_evaluacion, 'Primer parcial de Programación I'
FROM curso_materia cm
JOIN tipo_evaluacion te ON te.nombre = 'PRIMER_PARCIAL'
JOIN curso c ON c.id_curso = cm.id_curso
JOIN materia m ON m.id_materia = cm.id_materia
WHERE c.nombre = '1° Año A' AND m.nombre = 'Programación I';

INSERT INTO calificacion (id_evaluacion, id_alumno, nota)
SELECT e.id_evaluacion, a.id_alumno, v.nota
FROM evaluacion e
JOIN (VALUES ('44111222', 8), ('44333444', 3)) AS v(dni, nota) ON TRUE
JOIN alumno a ON a.dni = v.dni;

INSERT INTO asistencia (id_alumno, id_curso, id_materia, fecha, id_estado_asistencia)
SELECT a.id_alumno, c.id_curso, m.id_materia, v.fecha::date, ea.id_estado_asistencia
FROM (VALUES ('44111222', '2026-03-09', 'PRESENTE'), ('44333444', '2026-03-09', 'PRESENTE'),
             ('44111222', '2026-03-10', 'PRESENTE'), ('44333444', '2026-03-10', 'AUSENTE'),
             ('44111222', '2026-03-11', 'TARDANZA'), ('44333444', '2026-03-11', 'JUSTIFICADO')
     ) AS v(dni, fecha, estado)
JOIN alumno a ON a.dni = v.dni
JOIN curso c ON c.nombre = '1° Año A'
JOIN materia m ON m.nombre = 'Programación I'
JOIN estado_asistencia ea ON ea.nombre = v.estado;

-- Mesa de examen (RF49)
INSERT INTO mesa_examen (id_materia, id_docente, fecha, cupo, tribunal, turno)
SELECT m.id_materia, d.id_docente, DATE '2026-12-10', 20, 'Pérez, Gómez', 'MANANA'
FROM materia m, docente d
WHERE m.nombre = 'Programación I' AND d.dni = '25111222';

-- Solicitudes y documentacion de prueba (RF53)
INSERT INTO solicitud (id_alumno, tipo, fecha_solicitud, motivo, observaciones, estado)
SELECT a.id_alumno, v.tipo, v.fecha::date, v.motivo, v.obs, v.estado
FROM alumno a
JOIN (VALUES
    ('44111222', 'CONSTANCIA', '2026-03-15', 'Constancia de alumno regular para trámite de transporte', 'Trámite urgente', 'APROBADA'),
    ('44111222', 'PASANTIA', '2026-04-01', 'Postulación a pasantía en empresa tecnológica', NULL, 'EN_PROCESO'),
    ('44333444', 'CONSTANCIA', '2026-03-20', 'Constancia de examen final rendido', NULL, 'PENDIENTE')
) AS v(dni, tipo, fecha, motivo, obs, estado) ON a.dni = v.dni;

INSERT INTO documentacion (id_alumno, tipo, obligatorio, estado, fecha_entrega, observaciones)
SELECT a.id_alumno, v.tipo, v.obligatorio::boolean, v.estado, v.fecha::date, v.obs
FROM alumno a
JOIN (VALUES
    ('44111222', 'DNI', true, 'ENTREGADO', '2026-02-15', 'Fotocopia certificada'),
    ('44111222', 'TITULO_SECUNDARIO', true, 'ENTREGADO', '2026-02-15', 'Legalizado por ministerio'),
    ('44111222', 'APTO_MEDICO', true, 'ENTREGADO', '2026-03-01', 'Vigencia anual'),
    ('44111222', 'FOTOS', false, 'ENTREGADO', '2026-02-15', '4 fotos 4x4'),
    ('44333444', 'DNI', true, 'ENTREGADO', '2026-02-20', NULL),
    ('44333444', 'TITULO_SECUNDARIO', true, 'PENDIENTE', NULL, 'Constancia en trámite'),
    ('44333444', 'APTO_MEDICO', true, 'PENDIENTE', NULL, 'Turno médico pendiente')
) AS v(dni, tipo, obligatorio, estado, fecha, obs) ON a.dni = v.dni;
