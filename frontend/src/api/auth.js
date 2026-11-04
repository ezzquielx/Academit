import apiClient, { isMock } from './client'
import { mockDb, guardarAltasMock } from './mocks/data'
import { createApiError } from './errors'

/**
 * Inicia sesión con usuario y contraseña.
 * REST: POST /api/auth/login
 * @param {string} username - Nombre de usuario
 * @param {string} password - Contraseña
 * @returns {Promise<Object>} { usuario: { idUsuario, username, nombre, rol, email, idAlumno, idDocente }, token }
 */
export const loginApi = async (username, password) => {
  if (isMock) {
    await new Promise((resolve) => setTimeout(resolve, 200))

    const encontrado = mockDb.usuarios.find(
      (u) => u.username.toLowerCase() === username.trim().toLowerCase()
    )

    if (!encontrado) {
      throw createApiError(404, 'Usuario no encontrado', [
        {
          campo: 'username',
          mensaje: 'Usuario no encontrado. Usuarios válidos: admin, administrativo, docente, alumno',
        },
      ])
    }

    if (password !== (encontrado.password || '1234')) {
      throw createApiError(401, 'Credenciales inválidas', [
        { campo: 'password', mensaje: 'Contraseña incorrecta. (La contraseña de prueba es 1234)' },
      ])
    }

    return {
      usuario: {
        idUsuario: encontrado.idUsuario,
        username: encontrado.username,
        nombre: encontrado.nombre,
        rol: encontrado.rol,
        email: encontrado.email,
        // Vínculo con el padrón de alumnos/docentes. El backend todavía no lo
        // expone en UsuarioSesionDTO: ver docs/pendientes.md.
        idAlumno: encontrado.idAlumno ?? null,
        idDocente: encontrado.idDocente ?? null,
      },
      token: 'mock-token-jwt',
    }
  }

  const response = await apiClient.post('/auth/login', { username, password })
  return response.data
}

/**
 * Cierra la sesión activa en el sistema.
 * REST: POST /api/auth/logout
 * @returns {Promise<Object>}
 */
export const logoutApi = async () => {
  if (isMock) {
    await new Promise((resolve) => setTimeout(resolve, 100))
    return { success: true }
  }

  const response = await apiClient.post('/auth/logout')
  return response.data
}

/**
 * Registra a un nuevo alumno en el sistema.
 * El alumno nace sin relaciones: sin curso, matrícula, cuotas, notas ni asistencias.
 * REST: POST /api/auth/registro
 * @param {Object} data - Datos personales, carrera, plan, username y password
 * @returns {Promise<Object>} Datos del alumno autenticado y token
 */
export const registroApi = async (data) => {
  if (isMock) {
    await new Promise((resolve) => setTimeout(resolve, 300))

    const username = String(data.username || '').trim().toLowerCase()
    const dni = String(data.dni || '').trim()

    if (mockDb.usuarios.some((u) => u.username.toLowerCase() === username)) {
      throw createApiError(409, 'Nombre de usuario no disponible', [
        { campo: 'username', mensaje: 'Ese nombre de usuario ya está registrado' },
      ])
    }
    if (mockDb.alumnos.some((a) => a.dni === dni)) {
      throw createApiError(409, 'DNI ya registrado', [
        { campo: 'dni', mensaje: 'Ya existe un alumno con ese DNI' },
      ])
    }

    const idAlumno = Math.max(...mockDb.alumnos.map((a) => a.idAlumno), 0) + 1
    const carrera = mockDb.carreras.find((c) => c.idCarrera === Number(data.idCarrera))
    const plan = mockDb.planesEstudio.find((p) => p.idPlan === Number(data.idPlan))

    // Alta sin datos precargados: solo lo que cargó el aspirante.
    const nuevoAlumno = {
      idAlumno,
      legajo: `ALU-${1000 + idAlumno}`,
      nombre: data.nombre,
      apellido: data.apellido,
      dni,
      email: data.email,
      telefono: data.telefono || '',
      fechaNacimiento: data.fechaNacimiento,
      domicilio: data.domicilio || '',
      carrera: carrera?.nombre || '',
      idCarrera: carrera?.idCarrera ?? null,
      idPlan: plan?.idPlan ?? null,
      planNombre: plan?.nombre || '',
      idCurso: null,
      cursoNombre: null,
      idMatricula: null,
      estadoMatricula: 'PENDIENTE',
      cicloLectivo: new Date().getFullYear(),
      estado: 'PENDIENTE',
      origen: 'registro',
    }
    mockDb.alumnos.push(nuevoAlumno)

    const idUsuario = Math.max(...mockDb.usuarios.map((u) => u.idUsuario), 0) + 1
    const nombreCompleto = `${data.nombre} ${data.apellido}`.trim()
    const nuevoUsuario = {
      idUsuario,
      username,
      nombre: nombreCompleto,
      email: data.email,
      rol: 'ALUMNO',
      estado: 'INACTIVO',
      ultimoAcceso: '-',
      password: data.password,
      idAlumno,
      origen: 'registro',
    }
    mockDb.usuarios.push(nuevoUsuario)
    guardarAltasMock()

    return {
      usuario: {
        idUsuario,
        username,
        nombre: nombreCompleto,
        rol: 'ALUMNO',
        email: data.email,
        idAlumno,
        idDocente: null,
      },
      token: 'mock-token-jwt',
    }
  }

  const response = await apiClient.post('/auth/registro', data)
  return response.data
}

/**
 * Obtiene las carreras disponibles para aspirantes y nuevos estudiantes (público).
 * REST: GET /api/auth/carreras
 * @returns {Promise<Array<Object>>}
 */
export const getOfertaCarrerasApi = async () => {
  if (isMock) {
    return [...mockDb.carreras]
  }
  const response = await apiClient.get('/auth/carreras')
  return response.data
}

/**
 * Obtiene los planes de estudio disponibles para aspirantes y nuevos estudiantes (público).
 * REST: GET /api/auth/planes-estudio
 * @returns {Promise<Array<Object>>}
 */
export const getOfertaPlanesApi = async () => {
  if (isMock) {
    return [...mockDb.planesEstudio]
  }
  const response = await apiClient.get('/auth/planes-estudio')
  return response.data
}

/**
 * Modifica la contraseña del usuario en sesión.
 * REST: POST /api/auth/cambiar-password
 * @param {string} actual - Contraseña vigente
 * @param {string} nueva - Nueva contraseña deseada
 * @returns {Promise<Object>}
 */
export const cambiarPasswordApi = async (actual, nueva) => {
  if (isMock) {
    await new Promise((resolve) => setTimeout(resolve, 200))
    if (actual !== '1234') {
      throw createApiError(400, 'Error al cambiar contraseña', [
        { campo: 'actual', mensaje: 'La contraseña actual es incorrecta' },
      ])
    }
    return { success: true, message: 'Contraseña actualizada con éxito' }
  }

  const response = await apiClient.post('/auth/cambiar-password', { actual, nueva })
  return response.data
}
