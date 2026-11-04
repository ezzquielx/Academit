import { useState, useEffect } from 'react'
import { Button, Form, Row, Col, Badge } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import FormModal from '../../components/FormModal'
import { useToast } from '../../context/ToastContext'
import { getMaterias, createMateria, updateMateria } from '../../api/materias'

export const GestionMateriasPage = () => {
  const { showToast } = useToast()
  const [materias, setMaterias] = useState([])
  const [loading, setLoading] = useState(false)
  const [showModal, setShowModal] = useState(false)
  const [editingMateria, setEditingMateria] = useState(null)

  const [form, setForm] = useState({
    codigo: '',
    nombre: '',
    anio: 1,
    regimen: 'Anual',
    horasSemanales: 4,
    correlativasTexto: '',
  })

  const cargar = async () => {
    try {
      const data = await getMaterias()
      setMaterias(data)
    } catch {
      showToast('Error al cargar materias', 'danger')
    }
  }

  useEffect(() => {
    cargar()
  }, [])

  const handleOpenNuevo = () => {
    setEditingMateria(null)
    setForm({ codigo: '', nombre: '', anio: 1, regimen: 'Anual', horasSemanales: 4, correlativasTexto: '' })
    setShowModal(true)
  }

  const getNombreCorrelativa = (c) => {
    if (typeof c === 'number') {
      const mat = materias.find((m) => m.idMateria === c)
      return mat ? mat.nombre : `Materia #${c}`
    }
    return c
  }

  const handleOpenEditar = (m) => {
    setEditingMateria(m)
    setForm({
      codigo: m.codigo,
      nombre: m.nombre,
      anio: m.anio,
      regimen: m.regimen,
      horasSemanales: m.horasSemanales,
      correlativasTexto: (m.correlativas || []).map(getNombreCorrelativa).join(', '),
    })
    setShowModal(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      const correlativas = form.correlativasTexto
        ? form.correlativasTexto
            .split(',')
            .map((s) => s.trim())
            .filter(Boolean)
            .map((nomOId) => {
              const mat = materias.find((m) => m.nombre.toLowerCase() === nomOId.toLowerCase())
              return mat ? mat.idMateria : nomOId
            })
        : []

      const payload = {
        ...form,
        correlativas,
      }

      if (editingMateria) {
        await updateMateria(editingMateria.idMateria, payload)
        showToast('Materia actualizada con éxito', 'success')
      } else {
        await createMateria(payload)
        showToast('Materia registrada con éxito', 'success')
      }
      setShowModal(false)
      cargar()
    } catch (err) {
      showToast(err.message || 'Error al guardar materia', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    { key: 'codigo', label: 'Código', width: '110px' },
    {
      key: 'nombre',
      label: 'Nombre de Asignatura',
      render: (row) => <strong style={{ color: 'var(--m-onSurface)' }}>{row.nombre}</strong>,
    },
    {
      key: 'anio',
      label: 'Año',
      render: (row) => <Badge bg="secondary">{row.anio}° Año</Badge>,
    },
    { key: 'regimen', label: 'Régimen de Cursada' },
    { key: 'horasSemanales', label: 'Horas Semanales' },
    {
      key: 'correlativas',
      label: 'Correlativas Exigidas',
      render: (row) =>
        row.correlativas && row.correlativas.length > 0 ? (
          row.correlativas.map((c, idx) => (
            <Badge key={idx} bg="dark" className="me-1 border border-secondary">
              {getNombreCorrelativa(c)}
            </Badge>
          ))
        ) : (
          <span className="text-muted small fst-italic">Sin correlativas previas</span>
        ),
    },
    {
      key: 'acciones',
      label: 'Acciones',
      align: 'right',
      render: (row) => (
        <Button variant="outline-primary" size="sm" onClick={() => handleOpenEditar(row)}>
          <i className="bi bi-pencil" /> Editar
        </Button>
      ),
    },
  ]

  return (
    <div>
      <PageHeader
        title="Gestión de Materias"
        subtitle="Catálogo curricular de asignaturas, régimen cuatrimestral/anual y correlatividades"
        icon="bi-book"
        actions={
          <Button variant="primary" size="sm" onClick={handleOpenNuevo}>
            <i className="bi bi-plus-lg me-1" /> Nueva Materia
          </Button>
        }
      />

      <DataTable
        columns={columns}
        data={materias}
        keyField="idMateria"
        searchPlaceholder="Buscar por código, materia o correlativas..."
        searchKeys={['codigo', 'nombre', 'regimen']}
      />

      <FormModal
        show={showModal}
        onHide={() => setShowModal(false)}
        onSubmit={handleSubmit}
        title={editingMateria ? `Editar Materia - ${editingMateria.codigo}` : 'Alta de Nueva Asignatura'}
        submitText={editingMateria ? 'Guardar Cambios' : 'Crear Asignatura'}
        loading={loading}
      >
        <Row className="g-3">
          <Col md={6}>
            <Form.Group controlId="matNombre">
              <Form.Label className="small fw-semibold">Nombre de la Materia <span className="text-danger">*</span></Form.Label>
              <Form.Control
                type="text"
                value={form.nombre}
                onChange={(e) => setForm({ ...form, nombre: e.target.value })}
                required
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>
          <Col md={6}>
            <Form.Group controlId="matCodigo">
              <Form.Label className="small fw-semibold">Código Curricular</Form.Label>
              <Form.Control
                type="text"
                placeholder="Ej. MAT-101"
                value={form.codigo}
                onChange={(e) => setForm({ ...form, codigo: e.target.value })}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>
          <Col md={4}>
            <Form.Group controlId="matAnio">
              <Form.Label className="small fw-semibold">Año de Cursada</Form.Label>
              <Form.Select
                value={form.anio}
                onChange={(e) => setForm({ ...form, anio: Number(e.target.value) })}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              >
                <option value={1}>1° Año</option>
                <option value={2}>2° Año</option>
                <option value={3}>3° Año</option>
              </Form.Select>
            </Form.Group>
          </Col>
          <Col md={4}>
            <Form.Group controlId="matRegimen">
              <Form.Label className="small fw-semibold">Régimen</Form.Label>
              <Form.Select
                value={form.regimen}
                onChange={(e) => setForm({ ...form, regimen: e.target.value })}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              >
                <option value="Anual">Anual</option>
                <option value="1° Cuatrimestre">1° Cuatrimestre</option>
                <option value="2° Cuatrimestre">2° Cuatrimestre</option>
              </Form.Select>
            </Form.Group>
          </Col>
          <Col md={4}>
            <Form.Group controlId="matHoras">
              <Form.Label className="small fw-semibold">Horas Semanales</Form.Label>
              <Form.Control
                type="number"
                min="1"
                value={form.horasSemanales}
                onChange={(e) => setForm({ ...form, horasSemanales: e.target.value })}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>
          <Col md={12}>
            <Form.Group controlId="matCorrelativas">
              <Form.Label className="small fw-semibold">Correlativas Previas (separadas por coma)</Form.Label>
              <Form.Control
                type="text"
                placeholder="Ej. Programación I, Bases de Datos I"
                value={form.correlativasTexto}
                onChange={(e) => setForm({ ...form, correlativasTexto: e.target.value })}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>
        </Row>
      </FormModal>
    </div>
  )
}

export default GestionMateriasPage
