import { useState, useEffect } from 'react'
import { Button, Form, Row, Col, Modal, Card, Badge, ListGroup } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import FormModal from '../../components/FormModal'
import ConfirmModal from '../../components/ConfirmModal'
import { useToast } from '../../context/ToastContext'
import { useModo } from '../../hooks/useModo'
import {
  getCarreras,
  createCarrera,
  updateCarrera,
  deleteCarrera,
  getCarreraPlanes,
  getCarreraCursos,
} from '../../api/carreras'
import { ETIQUETAS_TURNO } from '../../config/etiquetas'

export const GestionCarrerasPage = () => {
  const { showToast } = useToast()
  const modo = useModo()
  const esLectura = modo === 'lectura'

  const [carreras, setCarreras] = useState([])
  const [loading, setLoading] = useState(false)

  // Estado del modal de alta / edición
  const [showModal, setShowModal] = useState(false)
  const [editingCarrera, setEditingCarrera] = useState(null)
  const [form, setForm] = useState({
    codigo: '',
    nombre: '',
    tituloOtorgado: '',
    duracionAnios: 3,
    resolucion: '',
  })
  const [errors, setErrors] = useState({})

  // Estado del modal de confirmación de baja lógica
  const [carreraToDelete, setCarreraToDelete] = useState(null)

  // Estado del modal de detalle (al abrir una carrera se ven sus planes y sus cursos)
  const [carreraDetalle, setCarreraDetalle] = useState(null)
  const [detallePlanes, setDetallePlanes] = useState([])
  const [detalleCursos, setDetalleCursos] = useState([])
  const [loadingDetalle, setLoadingDetalle] = useState(false)

  const cargar = async () => {
    try {
      const data = await getCarreras()
      setCarreras(data)
    } catch {
      showToast('Error al cargar carreras académicas', 'danger')
    }
  }

  useEffect(() => {
    cargar()
  }, [])

  // Validaciones del formulario
  const validarForm = () => {
    const err = {}
    if (!form.codigo.trim()) {
      err.codigo = 'El código identificador es obligatorio'
    }
    if (!form.nombre.trim()) {
      err.nombre = 'La denominación de la carrera es obligatoria'
    } else if (form.nombre.trim().length < 5) {
      err.nombre = 'El nombre debe tener al menos 5 caracteres'
    }
    if (!form.tituloOtorgado.trim()) {
      err.tituloOtorgado = 'El título otorgado es obligatorio'
    }
    setErrors(err)
    return Object.keys(err).length === 0
  }

  const handleOpenNuevo = () => {
    setEditingCarrera(null)
    setForm({
      codigo: '',
      nombre: '',
      tituloOtorgado: '',
      duracionAnios: 3,
      resolucion: '',
    })
    setErrors({})
    setShowModal(true)
  }

  const handleOpenEditar = (c) => {
    setEditingCarrera(c)
    setForm({
      codigo: c.codigo || '',
      nombre: c.nombre || '',
      tituloOtorgado: c.tituloOtorgado || '',
      duracionAnios: c.duracionAnios || 3,
      resolucion: c.resolucion || '',
    })
    setErrors({})
    setShowModal(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!validarForm()) return

    setLoading(true)
    try {
      if (editingCarrera) {
        await updateCarrera(editingCarrera.idCarrera, form)
        showToast('Carrera actualizada exitosamente', 'success')
      } else {
        await createCarrera(form)
        showToast('Nueva carrera incorporada a la oferta académica', 'success')
      }
      setShowModal(false)
      cargar()
    } catch (err) {
      showToast(err.message || 'Error al guardar carrera', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const handleConfirmBaja = async () => {
    if (!carreraToDelete) return
    setLoading(true)
    try {
      await deleteCarrera(carreraToDelete.idCarrera)
      showToast(`Baja lógica realizada: la carrera ${carreraToDelete.nombre} pasó a INACTIVA`, 'warning')
      setCarreraToDelete(null)
      cargar()
    } catch (err) {
      showToast(err.message || 'Error al procesar la baja', 'danger')
    } finally {
      setLoading(false)
    }
  }

  // Abrir detalle de planes y cursos asociados a la carrera
  const handleOpenDetalle = async (c) => {
    setCarreraDetalle(c)
    setLoadingDetalle(true)
    try {
      const [planes, cursos] = await Promise.all([
        getCarreraPlanes(c.idCarrera),
        getCarreraCursos(c.idCarrera),
      ])
      setDetallePlanes(planes)
      setDetalleCursos(cursos)
    } catch {
      showToast('Error al cargar planes y cursos de la carrera', 'danger')
    } finally {
      setLoadingDetalle(false)
    }
  }

  const columns = [
    { key: 'codigo', label: 'Código', width: '100px' },
    {
      key: 'nombre',
      label: 'Carrera',
      render: (row) => (
        <div>
          <strong style={{ color: 'var(--m-onSurface)' }}>{row.nombre}</strong>
          <div className="small text-muted">{row.tituloOtorgado}</div>
        </div>
      ),
    },
    {
      key: 'duracionAnios',
      label: 'Duración',
      render: (row) => `${row.duracionAnios} años`,
    },
    {
      key: 'resolucion',
      label: 'Resolución Ministerial',
      render: (row) => <span className="small text-muted">{row.resolucion || 'N/A'}</span>,
    },
    {
      key: 'estado',
      label: 'Condición',
      render: (row) => <StatusBadge status={row.estado} />,
    },
    {
      key: 'planesCursos',
      label: 'Estructura',
      render: (row) => (
        <Button
          variant="outline-info"
          size="sm"
          onClick={() => handleOpenDetalle(row)}
          title="Ver planes y cursos"
          aria-label="Ver planes y cursos"
        >
          <i className="bi bi-diagram-3-fill" />
        </Button>
      ),
    },
    ...(!esLectura
      ? [
          {
            key: 'acciones',
            label: 'Acciones',
            align: 'right',
            render: (row) => (
              <div className="d-flex justify-content-end gap-1">
                <Button
                  variant="outline-primary"
                  size="sm"
                  onClick={() => handleOpenEditar(row)}
                  title="Editar carrera"
                  aria-label="Editar carrera"
                >
                  <i className="bi bi-pencil" />
                </Button>
                {row.estado !== 'INACTIVA' && (
                  <Button
                    variant="outline-danger"
                    size="sm"
                    onClick={() => setCarreraToDelete(row)}
                    title="Baja lógica de carrera"
                    aria-label="Baja lógica de carrera"
                  >
                    <i className="bi bi-trash" />
                  </Button>
                )}
              </div>
            ),
          },
        ]
      : []),
  ]

  return (
    <div>
      <PageHeader
        title="Gestión de Carreras"
        subtitle={
          esLectura
            ? 'Consulta institucional de oferta académica, planes de estudio y comisiones asociadas (Modo Lectura)'
            : 'Administración de la oferta formativa: alta, modificación, planes de estudio y comisiones'
        }
        icon="bi-mortarboard"
        actions={
          !esLectura && (
            <Button
              variant="primary"
              size="sm"
              onClick={handleOpenNuevo}
              className="d-flex align-items-center gap-1"
            >
              <i className="bi bi-plus-lg" />
              <span>Nueva Carrera</span>
            </Button>
          )
        }
      />

      <DataTable
        columns={columns}
        data={carreras}
        keyField="idCarrera"
        searchPlaceholder="Buscar por código, nombre o título..."
        searchKeys={['codigo', 'nombre', 'tituloOtorgado', 'resolucion']}
      />

      {/* Modal de Alta y Modificación (Oculto en modo lectura) */}
      {!esLectura && (
        <FormModal
          show={showModal}
          onHide={() => setShowModal(false)}
          onSubmit={handleSubmit}
          title={editingCarrera ? `Editar Carrera - ${editingCarrera.codigo}` : 'Registrar Nueva Carrera'}
          submitText={editingCarrera ? 'Guardar Cambios' : 'Registrar Carrera'}
          loading={loading}
        >
          <Row className="g-3">
            <Col md={4}>
              <Form.Group controlId="carrCodigo">
                <Form.Label className="small fw-semibold">
                  Código Identificador <span className="text-danger">*</span>
                </Form.Label>
                <Form.Control
                  type="text"
                  placeholder="Ej. TSDS"
                  value={form.codigo}
                  onChange={(e) => setForm({ ...form, codigo: e.target.value.toUpperCase() })}
                  isInvalid={Boolean(errors.codigo)}
                  style={{
                    backgroundColor: 'var(--m-surfaceVariant)',
                    color: 'var(--m-onSurface)',
                    borderColor: 'var(--m-outline)',
                  }}
                />
                <Form.Control.Feedback type="invalid">{errors.codigo}</Form.Control.Feedback>
              </Form.Group>
            </Col>

            <Col md={8}>
              <Form.Group controlId="carrNombre">
                <Form.Label className="small fw-semibold">
                  Nombre de la Carrera <span className="text-danger">*</span>
                </Form.Label>
                <Form.Control
                  type="text"
                  placeholder="Ej. Tecnicatura Superior en Desarrollo de Software"
                  value={form.nombre}
                  onChange={(e) => setForm({ ...form, nombre: e.target.value })}
                  isInvalid={Boolean(errors.nombre)}
                  style={{
                    backgroundColor: 'var(--m-surfaceVariant)',
                    color: 'var(--m-onSurface)',
                    borderColor: 'var(--m-outline)',
                  }}
                />
                <Form.Control.Feedback type="invalid">{errors.nombre}</Form.Control.Feedback>
              </Form.Group>
            </Col>

            <Col md={8}>
              <Form.Group controlId="carrTitulo">
                <Form.Label className="small fw-semibold">
                  Título Otorgado <span className="text-danger">*</span>
                </Form.Label>
                <Form.Control
                  type="text"
                  placeholder="Ej. Técnico Superior en Desarrollo de Software"
                  value={form.tituloOtorgado}
                  onChange={(e) => setForm({ ...form, tituloOtorgado: e.target.value })}
                  isInvalid={Boolean(errors.tituloOtorgado)}
                  style={{
                    backgroundColor: 'var(--m-surfaceVariant)',
                    color: 'var(--m-onSurface)',
                    borderColor: 'var(--m-outline)',
                  }}
                />
                <Form.Control.Feedback type="invalid">{errors.tituloOtorgado}</Form.Control.Feedback>
              </Form.Group>
            </Col>

            <Col md={4}>
              <Form.Group controlId="carrDuracion">
                <Form.Label className="small fw-semibold">Duración (años)</Form.Label>
                <Form.Control
                  type="number"
                  min="1"
                  max="10"
                  value={form.duracionAnios}
                  onChange={(e) => setForm({ ...form, duracionAnios: Number(e.target.value) })}
                  style={{
                    backgroundColor: 'var(--m-surfaceVariant)',
                    color: 'var(--m-onSurface)',
                    borderColor: 'var(--m-outline)',
                  }}
                />
              </Form.Group>
            </Col>

            <Col md={12}>
              <Form.Group controlId="carrResolucion">
                <Form.Label className="small fw-semibold">Resolución Ministerial / Dictamen</Form.Label>
                <Form.Control
                  type="text"
                  placeholder="Ej. Res. Ministerial N° 1420/22"
                  value={form.resolucion}
                  onChange={(e) => setForm({ ...form, resolucion: e.target.value })}
                  style={{
                    backgroundColor: 'var(--m-surfaceVariant)',
                    color: 'var(--m-onSurface)',
                    borderColor: 'var(--m-outline)',
                  }}
                />
              </Form.Group>
            </Col>
          </Row>
        </FormModal>
      )}

      {/* Modal de Detalle: al abrir una carrera se ven sus planes y sus cursos */}
      <Modal
        show={Boolean(carreraDetalle)}
        onHide={() => setCarreraDetalle(null)}
        size="lg"
        centered
        animation={false}
      >
        <Modal.Header
          closeButton
          closeVariant="white"
          className="border-bottom border-secondary border-opacity-25"
          style={{ backgroundColor: 'var(--m-surface)', color: 'var(--m-onSurface)' }}
        >
          <Modal.Title className="fs-5 d-flex align-items-center gap-2">
            <i className="bi bi-mortarboard-fill" style={{ color: 'var(--m-primary)' }} />
            <span>{carreraDetalle?.nombre}</span>
          </Modal.Title>
        </Modal.Header>

        <Modal.Body style={{ backgroundColor: 'var(--m-surface)', color: 'var(--m-onSurface)' }}>
          {loadingDetalle ? (
            <div className="text-center py-4 text-muted">Cargando planes y cursos...</div>
          ) : (
            <Row className="g-4">
              {/* Sección Planes de Estudio */}
              <Col md={6}>
                <Card className="border-0  h-100" style={{ backgroundColor: 'var(--m-surfaceVariant)' }}>
                  <Card.Header
                    className="p-3 border-bottom border-secondary border-opacity-25 d-flex justify-content-between align-items-center"
                    style={{ backgroundColor: 'transparent' }}
                  >
                    <strong style={{ color: 'var(--m-primary)' }}>
                      <i className="bi bi-diagram-3 me-2" />
                      Planes de Estudio
                    </strong>
                    <Badge bg="secondary">{detallePlanes.length}</Badge>
                  </Card.Header>
                  <Card.Body className="p-2">
                    {detallePlanes.length === 0 ? (
                      <div className="p-3 text-muted small fst-italic">No hay planes registrados para esta carrera.</div>
                    ) : (
                      <ListGroup variant="flush">
                        {detallePlanes.map((plan) => (
                          <ListGroup.Item
                            key={plan.idPlan}
                            className="p-3 border-bottom border-secondary border-opacity-25"
                            style={{ backgroundColor: 'transparent', color: 'var(--m-onSurface)' }}
                          >
                            <div className="d-flex justify-content-between align-items-start mb-1">
                              <div>
                                <strong>{plan.nombre}</strong>
                                <div className="small text-muted">{plan.codigo} • Res. {plan.anioResolucion}</div>
                              </div>
                              <StatusBadge status={plan.estado} />
                            </div>
                            <div className="small text-muted mt-2">
                              {plan.cantidadMaterias} materias curriculares • {plan.horasTotales} hs reloj
                            </div>
                          </ListGroup.Item>
                        ))}
                      </ListGroup>
                    )}
                  </Card.Body>
                </Card>
              </Col>

              {/* Sección Cursos / Comisiones */}
              <Col md={6}>
                <Card className="border-0  h-100" style={{ backgroundColor: 'var(--m-surfaceVariant)' }}>
                  <Card.Header
                    className="p-3 border-bottom border-secondary border-opacity-25 d-flex justify-content-between align-items-center"
                    style={{ backgroundColor: 'transparent' }}
                  >
                    <strong style={{ color: 'var(--m-primary)' }}>
                      <i className="bi bi-building me-2" />
                      Cursos / Comisiones
                    </strong>
                    <Badge bg="secondary">{detalleCursos.length}</Badge>
                  </Card.Header>
                  <Card.Body className="p-2">
                    {detalleCursos.length === 0 ? (
                      <div className="p-3 text-muted small fst-italic">No hay comisiones asignadas para esta carrera.</div>
                    ) : (
                      <ListGroup variant="flush">
                        {detalleCursos.map((curso) => (
                          <ListGroup.Item
                            key={curso.idCurso}
                            className="p-3 border-bottom border-secondary border-opacity-25"
                            style={{ backgroundColor: 'transparent', color: 'var(--m-onSurface)' }}
                          >
                            <div className="d-flex justify-content-between align-items-start mb-1">
                              <div>
                                <strong>{curso.nombre}</strong>
                                <div className="small text-muted">{curso.codigo} • Turno {ETIQUETAS_TURNO[curso.turno] || curso.turno}</div>
                              </div>
                              <Badge bg={curso.cuposDisponibles > 0 ? 'success' : 'danger'}>
                                {curso.cuposDisponibles} cupos libres
                              </Badge>
                            </div>
                            <div className="small text-muted mt-2">
                              {curso.aula} • Inscriptos: {curso.inscriptos} / {curso.capacidadMaxima}
                            </div>
                          </ListGroup.Item>
                        ))}
                      </ListGroup>
                    )}
                  </Card.Body>
                </Card>
              </Col>
            </Row>
          )}
        </Modal.Body>

        <Modal.Footer
          className="border-top border-secondary border-opacity-25"
          style={{ backgroundColor: 'var(--m-surface)' }}
        >
          <Button variant="secondary" size="sm" onClick={() => setCarreraDetalle(null)}>
            Cerrar Detalle
          </Button>
        </Modal.Footer>
      </Modal>

      {/* Modal de Baja Lógica */}
      {!esLectura && (
        <ConfirmModal
          show={Boolean(carreraToDelete)}
          onHide={() => setCarreraToDelete(null)}
          onConfirm={handleConfirmBaja}
          title="Confirmar Baja Lógica de Carrera"
          message={`¿Está seguro de dar de baja la carrera "${carreraToDelete?.nombre}" (${carreraToDelete?.codigo})? Su condición pasará a INACTIVA.`}
          confirmText="Confirmar Baja"
          variant="danger"
          loading={loading}
        />
      )}
    </div>
  )
}

export default GestionCarrerasPage
