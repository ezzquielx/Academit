import { Navigate, useLocation, Link } from 'react-router-dom'
import { Container, Alert } from 'react-bootstrap'
import { useAuth } from '../context/AuthContext'
import { ModoProvider } from '../context/ModoContext'

/**
 * Componente para proteger rutas según estado de autenticación y rol.
 * - Si no hay sesión: redirige al login.
 * - Si el rol no está permitido: muestra mensaje de "Acceso denegado".
 * - Si tiene permiso: renderiza el contenido (children).
 */
export const ProtectedRoute = ({ children, allowedRoles, item }) => {
  const { isAuthenticated, rol } = useAuth()
  const location = useLocation()

  // 1. Redirección si no está autenticado
  if (!isAuthenticated) {
    return <Navigate to="/login" state={{ from: location }} replace />
  }

  // 2. Control de rol
  if (allowedRoles && !allowedRoles.includes(rol)) {
    return (
      <Container className="my-5">
        <Alert variant="danger" className="p-4 ">
          <Alert.Heading className="h4">Acceso denegado</Alert.Heading>
          <p className="mb-3">
            No poseés los permisos necesarios para acceder a esta sección con el rol de{' '}
            <strong>{rol}</strong>.
          </p>
          <hr />
          <p className="mb-0">
            Podés regresar al{' '}
            <Link to="/" className="alert-link">
              panel principal
            </Link>{' '}
            o consultar con el administrador del sistema.
          </p>
        </Alert>
      </Container>
    )
  }

  // 3. Permiso concedido - proveer modo contextual si está configurado
  const modo = (item?.modoPorRol && rol && item.modoPorRol[rol]) || item?.modo || 'edicion'
  return <ModoProvider value={modo}>{children}</ModoProvider>
}

export default ProtectedRoute
