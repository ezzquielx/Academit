import { Routes, Route, Navigate } from 'react-router-dom'
import { MENU_GRUPOS } from './config/menu'
import ProtectedRoute from './components/ProtectedRoute'
import Login from './pages/Login'
import Registro from './pages/Registro'
import NotFound from './pages/NotFound'

/**
 * GENERADOR DINÁMICO DE RUTAS
 * Se nutre directamente de src/config/menu.js.
 * Garantiza que toda pantalla del menú tenga ruta protegida y viceversa.
 */
export const AppRoutes = () => {
  // Aplanar todos los ítems de todos los grupos del menú
  const todasLasRutas = MENU_GRUPOS.flatMap((grupo) => grupo.items)

  return (
    <Routes>
      {/* 1. Rutas públicas de acceso y registro */}
      <Route path="/login" element={<Login />} />
      <Route path="/registro" element={<Registro />} />
      <Route path="/inicio" element={<Navigate to="/" replace />} />
      <Route path="/usuarios" element={<Navigate to="/sistema/usuarios" replace />} />

      {/* 2. Rutas generadas automáticamente desde MENU_GRUPOS */}
      {todasLasRutas.map((item) => {
        const Component = item.component
        return (
          <Route
            key={item.path}
            path={item.path}
            element={
              <ProtectedRoute allowedRoles={item.roles} item={item}>
                <Component />
              </ProtectedRoute>
            }
          />
        )
      })}

      {/* 3. Ruta comodín para recursos inexistentes */}
      <Route path="*" element={<NotFound />} />
    </Routes>
  )
}

export default AppRoutes
