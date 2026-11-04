import { useState, useEffect } from 'react'
import { Button, Form, Row, Col, Alert } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import FormModal from '../../components/FormModal'
import ConfirmModal from '../../components/ConfirmModal'
import { useToast } from '../../context/ToastContext'
import { getInscripciones, createInscripcion, cancelarInscripcion } from '../../api/inscripciones'
import { getAlumnos } from '../../api/alumnos'
import { getCursos } from '../../api/cursos'
import { getCiclosLectivos } from '../../api/ciclosLectivos'
import { ETIQUETAS_TURNO } from '../../config/etiquetas'

export const GestionInscripcionesPage = () => {
  const { showToast } = useToast()
  const [inscripciones, setInscripciones] = useState([])
  const [alumnos, setAlumnos] = useState([])
  const [cursos, setCursos] = useState([])
  const [ciclos, setCiclos] = useState([])
  const [loading, setLoading] = useState(false)
  const [showModal, setShowModal] = useState(false)
  const [inscToDelete, setInscToDelete] = useState(null)

  const [form, setForm] = useState({
    idAlumno: 1,
    idCurso: 1,
    cicloLectivo: 2026,
  })

  const cargar = async () => {
    try {
      const data = await getInscripciones()
      setInscripciones(data)
    } catch {
      showToast('Error al cargar inscripciones', 'danger')
    }
  }

  useEffect(() => {
    cargar()
    getAlumnos().then(setAlumnos).catch(() => {})
    getCursos().then(setCursos).catch(() => {})
    getCiclosLectivos().then(setCiclos).catch(() => {})
  }, [])

  // Alumno seleccionado y cursos exclusivos de su carrera
  const alumnoSeleccionado = alumnos.find((a) => a.idAlumno === Number(form.idAlumno))
  const cursosDeLaCarrera = cursos.filter(
    (c) => !alumnoSeleccionado || !c.idCarrera || c.idCarrera === alumnoSeleccionado.idCarrera
  )
  const selectedCursoInfo = cursos.find((c) => c.idCurso === Number(form.idCurso))

  const handleAlumnoChange = (idAlu) => {
    const alu = alumnos.find((a) => a.idAlumno === Number(idAlu))
    const cursosFiltrados = cursos.filter(
      (c) => !alu || !c.idCarrera || c.idCarrera === alu.idCarrera
    )
    const primerCursoValido = cursosFiltrados[0]?.idCurso || (cursos[0]?.idCurso || 1)
    setForm({
      ...form,
      idAlumno: Number(idAlu),
      idCurso: primerCursoValido,
    })
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (selectedCursoInfo && selectedCursoInfo.cuposDisponibles <= 0) {
      showToast('No es posible inscribir: el curso no posee cupos disponibles', 'danger')
      return
    }

    setLoading(true)
    try {
      await createInscripcion(form)
      showToast('Inscripción al curso confirmada con éxito', 'success')
      setShowModal(false)
      cargar()
      // Recargar cursos para actualizar cupos
      getCursos().then(setCursos)
    } catch (err) {
      showToast(err.message || 'Error al inscribir alumno', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const handleConfirmBaja = async () => {
    if (!inscToDelete) return
    setLoading(true)
    try {
      await cancelarInscripcion(inscToDelete.idInscripcion)
      showToast('Inscripción dada de baja y cupo liberado', 'warning')
      setInscToDelete(null)
      cargar()
      getCursos().then(setCursos)
    } catch (err) {
      showToast(err.message || 'Error al cancelar inscripción', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    { key: 'idInscripcion', label: '# ID', width: '80px' },
    {
      key: 'alumnoNombre',
      label: 'Estudiante',
      render: (row) => <strong style={{ color: 'var(--m-onSurface)' }}>{row.alumnoNombre}</strong>,
    },
    { key: 'cursoNombre', label: 'Curso / Comisión' },
    { key: 'cicloLectivo', label: 'Ciclo Lectivo' },
    { key: 'fechaInscripcion', label: 'Fecha' },
    {
      key: 'estado',
      label: 'Estado',
      render: (row) => <StatusBadge status={row.estado} />,
    },
    {
      key: 'acciones',
      label: 'Acciones',
      align: 'right',
      render: (row) => (
        row.estado !== 'CANCELADA' && (
          <Button
            variant="outline-danger"
            size="sm"
            onClick={() => setInscToDelete(row)}
            title="Cancelar inscripción"
            aria-label="Cancelar inscripción"
          >
            <i className="bi bi-x-circle" />
          </Button>
        )
      ),
    },
  ]

  return (
    <div>
      <PageHeader
        title="Inscripciones a Cursos"
        subtitle="Matriculación a comisiones: sólo se ofrecen cursos correspondientes a la carrera del alumno, con aviso de cupo"
        icon="bi-journal-plus"
        actions={
          <Button
            variant="primary"
            size="sm"
            onClick={() => {
              const primerAlu = alumnos[0]
              const primerCurso = cursos.filter((c) => !primerAlu || !c.idCarrera || c.idCarrera === primerAlu.idCarrera)[0]
              setForm({
                idAlumno: primerAlu?.idAlumno || 1,
                idCurso: primerCurso?.idCurso || 1,
                cicloLectivo: 2026,
              })
              setShowModal(true)
            }}
          >
            <i className="bi bi-plus-lg me-1" /> Nueva Inscripción
          </Button>
        }
      />

      <DataTable
        columns={columns}
        data={inscripciones}
        keyField="idInscripcion"
        searchPlaceholder="Buscar por alumno o curso..."
        searchKeys={['alumnoNombre', 'cursoNombre']}
      />

      <FormModal
        show={showModal}
        onHide={() => setShowModal(false)}
        onSubmit={handleSubmit}
        title="Inscribir Alumno a Curso / Comisión"
        submitText="Confirmar Inscripción"
        loading={loading}
      >
        <Row className="g-3">
          <Col md={12}>
            <Form.Group controlId="inscAlumno">
              <Form.Label className="small fw-semibold">Estudiante a Inscribir <span className="text-danger">*</span></Form.Label>
              <Form.Select
                value={form.idAlumno}
                onChange={(e) => handleAlumnoChange(e.target.value)}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              >
                {alumnos.map((a) => (
                  <option key={a.idAlumno} value={a.idAlumno}>
                    {a.apellido}, {a.nombre} (Legajo: {a.legajo} - {a.carrera})
                  </option>
                ))}
              </Form.Select>
            </Form.Group>
            {alumnoSeleccionado && (
              <div className="small mt-1 text-muted d-flex align-items-center gap-2">
                <i className="bi bi-mortarboard text-warning" />
                <span>Carrera: <strong>{alumnoSeleccionado.carrera}</strong></span>
              </div>
            )}
          </Col>

          <Col md={6}>
            <Form.Group controlId="inscCiclo">
              <Form.Label className="small fw-semibold">Ciclo Lectivo</Form.Label>
              <Form.Select
                value={form.cicloLectivo}
                onChange={(e) => setForm({ ...form, cicloLectivo: Number(e.target.value) })}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              >
                {ciclos.map((c) => (
                  <option key={c.idCiclo} value={c.anio}>
                    {c.nombre}
                  </option>
                ))}
              </Form.Select>
            </Form.Group>
          </Col>

          <Col md={6}>
            <Form.Group controlId="inscCurso">
              <Form.Label className="small fw-semibold">
                Comisión Habilitada (Solo de su carrera) <span className="text-danger">*</span>
              </Form.Label>
              <Form.Select
                value={form.idCurso}
                onChange={(e) => setForm({ ...form, idCurso: Number(e.target.value) })}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              >
                {cursosDeLaCarrera.length === 0 ? (
                  <option disabled value="">No hay cursos para esta carrera</option>
                ) : (
                  cursosDeLaCarrera.map((c) => (
                    <option key={c.idCurso} value={c.idCurso}>
                      {c.nombre} ({ETIQUETAS_TURNO[c.turno] || c.turno}) - {c.cuposDisponibles} cupos libres
                    </option>
                  ))
                )}
              </Form.Select>
            </Form.Group>
          </Col>

          {/* Aviso visual de cupo */}
          <Col md={12}>
            {selectedCursoInfo && (
              <Alert
                variant={selectedCursoInfo.cuposDisponibles > 5 ? 'info' : selectedCursoInfo.cuposDisponibles > 0 ? 'warning' : 'danger'}
                className="mb-0 small d-flex align-items-center gap-2"
              >
                <i className={`bi ${selectedCursoInfo.cuposDisponibles === 0 ? 'bi-x-circle-fill' : 'bi-info-circle-fill'} fs-5`} />
                <div>
                  <strong>Aviso de Cupo ({selectedCursoInfo.nombre}):</strong>{' '}
                  Capacidad: {selectedCursoInfo.capacidadMaxima} | Inscriptos: {selectedCursoInfo.inscriptos} |{' '}
                  <span className="fw-bold">
                    {selectedCursoInfo.cuposDisponibles > 0
                      ? `${selectedCursoInfo.cuposDisponibles} vacante(s) disponible(s)`
                      : '¡CUPO COMPLETO! No quedan vacantes en esta comisión.'}
                  </span>
                </div>
              </Alert>
            )}
          </Col>
        </Row>
      </FormModal>

      <ConfirmModal
        show={Boolean(inscToDelete)}
        onHide={() => setInscToDelete(null)}
        onConfirm={handleConfirmBaja}
        title="Cancelar Inscripción a Curso"
        message={`¿Desea cancelar la inscripción del estudiante ${inscToDelete?.alumnoNombre} en el curso "${inscToDelete?.cursoNombre}"? Se liberará el cupo en la comisión.`}
        confirmText="Confirmar Cancelación"
        variant="warning"
        loading={loading}
      />
    </div>
  )
}

export default GestionInscripcionesPage
