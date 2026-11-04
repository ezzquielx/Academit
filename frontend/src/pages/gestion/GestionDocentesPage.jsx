import { useState, useEffect } from 'react'
import { Button, Form, Row, Col } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import FormModal from '../../components/FormModal'
import ConfirmModal from '../../components/ConfirmModal'
import { useToast } from '../../context/ToastContext'
import { useModo } from '../../hooks/useModo'
import { getDocentes, createDocente, updateDocente, deleteDocente } from '../../api/docentes'

export const GestionDocentesPage = () => {
  const { showToast } = useToast()
  const modo = useModo()
  const esLectura = modo === 'lectura'

  const [docentes, setDocentes] = useState([])
  const [loading, setLoading] = useState(false)
  const [showModal, setShowModal] = useState(false)
  const [editingDocente, setEditingDocente] = useState(null)
  const [docenteToDelete, setDocenteToDelete] = useState(null)

  const [form, setForm] = useState({
    nombre: '',
    apellido: '',
    dni: '',
    email: '',
    titulo: '',
    telefono: '',
  })
  const [errors, setErrors] = useState({})

  const cargar = async () => {
    try {
      const data = await getDocentes()
      setDocentes(data)
    } catch {
      showToast('Error al cargar docentes', 'danger')
    }
  }

  useEffect(() => {
    cargar()
  }, [])

  const handleOpenNuevo = () => {
    setEditingDocente(null)
    setForm({ nombre: '', apellido: '', dni: '', email: '', titulo: '', telefono: '' })
    setErrors({})
    setShowModal(true)
  }

  const handleOpenEditar = (doc) => {
    setEditingDocente(doc)
    setForm({
      nombre: doc.nombre || '',
      apellido: doc.apellido || '',
      dni: doc.dni || '',
      email: doc.email || '',
      titulo: doc.titulo || '',
      telefono: doc.telefono || '',
    })
    setErrors({})
    setShowModal(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const newErrors = {}
    if (!form.nombre.trim()) newErrors.nombre = 'Nombre requerido'
    if (!form.apellido.trim()) newErrors.apellido = 'Apellido requerido'
    if (!form.dni.trim()) newErrors.dni = 'DNI requerido'
    if (!form.email.trim()) newErrors.email = 'Email requerido'

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors)
      return
    }

    setLoading(true)
    try {
      if (editingDocente) {
        await updateDocente(editingDocente.idDocente, form)
        showToast('Docente actualizado con éxito', 'success')
      } else {
        await createDocente(form)
        showToast('Docente dado de alta con éxito', 'success')
      }
      setShowModal(false)
      cargar()
    } catch (err) {
      showToast(err.message || 'Error al guardar docente', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const handleConfirmBaja = async () => {
    if (!docenteToDelete) return
    setLoading(true)
    try {
      await deleteDocente(docenteToDelete.idDocente)
      showToast(`Baja lógica realizada para ${docenteToDelete.apellido}`, 'warning')
      setDocenteToDelete(null)
      cargar()
    } catch (err) {
      showToast(err.message || 'Error al dar de baja', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    { key: 'legajo', label: 'Legajo', width: '100px' },
    {
      key: 'nombreCompleto',
      label: 'Docente',
      render: (row) => (
        <div>
          <strong style={{ color: 'var(--m-onSurface)' }}>
            {row.apellido}, {row.nombre}
          </strong>
          <div className="text-muted small">{row.email}</div>
        </div>
      ),
    },
    { key: 'dni', label: 'DNI' },
    { key: 'titulo', label: 'Título' },
    { key: 'telefono', label: 'Teléfono' },
    {
      key: 'estado',
      label: 'Condición',
      render: (row) => <StatusBadge status={row.estado} />,
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
                  title="Editar docente"
                  aria-label="Editar docente"
                >
                  <i className="bi bi-pencil" />
                </Button>
                {row.estado !== 'INACTIVO' && (
                  <Button
                    variant="outline-danger"
                    size="sm"
                    onClick={() => setDocenteToDelete(row)}
                    title="Desactivar docente"
                    aria-label="Desactivar docente"
                  >
                    <i className="bi bi-person-x" />
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
        title="Gestión de Docentes"
        subtitle={
          esLectura
            ? 'Padrón docente: consulta de cuerpo docente y legajos académicos (Modo Lectura)'
            : 'Padrón docente: alta, asignación de título y legajo académico'
        }
        icon="bi-person-video3"
        actions={
          !esLectura && (
            <Button variant="primary" size="sm" onClick={handleOpenNuevo}>
              <i className="bi bi-plus-lg me-1" /> Nuevo Docente
            </Button>
          )
        }
      />

      <DataTable
        columns={columns}
        data={docentes}
        keyField="idDocente"
        searchPlaceholder="Buscar por nombre, apellido, DNI o título..."
        searchKeys={['nombre', 'apellido', 'dni', 'titulo', 'legajo']}
      />

      {!esLectura && (
        <>
          <FormModal
        show={showModal}
        onHide={() => setShowModal(false)}
        onSubmit={handleSubmit}
        title={editingDocente ? `Editar Docente - ${editingDocente.legajo}` : 'Registrar Nuevo Docente'}
        submitText={editingDocente ? 'Guardar Cambios' : 'Registrar'}
        loading={loading}
      >
        <Row className="g-3">
          <Col md={6}>
            <Form.Group controlId="docNombre">
              <Form.Label className="small fw-semibold">Nombre <span className="text-danger">*</span></Form.Label>
              <Form.Control
                type="text"
                value={form.nombre}
                onChange={(e) => setForm({ ...form, nombre: e.target.value })}
                isInvalid={Boolean(errors.nombre)}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>
          <Col md={6}>
            <Form.Group controlId="docApellido">
              <Form.Label className="small fw-semibold">Apellido <span className="text-danger">*</span></Form.Label>
              <Form.Control
                type="text"
                value={form.apellido}
                onChange={(e) => setForm({ ...form, apellido: e.target.value })}
                isInvalid={Boolean(errors.apellido)}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>
          <Col md={6}>
            <Form.Group controlId="docDni">
              <Form.Label className="small fw-semibold">DNI <span className="text-danger">*</span></Form.Label>
              <Form.Control
                type="text"
                value={form.dni}
                onChange={(e) => setForm({ ...form, dni: e.target.value })}
                isInvalid={Boolean(errors.dni)}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>
          <Col md={6}>
            <Form.Group controlId="docEmail">
              <Form.Label className="small fw-semibold">Email <span className="text-danger">*</span></Form.Label>
              <Form.Control
                type="email"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                isInvalid={Boolean(errors.email)}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>
          <Col md={6}>
            <Form.Group controlId="docTitulo">
              <Form.Label className="small fw-semibold">Título</Form.Label>
              <Form.Control
                type="text"
                value={form.titulo}
                onChange={(e) => setForm({ ...form, titulo: e.target.value })}
                placeholder="Ej. Lic. en Sistemas"
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>
          <Col md={6}>
            <Form.Group controlId="docTel">
              <Form.Label className="small fw-semibold">Teléfono</Form.Label>
              <Form.Control
                type="text"
                value={form.telefono}
                onChange={(e) => setForm({ ...form, telefono: e.target.value })}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>
        </Row>
      </FormModal>

      <ConfirmModal
        show={Boolean(docenteToDelete)}
        onHide={() => setDocenteToDelete(null)}
        onConfirm={handleConfirmBaja}
        title="Confirmar Baja Lógica de Docente"
        message={`¿Desea dar de baja al docente ${docenteToDelete?.apellido}, ${docenteToDelete?.nombre}? Su estado pasará a INACTIVO.`}
        confirmText="Confirmar Baja"
        variant="danger"
        loading={loading}
      />
        </>
      )}
    </div>
  )
}

export default GestionDocentesPage
