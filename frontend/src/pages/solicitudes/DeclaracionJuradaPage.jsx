import { useState, useEffect } from 'react'
import { Alert, Card, Form, Row, Col, Button } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { getDeclaracionesJuradas, createDeclaracionJurada } from '../../api/declaracionesJuradas'
import { getMaterias } from '../../api/materias'

export const DeclaracionJuradaPage = () => {
  const { user } = useAuth()
  const { showToast } = useToast()
  const [declaraciones, setDeclaraciones] = useState([])
  const [materias, setMaterias] = useState([])
  const [materiasSeleccionadas, setMateriasSeleccionadas] = useState([1, 2])
  const [observaciones, setObservaciones] = useState('')
  const [loading, setLoading] = useState(false)
  const alumnoId = user?.idAlumno || 1

  const cargar = async () => {
    try {
      const [ddjjData, matsData] = await Promise.all([
        getDeclaracionesJuradas({ idAlumno: alumnoId }),
        getMaterias(),
      ])
      setDeclaraciones(ddjjData)
      setMaterias(matsData)
    } catch {
      showToast('Error al cargar declaraciones juradas', 'danger')
    }
  }

  useEffect(() => {
    cargar()
  }, [])

  const handleToggleMateria = (idMateria) => {
    setMateriasSeleccionadas((prev) =>
      prev.includes(idMateria) ? prev.filter((id) => id !== idMateria) : [...prev, idMateria]
    )
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (materiasSeleccionadas.length === 0) {
      showToast('Debe seleccionar al menos una materia para la declaración jurada', 'warning')
      return
    }

    setLoading(true)
    try {
      await createDeclaracionJurada({
        idAlumno: alumnoId,
        idCiclo: 1,
        materiasDeclaradasIds: materiasSeleccionadas,
        observaciones: observaciones.trim() || 'Declaración jurada presentada bajo régimen reglamentario (RF51).',
      })
      showToast('Declaración jurada presentada formalmente (RF51)', 'success')
      setObservaciones('')
      cargar()
    } catch (err) {
      showToast(err, 'danger')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    { key: 'idDeclaracion', label: '# DJ', width: '80px' },
    { key: 'fecha', label: 'Fecha' },
    { key: 'cicloNombre', label: 'Ciclo Lectivo' },
    {
      key: 'materias',
      label: 'Materias Declaradas',
      render: (row) =>
        row.materiasNombres && row.materiasNombres.length > 0
          ? row.materiasNombres.join(', ')
          : 'Materias del plan',
    },
    {
      key: 'estado',
      label: 'Estado',
      render: (row) => <StatusBadge status={row.estado} />,
    },
    {
      key: 'observaciones',
      label: 'Observaciones / Resolución',
      render: (row) => <span className="small text-muted">{row.observaciones || '-'}</span>,
    },
  ]

  return (
    <div>
      <PageHeader
        title="Declaración Jurada de Materias a Cursar"
        subtitle="Presentación formal de compromiso académico y correlatividades para cursada (RF51)"
        icon="bi-file-earmark-check"
      />

      <Alert variant="warning" className="small mb-4 d-flex align-items-center gap-2">
        <i className="bi bi-shield-exclamation fs-5" />
        <div>
          <strong>Requisito RF51:</strong> Mediante esta declaración jurada, el estudiante declara bajo juramento cumplir con las correlatividades exigidas para cursar las materias seleccionadas en el ciclo lectivo 2026.
        </div>
      </Alert>

      {/* Formulario de Presentación de DJ */}
      <Card className="border-0  mb-4" style={{ backgroundColor: 'var(--m-surface)' }}>
        <Card.Header
          className="p-3 border-bottom border-secondary border-opacity-25 d-flex align-items-center gap-2"
          style={{ backgroundColor: 'var(--m-surfaceVariant)' }}
        >
          <i className="bi bi-pencil-square" style={{ color: 'var(--m-primary)' }} />
          <strong style={{ color: 'var(--m-onSurface)' }}>Nueva Declaración Jurada</strong>
        </Card.Header>
        <Card.Body className="p-3 p-md-4">
          <Form onSubmit={handleSubmit}>
            <Row className="g-3">
              <Col md={12}>
                <Form.Label className="small fw-semibold" style={{ color: 'var(--m-onSurface)' }}>
                  Ciclo Lectivo
                </Form.Label>
                <Form.Control
                  type="text"
                  value="Ciclo Lectivo 2026"
                  disabled
                  readOnly
                  style={{
                    backgroundColor: 'var(--m-surfaceVariant)',
                    color: 'var(--m-onSurface)',
                    borderColor: 'var(--m-outline)',
                  }}
                />
              </Col>

              <Col md={12}>
                <Form.Label className="small fw-semibold" style={{ color: 'var(--m-onSurface)' }}>
                  Materias a Cursar bajo Declaración Jurada <span className="text-danger">*</span>
                </Form.Label>
                <div
                  className="p-3 rounded border"
                  style={{
                    backgroundColor: 'var(--m-surfaceVariant)',
                    borderColor: 'var(--m-outline)',
                    maxHeight: '200px',
                    overflowY: 'auto',
                  }}
                >
                  {materias.map((m) => (
                    <Form.Check
                      key={m.idMateria}
                      type="checkbox"
                      id={`materia-check-${m.idMateria}`}
                      label={`${m.codigo} - ${m.nombre} (${m.horasSemanales} hs/sem)`}
                      checked={materiasSeleccionadas.includes(m.idMateria)}
                      onChange={() => handleToggleMateria(m.idMateria)}
                      className="mb-2"
                    />
                  ))}
                </div>
              </Col>

              <Col md={12}>
                <Form.Group controlId="djObservaciones">
                  <Form.Label className="small fw-semibold" style={{ color: 'var(--m-onSurface)' }}>
                    Observaciones o Justificación de Correlatividades
                  </Form.Label>
                  <Form.Control
                    as="textarea"
                    rows={2}
                    placeholder="Indique detalles o aclaraciones sobre su situación de correlatividad..."
                    value={observaciones}
                    onChange={(e) => setObservaciones(e.target.value)}
                    style={{
                      backgroundColor: 'var(--m-surfaceVariant)',
                      color: 'var(--m-onSurface)',
                      borderColor: 'var(--m-outline)',
                    }}
                  />
                </Form.Group>
              </Col>

              <Col md={12} className="d-flex justify-content-end">
                <Button
                  variant="primary"
                  type="submit"
                  disabled={loading}
                  className="px-4"
                >
                  {loading ? 'Cargando…' : 'Presentar Declaración Jurada'}
                </Button>
              </Col>
            </Row>
          </Form>
        </Card.Body>
      </Card>

      <h5 className="fw-bold mb-3" style={{ color: 'var(--m-primary)' }}>
        Mis Declaraciones Juradas Presentadas
      </h5>

      <DataTable
        columns={columns}
        data={declaraciones}
        keyField="idDeclaracion"
        searchPlaceholder="Buscar en mis declaraciones..."
        searchKeys={['cicloNombre', 'observaciones', 'estado']}
      />
    </div>
  )
}

export default DeclaracionJuradaPage
