import apiClient, { isMock } from './client'
import { mockDb } from './mocks/data'

/**
 * Obtiene las materias de un plan de estudio estructuradas con año y correlativas.
 * REST: GET /api/planes-estudio/{id}/materias
 * @param {number|string} idPlan - Identificador del plan de estudio
 * @returns {Promise<Array<{ idMateria: number, nombre: string, anio: number, correlativas: number[] }>>}
 */
export const getMateriasDePlan = async (idPlan) => {
  if (isMock) {
    await new Promise((resolve) => setTimeout(resolve, 150))
    const mats = mockDb.materias.filter((m) => m.idPlan === Number(idPlan))
    return mats.map((m) => ({
      idMateria: m.idMateria,
      nombre: m.nombre,
      anio: m.anio,
      codigo: m.codigo,
      regimen: m.regimen,
      horasSemanales: m.horasSemanales,
      correlativas: (m.correlativas || []).map((c) => {
        if (typeof c === 'number') return c
        const encontrada = mockDb.materias.find((other) => other.nombre === c || other.codigo === c)
        return encontrada ? encontrada.idMateria : c
      }),
    }))
  }
  const response = await apiClient.get(`/planes-estudio/${idPlan}/materias`)
  return response.data
}

/**
 * Obtiene todos los planes de estudio del instituto.
 * REST: GET /api/planes-estudio
 * @returns {Promise<Array<Object>>}
 */
export const getPlanesEstudio = async () => {
  if (isMock) {
    await new Promise((resolve) => setTimeout(resolve, 150))
    return [...mockDb.planesEstudio]
  }
  const response = await apiClient.get('/planes-estudio')
  return response.data
}

export const getPlanes = getPlanesEstudio
