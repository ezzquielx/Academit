import { useState, useEffect } from 'react'
import { Link } from 'react-router-dom'
import { useAuth } from '../../context/AuthContext'
import StatusBadge from '../../components/StatusBadge'
import { getCuotas } from '../../api/cuotas'
import { getMesasExamen } from '../../api/mesasExamen'
import { getCalificaciones } from '../../api/calificaciones'
import { getSolicitudes } from '../../api/solicitudes'
import { getAlumnos } from '../../api/alumnos'

export const InicioPage = () => {
  const { rol, nombre, usuario, user } = useAuth()
  const alumnoId = user?.idAlumno || 1
  const [alumnoCuotas, setAlumnoCuotas] = useState([])
  const [alumnoMesas, setAlumnoMesas] = useState([])
  const [alumnoCalificaciones, setAlumnoCalificaciones] = useState([])
  const [solicitudesCount, setSolicitudesCount] = useState(0)
  const [alumnosCount, setAlumnosCount] = useState(0)

  useEffect(() => {
    getCuotas(alumnoId).then((data) => setAlumnoCuotas(data || [])).catch(() => {})
    getMesasExamen()
      .then((data) => setAlumnoMesas((data || []).filter((m) => m.alumnosInscriptosIds?.includes(alumnoId))))
      .catch(() => {})
    getCalificaciones({ idAlumno: alumnoId }).then((data) => setAlumnoCalificaciones(data || [])).catch(() => {})
    getSolicitudes().then((data) => setSolicitudesCount((data || []).filter((s) => s.estado === 'PENDIENTE').length)).catch(() => {})
    getAlumnos().then((data) => setAlumnosCount((data || []).length)).catch(() => {})
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
    ? (alumnoCalificaciones.reduce((acc, c) => acc + (c.nota || 0), 0) / alumnoCalificaciones.length).toFixed(2).replace('.', ',')
    : '8,50'

  // Materias de ejemplo para el alumno
  const materiasAlumno = [
    { materia: 'Análisis de Sistemas', docente: 'Paz, Marta', p1: 9, p2: 8, asis: 95, cond: 'Promociona', condTipo: 'e-ok' },
    { materia: 'Base de Datos II', docente: 'Aguirre, Hernán', p1: 8, p2: null, asis: 88, cond: 'En curso', condTipo: 'e-pend' },
    { materia: 'Programación III', docente: 'Roldán, Esteban', p1: 7, p2: 6, asis: 91, cond: 'Regular', condTipo: 'e-ok' },
    { materia: 'Inglés Técnico', docente: 'Medina, Carla', p1: 3, p2: null, asis: 72, cond: 'En riesgo', condTipo: 'e-mal' },
  ]

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
              <dt>Legajo</dt>
              <dd className="num">{rol === 'ALUMNO' ? '2024-0318' : (rol === 'DOCENTE' ? 'DOC-104' : 'ADM-012')}</dd>
              <dt>Carrera</dt>
              <dd>Tecnicatura en Programación, plan 2022</dd>
              <dt>Ciclo lectivo</dt>
              <dd className="num">2026, segundo cuatrimestre</dd>
              {rol === 'ALUMNO' && (
                <>
                  <dt>Promedio</dt>
                  <dd className="num">{promedioNotas} sobre {alumnoCalificaciones.length || 4} notas cargadas</dd>
                  <dt>Cuotas</dt>
                  <dd>
                    {cuotasVencidas.length > 0 ? (
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
              )}
            </dl>
          </section>

          {/* Bloque principal según rol */}
          {rol === 'ALUMNO' && (
            <section className="bloque">
              <h2>Materias que cursás</h2>
              <div className="tabla-caja">
                <table>
                  <caption>Nota mínima para aprobar: 4. Asistencia mínima para regularizar: 75 %.</caption>
                  <thead>
                    <tr>
                      <th scope="col">Materia</th>
                      <th scope="col">Docente</th>
                      <th scope="col" className="d">Parcial 1</th>
                      <th scope="col" className="d">Parcial 2</th>
                      <th scope="col" className="d">Asistencia</th>
                      <th scope="col">Condición</th>
                    </tr>
                  </thead>
                  <tbody>
                    {materiasAlumno.map((m) => (
                      <tr key={m.materia}>
                        <td className="titulo-ficha" data-l="Materia">{m.materia}</td>
                        <td data-l="Docente">{m.docente}</td>
                        <td className="d num" data-l="Parcial 1">{m.p1 !== null ? m.p1 : '–'}</td>
                        <td className="d num" data-l="Parcial 2">{m.p2 !== null ? m.p2 : '–'}</td>
                        <td className="d num" data-l="Asistencia">{m.asis} %</td>
                        <td data-l="Condición">
                          <span className={`estado ${m.condTipo}`}>{m.cond}</span>
                        </td>
                      </tr>
                    ))}
                  </tbody>
                </table>
              </div>
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
                <>
                  <li>
                    <div className="dia">
                      <b className="num">24</b>
                      <small>oct</small>
                    </div>
                    <div>
                      <div>Parcial 2 de Base de Datos II</div>
                      <div className="cuando">en 4 días</div>
                      <div className="quien">Aula 4, 19:00</div>
                    </div>
                  </li>
                  <li>
                    <div className="dia">
                      <b className="num">28</b>
                      <small>oct</small>
                    </div>
                    <div>
                      <div>Mesa de examen: Programación II</div>
                      <div className="cuando">en 8 días</div>
                      <div className="quien">Tribunal Roldán, Ibarra · 18:30</div>
                    </div>
                  </li>
                  <li>
                    <div className="dia">
                      <b className="num">4</b>
                      <small>nov</small>
                    </div>
                    <div>
                      <div>Mesa de examen: Base de Datos I</div>
                      <div className="cuando">en 15 días</div>
                      <div className="quien">Tribunal Aguirre, Paz · 18:30</div>
                    </div>
                  </li>
                  <li>
                    <div className="dia">
                      <b className="num">20</b>
                      <small>nov</small>
                    </div>
                    <div>
                      <div>Cierre de inscripción a mesas</div>
                      <div className="cuando">en 31 días</div>
                      <div className="quien">Secretaría académica</div>
                    </div>
                  </li>
                </>
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
