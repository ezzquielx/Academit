-- Datos iniciales y de prueba (PostgreSQL)
--
-- Ejecutar DESPUES de 01-schema.sql sobre la base vacia.
-- Usuarios de prueba (contrasena de todos: Academit123, guardada con BCrypt):
--   admin | administrativo | docente1 | alumno1
-- Solo para desarrollo: no usar estos usuarios en un entorno real.

-- Roles (RF04, RN17)
INSERT INTO rol (nombre) VALUES ('ADMINISTRADOR'), ('ADMINISTRATIVO'), ('DOCENTE'), ('ALUMNO');

-- Catalogos (reemplazan a los enums)
INSERT INTO estado_persona (nombre) VALUES ('ACTIVO'), ('INACTIVO');
INSERT INTO estado_matricula (nombre) VALUES ('PENDIENTE'), ('ACTIVA'), ('CANCELADA');
INSERT INTO estado_cuota (nombre) VALUES ('PENDIENTE'), ('PAGADA'), ('VENCIDA'), ('ANULADA');
INSERT INTO tipo_cuota (nombre) VALUES ('CUOTA_MENSUAL'), ('SEGURO_CUATRIMESTRAL');
INSERT INTO medio_pago (nombre) VALUES ('EFECTIVO'), ('TRANSFERENCIA'), ('TARJETA'), ('OTRO');
INSERT INTO tipo_evaluacion (nombre) VALUES
    ('PARCIAL'), ('PRIMER_PARCIAL'), ('SEGUNDO_PARCIAL'), ('TRABAJO_PRACTICO'), ('EXAMEN_FINAL'), ('RECUPERATORIO');
INSERT INTO estado_asistencia (nombre) VALUES ('PRESENTE'), ('AUSENTE'), ('TARDANZA'), ('JUSTIFICADO');

-- --------------------------------------------------------------------------
-- 1. CARRERAS (5 Ingenierías + Ciencias de la Salud + Abogacía)
-- --------------------------------------------------------------------------
INSERT INTO carrera (codigo, nombre, titulo_otorgado, duracion_anios, resolucion) VALUES
    ('ISI', 'Ingeniería en Sistemas de Información', 'Ingeniero/a en Sistemas de Información', 5, 'Res. Ministerial N° 1020/21'),
    ('IE',  'Ingeniería Electrónica',                'Ingeniero/a Electrónico/a',             5, 'Res. Ministerial N° 1021/21'),
    ('IM',  'Ingeniería Mecánica',                   'Ingeniero/a Mecánico/a',                5, 'Res. Ministerial N° 1022/21'),
    ('IQ',  'Ingeniería Química',                    'Ingeniero/a Químico/a',                 5, 'Res. Ministerial N° 1023/21'),
    ('IC',  'Ingeniería Civil',                      'Ingeniero/a Civil',                     5, 'Res. Ministerial N° 1024/21'),
    ('MED', 'Medicina',                              'Médico/a',                              6, 'Res. Ministerial N° 1550/20'),
    ('ENF', 'Licenciatura en Enfermería',            'Licenciado/a en Enfermería',            4, 'Res. Ministerial N° 1551/20'),
    ('ABO', 'Abogacía',                              'Abogado/a',                             5, 'Res. Ministerial N° 850/19');

-- --------------------------------------------------------------------------
-- 2. PLANES DE ESTUDIO (1 por carrera)
-- --------------------------------------------------------------------------
INSERT INTO plan_estudio (id_carrera, codigo, nombre, anio_aprobacion)
SELECT c.id_carrera, 'PLAN-2024-' || c.codigo, 'Plan 2024 - ' || c.nombre, 2024
FROM carrera c;

-- --------------------------------------------------------------------------
-- 3. MATERIAS
-- --------------------------------------------------------------------------
INSERT INTO materia (codigo, nombre, regimen, horas_semanales) VALUES
    -- Tronco común Ingenierías
    ('AM-I',    'Análisis Matemático I',               'ANUAL',         5),
    ('AGA',     'Álgebra y Geometría Analítica',       'ANUAL',         5),
    ('FIS-I',   'Física I',                            'CUATRIMESTRAL', 5),
    ('QUI-GEN', 'Química General',                     'CUATRIMESTRAL', 4),
    ('INFO-I',  'Informática General',                 'CUATRIMESTRAL', 4),
    ('AM-II',   'Análisis Matemático II',              'ANUAL',         5),
    ('FIS-II',  'Física II',                           'CUATRIMESTRAL', 5),
    -- Específicas Sistemas
    ('AED',     'Algoritmos y Estructuras de Datos',   'ANUAL',         6),
    ('SyO',     'Sistemas y Organizaciones',           'CUATRIMESTRAL', 4),
    ('BD-I',    'Bases de Datos',                      'CUATRIMESTRAL', 4),
    -- Específicas Electrónica / Mecánica / Química / Civil
    ('CIR-I',   'Circuitos Eléctricos I',              'ANUAL',         5),
    ('MEC-I',   'Sistemas Mecánicos y Materiales',     'ANUAL',         5),
    ('Q-ORG',   'Química Orgánica',                    'ANUAL',         5),
    ('ESTAB',   'Estabilidad y Resistencia',           'ANUAL',         5),
    -- Ciencias de la Salud (Medicina y Enfermería)
    ('ANAT',    'Anatomía Humana',                     'ANUAL',         6),
    ('HISTO',   'Histología y Embriología',            'CUATRIMESTRAL', 4),
    ('BIO-CEL', 'Biología Celular y Genética',         'CUATRIMESTRAL', 4),
    ('FISIO',   'Fisiología Humana',                   'ANUAL',         6),
    ('BIOQ',    'Bioquímica Médica',                   'CUATRIMESTRAL', 5),
    ('ENF-BAS', 'Fundamentos de Enfermería',           'ANUAL',         6),
    ('SAL-PUB', 'Salud Pública y Epidemiología',       'CUATRIMESTRAL', 4),
    ('FARMA',   'Farmacología Clínica',                'CUATRIMESTRAL', 4),
    -- Abogacía
    ('INT-DER', 'Introducción al Derecho',             'CUATRIMESTRAL', 4),
    ('DER-CON', 'Derecho Constitucional',              'ANUAL',         5),
    ('CIV-I',   'Derecho Civil I (Parte General)',     'ANUAL',         5),
    ('PEN-I',   'Derecho Penal I',                     'ANUAL',         5),
    ('DER-ROM', 'Derecho Romano',                      'CUATRIMESTRAL', 4),
    ('OBLIG',   'Derecho de las Obligaciones',         'ANUAL',         5);

-- Mapeo plan_materia
-- Sistemas
INSERT INTO plan_materia (id_plan, id_materia, anio_plan)
SELECT p.id_plan, m.id_materia, v.anio
FROM plan_estudio p
JOIN carrera c ON c.id_carrera = p.id_carrera AND c.codigo = 'ISI'
JOIN (VALUES ('Análisis Matemático I', 1), ('Álgebra y Geometría Analítica', 1),
             ('Física I', 1), ('Algoritmos y Estructuras de Datos', 1), ('Sistemas y Organizaciones', 1),
             ('Análisis Matemático II', 2), ('Física II', 2), ('Bases de Datos', 2)
     ) AS v(mat, anio) ON TRUE
JOIN materia m ON m.nombre = v.mat;

-- Electrónica
INSERT INTO plan_materia (id_plan, id_materia, anio_plan)
SELECT p.id_plan, m.id_materia, v.anio
FROM plan_estudio p
JOIN carrera c ON c.id_carrera = p.id_carrera AND c.codigo = 'IE'
JOIN (VALUES ('Análisis Matemático I', 1), ('Álgebra y Geometría Analítica', 1),
             ('Física I', 1), ('Informática General', 1),
             ('Circuitos Eléctricos I', 2), ('Análisis Matemático II', 2), ('Física II', 2)
     ) AS v(mat, anio) ON TRUE
JOIN materia m ON m.nombre = v.mat;

-- Mecánica
INSERT INTO plan_materia (id_plan, id_materia, anio_plan)
SELECT p.id_plan, m.id_materia, v.anio
FROM plan_estudio p
JOIN carrera c ON c.id_carrera = p.id_carrera AND c.codigo = 'IM'
JOIN (VALUES ('Análisis Matemático I', 1), ('Álgebra y Geometría Analítica', 1),
             ('Física I', 1), ('Química General', 1),
             ('Sistemas Mecánicos y Materiales', 2), ('Análisis Matemático II', 2)
     ) AS v(mat, anio) ON TRUE
JOIN materia m ON m.nombre = v.mat;

-- Química
INSERT INTO plan_materia (id_plan, id_materia, anio_plan)
SELECT p.id_plan, m.id_materia, v.anio
FROM plan_estudio p
JOIN carrera c ON c.id_carrera = p.id_carrera AND c.codigo = 'IQ'
JOIN (VALUES ('Análisis Matemático I', 1), ('Álgebra y Geometría Analítica', 1),
             ('Química General', 1), ('Física I', 1),
             ('Química Orgánica', 2), ('Análisis Matemático II', 2)
     ) AS v(mat, anio) ON TRUE
JOIN materia m ON m.nombre = v.mat;

-- Civil
INSERT INTO plan_materia (id_plan, id_materia, anio_plan)
SELECT p.id_plan, m.id_materia, v.anio
FROM plan_estudio p
JOIN carrera c ON c.id_carrera = p.id_carrera AND c.codigo = 'IC'
JOIN (VALUES ('Análisis Matemático I', 1), ('Álgebra y Geometría Analítica', 1),
             ('Física I', 1), ('Química General', 1),
             ('Estabilidad y Resistencia', 2), ('Análisis Matemático II', 2)
     ) AS v(mat, anio) ON TRUE
JOIN materia m ON m.nombre = v.mat;

-- Medicina
INSERT INTO plan_materia (id_plan, id_materia, anio_plan)
SELECT p.id_plan, m.id_materia, v.anio
FROM plan_estudio p
JOIN carrera c ON c.id_carrera = p.id_carrera AND c.codigo = 'MED'
JOIN (VALUES ('Anatomía Humana', 1), ('Histología y Embriología', 1), ('Biología Celular y Genética', 1),
             ('Fisiología Humana', 2), ('Bioquímica Médica', 2)
     ) AS v(mat, anio) ON TRUE
JOIN materia m ON m.nombre = v.mat;

-- Enfermería
INSERT INTO plan_materia (id_plan, id_materia, anio_plan)
SELECT p.id_plan, m.id_materia, v.anio
FROM plan_estudio p
JOIN carrera c ON c.id_carrera = p.id_carrera AND c.codigo = 'ENF'
JOIN (VALUES ('Fundamentos de Enfermería', 1), ('Anatomía Humana', 1), ('Salud Pública y Epidemiología', 1),
             ('Fisiología Humana', 2), ('Farmacología Clínica', 2)
     ) AS v(mat, anio) ON TRUE
JOIN materia m ON m.nombre = v.mat;

-- Abogacía
INSERT INTO plan_materia (id_plan, id_materia, anio_plan)
SELECT p.id_plan, m.id_materia, v.anio
FROM plan_estudio p
JOIN carrera c ON c.id_carrera = p.id_carrera AND c.codigo = 'ABO'
JOIN (VALUES ('Introducción al Derecho', 1), ('Derecho Constitucional', 1), ('Derecho Romano', 1),
             ('Derecho Civil I (Parte General)', 1),
             ('Derecho Penal I', 2), ('Derecho de las Obligaciones', 2)
     ) AS v(mat, anio) ON TRUE
JOIN materia m ON m.nombre = v.mat;

-- Correlativas para materias de 2° año
INSERT INTO correlativa (id_plan, id_materia, id_materia_requerida)
SELECT p.id_plan, m.id_materia, r.id_materia
FROM plan_estudio p
JOIN carrera c ON c.id_carrera = p.id_carrera AND c.codigo = 'ISI'
JOIN (VALUES ('Análisis Matemático II', 'Análisis Matemático I'),
             ('Física II', 'Física I'),
             ('Bases de Datos', 'Algoritmos y Estructuras de Datos')
     ) AS v(materia, requerida) ON TRUE
JOIN materia m ON m.nombre = v.materia
JOIN materia r ON r.nombre = v.requerida;

INSERT INTO correlativa (id_plan, id_materia, id_materia_requerida)
SELECT p.id_plan, m.id_materia, r.id_materia
FROM plan_estudio p
JOIN carrera c ON c.id_carrera = p.id_carrera AND c.codigo = 'MED'
JOIN (VALUES ('Fisiología Humana', 'Anatomía Humana'),
             ('Bioquímica Médica', 'Biología Celular y Genética')
     ) AS v(materia, requerida) ON TRUE
JOIN materia m ON m.nombre = v.materia
JOIN materia r ON r.nombre = v.requerida;

INSERT INTO correlativa (id_plan, id_materia, id_materia_requerida)
SELECT p.id_plan, m.id_materia, r.id_materia
FROM plan_estudio p
JOIN carrera c ON c.id_carrera = p.id_carrera AND c.codigo = 'ABO'
JOIN (VALUES ('Derecho de las Obligaciones', 'Derecho Civil I (Parte General)'),
             ('Derecho Penal I', 'Introducción al Derecho')
     ) AS v(materia, requerida) ON TRUE
JOIN materia m ON m.nombre = v.materia
JOIN materia r ON r.nombre = v.requerida;

-- --------------------------------------------------------------------------
-- 4. CICLOS LECTIVOS Y DOCENTES
-- --------------------------------------------------------------------------
INSERT INTO ciclo_lectivo (anio, nombre, estado, fecha_inicio, fecha_fin) VALUES
    (2026, 'Ciclo Lectivo 2026', 'ACTIVO', '2026-03-02', '2026-12-18'),
    (2027, 'Ciclo Lectivo 2027', 'PLANIFICADO', '2027-03-01', '2027-12-17');

INSERT INTO docente (dni, apellido, nombre, telefono, email, titulo, id_estado_persona)
SELECT v.dni, v.apellido, v.nombre, v.tel, v.email, v.titulo, e.id_estado_persona
FROM (VALUES
    ('25111222', 'Pérez', 'Laura', '3815000001', 'laura.perez@instituto.edu.ar', 'Ingeniera en Sistemas'),
    ('27333444', 'Gómez', 'Carlos', '3815000002', 'carlos.gomez@instituto.edu.ar', 'Licenciado en Matemática'),
    ('28555666', 'Méndez', 'Roberto', '3815000003', 'roberto.mendez@instituto.edu.ar', 'Médico Cirujano'),
    ('29777888', 'Albarracín', 'Patricia', '3815000004', 'patricia.albarracin@instituto.edu.ar', 'Abogada Especialista en Derecho Constitucional')
) AS v(dni, apellido, nombre, tel, email, titulo)
JOIN estado_persona e ON e.nombre = 'ACTIVO';

-- --------------------------------------------------------------------------
-- 5. ALUMNOS (10 por carrera = 80 alumnos en total)
-- --------------------------------------------------------------------------
-- Ingeniería en Sistemas (DNI 43100001 - 43100010)
INSERT INTO alumno (dni, apellido, nombre, fecha_nacimiento, domicilio, telefono, email, id_plan, id_estado_persona)
SELECT v.dni, v.apellido, v.nombre, v.nac::date, v.dom, v.tel, v.email, p.id_plan, e.id_estado_persona
FROM plan_estudio p
JOIN carrera c ON c.id_carrera = p.id_carrera AND c.codigo = 'ISI'
CROSS JOIN estado_persona e
JOIN (VALUES
    ('43100001', 'Benítez', 'Lucía', '2002-04-18', 'Av. Corrientes 1234, CABA', '11-4567-8901', 'lucia.benitez@instituto.edu.ar'),
    ('43100002', 'Rodríguez', 'Martín', '2003-08-22', 'Calle Falsa 123, CABA', '11-4567-8902', 'martin.rodriguez@instituto.edu.ar'),
    ('43100003', 'Suárez', 'Camila', '2001-11-05', 'Av. Santa Fe 4567, CABA', '11-4567-8903', 'camila.suarez@instituto.edu.ar'),
    ('43100004', 'Fernández', 'Lucas', '2002-01-30', 'Belgrano 890, Quilmes', '11-4567-8904', 'lucas.fernandez@instituto.edu.ar'),
    ('43100005', 'López', 'Valentina', '2004-06-14', 'Mitre 540, San Isidro', '11-4567-8905', 'valentina.lopez@instituto.edu.ar'),
    ('43100006', 'Díaz', 'Joaquín', '2003-09-09', 'Rivadavia 2100, Morón', '11-4567-8906', 'joaquin.diaz@instituto.edu.ar'),
    ('43100007', 'Martínez', 'Sofía', '2002-12-19', 'Lavalle 340, Ramos Mejía', '11-4567-8907', 'sofia.martinez@instituto.edu.ar'),
    ('43100008', 'Romero', 'Ignacio', '2001-07-25', 'Alsina 780, Avellaneda', '11-4567-8908', 'ignacio.romero@instituto.edu.ar'),
    ('43100009', 'Alonso', 'Mía', '2003-03-12', 'San Martín 120, Lanús', '11-4567-8909', 'mia.alonso@instituto.edu.ar'),
    ('43100010', 'Torres', 'Mateo', '2004-10-02', 'Callao 980, CABA', '11-4567-8910', 'mateo.torres@instituto.edu.ar')
) AS v(dni, apellido, nombre, nac, dom, tel, email) ON TRUE
WHERE e.nombre = 'ACTIVO';

-- Ingeniería Electrónica (DNI 43200001 - 43200010)
INSERT INTO alumno (dni, apellido, nombre, fecha_nacimiento, domicilio, telefono, email, id_plan, id_estado_persona)
SELECT v.dni, v.apellido, v.nombre, v.nac::date, v.dom, v.tel, v.email, p.id_plan, e.id_estado_persona
FROM plan_estudio p
JOIN carrera c ON c.id_carrera = p.id_carrera AND c.codigo = 'IE'
CROSS JOIN estado_persona e
JOIN (VALUES
    ('43200001', 'Giménez', 'Facundo', '2002-05-10', 'Av. Pueyrredón 650, CABA', '11-4600-0001', 'facundo.gimenez@instituto.edu.ar'),
    ('43200002', 'Navarro', 'Julieta', '2003-02-14', 'Colombres 120, Lomas de Zamora', '11-4600-0002', 'julieta.navarro@instituto.edu.ar'),
    ('43200003', 'Mendoza', 'Tomás', '2001-09-18', 'Av. Las Heras 2300, CABA', '11-4600-0003', 'tomas.mendoza@instituto.edu.ar'),
    ('43200004', 'Acosta', 'Paula', '2004-03-22', 'Roca 450, Temperley', '11-4600-0004', 'paula.acosta@instituto.edu.ar'),
    ('43200005', 'Vargas', 'Santiago', '2002-11-30', 'Gorriti 4900, CABA', '11-4600-0005', 'santiago.vargas@instituto.edu.ar'),
    ('43200006', 'Castro', 'Milagros', '2003-07-04', 'Pellegrini 310, Banfield', '11-4600-0006', 'milagros.castro@instituto.edu.ar'),
    ('43200007', 'Flores', 'Bruno', '2004-08-15', 'Directorio 1500, CABA', '11-4600-0007', 'bruno.flores@instituto.edu.ar'),
    ('43200008', 'Medina', 'Florencia', '2001-12-01', 'Loria 200, Lomas de Zamora', '11-4600-0008', 'florencia.medina@instituto.edu.ar'),
    ('43200009', 'Sosa', 'Rodrigo', '2003-04-27', 'Paraná 800, Olivos', '11-4600-0009', 'rodrigo.sosa@instituto.edu.ar'),
    ('43200010', 'Ramos', 'Candela', '2002-10-14', 'Maipú 1400, Vicente López', '11-4600-0010', 'candela.ramos@instituto.edu.ar')
) AS v(dni, apellido, nombre, nac, dom, tel, email) ON TRUE
WHERE e.nombre = 'ACTIVO';

-- Ingeniería Mecánica (DNI 43300001 - 43300010)
INSERT INTO alumno (dni, apellido, nombre, fecha_nacimiento, domicilio, telefono, email, id_plan, id_estado_persona)
SELECT v.dni, v.apellido, v.nombre, v.nac::date, v.dom, v.tel, v.email, p.id_plan, e.id_estado_persona
FROM plan_estudio p
JOIN carrera c ON c.id_carrera = p.id_carrera AND c.codigo = 'IM'
CROSS JOIN estado_persona e
JOIN (VALUES
    ('43300001', 'Aguirre', 'Ezequiel', '2002-06-12', 'Boedo 780, CABA', '11-4700-0001', 'ezequiel.aguirre@instituto.edu.ar'),
    ('43300002', 'Ponce', 'Abril', '2003-01-20', 'Centenario 350, San Isidro', '11-4700-0002', 'abril.ponce@instituto.edu.ar'),
    ('43300003', 'Ríos', 'Gonzalo', '2001-08-11', 'Av. La Plata 1200, CABA', '11-4700-0003', 'gonzalo.rios@instituto.edu.ar'),
    ('43300004', 'Maldonado', 'Micaela', '2004-05-02', 'Güemes 2100, CABA', '11-4700-0004', 'micaela.maldonado@instituto.edu.ar'),
    ('43300005', 'Cabrera', 'Agustín', '2002-10-18', 'Brandsen 400, Quilmes', '11-4700-0005', 'agustin.cabrera@instituto.edu.ar'),
    ('43300006', 'Morales', 'Zoe', '2003-11-29', 'Caseros 600, CABA', '11-4700-0006', 'zoe.morales@instituto.edu.ar'),
    ('43300007', 'Herrera', 'Lautaro', '2004-02-17', 'Montes de Oca 900, Barracas', '11-4700-0007', 'lautaro.herrera@instituto.edu.ar'),
    ('43300008', 'Paz', 'Victoria', '2001-09-05', 'Triunvirato 3400, Villa Urquiza', '11-4700-0008', 'victoria.paz@instituto.edu.ar'),
    ('43300009', 'Vega', 'Esteban', '2003-07-21', 'Beiró 4100, Villa Devoto', '11-4700-0009', 'esteban.vega@instituto.edu.ar'),
    ('43300010', 'Ferrer', 'Sol', '2002-03-31', 'Cuenca 2700, Villa del Parque', '11-4700-0010', 'sol.ferrer@instituto.edu.ar')
) AS v(dni, apellido, nombre, nac, dom, tel, email) ON TRUE
WHERE e.nombre = 'ACTIVO';

-- Ingeniería Química (DNI 43400001 - 43400010)
INSERT INTO alumno (dni, apellido, nombre, fecha_nacimiento, domicilio, telefono, email, id_plan, id_estado_persona)
SELECT v.dni, v.apellido, v.nombre, v.nac::date, v.dom, v.tel, v.email, p.id_plan, e.id_estado_persona
FROM plan_estudio p
JOIN carrera c ON c.id_carrera = p.id_carrera AND c.codigo = 'IQ'
CROSS JOIN estado_persona e
JOIN (VALUES
    ('43400001', 'Domínguez', 'Manuel', '2002-07-14', 'Independencia 1800, CABA', '11-4800-0001', 'manuel.dominguez@instituto.edu.ar'),
    ('43400002', 'Ortiz', 'Luciana', '2003-03-25', 'San Juan 2500, CABA', '11-4800-0002', 'luciana.ortiz@instituto.edu.ar'),
    ('43400003', 'Silva', 'Nicolás', '2001-10-10', 'Garay 1400, CABA', '11-4800-0003', 'nicolas.silva@instituto.edu.ar'),
    ('43400004', 'Ibarra', 'Delfina', '2004-04-05', 'Entre Ríos 700, CABA', '11-4800-0004', 'delfina.ibarra@instituto.edu.ar'),
    ('43400005', 'Cardozo', 'Julián', '2002-09-15', 'Jujuy 800, CABA', '11-4800-0005', 'julian.cardozo@instituto.edu.ar'),
    ('43400006', 'Luna', 'Brenda', '2003-06-30', 'Brasil 1100, Constitución', '11-4800-0006', 'brenda.luna@instituto.edu.ar'),
    ('43400007', 'Villalba', 'Federico', '2004-01-12', 'Paseo Colón 850, San Telmo', '11-4800-0007', 'federico.villalba@instituto.edu.ar'),
    ('43400008', 'Guerrero', 'Jazmín', '2001-11-23', 'Defensa 600, San Telmo', '11-4800-0008', 'jazmin.guerrero@instituto.edu.ar'),
    ('43400009', 'Coria', 'Leandro', '2003-08-08', 'Balcarce 400, CABA', '11-4800-0009', 'leandro.coria@instituto.edu.ar'),
    ('43400010', 'Bustos', 'Renata', '2002-02-18', 'Perú 900, San Telmo', '11-4800-0010', 'renata.bustos@instituto.edu.ar')
) AS v(dni, apellido, nombre, nac, dom, tel, email) ON TRUE
WHERE e.nombre = 'ACTIVO';

-- Ingeniería Civil (DNI 43500001 - 43500010)
INSERT INTO alumno (dni, apellido, nombre, fecha_nacimiento, domicilio, telefono, email, id_plan, id_estado_persona)
SELECT v.dni, v.apellido, v.nombre, v.nac::date, v.dom, v.tel, v.email, p.id_plan, e.id_estado_persona
FROM plan_estudio p
JOIN carrera c ON c.id_carrera = p.id_carrera AND c.codigo = 'IC'
CROSS JOIN estado_persona e
JOIN (VALUES
    ('43500001', 'Godoy', 'Bautista', '2002-08-20', 'Cabildo 2200, Belgrano', '11-4900-0001', 'bautista.godoy@instituto.edu.ar'),
    ('43500002', 'Pereyra', 'Clara', '2003-04-16', 'Juramento 1900, Belgrano', '11-4900-0002', 'clara.pereyra@instituto.edu.ar'),
    ('43500003', 'Ojeda', 'Thiago', '2001-12-05', 'Cramer 2100, Belgrano', '11-4900-0003', 'thiago.ojeda@instituto.edu.ar'),
    ('43500004', 'Benítez', 'Antonella', '2004-06-28', 'Monroe 3100, Coghlan', '11-4900-0004', 'antonella.benitez@instituto.edu.ar'),
    ('43500005', 'Arias', 'Felipe', '2002-01-11', 'Congreso 2400, Belgrano', '11-4900-0005', 'felipe.arias@instituto.edu.ar'),
    ('43500006', 'Correa', 'Guadalupe', '2003-10-03', 'Iberá 2800, Núñez', '11-4900-0006', 'guadalupe.correa@instituto.edu.ar'),
    ('43500007', 'Guzmán', 'Benjamín', '2004-07-19', 'García del Río 2900, Saavedra', '11-4900-0007', 'benjamin.guzman@instituto.edu.ar'),
    ('43500008', 'Bravo', 'Constanza', '2001-05-22', 'Crisólogo Larralde 2200, Núñez', '11-4900-0008', 'constanza.bravo@instituto.edu.ar'),
    ('43500009', 'Valdez', 'Simón', '2003-09-17', 'Libertador 6200, Núñez', '11-4900-0009', 'simon.valdez@instituto.edu.ar'),
    ('43500010', 'Ochoa', 'Pilar', '2002-11-08', 'Ugarte 1800, Belgrano', '11-4900-0010', 'pilar.ochoa@instituto.edu.ar')
) AS v(dni, apellido, nombre, nac, dom, tel, email) ON TRUE
WHERE e.nombre = 'ACTIVO';

-- Medicina (DNI 43600001 - 43600010)
INSERT INTO alumno (dni, apellido, nombre, fecha_nacimiento, domicilio, telefono, email, id_plan, id_estado_persona)
SELECT v.dni, v.apellido, v.nombre, v.nac::date, v.dom, v.tel, v.email, p.id_plan, e.id_estado_persona
FROM plan_estudio p
JOIN carrera c ON c.id_carrera = p.id_carrera AND c.codigo = 'MED'
CROSS JOIN estado_persona e
JOIN (VALUES
    ('43600001', 'Salas', 'Emiliano', '2002-03-15', 'Córdoba 2100, Recoleta', '11-5100-0001', 'emiliano.salas@instituto.edu.ar'),
    ('43600002', 'Lucero', 'Bianca', '2003-09-24', 'Santa Fe 1900, Recoleta', '11-5100-0002', 'bianca.lucero@instituto.edu.ar'),
    ('43600003', 'Molina', 'Gaspar', '2001-06-18', 'Junín 1100, Recoleta', '11-5100-0003', 'gaspar.molina@instituto.edu.ar'),
    ('43600004', 'Campos', 'Lara', '2004-02-09', 'Uriburu 850, Balvanera', '11-5100-0004', 'lara.campos@instituto.edu.ar'),
    ('43600005', 'Ferreyra', 'Ramiro', '2002-12-04', 'Azcuénaga 1200, Recoleta', '11-5100-0005', 'ramiro.ferreyra@instituto.edu.ar'),
    ('43600006', 'Duarte', 'Alma', '2003-08-14', 'Paso 600, Balvanera', '11-5100-0006', 'alma.duarte@instituto.edu.ar'),
    ('43600007', 'Soria', 'Franco', '2004-11-21', 'Castelli 300, Balvanera', '11-5100-0007', 'franco.soria@instituto.edu.ar'),
    ('43600008', 'Carrizo', 'Catalina', '2001-04-11', 'Pasteur 500, Balvanera', '11-5100-0008', 'catalina.carrizo@instituto.edu.ar'),
    ('43600009', 'Barrios', 'Leonel', '2003-05-03', 'Viamonte 1900, Recoleta', '11-5100-0009', 'leonel.barrios@instituto.edu.ar'),
    ('43600010', 'Montenegro', 'Emma', '2002-07-28', 'Tucumán 2200, Balvanera', '11-5100-0010', 'emma.montenegro@instituto.edu.ar')
) AS v(dni, apellido, nombre, nac, dom, tel, email) ON TRUE
WHERE e.nombre = 'ACTIVO';

-- Licenciatura en Enfermería (DNI 43700001 - 43700010)
INSERT INTO alumno (dni, apellido, nombre, fecha_nacimiento, domicilio, telefono, email, id_plan, id_estado_persona)
SELECT v.dni, v.apellido, v.nombre, v.nac::date, v.dom, v.tel, v.email, p.id_plan, e.id_estado_persona
FROM plan_estudio p
JOIN carrera c ON c.id_carrera = p.id_carrera AND c.codigo = 'ENF'
CROSS JOIN estado_persona e
JOIN (VALUES
    ('43700001', 'Peralta', 'Damián', '2002-09-02', 'Gaona 1400, Caballito', '11-5200-0001', 'damian.peralta@instituto.edu.ar'),
    ('43700002', 'Chávez', 'Morena', '2003-05-19', 'Rivadavia 5200, Caballito', '11-5200-0002', 'morena.chavez@instituto.edu.ar'),
    ('43700003', 'Juárez', 'Iván', '2001-08-30', 'Directorio 800, Caballito', '11-5200-0003', 'ivan.juarez@instituto.edu.ar'),
    ('43700004', 'Villarreal', 'Elena', '2004-03-14', 'Pedro Goyena 1100, Caballito', '11-5200-0004', 'elena.villarreal@instituto.edu.ar'),
    ('43700005', 'Avila', 'Manoel', '2002-11-17', 'Alberdi 1900, Flores', '11-5200-0005', 'manoel.avila@instituto.edu.ar'),
    ('43700006', 'Mercado', 'Guillermina', '2003-12-08', 'Carabobo 600, Flores', '11-5200-0006', 'guillermina.mercado@instituto.edu.ar'),
    ('43700007', 'Salvatierra', 'Máximo', '2004-06-01', 'Varela 900, Flores', '11-5200-0007', 'maximo.salvatierra@instituto.edu.ar'),
    ('43700008', 'Escobar', 'Juana', '2001-07-16', 'Nazca 1200, Flores', '11-5200-0008', 'juana.escobar@instituto.edu.ar'),
    ('43700009', 'Palacios', 'Alejo', '2003-01-22', 'Boyacá 1400, Flores', '11-5200-0009', 'alejo.palacios@instituto.edu.ar'),
    ('43700010', 'Giménez', 'Maia', '2002-10-09', 'Artigas 1800, Villa Mitre', '11-5200-0010', 'maia.gimenez@instituto.edu.ar')
) AS v(dni, apellido, nombre, nac, dom, tel, email) ON TRUE
WHERE e.nombre = 'ACTIVO';

-- Abogacía (DNI 43800001 - 43800010)
INSERT INTO alumno (dni, apellido, nombre, fecha_nacimiento, domicilio, telefono, email, id_plan, id_estado_persona)
SELECT v.dni, v.apellido, v.nombre, v.nac::date, v.dom, v.tel, v.email, p.id_plan, e.id_estado_persona
FROM plan_estudio p
JOIN carrera c ON c.id_carrera = p.id_carrera AND c.codigo = 'ABO'
CROSS JOIN estado_persona e
JOIN (VALUES
    ('43800001', 'Figueroa', 'Santiago', '2002-04-04', 'Talcahuano 750, Tribunales', '11-5300-0001', 'santiago.figueroa@instituto.edu.ar'),
    ('43800002', 'Santillán', 'Malena', '2003-10-15', 'Uruguay 400, San Nicolás', '11-5300-0002', 'malena.santillan@instituto.edu.ar'),
    ('43800003', 'Zárate', 'Ciro', '2001-11-20', 'Paraná 550, San Nicolás', '11-5300-0003', 'ciro.zarate@instituto.edu.ar'),
    ('43800004', 'Moya', 'Martina', '2004-08-07', 'Montevideo 600, San Nicolás', '11-5300-0004', 'martina.moya@instituto.edu.ar'),
    ('43800005', 'Quiroga', 'Álvaro', '2002-01-25', 'Rodríguez Peña 800, Recoleta', '11-5300-0005', 'alvaro.quiroga@instituto.edu.ar'),
    ('43800006', 'Godoy', 'Lucero', '2003-06-11', 'Callao 1100, Recoleta', '11-5300-0006', 'lucero.godoy@instituto.edu.ar'),
    ('43800007', 'Farías', 'Lisandro', '2004-09-03', 'Riobamba 900, Recoleta', '11-5300-0007', 'lisandro.farias@instituto.edu.ar'),
    ('43800008', 'Brizuela', 'Victoria', '2001-03-19', 'Ayacucho 1300, Recoleta', '11-5300-0008', 'victoria.brizuela@instituto.edu.ar'),
    ('43800009', 'Villafañe', 'Camilo', '2003-07-29', 'Juncal 1600, Recoleta', '11-5300-0009', 'camilo.villafane@instituto.edu.ar'),
    ('43800010', 'Cisneros', 'Amparo', '2002-05-13', 'Arenales 1800, Recoleta', '11-5300-0010', 'amparo.cisneros@instituto.edu.ar')
) AS v(dni, apellido, nombre, nac, dom, tel, email) ON TRUE
WHERE e.nombre = 'ACTIVO';

-- --------------------------------------------------------------------------
-- 6. USUARIOS DE PRUEBA
-- --------------------------------------------------------------------------
-- Clave de todos: Academit123 ($2b$10$6KLhDtEtvnNTo1sGZJo1wu/.H3xpna6fkSlFCJqwI/cK/iBCzpOG6)
INSERT INTO usuario (username, password_hash, id_rol, id_alumno, id_docente)
SELECT v.usr, '$2b$10$6KLhDtEtvnNTo1sGZJo1wu/.H3xpna6fkSlFCJqwI/cK/iBCzpOG6', r.id_rol,
       (SELECT id_alumno FROM alumno WHERE dni = v.dni_alumno),
       (SELECT id_docente FROM docente WHERE dni = v.dni_docente)
FROM (VALUES
    ('admin',          'ADMINISTRADOR', NULL,       NULL),
    ('administrativo', 'ADMINISTRATIVO', NULL,       NULL),
    ('docente1',       'DOCENTE',        NULL,       '25111222'),
    ('alumno1',        'ALUMNO',         '43100001', NULL), -- Lucía Benítez (Sistemas)
    ('alumno2',        'ALUMNO',         '43200001', NULL), -- Facundo Giménez (Electrónica)
    ('alumno3',        'ALUMNO',         '43600001', NULL), -- Emiliano Salas (Medicina)
    ('alumno4',        'ALUMNO',         '43800001', NULL)  -- Santiago Figueroa (Abogacía)
) AS v(usr, rol, dni_alumno, dni_docente)
JOIN rol r ON r.nombre = v.rol;

-- --------------------------------------------------------------------------
-- 7. CURSOS / COMISIONES 2026 (1 por carrera)
-- --------------------------------------------------------------------------
INSERT INTO curso (codigo, nombre, turno, capacidad_maxima, aula, id_ciclo, id_carrera, id_plan)
SELECT '1A-' || c.codigo || '-TM', '1° Año A (' || c.codigo || ')', 'MANANA', 35, 'Aula 10' || c.id_carrera || ' (Pabellón Central)', ci.id_ciclo, c.id_carrera, p.id_plan
FROM carrera c
JOIN plan_estudio p ON p.id_carrera = c.id_carrera
CROSS JOIN ciclo_lectivo ci
WHERE ci.anio = 2026;

-- Cátedras de primer año asignadas a docentes
INSERT INTO curso_materia (id_curso, id_materia, id_docente, rol_docente)
SELECT c.id_curso, m.id_materia, d.id_docente, 'TITULAR'
FROM curso c
JOIN carrera ca ON ca.id_carrera = c.id_carrera AND ca.codigo = 'ISI'
JOIN materia m ON m.nombre = 'Algoritmos y Estructuras de Datos'
CROSS JOIN docente d
WHERE d.dni = '25111222'; -- Laura Pérez

INSERT INTO curso_materia (id_curso, id_materia, id_docente, rol_docente)
SELECT c.id_curso, m.id_materia, d.id_docente, 'TITULAR'
FROM curso c
JOIN carrera ca ON ca.id_carrera = c.id_carrera AND ca.codigo = 'ISI'
JOIN materia m ON m.nombre = 'Análisis Matemático I'
CROSS JOIN docente d
WHERE d.dni = '27333444'; -- Carlos Gómez

INSERT INTO curso_materia (id_curso, id_materia, id_docente, rol_docente)
SELECT c.id_curso, m.id_materia, d.id_docente, 'TITULAR'
FROM curso c
JOIN carrera ca ON ca.id_carrera = c.id_carrera AND ca.codigo = 'MED'
JOIN materia m ON m.nombre = 'Anatomía Humana'
CROSS JOIN docente d
WHERE d.dni = '28555666'; -- Roberto Méndez

INSERT INTO curso_materia (id_curso, id_materia, id_docente, rol_docente)
SELECT c.id_curso, m.id_materia, d.id_docente, 'TITULAR'
FROM curso c
JOIN carrera ca ON ca.id_carrera = c.id_carrera AND ca.codigo = 'ABO'
JOIN materia m ON m.nombre = 'Derecho Constitucional'
CROSS JOIN docente d
WHERE d.dni = '29777888'; -- Patricia Albarracín

-- --------------------------------------------------------------------------
-- 8. INSCRIPCIONES A CURSADA Y MATRÍCULAS 2026
-- --------------------------------------------------------------------------
-- Inscribir a todos los 80 alumnos en la comisión de su carrera
INSERT INTO inscripcion (id_alumno, id_curso, id_ciclo)
SELECT a.id_alumno, c.id_curso, c.id_ciclo
FROM alumno a
JOIN plan_estudio p ON p.id_plan = a.id_plan
JOIN curso c ON c.id_plan = p.id_plan AND c.id_ciclo = (SELECT id_ciclo FROM ciclo_lectivo WHERE anio = 2026);

-- Generar Matrícula 2026 para todos los alumnos
INSERT INTO matricula (id_alumno, id_ciclo, importe, id_estado_matricula)
SELECT a.id_alumno, (SELECT id_ciclo FROM ciclo_lectivo WHERE anio = 2026), 35000.00, em.id_estado_matricula
FROM alumno a
CROSS JOIN estado_matricula em
WHERE em.nombre = 'ACTIVA';

-- Cuotas de la matrícula de Lucía Benítez (alumno1): 12 mensuales + 3 seguros
INSERT INTO cuota (id_matricula, numero, id_tipo_cuota, fecha_vencimiento, importe, id_estado_cuota)
SELECT m.id_matricula, n, t.id_tipo_cuota,
       (DATE '2026-03-10' + ((n - 1) * INTERVAL '1 month'))::date, 28000.00, e.id_estado_cuota
FROM matricula m
JOIN alumno a ON a.id_alumno = m.id_alumno AND a.dni = '43100001'
CROSS JOIN generate_series(1, 12) AS n
JOIN tipo_cuota t ON t.nombre = 'CUOTA_MENSUAL'
JOIN estado_cuota e ON e.nombre = CASE WHEN n <= 6 THEN 'PAGADA' ELSE 'PENDIENTE' END;

INSERT INTO cuota (id_matricula, numero, id_tipo_cuota, fecha_vencimiento, importe, id_estado_cuota)
SELECT m.id_matricula, n, t.id_tipo_cuota,
       (DATE '2026-03-31' + ((n - 1) * INTERVAL '4 months'))::date, 7000.00, e.id_estado_cuota
FROM matricula m
JOIN alumno a ON a.id_alumno = m.id_alumno AND a.dni = '43100001'
CROSS JOIN generate_series(1, 3) AS n
JOIN tipo_cuota t ON t.nombre = 'SEGURO_CUATRIMESTRAL'
JOIN estado_cuota e ON e.nombre = CASE WHEN n = 1 THEN 'PAGADA' ELSE 'PENDIENTE' END;

-- Pagos de las cuotas pagadas de Lucía
INSERT INTO pago (id_cuota, id_usuario, fecha, importe, id_medio_pago, comprobante)
SELECT c.id_cuota, u.id_usuario, c.fecha_vencimiento, c.importe, mp.id_medio_pago,
       '2026-' || LPAD((ROW_NUMBER() OVER (ORDER BY c.id_cuota))::text, 6, '0')
FROM cuota c
JOIN estado_cuota e ON e.id_estado_cuota = c.id_estado_cuota AND e.nombre = 'PAGADA'
JOIN usuario u ON u.username = 'administrativo'
JOIN medio_pago mp ON mp.nombre = 'TRANSFERENCIA';

-- Evaluaciones de prueba en cátedras
INSERT INTO evaluacion (id_curso_materia, fecha, id_tipo_evaluacion, descripcion)
SELECT cm.id_curso_materia, DATE '2026-05-20', te.id_tipo_evaluacion, 'Primer Parcial de Algoritmos'
FROM curso_materia cm
JOIN materia m ON m.id_materia = cm.id_materia AND m.nombre = 'Algoritmos y Estructuras de Datos'
CROSS JOIN tipo_evaluacion te
WHERE te.nombre = 'PARCIAL';

-- Calificaciones de prueba
INSERT INTO calificacion (id_evaluacion, id_alumno, nota)
SELECT e.id_evaluacion, a.id_alumno, v.nota
FROM evaluacion e
JOIN (VALUES ('43100001', 9), ('43100002', 7), ('43100003', 4)) AS v(dni, nota) ON TRUE
JOIN alumno a ON a.dni = v.dni;

-- Asistencias de prueba
INSERT INTO asistencia (id_alumno, id_curso, id_materia, fecha, id_estado_asistencia)
SELECT a.id_alumno, c.id_curso, m.id_materia, v.fecha::date, ea.id_estado_asistencia
FROM (VALUES
    ('43100001', '2026-03-09', 'PRESENTE'),
    ('43100002', '2026-03-09', 'PRESENTE'),
    ('43100003', '2026-03-09', 'AUSENTE'),
    ('43100001', '2026-03-11', 'PRESENTE'),
    ('43100002', '2026-03-11', 'TARDANZA')
) AS v(dni, fecha, estado)
JOIN alumno a ON a.dni = v.dni
JOIN curso c ON c.nombre LIKE '1° Año A (ISI)'
JOIN materia m ON m.nombre = 'Algoritmos y Estructuras de Datos'
JOIN estado_asistencia ea ON ea.nombre = v.estado;

-- Mesa de examen final abierta
INSERT INTO mesa_examen (id_materia, id_docente, fecha, cupo, tribunal, turno, estado)
SELECT m.id_materia, d.id_docente, DATE '2026-12-15', 30, 'Prof. Pérez / Prof. Gómez', 'MANANA', 'ABIERTA'
FROM materia m, docente d
WHERE m.nombre = 'Algoritmos y Estructuras de Datos' AND d.dni = '25111222';

-- Solicitudes de prueba
INSERT INTO solicitud (id_alumno, tipo, fecha_solicitud, motivo, observaciones, estado)
SELECT a.id_alumno, v.tipo, v.fecha::date, v.motivo, v.obs, v.estado
FROM alumno a
JOIN (VALUES
    ('43100001', 'CONSTANCIA', '2026-03-15', 'Constancia de alumno regular para boleto estudiantil', 'Presentado online', 'APROBADA'),
    ('43100001', 'PASANTIA',   '2026-04-01', 'Postulación a pasantía en desarrollo backend', 'Pasantía universitaria', 'EN_PROCESO'),
    ('43100002', 'CONSTANCIA', '2026-03-20', 'Constancia de materias aprobadas', NULL, 'PENDIENTE')
) AS v(dni, tipo, fecha, motivo, obs, estado) ON a.dni = v.dni;

-- Documentación académica obligatoria
INSERT INTO documentacion (id_alumno, tipo, obligatorio, estado, fecha_entrega, observaciones)
SELECT a.id_alumno, v.tipo, v.obligatorio::boolean, v.estado, v.fecha::date, v.obs
FROM alumno a
JOIN (VALUES
    ('43100001', 'DNI', true, 'ENTREGADO', '2026-02-15', 'Fotocopia DNI autenticada'),
    ('43100001', 'TITULO_SECUNDARIO', true, 'ENTREGADO', '2026-02-15', 'Certificado analítico legalizado'),
    ('43100001', 'APTO_MEDICO', true, 'ENTREGADO', '2026-03-01', 'Certificado médico oficial'),
    ('43100002', 'DNI', true, 'ENTREGADO', '2026-02-20', NULL),
    ('43100002', 'TITULO_SECUNDARIO', true, 'PENDIENTE', NULL, 'Constancia en trámite')
) AS v(dni, tipo, obligatorio, estado, fecha, obs) ON a.dni = v.dni;
