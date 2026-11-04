import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import StatusBadge from '../../components/StatusBadge'
import EmptyState from '../../components/EmptyState'
import { getCuotas } from '../../api/cuotas'
import { getMesasExamen } from '../../api/mesasExamen'
import { getCalificaciones } from '../../api/calificaciones'
import { getSolicitudes } from '../../api/solicitudes'
import { getAlumnos, getAlumnoById } from '../../api/alumnos'
import { ETIQUETAS_VACIO } from '../../config/etiquetas'

export const InicioPage = () => {
  const { rol, nombre, usuario, idAlumno } = useAuth()
  const alumnoId = rol === 'ALUMNO' ? idAlumno : null
  const [alumno, setAlumno] = useState(null)
  const [alumnoCuotas, setAlumnoCuotas] = useState([])
  const [alumnoMesas, setAlumnoMesas] = useState([])
  const [alumnoCalificaciones, setAlumnoCalificaciones] = useState([])
  const [solicitudesCount, setSolicitudesCount] = useState(0)
  const [alumnosCount, setAlumnosCount] = useState(0)

  useEffect(() => {
    getAlumnos().then((data) => setAlumnosCount((data || []).length)).catch(() => {})
    getMesasExamen()
      .then((data) =>
        setAlumnoMesas(
          alumnoId ? (data || []).filter((m) => m.alumnosInscriptosIds?.includes(alumnoId)) : []
        )
      )
      .catch(() => {})
    getSolicitudes(alumnoId ? { idAlumno: alumnoId } : {})
      .then((data) => setSolicitudesCount((data || []).filter((s) => s.estado === 'PENDIENTE').length))
      .catch(() => {})
    // Sin idAlumno no se consulta nada del padrón: nunca mostrar datos de otro alumno.
    if (!alumnoId) return
    getAlumnoById(alumnoId).then(setAlumno).catch(() => {})
    getCuotas(alumnoId).then((data) => setAlumnoCuotas(data || [])).catch(() => {})
    getCalificaciones({ idAlumno: alumnoId }).then((data) => setAlumnoCalificaciones(data || [])).catch(() => {})
  }, [rol, alumnoId])

  const getGreeting = () => {
    const h = new Date().getHours()
    if (h < 6) return 'Buenas noches'
    if (h < 13) return 'Buenos días'
    if (h < 20) return 'Buenas tardes'
    return 'Buenas noches'
  }

  const getHoyFormatted = () => {
    const f = new Date().toLocaleDateString('es-AR', {
      weekday: 'long',
      day: 'numeric',
      month: 'long',
      year: 'numeric',
    })
    return f.charAt(0).toUpperCase() + f.slice(1) + '.'
  }

  const DIA_MS = 86400000
  const getCuando = (dateStr) => {
    if (!dateStr) return ''
    const d = new Date(dateStr)
    d.setHours(0, 0, 0, 0)
    const hoy = new Date()
    hoy.setHours(0, 0, 0, 0)
    const diff = Math.round((d.getTime() - hoy.getTime()) / DIA_MS)
    if (diff === 0) return 'hoy'
    if (diff === 1) return 'mañana'
    if (diff === -1) return 'ayer'
    if (diff < 0) return `hace ${-diff} días`
    return `en ${diff} días`
  }

  const cuotasVencidas = alumnoCuotas.filter((c) => c.estado === 'VENCIDA')
  const promedioNotas = alumnoCalificaciones.length > 0
    ? (alumnoCalificaciones.reduce((acc, c) => acc + (Number(c.nota) || 0), 0) / alumnoCalificaciones.length)
        .toFixed(2)
        .replace('.', ',')
    : null

  // Materias armadas con las calificaciones reales del alumno logueado.
  const materiasAlumno = Object.values(
    alumnoCalificaciones.reduce((acc, c) => {
      const clave = c.idMateria ?? c.materiaNombre
      if (!acc[clave]) {
        acc[clave] = { clave, materia: c.materiaNombre || '—', notas: [], evaluaciones: 0 }
      }
      acc[clave].notas.push(Number(c.nota) || 0)
      acc[clave].evaluaciones += 1
      return acc
    }, {})
  ).map((m) => {
    const promedio = m.notas.reduce((a, b) => a + b, 0) / m.notas.length
    return {
      ...m,
      promedio: promedio.toFixed(1).replace('.', ','),
      cond: promedio >= 7 ? 'Promociona' : promedio >= 4 ? 'Regular' : 'En riesgo',
      condTipo: promedio >= 7 ? 'e-ok' : promedio >= 4 ? 'e-pend' : 'e-mal',
    }
  })

  return (
    <div>
      <h1>{getGreeting()}, {nombre || usuario}</h1>
      <p className="fecha">{getHoyFormatted()}</p>

      <div className="cols">
        <div>
          {/* Ficha "Tu situación" */}
          <section className="bloque">
            <h2>Tu situación</h2>
            <dl className="ficha">
              {rol === 'ALUMNO' ? (
                <>
                  <dt>Legajo</dt>
                  <dd className="num">{alumno?.legajo || '—'}</dd>
                  <dt>Carrera</dt>
                  <dd>
                    {alumno?.carrera || '—'}
                    {alumno?.planNombre ? `, ${alumno.planNombre}` : ''}
                  </dd>
                  <dt>Comisión</dt>
                  <dd>{alumno?.cursoNombre || ETIQUETAS_VACIO.SIN_COMISION}</dd>
                  <dt>Matrícula</dt>
                  <dd>
                    <StatusBadge status={alumno?.estadoMatricula || 'PENDIENTE'} />
                  </dd>
                  <dt>Ciclo lectivo</dt>
                  <dd className="num">{alumno?.cicloLectivo || '—'}</dd>
                  <dt>Promedio</dt>
                  <dd className="num">
                    {promedioNotas
                      ? `${promedioNotas} sobre ${alumnoCalificaciones.length} notas cargadas`
                      : ETIQUETAS_VACIO.SIN_NOTAS}
                  </dd>
                  <dt>Cuotas</dt>
                  <dd>
                    {alumnoCuotas.length === 0 ? (
                      <span className="estado e-pend">{ETIQUETAS_VACIO.SIN_CUOTAS}</span>
                    ) : cuotasVencidas.length > 0 ? (
                      <>
                        <span className="estado e-mal">
                          {cuotasVencidas.length} {cuotasVencidas.length === 1 ? 'vencida' : 'vencidas'}
                        </span>
                        {' · '}
                        <Link to="/academico/situacion" style={{ color: 'var(--primario)' }}>
                          ver detalle
                        </Link>
                      </>
                    ) : (
                      <span className="estado e-ok">Al día</span>
                    )}
                  </dd>
                </>
              ) : (
                <>
                  <dt>Usuario</dt>
                  <dd className="num">{usuario || '—'}</dd>
                  <dt>Rol</dt>
                  <dd>{rol || '—'}</dd>
                  <dt>Ciclo lectivo</dt>
                  <dd className="num">2026, segundo cuatrimestre</dd>
                </>
              )}
            </dl>
          </section>

          {/* Bloque principal según rol */}
          {rol === 'ALUMNO' && (
            <section className="bloque">
              <h2>Materias que cursás</h2>
              {materiasAlumno.length === 0 ? (
                <EmptyState
                  title={ETIQUETAS_VACIO.SIN_MATERIAS}
                  description={ETIQUETAS_VACIO.SIN_MATERIAS_DETALLE}
                />
              ) : (
                <div className="tabla-caja">
                  <table>
                    <caption>Nota mínima para aprobar: 4. Asistencia mínima para regularizar: 75 %.</caption>
                    <thead>
                      <tr>
                        <th scope="col">Materia</th>
                        <th scope="col" className="d">Evaluaciones</th>
                        <th scope="col" className="d">Promedio</th>
                        <th scope="col">Condición</th>
                      </tr>
                    </thead>
                    <tbody>
                      {materiasAlumno.map((m) => (
                        <tr key={m.clave}>
                          <td className="titulo-ficha" data-l="Materia">{m.materia}</td>
                          <td className="d num" data-l="Evaluaciones">{m.evaluaciones}</td>
                          <td className="d num" data-l="Promedio">{m.promedio}</td>
                          <td data-l="Condición">
                            <span className={`estado ${m.condTipo}`}>{m.cond}</span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              )}
            </section>
          )}

          {rol === 'DOCENTE' && (
            <section className="bloque">
              <h2>Mis comisiones activas</h2>
              <div className="tabla-caja">
                <table>
                  <caption>Ciclo lectivo 2026 · Cursado del segundo cuatrimestre</caption>
                  <thead>
                    <tr>
                      <th scope="col">Materia</th>
                      <th scope="col">Comisión</th>
                      <th scope="col" className="d">Alumnos</th>
                      <th scope="col">Acción</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td className="titulo-ficha" data-l="Materia">Programación I</td>
                      <td data-l="Comisión">1° Año - Turno Mañana (Aula 101)</td>
                      <td className="d num" data-l="Alumnos">29</td>
                      <td data-l="Acción">
                        <Link to="/docencia/calificaciones" className="btn lig chico">
                          Calificar
                        </Link>
                      </td>
                    </tr>
                    <tr>
                      <td className="titulo-ficha" data-l="Materia">Programación II</td>
                      <td data-l="Comisión">2° Año - Turno Tarde (Lab 2)</td>
                      <td className="d num" data-l="Alumnos">28</td>
                      <td data-l="Acción">
                        <Link to="/docencia/calificaciones" className="btn lig chico">
                          Calificar
                        </Link>
                      </td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </section>
          )}

          {(rol === 'ADMINISTRATIVO' || rol === 'ADMINISTRADOR') && (
            <section className="bloque">
              <h2>Ocupación de comisiones</h2>
              <div className="tabla-caja">
                <table>
                  <caption>Ciclo lectivo 2026 · {alumnosCount} alumnos matriculados · {solicitudesCount} solicitudes pendientes</caption>
                  <thead>
                    <tr>
                      <th scope="col">Comisión</th>
                      <th scope="col">Turno y Aula</th>
                      <th scope="col" className="d">Inscriptos</th>
                      <th scope="col" className="d">Cupo</th>
                      <th scope="col">Estado</th>
                    </tr>
                  </thead>
                  <tbody>
                    <tr>
                      <td className="titulo-ficha" data-l="Comisión">1° Año - Programación</td>
                      <td data-l="Turno y Aula">Mañana (Aula 101)</td>
                      <td className="d num" data-l="Inscriptos">29</td>
                      <td className="d num" data-l="Cupo">35</td>
                      <td data-l="Estado"><StatusBadge status="ACTIVO" /></td>
                    </tr>
                    <tr>
                      <td className="titulo-ficha" data-l="Comisión">2° Año - Programación</td>
                      <td data-l="Turno y Aula">Tarde (Lab 2)</td>
                      <td className="d num" data-l="Inscriptos">28</td>
                      <td className="d num" data-l="Cupo">30</td>
                      <td data-l="Estado"><StatusBadge status="ACTIVO" /></td>
                    </tr>
                    <tr>
                      <td className="titulo-ficha" data-l="Comisión">1° Año - Sistemas</td>
                      <td data-l="Turno y Aula">Noche (Aula Magna)</td>
                      <td className="d num" data-l="Inscriptos">40</td>
                      <td className="d num" data-l="Cupo">40</td>
                      <td data-l="Estado"><StatusBadge status="CERRADO" /></td>
                    </tr>
                  </tbody>
                </table>
              </div>
            </section>
          )}
        </div>

        {/* Columna lateral de página (lat) */}
        <aside className="lat">
          <section>
            <h2>Agenda</h2>
            <ul className="agenda">
              {alumnoMesas.length > 0 ? (
                alumnoMesas.slice(0, 4).map((m) => {
                  const d = new Date(m.fecha)
                  const diaNum = isNaN(d.getDate()) ? '28' : d.getDate()
                  const mesStr = isNaN(d.getMonth())
                    ? 'oct'
                    : d.toLocaleDateString('es-AR', { month: 'short' }).replace('.', '')

                  return (
                    <li key={m.idMesa}>
                      <div className="dia">
                        <b className="num">{diaNum}</b>
                        <small>{mesStr}</small>
                      </div>
                      <div>
                        <div>{m.materiaNombre}</div>
                        <div className="cuando">{getCuando(m.fecha)}</div>
                        <div className="quien">
                          Tribunal: {m.presidente || 'Roldán, Ibarra'} · {m.hora || '18:30'}
                        </div>
                      </div>
                    </li>
                  )
                })
              ) : (
                <li>
                  <EmptyState
                    title={ETIQUETAS_VACIO.SIN_MESAS}
                    description={ETIQUETAS_VACIO.SIN_MESAS_DETALLE}
                  />
                </li>
              )}
            </ul>
          </section>

          <section>
            <h2>Avisos de secretaría</h2>
            <ul className="avisos">
              <li>La inscripción a mesas de examen cierra 48 horas antes de cada fecha.</li>
              <li>Quien tenga cuotas vencidas debe regularizar su situación en administración antes de rendir.</li>
              <li>La declaración jurada de materias del próximo ciclo se presenta hasta fin de noviembre.</li>
            </ul>
          </section>
        </aside>
      </div>
    </div>
  )
}

export default InicioPage
