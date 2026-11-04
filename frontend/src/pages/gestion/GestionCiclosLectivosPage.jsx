import { useState, useEffect } from 'react'
import { Button, Form, Row, Col } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import FormModal from '../../components/FormModal'
import { useToast } from '../../context/ToastContext'
import { getCiclosLectivos, createCicloLectivo, updateCicloLectivo } from '../../api/ciclosLectivos'

export const GestionCiclosLectivosPage = () => {
  const { showToast } = useToast()
  const [ciclos, setCiclos] = useState([])
  const [loading, setLoading] = useState(false)
  const [showModal, setShowModal] = useState(false)
  const [editingCiclo, setEditingCiclo] = useState(null)

  const [form, setForm] = useState({
    anio: 2026,
    nombre: 'Ciclo Lectivo 2026',
    fechaInicio: '2026-03-01',
    fechaFin: '2026-12-20',
    estado: 'ACTIVO',
  })

  const cargar = async () => {
    try {
      const data = await getCiclosLectivos()
      setCiclos(data)
    } catch {
      showToast('Error al cargar ciclos lectivos', 'danger')
    }
  }

  useEffect(() => {
    cargar()
  }, [])

  const handleOpenNuevo = () => {
    setEditingCiclo(null)
    setForm({
      anio: 2027,
      nombre: 'Ciclo Lectivo 2027',
      fechaInicio: '2027-03-01',
      fechaFin: '2027-12-20',
      estado: 'ACTIVO',
    })
    setShowModal(true)
  }

  const handleOpenEditar = (c) => {
    setEditingCiclo(c)
    setForm({
      anio: c.anio,
      nombre: c.nombre,
      fechaInicio: c.fechaInicio,
      fechaFin: c.fechaFin,
      estado: c.estado,
    })
    setShowModal(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      if (editingCiclo) {
        await updateCicloLectivo(editingCiclo.idCiclo, form)
        showToast('Ciclo lectivo actualizado', 'success')
      } else {
        await createCicloLectivo(form)
        showToast('Nuevo ciclo lectivo abierto', 'success')
      }
      setShowModal(false)
      cargar()
    } catch (err) {
      showToast(err.message || 'Error al guardar ciclo', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    { key: 'anio', label: 'Año Calendario', width: '130px' },
    {
      key: 'nombre',
      label: 'Denominación',
      render: (row) => <strong style={{ color: 'var(--m-onSurface)' }}>{row.nombre}</strong>,
    },
    { key: 'fechaInicio', label: 'Fecha de Inicio' },
    { key: 'fechaFin', label: 'Fecha de Cierre' },
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
        <Button
          variant="outline-primary"
          size="sm"
          onClick={() => handleOpenEditar(row)}
          title="Editar ciclo lectivo"
          aria-label="Editar ciclo lectivo"
        >
          <i className="bi bi-pencil" />
        </Button>
      ),
    },
  ]

  return (
    <div>
      <PageHeader
        title="Gestión de Ciclos Lectivos"
        subtitle="Apertura y administración de períodos lectivos institucionales y calendarios académicos"
        icon="bi-calendar-range"
        actions={
          <Button variant="primary" size="sm" onClick={handleOpenNuevo}>
            <i className="bi bi-plus-lg me-1" /> Nuevo Ciclo Lectivo
          </Button>
        }
      />

      <DataTable
        columns={columns}
        data={ciclos}
        keyField="idCiclo"
        searchPlaceholder="Buscar por denominación o año..."
        searchKeys={['nombre', 'anio', 'estado']}
      />

      <FormModal
        show={showModal}
        onHide={() => setShowModal(false)}
        onSubmit={handleSubmit}
        title={editingCiclo ? `Editar ${editingCiclo.nombre}` : 'Nuevo Ciclo Lectivo'}
        submitText={editingCiclo ? 'Guardar Cambios' : 'Abrir Ciclo'}
        loading={loading}
      >
        <Row className="g-3">
          <Col md={6}>
            <Form.Group controlId="cicloAnio">
              <Form.Label className="small fw-semibold">Año Calendario</Form.Label>
              <Form.Control
                type="number"
                value={form.anio}
                onChange={(e) => setForm({ ...form, anio: Number(e.target.value) })}
                required
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>
          <Col md={6}>
            <Form.Group controlId="cicloNombre">
              <Form.Label className="small fw-semibold">Nombre Oficial</Form.Label>
              <Form.Control
                type="text"
                value={form.nombre}
                onChange={(e) => setForm({ ...form, nombre: e.target.value })}
                required
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>
          <Col md={4}>
            <Form.Group controlId="cicloInicio">
              <Form.Label className="small fw-semibold">Fecha de Inicio</Form.Label>
              <Form.Control
                type="date"
                value={form.fechaInicio}
                onChange={(e) => setForm({ ...form, fechaInicio: e.target.value })}
                required
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>
          <Col md={4}>
            <Form.Group controlId="cicloFin">
              <Form.Label className="small fw-semibold">Fecha de Finalización</Form.Label>
              <Form.Control
                type="date"
                value={form.fechaFin}
                onChange={(e) => setForm({ ...form, fechaFin: e.target.value })}
                required
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>
          <Col md={4}>
            <Form.Group controlId="cicloEstado">
              <Form.Label className="small fw-semibold">Estado</Form.Label>
              <Form.Select
                value={form.estado}
                onChange={(e) => setForm({ ...form, estado: e.target.value })}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              >
                <option value="ACTIVO">Activo</option>
                <option value="CERRADO">Cerrado</option>
              </Form.Select>
            </Form.Group>
          </Col>
        </Row>
      </FormModal>
    </div>
  )
}

export default GestionCiclosLectivosPage
