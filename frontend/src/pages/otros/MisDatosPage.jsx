import { useState, useEffect } from 'react'
import { Card, Form, Button, Row, Col } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import StatusBadge from '../../components/StatusBadge'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { getMiPerfil, actualizarMiPerfil } from '../../api/usuarios'

export const MisDatosPage = () => {
  const { usuario, nombre, rol } = useAuth()
  const { showToast } = useToast()

  const [formData, setFormData] = useState({
    nombre: nombre || '',
    email: '',
    dni: '',
    telefono: '',
    domicilio: '',
    fechaNacimiento: '',
    carrera: '',
  })

  const [isEditing, setIsEditing] = useState(false)
  const [loading, setLoading] = useState(true)
  const [saving, setSaving] = useState(false)

  useEffect(() => {
    getMiPerfil()
      .then((data) => {
        setFormData((prev) => ({
          ...prev,
          ...data,
          domicilio: data.domicilio || prev.domicilio,
        }))
      })
      .catch(() => {
        showToast('Error al cargar datos de perfil', 'danger')
      })
      .finally(() => setLoading(false))
  }, [])

  const handleSubmit = async (e) => {
    e.preventDefault()
    setSaving(true)
    try {
      const actualizado = await actualizarMiPerfil(formData)
      setFormData((prev) => ({
        ...prev,
        ...actualizado,
        domicilio: actualizado.domicilio || prev.domicilio,
      }))
      setIsEditing(false)
      showToast('Tus datos personales se han actualizado correctamente', 'success')
    } catch (err) {
      showToast(err.message || 'Error al actualizar perfil', 'danger')
    } finally {
      setSaving(false)
    }
  }

  if (loading) {
    return (
      <div className="text-center py-5 small text-muted">
        Cargando…
      </div>
    )
  }

  return (
    <div>
      <PageHeader
        title="Mis Datos Personales"
        subtitle="Consulta y modificación de datos de contacto y filiación personal"
        icon="bi-person-badge"
        actions={
          !isEditing ? (
            <Button variant="primary" size="sm" onClick={() => setIsEditing(true)}>
              <i className="bi bi-pencil me-1" /> Editar Datos
            </Button>
          ) : (
            <Button variant="outline-secondary" size="sm" onClick={() => setIsEditing(false)}>
              Cancelar Edición
            </Button>
          )
        }
      />

      <Row className="g-4">
        <Col lg={4}>
          <Card className="border-0  text-center p-4" style={{ backgroundColor: 'var(--m-surface)' }}>
            <div
              className="rounded-circle mx-auto d-flex align-items-center justify-content-center mb-3"
              style={{
                width: '84px',
                height: '84px',
                backgroundColor: 'var(--m-surfaceVariant)',
                color: 'var(--m-primary)',
                fontSize: '2.5rem',
              }}
            >
              <i className="bi bi-person-fill" />
            </div>
            <h5 className="fw-bold mb-1" style={{ color: 'var(--m-onSurface)' }}>
              {formData.nombre}
            </h5>
            <div className="mb-2">
              <StatusBadge status={rol} />
            </div>
            <span className="text-muted small">@{usuario}</span>

            <div className="mt-4 pt-3 border-top border-secondary border-opacity-25 text-start small">
              <div className="mb-2">
                <span className="text-muted">DNI: </span>
                <strong style={{ color: 'var(--m-onSurface)' }}>{formData.dni}</strong>
              </div>
              <div className="mb-2">
                <span className="text-muted">Fecha Nacimiento: </span>
                <strong style={{ color: 'var(--m-onSurface)' }}>{formData.fechaNacimiento}</strong>
              </div>
              <div>
                <span className="text-muted">Carrera: </span>
                <strong style={{ color: 'var(--m-onSurface)' }}>{formData.carrera}</strong>
              </div>
            </div>
          </Card>
        </Col>

        <Col lg={8}>
          <Card className="border-0 " style={{ backgroundColor: 'var(--m-surface)' }}>
            <Card.Header
              className="p-3 border-bottom border-secondary border-opacity-25"
              style={{ backgroundColor: 'var(--m-surfaceVariant)' }}
            >
              <strong style={{ color: 'var(--m-onSurface)' }}>Información de Contacto y Residencia</strong>
            </Card.Header>
            <Card.Body className="p-4">
              <Form onSubmit={handleSubmit}>
                <Row className="g-3">
                  <Col md={6}>
                    <Form.Group controlId="perfilNombre">
                      <Form.Label className="small fw-semibold">Nombre Completo</Form.Label>
                      <Form.Control
                        type="text"
                        value={formData.nombre}
                        disabled={!isEditing}
                        onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                        style={{
                          backgroundColor: isEditing ? 'var(--m-surfaceVariant)' : 'var(--m-surface)',
                          color: 'var(--m-onSurface)',
                        }}
                      />
                    </Form.Group>
                  </Col>

                  <Col md={6}>
                    <Form.Group controlId="perfilEmail">
                      <Form.Label className="small fw-semibold">Correo Electrónico Institucional</Form.Label>
                      <Form.Control
                        type="email"
                        value={formData.email}
                        disabled
                        style={{ backgroundColor: 'var(--m-surface)', color: 'var(--m-onSurface)' }}
                      />
                    </Form.Group>
                  </Col>

                  <Col md={6}>
                    <Form.Group controlId="perfilTelefono">
                      <Form.Label className="small fw-semibold">Teléfono de Contacto</Form.Label>
                      <Form.Control
                        type="text"
                        value={formData.telefono}
                        disabled={!isEditing}
                        onChange={(e) => setFormData({ ...formData, telefono: e.target.value })}
                        style={{
                          backgroundColor: isEditing ? 'var(--m-surfaceVariant)' : 'var(--m-surface)',
                          color: 'var(--m-onSurface)',
                        }}
                      />
                    </Form.Group>
                  </Col>

                  <Col md={6}>
                    <Form.Group controlId="perfilDomicilio">
                      <Form.Label className="small fw-semibold">Domicilio</Form.Label>
                      <Form.Control
                        type="text"
                        value={formData.domicilio}
                        disabled={!isEditing}
                        onChange={(e) => setFormData({ ...formData, domicilio: e.target.value })}
                        style={{
                          backgroundColor: isEditing ? 'var(--m-surfaceVariant)' : 'var(--m-surface)',
                          color: 'var(--m-onSurface)',
                        }}
                      />
                    </Form.Group>
                  </Col>

                    {isEditing && (
                      <Col md={12} className="d-flex justify-content-end mt-4">
                        <Button variant="primary" type="submit" size="sm" className="px-4" disabled={saving}>
                          {saving ? 'Guardando…' : 'Guardar Modificaciones'}
                        </Button>
                      </Col>
                    )}
                </Row>
              </Form>
            </Card.Body>
          </Card>
        </Col>
      </Row>
    </div>
  )
}

export default MisDatosPage
