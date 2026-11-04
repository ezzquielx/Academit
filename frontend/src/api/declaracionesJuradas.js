import apiClient, { isMock } from './client'
import { mockDb } from './mocks/data'
import { createApiError } from './errors'

/**
 * Obtiene el listado de declaraciones juradas de materias a cursar.
 * REST: GET /api/declaraciones-juradas
 * @param {Object} [filtros={}] - Filtros opcionales (idAlumno, idCiclo)
 * @returns {Promise<Array<Object>>}
 */
export const getDeclaracionesJuradas = async (filtros = {}) => {
  if (isMock) {
    await new Promise((resolve) => setTimeout(resolve, 150))
    let lista = [...(mockDb.declaracionesJuradas || [])]
    if (filtros.idAlumno) {
      lista = lista.filter((d) => d.idAlumno === Number(filtros.idAlumno))
    }
    if (filtros.idCiclo) {
      lista = lista.filter((d) => d.idCiclo === Number(filtros.idCiclo))
    }
    return lista
  }
  const response = await apiClient.get('/declaraciones-juradas', { params: filtros })
  return response.data
}

/**
 * Presenta una nueva declaración jurada de materias (RF51).
 * REST: POST /api/declaraciones-juradas
 * @param {Object} data - Datos ({ idAlumno, idCiclo, materiasDeclaradasIds, observaciones })
 * @returns {Promise<Object>}
 */
export const createDeclaracionJurada = async (data) => {
  if (isMock) {
    await new Promise((resolve) => setTimeout(resolve, 200))
    if (!Array.isArray(data.materiasDeclaradasIds) || data.materiasDeclaradasIds.length === 0) {
      throw createApiError(400, 'Validación fallida', [
        {
          campo: 'materiasDeclaradasIds',
          mensaje: 'Debe seleccionar al menos una materia para la declaración jurada',
        },
      ])
    }
    if (!data.idAlumno) {
      throw createApiError(400, 'Validación fallida', [
        { campo: 'idAlumno', mensaje: 'La declaración jurada debe indicar el alumno' },
      ])
    }
    const list = mockDb.declaracionesJuradas || []
    const nuevoId = Math.max(...list.map((d) => d.idDeclaracion), 0) + 1
    const idAlumno = Number(data.idAlumno)
    const alumno = mockDb.alumnos?.find((a) => a.idAlumno === idAlumno)
    if (!alumno) {
      throw createApiError(404, 'Alumno no encontrado', [
        { campo: 'idAlumno', mensaje: 'Alumno no encontrado' },
      ])
    }
    const ciclo = mockDb.ciclosLectivos?.find((c) => c.idCiclo === Number(data.idCiclo))

    const materiasIds = Array.isArray(data.materiasDeclaradasIds) ? data.materiasDeclaradasIds : []
    const materiasNombres = materiasIds
      .map((id) => mockDb.materias?.find((m) => m.idMateria === Number(id))?.nombre)
      .filter(Boolean)

    const nueva = {
      idDeclaracion: nuevoId,
      idAlumno,
      alumnoNombre: `${alumno.nombre} ${alumno.apellido}`,
      idCiclo: ciclo?.idCiclo ?? null,
      cicloNombre: ciclo?.nombre || '',
      fecha: new Date().toISOString().split('T')[0],
      materiasDeclaradasIds: materiasIds,
      materiasNombres,
      estado: 'PENDIENTE',
      observaciones: data.observaciones || data.motivo || '',
    }
    list.unshift(nueva)
    mockDb.declaracionesJuradas = list
    return { ...nueva }
  }
  const response = await apiClient.post('/declaraciones-juradas', data)
  return response.data
}
