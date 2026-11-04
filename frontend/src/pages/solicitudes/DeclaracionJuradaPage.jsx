import { useState, useEffect } from 'react'
import { Alert, Card, Form, Row, Col, Button, Modal } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import ConfirmModal from '../../components/ConfirmModal'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { getDeclaracionesJuradas, createDeclaracionJurada } from '../../api/declaracionesJuradas'
import { getMaterias } from '../../api/materias'
import { getCiclosLectivos } from '../../api/ciclosLectivos'
import { getAlumnoById } from '../../api/alumnos'
import { ETIQUETAS_VACIO } from '../../config/etiquetas'

export const DeclaracionJuradaPage = () => {
  const { idAlumno } = useAuth()
  const { showToast } = useToast()
  const [declaraciones, setDeclaraciones] = useState([])
  const [materias, setMaterias] = useState([])
  const [cicloActivo, setCicloActivo] = useState(null)
  const [materiasSeleccionadas, setMateriasSeleccionadas] = useState([])
  const [observaciones, setObservaciones] = useState('')
  const [loading, setLoading] = useState(false)
  const [confirmarEnvio, setConfirmarEnvio] = useState(false)
  const [detalle, setDetalle] = useState(null)
  const alumnoId = idAlumno

  const cargar = async () => {
    if (!alumnoId) return
    try {
      const [ddjjData, ciclos, alumno] = await Promise.all([
        getDeclaracionesJuradas({ idAlumno: alumnoId }),
        getCiclosLectivos(),
        getAlumnoById(alumnoId),
      ])
      setDeclaraciones(ddjjData || [])
      const activo = (ciclos || []).find((c) => c.estado === 'ACTIVO') || (ciclos || [])[0] || null
      setCicloActivo(activo)
      // Solo las materias del plan del alumno logueado (sin datos precargados de otros).
      const mats = await getMaterias(alumno?.idPlan ? { idPlan: alumno.idPlan } : {})
      setMaterias(mats || [])
    } catch {
      showToast('Error al cargar declaraciones juradas', 'danger')
    }
  }

  useEffect(() => {
    cargar()
  }, [alumnoId])

  const handleToggleMateria = (idMateria) => {
    setMateriasSeleccionadas((prev) =>
      prev.includes(idMateria) ? prev.filter((id) => id !== idMateria) : [...prev, idMateria]
    )
  }

  const handleSubmit = (e) => {
    e.preventDefault()
    if (materiasSeleccionadas.length === 0) {
      showToast('Debe seleccionar al menos una materia para la declaración jurada', 'warning')
      return
    }
    setConfirmarEnvio(true)
  }

  const presentar = async () => {
    setLoading(true)
    try {
      await createDeclaracionJurada({
        idAlumno: alumnoId,
        idCiclo: cicloActivo?.idCiclo ?? null,
        materiasDeclaradasIds: materiasSeleccionadas,
        observaciones: observaciones.trim() || undefined,
      })
      showToast('Declaración jurada presentada formalmente (RF51)', 'success')
      setObservaciones('')
      setMateriasSeleccionadas([])
      setConfirmarEnvio(false)
      cargar()
    } catch (err) {
      showToast(err?.message || 'No se pudo presentar la declaración jurada', 'danger')
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
          : '—',
    },
    {
      key: 'estado',
      label: 'Estado',
      render: (row) => <StatusBadge status={row.estado} />,
    },
    {
      key: 'acciones',
      label: 'Detalle',
      align: 'right',
      render: (row) => (
        <Button
          variant="outline-secondary"
          size="sm"
          onClick={() => setDetalle(row)}
          title="Ver detalle de declaración jurada"
          aria-label="Ver detalle de declaración jurada"
        >
          <i className="bi bi-eye" />
        </Button>
      ),
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
          <strong>Requisito RF51:</strong> Mediante esta declaración jurada, el estudiante declara
          bajo juramento cumplir con las correlatividades exigidas para cursar las materias
          seleccionadas{cicloActivo?.nombre ? ` en el ${cicloActivo.nombre}` : ''}.
        </div>
      </Alert>

      {!alumnoId && (
        <Alert variant="danger" className="small mb-4">
          {ETIQUETAS_VACIO.SIN_LEGAJO}. {ETIQUETAS_VACIO.SIN_LEGAJO_DETALLE}
        </Alert>
      )}

      {/* Formulario de Presentación de DJ */}
      <Card className="border-0 mb-4" style={{ backgroundColor: 'var(--m-surface)' }}>
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
              <Col xs={12}>
                <Form.Label className="small fw-semibold" style={{ color: 'var(--m-onSurface)' }}>
                  Ciclo Lectivo
                </Form.Label>
                <Form.Control
                  type="text"
                  value={cicloActivo?.nombre || 'Sin ciclo lectivo activo'}
                  disabled
                  readOnly
                  style={{
                    backgroundColor: 'var(--m-surfaceVariant)',
                    color: 'var(--m-onSurface)',
                    borderColor: 'var(--m-outline)',
                  }}
                />
              </Col>

              <Col xs={12}>
                <Form.Label className="small fw-semibold" style={{ color: 'var(--m-onSurface)' }}>
                  Materias a Cursar bajo Declaración Jurada <span className="text-danger">*</span>
                </Form.Label>
                {materias.length === 0 ? (
                  <div className="small text-muted">{ETIQUETAS_VACIO.SIN_MATERIAS}</div>
                ) : (
                  <div
                    className="p-3 rounded border"
                    style={{
                      backgroundColor: 'var(--m-surfaceVariant)',
                      borderColor: 'var(--m-outline)',
                      maxHeight: '220px',
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
                )}
              </Col>

              <Col xs={12}>
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

              <Col xs={12} className="d-flex flex-wrap justify-content-end gap-2">
                <Button
                  variant="outline-secondary"
                  type="button"
                  disabled
                  title="La descarga en PDF no tiene endpoint en el contrato (ver docs/pendientes.md)"
                >
                  <i className="bi bi-download me-1" /> Descargar PDF
                </Button>
                <Button
                  variant="primary"
                  type="submit"
                  disabled={loading || !alumnoId || materias.length === 0}
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
        emptyTitle={ETIQUETAS_VACIO.SIN_DECLARACIONES}
        emptyDescription={ETIQUETAS_VACIO.SIN_DECLARACIONES_DETALLE}
      />

      <ConfirmModal
        show={confirmarEnvio}
        onHide={() => setConfirmarEnvio(false)}
        onConfirm={presentar}
        loading={loading}
        title="Presentar declaración jurada"
        message={`Vas a declarar ${materiasSeleccionadas.length} materia(s) bajo juramento. Una vez presentada, la declaración queda registrada para revisión administrativa.`}
        confirmText="Presentar"
        variant="primary"
      />

      <Modal show={Boolean(detalle)} onHide={() => setDetalle(null)} centered animation={false}>
        <Modal.Header closeButton>
          <Modal.Title className="fs-6 fw-bold">Declaración jurada #{detalle?.idDeclaracion}</Modal.Title>
        </Modal.Header>
        <Modal.Body className="small">
          {detalle && (
            <dl className="mb-0">
              <dt>Fecha</dt>
              <dd>{detalle.fecha || '—'}</dd>
              <dt>Ciclo lectivo</dt>
              <dd>{detalle.cicloNombre || '—'}</dd>
              <dt>Estado</dt>
              <dd>
                <StatusBadge status={detalle.estado} />
              </dd>
              <dt>Materias declaradas</dt>
              <dd>
                {(detalle.materiasNombres || []).length === 0 ? (
                  '—'
                ) : (
                  <ul className="mb-0 ps-3">
                    {detalle.materiasNombres.map((m) => (
                      <li key={m}>{m}</li>
                    ))}
                  </ul>
                )}
              </dd>
              <dt>Observaciones</dt>
              <dd className="mb-0">{detalle.observaciones || '—'}</dd>
            </dl>
          )}
        </Modal.Body>
        <Modal.Footer>
          <Button variant="outline-secondary" size="sm" onClick={() => setDetalle(null)}>
            Cerrar
          </Button>
        </Modal.Footer>
      </Modal>
    </div>
  )
}

export default DeclaracionJuradaPage
