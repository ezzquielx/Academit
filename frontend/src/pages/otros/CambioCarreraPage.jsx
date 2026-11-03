import { useState, useEffect } from 'react'
import { Form, Col } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import SolicitudForm from '../../components/SolicitudForm'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { getSolicitudes, createSolicitud } from '../../api/solicitudes'
import { ETIQUETAS_TIPO_SOLICITUD } from '../../config/etiquetas'

export const CambioCarreraPage = () => {
  const { user } = useAuth()
  const { showToast } = useToast()
  const [solicitudes, setSolicitudes] = useState([])
  const [carreraDestino, setCarreraDestino] = useState('Tecnicatura Superior en Ciencia de Datos e IA')
  const [loading, setLoading] = useState(false)
  const alumnoId = user?.idAlumno || 1

  const cargar = async () => {
    try {
      const data = await getSolicitudes({ idAlumno: alumnoId, tipo: 'CAMBIO_CARRERA' })
      setSolicitudes(data)
    } catch {
      showToast('Error al cargar trámites', 'danger')
    }
  }

  useEffect(() => {
    cargar()
  }, [])

  const handleSubmit = async (formData) => {
    setLoading(true)
    try {
      await createSolicitud({
        idAlumno: alumnoId,
        tipo: 'CAMBIO_CARRERA',
        motivo: `[Destino: ${carreraDestino}] ${formData.motivo}`,
        observaciones: formData.observaciones,
      })
      showToast('Solicitud de cambio de carrera remitida a Rectoría', 'success')
      cargar()
    } catch (err) {
      showToast(err.message || 'Error al solicitar cambio', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    {
      key: 'tipo',
      label: 'Trámite',
      render: (row) => ETIQUETAS_TIPO_SOLICITUD[row.tipo] || row.tipo,
    },
    { key: 'fechaSolicitud', label: 'Fecha Solicitud' },
    { key: 'motivo', label: 'Fundamentación' },
    {
      key: 'estado',
      label: 'Estado',
      render: (row) => <StatusBadge status={row.estado} />,
    },
    {
      key: 'respuesta',
      label: 'Dictamen de Coordinación',
      render: (row) => <span className="small text-muted">{row.respuesta || 'En consideración'}</span>,
    },
  ]

  return (
    <div>
      <PageHeader
        title="Solicitud de Cambio de Carrera"
        subtitle="Petición formal de pase de plan o cambio de carrera ante la Secretaría Académica"
        icon="bi-arrow-left-right"
      />

      <div
        className="p-3 mb-4 rounded border"
        style={{
          backgroundColor: 'var(--m-surfaceVariant)',
          color: 'var(--m-onSurface)',
          borderColor: 'var(--m-outline)',
        }}
      >
        <span className="small text-muted text-uppercase fw-semibold">Carrera de Origen Actual</span>
        <div className="fw-bold fs-6 mt-1">
          Tecnicatura Superior en Desarrollo de Software (Plan 2024)
        </div>
      </div>

      <SolicitudForm
        tipoPredefinido="CAMBIO_CARRERA"
        extraFields={
          <Col md={12}>
            <Form.Group controlId="carreraDestino">
              <Form.Label className="small fw-semibold" style={{ color: 'var(--m-onSurface)' }}>
                Carrera de Destino Solicitada <span className="text-danger">*</span>
              </Form.Label>
              <Form.Select
                value={carreraDestino}
                onChange={(e) => setCarreraDestino(e.target.value)}
                style={{
                  backgroundColor: 'var(--m-surfaceVariant)',
                  color: 'var(--m-onSurface)',
                  borderColor: 'var(--m-outline)',
                }}
              >
                <option value="Tecnicatura Superior en Ciencia de Datos e IA">
                  Tecnicatura Superior en Ciencia de Datos e IA
                </option>
                <option value="Tecnicatura Superior en Ciberseguridad">
                  Tecnicatura Superior en Ciberseguridad
                </option>
                <option value="Tecnicatura Superior en Infraestructura y Redes">
                  Tecnicatura Superior en Infraestructura y Redes
                </option>
              </Form.Select>
            </Form.Group>
          </Col>
        }
        onSubmit={handleSubmit}
        loading={loading}
      />

      <h5 className="fw-bold mb-3" style={{ color: 'var(--m-primary)' }}>
        Mis Solicitudes de Cambio de Carrera
      </h5>

      <DataTable
        columns={columns}
        data={solicitudes}
        keyField="idSolicitud"
        searchPlaceholder="Buscar en solicitudes de cambio..."
        searchKeys={['tipo', 'motivo', 'estado']}
      />
    </div>
  )
}

export default CambioCarreraPage
