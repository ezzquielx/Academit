import { useState, useEffect } from 'react'
import { Card, Button, Form, Alert } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import StatusBadge from '../../components/StatusBadge'
import ConfirmModal from '../../components/ConfirmModal'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { cancelarMatricula, getMatriculas } from '../../api/matriculas'

export const CancelarMatriculaPage = () => {
  const { user } = useAuth()
  const { showToast } = useToast()
  const [matricula, setMatricula] = useState(null)
  const [motivo, setMotivo] = useState('')
  const [showConfirm, setShowConfirm] = useState(false)
  const [loading, setLoading] = useState(false)
  const alumnoId = user?.idAlumno || 1

  const cargar = async () => {
    try {
      const mats = await getMatriculas()
      const m = mats.find((item) => item.idAlumno === alumnoId)
      if (m) setMatricula(m)
    } catch {
      showToast('Error al cargar matrícula', 'danger')
    }
  }

  useEffect(() => {
    cargar()
  }, [])

  const handleConfirmCancelacion = async () => {
    setLoading(true)
    try {
      const idMat = matricula ? matricula.idMatricula : alumnoId
      await cancelarMatricula(idMat, motivo)
      showToast('La matrícula ha sido formalmente cancelada (RF27)', 'warning')
      setShowConfirm(false)
      cargar()
    } catch (err) {
      showToast(err.message || 'Error al cancelar la matrícula', 'danger')
    } finally {
      setLoading(false)
    }
  }

  return (
    <div>
      <PageHeader
        title="Cancelación de Matrícula"
        subtitle="Baja definitiva de la cursada institucional (Requisito Funcional RF27)"
        icon="bi-x-circle"
      />

      <Alert variant="danger" className="mb-4">
        <div className="d-flex align-items-start gap-2">
          <i className="bi bi-exclamation-octagon-fill fs-4 mt-1" />
          <div>
            <h5 className="alert-heading fw-bold mb-1">¡Advertencia Importante (RF27)!</h5>
            <p className="small mb-0">
              Al confirmar esta solicitud, tu matrícula del ciclo lectivo 2026 pasará irreversiblemente al estado <strong>CANCELADA</strong>. Perderás la condición de estudiante regular, el acceso al aula virtual y el derecho a rendir exámenes finales hasta una futura reinscripción formal.
            </p>
          </div>
        </div>
      </Alert>

      <Card className="border-0 " style={{ backgroundColor: 'var(--m-surface)' }}>
        <Card.Header
          className="p-3 border-bottom border-secondary border-opacity-25 d-flex justify-content-between align-items-center"
          style={{ backgroundColor: 'var(--m-surfaceVariant)' }}
        >
          <strong style={{ color: 'var(--m-onSurface)' }}>Estado de Matrícula Actual</strong>
          <StatusBadge status={matricula?.estado || 'ACTIVA'} />
        </Card.Header>
        <Card.Body className="p-4">
          <div className="mb-3">
            <span className="text-muted small">Alumno/a: </span>
            <strong style={{ color: 'var(--m-onSurface)' }}>Lucía Benítez (ALU-1001)</strong>
          </div>
          <div className="mb-3">
            <span className="text-muted small">Ciclo Lectivo: </span>
            <strong style={{ color: 'var(--m-onSurface)' }}>2026</strong>
          </div>
          <div className="mb-4">
            <span className="text-muted small">Estado Actual: </span>
            <strong style={{ color: matricula?.estado === 'CANCELADA' ? 'var(--m-error)' : 'var(--m-secondary)' }}>
              {matricula?.estado || 'ACTIVA'}
            </strong>
          </div>

          {matricula?.estado === 'CANCELADA' ? (
            <Alert variant="secondary" className="mb-0">
              <i className="bi bi-info-circle me-1" />
              Tu matrícula ya se encuentra en estado <strong>CANCELADA</strong>. No es posible ejecutar nuevas cancelaciones sobre este legajo.
            </Alert>
          ) : (
            <Form onSubmit={(e) => { e.preventDefault(); setShowConfirm(true); }}>
              <Form.Group className="mb-4" controlId="motivoBaja">
                <Form.Label className="small fw-semibold" style={{ color: 'var(--m-onSurface)' }}>
                  Motivo de la Cancelación Voluntaria <span className="text-danger">*</span>
                </Form.Label>
                <Form.Control
                  as="textarea"
                  rows={3}
                  placeholder="Por favor, explicá brevemente la causa de la cancelación..."
                  value={motivo}
                  onChange={(e) => setMotivo(e.target.value)}
                  required
                  style={{
                    backgroundColor: 'var(--m-surfaceVariant)',
                    color: 'var(--m-onSurface)',
                    borderColor: 'var(--m-outline)',
                  }}
                />
              </Form.Group>

              <div className="d-flex justify-content-end">
                <Button
                  variant="danger"
                  type="submit"
                  disabled={!motivo.trim() || loading}
                  className="d-flex align-items-center gap-2"
                >
                  <i className="bi bi-x-circle-fill" />
                  <span>Solicitar Cancelación de Matrícula (RF27)</span>
                </Button>
              </div>
            </Form>
          )}
        </Card.Body>
      </Card>

      <ConfirmModal
        show={showConfirm}
        onHide={() => setShowConfirm(false)}
        onConfirm={handleConfirmCancelacion}
        title="¿Confirmar Cancelación de Matrícula (RF27)?"
        message="¿Confirmás de manera definitiva la baja de tu matrícula? Tu estado pasará de forma inmediata a CANCELADA en el sistema de gestión académica."
        confirmText="Sí, cancelar mi matrícula"
        cancelText="Volver atrás"
        variant="danger"
        loading={loading}
      />
    </div>
  )
}

export default CancelarMatriculaPage
