import { useState, useEffect } from 'react'
import { Link, useNavigate } from 'react-router-dom'
import { useAuth } from '../context/AuthContext'
import { getOfertaCarrerasApi, getOfertaPlanesApi } from '../api/auth'

export const Registro = () => {
  const { registro } = useAuth()
  const navigate = useNavigate()

  const [carreras, setCarreras] = useState([])
  const [planes, setPlanes] = useState([])
  const [loading, setLoading] = useState(false)
  const [cargandoCatalogos, setCargandoCatalogos] = useState(true)

  const [form, setForm] = useState({
    nombre: '',
    apellido: '',
    dni: '',
    email: '',
    telefono: '',
    domicilio: '',
    fechaNacimiento: '',
    idCarrera: '',
    idPlan: '',
    username: '',
    password: '',
    confirmPassword: '',
  })

  const [errorMsg, setErrorMsg] = useState('')
  const [errorErrors, setErrorErrors] = useState([])
  const [registroExitoso, setRegistroExitoso] = useState(false)
  const [mensajeExito, setMensajeExito] = useState('')

  useEffect(() => {
    Promise.all([getOfertaCarrerasApi(), getOfertaPlanesApi()])
      .then(([cars, pls]) => {
        setCarreras(cars || [])
        setPlanes(pls || [])
        if (cars && cars.length > 0) {
          const primerCarrera = cars[0].idCarrera
          const planesDeEsta = (pls || []).filter((p) => p.idCarrera === primerCarrera)
          setForm((prev) => ({
            ...prev,
            idCarrera: primerCarrera,
            idPlan: planesDeEsta.length > 0 ? planesDeEsta[0].idPlan : '',
          }))
        }
      })
      .catch(() => {
        setErrorMsg('Error al cargar la oferta académica para el registro')
      })
      .finally(() => setCargandoCatalogos(false))
  }, [])

  const handleCarreraChange = (idCarreraNum) => {
    const planesFiltrados = planes.filter((p) => p.idCarrera === idCarreraNum)
    setForm((prev) => ({
      ...prev,
      idCarrera: idCarreraNum,
      idPlan: planesFiltrados.length > 0 ? planesFiltrados[0].idPlan : '',
    }))
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setErrorMsg('')
    setErrorErrors([])

    // Validaciones locales
    if (!form.nombre.trim() || !form.apellido.trim()) {
      setErrorMsg('Nombre y apellido son obligatorios.')
      return
    }
    if (!/^[0-9]{7,8}$/.test(form.dni.trim())) {
      setErrorMsg('El DNI debe tener 7 u 8 dígitos numéricos sin puntos ni espacios.')
      return
    }
    if (!form.email.trim() || !form.email.includes('@')) {
      setErrorMsg('Ingresá un correo electrónico válido.')
      return
    }
    if (!form.fechaNacimiento) {
      setErrorMsg('La fecha de nacimiento es obligatoria.')
      return
    }
    if (!form.idCarrera || !form.idPlan) {
      setErrorMsg('Debes seleccionar una carrera y su plan de estudios.')
      return
    }
    if (form.username.trim().length < 3) {
      setErrorMsg('El nombre de usuario debe tener al menos 3 caracteres.')
      return
    }
    if (form.password.length < 6) {
      setErrorMsg('La contraseña debe tener al menos 6 caracteres.')
      return
    }
    if (form.password !== form.confirmPassword) {
      setErrorMsg('Las contraseñas no coinciden.')
      return
    }

    setLoading(true)
    try {
      const resp = await registro({
        nombre: form.nombre.trim(),
        apellido: form.apellido.trim(),
        dni: form.dni.trim(),
        email: form.email.trim(),
        telefono: form.telefono.trim(),
        domicilio: form.domicilio.trim(),
        fechaNacimiento: form.fechaNacimiento,
        idCarrera: Number(form.idCarrera),
        idPlan: Number(form.idPlan),
        username: form.username.trim().toLowerCase(),
        password: form.password,
      })
      setMensajeExito(resp?.message || 'Tendrá que esperar hasta que un administrador valide su cuenta.')
      setRegistroExitoso(true)
    } catch (err) {
      setErrorMsg(err.message || 'Error al completar el registro')
      setErrorErrors(err.errors || [])
    } finally {
      setLoading(false)
    }
  }

  const planesDisponibles = planes.filter((p) => p.idCarrera === Number(form.idCarrera))

  return (
    <section className="ingreso" aria-label="Registro de Estudiante" style={{ minHeight: '100vh', padding: '24px 0' }}>
      <div className="izq gutter" style={{ maxWidth: '420px' }}>
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
          Portal de Autogestión Estudiantil. Registrate para ingresar a tu carrera, consultar correlatividades, inscribirte a cursadas y exámenes finales.
        </p>
        <div className="pie-login">
          <small>
            ¿Ya tenés una cuenta registrada?{' '}
            <Link to="/login" style={{ color: 'var(--primario)', fontWeight: 600 }}>
              Iniciá sesión aquí
            </Link>
          </small>
        </div>
      </div>

      <div className="der gutter" style={{ maxWidth: '640px' }}>
        {registroExitoso ? (
          <div style={{ padding: '24px 8px', textAlign: 'center' }}>
            <div
              style={{
                width: '72px',
                height: '72px',
                margin: '0 auto 20px',
                borderRadius: '50%',
                backgroundColor: 'rgba(217, 119, 6, 0.12)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#d97706',
                fontSize: '36px',
              }}
            >
              <i className="bi bi-clock-history"></i>
            </div>

            <h1 style={{ fontSize: '24px', fontWeight: 700, marginBottom: '12px' }}>
              Registro Recibido
            </h1>

            <div
              style={{
                backgroundColor: '#fffbeb',
                border: '1px solid #fde68a',
                color: '#92400e',
                padding: '16px 20px',
                borderRadius: '8px',
                fontSize: '15px',
                fontWeight: 600,
                marginBottom: '20px',
                lineHeight: 1.5,
              }}
            >
              <i className="bi bi-info-circle-fill me-2"></i>
              {mensajeExito || 'Tendrá que esperar hasta que un administrador valide su cuenta.'}
            </div>

            <p style={{ fontSize: '14px', color: 'var(--suave)', marginBottom: '24px', lineHeight: 1.6 }}>
              Tu solicitud de registro para{' '}
              <strong>
                {carreras.find((c) => c.idCarrera === Number(form.idCarrera))?.nombre || 'la carrera seleccionada'}
              </strong>{' '}
              ha sido enviada correctamente. Una vez que un administrador revise y active tu usuario en el sistema, vas a poder iniciar sesión con tus credenciales.
            </p>

            <div
              style={{
                textAlign: 'left',
                backgroundColor: 'var(--papel, #f8f9fa)',
                border: '1px solid var(--borde, #e9ecef)',
                padding: '16px',
                borderRadius: '8px',
                marginBottom: '28px',
                fontSize: '13.5px',
                lineHeight: 1.8,
              }}
            >
              <div><strong>Estudiante:</strong> {form.nombre} {form.apellido}</div>
              <div><strong>DNI:</strong> {form.dni}</div>
              <div><strong>Usuario asignado:</strong> @{form.username.toLowerCase()}</div>
              <div><strong>Correo electrónico:</strong> {form.email}</div>
            </div>

            <button
              type="button"
              className="btn"
              onClick={() => navigate('/login')}
              style={{ width: '100%', padding: '12px', fontSize: '15px' }}
            >
              Volver al Inicio de Sesión
            </button>
          </div>
        ) : (
          <form onSubmit={handleSubmit} noValidate>
            <h1>Inscripción y Registro de Estudiante</h1>
            <p style={{ fontSize: '13.5px', color: 'var(--suave)', marginBottom: '16px' }}>
              Completá tus datos personales y académicos para crear tu legajo y usuario.
            </p>

          {errorMsg && (
            <div
              className="error-campo"
              role="alert"
              style={{
                padding: '10px 14px',
                border: '1px solid var(--error)',
                backgroundColor: 'var(--papel)',
                marginBottom: '16px',
                borderRadius: '6px',
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

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '12px' }}>
            <div className="campo">
              <label htmlFor="nombre">Nombre <span style={{ color: 'var(--error)' }}>*</span></label>
              <input
                id="nombre"
                type="text"
                value={form.nombre}
                onChange={(e) => setForm({ ...form, nombre: e.target.value })}
                required
                placeholder="Ej. Lucas"
              />
            </div>

            <div className="campo">
              <label htmlFor="apellido">Apellido <span style={{ color: 'var(--error)' }}>*</span></label>
              <input
                id="apellido"
                type="text"
                value={form.apellido}
                onChange={(e) => setForm({ ...form, apellido: e.target.value })}
                required
                placeholder="Ej. Gómez"
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '12px' }}>
            <div className="campo">
              <label htmlFor="dni">DNI (sin puntos) <span style={{ color: 'var(--error)' }}>*</span></label>
              <input
                id="dni"
                type="text"
                maxLength="8"
                value={form.dni}
                onChange={(e) => setForm({ ...form, dni: e.target.value.replace(/\D/g, '') })}
                required
                placeholder="Ej. 43111222"
              />
            </div>

            <div className="campo">
              <label htmlFor="email">Correo Electrónico <span style={{ color: 'var(--error)' }}>*</span></label>
              <input
                id="email"
                type="email"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                required
                placeholder="ejemplo@correo.com"
              />
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '12px' }}>
            <div className="campo">
              <label htmlFor="telefono">Teléfono / Celular</label>
              <input
                id="telefono"
                type="tel"
                value={form.telefono}
                onChange={(e) => setForm({ ...form, telefono: e.target.value })}
                placeholder="Ej. 11-4567-8900"
              />
            </div>

            <div className="campo">
              <label htmlFor="fechaNacimiento">Fecha de Nacimiento <span style={{ color: 'var(--error)' }}>*</span></label>
              <input
                id="fechaNacimiento"
                type="date"
                value={form.fechaNacimiento}
                onChange={(e) => setForm({ ...form, fechaNacimiento: e.target.value })}
                required
              />
            </div>
          </div>

          <div className="campo">
            <label htmlFor="domicilio">Domicilio</label>
            <input
              id="domicilio"
              type="text"
              value={form.domicilio}
              onChange={(e) => setForm({ ...form, domicilio: e.target.value })}
              placeholder="Ej. Av. San Martín 1234, CABA"
            />
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '12px', marginTop: '6px' }}>
            <div className="campo">
              <label htmlFor="carrera">Carrera Universitaria <span style={{ color: 'var(--error)' }}>*</span></label>
              <select
                id="carrera"
                value={form.idCarrera}
                disabled={cargandoCatalogos}
                onChange={(e) => handleCarreraChange(Number(e.target.value))}
                style={{
                  width: '100%',
                  padding: '10px',
                  backgroundColor: 'var(--papel)',
                  color: 'var(--texto)',
                  border: '1px solid var(--linea)',
                  borderRadius: '6px',
                }}
              >
                {carreras.map((c) => (
                  <option key={c.idCarrera} value={c.idCarrera}>
                    {c.nombre} ({c.codigo})
                  </option>
                ))}
              </select>
            </div>

            <div className="campo">
              <label htmlFor="plan">Plan de Estudios <span style={{ color: 'var(--error)' }}>*</span></label>
              <select
                id="plan"
                value={form.idPlan}
                disabled={cargandoCatalogos || planesDisponibles.length === 0}
                onChange={(e) => setForm({ ...form, idPlan: Number(e.target.value) })}
                style={{
                  width: '100%',
                  padding: '10px',
                  backgroundColor: 'var(--papel)',
                  color: 'var(--texto)',
                  border: '1px solid var(--linea)',
                  borderRadius: '6px',
                }}
              >
                {planesDisponibles.map((p) => (
                  <option key={p.idPlan} value={p.idPlan}>
                    {p.nombre} ({p.codigo})
                  </option>
                ))}
              </select>
            </div>
          </div>

          <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(240px, 1fr))', gap: '12px', marginTop: '10px' }}>
            <div className="campo">
              <label htmlFor="regUsername">Nombre de Usuario <span style={{ color: 'var(--error)' }}>*</span></label>
              <input
                id="regUsername"
                type="text"
                autoComplete="username"
                value={form.username}
                onChange={(e) => setForm({ ...form, username: e.target.value })}
                required
                placeholder="Ej. lucas.gomez"
              />
            </div>

            <div className="campo">
              <label htmlFor="regPassword">Contraseña (mínimo 6 car.) <span style={{ color: 'var(--error)' }}>*</span></label>
              <input
                id="regPassword"
                type="password"
                autoComplete="new-password"
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
                required
              />
            </div>
          </div>

          <div className="campo" style={{ marginBottom: '20px' }}>
            <label htmlFor="confirmPassword">Confirmar Contraseña <span style={{ color: 'var(--error)' }}>*</span></label>
            <input
              id="confirmPassword"
              type="password"
              autoComplete="new-password"
              value={form.confirmPassword}
              onChange={(e) => setForm({ ...form, confirmPassword: e.target.value })}
              required
            />
          </div>

          <button className="btn" type="submit" disabled={loading} style={{ width: '100%', padding: '12px', fontSize: '15px' }}>
            {loading ? 'Enviando solicitud…' : 'Completar Registro'}
          </button>

          <div style={{ marginTop: '16px', textAlign: 'center' }}>
            <span style={{ fontSize: '13px', color: 'var(--suave)' }}>
              ¿Ya estás registrado?{' '}
              <Link to="/login" style={{ color: 'var(--primario)', fontWeight: 600 }}>
                Iniciá sesión aquí
              </Link>
            </span>
          </div>
        </form>
        )}
      </div>
    </section>
  )
}

export default Registro

