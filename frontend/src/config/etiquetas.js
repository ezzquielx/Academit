/**
 * Diccionario centralizado de etiquetas visibles para valores de enums en Academit.
 * Regla: Los datos en backend/mocks son códigos sin tildes ni espacios (ej: MANANA, CONSTANCIA, DNI).
 * Ningún texto visible de pantalla debe usarse como valor de dato.
 */

export const ETIQUETAS_TURNO = {
  MANANA: 'Mañana',
  TARDE: 'Tarde',
  NOCHE: 'Noche',
}

export const ETIQUETAS_TIPO_SOLICITUD = {
  CONSTANCIA: 'Constancia de Alumno Regular',
  PASANTIA: 'Pasantía Educativa',
  TITULO_INTERMEDIO: 'Solicitud de Título Intermedio',
  ACADEMICA: 'Solicitud Académica',
  CAMBIO_CARRERA: 'Cambio de Carrera',
  REINCORPORACION: 'Reincorporación a Carrera',
}

export const ETIQUETAS_TIPO_DOCUMENTACION = {
  DNI: 'DNI (Frente y Dorso)',
  TITULO_SECUNDARIO: 'Título Secundario',
  APTO_MEDICO: 'Certificado de Aptitud Médica',
  FOTOS: 'Fotos 4x4',
}

export const ETIQUETAS_ESTADO_ALUMNO = {
  ACTIVO: 'Activo',
  INACTIVO: 'Inactivo',
}

export const ETIQUETAS_ESTADO_DOCENTE = {
  ACTIVO: 'Activo',
  INACTIVO: 'Inactivo',
}

/**
 * Textos de estados vacíos y avisos de sesión.
 * Se centralizan acá para que ninguna pantalla escriba texto visible suelto.
 */
export const ETIQUETAS_VACIO = {
  SIN_REGISTROS: 'Sin registros',
  SIN_REGISTROS_DETALLE: 'No hay información disponible para mostrar.',
  SIN_MATERIAS: 'Todavía no estás inscripto a ninguna materia',
  SIN_MATERIAS_DETALLE: 'Cuando los administrativos validen tu legajo vas a ver acá tus materias y calificaciones.',
  SIN_NOTAS: 'Sin notas cargadas',
  SIN_CUOTAS: 'Sin cuotas registradas',
  SIN_COMISION: 'Sin comisión asignada',
  SIN_MESAS: 'Sin exámenes programados',
  SIN_MESAS_DETALLE: 'No tenés mesas de examen próximas ni inscripciones pendientes.',
  SIN_ASISTENCIAS: 'Todavía no hay asistencias registradas',
  SIN_ASISTENCIAS_DETALLE: 'Cuando el docente tome lista vas a ver acá el detalle.',
  SIN_DECLARACIONES: 'Todavía no presentaste declaraciones juradas',
  SIN_DECLARACIONES_DETALLE: 'Completá el formulario para declarar las materias que vas a cursar.',
  SIN_SOLICITUDES: 'Todavía no generaste solicitudes',
  SIN_SOLICITUDES_DETALLE: 'Las solicitudes que presentes van a aparecer en esta lista.',
  SIN_DOCUMENTACION: 'Todavía no hay documentación registrada',
  SIN_DOCUMENTACION_DETALLE: 'Cuando presentes tu legajo vas a ver acá el estado de cada documento.',
  SIN_MATRICULA: 'Todavía no tenés matrícula asignada',
  SIN_MATRICULA_DETALLE: 'Los administrativos van a generar tu matrícula al validar tus datos.',
  SIN_LEGAJO: 'No pudimos identificar tu legajo',
  SIN_LEGAJO_DETALLE: 'Cerrá sesión y volvé a ingresar. Si el problema sigue, consultá con administración.',
  REGISTRO_AVISO: 'Te avisaremos cuando los administrativos validen tus datos.',
  SIN_PERMISO_EDICION: 'Solo el docente asignado puede registrar notas y asistencias.',
}

export const ETIQUETAS = {
  TURNO: ETIQUETAS_TURNO,
  TIPO_SOLICITUD: ETIQUETAS_TIPO_SOLICITUD,
  TIPO_DOCUMENTACION: ETIQUETAS_TIPO_DOCUMENTACION,
  ESTADO_ALUMNO: ETIQUETAS_ESTADO_ALUMNO,
  ESTADO_DOCENTE: ETIQUETAS_ESTADO_DOCENTE,
  VACIO: ETIQUETAS_VACIO,
}

export default ETIQUETAS
