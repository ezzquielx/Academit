import { useState, useEffect } from 'react'
import { Button, Form, Row, Col, Badge } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import FormModal from '../../components/FormModal'
import { useToast } from '../../context/ToastContext'
import { getMesasExamen, createMesa, updateMesa } from '../../api/mesasExamen'
import { getMaterias } from '../../api/materias'
import { ETIQUETAS_TURNO } from '../../config/etiquetas'

export const GestionMesasPage = () => {
  const { showToast } = useToast()
  const [mesas, setMesas] = useState([])
  const [materias, setMaterias] = useState([])
  const [loading, setLoading] = useState(false)
  const [showModal, setShowModal] = useState(false)
  const [editingMesa, setEditingMesa] = useState(null)

  const [form, setForm] = useState({
    idMateria: 1,
    fecha: '2026-06-15',
    hora: '09:00 hs',
    turno: 'MANANA',
    tribunal: '',
    aula: 'Aula 101',
    cupoMaximo: 30,
    estadoMesa: 'ABIERTA',
  })

  const cargar = async () => {
    try {
      const data = await getMesasExamen()
      setMesas(data)
    } catch {
      showToast('Error al cargar mesas de examen', 'danger')
    }
  }

  useEffect(() => {
    cargar()
    getMaterias().then(setMaterias).catch(() => {})
  }, [])

  const handleOpenNuevo = () => {
    setEditingMesa(null)
    setForm({
      idMateria: 1,
      fecha: '2026-06-15',
      hora: '09:00 hs',
      turno: 'MANANA',
      tribunal: '',
      aula: 'Aula 101',
      cupoMaximo: 30,
      estadoMesa: 'ABIERTA',
    })
    setShowModal(true)
  }

  const handleOpenEditar = (m) => {
    setEditingMesa(m)
    setForm({
      idMateria: m.idMateria,
      fecha: m.fecha,
      hora: m.hora,
      turno: m.turno,
      tribunal: m.tribunal,
      aula: m.aula,
      cupoMaximo: m.cupoMaximo,
      estado: m.estado || m.estadoMesa,
      estadoMesa: m.estadoMesa || m.estado,
    })
    setShowModal(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      if (editingMesa) {
        await updateMesa(editingMesa.idMesa, {
          ...form,
          estado: form.estado || form.estadoMesa,
        })
        showToast('Mesa de examen actualizada', 'success')
      } else {
        const mat = materias.find((m) => m.idMateria === Number(form.idMateria))
        await createMesa({
          ...form,
          idCarrera: mat?.idCarrera || form.idCarrera || 1,
          estado: form.estado || form.estadoMesa,
        })
        showToast('Mesa de examen creada y abierta', 'success')
      }
      setShowModal(false)
      cargar()
    } catch (err) {
      showToast(err.message || 'Error al guardar mesa', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    { key: 'materiaNombre', label: 'Asignatura' },
    { key: 'fecha', label: 'Fecha' },
    { key: 'hora', label: 'Hora' },
    {
      key: 'turno',
      label: 'Turno',
      render: (row) => <Badge bg="secondary">{ETIQUETAS_TURNO[row.turno] || row.turno}</Badge>,
    },
    { key: 'aula', label: 'Aula' },
    { key: 'tribunal', label: 'Tribunal Evaluador' },
    {
      key: 'inscriptos',
      label: 'Inscriptos / Cupo',
      render: (row) => `${row.inscriptos} / ${row.cupoMaximo}`,
    },
    {
      key: 'estadoMesa',
      label: 'Estado',
      render: (row) => <StatusBadge status={row.estado || row.estadoMesa} />,
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
          title="Editar mesa"
          aria-label="Editar mesa"
        >
          <i className="bi bi-pencil" />
        </Button>
      ),
    },
  ]

  return (
    <div>
      <PageHeader
        title="Gestión de Mesas de Examen"
        subtitle="Programación de fechas de examen final, conformación de tribunales docentes y cupos"
        icon="bi-calendar-plus"
        actions={
          <Button variant="primary" size="sm" onClick={handleOpenNuevo}>
            <i className="bi bi-plus-lg me-1" /> Nueva Mesa
          </Button>
        }
      />

      <DataTable
        columns={columns}
        data={mesas}
        keyField="idMesa"
        searchPlaceholder="Buscar por materia, tribunal o aula..."
        searchKeys={['materiaNombre', 'tribunal', 'aula', 'fecha']}
      />

      <FormModal
        show={showModal}
        onHide={() => setShowModal(false)}
        onSubmit={handleSubmit}
        title={editingMesa ? `Editar Mesa - ${editingMesa.materiaNombre}` : 'Crear Mesa de Examen Final'}
        submitText={editingMesa ? 'Guardar Cambios' : 'Crear Mesa'}
        loading={loading}
      >
        <Row className="g-3">
          <Col md={12}>
            <Form.Group controlId="mesaMateria">
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

          <Col md={4}>
            <Form.Group controlId="mesaFecha">
              <Form.Label className="small fw-semibold">Fecha</Form.Label>
              <Form.Control
                type="date"
                value={form.fecha}
                onChange={(e) => setForm({ ...form, fecha: e.target.value })}
                required
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>

          <Col md={4}>
            <Form.Group controlId="mesaHora">
              <Form.Label className="small fw-semibold">Horario</Form.Label>
              <Form.Control
                type="text"
                value={form.hora}
                onChange={(e) => setForm({ ...form, hora: e.target.value })}
                placeholder="Ej. 09:00 hs"
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>

          <Col md={4}>
            <Form.Group controlId="mesaTurno">
              <Form.Label className="small fw-semibold">Turno</Form.Label>
              <Form.Select
                value={form.turno}
                onChange={(e) => setForm({ ...form, turno: e.target.value })}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              >
                <option value="MANANA">Mañana</option>
                <option value="TARDE">Tarde</option>
                <option value="NOCHE">Noche</option>
              </Form.Select>
            </Form.Group>
          </Col>

          <Col md={6}>
            <Form.Group controlId="mesaTribunal">
              <Form.Label className="small fw-semibold">Tribunal Examinador</Form.Label>
              <Form.Control
                type="text"
                placeholder="Ej. Prof. Gómez / Prof. Peralta"
                value={form.tribunal}
                onChange={(e) => setForm({ ...form, tribunal: e.target.value })}
                required
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>

          <Col md={3}>
            <Form.Group controlId="mesaAula">
              <Form.Label className="small fw-semibold">Aula</Form.Label>
              <Form.Control
                type="text"
                value={form.aula}
                onChange={(e) => setForm({ ...form, aula: e.target.value })}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>

          <Col md={3}>
            <Form.Group controlId="mesaCupo">
              <Form.Label className="small fw-semibold">Cupo Máximo</Form.Label>
              <Form.Control
                type="number"
                min="1"
                value={form.cupoMaximo}
                onChange={(e) => setForm({ ...form, cupoMaximo: Number(e.target.value) })}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>
        </Row>
      </FormModal>
    </div>
  )
}

export default GestionMesasPage
