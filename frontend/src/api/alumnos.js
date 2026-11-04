import apiClient, { isMock } from './client'
import { mockDb, guardarAltasMock } from './mocks/data'
import { createApiError } from './errors'

/**
 * Obtiene el listado completo de alumnos.
 * REST: GET /api/alumnos
 * @returns {Promise<Array<Object>>}
 */
export const getAlumnos = async () => {
  if (isMock) {
    await new Promise((resolve) => setTimeout(resolve, 150))
    return [...mockDb.alumnos]
  }

  const response = await apiClient.get('/alumnos')
  return response.data
}

/**
 * Obtiene un alumno por su ID.
 * REST: GET /api/alumnos/{id}
 * @param {number|string} idAlumno - Identificador del alumno
 * @returns {Promise<Object>}
 */
export const getAlumnoById = async (idAlumno) => {
  if (isMock) {
    await new Promise((resolve) => setTimeout(resolve, 100))
    const found = mockDb.alumnos.find((a) => a.idAlumno === Number(idAlumno))
    if (!found) {
      throw createApiError(404, 'Alumno no encontrado', [
        { campo: 'idAlumno', mensaje: 'Alumno no encontrado' },
      ])
    }
    return { ...found }
  }

  const response = await apiClient.get(`/alumnos/${idAlumno}`)
  return response.data
}

/**
 * Crea un nuevo alumno en el sistema.
 * REST: POST /api/alumnos
 * @param {Object} alumnoData - Datos del nuevo alumno
 * @returns {Promise<Object>}
 */
export const createAlumno = async (alumnoData) => {
  if (isMock) {
    await new Promise((resolve) => setTimeout(resolve, 200))
    const nuevoId = Math.max(...mockDb.alumnos.map((a) => a.idAlumno), 0) + 1
    const nuevo = {
      idAlumno: nuevoId,
      legajo: alumnoData.legajo || `ALU-${1000 + nuevoId}`,
      nombre: alumnoData.nombre,
      apellido: alumnoData.apellido,
      dni: alumnoData.dni,
      email: alumnoData.email,
      telefono: alumnoData.telefono || 'Sin teléfono',
      fechaNacimiento: alumnoData.fechaNacimiento || '2000-01-01',
      domicilio: alumnoData.domicilio || 'No especificado',
      carrera: alumnoData.carrera || '',
      idCarrera: alumnoData.idCarrera ?? null,
      idPlan: alumnoData.idPlan ?? null,
      planNombre: alumnoData.planNombre || '',
      // Un alta nace sin relaciones: sin curso ni matrícula asignados.
      idCurso: alumnoData.idCurso ?? null,
      cursoNombre: alumnoData.cursoNombre || null,
      idMatricula: alumnoData.idMatricula ?? null,
      estadoMatricula: alumnoData.estadoMatricula || 'PENDIENTE',
      cicloLectivo: alumnoData.cicloLectivo || new Date().getFullYear(),
      estado: alumnoData.estado || 'ACTIVO',
      origen: 'registro',
    }
    mockDb.alumnos.unshift(nuevo)
    guardarAltasMock()
    return { ...nuevo }
  }

  const response = await apiClient.post('/alumnos', alumnoData)
  return response.data
}

/**
 * Actualiza los datos de un alumno existente.
 * REST: PUT /api/alumnos/{id}
 * @param {number|string} idAlumno - Identificador del alumno
 * @param {Object} alumnoData - Campos actualizados
 * @returns {Promise<Object>}
 */
export const updateAlumno = async (idAlumno, alumnoData) => {
  if (isMock) {
    await new Promise((resolve) => setTimeout(resolve, 200))
    const index = mockDb.alumnos.findIndex((a) => a.idAlumno === Number(idAlumno))
    if (index === -1) {
      throw createApiError(404, 'Alumno no encontrado', [
        { campo: 'idAlumno', mensaje: 'Alumno no encontrado' },
      ])
    }

    mockDb.alumnos[index] = {
      ...mockDb.alumnos[index],
      ...alumnoData,
    }
    return { ...mockDb.alumnos[index] }
  }

  const response = await apiClient.put(`/alumnos/${idAlumno}`, alumnoData)
  return response.data
}

/**
 * Realiza la baja lógica de un alumno.
 * REST: DELETE /api/alumnos/{id}
 * @param {number|string} idAlumno - Identificador del alumno
 * @returns {Promise<Object>}
 */
export const deleteAlumno = async (idAlumno) => {
  if (isMock) {
    await new Promise((resolve) => setTimeout(resolve, 150))
    const index = mockDb.alumnos.findIndex((a) => a.idAlumno === Number(idAlumno))
    if (index === -1) {
      throw createApiError(404, 'Alumno no encontrado', [
        { campo: 'idAlumno', mensaje: 'Alumno no encontrado' },
      ])
    }

    mockDb.alumnos[index].estado = 'INACTIVO'
    return { success: true }
  }

  const response = await apiClient.delete(`/alumnos/${idAlumno}`)
  return response.data
}

/**
 * Obtiene la situación académica consolidada del alumno (materias cursadas, regularizadas y notas finales).
 * REST: GET /api/alumnos/{id}/situacion-academica
 * @param {number|string} idAlumno
 * @returns {Promise<Object>}
 */
export const getSituacionAcademica = async (idAlumno) => {
  if (isMock) {
    await new Promise((resolve) => setTimeout(resolve, 150))
    const alumno = mockDb.alumnos.find((a) => a.idAlumno === Number(idAlumno))
    const calificaciones = mockDb.calificaciones.filter((c) => c.idAlumno === Number(idAlumno))
    return {
      alumno,
      calificaciones,
      materiasAprobadas: calificaciones.filter((c) => c.nota >= 4).length,
    }
  }

  const response = await apiClient.get(`/alumnos/${idAlumno}/situacion-academica`)
  return response.data
}

/**
 * Obtiene la situación arancelaria y administrativa del alumno (matrícula, cuotas, deudas y pagos).
 * REST: GET /api/alumnos/{id}/situacion-administrativa
 * @param {number|string} idAlumno
 * @returns {Promise<Object>}
 */
export const getSituacionAdministrativa = async (idAlumno) => {
  if (isMock) {
    await new Promise((resolve) => setTimeout(resolve, 150))
    const alumno = mockDb.alumnos.find((a) => a.idAlumno === Number(idAlumno))
    const cuotas = mockDb.cuotas.filter((c) => c.idAlumno === Number(idAlumno))
    const pagos = mockDb.pagos.filter((p) => p.idAlumno === Number(idAlumno))
    const matricula = mockDb.matriculas.find((m) => m.idAlumno === Number(idAlumno))
    const deuda = cuotas
      .filter((c) => c.estado === 'PENDIENTE' || c.estado === 'VENCIDA')
      .reduce((acc, c) => acc + c.importe, 0)
    const totalAbonado = pagos.reduce((acc, p) => acc + p.importe, 0)

    return {
      alumno,
      matricula,
      cuotas,
      pagos,
      deudaTotal: deuda,
      totalAbonado,
    }
  }

  const response = await apiClient.get(`/alumnos/${idAlumno}/situacion-administrativa`)
  return response.data
}
