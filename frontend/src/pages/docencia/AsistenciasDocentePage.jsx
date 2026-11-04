import { useState, useEffect } from 'react'
import PageHeader from '../../components/PageHeader'
import StatusBadge from '../../components/StatusBadge'
import { useToast } from '../../context/ToastContext'
import { getAlumnos } from '../../api/alumnos'
import { getCursos } from '../../api/cursos'
import { getMaterias } from '../../api/materias'
import { getAsistencias, registrarAsistencias } from '../../api/asistencias'
import { getAsignaciones } from '../../api/asignaciones'
import { useAuth } from '../../context/AuthContext'
import { ETIQUETAS_VACIO } from '../../config/etiquetas'

export const AsistenciasDocentePage = () => {
  const { showToast } = useToast()
  const { idDocente } = useAuth()
  const [asignaciones, setAsignaciones] = useState([])
  const [alumnos, setAlumnos] = useState([])
  const [cursos, setCursos] = useState([])
  const [materias, setMaterias] = useState([])
  const [selectedCurso, setSelectedCurso] = useState(1)
  const [selectedMateria, setSelectedMateria] = useState(1)
  const [selectedFecha, setSelectedFecha] = useState(new Date().toISOString().split('T')[0])

  const [estados, setEstados] = useState({}) // { [idAlumno]: 'PRESENTE' | 'AUSENTE' | 'TARDANZA' | 'JUSTIFICADO' }
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    getCursos().then(setCursos).catch(() => {})
    getMaterias().then(setMaterias).catch(() => {})
    getAlumnos().then(setAlumnos).catch(() => {})
    // Solo las cátedras asignadas al docente logueado.
    getAsignaciones()
      .then((data) => {
        const propias = idDocente ? data.filter((a) => a.idDocente === idDocente) : []
        setAsignaciones(propias)
        if (propias[0]) {
          setSelectedCurso(propias[0].idCurso)
          setSelectedMateria(propias[0].idMateria)
        }
      })
      .catch(() => {})
  }, [idDocente])

  useEffect(() => {
    getAsistencias({ idCurso: selectedCurso, fecha: selectedFecha }).then((asists) => {
      const mapa = {}
      asists.forEach((a) => {
        mapa[a.idAlumno] = a.estado
      })
      setEstados(mapa)
    })
  }, [selectedCurso, selectedFecha])

  const handleSetEstado = (idAlumno, estado) => {
    setEstados((prev) => ({
      ...prev,
      [idAlumno]: estado,
    }))
  }

  const handleMarcarTodos = (estado) => {
    const mapa = { ...estados }
    alumnosDelCurso.forEach((a) => {
      mapa[a.idAlumno] = estado
    })
    setEstados(mapa)
  }

  const handleGuardar = async () => {
    setSaving(true)
    try {
      const lista = alumnosDelCurso.map((a) => ({
        idAlumno: a.idAlumno,
        alumnoNombre: `${a.nombre} ${a.apellido}`,
        idCurso: selectedCurso,
        idMateria: selectedMateria,
        fecha: selectedFecha,
        estado: estados[a.idAlumno] || 'PRESENTE',
      }))

      await registrarAsistencias(lista)
      showToast(`Asistencia de ${lista.length} estudiantes guardada exitosamente`, 'success')
    } catch (err) {
      showToast(err.message || 'Error al guardar asistencias', 'danger')
    } finally {
      setSaving(false)
    }
  }

  const alumnosDelCurso = alumnos.filter((a) => a.idCurso === Number(selectedCurso))
  const cursosDisponibles = cursos.filter((c) =>
    asignaciones.some((a) => a.idCurso === c.idCurso)
  )
  const materiasDisponibles = materias.filter((m) =>
    asignaciones.some((a) => a.idMateria === m.idMateria)
  )
  const puedeEditar = asignaciones.some(
    (a) => a.idCurso === Number(selectedCurso) && a.idMateria === Number(selectedMateria)
  )

  return (
    <div>
      <PageHeader
        title="Tomar Asistencia"
        subtitle="Control diario de presentismo por curso y fecha (PRESENTE, AUSENTE, TARDANZA, JUSTIFICADO)"
        actions={
          <button
            type="button"
            className="btn"
            disabled={saving || alumnosDelCurso.length === 0 || !puedeEditar}
            onClick={handleGuardar}
          >
            {saving ? 'Guardando…' : 'Guardar Presentismo'}
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

      {/* Selectores de curso, materia y fecha */}
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
          <div className="campo" style={{ flex: '1 1 200px' }}>
            <label htmlFor="asistCurso">Curso / Comisión</label>
            <select
              id="asistCurso"
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

          <div className="campo" style={{ flex: '1 1 200px' }}>
            <label htmlFor="asistMateria">Materia</label>
            <select
              id="asistMateria"
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
            <label htmlFor="asistFecha">Fecha de Clase</label>
            <input
              id="asistFecha"
              type="date"
              className="c"
              value={selectedFecha}
              onChange={(e) => setSelectedFecha(e.target.value)}
            />
          </div>
        </div>

        <div
          style={{
            marginTop: '14px',
            paddingTop: '12px',
            borderTop: '1px solid var(--linea)',
            display: 'flex',
            alignItems: 'center',
            gap: '8px',
            flexWrap: 'wrap',
          }}
        >
          <span style={{ fontSize: '13.5px', color: 'var(--suave)' }}>Acciones rápidas para todos:</span>
          <button
            type="button"
            className="btn lig chico"
            onClick={() => handleMarcarTodos('PRESENTE')}
          >
            Todos Presentes
          </button>
          <button
            type="button"
            className="btn lig chico"
            onClick={() => handleMarcarTodos('AUSENTE')}
          >
            Todos Ausentes
          </button>
        </div>
      </div>

      {/* Lista de estudiantes y botones de estado */}
      <div className="tabla-caja">
        <table>
          <caption>Estudiantes convocados para la comisión y fecha seleccionada</caption>
          <thead>
            <tr>
              <th scope="col">Legajo</th>
              <th scope="col">Estudiante</th>
              <th scope="col">DNI</th>
              <th scope="col">Estado Seleccionado</th>
              <th scope="col" className="d">Registrar Condición</th>
            </tr>
          </thead>
          <tbody>
            {alumnosDelCurso.length === 0 ? (
              <tr>
                <td colSpan={5} style={{ textAlign: 'center', padding: '24px', color: 'var(--suave)' }}>
                  No hay estudiantes registrados en este curso.
                </td>
              </tr>
            ) : (
              alumnosDelCurso.map((alu) => {
                const est = estados[alu.idAlumno] || 'PRESENTE'
                return (
                  <tr key={alu.idAlumno}>
                    <td className="titulo-ficha font-monospace num" data-l="Legajo">{alu.legajo}</td>
                    <td data-l="Estudiante">
                      <strong>
                        {alu.apellido}, {alu.nombre}
                      </strong>
                    </td>
                    <td className="num" data-l="DNI">{alu.dni}</td>
                    <td data-l="Estado Seleccionado">
                      <StatusBadge status={est} />
                    </td>
                    <td className="d" data-l="Registrar Condición">
                      <div className="confirma" style={{ justifyContent: 'flex-end' }}>
                        <button
                          type="button"
                          className={`btn chico ${est === 'PRESENTE' ? '' : 'lig'}`}
                          onClick={() => handleSetEstado(alu.idAlumno, 'PRESENTE')}
                          aria-label="Presente"
                        >
                          P
                        </button>
                        <button
                          type="button"
                          className={`btn chico ${est === 'TARDANZA' ? '' : 'lig'}`}
                          onClick={() => handleSetEstado(alu.idAlumno, 'TARDANZA')}
                          aria-label="Tardanza"
                        >
                          T
                        </button>
                        <button
                          type="button"
                          className={`btn chico ${est === 'JUSTIFICADO' ? '' : 'lig'}`}
                          onClick={() => handleSetEstado(alu.idAlumno, 'JUSTIFICADO')}
                          aria-label="Justificado"
                        >
                          J
                        </button>
                        <button
                          type="button"
                          className={`btn chico ${est === 'AUSENTE' ? '' : 'lig'}`}
                          onClick={() => handleSetEstado(alu.idAlumno, 'AUSENTE')}
                          aria-label="Ausente"
                        >
                          A
                        </button>
                      </div>
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

export default AsistenciasDocentePage
