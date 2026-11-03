import { useState, useEffect, useCallback } from 'react'
import { Card, Row, Col, Badge, Button, ListGroup, Form } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import FormModal from '../../components/FormModal'
import { useToast } from '../../context/ToastContext'
import { useModo } from '../../hooks/useModo'
import { updateMateria } from '../../api/materias'
import { getCarreras } from '../../api/carreras'
import { getPlanesEstudio, getMateriasDePlan } from '../../api/planes'

export const GestionPlanEstudiosPage = () => {
  const { showToast } = useToast()
  const modo = useModo()
  const esLectura = modo === 'lectura'

  const [carreras, setCarreras] = useState([])
  const [planes, setPlanes] = useState([])
  const [materiasPlan, setMateriasPlan] = useState([])
  const [selectedCarreraId, setSelectedCarreraId] = useState(1)
  const [editingMateria, setEditingMateria] = useState(null)
  const [correlativasTexto, setCorrelativasTexto] = useState('')
  const [loading, setLoading] = useState(false)

  const cargarDatos = async () => {
    try {
      const [cars, pls] = await Promise.all([
        getCarreras(),
        getPlanesEstudio(),
      ])
      setCarreras(cars)
      setPlanes(pls)
    } catch {
      showToast('Error al cargar plan de estudios o carreras', 'danger')
    }
  }

  useEffect(() => {
    cargarDatos()
  }, [])

  const carreraActual = carreras.find((c) => c.idCarrera === Number(selectedCarreraId)) || carreras[0]
  const planActual = planes.find((p) => p.idCarrera === Number(selectedCarreraId))

  const cargarMateriasPlan = useCallback(async (idPlan) => {
    if (!idPlan) {
      setMateriasPlan([])
      return
    }
    try {
      const mats = await getMateriasDePlan(idPlan)
      setMateriasPlan(mats)
    } catch {
      showToast('Error al cargar materias del plan', 'danger')
    }
  }, [showToast])

  useEffect(() => {
    if (planActual?.idPlan) {
      cargarMateriasPlan(planActual.idPlan)
    } else {
      setMateriasPlan([])
    }
  }, [planActual?.idPlan, cargarMateriasPlan])

  const handleOpenEditarCorrelativas = (m) => {
    setEditingMateria(m)
    const nombres = (m.correlativas || []).map((c) => {
      if (typeof c === 'number') {
        const encontrada = materiasPlan.find((x) => x.idMateria === c)
        return encontrada ? encontrada.nombre : c
      }
      return c
    })
    setCorrelativasTexto(nombres.join(', '))
  }

  const handleGuardarCorrelativas = async (e) => {
    e.preventDefault()
    if (!editingMateria) return
    setLoading(true)
    try {
      const correlativas = correlativasTexto
        .split(',')
        .map((s) => s.trim())
        .filter(Boolean)
        .map((nomOId) => {
          const encontrada = materiasPlan.find((x) => x.nombre.toLowerCase() === nomOId.toLowerCase())
          return encontrada ? encontrada.idMateria : nomOId
        })

      await updateMateria(editingMateria.idMateria, { correlativas })
      showToast(`Correlatividades actualizadas para ${editingMateria.nombre}`, 'success')
      setEditingMateria(null)
      if (planActual?.idPlan) {
        cargarMateriasPlan(planActual.idPlan)
      }
    } catch (err) {
      showToast(err.message || 'Error al actualizar', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const anios = [1, 2, 3]

  return (
    <div>
      <PageHeader
        title="Gestión del Plan de Estudios y Correlativas"
        subtitle={
          esLectura
            ? 'Consulta del árbol curricular y régimen de correlatividades por carrera (Modo Lectura)'
            : 'Administración del árbol de correlatividades, malla curricular y condiciones de cursada'
        }
        icon="bi-diagram-3-fill"
      />

      {/* Selector de Carrera para filtrar el plan */}
      <Card className="border-0  mb-4" style={{ backgroundColor: 'var(--m-surface)' }}>
        <Card.Body className="p-3">
          <Row className="align-items-center g-3">
            <Col md={5}>
              <Form.Group controlId="filtroCarrera">
                <Form.Label className="small fw-semibold text-uppercase text-muted">
                  Filtrar por Carrera Académica:
                </Form.Label>
                <Form.Select
                  value={selectedCarreraId}
                  onChange={(e) => setSelectedCarreraId(Number(e.target.value))}
                  style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
                >
                  {carreras.map((c) => (
                    <option key={c.idCarrera} value={c.idCarrera}>
                      {c.nombre} ({c.codigo})
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>
            </Col>

            <Col md={7}>
              <div
                className="p-2 px-3 rounded-2 small d-flex align-items-center justify-content-between"
                style={{ backgroundColor: 'var(--m-surfaceVariant)' }}
              >
                <div>
                  <strong style={{ color: 'var(--m-primary)' }}>
                    {carreraActual?.nombre || 'Carrera Seleccionada'}
                  </strong>
                  <div className="text-muted">
                    {planActual ? `${planActual.nombre} (${planActual.codigo}) - ${planActual.estado}` : 'Plan Vigente'}
                  </div>
                </div>
                <Badge bg="secondary">
                  {materiasPlan.length} asignaturas totales
                </Badge>
              </div>
            </Col>
          </Row>
        </Card.Body>
      </Card>

      <Row className="g-4">
        {anios.map((anio) => {
          const mats = materiasPlan.filter((m) => m.anio === anio)
          return (
            <Col key={anio} lg={4}>
              <Card className="border-0  h-100" style={{ backgroundColor: 'var(--m-surface)' }}>
                <Card.Header
                  className="p-3 border-bottom border-secondary border-opacity-25 d-flex justify-content-between align-items-center"
                  style={{ backgroundColor: 'var(--m-surfaceVariant)' }}
                >
                  <strong className="fs-6" style={{ color: 'var(--m-primary)' }}>
                    Malla {anio}° Año
                  </strong>
                  <Badge bg="secondary">{mats.length} asignaturas</Badge>
                </Card.Header>
                <Card.Body className="p-2">
                  {mats.length === 0 ? (
                    <div className="p-3 text-muted small fst-italic">
                      No hay asignaturas registradas para este año en el plan seleccionado.
                    </div>
                  ) : (
                    <ListGroup variant="flush">
                      {mats.map((m) => (
                        <ListGroup.Item
                          key={m.idMateria}
                          className="p-3 border-bottom border-secondary border-opacity-25"
                          style={{ backgroundColor: 'transparent' }}
                        >
                          <div className="d-flex justify-content-between align-items-start mb-1">
                            <div>
                              <strong style={{ color: 'var(--m-onSurface)' }}>{m.nombre}</strong>
                              <div className="text-muted small">
                                {m.codigo} • {m.horasSemanales} hs/sem
                              </div>
                            </div>

                            {/* El botón de editar se oculta en modo lectura */}
                            {!esLectura && (
                              <Button
                                variant="outline-primary"
                                size="sm"
                                onClick={() => handleOpenEditarCorrelativas(m)}
                                title="Editar correlativas"
                              >
                                <i className="bi bi-gear" />
                              </Button>
                            )}
                          </div>

                          <div className="mt-2 pt-2 border-top border-secondary border-opacity-10 small">
                            <span className="text-muted">Correlativas: </span>
                            {m.correlativas && m.correlativas.length > 0 ? (
                              m.correlativas.map((c, idx) => {
                                const nombre = typeof c === 'number'
                                  ? (materiasPlan.find((x) => x.idMateria === c)?.nombre || `Materia #${c}`)
                                  : c
                                return (
                                  <Badge key={idx} bg="dark" className="me-1 border border-secondary">
                                    {nombre}
                                  </Badge>
                                )
                              })
                            ) : (
                              <span className="text-muted fst-italic">Sin requisitos previos</span>
                            )}
                          </div>
                        </ListGroup.Item>
                      ))}
                    </ListGroup>
                  )}
                </Card.Body>
              </Card>
            </Col>
          )
        })}
      </Row>

      {!esLectura && (
        <FormModal
          show={Boolean(editingMateria)}
          onHide={() => setEditingMateria(null)}
          onSubmit={handleGuardarCorrelativas}
          title={`Correlatividades para: ${editingMateria?.nombre}`}
          submitText="Guardar Correlatividades"
          loading={loading}
        >
          <p className="small mb-3">
            Ingrese los nombres exactos de las materias correlativas requeridas para cursar esta asignatura, separadas por coma.
          </p>
          <textarea
            rows={3}
            className="form-control"
            placeholder="Ej. Programación I, Bases de Datos I"
            value={correlativasTexto}
            onChange={(e) => setCorrelativasTexto(e.target.value)}
            style={{
              backgroundColor: 'var(--m-surfaceVariant)',
              color: 'var(--m-onSurface)',
              borderColor: 'var(--m-outline)',
            }}
          />
        </FormModal>
      )}
    </div>
  )
}

export default GestionPlanEstudiosPage
