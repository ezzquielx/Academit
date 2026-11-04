import { useState, useEffect, useRef } from 'react'
import { Link, NavLink, useLocation, useNavigate } from 'react-router-dom'
import { Alert } from 'react-bootstrap'
import { useAuth } from '../context/AuthContext'
import { MENU_GRUPOS } from '../config/menu'
import { ETIQUETAS_VACIO } from '../config/etiquetas'

export const Layout = ({ children }) => {
  const { isAuthenticated, rol, nombre, usuario, idAlumno, logout } = useAuth()
  const [showMockAlert, setShowMockAlert] = useState(true)
  const [menuAbierto, setMenuAbierto] = useState(false)
  const abrirBtnRef = useRef(null)
  const cerrarBtnRef = useRef(null)

  const location = useLocation()
  const navigate = useNavigate()

  const isMock = import.meta.env.VITE_USE_MOCK === 'true'

  // Filtrar grupos y opciones según el rol del usuario activo
  const gruposVisibles = MENU_GRUPOS.map((grupo) => {
    const itemsPermitidos = grupo.items.filter((item) =>
      rol ? item.roles.includes(rol) : false
    )
    return {
      ...grupo,
      items: itemsPermitidos,
    }
  }).filter((grupo) => grupo.items.length > 0)

  // Determinar grupo y pantalla activa para miga de pan
  let grupoActivo = null
  let itemActivo = null
  for (const g of gruposVisibles) {
    const it = g.items.find((item) => item.path === location.pathname)
    if (it) {
      grupoActivo = g
      itemActivo = it
      break
    }
  }

  const abrirMenu = () => {
    setMenuAbierto(true)
    document.body.classList.add('bloqueado')
    setTimeout(() => {
      cerrarBtnRef.current?.focus()
    }, 0)
  }

  const cerrarMenu = () => {
    setMenuAbierto(false)
    document.body.classList.remove('bloqueado')
    abrirBtnRef.current?.focus()
  }

  // Cerrar con Escape
  useEffect(() => {
    const handleKeyDown = (e) => {
      if (e.key === 'Escape' && menuAbierto) {
        cerrarMenu()
      }
    }
    window.addEventListener('keydown', handleKeyDown)
    return () => window.removeEventListener('keydown', handleKeyDown)
  }, [menuAbierto])

  // Cerrar panel automáticamente en >= 901px
  useEffect(() => {
    const mql = window.matchMedia('(min-width: 901px)')
    const handleChange = (e) => {
      if (e.matches) {
        setMenuAbierto(false)
        document.body.classList.remove('bloqueado')
      }
    }
    mql.addEventListener('change', handleChange)
    return () => mql.removeEventListener('change', handleChange)
  }, [])

  // Limpieza al desmontar
  useEffect(() => {
    return () => {
      document.body.classList.remove('bloqueado')
    }
  }, [])

  // Si no está autenticado o está en login/registro, renderizar children directamente
  if (!isAuthenticated || location.pathname === '/login' || location.pathname === '/registro') {
    return <main>{children}</main>
  }

  const handleLogout = async () => {
    await logout()
    navigate('/login')
  }

  const formatRol = (r) => {
    if (!r) return ''
    if (r === 'ALUMNO') return 'Alumno'
    if (r === 'DOCENTE') return 'Docente'
    if (r === 'ADMINISTRATIVO') return 'Administrativo'
    if (r === 'ADMINISTRADOR') return 'Administrador'
    return r.charAt(0) + r.slice(1).toLowerCase()
  }

  return (
    <div className="app">
      {/* Velo oscuro para mobile */}
      {menuAbierto && (
        <div
          className="velo"
          id="velo"
          onClick={cerrarMenu}
          aria-hidden="true"
        />
      )}

      {/* 1. Barra lateral fija de 276px (o panel responsive <= 900px) */}
      <aside
        className={`lateral ${menuAbierto ? 'abierto' : ''}`.trim()}
        id="lateral"
        aria-label="Navegación"
      >
        <div className="marca">
          <Link
            to="/"
            style={{ display: 'flex', alignItems: 'center', gap: '12px', textDecoration: 'none', color: 'inherit' }}
            onClick={cerrarMenu}
          >
            <img
              src="/academit.svg"
              alt="Logo Academit"
              className="escudo"
              width="40"
              height="43"
              style={{ objectFit: 'contain', flexShrink: 0 }}
            />
            <div>
              <b>Academit</b>
              <small>Gestión académica — Taller Integrador III</small>
            </div>
          </Link>

          <button
            className="cerrar"
            type="button"
            id="cerrar"
            ref={cerrarBtnRef}
            onClick={cerrarMenu}
            aria-label="Cerrar menú"
          >
            ✕
          </button>
        </div>

        {/* Navegación por grupos */}
        <nav className="nav nav-lateral" id="nav" aria-label="Principal">
          {gruposVisibles.map((grupo) => (
            <div key={grupo.id} className="grupo">
              <div className="titulo">{grupo.titulo}</div>
              <ul>
                {grupo.items.map((item) => (
                  <li key={item.path}>
                    <NavLink
                      to={item.path}
                      end={item.path === '/'}
                      className={({ isActive }) => `item ${isActive ? 'activo' : ''}`.trim()}
                      onClick={cerrarMenu}
                    >
                      {item.label}
                    </NavLink>
                  </li>
                ))}
              </ul>
            </div>
          ))}
        </nav>

        {/* Pie de barra lateral fijo con usuario y botón Salir */}
        <div className="pie">
          <div>
            <b id="u-nombre">{nombre || usuario}</b>
            <small id="u-rol">{formatRol(rol)}</small>
          </div>
          <button
            className="salir"
            type="button"
            onClick={handleLogout}
            aria-label="Cerrar sesión"
          >
            Salir
          </button>
        </div>
      </aside>

      {/* 2. Área principal */}
      <div className="principal">
        {/* Franja superior con botón Menú (mobile), miga de pan y usuario */}
        <div className="superior gutter">
          <button
            className="abrir"
            type="button"
            id="abrir"
            ref={abrirBtnRef}
            aria-controls="lateral"
            aria-expanded={menuAbierto}
            onClick={abrirMenu}
          >
            <svg width="18" height="14" viewBox="0 0 18 14" aria-hidden="true">
              <path d="M0 1h18M0 7h18M0 13h18" stroke="currentColor" strokeWidth="2" />
            </svg>
            Menú
          </button>

          <div className="miga" id="miga">
            {grupoActivo ? grupoActivo.titulo : 'Inicio'} / <b>{itemActivo ? itemActivo.label : 'Inicio'}</b>
          </div>

          <div className="quien">
            <b id="q-nombre">{nombre || usuario}</b> · <span id="q-rol">{formatRol(rol)}</span>
          </div>
        </div>

        {/* Alerta de maqueta si VITE_USE_MOCK=true */}
        {isMock && showMockAlert && (
          <div className="gutter pt-3">
            <div className="pagina" style={{ paddingBlock: 0 }}>
              <Alert
                variant="warning"
                dismissible
                transition={false}
                onClose={() => setShowMockAlert(false)}
                className="py-2 px-3 small d-flex justify-content-between align-items-center mb-0"
                style={{ backgroundColor: 'var(--papel)', borderColor: 'var(--acento)', color: 'var(--texto)' }}
              >
                <div>
                  <strong>Vista de maqueta:</strong> Operaciones simuladas en memoria local.
                </div>
              </Alert>
            </div>
          </div>
        )}

        {/* Sin vínculo al padrón de alumnos no se consultan datos de nadie */}
        {rol === 'ALUMNO' && !idAlumno && (
          <div className="gutter pt-3">
            <Alert variant="danger" className="py-2 px-3 small mb-0">
              <strong>{ETIQUETAS_VACIO.SIN_LEGAJO}.</strong> {ETIQUETAS_VACIO.SIN_LEGAJO_DETALLE}
            </Alert>
          </div>
        )}

        {/* Contenido principal de la página */}
        <main className="pagina gutter">
          {children}
        </main>
      </div>
    </div>
  )
}

export default Layout
