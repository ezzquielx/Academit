import { useState, useEffect } from 'react'
import { Button, Form, Row, Col, Badge } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import FormModal from '../../components/FormModal'
import ConfirmModal from '../../components/ConfirmModal'
import { useToast } from '../../context/ToastContext'
import { getUsuarios, createUsuario, updateUsuario, deleteUsuario } from '../../api/usuarios'

export const GestionUsuariosPage = () => {
  const { showToast } = useToast()
  const [usuarios, setUsuarios] = useState([])
  const [loading, setLoading] = useState(false)
  const [showModal, setShowModal] = useState(false)
  const [editingUsuario, setEditingUsuario] = useState(null)
  const [userToDelete, setUserToDelete] = useState(null)

  const [form, setForm] = useState({
    username: '',
    nombre: '',
    email: '',
    rol: 'ALUMNO',
  })
  const [errors, setErrors] = useState({})

  const cargar = async () => {
    try {
      const data = await getUsuarios()
      setUsuarios(data)
    } catch {
      showToast('Error al cargar usuarios del sistema', 'danger')
    }
  }

  useEffect(() => {
    cargar()
  }, [])

  const handleOpenNuevo = () => {
    setEditingUsuario(null)
    setForm({ username: '', nombre: '', email: '', rol: 'ALUMNO' })
    setErrors({})
    setShowModal(true)
  }

  const handleOpenEditar = (u) => {
    setEditingUsuario(u)
    setForm({
      username: u.username,
      nombre: u.nombre,
      email: u.email,
      rol: u.rol,
      estado: u.estado || 'ACTIVO',
    })
    setErrors({})
    setShowModal(true)
  }

  const handleValidarUsuario = async (u) => {
    setLoading(true)
    try {
      await updateUsuario(u.idUsuario, {
        username: u.username,
        nombre: u.nombre,
        email: u.email,
        rol: u.rol,
        estado: 'ACTIVO',
      })
      showToast(`Cuenta de @${u.username} validada y activada exitosamente`, 'success')
      cargar()
    } catch (err) {
      showToast(err.message || 'Error al validar usuario', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    const newErrors = {}
    if (!form.username.trim()) newErrors.username = 'Nombre de usuario requerido'
    if (!form.nombre.trim()) newErrors.nombre = 'Nombre y apellido requerido'
    if (!form.email.trim()) newErrors.email = 'Email requerido'

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors)
      return
    }

    setLoading(true)
    try {
      if (editingUsuario) {
        await updateUsuario(editingUsuario.idUsuario, form)
        showToast('Usuario y roles actualizados con éxito', 'success')
      } else {
        await createUsuario(form)
        showToast('Usuario dado de alta exitosamente (clave por defecto: 1234)', 'success')
      }
      setShowModal(false)
      cargar()
    } catch (err) {
      showToast(err.message || 'Error al guardar usuario', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const handleConfirmBaja = async () => {
    if (!userToDelete) return
    setLoading(true)
    try {
      await deleteUsuario(userToDelete.idUsuario)
      showToast(`Usuario ${userToDelete.username} deshabilitado (baja lógica)`, 'warning')
      setUserToDelete(null)
      cargar()
    } catch (err) {
      showToast(err.message || 'Error al deshabilitar usuario', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    { key: 'idUsuario', label: '# ID', width: '70px' },
    {
      key: 'username',
      label: 'Nombre de Usuario',
      render: (row) => <strong className="font-monospace" style={{ color: 'var(--m-primary)' }}>@{row.username}</strong>,
    },
    { key: 'nombre', label: 'Nombre Completo' },
    { key: 'email', label: 'Correo Electrónico' },
    {
      key: 'rol',
      label: 'Rol del Sistema',
      render: (row) => (
        <Badge
          bg={
            row.rol === 'ADMINISTRADOR'
              ? 'danger'
              : row.rol === 'ADMINISTRATIVO'
              ? 'warning'
              : row.rol === 'DOCENTE'
              ? 'info'
              : 'success'
          }
        >
          {row.rol}
        </Badge>
      ),
    },
    {
      key: 'estado',
      label: 'Estado',
      render: (row) => <StatusBadge status={row.estado} />,
    },
    { key: 'ultimoAcceso', label: 'Último Acceso' },
    {
      key: 'acciones',
      label: 'Acciones',
      align: 'right',
      render: (row) => (
        <div className="d-flex justify-content-end gap-1">
          {row.estado === 'INACTIVO' && (
            <Button
              variant="outline-success"
              size="sm"
              title="Validar y activar cuenta"
              aria-label="Validar y activar cuenta"
              onClick={() => handleValidarUsuario(row)}
            >
              <i className="bi bi-person-check" />
            </Button>
          )}
          <Button
            variant="outline-primary"
            size="sm"
            onClick={() => handleOpenEditar(row)}
            title="Editar usuario"
            aria-label="Editar usuario"
          >
            <i className="bi bi-pencil" />
          </Button>
          {row.estado !== 'INACTIVO' && (
            <Button
              variant="outline-danger"
              size="sm"
              onClick={() => setUserToDelete(row)}
              title="Desactivar usuario"
              aria-label="Desactivar usuario"
            >
              <i className="bi bi-person-x" />
            </Button>
          )}
        </div>
      ),
    },
  ]

  return (
    <div>
      <PageHeader
        title="Gestión de Usuarios y Roles"
        subtitle="Control de accesos del sistema: alta de cuentas, asignación de perfiles (ADMINISTRADOR, ADMINISTRATIVO, DOCENTE, ALUMNO) y baja lógica"
        icon="bi-shield-lock"
        actions={
          <Button variant="primary" size="sm" onClick={handleOpenNuevo}>
            <i className="bi bi-plus-lg me-1" /> Nuevo Usuario
          </Button>
        }
      />

      <DataTable
        columns={columns}
        data={usuarios}
        keyField="idUsuario"
        searchPlaceholder="Buscar por username, nombre o rol..."
        searchKeys={['username', 'nombre', 'email', 'rol']}
      />

      <FormModal
        show={showModal}
        onHide={() => setShowModal(false)}
        onSubmit={handleSubmit}
        title={editingUsuario ? `Editar Usuario @${editingUsuario.username}` : 'Alta de Cuenta de Usuario'}
        submitText={editingUsuario ? 'Guardar Cambios' : 'Registrar Cuenta'}
        loading={loading}
      >
        <Row className="g-3">
          <Col md={6}>
            <Form.Group controlId="usrUsername">
              <Form.Label className="small fw-semibold">Nombre de Usuario (@login) <span className="text-danger">*</span></Form.Label>
              <Form.Control
                type="text"
                value={form.username}
                onChange={(e) => setForm({ ...form, username: e.target.value })}
                disabled={Boolean(editingUsuario)}
                isInvalid={Boolean(errors.username)}
                placeholder="Ej. mrodriguez"
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>
          <Col md={6}>
            <Form.Group controlId="usrNombre">
              <Form.Label className="small fw-semibold">Nombre y Apellido <span className="text-danger">*</span></Form.Label>
              <Form.Control
                type="text"
                value={form.nombre}
                onChange={(e) => setForm({ ...form, nombre: e.target.value })}
                isInvalid={Boolean(errors.nombre)}
                placeholder="Ej. Martín Rodríguez"
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>
          <Col md={6}>
            <Form.Group controlId="usrEmail">
              <Form.Label className="small fw-semibold">Correo Electrónico <span className="text-danger">*</span></Form.Label>
              <Form.Control
                type="email"
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                isInvalid={Boolean(errors.email)}
                placeholder="email@instituto.edu.ar"
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>
          <Col md={6}>
            <Form.Group controlId="usrRol">
              <Form.Label className="small fw-semibold">Rol Asignado en el Sistema <span className="text-danger">*</span></Form.Label>
              <Form.Select
                value={form.rol}
                onChange={(e) => setForm({ ...form, rol: e.target.value })}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              >
                <option value="ALUMNO">ALUMNO</option>
                <option value="DOCENTE">DOCENTE</option>
                <option value="ADMINISTRATIVO">ADMINISTRATIVO</option>
                <option value="ADMINISTRADOR">ADMINISTRADOR</option>
              </Form.Select>
            </Form.Group>
          </Col>
          {editingUsuario && (
            <Col md={6}>
              <Form.Group controlId="usrEstado">
                <Form.Label className="small fw-semibold">Estado de la Cuenta <span className="text-danger">*</span></Form.Label>
                <Form.Select
                  value={form.estado || 'ACTIVO'}
                  onChange={(e) => setForm({ ...form, estado: e.target.value })}
                  style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
                >
                  <option value="ACTIVO">ACTIVO (Habilitada)</option>
                  <option value="INACTIVO">INACTIVO (Pendiente de validación o Baja)</option>
                </Form.Select>
              </Form.Group>
            </Col>
          )}
        </Row>
      </FormModal>

      <ConfirmModal
        show={Boolean(userToDelete)}
        onHide={() => setUserToDelete(null)}
        onConfirm={handleConfirmBaja}
        title="Deshabilitar Usuario del Sistema"
        message={`¿Está seguro de revocar el acceso y aplicar baja lógica a @${userToDelete?.username} (${userToDelete?.nombre})? Su estado pasará a INACTIVO.`}
        confirmText="Confirmar Baja"
        variant="danger"
        loading={loading}
      />
    </div>
  )
}

export default GestionUsuariosPage
