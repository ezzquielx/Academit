/* eslint-disable react-refresh/only-export-components */
import { createContext, useContext, useState } from 'react'
import { loginApi, logoutApi, registroApi } from '../api/auth'

// 
// AISLAMIENTO DE SESIÓN:
// Centraliza cómo se guarda, recupera y destruye la sesión del usuario.
// Cuando se defina la arquitectura definitiva en el backend:
// - Si es JWT: se guarda/lee el token acá (ej. sessionStorage o localStorage).
// - Si es cookie de sesión (HttpOnly): el navegador la gestiona automáticamente,
//   y acá solo se consulta un endpoint tipo /api/auth/me o se guarda el perfil.
//
const SESSION_STORAGE_KEY = 'instituto_sesion'

const getStoredSession = () => {
  try {
    const raw = sessionStorage.getItem(SESSION_STORAGE_KEY)
    return raw ? JSON.parse(raw) : null
  } catch {
    return null
  }
}

const saveStoredSession = (userData) => {
  try {
    sessionStorage.setItem(SESSION_STORAGE_KEY, JSON.stringify(userData))
  } catch (error) {
    console.error('Error al persistir la sesión:', error)
  }
}

const clearStoredSession = () => {
  try {
    sessionStorage.removeItem(SESSION_STORAGE_KEY)
  } catch (error) {
    console.error('Error al eliminar la sesión:', error)
  }
}

// 
// CONTEXTO Y PROVIDER
// 
const AuthContext = createContext(null)

export const AuthProvider = ({ children }) => {
  // Inicializa la sesión desde el almacenamiento aislado
  const [session, setSession] = useState(() => getStoredSession())

  const login = async (username, password) => {
    const data = await loginApi(username, password)
    // data contiene { usuario, nombre, rol }
    setSession(data)
    saveStoredSession(data)
    return data
  }

  const registro = async (registroData) => {
    return await registroApi(registroData)
  }

  const logout = async () => {
    try {
      await logoutApi()
    } finally {
      setSession(null)
      clearStoredSession()
    }
  }

  const sessionUser =
    session?.usuario && typeof session.usuario === 'object' ? session.usuario : session

  const value = {
    user: session,
    token: session?.token || null,
    idUsuario: sessionUser?.idUsuario || null,
    usuario: sessionUser?.username || (typeof session?.usuario === 'string' ? session.usuario : null),
    rol: sessionUser?.rol || session?.rol || null,
    nombre: sessionUser?.nombre || session?.nombre || null,
    email: sessionUser?.email || session?.email || null,
    isAuthenticated: Boolean(session),
    login,
    registro,
    logout,
  }

  return <AuthContext.Provider value={value}>{children}</AuthContext.Provider>
}

// Hook personalizado para consumir el contexto de autenticación
export const useAuth = () => {
  const context = useContext(AuthContext)
  if (!context) {
    throw new Error('useAuth debe ser utilizado dentro de un AuthProvider')
  }
  return context
}

export default AuthContext
