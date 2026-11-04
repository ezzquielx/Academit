import { useState, useEffect } from 'react'
import { Button, Form, Row, Col, Badge } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import FormModal from '../../components/FormModal'
import ConfirmModal from '../../components/ConfirmModal'
import { useToast } from '../../context/ToastContext'
import { useModo } from '../../hooks/useModo'
import { getAsignaciones, createAsignacion, deleteAsignacion } from '../../api/asignaciones'
import { getDocentes } from '../../api/docentes'
import { getMaterias } from '../../api/materias'
import { getCursos } from '../../api/cursos'
import { ETIQUETAS_TURNO } from '../../config/etiquetas'

export const GestionAsignacionesPage = () => {
  const { showToast } = useToast()
  const modo = useModo()
  const esLectura = modo === 'lectura'

  const [asignaciones, setAsignaciones] = useState([])
  const [docentes, setDocentes] = useState([])
  const [materias, setMaterias] = useState([])
  const [cursos, setCursos] = useState([])
  const [loading, setLoading] = useState(false)
  const [showModal, setShowModal] = useState(false)
  const [asigToDelete, setAsigToDelete] = useState(null)

  const [form, setForm] = useState({
    idDocente: 1,
    idMateria: 1,
    idCurso: 1,
    rolDocente: 'Titular',
  })

  const cargar = async () => {
    try {
      const data = await getAsignaciones()
      setAsignaciones(data)
    } catch {
      showToast('Error al cargar asignaciones', 'danger')
    }
  }

  useEffect(() => {
    cargar()
    getDocentes().then(setDocentes).catch(() => {})
    getMaterias().then(setMaterias).catch(() => {})
    getCursos().then(setCursos).catch(() => {})
  }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      await createAsignacion(form)
      showToast('Asignación docente creada exitosamente', 'success')
      setShowModal(false)
      cargar()
    } catch (err) {
      showToast(err.message || 'Error al crear asignación', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const handleConfirmBaja = async () => {
    if (!asigToDelete) return
    setLoading(true)
    try {
      await deleteAsignacion(asigToDelete.idAsignacion)
      showToast('Asignación eliminada', 'warning')
      setAsigToDelete(null)
      cargar()
    } catch (err) {
      showToast(err.message || 'Error al eliminar asignación', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    { key: 'docenteNombre', label: 'Profesor / Docente' },
    { key: 'materiaNombre', label: 'Materia' },
    { key: 'cursoNombre', label: 'Curso / Comisión' },
    {
      key: 'rolDocente',
      label: 'Cargo',
      render: (row) => <Badge bg="primary">{row.rolDocente}</Badge>,
    },
    ...(!esLectura
      ? [
          {
            key: 'acciones',
            label: 'Acciones',
            align: 'right',
            render: (row) => (
              <Button
                variant="outline-danger"
                size="sm"
                onClick={() => setAsigToDelete(row)}
                title="Quitar asignación"
                aria-label="Quitar asignación"
              >
                <i className="bi bi-trash" />
              </Button>
            ),
          },
        ]
      : []),
  ]

  return (
    <div>
      <PageHeader
        title="Asignaciones Docente - Curso - Materia"
        subtitle={
          esLectura
            ? 'Vínculo institucional entre equipo de cátedra, asignatura y comisión horaria (Modo Lectura)'
            : 'Vínculo institucional entre equipo de cátedra, asignatura y comisión horaria'
        }
        icon="bi-link-45deg"
        actions={
          !esLectura && (
            <Button variant="primary" size="sm" onClick={() => setShowModal(true)}>
              <i className="bi bi-plus-lg me-1" /> Nueva Asignación
            </Button>
          )
        }
      />

      <DataTable
        columns={columns}
        data={asignaciones}
        keyField="idAsignacion"
        searchPlaceholder="Buscar por docente, materia o curso..."
        searchKeys={['docenteNombre', 'materiaNombre', 'cursoNombre']}
      />

      {!esLectura && (
        <>
          <FormModal
            show={showModal}
            onHide={() => setShowModal(false)}
            onSubmit={handleSubmit}
            title="Crear Asignación Docente"
            submitText="Asignar Docente"
            loading={loading}
          >
            <Row className="g-3">
              <Col md={12}>
                <Form.Group controlId="asigDocente">
                  <Form.Label className="small fw-semibold">Docente</Form.Label>
                  <Form.Select
                    value={form.idDocente}
                    onChange={(e) => setForm({ ...form, idDocente: Number(e.target.value) })}
                    style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
                  >
                    {docentes.map((d) => (
                      <option key={d.idDocente} value={d.idDocente}>
                        {d.apellido}, {d.nombre} ({d.titulo})
                      </option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>

              <Col md={6}>
                <Form.Group controlId="asigMateria">
                  <Form.Label className="small fw-semibold">Materia</Form.Label>
                  <Form.Select
                    value={form.idMateria}
                    onChange={(e) => setForm({ ...form, idMateria: Number(e.target.value) })}
                    style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
                  >
                    {materias.map((m) => (
                      <option key={m.idMateria} value={m.idMateria}>
                        {m.nombre} ({m.codigo})
                      </option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>

              <Col md={6}>
                <Form.Group controlId="asigCurso">
                  <Form.Label className="small fw-semibold">Curso / Comisión</Form.Label>
                  <Form.Select
                    value={form.idCurso}
                    onChange={(e) => setForm({ ...form, idCurso: Number(e.target.value) })}
                    style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
                  >
                    {cursos.map((c) => (
                      <option key={c.idCurso} value={c.idCurso}>
                        {c.nombre} ({ETIQUETAS_TURNO[c.turno] || c.turno})
                      </option>
                    ))}
                  </Form.Select>
                </Form.Group>
              </Col>

              <Col md={12}>
                <Form.Group controlId="asigRol">
                  <Form.Label className="small fw-semibold">Cargo / Rol en la Cátedra</Form.Label>
                  <Form.Select
                    value={form.rolDocente}
                    onChange={(e) => setForm({ ...form, rolDocente: e.target.value })}
                    style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
                  >
                    <option value="Titular">Profesor Titular</option>
                    <option value="Adjunto">Profesor Adjunto</option>
                    <option value="Ayudante de Primera">Ayudante de Primera</option>
                    <option value="Jefe de Trabajos Prácticos">Jefe de Trabajos Prácticos (JTP)</option>
                  </Form.Select>
                </Form.Group>
              </Col>
            </Row>
          </FormModal>

          <ConfirmModal
            show={Boolean(asigToDelete)}
            onHide={() => setAsigToDelete(null)}
            onConfirm={handleConfirmBaja}
            title="Quitar Asignación Docente"
            message={`¿Desea desvincular al docente ${asigToDelete?.docenteNombre} de la materia ${asigToDelete?.materiaNombre}?`}
            confirmText="Confirmar Desvinculación"
            variant="danger"
            loading={loading}
          />
        </>
      )}
    </div>
  )
}

export default GestionAsignacionesPage
