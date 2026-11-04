import { useState, useEffect } from 'react'
import PageHeader from '../../components/PageHeader'
import StatusBadge from '../../components/StatusBadge'
import { useToast } from '../../context/ToastContext'
import { getAlumnos } from '../../api/alumnos'
import { getMaterias } from '../../api/materias'
import { getCursos } from '../../api/cursos'
import { getEvaluaciones } from '../../api/evaluaciones'
import { getCalificaciones, guardarCalificaciones } from '../../api/calificaciones'
import { getAsignaciones } from '../../api/asignaciones'
import { useAuth } from '../../context/AuthContext'
import { ETIQUETAS_VACIO } from '../../config/etiquetas'

export const CalificacionesDocentePage = () => {
  const { showToast } = useToast()
  const { idDocente } = useAuth()
  const [asignaciones, setAsignaciones] = useState([])
  const [alumnos, setAlumnos] = useState([])
  const [materias, setMaterias] = useState([])
  const [cursos, setCursos] = useState([])
  const [evaluaciones, setEvaluaciones] = useState([])
  const [selectedMateria, setSelectedMateria] = useState(1)
  const [selectedCurso, setSelectedCurso] = useState(1)
  const [selectedEvaluacion, setSelectedEvaluacion] = useState(1)
  const [evaluacionNombre, setEvaluacionNombre] = useState('Primer Parcial')
  const [tipo, setTipo] = useState('PARCIAL')

  const [notas, setNotas] = useState({}) // { [idAlumno]: { nota, observacion } }
  const [erroresPorAlumno, setErroresPorAlumno] = useState({})
  const [loading, setLoading] = useState(false)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    getMaterias().then(setMaterias).catch(() => {})
    getCursos().then(setCursos).catch(() => {})
    getAlumnos().then(setAlumnos).catch(() => {})
    getEvaluaciones().then(setEvaluaciones).catch(() => {})
    // Solo las cátedras asignadas al docente logueado.
    getAsignaciones()
      .then((data) => {
        const propias = idDocente ? data.filter((a) => a.idDocente === idDocente) : []
        setAsignaciones(propias)
        if (propias[0]) {
          setSelectedMateria(propias[0].idMateria)
          setSelectedCurso(propias[0].idCurso)
        }
      })
      .catch(() => {})
  }, [idDocente])

  // Al cambiar materia o curso, sincronizar evaluación activa
  useEffect(() => {
    const disponibles = evaluaciones.filter(
      (e) => e.idMateria === Number(selectedMateria) && e.idCurso === Number(selectedCurso)
    )
    if (disponibles.length > 0) {
      setSelectedEvaluacion(disponibles[0].idEvaluacion)
      setEvaluacionNombre(disponibles[0].descripcion)
      setTipo(disponibles[0].tipo)
    }
  }, [selectedMateria, selectedCurso, evaluaciones])

  useEffect(() => {
    setLoading(true)
    getCalificaciones({ idMateria: selectedMateria, tipo, idEvaluacion: selectedEvaluacion })
      .then((cals) => {
        const mapa = {}
        cals.forEach((c) => {
          if (c.evaluacionNombre === evaluacionNombre || c.idEvaluacion === Number(selectedEvaluacion)) {
            mapa[c.idAlumno] = {
              nota: c.nota,
              observacion: c.observacion || '',
            }
          }
        })
        setNotas(mapa)
      })
      .finally(() => setLoading(false))
  }, [selectedMateria, selectedCurso, selectedEvaluacion, evaluacionNombre, tipo])

  const handleNotaChange = (idAlumno, valor) => {
    const num = valor === '' ? '' : valor
    setNotas((prev) => ({
      ...prev,
      [idAlumno]: {
        ...prev[idAlumno],
        nota: num,
      },
    }))
    if (erroresPorAlumno[idAlumno]) {
      setErroresPorAlumno((prev) => {
        const copia = { ...prev }
        delete copia[idAlumno]
        return copia
      })
    }
  }

  const handleObsChange = (idAlumno, valor) => {
    setNotas((prev) => ({
      ...prev,
      [idAlumno]: {
        ...prev[idAlumno],
        observacion: valor,
      },
    }))
  }

  const handleGuardarTodas = async () => {
    setSaving(true)
    setErroresPorAlumno({})
    try {
      const lista = Object.entries(notas)
        .filter(([, item]) => item.nota !== '' && item.nota !== undefined && item.nota !== null)
        .map(([idAlu, item]) => ({
          idAlumno: Number(idAlu),
          nota: item.nota === '' ? '' : Number(item.nota),
          observacion: item.observacion || '',
        }))

      if (lista.length === 0) {
        showToast('No hay calificaciones ingresadas para guardar', 'warning')
        setSaving(false)
        return
      }

      await guardarCalificaciones(selectedEvaluacion, lista)
      showToast('Calificaciones guardadas exitosamente', 'success')
      setErroresPorAlumno({})
    } catch (err) {
      if (err.errores) {
        setErroresPorAlumno(err.errores)
      }
      showToast(err.message || 'Error al guardar calificaciones', 'danger')
    } finally {
      setSaving(false)
    }
  }

  const alumnosDelCurso = alumnos.filter((a) => a.idCurso === Number(selectedCurso))
  const materiasDisponibles = materias.filter((m) =>
    asignaciones.some((a) => a.idMateria === m.idMateria)
  )
  const cursosDisponibles = cursos.filter((c) =>
    asignaciones.some((a) => a.idCurso === c.idCurso)
  )
  const puedeEditar = asignaciones.some(
    (a) => a.idMateria === Number(selectedMateria) && a.idCurso === Number(selectedCurso)
  )

  return (
    <div>
      <PageHeader
        title="Carga de Calificaciones"
        subtitle="Grilla de alumnos con validación de notas (escala numérica oficial de 1 a 10)"
        actions={
          <button
            type="button"
            className="btn"
            disabled={saving || loading || alumnosDelCurso.length === 0 || !puedeEditar}
            onClick={handleGuardarTodas}
          >
            {saving ? 'Guardando…' : 'Guardar Calificaciones'}
          </button>
        }
      />

      {!puedeEditar && (
        <div
          role="alert"
          className="small mb-3"
          style={{
            padding: '10px 14px',
            border: '1px solid var(--linea)',
            backgroundColor: 'var(--papel)',
            color: 'var(--suave)',
            borderRadius: '3px',
          }}
        >
          <i className="bi bi-info-circle me-2" />
          {ETIQUETAS_VACIO.SIN_PERMISO_EDICION}
        </div>
      )}

      {/* Selectores de contexto */}
      <div
        className="bloque"
        style={{
          backgroundColor: 'var(--papel)',
          border: '1px solid var(--linea)',
          borderRadius: '3px',
          padding: '16px',
        }}
      >
        <div className="filtros" style={{ margin: 0 }}>
          <div className="campo" style={{ flex: '1 1 180px' }}>
            <label htmlFor="selMateria">Materia</label>
            <select
              id="selMateria"
              className="c"
              value={selectedMateria}
              onChange={(e) => setSelectedMateria(Number(e.target.value))}
            >
              {materiasDisponibles.map((m) => (
                <option key={m.idMateria} value={m.idMateria}>
                  {m.nombre}
                </option>
              ))}
            </select>
          </div>

          <div className="campo" style={{ flex: '1 1 180px' }}>
            <label htmlFor="selCurso">Curso / Comisión</label>
            <select
              id="selCurso"
              className="c"
              value={selectedCurso}
              onChange={(e) => setSelectedCurso(Number(e.target.value))}
            >
              {cursosDisponibles.map((c) => (
                <option key={c.idCurso} value={c.idCurso}>
                  {c.nombre}
                </option>
              ))}
            </select>
          </div>

          <div className="campo" style={{ flex: '1 1 220px' }}>
            <label htmlFor="selEvaluacion">Evaluación Planificada</label>
            <select
              id="selEvaluacion"
              className="c"
              value={selectedEvaluacion}
              onChange={(e) => {
                const id = Number(e.target.value)
                setSelectedEvaluacion(id)
                const ev = evaluaciones.find((item) => item.idEvaluacion === id)
                if (ev) {
                  setEvaluacionNombre(ev.descripcion)
                  setTipo(ev.tipo)
                }
              }}
            >
              {evaluaciones.map((ev) => (
                <option key={ev.idEvaluacion} value={ev.idEvaluacion}>
                  #{ev.idEvaluacion} - {ev.tipo}: {ev.descripcion}
                </option>
              ))}
            </select>
          </div>

          <div className="campo" style={{ flex: '1 1 180px' }}>
            <label htmlFor="selInstancia">Instancia / Detalle</label>
            <input
              id="selInstancia"
              type="text"
              className="c"
              value={evaluacionNombre}
              onChange={(e) => setEvaluacionNombre(e.target.value)}
              placeholder="Ej. Primer Parcial"
            />
          </div>
        </div>
      </div>

      {/* Grilla de carga de notas */}
      <div className="tabla-caja">
        <table>
          <caption>Escala 1 a 10 · Aprobación con 4 o más puntos</caption>
          <thead>
            <tr>
              <th scope="col">Legajo</th>
              <th scope="col">Estudiante</th>
              <th scope="col">DNI</th>
              <th scope="col" className="d" style={{ width: '130px' }}>Nota (1-10)</th>
              <th scope="col">Condición</th>
              <th scope="col">Observación</th>
            </tr>
          </thead>
          <tbody>
            {alumnosDelCurso.length === 0 ? (
              <tr>
                <td colSpan={6} style={{ textAlign: 'center', padding: '24px', color: 'var(--suave)' }}>
                  No hay estudiantes registrados en este curso.
                </td>
              </tr>
            ) : (
              alumnosDelCurso.map((alu) => {
                const valorNota = notas[alu.idAlumno]?.nota ?? ''
                const valorObs = notas[alu.idAlumno]?.observacion ?? ''
                const errorItem = erroresPorAlumno[alu.idAlumno]
                const condicion = valorNota === '' ? null : Number(valorNota) >= 4 ? 'APROBADO' : 'DESAPROBADO'

                return (
                  <tr key={alu.idAlumno}>
                    <td className="titulo-ficha font-monospace num" data-l="Legajo">{alu.legajo}</td>
                    <td data-l="Estudiante">
                      <strong>
                        {alu.apellido}, {alu.nombre}
                      </strong>
                    </td>
                    <td className="num" data-l="DNI">{alu.dni}</td>
                    <td className="d" data-l="Nota (1-10)">
                      <input
                        type="number"
                        min="1"
                        max="10"
                        step="0.5"
                        className="c num"
                        style={{
                          width: '80px',
                          textAlign: 'right',
                          borderColor: errorItem ? 'var(--error)' : 'var(--linea-fuerte)',
                        }}
                        value={valorNota}
                        onChange={(e) => handleNotaChange(alu.idAlumno, e.target.value)}
                        placeholder="–"
                        aria-label={`Nota de ${alu.nombre} ${alu.apellido}`}
                      />
                      {errorItem && (
                        <div className="error-campo" style={{ fontSize: '12px' }}>
                          {errorItem}
                        </div>
                      )}
                    </td>
                    <td data-l="Condición">
                      {condicion ? (
                        <StatusBadge status={condicion} />
                      ) : (
                        <span style={{ color: 'var(--suave)' }}>Sin calificar</span>
                      )}
                    </td>
                    <td data-l="Observación">
                      <input
                        type="text"
                        className="c"
                        style={{ width: '100%', minWidth: '160px' }}
                        value={valorObs}
                        onChange={(e) => handleObsChange(alu.idAlumno, e.target.value)}
                        placeholder="Comentario opcional..."
                        aria-label={`Observación de ${alu.nombre} ${alu.apellido}`}
                      />
                    </td>
                  </tr>
                )
              })
            )}
          </tbody>
        </table>
      </div>
    </div>
  )
}

export default CalificacionesDocentePage
