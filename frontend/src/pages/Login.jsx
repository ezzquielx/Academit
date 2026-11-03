import { useState } from 'react'
import { useNavigate, useLocation } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'

export const Login = () => {
  const [username, setUsername] = useState('')
  const [password, setPassword] = useState('')
  const [errorMsg, setErrorMsg] = useState('')
  const [errorErrors, setErrorErrors] = useState([])
  const [loading, setLoading] = useState(false)

  const { login } = useAuth()
  const navigate = useNavigate()
  const location = useLocation()

  const from = location.state?.from?.pathname || '/'

  const handleSubmit = async (e) => {
    e.preventDefault()
    setErrorMsg('')
    setErrorErrors([])

    if (!username.trim() || !password.trim()) {
      setErrorMsg('Ingresá tu usuario y contraseña.')
      return
    }

    try {
      setLoading(true)
      await login(username.trim(), password.trim())
      navigate(from, { replace: true })
    } catch (err) {
      setErrorMsg(err.message || 'Error al iniciar sesión')
      setErrorErrors(err.errors || [])
    } finally {
      setLoading(false)
    }
  }

  const isMock = import.meta.env.VITE_USE_MOCK === 'true'
  const defaultPass = isMock ? '1234' : 'Academit123'

  const handleQuickFill = (user) => {
    setUsername(user)
    setPassword(defaultPass)
    setErrorMsg('')
    setErrorErrors([])
  }

  return (
    <section className="ingreso" aria-label="Ingreso">
      <div className="izq gutter">
        <img
          src="/academit.svg"
          alt="Logo Academit"
          className="escudo"
          width="56"
          height="60"
          style={{ objectFit: 'contain' }}
        />
        <b>Academit</b>
        <p>
          Ingresá con el usuario que te dio la secretaría. Si olvidaste la contraseña, pedí el cambio en administración.
        </p>
        <div className="pie-login">
          <small>
            Este programa fue hecho como proyecto para Taller Integrador III y licenciado con BSD3.
          </small>
        </div>
      </div>

      <div className="der gutter">
        <form onSubmit={handleSubmit} noValidate>
          <h1>Ingresar</h1>

          {errorMsg && (
            <div
              className="error-campo"
              role="alert"
              style={{
                padding: '8px 12px',
                border: '1px solid var(--error)',
                backgroundColor: 'var(--papel)',
              }}
            >
              <strong>{errorMsg}</strong>
              {errorErrors.length > 0 && (
                <ul style={{ margin: '6px 0 0', paddingLeft: '18px' }}>
                  {errorErrors.map((e, idx) => (
                    <li key={idx}>
                      {e.campo && e.campo !== 'general' && <strong>{e.campo}: </strong>}
                      {e.mensaje}
                    </li>
                  ))}
                </ul>
              )}
            </div>
          )}

          <div className="campo">
            <label htmlFor="usuario">Usuario</label>
            <input
              id="usuario"
              type="text"
              autoComplete="username"
              value={username}
              onChange={(e) => setUsername(e.target.value)}
              required
            />
          </div>

          <div className="campo">
            <label htmlFor="clave">Contraseña</label>
            <input
              id="clave"
              type="password"
              autoComplete="current-password"
              value={password}
              onChange={(e) => setPassword(e.target.value)}
              required
            />
          </div>

          <button className="btn" type="submit" disabled={loading} style={{ width: '100%' }}>
            {loading ? 'Cargando…' : 'Ingresar'}
          </button>

          <div style={{ marginTop: '16px', borderTop: '1px solid var(--linea)', paddingTop: '12px' }}>
            <p style={{ fontSize: '13px', color: 'var(--suave)', marginBottom: '8px' }}>
              Usuarios de prueba (clave: <code>{defaultPass}</code>):
            </p>
            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '6px' }}>
              <button
                type="button"
                className="btn lig chico"
                onClick={() => handleQuickFill('admin')}
              >
                admin
              </button>
              <button
                type="button"
                className="btn lig chico"
                onClick={() => handleQuickFill('administrativo')}
              >
                administrativo
              </button>
              <button
                type="button"
                className="btn lig chico"
                onClick={() => handleQuickFill('docente')}
              >
                docente
              </button>
              <button
                type="button"
                className="btn lig chico"
                onClick={() => handleQuickFill('alumno')}
              >
                alumno
              </button>
            </div>
          </div>
        </form>
      </div>
    </section>
  )
}

export default Login
