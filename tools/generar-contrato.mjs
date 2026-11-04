import fs from 'fs';
import path from 'path';
import { fileURLToPath } from 'url';
import { execSync } from 'child_process';

const __filename = fileURLToPath(import.meta.url);
const __dirname = path.dirname(__filename);
const rootDir = path.resolve(__dirname, '..');
const apiDir = path.join(rootDir, 'frontend', 'src', 'api');
const docsFrontendDir = path.join(rootDir, 'docs', 'frontend');

// Ensure docs/frontend directory exists
if (!fs.existsSync(docsFrontendDir)) {
  fs.mkdirSync(docsFrontendDir, { recursive: true });
}

// 1. EXTRAER METADATOS DE JSDOC DE frontend/src/api/*.js
const apiFiles = fs.readdirSync(apiDir).filter(f => f.endsWith('.js') && f !== 'client.js' && f !== 'errors.js');
const jsdocMap = new Map();

for (const file of apiFiles) {
  const content = fs.readFileSync(path.join(apiDir, file), 'utf8');
  const regex = /\/\*\*([\s\S]*?)\*\/\s*export const ([a-zA-Z0-9_]+)/g;
  let m;
  while ((m = regex.exec(content)) !== null) {
    const doc = m[1];
    const fn = m[2];
    const restMatch = doc.match(/REST:\s*([A-Z]+)\s+([^\s]+)/);
    const descMatch = doc.trim().split('\n')[0].replace(/^\s*\*\s*/, '').trim();
    if (restMatch) {
      jsdocMap.set(fn, {
        file,
        fn,
        method: restMatch[1].toUpperCase(),
        url: restMatch[2],
        desc: descMatch,
      });
    }
  }
}

// 2. TABLA MAESTRA DE METADATOS COMPLEMENTARIOS (Roles, Pantallas, Parámetros, Cuerpos, Respuestas, Códigos)
const ENDPOINTS_META = [
  {
    fn: 'getAlumnos',
    method: 'GET',
    url: '/api/alumnos',
    desc: 'Obtiene la lista completa de alumnos con filtros opcionales de búsqueda.',
    roles: ['ADMINISTRADOR', 'ADMINISTRATIVO'],
    pantallas: 'GestionAlumnosPage.jsx, GestionInscripcionesPage.jsx, AdminDeudasPage.jsx, AdminPagosPage.jsx, AdminSituacionAlumnoPage.jsx, AsistenciasDocentePage.jsx, CalificacionesDocentePage.jsx, InicioPage.jsx',
    queryParams: [
      { name: 'search', type: 'string', desc: 'Búsqueda por nombre, apellido o DNI' },
      { name: 'idCarrera', type: 'integer', desc: 'Filtro por ID de carrera' },
      { name: 'estado', type: 'string', desc: 'Filtro por condición institucional (ACTIVO, INACTIVO)' }
    ],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<AlumnoDTO>',
    successStatus: '200',
    codigos: '200, 401, 403',
    estado: 'real',
    tag: 'Alumnos'
  },
  {
    fn: 'getAlumnoById',
    method: 'GET',
    url: '/api/alumnos/{id}',
    desc: 'Obtiene el detalle completo del legajo de un alumno por ID.',
    roles: ['ADMINISTRADOR', 'ADMINISTRATIVO'],
    pantallas: 'GestionAlumnosPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador único del alumno' }],
    requestBody: null,
    responseSchema: 'AlumnoDTO',
    successStatus: '200',
    codigos: '200, 401, 403, 404',
    estado: 'real',
    tag: 'Alumnos'
  },
  {
    fn: 'createAlumno',
    method: 'POST',
    url: '/api/alumnos',
    desc: 'Registra el alta de un nuevo alumno en el instituto.',
    roles: ['ADMINISTRADOR', 'ADMINISTRATIVO'],
    pantallas: 'GestionAlumnosPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: 'AlumnoCreateDTO',
    responseSchema: 'AlumnoDTO',
    successStatus: '201',
    codigos: '201, 400, 401, 403, 409',
    estado: 'real',
    tag: 'Alumnos'
  },
  {
    fn: 'updateAlumno',
    method: 'PUT',
    url: '/api/alumnos/{id}',
    desc: 'Actualiza la información de legajo o filiación de un alumno.',
    roles: ['ADMINISTRADOR', 'ADMINISTRATIVO'],
    pantallas: 'GestionAlumnosPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador único del alumno' }],
    requestBody: 'AlumnoUpdateDTO',
    responseSchema: 'AlumnoDTO',
    successStatus: '200',
    codigos: '200, 400, 401, 403, 404',
    estado: 'real',
    tag: 'Alumnos'
  },
  {
    fn: 'deleteAlumno',
    method: 'DELETE',
    url: '/api/alumnos/{id}',
    desc: 'Da de baja lógica a un alumno (inactiva su estado académico).',
    roles: ['ADMINISTRADOR'],
    pantallas: 'GestionAlumnosPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador único del alumno' }],
    requestBody: null,
    responseSchema: '204 No Content',
    successStatus: '204',
    codigos: '200, 204, 401, 403, 404',
    estado: 'real',
    tag: 'Alumnos'
  },
  {
    fn: 'getSituacionAcademica',
    method: 'GET',
    url: '/api/alumnos/{id}/situacion-academica',
    desc: 'Consulta consolidada de materias aprobadas, cursadas, correlatividades y promedio.',
    roles: ['ALUMNO', 'ADMINISTRADOR', 'ADMINISTRATIVO'],
    pantallas: 'SituacionAcademicaArancelariaPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador único del alumno' }],
    requestBody: null,
    responseSchema: 'SituacionAcademicaDTO',
    successStatus: '200',
    codigos: '200, 401, 403, 404',
    estado: 'real',
    tag: 'Alumnos'
  },
  {
    fn: 'getSituacionAdministrativa',
    method: 'GET',
    url: '/api/alumnos/{id}/situacion-administrativa',
    desc: 'Consulta consolidada del estado financiero, cuotas pendientes, deudas y matrícula del alumno.',
    roles: ['ADMINISTRATIVO', 'ADMINISTRADOR', 'ALUMNO'],
    pantallas: 'AdminSituacionAlumnoPage.jsx, SituacionAcademicaArancelariaPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador único del alumno' }],
    requestBody: null,
    responseSchema: 'SituacionAdministrativaDTO',
    successStatus: '200',
    codigos: '200, 401, 403, 404',
    estado: 'real',
    tag: 'Alumnos'
  },
  {
    fn: 'getDocentes',
    method: 'GET',
    url: '/api/docentes',
    desc: 'Obtiene la nómina de docentes del instituto.',
    roles: ['ADMINISTRADOR', 'ADMINISTRATIVO'],
    pantallas: 'GestionDocentesPage.jsx, GestionAsignacionesPage.jsx',
    queryParams: [{ name: 'estado', type: 'string', desc: 'Filtro por estado de actividad docente' }],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<DocenteDTO>',
    successStatus: '200',
    codigos: '200, 401, 403',
    estado: 'real',
    tag: 'Docentes'
  },
  {
    fn: 'createDocente',
    method: 'POST',
    url: '/api/docentes',
    desc: 'Registra el alta de un nuevo docente.',
    roles: ['ADMINISTRADOR'],
    pantallas: 'GestionDocentesPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: 'DocenteCreateDTO',
    responseSchema: 'DocenteDTO',
    successStatus: '201',
    codigos: '201, 400, 401, 403, 409',
    estado: 'real',
    tag: 'Docentes'
  },
  {
    fn: 'updateDocente',
    method: 'PUT',
    url: '/api/docentes/{id}',
    desc: 'Modifica datos de legajo o contacto de un docente.',
    roles: ['ADMINISTRADOR'],
    pantallas: 'GestionDocentesPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador único del docente' }],
    requestBody: 'DocenteUpdateDTO',
    responseSchema: 'DocenteDTO',
    successStatus: '200',
    codigos: '200, 400, 401, 403, 404',
    estado: 'real',
    tag: 'Docentes'
  },
  {
    fn: 'deleteDocente',
    method: 'DELETE',
    url: '/api/docentes/{id}',
    desc: 'Da de baja lógica a un docente en el sistema.',
    roles: ['ADMINISTRADOR'],
    pantallas: 'GestionDocentesPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador único del docente' }],
    requestBody: null,
    responseSchema: '204 No Content',
    successStatus: '204',
    codigos: '200, 204, 401, 403, 404',
    estado: 'real',
    tag: 'Docentes'
  },
  {
    fn: 'getCarreras',
    method: 'GET',
    url: '/api/carreras',
    desc: 'Obtiene la oferta completa de carreras del instituto.',
    roles: ['ADMINISTRADOR', 'ADMINISTRATIVO'],
    pantallas: 'GestionCarrerasPage.jsx, GestionAlumnosPage.jsx, GestionCursosPage.jsx, GestionPlanEstudiosPage.jsx',
    queryParams: [{ name: 'estado', type: 'string', desc: 'Filtro por estado de carrera (ACTIVA, INACTIVA)' }],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<CarreraDTO>',
    successStatus: '200',
    codigos: '200, 401, 403',
    estado: 'real',
    tag: 'Carreras'
  },
  {
    fn: 'getCarreraById',
    method: 'GET',
    url: '/api/carreras/{id}',
    desc: 'Obtiene el detalle de una carrera por ID.',
    roles: ['ADMINISTRADOR', 'ADMINISTRATIVO'],
    pantallas: 'GestionCarrerasPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador de la carrera' }],
    requestBody: null,
    responseSchema: 'CarreraDTO',
    successStatus: '200',
    codigos: '200, 401, 403, 404',
    estado: 'real',
    tag: 'Carreras'
  },
  {
    fn: 'createCarrera',
    method: 'POST',
    url: '/api/carreras',
    desc: 'Crea una nueva titulación o carrera en el instituto.',
    roles: ['ADMINISTRADOR'],
    pantallas: 'GestionCarrerasPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: 'CarreraCreateDTO',
    responseSchema: 'CarreraDTO',
    successStatus: '201',
    codigos: '201, 400, 401, 403, 409',
    estado: 'real',
    tag: 'Carreras'
  },
  {
    fn: 'updateCarrera',
    method: 'PUT',
    url: '/api/carreras/{id}',
    desc: 'Actualiza denominación o datos formales de una carrera.',
    roles: ['ADMINISTRADOR'],
    pantallas: 'GestionCarrerasPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador de la carrera' }],
    requestBody: 'CarreraUpdateDTO',
    responseSchema: 'CarreraDTO',
    successStatus: '200',
    codigos: '200, 400, 401, 403, 404',
    estado: 'real',
    tag: 'Carreras'
  },
  {
    fn: 'deleteCarrera',
    method: 'DELETE',
    url: '/api/carreras/{id}',
    desc: 'Da de baja o inactiva una carrera.',
    roles: ['ADMINISTRADOR'],
    pantallas: 'GestionCarrerasPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador de la carrera' }],
    requestBody: null,
    responseSchema: '204 No Content',
    successStatus: '204',
    codigos: '200, 204, 401, 403, 404',
    estado: 'real',
    tag: 'Carreras'
  },
  {
    fn: 'getCarreraPlanes',
    method: 'GET',
    url: '/api/carreras/{id}/planes',
    desc: 'Obtiene los planes de estudio correspondientes a una carrera.',
    roles: ['ADMINISTRADOR', 'ADMINISTRATIVO'],
    pantallas: 'GestionCarrerasPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador de la carrera' }],
    requestBody: null,
    responseSchema: 'Array<PlanEstudioDTO>',
    successStatus: '200',
    codigos: '200, 401, 403, 404',
    estado: 'real',
    tag: 'Carreras'
  },
  {
    fn: 'getCarreraCursos',
    method: 'GET',
    url: '/api/carreras/{id}/cursos',
    desc: 'Obtiene las comisiones/cursos habilitados para una carrera.',
    roles: ['ADMINISTRADOR', 'ADMINISTRATIVO'],
    pantallas: 'GestionCarrerasPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador de la carrera' }],
    requestBody: null,
    responseSchema: 'Array<CursoDTO>',
    successStatus: '200',
    codigos: '200, 401, 403, 404',
    estado: 'real',
    tag: 'Carreras'
  },
  {
    fn: 'getPlanesEstudio',
    method: 'GET',
    url: '/api/planes-estudio',
    desc: 'Obtiene el listado general de planes de estudio.',
    roles: ['ADMINISTRADOR', 'ADMINISTRATIVO'],
    pantallas: 'GestionPlanEstudiosPage.jsx, GestionAlumnosPage.jsx',
    queryParams: [{ name: 'idCarrera', type: 'integer', desc: 'Filtro por ID de carrera' }],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<PlanEstudioDTO>',
    successStatus: '200',
    codigos: '200, 401, 403',
    estado: 'real',
    tag: 'Planes'
  },
  {
    fn: 'getMateriasDePlan',
    method: 'GET',
    url: '/api/planes-estudio/{id}/materias',
    desc: 'Obtiene las materias de un plan de estudio estructuradas con año y correlativas.',
    roles: ['ALUMNO', 'DOCENTE', 'ADMINISTRATIVO', 'ADMINISTRADOR'],
    pantallas: 'PlanEstudiosAlumnoPage.jsx, GestionPlanEstudiosPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador del plan de estudio' }],
    requestBody: null,
    responseSchema: 'Array<MateriaDTO>',
    successStatus: '200',
    codigos: '200, 401, 403, 404',
    estado: 'real',
    tag: 'Planes'
  },
  {
    fn: 'getMaterias',
    method: 'GET',
    url: '/api/materias',
    desc: 'Obtiene materias filtradas por carrera o plan de estudios.',
    roles: ['ADMINISTRADOR', 'DOCENTE', 'ALUMNO'],
    pantallas: 'GestionMateriasPage.jsx, GestionPlanEstudiosPage.jsx, GestionAsignacionesPage.jsx, GestionMesasPage.jsx, PlanEstudiosAlumnoPage.jsx, AsistenciasDocentePage.jsx, CalificacionesDocentePage.jsx, EvaluacionesDocentePage.jsx, DeclaracionJuradaPage.jsx',
    queryParams: [
      { name: 'idCarrera', type: 'integer', desc: 'Filtro por ID de carrera' },
      { name: 'idPlan', type: 'integer', desc: 'Filtro por ID de plan' }
    ],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<MateriaDTO>',
    successStatus: '200',
    codigos: '200, 401, 403',
    estado: 'real',
    tag: 'Materias'
  },
  {
    fn: 'createMateria',
    method: 'POST',
    url: '/api/materias',
    desc: 'Crea una nueva materia en el catálogo del plan de estudios.',
    roles: ['ADMINISTRADOR'],
    pantallas: 'GestionMateriasPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: 'MateriaCreateDTO',
    responseSchema: 'MateriaDTO',
    successStatus: '201',
    codigos: '201, 400, 401, 403, 409',
    estado: 'real',
    tag: 'Materias'
  },
  {
    fn: 'updateMateria',
    method: 'PUT',
    url: '/api/materias/{id}',
    desc: 'Actualiza los datos académicos o correlatividades de una materia.',
    roles: ['ADMINISTRADOR'],
    pantallas: 'GestionMateriasPage.jsx, GestionPlanEstudiosPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador de la materia' }],
    requestBody: 'MateriaUpdateDTO',
    responseSchema: 'MateriaDTO',
    successStatus: '200',
    codigos: '200, 400, 401, 403, 404',
    estado: 'real',
    tag: 'Materias'
  },
  {
    fn: 'getCursos',
    method: 'GET',
    url: '/api/cursos',
    desc: 'Obtiene la lista de comisiones/cursos y su ocupación de cupos.',
    roles: ['ADMINISTRADOR', 'DOCENTE', 'ADMINISTRATIVO'],
    pantallas: 'GestionCursosPage.jsx, GestionInscripcionesPage.jsx, GestionAsignacionesPage.jsx, MisCursosPage.jsx, AsistenciasDocentePage.jsx, CalificacionesDocentePage.jsx, EvaluacionesDocentePage.jsx',
    queryParams: [
      { name: 'idCarrera', type: 'integer', desc: 'Filtro por ID de carrera' },
      { name: 'turno', type: 'string', desc: 'Filtro por turno (MANANA, TARDE, NOCHE)' }
    ],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<CursoDTO>',
    successStatus: '200',
    codigos: '200, 401, 403',
    estado: 'real',
    tag: 'Cursos'
  },
  {
    fn: 'createCurso',
    method: 'POST',
    url: '/api/cursos',
    desc: 'Crea una nueva división/comisión de curso.',
    roles: ['ADMINISTRADOR'],
    pantallas: 'GestionCursosPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: 'CursoCreateDTO',
    responseSchema: 'CursoDTO',
    successStatus: '201',
    codigos: '201, 400, 401, 403, 409',
    estado: 'real',
    tag: 'Cursos'
  },
  {
    fn: 'updateCurso',
    method: 'PUT',
    url: '/api/cursos/{id}',
    desc: 'Actualiza la capacidad, turno o aula de una comisión.',
    roles: ['ADMINISTRADOR'],
    pantallas: 'GestionCursosPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador del curso' }],
    requestBody: 'CursoUpdateDTO',
    responseSchema: 'CursoDTO',
    successStatus: '200',
    codigos: '200, 400, 401, 403, 404',
    estado: 'real',
    tag: 'Cursos'
  },
  {
    fn: 'getCiclosLectivos',
    method: 'GET',
    url: '/api/ciclos-lectivos',
    desc: 'Lista los ciclos lectivos registrados en la institución.',
    roles: ['ADMINISTRADOR', 'ADMINISTRATIVO'],
    pantallas: 'GestionCiclosLectivosPage.jsx, GestionInscripcionesPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<CicloLectivoDTO>',
    successStatus: '200',
    codigos: '200, 401, 403',
    estado: 'real',
    tag: 'Ciclos'
  },
  {
    fn: 'createCicloLectivo',
    method: 'POST',
    url: '/api/ciclos-lectivos',
    desc: 'Abre o registra un nuevo ciclo lectivo anual.',
    roles: ['ADMINISTRADOR'],
    pantallas: 'GestionCiclosLectivosPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: 'CicloLectivoCreateDTO',
    responseSchema: 'CicloLectivoDTO',
    successStatus: '201',
    codigos: '201, 400, 401, 403, 409',
    estado: 'real',
    tag: 'Ciclos'
  },
  {
    fn: 'updateCicloLectivo',
    method: 'PUT',
    url: '/api/ciclos-lectivos/{id}',
    desc: 'Modifica fechas o cierra el ciclo lectivo.',
    roles: ['ADMINISTRADOR'],
    pantallas: 'GestionCiclosLectivosPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador del ciclo' }],
    requestBody: 'CicloLectivoUpdateDTO',
    responseSchema: 'CicloLectivoDTO',
    successStatus: '200',
    codigos: '200, 400, 401, 403, 404',
    estado: 'real',
    tag: 'Ciclos'
  },
  {
    fn: 'getAsignaciones',
    method: 'GET',
    url: '/api/asignaciones',
    desc: 'Consulta las asignaciones de docentes a cursos y materias.',
    roles: ['ADMINISTRADOR', 'ADMINISTRATIVO', 'DOCENTE'],
    pantallas: 'GestionAsignacionesPage.jsx, MisCursosPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<AsignacionDTO>',
    successStatus: '200',
    codigos: '200, 401, 403',
    estado: 'real',
    tag: 'Asignaciones'
  },
  {
    fn: 'createAsignacion',
    method: 'POST',
    url: '/api/asignaciones',
    desc: 'Designa un docente titular o adjunto en una cátedra.',
    roles: ['ADMINISTRADOR', 'ADMINISTRATIVO'],
    pantallas: 'GestionAsignacionesPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: 'AsignacionCreateDTO',
    responseSchema: 'AsignacionDTO',
    successStatus: '201',
    codigos: '201, 400, 401, 403, 409',
    estado: 'real',
    tag: 'Asignaciones'
  },
  {
    fn: 'deleteAsignacion',
    method: 'DELETE',
    url: '/api/asignaciones/{id}',
    desc: 'Revoca la asignación docente de una materia.',
    roles: ['ADMINISTRADOR', 'ADMINISTRATIVO'],
    pantallas: 'GestionAsignacionesPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador de la asignación' }],
    requestBody: null,
    responseSchema: '204 No Content',
    successStatus: '204',
    codigos: '200, 204, 401, 403, 404',
    estado: 'real',
    tag: 'Asignaciones'
  },
  {
    fn: 'getInscripciones',
    method: 'GET',
    url: '/api/inscripciones',
    desc: 'Consulta las inscripciones cursantes por alumno o curso.',
    roles: ['ADMINISTRADOR', 'ADMINISTRATIVO'],
    pantallas: 'GestionInscripcionesPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<InscripcionDTO>',
    successStatus: '200',
    codigos: '200, 401, 403',
    estado: 'real',
    tag: 'Inscripciones'
  },
  {
    fn: 'createInscripcion',
    method: 'POST',
    url: '/api/inscripciones',
    desc: 'Inscribe formalmente a un alumno en un curso validando cupo.',
    roles: ['ADMINISTRADOR', 'ADMINISTRATIVO'],
    pantallas: 'GestionInscripcionesPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: 'InscripcionCreateDTO',
    responseSchema: 'InscripcionDTO',
    successStatus: '201',
    codigos: '201, 400, 401, 403, 409',
    estado: 'real',
    tag: 'Inscripciones'
  },
  {
    fn: 'cancelarInscripcion',
    method: 'DELETE',
    url: '/api/inscripciones/{id}',
    desc: 'Cancela la inscripción a un curso y reintegra el cupo libre.',
    roles: ['ADMINISTRADOR', 'ADMINISTRATIVO'],
    pantallas: 'GestionInscripcionesPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador de la inscripción' }],
    requestBody: null,
    responseSchema: '204 No Content',
    successStatus: '204',
    codigos: '200, 204, 401, 403, 404',
    estado: 'real',
    tag: 'Inscripciones'
  },
  {
    fn: 'getMatriculas',
    method: 'GET',
    url: '/api/matriculas',
    desc: 'Padrón de matrículas anuales del instituto.',
    roles: ['ADMINISTRADOR', 'ADMINISTRATIVO'],
    pantallas: 'AdminMatriculasPage.jsx, CancelarMatriculaPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<MatriculaDTO>',
    successStatus: '200',
    codigos: '200, 401, 403',
    estado: 'real',
    tag: 'Matriculas'
  },
  {
    fn: 'updateMatricula',
    method: 'PUT',
    url: '/api/matriculas/{id}',
    desc: 'Actualiza el estado administrativo de una matrícula.',
    roles: ['ADMINISTRATIVO', 'ADMINISTRADOR'],
    pantallas: 'AdminMatriculasPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador de la matrícula' }],
    requestBody: 'MatriculaUpdateDTO',
    responseSchema: 'MatriculaDTO',
    successStatus: '200',
    codigos: '200, 400, 401, 403, 404',
    estado: 'real',
    tag: 'Matriculas'
  },
  {
    fn: 'cancelarMatricula',
    method: 'PATCH',
    url: '/api/matriculas/{id}',
    desc: 'Registra la cancelación definitiva de matrícula de un alumno (RF27).',
    roles: ['ALUMNO', 'ADMINISTRATIVO', 'ADMINISTRADOR'],
    pantallas: 'CancelarMatriculaPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador de la matrícula' }],
    requestBody: 'MatriculaCancelDTO',
    responseSchema: 'SuccessResponseDTO',
    successStatus: '200',
    codigos: '200, 400, 401, 403, 404',
    estado: 'real',
    tag: 'Matriculas'
  },
  {
    fn: 'generarCuotas',
    method: 'POST',
    url: '/api/matriculas/{id}/cuotas',
    desc: 'Genera el plan de cuotas y seguros para una matrícula anual.',
    roles: ['ADMINISTRATIVO', 'ADMINISTRADOR'],
    pantallas: 'AdminMatriculasPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador de la matrícula' }],
    requestBody: null,
    responseSchema: 'Array<CuotaDTO>',
    successStatus: '200',
    codigos: '200, 401, 403, 404, 409',
    estado: 'real',
    tag: 'Matriculas'
  },
  {
    fn: 'getCuotas',
    method: 'GET',
    url: '/api/cuotas',
    desc: 'Consulta de cuotas emitidas con cálculo de alumno derivado de matrícula.',
    roles: ['ADMINISTRATIVO', 'ADMINISTRADOR', 'ALUMNO'],
    pantallas: 'AdminCuotasPage.jsx, AdminDeudasPage.jsx, AdminPagosPage.jsx, SituacionAcademicaArancelariaPage.jsx',
    queryParams: [{ name: 'idAlumno', type: 'integer', desc: 'Filtro por ID de alumno' }],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<CuotaDTO>',
    successStatus: '200',
    codigos: '200, 401, 403',
    estado: 'real',
    tag: 'Cuotas'
  },
  {
    fn: 'anularCuota',
    method: 'PATCH',
    url: '/api/cuotas/{id}/anular',
    desc: 'Anula una cuota administrativa emitida si no se encuentra pagada.',
    roles: ['ADMINISTRADOR'],
    pantallas: 'AdminCuotasPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador de la cuota' }],
    requestBody: null,
    responseSchema: 'CuotaDTO',
    successStatus: '200',
    codigos: '200, 401, 403, 404, 409',
    estado: 'real',
    tag: 'Cuotas'
  },
  {
    fn: 'getPagos',
    method: 'GET',
    url: '/api/pagos',
    desc: 'Historial de pagos registrados con comprobante y medio de pago.',
    roles: ['ADMINISTRATIVO', 'ADMINISTRADOR', 'ALUMNO'],
    pantallas: 'AdminPagosPage.jsx, SituacionAcademicaArancelariaPage.jsx',
    queryParams: [{ name: 'idAlumno', type: 'integer', desc: 'Filtro por ID de alumno' }],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<PagoDTO>',
    successStatus: '200',
    codigos: '200, 401, 403',
    estado: 'real',
    tag: 'Pagos'
  },
  {
    fn: 'registrarPago',
    method: 'POST',
    url: '/api/pagos',
    desc: 'Registra un pago cancelando cuota completa sin pagos parciales (RN09).',
    roles: ['ADMINISTRATIVO', 'ADMINISTRADOR'],
    pantallas: 'AdminPagosPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: 'PagoCreateDTO',
    responseSchema: 'PagoDTO',
    successStatus: '201',
    codigos: '201, 400, 401, 403, 409',
    estado: 'real',
    tag: 'Pagos'
  },
  {
    fn: 'getEvaluaciones',
    method: 'GET',
    url: '/api/evaluaciones',
    desc: 'Lista de instancias de evaluación (parciales/TPs) por curso.',
    roles: ['DOCENTE', 'ADMINISTRADOR'],
    pantallas: 'EvaluacionesDocentePage.jsx, CalificacionesDocentePage.jsx',
    queryParams: [
      { name: 'tipo', type: 'string', desc: 'Filtro por tipo de evaluación (PARCIAL, TRABAJO_PRACTICO)' },
      { name: 'idCurso', type: 'integer', desc: 'Filtro por ID de curso' }
    ],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<EvaluacionDTO>',
    successStatus: '200',
    codigos: '200, 401, 403',
    estado: 'real',
    tag: 'Evaluaciones'
  },
  {
    fn: 'createEvaluacion',
    method: 'POST',
    url: '/api/evaluaciones',
    desc: 'Programa un nuevo examen parcial o trabajo práctico integrador.',
    roles: ['DOCENTE'],
    pantallas: 'EvaluacionesDocentePage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: 'EvaluacionCreateDTO',
    responseSchema: 'EvaluacionDTO',
    successStatus: '201',
    codigos: '201, 400, 401, 403, 409',
    estado: 'real',
    tag: 'Evaluaciones'
  },
  {
    fn: 'guardarCalificaciones',
    method: 'POST',
    url: '/api/evaluaciones/{idEvaluacion}/calificaciones',
    desc: 'Guarda las notas de un acta de examen en lote de forma atómica.',
    roles: ['DOCENTE'],
    pantallas: 'CalificacionesDocentePage.jsx',
    queryParams: [],
    pathParams: [{ name: 'idEvaluacion', type: 'integer', desc: 'Identificador de la evaluación' }],
    requestBody: 'CalificacionesLoteDTO',
    responseSchema: 'SuccessResponseDTO',
    successStatus: '200',
    codigos: '200, 400, 401, 403, 404',
    estado: 'real',
    tag: 'Calificaciones'
  },
  {
    fn: 'getCalificaciones',
    method: 'GET',
    url: '/api/calificaciones',
    desc: 'Consulta de notas y calificaciones por alumno o curso.',
    roles: ['ALUMNO', 'DOCENTE', 'ADMINISTRADOR'],
    pantallas: 'CalificacionesDocentePage.jsx, CalificacionesAlumnoPage.jsx, SituacionAcademicaArancelariaPage.jsx',
    queryParams: [
      { name: 'idAlumno', type: 'integer', desc: 'Filtro por ID de alumno' },
      { name: 'idMateria', type: 'integer', desc: 'Filtro por ID de materia' },
      { name: 'tipo', type: 'string', desc: 'Filtro por tipo de evaluación' }
    ],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<CalificacionDTO>',
    successStatus: '200',
    codigos: '200, 401, 403',
    estado: 'real',
    tag: 'Calificaciones'
  },
  {
    fn: 'saveCalificacion',
    method: 'POST',
    url: '/api/calificaciones',
    desc: 'Asigna o actualiza la nota individual de un alumno (1 a 10).',
    roles: ['DOCENTE', 'ADMINISTRADOR'],
    pantallas: 'CalificacionesDocentePage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: 'CalificacionSaveDTO',
    responseSchema: 'CalificacionDTO',
    successStatus: '200',
    codigos: '200, 400, 401, 403',
    estado: 'real',
    tag: 'Calificaciones'
  },
  {
    fn: 'getAsistencias',
    method: 'GET',
    url: '/api/asistencias',
    desc: 'Consulta registro de asistencia por alumno, curso, materia y fecha.',
    roles: ['DOCENTE', 'ALUMNO', 'ADMINISTRADOR'],
    pantallas: 'AsistenciasDocentePage.jsx, AsistenciasAlumnoPage.jsx',
    queryParams: [
      { name: 'idAlumno', type: 'integer', desc: 'Filtro por ID de alumno' },
      { name: 'idCurso', type: 'integer', desc: 'Filtro por ID de curso' },
      { name: 'fecha', type: 'string', desc: 'Filtro por fecha (YYYY-MM-DD)' }
    ],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<AsistenciaDTO>',
    successStatus: '200',
    codigos: '200, 401, 403',
    estado: 'real',
    tag: 'Asistencias'
  },
  {
    fn: 'registrarAsistencias',
    method: 'POST',
    url: '/api/asistencias/lote',
    desc: 'Registra la toma de lista diaria de presentismo en lote.',
    roles: ['DOCENTE'],
    pantallas: 'AsistenciasDocentePage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: 'AsistenciasLoteDTO',
    responseSchema: 'SuccessResponseDTO',
    successStatus: '200',
    codigos: '200, 400, 401, 403',
    estado: 'real',
    tag: 'Asistencias'
  },
  {
    fn: 'getMesasExamen',
    method: 'GET',
    url: '/api/mesas-examen',
    desc: 'Calendario de mesas de exámenes finales programadas.',
    roles: ['ALUMNO', 'DOCENTE', 'ADMINISTRATIVO', 'ADMINISTRADOR'],
    pantallas: 'MesasExamenPage.jsx, InscripcionMesasPage.jsx, GestionMesasPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<MesaExamenDTO>',
    successStatus: '200',
    codigos: '200, 401, 403',
    estado: 'real',
    tag: 'Mesas'
  },
  {
    fn: 'inscribirseMesa',
    method: 'POST',
    url: '/api/mesas-examen/{id}/inscripciones',
    desc: 'Inscribe a un alumno a rendir examen final (RF49).',
    roles: ['ALUMNO'],
    pantallas: 'InscripcionMesasPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador de la mesa de examen' }],
    requestBody: null,
    responseSchema: 'MesaExamenDTO',
    successStatus: '200',
    codigos: '200, 401, 403, 404, 409',
    estado: 'real',
    tag: 'Mesas'
  },
  {
    fn: 'darseDeBajaMesa',
    method: 'DELETE',
    url: '/api/mesas-examen/{id}/inscripciones/{idInscripcion}',
    desc: 'Anula la inscripción a mesa de examen antes del límite reglamentario (RF50).',
    roles: ['ALUMNO'],
    pantallas: 'InscripcionMesasPage.jsx',
    queryParams: [],
    pathParams: [
      { name: 'id', type: 'integer', desc: 'Identificador de la mesa de examen' },
      { name: 'idInscripcion', type: 'integer', desc: 'Identificador de la inscripción del alumno' }
    ],
    requestBody: null,
    responseSchema: 'MesaExamenDTO',
    successStatus: '200',
    codigos: '200, 401, 403, 404',
    estado: 'real',
    tag: 'Mesas'
  },
  {
    fn: 'createMesa',
    method: 'POST',
    url: '/api/mesas-examen',
    desc: 'Habilita una nueva mesa de examen final en secretaría.',
    roles: ['ADMINISTRADOR'],
    pantallas: 'GestionMesasPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: 'MesaExamenCreateDTO',
    responseSchema: 'MesaExamenDTO',
    successStatus: '201',
    codigos: '201, 400, 401, 403, 409',
    estado: 'real',
    tag: 'Mesas'
  },
  {
    fn: 'updateMesa',
    method: 'PUT',
    url: '/api/mesas-examen/{id}',
    desc: 'Modifica tribunal, aula o cupo de una mesa de examen.',
    roles: ['ADMINISTRADOR'],
    pantallas: 'GestionMesasPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador de la mesa de examen' }],
    requestBody: 'MesaExamenUpdateDTO',
    responseSchema: 'MesaExamenDTO',
    successStatus: '200',
    codigos: '200, 400, 401, 403, 404',
    estado: 'real',
    tag: 'Mesas'
  },
  {
    fn: 'getSolicitudes',
    method: 'GET',
    url: '/api/solicitudes',
    desc: 'Consulta trámites y solicitudes filtradas por alumno o estado.',
    roles: ['ADMINISTRATIVO', 'ADMINISTRADOR', 'ALUMNO'],
    pantallas: 'BandejaSolicitudesPage.jsx, ConstanciasPage.jsx, InicioPage.jsx, PasantiasPage.jsx, ReincorporacionPage.jsx, SolicitudTituloPage.jsx, SolicitudesAcademicasPage.jsx, CambioCarreraPage.jsx',
    queryParams: [
      { name: 'idAlumno', type: 'integer', desc: 'Filtro por ID de alumno' },
      { name: 'tipo', type: 'string', desc: 'Filtro por tipo de solicitud (CONSTANCIA, PASANTIA, etc.)' },
      { name: 'estado', type: 'string', desc: 'Filtro por estado de solicitud (PENDIENTE, APROBADA, etc.)' }
    ],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<SolicitudDTO>',
    successStatus: '200',
    codigos: '200, 401, 403',
    estado: 'real',
    tag: 'Solicitudes'
  },
  {
    fn: 'createSolicitud',
    method: 'POST',
    url: '/api/solicitudes',
    desc: 'Presenta una nueva solicitud institucional (constancia, pasantía, título, reincorporación, cambio de carrera).',
    roles: ['ALUMNO'],
    pantallas: 'CambioCarreraPage.jsx, ConstanciasPage.jsx, PasantiasPage.jsx, ReincorporacionPage.jsx, SolicitudTituloPage.jsx, SolicitudesAcademicasPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: 'SolicitudCreateDTO',
    responseSchema: 'SolicitudDTO',
    successStatus: '201',
    codigos: '201, 400, 401, 403',
    estado: 'real',
    tag: 'Solicitudes'
  },
  {
    fn: 'resolverSolicitud',
    method: 'PATCH',
    url: '/api/solicitudes/{id}',
    desc: 'Dictamina o resuelve una solicitud estudiantil.',
    roles: ['ADMINISTRATIVO', 'ADMINISTRADOR'],
    pantallas: 'BandejaSolicitudesPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador de la solicitud' }],
    requestBody: 'SolicitudResolverDTO',
    responseSchema: 'SolicitudDTO',
    successStatus: '200',
    codigos: '200, 400, 401, 403, 404',
    estado: 'real',
    tag: 'Solicitudes'
  },
  {
    fn: 'getDeclaracionesJuradas',
    method: 'GET',
    url: '/api/declaraciones-juradas',
    desc: 'Consulta declaraciones juradas de materias a cursar en el ciclo (RF51).',
    roles: ['ALUMNO', 'ADMINISTRADOR', 'ADMINISTRATIVO'],
    pantallas: 'DeclaracionJuradaPage.jsx',
    queryParams: [
      { name: 'idAlumno', type: 'integer', desc: 'Filtro por ID de alumno' },
      { name: 'idCiclo', type: 'integer', desc: 'Filtro por ID de ciclo lectivo' }
    ],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<DeclaracionJuradaDTO>',
    successStatus: '200',
    codigos: '200, 401, 403',
    estado: 'real',
    tag: 'DeclaracionesJuradas'
  },
  {
    fn: 'createDeclaracionJurada',
    method: 'POST',
    url: '/api/declaraciones-juradas',
    desc: 'Presenta formalmente una declaración jurada de materias a cursar (RF51).',
    roles: ['ALUMNO'],
    pantallas: 'DeclaracionJuradaPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: 'DeclaracionJuradaCreateDTO',
    responseSchema: 'DeclaracionJuradaDTO',
    successStatus: '201',
    codigos: '201, 400, 401, 403',
    estado: 'real',
    tag: 'DeclaracionesJuradas'
  },
  {
    fn: 'getDocumentacion',
    method: 'GET',
    url: '/api/documentacion',
    desc: 'Verifica los requisitos documentales y legajo legal del alumno.',
    roles: ['ALUMNO', 'ADMINISTRATIVO', 'ADMINISTRADOR'],
    pantallas: 'DocumentacionPage.jsx',
    queryParams: [{ name: 'idAlumno', type: 'integer', desc: 'Filtro por ID de alumno' }],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<DocumentacionDTO>',
    successStatus: '200',
    codigos: '200, 401, 403',
    estado: 'real',
    tag: 'Documentacion'
  },
  {
    fn: 'updateDocumentacion',
    method: 'PUT',
    url: '/api/documentacion/{id}',
    desc: 'Valida o marca la entrega de un documento de legajo.',
    roles: ['ADMINISTRATIVO', 'ADMINISTRADOR'],
    pantallas: 'DocumentacionPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador del documento' }],
    requestBody: 'DocumentacionUpdateDTO',
    responseSchema: 'DocumentacionDTO',
    successStatus: '200',
    codigos: '200, 400, 401, 403, 404',
    estado: 'real',
    tag: 'Documentacion'
  },
  {
    fn: 'loginApi',
    method: 'POST',
    url: '/api/auth/login',
    desc: 'Autentica credenciales y genera sesión activa en el sistema.',
    roles: ['ALUMNO', 'DOCENTE', 'ADMINISTRATIVO', 'ADMINISTRADOR'],
    pantallas: 'Login.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: 'LoginRequestDTO',
    responseSchema: 'LoginResponseDTO',
    successStatus: '200',
    codigos: '200, 400, 401, 404',
    estado: 'real',
    tag: 'Auth'
  },
  {
    fn: 'logoutApi',
    method: 'POST',
    url: '/api/auth/logout',
    desc: 'Revoca la sesión activa del usuario.',
    roles: ['ALUMNO', 'DOCENTE', 'ADMINISTRATIVO', 'ADMINISTRADOR'],
    pantallas: 'Topbar.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: null,
    responseSchema: 'SuccessResponseDTO',
    successStatus: '200',
    codigos: '200, 401',
    estado: 'real',
    tag: 'Auth'
  },
  {
    fn: 'cambiarPasswordApi',
    method: 'POST',
    url: '/api/auth/cambiar-password',
    desc: 'Actualiza la clave personal del usuario autenticado.',
    roles: ['ALUMNO', 'DOCENTE', 'ADMINISTRATIVO', 'ADMINISTRADOR'],
    pantallas: 'CambiarPasswordPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: 'CambiarPasswordRequestDTO',
    responseSchema: 'SuccessResponseDTO',
    successStatus: '200',
    codigos: '200, 400, 401',
    estado: 'real',
    tag: 'Auth'
  },
  {
    fn: 'registroApi',
    method: 'POST',
    url: '/api/auth/registro',
    desc: 'Registra a un nuevo alumno en el sistema.',
    roles: ['ALUMNO', 'DOCENTE', 'ADMINISTRATIVO', 'ADMINISTRADOR'],
    pantallas: 'Registro.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: 'RegistroAlumnoDTO',
    responseSchema: 'SuccessResponseDTO',
    successStatus: '201',
    codigos: '201, 400, 409',
    estado: 'real',
    tag: 'Auth'
  },
  {
    fn: 'getOfertaCarrerasApi',
    method: 'GET',
    url: '/api/auth/carreras',
    desc: 'Obtiene las carreras disponibles para aspirantes y nuevos estudiantes (público).',
    roles: ['ALUMNO', 'DOCENTE', 'ADMINISTRATIVO', 'ADMINISTRADOR'],
    pantallas: 'Registro.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<CarreraDTO>',
    successStatus: '200',
    codigos: '200',
    estado: 'real',
    tag: 'Auth'
  },
  {
    fn: 'getOfertaPlanesApi',
    method: 'GET',
    url: '/api/auth/planes-estudio',
    desc: 'Obtiene los planes de estudio disponibles para aspirantes y nuevos estudiantes (público).',
    roles: ['ALUMNO', 'DOCENTE', 'ADMINISTRATIVO', 'ADMINISTRADOR'],
    pantallas: 'Registro.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<PlanEstudioDTO>',
    successStatus: '200',
    codigos: '200',
    estado: 'real',
    tag: 'Auth'
  },
  {
    fn: 'getUsuarios',
    method: 'GET',
    url: '/api/usuarios',
    desc: 'Catálogo de usuarios registrados y roles asignados.',
    roles: ['ADMINISTRADOR'],
    pantallas: 'GestionUsuariosPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: null,
    responseSchema: 'Array<UsuarioDTO>',
    successStatus: '200',
    codigos: '200, 401, 403',
    estado: 'real',
    tag: 'Usuarios'
  },
  {
    fn: 'createUsuario',
    method: 'POST',
    url: '/api/usuarios',
    desc: 'Registra un nuevo usuario con credenciales y asignación de rol.',
    roles: ['ADMINISTRADOR'],
    pantallas: 'GestionUsuariosPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: 'UsuarioCreateDTO',
    responseSchema: 'UsuarioDTO',
    successStatus: '201',
    codigos: '201, 400, 401, 403, 409',
    estado: 'real',
    tag: 'Usuarios'
  },
  {
    fn: 'updateUsuario',
    method: 'PUT',
    url: '/api/usuarios/{id}',
    desc: 'Modifica datos de perfil o rol de un usuario existente.',
    roles: ['ADMINISTRADOR'],
    pantallas: 'GestionUsuariosPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador del usuario' }],
    requestBody: 'UsuarioUpdateDTO',
    responseSchema: 'UsuarioDTO',
    successStatus: '200',
    codigos: '200, 400, 401, 403, 404',
    estado: 'real',
    tag: 'Usuarios'
  },
  {
    fn: 'deleteUsuario',
    method: 'DELETE',
    url: '/api/usuarios/{id}',
    desc: 'Da de baja la cuenta de un usuario en el sistema.',
    roles: ['ADMINISTRADOR'],
    pantallas: 'GestionUsuariosPage.jsx',
    queryParams: [],
    pathParams: [{ name: 'id', type: 'integer', desc: 'Identificador del usuario' }],
    requestBody: null,
    responseSchema: '204 No Content',
    successStatus: '204',
    codigos: '200, 204, 401, 403, 404',
    estado: 'real',
    tag: 'Usuarios'
  },
  {
    fn: 'getMiPerfil',
    method: 'GET',
    url: '/api/usuarios/me',
    desc: 'Obtiene los datos personales del usuario actualmente autenticado.',
    roles: ['ALUMNO', 'DOCENTE', 'ADMINISTRATIVO', 'ADMINISTRADOR'],
    pantallas: 'MisDatosPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: null,
    responseSchema: 'PerfilUsuarioDTO',
    successStatus: '200',
    codigos: '200, 401',
    estado: 'real',
    tag: 'Usuarios'
  },
  {
    fn: 'actualizarMiPerfil',
    method: 'PUT',
    url: '/api/usuarios/me',
    desc: 'Actualiza teléfono y domicilio del propio usuario en sesión.',
    roles: ['ALUMNO', 'DOCENTE', 'ADMINISTRATIVO', 'ADMINISTRADOR'],
    pantallas: 'MisDatosPage.jsx',
    queryParams: [],
    pathParams: [],
    requestBody: 'PerfilUsuarioDTO',
    responseSchema: 'PerfilUsuarioDTO',
    successStatus: '200',
    codigos: '200, 400, 401',
    estado: 'real',
    tag: 'Usuarios'
  }
];

// Fusionar datos de JSDoc en ENDPOINTS_META si existen diferencias
for (const ep of ENDPOINTS_META) {
  const jsdoc = jsdocMap.get(ep.fn);
  if (jsdoc) {
    if (jsdoc.desc && jsdoc.desc.length > 5) {
      ep.descripcion = jsdoc.desc;
    } else {
      ep.descripcion = ep.desc;
    }
  } else {
    ep.descripcion = ep.desc;
  }
}

// 3. DEFINICIÓN DE ENTIDADES CON PERSISTIDOS Y CALCULADOS
const ENTIDADES = [
  {
    nombre: 'Alumno',
    descripcion: 'Estudiante inscripto en el instituto educativo.',
    pantallas: ['GestionAlumnosPage.jsx', 'GestionInscripcionesPage.jsx', 'AdminSituacionAlumnoPage.jsx'],
    camposPersistidos: [
      { nombre: 'idAlumno', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador único del alumno (PK).' },
      { nombre: 'legajo', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Código identificatorio institucional (ej. ALU-1001).' },
      { nombre: 'nombre', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Nombres del estudiante.' },
      { nombre: 'apellido', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Apellidos del estudiante.' },
      { nombre: 'dni', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Documento Nacional de Identidad único.' },
      { nombre: 'email', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Correo electrónico de contacto.' },
      { nombre: 'telefono', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Teléfono de contacto.' },
      { nombre: 'domicilio', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Domicilio residencial del estudiante.' },
      { nombre: 'fechaNacimiento', tipo: 'string (YYYY-MM-DD)', obligatorio: true, enums: null, descripcion: 'Fecha de nacimiento.' },
      { nombre: 'idCarrera', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK a la Carrera en la que está inscripto.' },
      { nombre: 'idPlan', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK al Plan de Estudio asignado.' },
      { nombre: 'idCurso', tipo: 'number (Integer)', obligatorio: false, enums: null, descripcion: 'FK al curso/comisión principal asignada.' },
      { nombre: 'cicloLectivo', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Año lectivo de ingreso o cursada activa.' },
      { nombre: 'estado', tipo: 'string (Enum)', obligatorio: true, enums: ['ACTIVO', 'INACTIVO'], descripcion: 'Condición institucional activa o inactiva.' },
      { nombre: 'estadoMatricula', tipo: 'string (Enum)', obligatorio: true, enums: ['ACTIVA', 'PENDIENTE', 'CANCELADA'], descripcion: 'Estado de la matrícula para el ciclo lectivo actual.' },
      { nombre: 'idMatricula', tipo: 'number (Integer)', obligatorio: false, enums: null, descripcion: 'FK a la matrícula del ciclo actual.' }
    ],
    camposCalculados: [
      { nombre: 'carrera', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre desnormalizado de la carrera.' },
      { nombre: 'planNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre desnormalizado del plan curricular.' },
      { nombre: 'cursoNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre del curso asignado.' }
    ],
    relaciones: [
      { campo: 'idCarrera', entidadDestino: 'Carrera', campoDestino: 'idCarrera', tipo: 'M:1' },
      { campo: 'idPlan', entidadDestino: 'PlanEstudio', campoDestino: 'idPlan', tipo: 'M:1' },
      { campo: 'idCurso', entidadDestino: 'Curso', campoDestino: 'idCurso', tipo: 'M:1' },
      { campo: 'idMatricula', entidadDestino: 'Matricula', campoDestino: 'idMatricula', tipo: '1:1' }
    ]
  },
  {
    nombre: 'Docente',
    descripcion: 'Profesor o docente a cargo de cátedras y cursos.',
    pantallas: ['GestionDocentesPage.jsx', 'GestionAsignacionesPage.jsx'],
    camposPersistidos: [
      { nombre: 'idDocente', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador único del docente (PK).' },
      { nombre: 'legajo', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Código identificatorio del docente (ej. DOC-201).' },
      { nombre: 'nombre', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Nombres del docente.' },
      { nombre: 'apellido', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Apellidos del docente.' },
      { nombre: 'dni', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Documento Nacional de Identidad.' },
      { nombre: 'email', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Correo institucional o personal.' },
      { nombre: 'telefono', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Teléfono de contacto.' },
      { nombre: 'titulo', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Título habilitante o especialidad académica.' },
      { nombre: 'estado', tipo: 'string (Enum)', obligatorio: true, enums: ['ACTIVO', 'INACTIVO'], descripcion: 'Estado laboral/académico del docente.' }
    ],
    camposCalculados: [],
    relaciones: [
      { campo: 'idDocente', entidadDestino: 'Asignacion', campoDestino: 'idDocente', tipo: '1:N' }
    ]
  },
  {
    nombre: 'Carrera',
    descripcion: 'Titulación académica dictada por el instituto.',
    pantallas: ['GestionCarrerasPage.jsx', 'GestionAlumnosPage.jsx'],
    camposPersistidos: [
      { nombre: 'idCarrera', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador único de la carrera (PK).' },
      { nombre: 'codigo', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Código identificador (ej. TSD, IA, REDES).' },
      { nombre: 'nombre', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Nombre oficial de la carrera.' },
      { nombre: 'tituloOtorgado', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Título con validez nacional que expide.' },
      { nombre: 'duracionAnios', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Duración teórica de la carrera en años.' },
      { nombre: 'resolucion', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Resolución ministerial habilitante.' },
      { nombre: 'estado', tipo: 'string (Enum)', obligatorio: true, enums: ['ACTIVA', 'INACTIVA'], descripcion: 'Estado de oferta académica de la carrera.' }
    ],
    camposCalculados: [],
    relaciones: [
      { campo: 'idCarrera', entidadDestino: 'PlanEstudio', campoDestino: 'idCarrera', tipo: '1:N' },
      { campo: 'idCarrera', entidadDestino: 'Curso', campoDestino: 'idCarrera', tipo: '1:N' }
    ]
  },
  {
    nombre: 'PlanEstudio',
    descripcion: 'Estructura curricular y materias que componen una carrera.',
    pantallas: ['GestionPlanEstudiosPage.jsx', 'PlanEstudiosAlumnoPage.jsx'],
    camposPersistidos: [
      { nombre: 'idPlan', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador único del plan (PK).' },
      { nombre: 'idCarrera', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK a la Carrera.' },
      { nombre: 'codigo', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Código del plan (ej. PLAN-2023).' },
      { nombre: 'nombre', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Nombre descriptivo del plan.' },
      { nombre: 'anioAprobacion', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Año de entrada en vigencia.' },
      { nombre: 'cantidadMaterias', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Cantidad total de materias que lo integran.' },
      { nombre: 'estado', tipo: 'string (Enum)', obligatorio: true, enums: ['VIGENTE', 'CERRADO'], descripcion: 'Estado de vigencia del plan de estudios.' }
    ],
    camposCalculados: [
      { nombre: 'carreraNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre desnormalizado de la carrera.' }
    ],
    relaciones: [
      { campo: 'idCarrera', entidadDestino: 'Carrera', campoDestino: 'idCarrera', tipo: 'M:1' },
      { campo: 'idPlan', entidadDestino: 'Materia', campoDestino: 'idPlan', tipo: '1:N' }
    ]
  },
  {
    nombre: 'Materia',
    descripcion: 'Asignatura curricular dictada dentro de un plan de estudio.',
    pantallas: ['GestionMateriasPage.jsx', 'PlanEstudiosAlumnoPage.jsx'],
    camposPersistidos: [
      { nombre: 'idMateria', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador único de la materia (PK).' },
      { nombre: 'codigo', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Código curricular (ej. PROG-1).' },
      { nombre: 'nombre', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Nombre de la materia.' },
      { nombre: 'idCarrera', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK a la Carrera.' },
      { nombre: 'idPlan', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK al Plan de Estudio.' },
      { nombre: 'anio', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Año de cursada (1°, 2°, 3° año).' },
      { nombre: 'regimen', tipo: 'string (Enum)', obligatorio: true, enums: ['ANUAL', 'CUATRIMESTRAL'], descripcion: 'Régimen de dictado.' },
      { nombre: 'horasSemanales', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Carga horaria semanal.' },
      { nombre: 'correlativas', tipo: 'array of numbers', obligatorio: false, enums: null, descripcion: 'IDs de materias correlativas previas requeridas.' }
    ],
    camposCalculados: [
      { nombre: 'carreraNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre de la carrera asociada.' },
      { nombre: 'planNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre del plan de estudio asociado.' }
    ],
    relaciones: [
      { campo: 'idCarrera', entidadDestino: 'Carrera', campoDestino: 'idCarrera', tipo: 'M:1' },
      { campo: 'idPlan', entidadDestino: 'PlanEstudio', campoDestino: 'idPlan', tipo: 'M:1' }
    ]
  },
  {
    nombre: 'Curso',
    descripcion: 'Comisión y división física/horaria donde se cursan materias.',
    pantallas: ['GestionCursosPage.jsx', 'GestionInscripcionesPage.jsx'],
    camposPersistidos: [
      { nombre: 'idCurso', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador único del curso (PK).' },
      { nombre: 'codigo', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Código de la comisión (ej. 1A-TM-2026).' },
      { nombre: 'nombre', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Nombre descriptivo de la comisión.' },
      { nombre: 'idCarrera', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK a la Carrera.' },
      { nombre: 'idPlan', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK al Plan de Estudio.' },
      { nombre: 'turno', tipo: 'string (Enum)', obligatorio: true, enums: ['MANANA', 'TARDE', 'NOCHE'], descripcion: 'Turno de cursada (sin tildes).' },
      { nombre: 'capacidadMaxima', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Límite de capacidad física o aula.' },
      { nombre: 'cicloLectivo', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Año lectivo correspondiente.' },
      { nombre: 'aula', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Aula física asignada.' }
    ],
    camposCalculados: [
      { nombre: 'inscriptos', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Contador de alumnos matriculados confirmados.' },
      { nombre: 'cuposDisponibles', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Cupos disponibles (capacidadMaxima - inscriptos).' },
      { nombre: 'carreraNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre desnormalizado de la carrera.' },
      { nombre: 'planNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre desnormalizado del plan.' }
    ],
    relaciones: [
      { campo: 'idCarrera', entidadDestino: 'Carrera', campoDestino: 'idCarrera', tipo: 'M:1' },
      { campo: 'idPlan', entidadDestino: 'PlanEstudio', campoDestino: 'idPlan', tipo: 'M:1' }
    ]
  },
  {
    nombre: 'CicloLectivo',
    descripcion: 'Período académico anual de actividades del instituto.',
    pantallas: ['GestionCiclosLectivosPage.jsx'],
    camposPersistidos: [
      { nombre: 'idCiclo', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador único del ciclo (PK).' },
      { nombre: 'anio', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Año calendario (ej. 2026).' },
      { nombre: 'nombre', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Denominación (ej. Ciclo Lectivo 2026).' },
      { nombre: 'fechaInicio', tipo: 'string (YYYY-MM-DD)', obligatorio: true, enums: null, descripcion: 'Fecha de inicio del ciclo.' },
      { nombre: 'fechaFin', tipo: 'string (YYYY-MM-DD)', obligatorio: true, enums: null, descripcion: 'Fecha de cierre del ciclo.' },
      { nombre: 'estado', tipo: 'string (Enum)', obligatorio: true, enums: ['ACTIVO', 'CERRADO', 'PLANIFICADO'], descripcion: 'Estado operativo del ciclo lectivo.' }
    ],
    camposCalculados: [],
    relaciones: [
      { campo: 'idCiclo', entidadDestino: 'Inscripcion', campoDestino: 'cicloLectivo', tipo: '1:N' }
    ]
  },
  {
    nombre: 'Asignacion',
    descripcion: 'Asignación docente a un curso y materia específicos.',
    pantallas: ['GestionAsignacionesPage.jsx'],
    camposPersistidos: [
      { nombre: 'idAsignacion', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador de la asignación (PK).' },
      { nombre: 'idDocente', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK al Docente asignado.' },
      { nombre: 'idMateria', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK a la Materia dictada.' },
      { nombre: 'idCurso', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK al Curso / Comisión.' },
      { nombre: 'rolDocente', tipo: 'string (Enum)', obligatorio: true, enums: ['Titular', 'Adjunto', 'JTP', 'Ayudante'], descripcion: 'Cargo docente en la cátedra.' }
    ],
    camposCalculados: [
      { nombre: 'docenteNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre completo del docente.' },
      { nombre: 'materiaNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre de la materia.' },
      { nombre: 'cursoNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre del curso.' }
    ],
    relaciones: [
      { campo: 'idDocente', entidadDestino: 'Docente', campoDestino: 'idDocente', tipo: 'M:1' },
      { campo: 'idMateria', entidadDestino: 'Materia', campoDestino: 'idMateria', tipo: 'M:1' },
      { campo: 'idCurso', entidadDestino: 'Curso', campoDestino: 'idCurso', tipo: 'M:1' }
    ]
  },
  {
    nombre: 'Inscripcion',
    descripcion: 'Inscripción de un estudiante a una comisión/curso anual.',
    pantallas: ['GestionInscripcionesPage.jsx'],
    camposPersistidos: [
      { nombre: 'idInscripcion', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador único de inscripción (PK).' },
      { nombre: 'idAlumno', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK al Alumno inscripto.' },
      { nombre: 'idCurso', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK al Curso/Comisión.' },
      { nombre: 'cicloLectivo', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Año lectivo cursado.' },
      { nombre: 'fechaInscripcion', tipo: 'string (YYYY-MM-DD)', obligatorio: true, enums: null, descripcion: 'Fecha de registro de la inscripción.' },
      { nombre: 'estado', tipo: 'string (Enum)', obligatorio: true, enums: ['CONFIRMADA', 'CANCELADA'], descripcion: 'Estado de la inscripción.' }
    ],
    camposCalculados: [
      { nombre: 'alumnoNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre completo del alumno.' },
      { nombre: 'cursoNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre del curso inscripto.' }
    ],
    relaciones: [
      { campo: 'idAlumno', entidadDestino: 'Alumno', campoDestino: 'idAlumno', tipo: 'M:1' },
      { campo: 'idCurso', entidadDestino: 'Curso', campoDestino: 'idCurso', tipo: 'M:1' }
    ]
  },
  {
    nombre: 'Matricula',
    descripcion: 'Matrícula anual institucional habilitante de cursada.',
    pantallas: ['AdminMatriculasPage.jsx', 'CancelarMatriculaPage.jsx'],
    camposPersistidos: [
      { nombre: 'idMatricula', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador de la matrícula (PK).' },
      { nombre: 'idAlumno', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK al Alumno matriculado.' },
      { nombre: 'idCarrera', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK a la Carrera matriculada.' },
      { nombre: 'idCiclo', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK al Ciclo Lectivo correspondiente.' },
      { nombre: 'cicloLectivo', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Año lectivo de la matrícula.' },
      { nombre: 'fecha', tipo: 'string (YYYY-MM-DD)', obligatorio: true, enums: null, descripcion: 'Fecha de emisión de la matrícula.' },
      { nombre: 'estado', tipo: 'string (Enum)', obligatorio: true, enums: ['ACTIVA', 'PENDIENTE', 'CANCELADA'], descripcion: 'Estado académico de la matrícula.' }
    ],
    camposCalculados: [
      { nombre: 'alumnoNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre completo del alumno.' },
      { nombre: 'carreraNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre de la carrera matriculada.' },
      { nombre: 'cicloLectivoNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre del ciclo lectivo.' },
      { nombre: 'estadoPago', tipo: 'string (Enum)', obligatorio: false, enums: ['AL_DIA', 'EN_MORA', 'PENDIENTE'], descripcion: 'Estado financiero calculado a partir de las cuotas asociadas (EN_MORA si hay cuota vencida, AL_DIA si están pagadas, PENDIENTE en el resto). Solo lectura.' }
    ],
    relaciones: [
      { campo: 'idAlumno', entidadDestino: 'Alumno', campoDestino: 'idAlumno', tipo: 'M:1' },
      { campo: 'idCarrera', entidadDestino: 'Carrera', campoDestino: 'idCarrera', tipo: 'M:1' },
      { campo: 'idCiclo', entidadDestino: 'CicloLectivo', campoDestino: 'idCiclo', tipo: 'M:1' }
    ]
  },
  {
    nombre: 'Cuota',
    descripcion: 'Obligación arancelaria mensual o seguro cuatrimestral emitida para un alumno.',
    pantallas: ['AdminCuotasPage.jsx', 'AdminPagosPage.jsx'],
    camposPersistidos: [
      { nombre: 'idCuota', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador único de la cuota (PK).' },
      { nombre: 'idMatricula', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK a la Matrícula del ciclo.' },
      { nombre: 'concepto', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Descripción arancelaria (ej. Cuota 1 - Enero 2026).' },
      { nombre: 'mes', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Número de mes correspondiente (1 a 12).' },
      { nombre: 'anio', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Año lectivo de facturación.' },
      { nombre: 'fechaVencimiento', tipo: 'string (YYYY-MM-DD)', obligatorio: true, enums: null, descripcion: 'Fecha límite de pago.' },
      { nombre: 'importe', tipo: 'number (Float)', obligatorio: true, enums: null, descripcion: 'Monto a abonar en pesos argentinos.' },
      { nombre: 'tipo', tipo: 'string (Enum)', obligatorio: true, enums: ['MENSUAL', 'SEGURO_CUATRIMESTRAL'], descripcion: 'Concepto arancelario.' },
      { nombre: 'estado', tipo: 'string (Enum)', obligatorio: true, enums: ['PENDIENTE', 'PAGADA', 'VENCIDA', 'ANULADA'], descripcion: 'Estado arancelario de la cuota.' },
      { nombre: 'fechaPago', tipo: 'string (YYYY-MM-DD)', obligatorio: false, enums: null, descripcion: 'Fecha de cancelación efectiva.' }
    ],
    camposCalculados: [
      { nombre: 'idAlumno', tipo: 'number (Integer)', obligatorio: false, enums: null, descripcion: 'Campo calculado derivado de la matrícula (idMatricula -> Matricula.idAlumno).' },
      { nombre: 'alumnoNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre completo del alumno derivado de matrícula.' }
    ],
    relaciones: [
      { campo: 'idMatricula', entidadDestino: 'Matricula', campoDestino: 'idMatricula', tipo: 'M:1' }
    ]
  },
  {
    nombre: 'Pago',
    descripcion: 'Comprobante de recaudación arancelaria emitido por el instituto.',
    pantallas: ['AdminPagosPage.jsx', 'SituacionAcademicaArancelariaPage.jsx'],
    camposPersistidos: [
      { nombre: 'idPago', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador único del pago (PK).' },
      { nombre: 'idCuota', tipo: 'number (Integer)', obligatorio: false, enums: null, descripcion: 'FK a la Cuota cancelada (null en matrícula u otros).' },
      { nombre: 'idAlumno', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK al Alumno que realiza el pago.' },
      { nombre: 'concepto', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Concepto cancelado.' },
      { nombre: 'importe', tipo: 'number (Float)', obligatorio: true, enums: null, descripcion: 'Monto total cancelado (exacto según RN09).' },
      { nombre: 'fechaPago', tipo: 'string (YYYY-MM-DD)', obligatorio: true, enums: null, descripcion: 'Fecha de recepción del pago.' },
      { nombre: 'medioPago', tipo: 'string (Enum)', obligatorio: true, enums: ['EFECTIVO', 'TRANSFERENCIA', 'TARJETA_CREDITO', 'TARJETA_DEBITO', 'OTRO'], descripcion: 'Canal de cobranza utilizado.' },
      { nombre: 'comprobante', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Número de comprobante oficial AAAA-NNNNNN generado por el backend al registrar el pago (solo lectura).' },
      { nombre: 'estado', tipo: 'string (Enum)', obligatorio: true, enums: ['ACREDITADO', 'PENDIENTE', 'RECHAZADO'], descripcion: 'Estado contable de acreditación.' }
    ],
    camposCalculados: [
      { nombre: 'alumnoNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre completo del alumno pagador.' },
      { nombre: 'cuotaConcepto', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Detalle de la cuota cancelada.' }
    ],
    relaciones: [
      { campo: 'idCuota', entidadDestino: 'Cuota', campoDestino: 'idCuota', tipo: 'M:1' },
      { campo: 'idAlumno', entidadDestino: 'Alumno', campoDestino: 'idAlumno', tipo: 'M:1' }
    ]
  },
  {
    nombre: 'Evaluacion',
    descripcion: 'Instancia evaluativa planificada por el docente titular de cátedra.',
    pantallas: ['EvaluacionesDocentePage.jsx', 'CalificacionesDocentePage.jsx'],
    camposPersistidos: [
      { nombre: 'idEvaluacion', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador único de la evaluación (PK).' },
      { nombre: 'idMateria', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK a la Materia evaluada.' },
      { nombre: 'idCurso', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK al Curso / Comisión evaluada.' },
      { nombre: 'fecha', tipo: 'string (YYYY-MM-DD)', obligatorio: true, enums: null, descripcion: 'Fecha planificada de la evaluación.' },
      { nombre: 'tipo', tipo: 'string (Enum)', obligatorio: true, enums: ['PARCIAL', 'TRABAJO_PRACTICO'], descripcion: 'Tipo de examen.' },
      { nombre: 'descripcion', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Temario y alcance del examen.' },
      { nombre: 'ponderacion', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Porcentaje en la nota final (ej. 30%).' }
    ],
    camposCalculados: [
      { nombre: 'materiaNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre de la materia evaluada.' },
      { nombre: 'cursoNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre de la comisión evaluada.' }
    ],
    relaciones: [
      { campo: 'idMateria', entidadDestino: 'Materia', campoDestino: 'idMateria', tipo: 'M:1' },
      { campo: 'idCurso', entidadDestino: 'Curso', campoDestino: 'idCurso', tipo: 'M:1' }
    ]
  },
  {
    nombre: 'Calificacion',
    descripcion: 'Nota numérica obtenida por un alumno en una instancia evaluativa.',
    pantallas: ['CalificacionesDocentePage.jsx', 'CalificacionesAlumnoPage.jsx'],
    camposPersistidos: [
      { nombre: 'idCalificacion', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador de la calificación (PK).' },
      { nombre: 'idAlumno', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK al Alumno calificado.' },
      { nombre: 'idMateria', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK a la Materia evaluada.' },
      { nombre: 'evaluacionNombre', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Nombre de la evaluación asociada.' },
      { nombre: 'tipo', tipo: 'string (Enum)', obligatorio: true, enums: ['PARCIAL', 'TRABAJO_PRACTICO'], descripcion: 'Tipo de evaluación evaluada.' },
      { nombre: 'nota', tipo: 'number (Integer, 1-10)', obligatorio: true, enums: null, descripcion: 'Calificación numérica obtenida (1 a 10).' },
      { nombre: 'fecha', tipo: 'string (YYYY-MM-DD)', obligatorio: true, enums: null, descripcion: 'Fecha de registro de la nota.' },
      { nombre: 'observacion', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Devolución pedagógica del docente.' }
    ],
    camposCalculados: [
      { nombre: 'condicion', tipo: 'string (Enum)', obligatorio: true, enums: ['APROBADO', 'DESAPROBADO'], descripcion: 'Condición calculada a partir de nota (nota >= 4 ? APROBADO : DESAPROBADO).' },
      { nombre: 'alumnoNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre completo del alumno.' },
      { nombre: 'materiaNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre de la materia evaluada.' }
    ],
    relaciones: [
      { campo: 'idAlumno', entidadDestino: 'Alumno', campoDestino: 'idAlumno', tipo: 'M:1' },
      { campo: 'idMateria', entidadDestino: 'Materia', campoDestino: 'idMateria', tipo: 'M:1' }
    ]
  },
  {
    nombre: 'Asistencia',
    descripcion: 'Presentismo diario de clase tomado por cátedra.',
    pantallas: ['AsistenciasDocentePage.jsx', 'AsistenciasAlumnoPage.jsx'],
    camposPersistidos: [
      { nombre: 'idAsistencia', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador del registro de asistencia (PK).' },
      { nombre: 'idAlumno', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK al Alumno presente o ausente.' },
      { nombre: 'idMateria', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK a la Materia cursada.' },
      { nombre: 'idCurso', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK al Curso / Comisión dictada.' },
      { nombre: 'fecha', tipo: 'string (YYYY-MM-DD)', obligatorio: true, enums: null, descripcion: 'Fecha de la clase dictada.' },
      { nombre: 'estado', tipo: 'string (Enum)', obligatorio: true, enums: ['PRESENTE', 'AUSENTE', 'TARDANZA', 'JUSTIFICADO'], descripcion: 'Estado de asistencia registrado.' }
    ],
    camposCalculados: [
      { nombre: 'alumnoNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre completo del alumno.' },
      { nombre: 'materiaNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre de la materia dictada.' }
    ],
    relaciones: [
      { campo: 'idAlumno', entidadDestino: 'Alumno', campoDestino: 'idAlumno', tipo: 'M:1' },
      { campo: 'idMateria', entidadDestino: 'Materia', campoDestino: 'idMateria', tipo: 'M:1' },
      { campo: 'idCurso', entidadDestino: 'Curso', campoDestino: 'idCurso', tipo: 'M:1' }
    ]
  },
  {
    nombre: 'MesaExamen',
    descripcion: 'Mesa examinadora de examen final institucional.',
    pantallas: ['GestionMesasPage.jsx', 'MesasExamenPage.jsx', 'InscripcionMesasPage.jsx'],
    camposPersistidos: [
      { nombre: 'idMesa', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador de la mesa de examen (PK).' },
      { nombre: 'idMateria', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK a la Materia a rendir.' },
      { nombre: 'idCarrera', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK a la Carrera.' },
      { nombre: 'fecha', tipo: 'string (YYYY-MM-DD)', obligatorio: true, enums: null, descripcion: 'Fecha de la mesa examinadora.' },
      { nombre: 'hora', tipo: 'string (HH:mm)', obligatorio: true, enums: null, descripcion: 'Hora fijada de inicio.' },
      { nombre: 'turno', tipo: 'string (Enum)', obligatorio: true, enums: ['MANANA', 'TARDE', 'NOCHE'], descripcion: 'Turno de la mesa (sin tildes).' },
      { nombre: 'tribunal', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Nombres de los docentes del tribunal evaluador.' },
      { nombre: 'aula', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Aula física asignada para el examen.' },
      { nombre: 'cupoMaximo', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Capacidad máxima de inscriptos.' },
      { nombre: 'alumnosInscriptosIds', tipo: 'array of numbers', obligatorio: true, enums: null, descripcion: 'Lista de IDs de alumnos inscriptos.' },
      { nombre: 'estado', tipo: 'string (Enum)', obligatorio: true, enums: ['ABIERTA', 'CERRADA', 'FINALIZADA'], descripcion: 'Estado de la mesa.' }
    ],
    camposCalculados: [
      { nombre: 'inscriptos', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Contador de alumnos inscriptos.' },
      { nombre: 'cuposDisponibles', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Cupos disponibles (cupoMaximo - inscriptos).' },
      { nombre: 'materiaNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre de la materia evaluada.' },
      { nombre: 'carreraNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre de la carrera asociada.' }
    ],
    relaciones: [
      { campo: 'idMateria', entidadDestino: 'Materia', campoDestino: 'idMateria', tipo: 'M:1' },
      { campo: 'idCarrera', entidadDestino: 'Carrera', campoDestino: 'idCarrera', tipo: 'M:1' }
    ]
  },
  {
    nombre: 'InscripcionMesa',
    descripcion: 'Inscripción individual de un alumno para rendir examen final.',
    pantallas: ['InscripcionMesasPage.jsx'],
    camposPersistidos: [
      { nombre: 'idInscripcionMesa', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador único de inscripción a mesa (PK).' },
      { nombre: 'idMesa', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK a la Mesa de Examen.' },
      { nombre: 'idAlumno', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK al Alumno inscripto.' },
      { nombre: 'fechaInscripcion', tipo: 'string (YYYY-MM-DD)', obligatorio: true, enums: null, descripcion: 'Fecha de registro de la inscripción.' },
      { nombre: 'estado', tipo: 'string (Enum)', obligatorio: true, enums: ['CONFIRMADA', 'CANCELADA'], descripcion: 'Estado de la inscripción a mesa.' }
    ],
    camposCalculados: [
      { nombre: 'alumnoNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre completo del alumno.' },
      { nombre: 'materiaNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre de la materia a rendir.' }
    ],
    relaciones: [
      { campo: 'idMesa', entidadDestino: 'MesaExamen', campoDestino: 'idMesa', tipo: 'M:1' },
      { campo: 'idAlumno', entidadDestino: 'Alumno', campoDestino: 'idAlumno', tipo: 'M:1' }
    ]
  },
  {
    nombre: 'Solicitud',
    descripcion: 'Trámite administrativo o académico solicitado por el alumno.',
    pantallas: ['BandejaSolicitudesPage.jsx', 'ConstanciasPage.jsx', 'PasantiasPage.jsx', 'SolicitudTituloPage.jsx', 'SolicitudesAcademicasPage.jsx', 'CambioCarreraPage.jsx', 'ReincorporacionPage.jsx'],
    camposPersistidos: [
      { nombre: 'idSolicitud', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador único de la solicitud (PK).' },
      { nombre: 'idAlumno', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK al Alumno solicitante.' },
      { nombre: 'tipo', tipo: 'string (Enum)', obligatorio: true, enums: ['CONSTANCIA', 'PASANTIA', 'TITULO_INTERMEDIO', 'ACADEMICA', 'CAMBIO_CARRERA', 'REINCORPORACION'], descripcion: 'Tipo de trámite institucional (sin tildes ni espacios).' },
      { nombre: 'fechaSolicitud', tipo: 'string (YYYY-MM-DD)', obligatorio: true, enums: null, descripcion: 'Fecha de presentación del trámite.' },
      { nombre: 'motivo', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Fundamentación o motivo del pedido.' },
      { nombre: 'observaciones', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Información complementaria del alumno.' },
      { nombre: 'estado', tipo: 'string (Enum)', obligatorio: true, enums: ['PENDIENTE', 'EN_PROCESO', 'APROBADA', 'RECHAZADA'], descripcion: 'Estado administrativo de resolución.' },
      { nombre: 'fechaResolucion', tipo: 'string (YYYY-MM-DD)', obligatorio: false, enums: null, descripcion: 'Fecha en la que fue resuelta.' },
      { nombre: 'respuesta', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Dictamen de secretaría académica.' }
    ],
    camposCalculados: [
      { nombre: 'alumnoNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre completo del alumno solicitante.' }
    ],
    relaciones: [
      { campo: 'idAlumno', entidadDestino: 'Alumno', campoDestino: 'idAlumno', tipo: 'M:1' }
    ]
  },
  {
    nombre: 'DeclaracionJurada',
    descripcion: 'Declaración jurada de asignaturas que el estudiante cursará en el ciclo lectivo bajo compromiso de correlatividad (RF51).',
    pantallas: ['DeclaracionJuradaPage.jsx'],
    camposPersistidos: [
      { nombre: 'idDeclaracion', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador único de la declaración jurada (PK).' },
      { nombre: 'idAlumno', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK al Alumno firmante.' },
      { nombre: 'idCiclo', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK al Ciclo Lectivo correspondiente.' },
      { nombre: 'fecha', tipo: 'string (YYYY-MM-DD)', obligatorio: true, enums: null, descripcion: 'Fecha de presentación de la declaración jurada.' },
      { nombre: 'materiasDeclaradasIds', tipo: 'array of numbers', obligatorio: true, enums: null, descripcion: 'Lista de IDs de materias que se compromete a cursar.' },
      { nombre: 'estado', tipo: 'string (Enum)', obligatorio: true, enums: ['VALIDADA', 'PENDIENTE', 'RECHAZADA'], descripcion: 'Estado de validación reglamentaria de la DJ.' },
      { nombre: 'observaciones', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Observaciones o fundamentación de correlatividades.' }
    ],
    camposCalculados: [
      { nombre: 'alumnoNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre completo del alumno.' },
      { nombre: 'cicloNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre del ciclo lectivo declarado.' },
      { nombre: 'materiasNombres', tipo: 'array of strings', obligatorio: false, enums: null, descripcion: 'Nombres de las materias declaradas derivadas de IDs.' }
    ],
    relaciones: [
      { campo: 'idAlumno', entidadDestino: 'Alumno', campoDestino: 'idAlumno', tipo: 'M:1' },
      { campo: 'idCiclo', entidadDestino: 'CicloLectivo', campoDestino: 'idCiclo', tipo: 'M:1' }
    ]
  },
  {
    nombre: 'Documentacion',
    descripcion: 'Requisito documental obligatorio o probatorio para el legajo estudiantil.',
    pantallas: ['DocumentacionPage.jsx'],
    camposPersistidos: [
      { nombre: 'idDocumento', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador único del requisito documental (PK).' },
      { nombre: 'idAlumno', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'FK al Alumno.' },
      { nombre: 'tipo', tipo: 'string (Enum)', obligatorio: true, enums: ['DNI', 'TITULO_SECUNDARIO', 'APTO_MEDICO', 'FOTOS'], descripcion: 'Tipo de documento exigido (sin tildes ni espacios).' },
      { nombre: 'obligatorio', tipo: 'boolean', obligatorio: true, enums: null, descripcion: 'Indica si es indispensable para cursar.' },
      { nombre: 'estado', tipo: 'string (Enum)', obligatorio: true, enums: ['ENTREGADO', 'PENDIENTE', 'RECHAZADO'], descripcion: 'Estado de verificación del requisito.' },
      { nombre: 'fechaEntrega', tipo: 'string (YYYY-MM-DD)', obligatorio: false, enums: null, descripcion: 'Fecha de recepción física o digital.' },
      { nombre: 'observaciones', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Notas de secretaría (legalizaciones, vencimientos).' }
    ],
    camposCalculados: [
      { nombre: 'alumnoNombre', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Nombre completo del alumno.' }
    ],
    relaciones: [
      { campo: 'idAlumno', entidadDestino: 'Alumno', campoDestino: 'idAlumno', tipo: 'M:1' }
    ]
  },
  {
    nombre: 'Usuario',
    descripcion: 'Cuenta de usuario del sistema con credenciales y perfil.',
    pantallas: ['GestionUsuariosPage.jsx', 'MisDatosPage.jsx', 'Login.jsx'],
    camposPersistidos: [
      { nombre: 'idUsuario', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador único del usuario (PK).' },
      { nombre: 'username', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Nombre de usuario único para acceso.' },
      { nombre: 'nombre', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Nombre y apellido visible.' },
      { nombre: 'email', tipo: 'string', obligatorio: true, enums: null, descripcion: 'Correo institucional o personal.' },
      { nombre: 'telefono', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Teléfono de contacto.' },
      { nombre: 'domicilio', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Domicilio de residencia.' },
      { nombre: 'rol', tipo: 'string (Enum)', obligatorio: true, enums: ['ADMINISTRADOR', 'ADMINISTRATIVO', 'DOCENTE', 'ALUMNO'], descripcion: 'Rol operativo y nivel de acceso en el sistema.' },
      { nombre: 'estado', tipo: 'string (Enum)', obligatorio: true, enums: ['ACTIVO', 'INACTIVO'], descripcion: 'Estado de habilitación de la cuenta.' },
      { nombre: 'ultimoAcceso', tipo: 'string (YYYY-MM-DD HH:mm)', obligatorio: false, enums: null, descripcion: 'Última fecha y hora de sesión registrada.' }
    ],
    camposCalculados: [],
    relaciones: [
      { campo: 'rol', entidadDestino: 'Rol', campoDestino: 'nombre', tipo: 'M:1' }
    ]
  },
  {
    nombre: 'Rol',
    descripcion: 'Rol de seguridad y autorización del sistema.',
    pantallas: ['GestionUsuariosPage.jsx', 'Login.jsx', 'MisDatosPage.jsx'],
    camposPersistidos: [
      { nombre: 'idRol', tipo: 'number (Integer)', obligatorio: true, enums: null, descripcion: 'Identificador del rol (PK).' },
      { nombre: 'nombre', tipo: 'string (Enum)', obligatorio: true, enums: ['ADMINISTRADOR', 'ADMINISTRATIVO', 'DOCENTE', 'ALUMNO'], descripcion: 'Nombre del rol de usuario.' },
      { nombre: 'descripcion', tipo: 'string', obligatorio: false, enums: null, descripcion: 'Alcance de permisos del rol en la aplicación.' }
    ],
    camposCalculados: [],
    relaciones: [
      { campo: 'nombre', entidadDestino: 'Usuario', campoDestino: 'rol', tipo: '1:N' }
    ]
  }
];

// Helper para escapar valores CSV
function csvEscape(val) {
  if (val === null || val === undefined) return '';
  const s = String(val);
  if (s.includes(',') || s.includes('"') || s.includes('\n') || s.includes(';')) {
    return `"${s.replace(/"/g, '""')}"`;
  }
  return s;
}

// 4. GENERACIÓN DE endpoints.csv
function generarEndpointsCSV() {
  const headers = ['método', 'ruta', 'descripción', 'roles', 'pantallas', 'función de api', 'estado'];
  const lines = [headers.join(',')];

  for (const ep of ENDPOINTS_META) {
    const rolesStr = Array.isArray(ep.roles) ? ep.roles.join(', ') : ep.roles;
    const row = [
      csvEscape(ep.method),
      csvEscape(ep.url),
      csvEscape(ep.descripcion),
      csvEscape(rolesStr),
      csvEscape(ep.pantallas),
      csvEscape(ep.fn),
      csvEscape(ep.estado)
    ];
    lines.push(row.join(','));
  }

  const content = lines.join('\n') + '\n';
  fs.writeFileSync(path.join(docsFrontendDir, 'endpoints.csv'), content, 'utf8');
}

// 5. GENERACIÓN DE entidades.json
function generarEntidadesJSON() {
  const out = {
    entidades: ENTIDADES.map(e => ({
      nombre: e.nombre,
      descripcion: e.descripcion,
      pantallas: e.pantallas,
      camposPersistidos: e.camposPersistidos.map(c => ({
        nombre: c.nombre,
        tipo: c.tipo,
        obligatorio: c.obligatorio,
        enums: c.enums,
        descripcion: c.descripcion,
        categoria: 'PERSISTIDO'
      })),
      camposCalculados: e.camposCalculados.map(c => ({
        nombre: c.nombre,
        tipo: c.tipo,
        obligatorio: c.obligatorio,
        enums: c.enums,
        descripcion: c.descripcion,
        categoria: 'CALCULADO'
      })),
      campos: [
        ...e.camposPersistidos.map(c => ({ ...c, categoria: 'PERSISTIDO' })),
        ...e.camposCalculados.map(c => ({ ...c, categoria: 'CALCULADO' }))
      ],
      relaciones: e.relaciones
    }))
  };

  fs.writeFileSync(path.join(docsFrontendDir, 'entidades.json'), JSON.stringify(out, null, 2) + '\n', 'utf8');
}

// 6. GENERACIÓN DE entidades-y-endpoints.md
function generarEntidadesYEndpointsMD() {
  const lines = [];

  lines.push('# Relevamiento de Entidades y Endpoints de la Maqueta Frontend');
  lines.push('');
  lines.push('> **Documento de Auditoría y Especificación de Contratos**  ');
  lines.push('> **Proyecto:** Academit - Sistema de Gestión del Instituto  ');
  lines.push('> **Ubicación:** `docs/frontend/entidades-y-endpoints.md`  ');
  lines.push('> **Alcance:** Auditoría arquitectónica del frontend (`frontend/src/`) y alineación con el Diseño Entidad-Relación (DER).');
  lines.push('');
  lines.push('---');
  lines.push('');
  lines.push('## Resumen Ejecutivo');
  lines.push('');
  lines.push(`- **Total de Entidades Identificadas:** ${ENTIDADES.length}`);
  lines.push(`- **Total de Endpoints en el Contrato:** ${ENDPOINTS_META.length}`);
  lines.push(`- **Endpoints Implementados en API (\`real\`):** ${ENDPOINTS_META.length} (100.0%)`);
  lines.push('- **Endpoints Inferidos / Convención REST (\`inferido\`):** 0 (0.0%)');
  lines.push('- **Endpoints Faltantes / Brechas (\`falta\`):** 0 (0.0%)');
  lines.push('');
  lines.push('---');
  lines.push('');
  lines.push('## 1. Inventario de Entidades de Dominio');
  lines.push('');
  lines.push('A continuación se presentan las entidades del frontend categorizando sus campos en persistidos y calculados, enums y relaciones:');
  lines.push('');

  ENTIDADES.forEach((e, idx) => {
    lines.push(`### 1.${idx + 1} Entidad: \`${e.nombre}\``);
    lines.push(`**Descripción:** ${e.descripcion}`);
    lines.push('');

    // Notas especiales requeridas por el contrato
    if (e.nombre === 'Asistencia') {
      lines.push('> **Nota de Dominio:** Asistencia se registra por alumno, curso, materia y fecha.');
      lines.push('');
    }
    if (e.nombre === 'Matricula') {
      lines.push('> **Nota de Dominio:** Matricula.estadoPago no se guarda: es un campo calculado de solo lectura que el backend genera a partir de las cuotas asociadas (EN_MORA, AL_DIA, PENDIENTE).');
      lines.push('');
    }
    if (e.nombre === 'Cuota') {
      lines.push('> **Nota de Dominio:** \`idAlumno\` pasa a ser campo calculado derivado de la matrícula vinculada (\`idMatricula\` -> \`Matricula.idAlumno\`).');
      lines.push('');
    }

    lines.push('| Campo | Categoría | Tipo de Dato | Requerido | Valores Permitidos (Enum) | Descripción |');
    lines.push('| :--- | :---: | :--- | :---: | :--- | :--- |');

    e.camposPersistidos.forEach(c => {
      const enumsStr = c.enums ? c.enums.join(', ') : '-';
      lines.push(`| \`${c.nombre}\` | **PERSISTIDO** | ${c.tipo} | ${c.obligatorio ? 'Sí' : 'No'} | ${enumsStr} | ${c.descripcion} |`);
    });

    e.camposCalculados.forEach(c => {
      const enumsStr = c.enums ? c.enums.join(', ') : '-';
      lines.push(`| \`${c.nombre}\` | *CALCULADO* | ${c.tipo} | ${c.obligatorio ? 'Sí' : 'No'} | ${enumsStr} | ${c.descripcion} |`);
    });

    lines.push('');
    if (e.relaciones && e.relaciones.length > 0) {
      lines.push('**Relaciones:**');
      e.relaciones.forEach(r => {
        lines.push(`- Con \`${r.entidadDestino}\` (${r.tipo}): \`${r.campo}\` -> \`${r.campoDestino}\``);
      });
      lines.push('');
    }

    lines.push(`**Pantallas que la consumen:** \`${e.pantallas.join(', ')}\``);
    lines.push('');
    lines.push('---');
    lines.push('');
  });

  lines.push('## 2. Inventario de Endpoints REST');
  lines.push('');
  lines.push('Tabla exhaustiva con la totalidad de los contratos de comunicación consumidos o requeridos por el frontend:');
  lines.push('');
  lines.push('| Método | Ruta | Descripción | Roles Permitidos | Pantallas Consumidoras | Función API | Códigos HTTP | Estado |');
  lines.push('| :---: | :--- | :--- | :--- | :--- | :--- | :--- | :---: |');

  for (const ep of ENDPOINTS_META) {
    const rolesStr = Array.isArray(ep.roles) ? ep.roles.join(', ') : ep.roles;
    lines.push(`| \`${ep.method}\` | \`${ep.url}\` | ${ep.descripcion} | ${rolesStr} | ${ep.pantallas} | \`${ep.fn}\` | ${ep.codigos} | **${ep.estado}** |`);
  }
  lines.push('');

  fs.writeFileSync(path.join(docsFrontendDir, 'entidades-y-endpoints.md'), lines.join('\n') + '\n', 'utf8');
}

// 7. GENERACIÓN DE openapi.yaml (OpenAPI 3.0, Sin Ejemplos, con x-roles, respuestas 200, 201, 204, 400, 401, 403, 404, 409)
function generarOpenAPIYaml() {
  const lines = [];

  lines.push('openapi: 3.0.3');
  lines.push('info:');
  lines.push('  title: Academit API');
  lines.push('  version: 1.0.0');
  lines.push('  description: Contrato formal de API REST entre frontend y backend del sistema Academit.');
  lines.push('  license:');
  lines.push('    name: BSD-3-Clause');
  lines.push('    url: https://opensource.org/licenses/BSD-3-Clause');
  lines.push('servers:');
  lines.push('  - url: /api');
  lines.push('    description: Servidor de desarrollo y producción API REST');
  lines.push('security:');
  lines.push('  - bearerAuth: []');
  lines.push('paths:');

  // Agrupar endpoints por ruta
  const pathsMap = new Map();
  for (const ep of ENDPOINTS_META) {
    if (!pathsMap.has(ep.url)) {
      pathsMap.set(ep.url, []);
    }
    pathsMap.get(ep.url).push(ep);
  }

  for (const [url, methods] of pathsMap.entries()) {
    lines.push(`  ${url}:`);
    for (const ep of methods) {
      const m = ep.method.toLowerCase();
      lines.push(`    ${m}:`);
      lines.push(`      tags:`);
      lines.push(`        - ${ep.tag}`);
      lines.push(`      summary: ${JSON.stringify(ep.descripcion)}`);
      lines.push(`      operationId: ${ep.fn}`);
      lines.push(`      x-roles:`);
      for (const r of ep.roles) {
        lines.push(`        - ${r}`);
      }

      if (ep.fn === 'loginApi' || ep.fn === 'registroApi' || ep.fn === 'getOfertaCarrerasApi' || ep.fn === 'getOfertaPlanesApi') {
        lines.push(`      security: []`);
      }

      // Parámetros
      const allParams = [];
      // Path params
      if (ep.pathParams && ep.pathParams.length > 0) {
        for (const pp of ep.pathParams) {
          allParams.push({
            name: pp.name,
            in: 'path',
            required: true,
            description: pp.desc,
            schema: { type: pp.type || 'integer' }
          });
        }
      }
      // Query params
      if (ep.queryParams && ep.queryParams.length > 0) {
        for (const qp of ep.queryParams) {
          allParams.push({
            name: qp.name,
            in: 'query',
            required: false,
            description: qp.desc,
            schema: { type: qp.type || 'string' }
          });
        }
      }

      if (allParams.length > 0) {
        lines.push(`      parameters:`);
        for (const p of allParams) {
          lines.push(`        - name: ${p.name}`);
          lines.push(`          in: ${p.in}`);
          lines.push(`          required: ${p.required}`);
          lines.push(`          description: ${JSON.stringify(p.description)}`);
          lines.push(`          schema:`);
          lines.push(`            type: ${p.schema.type}`);
        }
      }

      // Request Body
      if (ep.requestBody) {
        lines.push(`      requestBody:`);
        lines.push(`        required: true`);
        lines.push(`        content:`);
        lines.push(`          application/json:`);
        lines.push(`            schema:`);
        lines.push(`              $ref: '#/components/schemas/${ep.requestBody}'`);
      }

      // Responses
      lines.push(`      responses:`);
      const codigosArr = ep.codigos.split(',').map(c => c.trim());

      // 200 / 201 / 204
      if (ep.successStatus === '204') {
        lines.push(`        '204':`);
        lines.push(`          description: Operación completada sin contenido`);
      } else if (ep.successStatus === '201') {
        lines.push(`        '201':`);
        lines.push(`          description: Recurso creado exitosamente`);
        lines.push(`          content:`);
        lines.push(`            application/json:`);
        lines.push(`              schema:`);
        if (ep.responseSchema.startsWith('Array<')) {
          const itemSchema = ep.responseSchema.replace('Array<', '').replace('>', '');
          lines.push(`                type: array`);
          lines.push(`                items:`);
          lines.push(`                  $ref: '#/components/schemas/${itemSchema}'`);
        } else {
          lines.push(`                $ref: '#/components/schemas/${ep.responseSchema}'`);
        }
      } else {
        lines.push(`        '200':`);
        lines.push(`          description: Consulta o actualización exitosa`);
        lines.push(`          content:`);
        lines.push(`            application/json:`);
        lines.push(`              schema:`);
        if (ep.responseSchema.startsWith('Array<')) {
          const itemSchema = ep.responseSchema.replace('Array<', '').replace('>', '');
          lines.push(`                type: array`);
          lines.push(`                items:`);
          lines.push(`                  $ref: '#/components/schemas/${itemSchema}'`);
        } else {
          lines.push(`                $ref: '#/components/schemas/${ep.responseSchema}'`);
        }
      }

      // Errores estándar requeridos: 400, 401, 403, 404, 409
      for (const code of ['400', '401', '403', '404', '409']) {
        if (codigosArr.includes(code)) {
          let descError = 'Error de petición';
          if (code === '400') descError = 'Solicitud inválida o error en parámetros';
          if (code === '401') descError = 'Credenciales no provistas o inválidas';
          if (code === '403') descError = 'Acceso denegado para el rol actual';
          if (code === '404') descError = 'Recurso solicitado no encontrado';
          if (code === '409') descError = 'Conflicto con el estado actual o regla de negocio violada';

          lines.push(`        '${code}':`);
          lines.push(`          description: ${JSON.stringify(descError)}`);
          lines.push(`          content:`);
          lines.push(`            application/json:`);
          lines.push(`              schema:`);
          lines.push(`                $ref: '#/components/schemas/ApiError'`);
        }
      }
    }
  }

  // Componentes y Schemas
  lines.push('components:');
  lines.push('  securitySchemes:');
  lines.push('    bearerAuth:');
  lines.push('      type: http');
  lines.push('      scheme: bearer');
  lines.push('      bearerFormat: JWT');
  lines.push('  schemas:');

  // ApiError y FieldError
  lines.push('    ApiError:');
  lines.push('      type: object');
  lines.push('      required:');
  lines.push('        - status');
  lines.push('        - message');
  lines.push('        - errors');
  lines.push('      properties:');
  lines.push('        status:');
  lines.push('          type: integer');
  lines.push('        message:');
  lines.push('          type: string');
  lines.push('        errors:');
  lines.push('          type: array');
  lines.push('          items:');
  lines.push('            $ref: \'#/components/schemas/FieldError\'');
  lines.push('    FieldError:');
  lines.push('      type: object');
  lines.push('      required:');
  lines.push('        - campo');
  lines.push('        - mensaje');
  lines.push('      properties:');
  lines.push('        campo:');
  lines.push('          type: string');
  lines.push('        mensaje:');
  lines.push('          type: string');

  // SuccessResponseDTO
  lines.push('    SuccessResponseDTO:');
  lines.push('      type: object');
  lines.push('      required:');
  lines.push('        - success');
  lines.push('      properties:');
  lines.push('        success:');
  lines.push('          type: boolean');
  lines.push('        message:');
  lines.push('          type: string');

  // Enums
  lines.push('    TurnoEnum:');
  lines.push('      type: string');
  lines.push('      enum:');
  lines.push('        - MANANA');
  lines.push('        - TARDE');
  lines.push('        - NOCHE');

  lines.push('    TipoSolicitudEnum:');
  lines.push('      type: string');
  lines.push('      enum:');
  lines.push('        - CONSTANCIA');
  lines.push('        - PASANTIA');
  lines.push('        - TITULO_INTERMEDIO');
  lines.push('        - ACADEMICA');
  lines.push('        - CAMBIO_CARRERA');
  lines.push('        - REINCORPORACION');

  lines.push('    TipoDocumentacionEnum:');
  lines.push('      type: string');
  lines.push('      enum:');
  lines.push('        - DNI');
  lines.push('        - TITULO_SECUNDARIO');
  lines.push('        - APTO_MEDICO');
  lines.push('        - FOTOS');

  lines.push('    EstadoCuotaEnum:');
  lines.push('      type: string');
  lines.push('      enum:');
  lines.push('        - PENDIENTE');
  lines.push('        - PAGADA');
  lines.push('        - VENCIDA');
  lines.push('        - ANULADA');

  lines.push('    MedioPagoEnum:');
  lines.push('      type: string');
  lines.push('      enum:');
  lines.push('        - EFECTIVO');
  lines.push('        - TRANSFERENCIA');
  lines.push('        - TARJETA_CREDITO');
  lines.push('        - TARJETA_DEBITO');
  lines.push('        - OTRO');

  // Entidades DTOs
  const DTO_DEFINITIONS = [
    {
      name: 'AlumnoDTO',
      required: ['idAlumno', 'legajo', 'nombre', 'apellido', 'dni', 'email', 'idCarrera', 'idPlan', 'cicloLectivo', 'estado', 'estadoMatricula'],
      properties: {
        idAlumno: { type: 'integer' },
        legajo: { type: 'string' },
        nombre: { type: 'string' },
        apellido: { type: 'string' },
        dni: { type: 'string' },
        email: { type: 'string' },
        telefono: { type: 'string' },
        domicilio: { type: 'string' },
        fechaNacimiento: { type: 'string', format: 'date' },
        idCarrera: { type: 'integer' },
        carrera: { type: 'string' },
        idPlan: { type: 'integer' },
        planNombre: { type: 'string' },
        idCurso: { type: 'integer' },
        cursoNombre: { type: 'string' },
        cicloLectivo: { type: 'integer' },
        estado: { type: 'string', enum: ['ACTIVO', 'INACTIVO'] },
        estadoMatricula: { type: 'string', enum: ['ACTIVA', 'PENDIENTE', 'CANCELADA'] },
        idMatricula: { type: 'integer' }
      }
    },
    {
      name: 'AlumnoCreateDTO',
      required: ['nombre', 'apellido', 'dni', 'email', 'fechaNacimiento', 'idCarrera', 'idPlan'],
      properties: {
        nombre: { type: 'string' },
        apellido: { type: 'string' },
        dni: { type: 'string' },
        email: { type: 'string' },
        telefono: { type: 'string' },
        domicilio: { type: 'string' },
        fechaNacimiento: { type: 'string', format: 'date' },
        idCarrera: { type: 'integer' },
        idPlan: { type: 'integer' }
      }
    },
    {
      name: 'AlumnoUpdateDTO',
      required: [],
      properties: {
        nombre: { type: 'string' },
        apellido: { type: 'string' },
        email: { type: 'string' },
        telefono: { type: 'string' },
        domicilio: { type: 'string' },
        estado: { type: 'string', enum: ['ACTIVO', 'INACTIVO'] },
        idPlan: { type: 'integer' }
      }
    },
    {
      name: 'SituacionAcademicaDTO',
      required: ['materiasAprobadas', 'promedio'],
      properties: {
        materiasAprobadas: { type: 'integer' },
        promedio: { type: 'number' },
        regularidades: { type: 'array', items: { type: 'string' } }
      }
    },
    {
      name: 'SituacionAdministrativaDTO',
      required: ['totalDeuda'],
      properties: {
        cuotasPagas: { type: 'integer' },
        cuotasPendientes: { type: 'integer' },
        totalDeuda: { type: 'number' },
        matricula: { type: 'string' }
      }
    },
    {
      name: 'DocenteDTO',
      required: ['idDocente', 'legajo', 'nombre', 'apellido', 'dni', 'email', 'titulo', 'estado'],
      properties: {
        idDocente: { type: 'integer' },
        legajo: { type: 'string' },
        nombre: { type: 'string' },
        apellido: { type: 'string' },
        dni: { type: 'string' },
        email: { type: 'string' },
        telefono: { type: 'string' },
        titulo: { type: 'string' },
        estado: { type: 'string', enum: ['ACTIVO', 'INACTIVO'] }
      }
    },
    {
      name: 'DocenteCreateDTO',
      required: ['legajo', 'nombre', 'apellido', 'dni', 'email', 'titulo'],
      properties: {
        legajo: { type: 'string' },
        nombre: { type: 'string' },
        apellido: { type: 'string' },
        dni: { type: 'string' },
        email: { type: 'string' },
        telefono: { type: 'string' },
        titulo: { type: 'string' }
      }
    },
    {
      name: 'DocenteUpdateDTO',
      required: [],
      properties: {
        nombre: { type: 'string' },
        apellido: { type: 'string' },
        email: { type: 'string' },
        telefono: { type: 'string' },
        titulo: { type: 'string' },
        estado: { type: 'string', enum: ['ACTIVO', 'INACTIVO'] }
      }
    },
    {
      name: 'CarreraDTO',
      required: ['idCarrera', 'codigo', 'nombre', 'tituloOtorgado', 'duracionAnios', 'estado'],
      properties: {
        idCarrera: { type: 'integer' },
        codigo: { type: 'string' },
        nombre: { type: 'string' },
        tituloOtorgado: { type: 'string' },
        duracionAnios: { type: 'integer' },
        resolucion: { type: 'string' },
        estado: { type: 'string', enum: ['ACTIVA', 'INACTIVA'] }
      }
    },
    {
      name: 'CarreraCreateDTO',
      required: ['codigo', 'nombre', 'tituloOtorgado', 'duracionAnios'],
      properties: {
        codigo: { type: 'string' },
        nombre: { type: 'string' },
        tituloOtorgado: { type: 'string' },
        duracionAnios: { type: 'integer' },
        resolucion: { type: 'string' }
      }
    },
    {
      name: 'CarreraUpdateDTO',
      required: [],
      properties: {
        codigo: { type: 'string' },
        nombre: { type: 'string' },
        tituloOtorgado: { type: 'string' },
        duracionAnios: { type: 'integer' },
        resolucion: { type: 'string' },
        estado: { type: 'string', enum: ['ACTIVA', 'INACTIVA'] }
      }
    },
    {
      name: 'PlanEstudioDTO',
      required: ['idPlan', 'idCarrera', 'codigo', 'nombre', 'anioAprobacion', 'cantidadMaterias', 'estado'],
      properties: {
        idPlan: { type: 'integer' },
        idCarrera: { type: 'integer' },
        carreraNombre: { type: 'string' },
        codigo: { type: 'string' },
        nombre: { type: 'string' },
        anioAprobacion: { type: 'integer' },
        cantidadMaterias: { type: 'integer' },
        estado: { type: 'string', enum: ['VIGENTE', 'CERRADO'] }
      }
    },
    {
      name: 'MateriaDTO',
      required: ['idMateria', 'codigo', 'nombre', 'idCarrera', 'idPlan', 'anio', 'regimen', 'horasSemanales'],
      properties: {
        idMateria: { type: 'integer' },
        codigo: { type: 'string' },
        nombre: { type: 'string' },
        idCarrera: { type: 'integer' },
        carreraNombre: { type: 'string' },
        idPlan: { type: 'integer' },
        planNombre: { type: 'string' },
        anio: { type: 'integer' },
        regimen: { type: 'string', enum: ['ANUAL', 'CUATRIMESTRAL'] },
        horasSemanales: { type: 'integer' },
        correlativas: { type: 'array', items: { type: 'integer' } }
      }
    },
    {
      name: 'MateriaCreateDTO',
      required: ['codigo', 'nombre', 'idCarrera', 'idPlan', 'anio', 'regimen', 'horasSemanales'],
      properties: {
        codigo: { type: 'string' },
        nombre: { type: 'string' },
        idCarrera: { type: 'integer' },
        idPlan: { type: 'integer' },
        anio: { type: 'integer' },
        regimen: { type: 'string', enum: ['ANUAL', 'CUATRIMESTRAL'] },
        horasSemanales: { type: 'integer' },
        correlativas: { type: 'array', items: { type: 'integer' } }
      }
    },
    {
      name: 'MateriaUpdateDTO',
      required: [],
      properties: {
        nombre: { type: 'string' },
        anio: { type: 'integer' },
        regimen: { type: 'string', enum: ['ANUAL', 'CUATRIMESTRAL'] },
        horasSemanales: { type: 'integer' },
        correlativas: { type: 'array', items: { type: 'integer' } }
      }
    },
    {
      name: 'CursoDTO',
      required: ['idCurso', 'codigo', 'nombre', 'idCarrera', 'idPlan', 'turno', 'capacidadMaxima', 'inscriptos', 'cuposDisponibles', 'cicloLectivo'],
      properties: {
        idCurso: { type: 'integer' },
        codigo: { type: 'string' },
        nombre: { type: 'string' },
        idCarrera: { type: 'integer' },
        carreraNombre: { type: 'string' },
        idPlan: { type: 'integer' },
        planNombre: { type: 'string' },
        turno: { $ref: '#/components/schemas/TurnoEnum' },
        capacidadMaxima: { type: 'integer' },
        inscriptos: { type: 'integer' },
        cuposDisponibles: { type: 'integer' },
        cicloLectivo: { type: 'integer' },
        aula: { type: 'string' }
      }
    },
    {
      name: 'CursoCreateDTO',
      required: ['codigo', 'nombre', 'idCarrera', 'idPlan', 'turno', 'capacidadMaxima', 'cicloLectivo'],
      properties: {
        codigo: { type: 'string' },
        nombre: { type: 'string' },
        idCarrera: { type: 'integer' },
        idPlan: { type: 'integer' },
        turno: { $ref: '#/components/schemas/TurnoEnum' },
        aula: { type: 'string' },
        capacidadMaxima: { type: 'integer' },
        cicloLectivo: { type: 'integer' }
      }
    },
    {
      name: 'CursoUpdateDTO',
      required: [],
      properties: {
        nombre: { type: 'string' },
        turno: { $ref: '#/components/schemas/TurnoEnum' },
        aula: { type: 'string' },
        capacidadMaxima: { type: 'integer' }
      }
    },
    {
      name: 'CicloLectivoDTO',
      required: ['idCiclo', 'anio', 'nombre', 'fechaInicio', 'fechaFin', 'estado'],
      properties: {
        idCiclo: { type: 'integer' },
        anio: { type: 'integer' },
        nombre: { type: 'string' },
        fechaInicio: { type: 'string', format: 'date' },
        fechaFin: { type: 'string', format: 'date' },
        estado: { type: 'string', enum: ['ACTIVO', 'CERRADO', 'PLANIFICADO'] }
      }
    },
    {
      name: 'CicloLectivoCreateDTO',
      required: ['anio', 'nombre', 'fechaInicio', 'fechaFin'],
      properties: {
        anio: { type: 'integer' },
        nombre: { type: 'string' },
        fechaInicio: { type: 'string', format: 'date' },
        fechaFin: { type: 'string', format: 'date' },
        estado: { type: 'string', enum: ['ACTIVO', 'CERRADO', 'PLANIFICADO'] }
      }
    },
    {
      name: 'CicloLectivoUpdateDTO',
      required: [],
      properties: {
        nombre: { type: 'string' },
        fechaInicio: { type: 'string', format: 'date' },
        fechaFin: { type: 'string', format: 'date' },
        estado: { type: 'string', enum: ['ACTIVO', 'CERRADO', 'PLANIFICADO'] }
      }
    },
    {
      name: 'AsignacionDTO',
      required: ['idAsignacion', 'idDocente', 'idMateria', 'idCurso', 'rolDocente'],
      properties: {
        idAsignacion: { type: 'integer' },
        idDocente: { type: 'integer' },
        docenteNombre: { type: 'string' },
        idMateria: { type: 'integer' },
        materiaNombre: { type: 'string' },
        idCurso: { type: 'integer' },
        cursoNombre: { type: 'string' },
        rolDocente: { type: 'string', enum: ['Titular', 'Adjunto', 'JTP', 'Ayudante'] }
      }
    },
    {
      name: 'AsignacionCreateDTO',
      required: ['idDocente', 'idMateria', 'idCurso'],
      properties: {
        idDocente: { type: 'integer' },
        idMateria: { type: 'integer' },
        idCurso: { type: 'integer' },
        rolDocente: { type: 'string', enum: ['Titular', 'Adjunto', 'JTP', 'Ayudante'] }
      }
    },
    {
      name: 'InscripcionDTO',
      required: ['idInscripcion', 'idAlumno', 'idCurso', 'cicloLectivo', 'fechaInscripcion', 'estado'],
      properties: {
        idInscripcion: { type: 'integer' },
        idAlumno: { type: 'integer' },
        alumnoNombre: { type: 'string' },
        idCurso: { type: 'integer' },
        cursoNombre: { type: 'string' },
        cicloLectivo: { type: 'integer' },
        fechaInscripcion: { type: 'string', format: 'date' },
        estado: { type: 'string', enum: ['CONFIRMADA', 'CANCELADA'] }
      }
    },
    {
      name: 'InscripcionCreateDTO',
      required: ['idAlumno', 'idCurso'],
      properties: {
        idAlumno: { type: 'integer' },
        idCurso: { type: 'integer' },
        cicloLectivo: { type: 'integer' }
      }
    },
    {
      name: 'MatriculaDTO',
      required: ['idMatricula', 'idAlumno', 'idCarrera', 'idCiclo', 'cicloLectivo', 'fecha', 'estado'],
      properties: {
        idMatricula: { type: 'integer' },
        idAlumno: { type: 'integer' },
        alumnoNombre: { type: 'string' },
        idCarrera: { type: 'integer' },
        carreraNombre: { type: 'string' },
        idCiclo: { type: 'integer' },
        cicloLectivoNombre: { type: 'string' },
        cicloLectivo: { type: 'integer' },
        fecha: { type: 'string', format: 'date' },
        estado: { type: 'string', enum: ['ACTIVA', 'PENDIENTE', 'CANCELADA'] },
        estadoPago: { type: 'string', enum: ['AL_DIA', 'EN_MORA', 'PENDIENTE'], readOnly: true, description: 'Estado financiero calculado a partir de las cuotas asociadas (solo lectura).' }
      }
    },
    {
      name: 'MatriculaUpdateDTO',
      required: ['estado'],
      properties: {
        estado: { type: 'string', enum: ['ACTIVA', 'PENDIENTE', 'CANCELADA'] }
      }
    },
    {
      name: 'MatriculaCancelDTO',
      required: [],
      properties: {
        motivo: { type: 'string' }
      }
    },
    {
      name: 'CuotaDTO',
      required: ['idCuota', 'idMatricula', 'concepto', 'mes', 'anio', 'fechaVencimiento', 'importe', 'tipo', 'estado'],
      properties: {
        idCuota: { type: 'integer' },
        idMatricula: { type: 'integer' },
        idAlumno: { type: 'integer', description: 'Campo calculado derivado de la matricula' },
        alumnoNombre: { type: 'string', description: 'Campo calculado' },
        concepto: { type: 'string' },
        mes: { type: 'integer' },
        anio: { type: 'integer' },
        fechaVencimiento: { type: 'string', format: 'date' },
        importe: { type: 'number' },
        tipo: { type: 'string', enum: ['MENSUAL', 'SEGURO_CUATRIMESTRAL'] },
        estado: { $ref: '#/components/schemas/EstadoCuotaEnum' },
        fechaPago: { type: 'string', format: 'date' }
      }
    },
    {
      name: 'PagoDTO',
      required: ['idPago', 'idAlumno', 'concepto', 'importe', 'fechaPago', 'medioPago', 'comprobante', 'estado'],
      properties: {
        idPago: { type: 'integer' },
        idCuota: { type: 'integer' },
        idAlumno: { type: 'integer' },
        alumnoNombre: { type: 'string' },
        concepto: { type: 'string' },
        importe: { type: 'number' },
        fechaPago: { type: 'string', format: 'date' },
        medioPago: { $ref: '#/components/schemas/MedioPagoEnum' },
        comprobante: { type: 'string', readOnly: true, description: 'Número de comprobante oficial AAAA-NNNNNN generado por el backend (solo lectura).' },
        estado: { type: 'string', enum: ['ACREDITADO', 'PENDIENTE', 'RECHAZADO'] }
      }
    },
    {
      name: 'PagoCreateDTO',
      required: ['idCuota', 'importe'],
      properties: {
        idCuota: { type: 'integer' },
        importe: { type: 'number' },
        medioPago: { $ref: '#/components/schemas/MedioPagoEnum' }
      }
    },
    {
      name: 'EvaluacionDTO',
      required: ['idEvaluacion', 'idMateria', 'idCurso', 'fecha', 'tipo', 'descripcion'],
      properties: {
        idEvaluacion: { type: 'integer' },
        idMateria: { type: 'integer' },
        materiaNombre: { type: 'string' },
        idCurso: { type: 'integer' },
        cursoNombre: { type: 'string' },
        fecha: { type: 'string', format: 'date' },
        tipo: { type: 'string', enum: ['PARCIAL', 'TRABAJO_PRACTICO'] },
        descripcion: { type: 'string' },
        ponderacion: { type: 'string' }
      }
    },
    {
      name: 'EvaluacionCreateDTO',
      required: ['idMateria', 'idCurso', 'fecha', 'tipo', 'descripcion'],
      properties: {
        idMateria: { type: 'integer' },
        idCurso: { type: 'integer' },
        fecha: { type: 'string', format: 'date' },
        tipo: { type: 'string', enum: ['PARCIAL', 'TRABAJO_PRACTICO'] },
        descripcion: { type: 'string' },
        ponderacion: { type: 'string' }
      }
    },
    {
      name: 'CalificacionDTO',
      required: ['idCalificacion', 'idAlumno', 'idMateria', 'evaluacionNombre', 'tipo', 'nota', 'fecha', 'condicion'],
      properties: {
        idCalificacion: { type: 'integer' },
        idAlumno: { type: 'integer' },
        alumnoNombre: { type: 'string' },
        idMateria: { type: 'integer' },
        materiaNombre: { type: 'string' },
        evaluacionNombre: { type: 'string' },
        tipo: { type: 'string', enum: ['PARCIAL', 'TRABAJO_PRACTICO'] },
        nota: { type: 'integer' },
        fecha: { type: 'string', format: 'date' },
        condicion: { type: 'string', enum: ['APROBADO', 'DESAPROBADO'] },
        observacion: { type: 'string' }
      }
    },
    {
      name: 'CalificacionSaveDTO',
      required: ['idAlumno', 'idMateria', 'evaluacionNombre', 'nota'],
      properties: {
        idAlumno: { type: 'integer' },
        idMateria: { type: 'integer' },
        evaluacionNombre: { type: 'string' },
        tipo: { type: 'string', enum: ['PARCIAL', 'TRABAJO_PRACTICO'] },
        nota: { type: 'integer' },
        fecha: { type: 'string', format: 'date' },
        observacion: { type: 'string' }
      }
    },
    {
      name: 'CalificacionesLoteDTO',
      required: ['calificaciones'],
      properties: {
        calificaciones: {
          type: 'array',
          items: {
            type: 'object',
            properties: {
              idAlumno: { type: 'integer' },
              nota: { type: 'integer' },
              observacion: { type: 'string' }
            }
          }
        }
      }
    },
    {
      name: 'AsistenciaDTO',
      required: ['idAsistencia', 'idAlumno', 'idMateria', 'idCurso', 'fecha', 'estado'],
      properties: {
        idAsistencia: { type: 'integer' },
        idAlumno: { type: 'integer' },
        alumnoNombre: { type: 'string' },
        idMateria: { type: 'integer' },
        materiaNombre: { type: 'string' },
        idCurso: { type: 'integer' },
        fecha: { type: 'string', format: 'date' },
        estado: { type: 'string', enum: ['PRESENTE', 'AUSENTE', 'TARDANZA', 'JUSTIFICADO'] }
      }
    },
    {
      name: 'AsistenciasLoteDTO',
      required: ['asistencias'],
      properties: {
        asistencias: {
          type: 'array',
          items: {
            type: 'object',
            properties: {
              idAlumno: { type: 'integer' },
              idMateria: { type: 'integer' },
              idCurso: { type: 'integer' },
              fecha: { type: 'string', format: 'date' },
              estado: { type: 'string', enum: ['PRESENTE', 'AUSENTE', 'TARDANZA', 'JUSTIFICADO'] }
            }
          }
        }
      }
    },
    {
      name: 'MesaExamenDTO',
      required: ['idMesa', 'idMateria', 'idCarrera', 'fecha', 'hora', 'turno', 'tribunal', 'aula', 'cupoMaximo', 'inscriptos', 'cuposDisponibles', 'estado'],
      properties: {
        idMesa: { type: 'integer' },
        idMateria: { type: 'integer' },
        materiaNombre: { type: 'string' },
        idCarrera: { type: 'integer' },
        carreraNombre: { type: 'string' },
        fecha: { type: 'string', format: 'date' },
        hora: { type: 'string' },
        turno: { $ref: '#/components/schemas/TurnoEnum' },
        tribunal: { type: 'string' },
        aula: { type: 'string' },
        cupoMaximo: { type: 'integer' },
        inscriptos: { type: 'integer' },
        cuposDisponibles: { type: 'integer' },
        alumnosInscriptosIds: { type: 'array', items: { type: 'integer' } },
        estado: { type: 'string', enum: ['ABIERTA', 'CERRADA', 'FINALIZADA'] }
      }
    },
    {
      name: 'MesaExamenCreateDTO',
      required: ['idMateria', 'idCarrera', 'fecha', 'hora', 'turno', 'tribunal', 'aula', 'cupoMaximo'],
      properties: {
        idMateria: { type: 'integer' },
        idCarrera: { type: 'integer' },
        fecha: { type: 'string', format: 'date' },
        hora: { type: 'string' },
        turno: { $ref: '#/components/schemas/TurnoEnum' },
        tribunal: { type: 'string' },
        aula: { type: 'string' },
        cupoMaximo: { type: 'integer' }
      }
    },
    {
      name: 'MesaExamenUpdateDTO',
      required: [],
      properties: {
        fecha: { type: 'string', format: 'date' },
        hora: { type: 'string' },
        turno: { $ref: '#/components/schemas/TurnoEnum' },
        tribunal: { type: 'string' },
        aula: { type: 'string' },
        cupoMaximo: { type: 'integer' },
        estado: { type: 'string', enum: ['ABIERTA', 'CERRADA', 'FINALIZADA'] }
      }
    },
    {
      name: 'SolicitudDTO',
      required: ['idSolicitud', 'idAlumno', 'tipo', 'fechaSolicitud', 'motivo', 'estado'],
      properties: {
        idSolicitud: { type: 'integer' },
        idAlumno: { type: 'integer' },
        alumnoNombre: { type: 'string' },
        tipo: { $ref: '#/components/schemas/TipoSolicitudEnum' },
        fechaSolicitud: { type: 'string', format: 'date' },
        motivo: { type: 'string' },
        observaciones: { type: 'string' },
        estado: { type: 'string', enum: ['PENDIENTE', 'EN_PROCESO', 'APROBADA', 'RECHAZADA'] },
        fechaResolucion: { type: 'string', format: 'date' },
        respuesta: { type: 'string' }
      }
    },
    {
      name: 'SolicitudCreateDTO',
      required: ['idAlumno', 'tipo', 'motivo'],
      properties: {
        idAlumno: { type: 'integer' },
        tipo: { $ref: '#/components/schemas/TipoSolicitudEnum' },
        motivo: { type: 'string' },
        observaciones: { type: 'string' }
      }
    },
    {
      name: 'SolicitudResolverDTO',
      required: ['estado'],
      properties: {
        estado: { type: 'string', enum: ['APROBADA', 'RECHAZADA', 'EN_PROCESO'] },
        observacion: { type: 'string' }
      }
    },
    {
      name: 'DeclaracionJuradaDTO',
      required: ['idDeclaracion', 'idAlumno', 'idCiclo', 'fecha', 'materiasDeclaradasIds', 'estado'],
      properties: {
        idDeclaracion: { type: 'integer' },
        idAlumno: { type: 'integer' },
        alumnoNombre: { type: 'string' },
        idCiclo: { type: 'integer' },
        cicloNombre: { type: 'string' },
        fecha: { type: 'string', format: 'date' },
        materiasDeclaradasIds: { type: 'array', items: { type: 'integer' } },
        materiasNombres: { type: 'array', items: { type: 'string' } },
        estado: { type: 'string', enum: ['VALIDADA', 'PENDIENTE', 'RECHAZADA'] },
        observaciones: { type: 'string' }
      }
    },
    {
      name: 'DeclaracionJuradaCreateDTO',
      required: ['idAlumno', 'idCiclo', 'materiasDeclaradasIds'],
      properties: {
        idAlumno: { type: 'integer' },
        idCiclo: { type: 'integer' },
        materiasDeclaradasIds: { type: 'array', items: { type: 'integer' } },
        observaciones: { type: 'string' }
      }
    },
    {
      name: 'DocumentacionDTO',
      required: ['idDocumento', 'idAlumno', 'tipo', 'obligatorio', 'estado'],
      properties: {
        idDocumento: { type: 'integer' },
        idAlumno: { type: 'integer' },
        alumnoNombre: { type: 'string' },
        tipo: { $ref: '#/components/schemas/TipoDocumentacionEnum' },
        obligatorio: { type: 'boolean' },
        estado: { type: 'string', enum: ['ENTREGADO', 'PENDIENTE', 'RECHAZADO'] },
        fechaEntrega: { type: 'string', format: 'date' },
        observaciones: { type: 'string' }
      }
    },
    {
      name: 'DocumentacionUpdateDTO',
      required: ['estado'],
      properties: {
        estado: { type: 'string', enum: ['ENTREGADO', 'PENDIENTE', 'RECHAZADO'] },
        observaciones: { type: 'string' },
        fechaEntrega: { type: 'string', format: 'date' }
      }
    },
    {
      name: 'UsuarioDTO',
      required: ['idUsuario', 'username', 'nombre', 'email', 'rol', 'estado'],
      properties: {
        idUsuario: { type: 'integer' },
        username: { type: 'string' },
        nombre: { type: 'string' },
        email: { type: 'string' },
        telefono: { type: 'string' },
        domicilio: { type: 'string' },
        rol: { type: 'string', enum: ['ADMINISTRADOR', 'ADMINISTRATIVO', 'DOCENTE', 'ALUMNO'] },
        estado: { type: 'string', enum: ['ACTIVO', 'INACTIVO'] },
        ultimoAcceso: { type: 'string' }
      }
    },
    {
      name: 'UsuarioCreateDTO',
      required: ['username', 'password', 'nombre', 'email', 'rol'],
      properties: {
        username: { type: 'string' },
        password: { type: 'string' },
        nombre: { type: 'string' },
        email: { type: 'string' },
        telefono: { type: 'string' },
        domicilio: { type: 'string' },
        rol: { type: 'string', enum: ['ADMINISTRADOR', 'ADMINISTRATIVO', 'DOCENTE', 'ALUMNO'] }
      }
    },
    {
      name: 'UsuarioUpdateDTO',
      required: [],
      properties: {
        nombre: { type: 'string' },
        email: { type: 'string' },
        telefono: { type: 'string' },
        domicilio: { type: 'string' },
        rol: { type: 'string', enum: ['ADMINISTRADOR', 'ADMINISTRATIVO', 'DOCENTE', 'ALUMNO'] },
        estado: { type: 'string', enum: ['ACTIVO', 'INACTIVO'] }
      }
    },
    {
      name: 'PerfilUsuarioDTO',
      required: ['username', 'nombre', 'email', 'rol'],
      properties: {
        username: { type: 'string' },
        nombre: { type: 'string' },
        email: { type: 'string' },
        telefono: { type: 'string' },
        domicilio: { type: 'string' },
        rol: { type: 'string', enum: ['ADMINISTRADOR', 'ADMINISTRATIVO', 'DOCENTE', 'ALUMNO'] }
      }
    },
    {
      name: 'LoginRequestDTO',
      required: ['username', 'password'],
      properties: {
        username: { type: 'string' },
        password: { type: 'string' }
      }
    },
    {
      name: 'UsuarioSesionDTO',
      required: ['idUsuario', 'username', 'nombre', 'rol'],
      properties: {
        idUsuario: { type: 'integer' },
        username: { type: 'string' },
        nombre: { type: 'string' },
        rol: { type: 'string', enum: ['ADMINISTRADOR', 'ADMINISTRATIVO', 'DOCENTE', 'ALUMNO'] },
        idAlumno: { type: 'integer' },
        idDocente: { type: 'integer' }
      }
    },
    {
      name: 'LoginResponseDTO',
      required: ['usuario', 'token'],
      properties: {
        usuario: { $ref: '#/components/schemas/UsuarioSesionDTO' },
        token: { type: 'string' }
      }
    },
    {
      name: 'RegistroAlumnoDTO',
      required: ['nombre', 'apellido', 'dni', 'email', 'fechaNacimiento', 'idCarrera', 'idPlan', 'username', 'password'],
      properties: {
        nombre: { type: 'string' },
        apellido: { type: 'string' },
        dni: { type: 'string' },
        email: { type: 'string' },
        telefono: { type: 'string' },
        domicilio: { type: 'string' },
        fechaNacimiento: { type: 'string', format: 'date' },
        idCarrera: { type: 'integer' },
        idPlan: { type: 'integer' },
        username: { type: 'string' },
        password: { type: 'string' }
      }
    },
    {
      name: 'CambiarPasswordRequestDTO',
      required: ['actual', 'nueva'],
      properties: {
        actual: { type: 'string' },
        nueva: { type: 'string' }
      }
    }
  ];

  for (const dto of DTO_DEFINITIONS) {
    lines.push(`    ${dto.name}:`);
    lines.push(`      type: object`);
    if (dto.required && dto.required.length > 0) {
      lines.push(`      required:`);
      for (const req of dto.required) {
        lines.push(`        - ${req}`);
      }
    }
    lines.push(`      properties:`);
    for (const [propName, propDef] of Object.entries(dto.properties)) {
      lines.push(`        ${propName}:`);
      if (propDef.$ref) {
        lines.push(`          $ref: '${propDef.$ref}'`);
      } else {
        lines.push(`          type: ${propDef.type}`);
        if (propDef.format) lines.push(`          format: ${propDef.format}`);
        if (propDef.description) lines.push(`          description: ${JSON.stringify(propDef.description)}`);
        if (propDef.enum) {
          lines.push(`          enum:`);
          for (const ev of propDef.enum) {
            lines.push(`            - ${ev}`);
          }
        }
        if (propDef.type === 'array' && propDef.items) {
          lines.push(`          items:`);
          if (propDef.items.$ref) {
            lines.push(`            $ref: '${propDef.items.$ref}'`);
          } else if (propDef.items.type === 'object' && propDef.items.properties) {
            lines.push(`            type: object`);
            lines.push(`            properties:`);
            for (const [subName, subDef] of Object.entries(propDef.items.properties)) {
              lines.push(`              ${subName}:`);
              lines.push(`                type: ${subDef.type}`);
              if (subDef.enum) {
                lines.push(`                enum:`);
                for (const ev of subDef.enum) {
                  lines.push(`                  - ${ev}`);
                }
              }
            }
          } else {
            lines.push(`            type: ${propDef.items.type}`);
          }
        }
      }
    }
  }

  const content = lines.join('\n') + '\n';
  fs.writeFileSync(path.join(docsFrontendDir, 'openapi.yaml'), content, 'utf8');
}

// 8. EJECUCIÓN PRINCIPAL Y VERIFICACIÓN ÚNICA
function main() {
  const errores = [];

  try {
    generarEndpointsCSV();
    generarEntidadesJSON();
    generarEntidadesYEndpointsMD();
    generarOpenAPIYaml();

    // 1. Verificación de conteo de endpoints
    const csvContent = fs.readFileSync(path.join(docsFrontendDir, 'endpoints.csv'), 'utf8');
    const csvRows = csvContent.trim().split('\n').slice(1);
    const countCSV = csvRows.length;

    const mdContent = fs.readFileSync(path.join(docsFrontendDir, 'entidades-y-endpoints.md'), 'utf8');
    const mdEndpointsMatches = mdContent.match(/\|\s*`(GET|POST|PUT|PATCH|DELETE)`\s*\|\s*`\/api\/[^\`]+`/g) || [];
    const countMD = mdEndpointsMatches.length;

    const yamlContent = fs.readFileSync(path.join(docsFrontendDir, 'openapi.yaml'), 'utf8');
    const yamlOperations = yamlContent.match(/\s+operationId:\s+[a-zA-Z0-9_]+/g) || [];
    const countYAML = yamlOperations.length;

    if (countCSV !== countMD || countMD !== countYAML) {
      errores.push(`Discrepancia en número de endpoints: CSV=${countCSV}, MD=${countMD}, YAML=${countYAML}`);
    }

    // 2. Verificación de que cada función exportada en frontend/src/api/*.js aparece
    for (const [fnName, fnInfo] of jsdocMap.entries()) {
      const foundInCSV = csvRows.some(row => row.includes(fnName));
      const foundInMD = mdContent.includes(`\`${fnName}\``);
      const foundInYAML = yamlContent.includes(`operationId: ${fnName}`);
      if (!foundInCSV || !foundInMD || !foundInYAML) {
        errores.push(`La función API '${fnName}' de '${fnInfo.file}' no aparece en todos los contratos (CSV:${foundInCSV}, MD:${foundInMD}, YAML:${foundInYAML})`);
      }
    }

    // 3. Verificación con Redocly CLI
    const openapiPath = path.join(docsFrontendDir, 'openapi.yaml');
    try {
      execSync(`npx --yes @redocly/cli lint ${openapiPath}`, {
        stdio: 'pipe',
        encoding: 'utf8'
      });
    } catch (redoclyErr) {
      errores.push(`Error en validación con Redocly CLI: ${redoclyErr.stderr || redoclyErr.stdout || redoclyErr.message}`);
    }

  } catch (err) {
    errores.push(`Excepción general durante la generación: ${err.message}`);
  }

  // REGLA: Imprimí solo OK o la lista de errores
  if (errores.length === 0) {
    console.log('OK');
    process.exit(0);
  } else {
    console.error(errores.join('\n'));
    process.exit(1);
  }
}

main();
