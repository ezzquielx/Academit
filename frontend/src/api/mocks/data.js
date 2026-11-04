/**
 * Banco unificado y coherente de datos mockeados para Academit.
 * Alumno de referencia: Lucía Benítez (ALU-1001, DNI 42111222).
 */

export const mockDb = {
  // 0. Carreras Institucionales
  carreras: [
    {
      idCarrera: 1,
      codigo: 'TSDS',
      nombre: 'Tecnicatura Superior en Desarrollo de Software',
      tituloOtorgado: 'Técnico Superior en Desarrollo de Software',
      duracionAnios: 3,
      resolucion: 'Res. Ministerial N° 1420/22',
      estado: 'ACTIVA',
      planesCount: 1,
      cursosCount: 3,
    },
    {
      idCarrera: 2,
      codigo: 'TSCDIA',
      nombre: 'Tecnicatura Superior en Ciencia de Datos e IA',
      tituloOtorgado: 'Técnico Superior en Ciencia de Datos e Inteligencia Artificial',
      duracionAnios: 3,
      resolucion: 'Res. Ministerial N° 2045/24',
      estado: 'ACTIVA',
      planesCount: 1,
      cursosCount: 1,
    },
  ],

  // 0.1 Planes de Estudio
  planesEstudio: [
    {
      idPlan: 1,
      idCarrera: 1,
      carreraNombre: 'Tecnicatura Superior en Desarrollo de Software',
      codigo: 'PLAN-2024',
      nombre: 'Plan de Estudios 2024',
      anioResolucion: 2024,
      cantidadMaterias: 9,
      horasTotales: 1600,
      estado: 'VIGENTE',
    },
    {
      idPlan: 2,
      idCarrera: 2,
      carreraNombre: 'Tecnicatura Superior en Ciencia de Datos e IA',
      codigo: 'PLAN-2025',
      nombre: 'Plan de Estudios 2025',
      anioResolucion: 2025,
      cantidadMaterias: 5,
      horasTotales: 1650,
      estado: 'VIGENTE',
    },
  ],

  // 1. Alumnos
  alumnos: [
    {
      idAlumno: 1,
      legajo: 'ALU-1001',
      nombre: 'Lucía',
      apellido: 'Benítez',
      dni: '42111222',
      email: 'lucia.benitez@instituto.edu.ar',
      telefono: '11-4567-8901',
      fechaNacimiento: '2001-04-18',
      domicilio: 'Av. Corrientes 1234, CABA',
      carrera: 'Tecnicatura Superior en Desarrollo de Software',
      idCarrera: 1,
      idPlan: 1,
      planNombre: 'Plan de Estudios 2024',
      idCurso: 1,
      cursoNombre: '1° Año - Turno Mañana',
      idMatricula: 1,
      estadoMatricula: 'ACTIVA',
      cicloLectivo: 2026,
      estado: 'ACTIVO',
    },
    {
      idAlumno: 2,
      legajo: 'ALU-1002',
      nombre: 'Martín',
      apellido: 'Rodríguez',
      dni: '43333444',
      email: 'martin.rodriguez@instituto.edu.ar',
      telefono: '11-4567-8902',
      fechaNacimiento: '2002-08-22',
      domicilio: 'Calle Falsa 123, CABA',
      carrera: 'Tecnicatura Superior en Desarrollo de Software',
      idCarrera: 1,
      idPlan: 1,
      planNombre: 'Plan de Estudios 2024',
      idCurso: 1,
      cursoNombre: '1° Año - Turno Mañana',
      idMatricula: 2,
      estadoMatricula: 'ACTIVA',
      cicloLectivo: 2026,
      estado: 'ACTIVO',
    },
    {
      idAlumno: 3,
      legajo: 'ALU-1003',
      nombre: 'Camila',
      apellido: 'Suárez',
      dni: '41555666',
      email: 'camila.suarez@instituto.edu.ar',
      telefono: '11-4567-8903',
      fechaNacimiento: '2000-11-05',
      domicilio: 'Belgrano 456, CABA',
      carrera: 'Tecnicatura Superior en Desarrollo de Software',
      idCarrera: 1,
      idPlan: 1,
      planNombre: 'Plan de Estudios 2024',
      idCurso: 2,
      cursoNombre: '2° Año - Turno Tarde',
      idMatricula: 3,
      estadoMatricula: 'PENDIENTE',
      cicloLectivo: 2026,
      estado: 'ACTIVO',
    },
    {
      idAlumno: 4,
      legajo: 'ALU-1004',
      nombre: 'Esteban',
      apellido: 'Morales',
      dni: '44777888',
      email: 'esteban.morales@instituto.edu.ar',
      telefono: '11-4567-8904',
      fechaNacimiento: '2003-01-30',
      domicilio: 'Rivadavia 7890, CABA',
      carrera: 'Tecnicatura Superior en Desarrollo de Software',
      idCarrera: 1,
      idPlan: 1,
      planNombre: 'Plan de Estudios 2024',
      idCurso: 1,
      cursoNombre: '1° Año - Turno Mañana',
      idMatricula: 4,
      estadoMatricula: 'CANCELADA',
      cicloLectivo: 2026,
      estado: 'INACTIVO',
    },
    {
      idAlumno: 5,
      legajo: 'ALU-1005',
      nombre: 'Facundo',
      apellido: 'Díaz',
      dni: '42888999',
      email: 'facundo.diaz@instituto.edu.ar',
      telefono: '11-4567-8905',
      fechaNacimiento: '2001-09-12',
      domicilio: 'Cabildo 2500, CABA',
      carrera: 'Tecnicatura Superior en Ciencia de Datos e IA',
      idCarrera: 2,
      idPlan: 2,
      planNombre: 'Plan de Estudios 2025',
      idCurso: 4,
      cursoNombre: '1° Año CD - Turno Mañana',
      idMatricula: 5,
      estadoMatricula: 'ACTIVA',
      cicloLectivo: 2026,
      estado: 'ACTIVO',
    },
  ],

  // 2. Docentes
  docentes: [
    {
      idDocente: 1,
      legajo: 'DOC-201',
      nombre: 'Laura',
      apellido: 'Pérez',
      dni: '25111222',
      email: 'laura.perez@instituto.edu.ar',
      titulo: 'Ingeniera en Sistemas',
      telefono: '3815000001',
      estado: 'ACTIVO',
    },
    {
      idDocente: 2,
      legajo: 'DOC-202',
      nombre: 'Carlos',
      apellido: 'Gómez',
      dni: '27333444',
      email: 'carlos.gomez@instituto.edu.ar',
      titulo: 'Licenciado en Matemática',
      telefono: '3815000002',
      estado: 'ACTIVO',
    },
    {
      idDocente: 3,
      legajo: 'DOC-203',
      nombre: 'Roberto',
      apellido: 'Sánchez',
      dni: '28999000',
      email: 'roberto.sanchez@instituto.edu.ar',
      titulo: 'Matemática y Lógica',
      telefono: '11-5555-9012',
      estado: 'ACTIVO',
    },
    {
      idDocente: 4,
      legajo: 'DOC-204',
      nombre: 'Elena',
      apellido: 'Fabbri',
      dni: '31222333',
      email: 'elena.fabbri@instituto.edu.ar',
      titulo: 'Arquitectura y Sistemas Operativos',
      telefono: '11-5555-3456',
      estado: 'ACTIVO',
    },
  ],

  // 3. Materias
  materias: [
    { idMateria: 1, idCarrera: 1, idPlan: 1, carreraNombre: 'Tecnicatura Superior en Desarrollo de Software', codigo: 'MAT-101', nombre: 'Programación I', anio: 1, regimen: 'Anual', horasSemanales: 6, correlativas: [] },
    { idMateria: 2, idCarrera: 1, idPlan: 1, carreraNombre: 'Tecnicatura Superior en Desarrollo de Software', codigo: 'MAT-102', nombre: 'Bases de Datos I', anio: 1, regimen: '1° Cuatrimestre', horasSemanales: 4, correlativas: [] },
    { idMateria: 3, idCarrera: 1, idPlan: 1, carreraNombre: 'Tecnicatura Superior en Desarrollo de Software', codigo: 'MAT-103', nombre: 'Sistemas de Computación', anio: 1, regimen: '1° Cuatrimestre', horasSemanales: 4, correlativas: [] },
    { idMateria: 4, idCarrera: 1, idPlan: 1, carreraNombre: 'Tecnicatura Superior en Desarrollo de Software', codigo: 'MAT-104', nombre: 'Matemática Discreta', anio: 1, regimen: '2° Cuatrimestre', horasSemanales: 4, correlativas: [] },
    { idMateria: 5, idCarrera: 1, idPlan: 1, carreraNombre: 'Tecnicatura Superior en Desarrollo de Software', codigo: 'MAT-201', nombre: 'Programación II', anio: 2, regimen: 'Anual', horasSemanales: 6, correlativas: [1] },
    { idMateria: 6, idCarrera: 1, idPlan: 1, carreraNombre: 'Tecnicatura Superior en Desarrollo de Software', codigo: 'MAT-202', nombre: 'Bases de Datos II', anio: 2, regimen: '1° Cuatrimestre', horasSemanales: 4, correlativas: [2] },
    { idMateria: 7, idCarrera: 1, idPlan: 1, carreraNombre: 'Tecnicatura Superior en Desarrollo de Software', codigo: 'MAT-203', nombre: 'Ingeniería de Software', anio: 2, regimen: '2° Cuatrimestre', horasSemanales: 4, correlativas: [1] },
    { idMateria: 8, idCarrera: 1, idPlan: 1, carreraNombre: 'Tecnicatura Superior en Desarrollo de Software', codigo: 'MAT-301', nombre: 'Práctica Profesionalizante', anio: 3, regimen: 'Anual', horasSemanales: 8, correlativas: [5, 6] },
    { idMateria: 9, idCarrera: 1, idPlan: 1, carreraNombre: 'Tecnicatura Superior en Desarrollo de Software', codigo: 'MAT-302', nombre: 'Seguridad y Auditoría', anio: 3, regimen: '1° Cuatrimestre', horasSemanales: 4, correlativas: [3] },
    // Materias Carrera 2: Ciencia de Datos e IA
    { idMateria: 10, idCarrera: 2, idPlan: 2, carreraNombre: 'Tecnicatura Superior en Ciencia de Datos e IA', codigo: 'CD-101', nombre: 'Estadística y Probabilidad I', anio: 1, regimen: '1° Cuatrimestre', horasSemanales: 4, correlativas: [] },
    { idMateria: 11, idCarrera: 2, idPlan: 2, carreraNombre: 'Tecnicatura Superior en Ciencia de Datos e IA', codigo: 'CD-102', nombre: 'Introducción a la Ciencia de Datos', anio: 1, regimen: '1° Cuatrimestre', horasSemanales: 4, correlativas: [] },
    { idMateria: 12, idCarrera: 2, idPlan: 2, carreraNombre: 'Tecnicatura Superior en Ciencia de Datos e IA', codigo: 'CD-103', nombre: 'Álgebra Lineal Computacional', anio: 1, regimen: '2° Cuatrimestre', horasSemanales: 4, correlativas: [] },
    { idMateria: 13, idCarrera: 2, idPlan: 2, carreraNombre: 'Tecnicatura Superior en Ciencia de Datos e IA', codigo: 'CD-104', nombre: 'Programación para Datos (Python)', anio: 1, regimen: '2° Cuatrimestre', horasSemanales: 6, correlativas: [11] },
    { idMateria: 14, idCarrera: 2, idPlan: 2, carreraNombre: 'Tecnicatura Superior en Ciencia de Datos e IA', codigo: 'CD-201', nombre: 'Machine Learning I', anio: 2, regimen: '1° Cuatrimestre', horasSemanales: 6, correlativas: [13, 10] },
  ],

  // 4. Cursos (Comisiones)
  cursos: [
    {
      idCurso: 1,
      idCarrera: 1,
      carreraNombre: 'Tecnicatura Superior en Desarrollo de Software',
      idPlan: 1,
      codigo: '1A-INF-TM',
      nombre: '1° Año - Turno Mañana',
      turno: 'MANANA',
      capacidadMaxima: 35,
      inscriptos: 29,
      cuposDisponibles: 6,
      cicloLectivo: 2026,
      aula: 'Aula 101 (Pabellón A)',
    },
    {
      idCurso: 2,
      idCarrera: 1,
      carreraNombre: 'Tecnicatura Superior en Desarrollo de Software',
      idPlan: 1,
      codigo: '2A-INF-TT',
      nombre: '2° Año - Turno Tarde',
      turno: 'TARDE',
      capacidadMaxima: 30,
      inscriptos: 28,
      cuposDisponibles: 2,
      cicloLectivo: 2026,
      aula: 'Laboratorio de Sistemas 2',
    },
    {
      idCurso: 3,
      idCarrera: 1,
      carreraNombre: 'Tecnicatura Superior en Desarrollo de Software',
      idPlan: 1,
      codigo: '1B-INF-TN',
      nombre: '1° Año - Turno Noche',
      turno: 'NOCHE',
      capacidadMaxima: 40,
      inscriptos: 40,
      cuposDisponibles: 0,
      cicloLectivo: 2026,
      aula: 'Aula Magna 3',
    },
    {
      idCurso: 4,
      idCarrera: 2,
      carreraNombre: 'Tecnicatura Superior en Ciencia de Datos e IA',
      idPlan: 2,
      codigo: '1A-CD-TM',
      nombre: '1° Año CD - Turno Mañana',
      turno: 'MANANA',
      capacidadMaxima: 30,
      inscriptos: 15,
      cuposDisponibles: 15,
      cicloLectivo: 2026,
      aula: 'Laboratorio de Datos 1',
    },
  ],

  // 5. Ciclos Lectivos
  ciclosLectivos: [
    { idCiclo: 1, anio: 2026, nombre: 'Ciclo Lectivo 2026', fechaInicio: '2026-03-01', fechaFin: '2026-12-20', estado: 'ACTIVO' },
    { idCiclo: 2, anio: 2025, nombre: 'Ciclo Lectivo 2025', fechaInicio: '2025-03-01', fechaFin: '2025-12-20', estado: 'CERRADO' },
  ],

  // 6. Asignaciones Docente - Curso - Materia
  asignaciones: [
    { idAsignacion: 1, idDocente: 1, docenteNombre: 'Laura Pérez', idMateria: 1, materiaNombre: 'Programación I', idCurso: 1, cursoNombre: '1° Año - Turno Mañana', rolDocente: 'Titular' },
    { idAsignacion: 2, idDocente: 2, docenteNombre: 'Carlos Gómez', idMateria: 2, materiaNombre: 'Bases de Datos I', idCurso: 1, cursoNombre: '1° Año - Turno Mañana', rolDocente: 'Titular' },
    { idAsignacion: 3, idDocente: 4, docenteNombre: 'Elena Fabbri', idMateria: 3, materiaNombre: 'Sistemas de Computación', idCurso: 1, cursoNombre: '1° Año - Turno Mañana', rolDocente: 'Titular' },
    { idAsignacion: 4, idDocente: 1, docenteNombre: 'Laura Pérez', idMateria: 5, materiaNombre: 'Programación II', idCurso: 2, cursoNombre: '2° Año - Turno Tarde', rolDocente: 'Titular' },
  ],

  // 7. Inscripciones de Alumnos a Cursos
  inscripciones: [
    { idInscripcion: 1, idAlumno: 1, alumnoNombre: 'Lucía Benítez', idCurso: 1, cursoNombre: '1° Año - Turno Mañana', cicloLectivo: 2026, fechaInscripcion: '2026-02-15', estado: 'CONFIRMADA' },
    { idInscripcion: 2, idAlumno: 2, alumnoNombre: 'Martín Rodríguez', idCurso: 1, cursoNombre: '1° Año - Turno Mañana', cicloLectivo: 2026, fechaInscripcion: '2026-02-16', estado: 'CONFIRMADA' },
    { idInscripcion: 3, idAlumno: 3, alumnoNombre: 'Camila Suárez', idCurso: 2, cursoNombre: '2° Año - Turno Tarde', cicloLectivo: 2026, fechaInscripcion: '2026-02-18', estado: 'CONFIRMADA' },
    { idInscripcion: 4, idAlumno: 4, alumnoNombre: 'Esteban Morales', idCurso: 1, cursoNombre: '1° Año - Turno Mañana', cicloLectivo: 2026, fechaInscripcion: '2026-02-20', estado: 'CANCELADA' },
  ],

  // 8. Evaluaciones (creadas por docentes)
  evaluaciones: [
    { idEvaluacion: 1, idMateria: 1, materiaNombre: 'Programación I', idCurso: 1, cursoNombre: '1° Año - TM', fecha: '2026-04-15', tipo: 'PARCIAL', descripcion: 'Primer Parcial - Lógica, Funciones y Punteros', ponderacion: '30%' },
    { idEvaluacion: 2, idMateria: 1, materiaNombre: 'Programación I', idCurso: 1, cursoNombre: '1° Año - TM', fecha: '2026-05-10', tipo: 'TRABAJO_PRACTICO', descripcion: 'TP Integrador 1 - Sistema de Archivos en C', ponderacion: '20%' },
    { idEvaluacion: 3, idMateria: 2, materiaNombre: 'Bases de Datos I', idCurso: 1, cursoNombre: '1° Año - TM', fecha: '2026-04-22', tipo: 'PARCIAL', descripcion: 'Primer Parcial - Modelo Relacional y Normalización', ponderacion: '40%' },
    { idEvaluacion: 4, idMateria: 2, materiaNombre: 'Bases de Datos I', idCurso: 1, cursoNombre: '1° Año - TM', fecha: '2026-05-18', tipo: 'TRABAJO_PRACTICO', descripcion: 'TP 1 - DDL y DML en PostgreSQL', ponderacion: '20%' },
  ],

  // 9. Calificaciones obtenidas
  calificaciones: [
    // Alumno 1: Lucía Benítez
    { idCalificacion: 1, idAlumno: 1, alumnoNombre: 'Lucía Benítez', idMateria: 1, materiaNombre: 'Programación I', evaluacionNombre: 'Primer Parcial', tipo: 'PARCIAL', nota: 8, fecha: '2026-04-15', condicion: 'APROBADO', observacion: 'Excelente desarrollo de algoritmos' },
    { idCalificacion: 2, idAlumno: 1, alumnoNombre: 'Lucía Benítez', idMateria: 1, materiaNombre: 'Programación I', evaluacionNombre: 'TP Integrador 1', tipo: 'TRABAJO_PRACTICO', nota: 10, fecha: '2026-05-10', condicion: 'APROBADO', observacion: 'Código limpio y pruebas completas' },
    { idCalificacion: 3, idAlumno: 1, alumnoNombre: 'Lucía Benítez', idMateria: 2, materiaNombre: 'Bases de Datos I', evaluacionNombre: 'Primer Parcial', tipo: 'PARCIAL', nota: 9, fecha: '2026-04-22', condicion: 'APROBADO', observacion: 'Muy buen diagrama DER' },
    { idCalificacion: 4, idAlumno: 1, alumnoNombre: 'Lucía Benítez', idMateria: 2, materiaNombre: 'Bases de Datos I', evaluacionNombre: 'TP 1 - DDL y DML', tipo: 'TRABAJO_PRACTICO', nota: 9, fecha: '2026-05-18', condicion: 'APROBADO', observacion: 'Consultas optimizadas' },
    // Alumno 2: Martín Rodríguez
    { idCalificacion: 5, idAlumno: 2, alumnoNombre: 'Martín Rodríguez', idMateria: 1, materiaNombre: 'Programación I', evaluacionNombre: 'Primer Parcial', tipo: 'PARCIAL', nota: 7, fecha: '2026-04-15', condicion: 'APROBADO', observacion: 'Aprobado con sugerencias' },
    { idCalificacion: 6, idAlumno: 2, alumnoNombre: 'Martín Rodríguez', idMateria: 2, materiaNombre: 'Bases de Datos I', evaluacionNombre: 'Primer Parcial', tipo: 'PARCIAL', nota: 6, fecha: '2026-04-22', condicion: 'APROBADO', observacion: 'Ajustar normalización' },
  ],

  // 10. Asistencias
  asistencias: [
    { idAsistencia: 1, idAlumno: 1, alumnoNombre: 'Lucía Benítez', idMateria: 1, materiaNombre: 'Programación I', idCurso: 1, fecha: '2026-04-01', estado: 'PRESENTE' },
    { idAsistencia: 2, idAlumno: 1, alumnoNombre: 'Lucía Benítez', idMateria: 1, materiaNombre: 'Programación I', idCurso: 1, fecha: '2026-04-03', estado: 'PRESENTE' },
    { idAsistencia: 3, idAlumno: 1, alumnoNombre: 'Lucía Benítez', idMateria: 1, materiaNombre: 'Programación I', idCurso: 1, fecha: '2026-04-08', estado: 'TARDANZA' },
    { idAsistencia: 4, idAlumno: 1, alumnoNombre: 'Lucía Benítez', idMateria: 1, materiaNombre: 'Programación I', idCurso: 1, fecha: '2026-04-10', estado: 'JUSTIFICADO' },
    { idAsistencia: 5, idAlumno: 1, alumnoNombre: 'Lucía Benítez', idMateria: 1, materiaNombre: 'Programación I', idCurso: 1, fecha: '2026-04-15', estado: 'PRESENTE' },
    { idAsistencia: 6, idAlumno: 1, alumnoNombre: 'Lucía Benítez', idMateria: 1, materiaNombre: 'Programación I', idCurso: 1, fecha: '2026-04-17', estado: 'PRESENTE' },
    { idAsistencia: 7, idAlumno: 1, alumnoNombre: 'Lucía Benítez', idMateria: 2, materiaNombre: 'Bases de Datos I', idCurso: 1, fecha: '2026-04-02', estado: 'PRESENTE' },
    { idAsistencia: 8, idAlumno: 1, alumnoNombre: 'Lucía Benítez', idMateria: 2, materiaNombre: 'Bases de Datos I', idCurso: 1, fecha: '2026-04-09', estado: 'PRESENTE' },
    { idAsistencia: 9, idAlumno: 1, alumnoNombre: 'Lucía Benítez', idMateria: 2, materiaNombre: 'Bases de Datos I', idCurso: 1, fecha: '2026-04-16', estado: 'AUSENTE' },
    { idAsistencia: 10, idAlumno: 1, alumnoNombre: 'Lucía Benítez', idMateria: 2, materiaNombre: 'Bases de Datos I', idCurso: 1, fecha: '2026-04-23', estado: 'PRESENTE' },
  ],

  // 11. Mesas de Examen
  mesasExamen: [
    {
      idMesa: 1,
      idMateria: 1,
      materiaNombre: 'Programación I',
      fecha: '2026-05-15',
      hora: '09:00 hs',
      turno: 'MANANA',
      tribunal: 'Prof. Carlos Gómez / Prof. Mariana Peralta',
      aula: 'Aula Magna 1',
      cupoMaximo: 30,
      inscriptos: 12,
      estadoMesa: 'ABIERTA',
      alumnosInscriptosIds: [1], // Lucía Benítez inscripta
    },
    {
      idMesa: 2,
      idMateria: 2,
      materiaNombre: 'Bases de Datos I',
      fecha: '2026-05-22',
      hora: '14:00 hs',
      turno: 'TARDE',
      tribunal: 'Prof. Mariana Peralta / Prof. Roberto Sánchez',
      aula: 'Laboratorio 2',
      cupoMaximo: 25,
      inscriptos: 8,
      estadoMesa: 'ABIERTA',
      alumnosInscriptosIds: [],
    },
    {
      idMesa: 3,
      idMateria: 3,
      materiaNombre: 'Sistemas de Computación',
      fecha: '2026-05-29',
      hora: '09:00 hs',
      turno: 'MANANA',
      tribunal: 'Prof. Elena Fabbri / Prof. Carlos Gómez',
      aula: 'Aula 104',
      cupoMaximo: 25,
      inscriptos: 15,
      estadoMesa: 'ABIERTA',
      alumnosInscriptosIds: [],
    },
    {
      idMesa: 4,
      idMateria: 4,
      materiaNombre: 'Matemática Discreta',
      fecha: '2026-06-05',
      hora: '18:00 hs',
      turno: 'NOCHE',
      tribunal: 'Prof. Roberto Sánchez / Prof. Carlos Gómez',
      aula: 'Aula 202',
      cupoMaximo: 30,
      inscriptos: 5,
      estadoMesa: 'ABIERTA',
      alumnosInscriptosIds: [],
    },
  ],

  // 12. Matrículas anuales
  matriculas: [
    {
      idMatricula: 1,
      idAlumno: 1,
      alumnoNombre: 'Lucía Benítez',
      cicloLectivo: 2026,
      importe: 35000,
      fechaAlta: '2026-02-10',
      estado: 'ACTIVA',
      estadoPago: 'AL_DIA',
    },
    {
      idMatricula: 2,
      idAlumno: 2,
      alumnoNombre: 'Martín Rodríguez',
      cicloLectivo: 2026,
      importe: 35000,
      fechaAlta: '2026-02-12',
      estado: 'ACTIVA',
      estadoPago: 'AL_DIA',
    },
    {
      idMatricula: 3,
      idAlumno: 3,
      alumnoNombre: 'Camila Suárez',
      cicloLectivo: 2026,
      importe: 35000,
      fechaAlta: '2026-02-15',
      estado: 'PENDIENTE',
      estadoPago: 'PENDIENTE',
    },
    {
      idMatricula: 4,
      idAlumno: 4,
      alumnoNombre: 'Esteban Morales',
      cicloLectivo: 2026,
      importe: 35000,
      fechaAlta: '2026-02-01',
      estado: 'CANCELADA',
      estadoPago: 'PENDIENTE',
    },
  ],

  // 13. Cuotas y Seguros (12 cuotas mensuales + 3 seguros cuatrimestrales para Lucía Benítez)
  cuotas: [
    // Cuotas mensuales de Lucía Benítez (idAlumno: 1, idMatricula: 1)
    { idCuota: 1, idMatricula: 1, idAlumno: 1, alumnoNombre: 'Lucía Benítez', concepto: 'Cuota 1 - Enero 2026', tipo: 'MENSUAL', periodo: 'Enero 2026', numero: 1, importe: 28000, fechaVencimiento: '2026-01-10', estado: 'PAGADA', fechaPago: '2026-01-08', comprobante: '2026-000002' },
    { idCuota: 2, idMatricula: 1, idAlumno: 1, alumnoNombre: 'Lucía Benítez', concepto: 'Cuota 2 - Febrero 2026', tipo: 'MENSUAL', periodo: 'Febrero 2026', numero: 2, importe: 28000, fechaVencimiento: '2026-02-10', estado: 'PAGADA', fechaPago: '2026-02-09', comprobante: '2026-000003' },
    { idCuota: 3, idMatricula: 1, idAlumno: 1, alumnoNombre: 'Lucía Benítez', concepto: 'Cuota 3 - Marzo 2026', tipo: 'MENSUAL', periodo: 'Marzo 2026', numero: 3, importe: 28000, fechaVencimiento: '2026-03-10', estado: 'PAGADA', fechaPago: '2026-03-05', comprobante: '2026-000004' },
    { idCuota: 4, idMatricula: 1, idAlumno: 1, alumnoNombre: 'Lucía Benítez', concepto: 'Cuota 4 - Abril 2026', tipo: 'MENSUAL', periodo: 'Abril 2026', numero: 4, importe: 28000, fechaVencimiento: '2026-04-10', estado: 'VENCIDA', fechaPago: null, comprobante: null },
    { idCuota: 5, idMatricula: 1, idAlumno: 1, alumnoNombre: 'Lucía Benítez', concepto: 'Cuota 5 - Mayo 2026', tipo: 'MENSUAL', periodo: 'Mayo 2026', numero: 5, importe: 28000, fechaVencimiento: '2026-05-10', estado: 'PENDIENTE', fechaPago: null, comprobante: null },
    { idCuota: 6, idMatricula: 1, idAlumno: 1, alumnoNombre: 'Lucía Benítez', concepto: 'Cuota 6 - Junio 2026', tipo: 'MENSUAL', periodo: 'Junio 2026', numero: 6, importe: 28000, fechaVencimiento: '2026-06-10', estado: 'PENDIENTE', fechaPago: null, comprobante: null },
    { idCuota: 7, idMatricula: 1, idAlumno: 1, alumnoNombre: 'Lucía Benítez', concepto: 'Cuota 7 - Julio 2026', tipo: 'MENSUAL', periodo: 'Julio 2026', numero: 7, importe: 28000, fechaVencimiento: '2026-07-10', estado: 'PENDIENTE', fechaPago: null, comprobante: null },
    { idCuota: 8, idMatricula: 1, idAlumno: 1, alumnoNombre: 'Lucía Benítez', concepto: 'Cuota 8 - Agosto 2026', tipo: 'MENSUAL', periodo: 'Agosto 2026', numero: 8, importe: 28000, fechaVencimiento: '2026-08-10', estado: 'PENDIENTE', fechaPago: null, comprobante: null },
    { idCuota: 9, idMatricula: 1, idAlumno: 1, alumnoNombre: 'Lucía Benítez', concepto: 'Cuota 9 - Septiembre 2026', tipo: 'MENSUAL', periodo: 'Septiembre 2026', numero: 9, importe: 28000, fechaVencimiento: '2026-09-10', estado: 'PENDIENTE', fechaPago: null, comprobante: null },
    { idCuota: 10, idMatricula: 1, idAlumno: 1, alumnoNombre: 'Lucía Benítez', concepto: 'Cuota 10 - Octubre 2026', tipo: 'MENSUAL', periodo: 'Octubre 2026', numero: 10, importe: 28000, fechaVencimiento: '2026-10-10', estado: 'PENDIENTE', fechaPago: null, comprobante: null },
    { idCuota: 11, idMatricula: 1, idAlumno: 1, alumnoNombre: 'Lucía Benítez', concepto: 'Cuota 11 - Noviembre 2026', tipo: 'MENSUAL', periodo: 'Noviembre 2026', numero: 11, importe: 28000, fechaVencimiento: '2026-11-10', estado: 'PENDIENTE', fechaPago: null, comprobante: null },
    { idCuota: 12, idMatricula: 1, idAlumno: 1, alumnoNombre: 'Lucía Benítez', concepto: 'Cuota 12 - Diciembre 2026', tipo: 'MENSUAL', periodo: 'Diciembre 2026', numero: 12, importe: 28000, fechaVencimiento: '2026-12-10', estado: 'PENDIENTE', fechaPago: null, comprobante: null },
    // 3 Seguros cuatrimestrales de Lucía Benítez (idAlumno: 1, idMatricula: 1)
    { idCuota: 13, idMatricula: 1, idAlumno: 1, alumnoNombre: 'Lucía Benítez', concepto: 'Seguro Cuatrimestral 1/3', tipo: 'SEGURO_CUATRIMESTRAL', periodo: '1° Cuatrimestre 2026', numero: 1, importe: 8000, fechaVencimiento: '2026-03-10', estado: 'PAGADA', fechaPago: '2026-03-05', comprobante: '2026-000005' },
    { idCuota: 14, idMatricula: 1, idAlumno: 1, alumnoNombre: 'Lucía Benítez', concepto: 'Seguro Cuatrimestral 2/3', tipo: 'SEGURO_CUATRIMESTRAL', periodo: '2° Cuatrimestre 2026', numero: 2, importe: 8000, fechaVencimiento: '2026-07-10', estado: 'PENDIENTE', fechaPago: null, comprobante: null },
    { idCuota: 15, idMatricula: 1, idAlumno: 1, alumnoNombre: 'Lucía Benítez', concepto: 'Seguro Cuatrimestral 3/3', tipo: 'SEGURO_CUATRIMESTRAL', periodo: '3° Cuatrimestre 2026', numero: 3, importe: 8000, fechaVencimiento: '2026-11-10', estado: 'PENDIENTE', fechaPago: null, comprobante: null },
    // Cuotas de otros alumnos
    { idCuota: 16, idMatricula: 2, concepto: 'Cuota 4 - Abril 2026', tipo: 'MENSUAL', periodo: 'Abril 2026', numero: 4, importe: 28000, fechaVencimiento: '2026-04-10', estado: 'PAGADA', fechaPago: '2026-04-02', comprobante: '2026-000006' },
    { idCuota: 17, idMatricula: 2, concepto: 'Cuota 3 - Marzo 2026', tipo: 'MENSUAL', periodo: 'Marzo 2026', numero: 3, importe: 28000, fechaVencimiento: '2026-03-10', estado: 'ANULADA', fechaPago: null, comprobante: null },
  ],

  // 14. Registro de Pagos
  pagos: [
    { idPago: 1, idCuota: null, idAlumno: 1, alumnoNombre: 'Lucía Benítez', concepto: 'Matrícula Anual 2026', importe: 35000, fechaPago: '2026-02-15', medioPago: 'TRANSFERENCIA', comprobante: '2026-000001', estado: 'ACREDITADO' },
    { idPago: 2, idCuota: 1, idAlumno: 1, alumnoNombre: 'Lucía Benítez', concepto: 'Cuota 1 - Enero 2026', importe: 28000, fechaPago: '2026-01-08', medioPago: 'TRANSFERENCIA', comprobante: '2026-000002', estado: 'ACREDITADO' },
    { idPago: 3, idCuota: 2, idAlumno: 1, alumnoNombre: 'Lucía Benítez', concepto: 'Cuota 2 - Febrero 2026', importe: 28000, fechaPago: '2026-02-09', medioPago: 'TARJETA', comprobante: '2026-000003', estado: 'ACREDITADO' },
    { idPago: 4, idCuota: 3, idAlumno: 1, alumnoNombre: 'Lucía Benítez', concepto: 'Cuota 3 - Marzo 2026', importe: 28000, fechaPago: '2026-03-05', medioPago: 'EFECTIVO', comprobante: '2026-000004', estado: 'ACREDITADO' },
    { idPago: 5, idCuota: 13, idAlumno: 1, alumnoNombre: 'Lucía Benítez', concepto: 'Seguro Cuatrimestral 1/3', importe: 8000, fechaPago: '2026-03-05', medioPago: 'EFECTIVO', comprobante: '2026-000005', estado: 'ACREDITADO' },
    { idPago: 6, idCuota: 16, idAlumno: 2, alumnoNombre: 'Martín Rodríguez', concepto: 'Cuota 4 - Abril 2026', importe: 28000, fechaPago: '2026-04-02', medioPago: 'TRANSFERENCIA', comprobante: '2026-000006', estado: 'ACREDITADO' },
    { idPago: 7, idCuota: null, idAlumno: 2, alumnoNombre: 'Martín Rodríguez', concepto: 'Matrícula Anual 2026', importe: 35000, fechaPago: '2026-03-01', medioPago: 'OTRO', comprobante: '2026-000007', estado: 'ACREDITADO' },
  ],

  // 15. Solicitudes de Alumnos
  solicitudes: [
    { idSolicitud: 1, idAlumno: 1, alumnoNombre: 'Lucía Benítez', tipo: 'CONSTANCIA', fechaSolicitud: '2026-03-10', motivo: 'Presentación laboral en empresa tecnológica', estado: 'APROBADA', fechaResolucion: '2026-03-12', respuesta: 'Constancia emitida y firmada digitalmente.' },
    { idSolicitud: 2, idAlumno: 1, alumnoNombre: 'Lucía Benítez', tipo: 'CAMBIO_CARRERA', fechaSolicitud: '2026-03-15', motivo: 'Pase a Ciencia de Datos e IA', estado: 'APROBADA', fechaResolucion: '2026-03-18', respuesta: 'Aprobada según plan de correlatividades.' },
    { idSolicitud: 3, idAlumno: 1, alumnoNombre: 'Lucía Benítez', tipo: 'PASANTIA', fechaSolicitud: '2026-04-02', motivo: 'Postulación a Pasantía en Software Dev S.A.', estado: 'EN_PROCESO', fechaResolucion: null, respuesta: 'En revisión por el coordinador de pasantías.' },
    { idSolicitud: 4, idAlumno: 1, alumnoNombre: 'Lucía Benítez', tipo: 'TITULO_INTERMEDIO', fechaSolicitud: '2026-04-12', motivo: 'Cumplimiento del 70% de la carga horaria', estado: 'PENDIENTE', fechaResolucion: null, respuesta: null },
    { idSolicitud: 5, idAlumno: 2, alumnoNombre: 'Martín Rodríguez', tipo: 'ACADEMICA', fechaSolicitud: '2026-04-05', motivo: 'Revisión de asistencia en Bases de Datos', estado: 'RECHAZADA', fechaResolucion: '2026-04-08', respuesta: 'El límite de justificación venció el 01/04.' },
    { idSolicitud: 6, idAlumno: 2, alumnoNombre: 'Martín Rodríguez', tipo: 'REINCORPORACION', fechaSolicitud: '2026-04-10', motivo: 'Reincorporación tras licencia laboral', estado: 'PENDIENTE', fechaResolucion: null, respuesta: null },
  ],

  // 15b. Declaraciones Juradas de Materias (RF51)
  declaracionesJuradas: [
    {
      idDeclaracion: 1,
      idAlumno: 1,
      alumnoNombre: 'Lucía Benítez',
      idCiclo: 1,
      cicloNombre: 'Ciclo Lectivo 2026',
      fecha: '2026-03-05',
      materiasDeclaradasIds: [1, 2],
      materiasNombres: ['Programación I', 'Bases de Datos I'],
      estado: 'VALIDADA',
      observaciones: 'Cumple con el régimen de correlatividades vigentes.',
    },
    {
      idDeclaracion: 2,
      idAlumno: 2,
      alumnoNombre: 'Martín Rodríguez',
      idCiclo: 1,
      cicloNombre: 'Ciclo Lectivo 2026',
      fecha: '2026-03-06',
      materiasDeclaradasIds: [1, 2],
      materiasNombres: ['Programación I', 'Bases de Datos I'],
      estado: 'VALIDADA',
      observaciones: 'Presentación en término reglamentario.',
    },
  ],

  // 16. Documentación legal/académica del Alumno
  documentacion: [
    { idDocumento: 1, idAlumno: 1, tipo: 'DNI', obligatorio: true, estado: 'ENTREGADO', fechaEntrega: '2026-02-10', observaciones: 'Copia certificada' },
    { idDocumento: 2, idAlumno: 1, tipo: 'TITULO_SECUNDARIO', obligatorio: true, estado: 'ENTREGADO', fechaEntrega: '2026-02-10', observaciones: 'Legalizado por Ministerio' },
    { idDocumento: 3, idAlumno: 1, tipo: 'APTO_MEDICO', obligatorio: true, estado: 'PENDIENTE', fechaEntrega: null, observaciones: 'Vencimiento próximo: 30/05/2026' },
    { idDocumento: 4, idAlumno: 1, tipo: 'FOTOS', obligatorio: true, estado: 'ENTREGADO', fechaEntrega: '2026-02-10', observaciones: 'Entregadas en secretaría' },
  ],

  // 17. Usuarios del Sistema y Roles
  usuarios: [
    { idUsuario: 1, username: 'admin', nombre: 'Administrador General', email: 'admin@instituto.edu.ar', rol: 'ADMINISTRADOR', estado: 'ACTIVO', ultimoAcceso: '2026-10-12 21:00', password: '1234' },
    { idUsuario: 2, username: 'administrativo', nombre: 'Personal Administrativo', email: 'administracion@instituto.edu.ar', rol: 'ADMINISTRATIVO', estado: 'ACTIVO', ultimoAcceso: '2026-10-12 20:30', password: '1234' },
    { idUsuario: 3, username: 'docente', nombre: 'Prof. Laura Pérez', email: 'laura.perez@instituto.edu.ar', rol: 'DOCENTE', estado: 'ACTIVO', ultimoAcceso: '2026-10-12 19:45', password: '1234', idDocente: 1 },
    { idUsuario: 4, username: 'alumno', nombre: 'Lucía Benítez', email: 'lucia.benitez@instituto.edu.ar', rol: 'ALUMNO', estado: 'ACTIVO', ultimoAcceso: '2026-10-12 21:15', password: '1234', idAlumno: 1 },
    { idUsuario: 5, username: 'docente1', nombre: 'Prof. Laura Pérez', email: 'laura.perez@instituto.edu.ar', rol: 'DOCENTE', estado: 'ACTIVO', ultimoAcceso: '2026-10-12 19:45', password: '1234', idDocente: 1 },
    { idUsuario: 6, username: 'alumno1', nombre: 'Lucía Benítez', email: 'lucia.benitez@instituto.edu.ar', rol: 'ALUMNO', estado: 'ACTIVO', ultimoAcceso: '2026-10-12 21:15', password: '1234', idAlumno: 1 },
  ],
}

/**
 * Persistencia liviana de altas en modo mock.
 * sessionStorage sobrevive a una recarga de la pestaña, así un alumno recién
 * registrado puede iniciar sesión sin que el banco en memoria se reinicie.
 */
const ALTAS_KEY = 'academit_mock_altas'

const cargarAltasMock = () => {
  try {
    const raw = typeof sessionStorage !== 'undefined' ? sessionStorage.getItem(ALTAS_KEY) : null
    if (!raw) return
    const { usuarios = [], alumnos = [] } = JSON.parse(raw)
    for (const u of usuarios) {
      if (!mockDb.usuarios.some((x) => x.username === u.username)) mockDb.usuarios.push(u)
    }
    for (const a of alumnos) {
      if (!mockDb.alumnos.some((x) => x.idAlumno === a.idAlumno)) mockDb.alumnos.push(a)
    }
  } catch {
    /* almacenamiento no disponible: se sigue con el banco en memoria */
  }
}

/** Guarda los registros creados por altas (no los datos de ejemplo). */
export const guardarAltasMock = () => {
  try {
    if (typeof sessionStorage === 'undefined') return
    const usuarios = mockDb.usuarios.filter((u) => u.origen === 'registro')
    const alumnos = mockDb.alumnos.filter((a) => a.origen === 'registro')
    sessionStorage.setItem(ALTAS_KEY, JSON.stringify({ usuarios, alumnos }))
  } catch {
    /* almacenamiento no disponible */
  }
}

cargarAltasMock()
