import { useState, useEffect } from 'react'
import PageHeader from '../../components/PageHeader'
import SolicitudForm from '../../components/SolicitudForm'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { getSolicitudes, createSolicitud } from '../../api/solicitudes'
import { ETIQUETAS_TIPO_SOLICITUD } from '../../config/etiquetas'

export const ReincorporacionPage = () => {
  const { idAlumno } = useAuth()
  const { showToast } = useToast()
  const [solicitudes, setSolicitudes] = useState([])
  const [loading, setLoading] = useState(false)
  const alumnoId = idAlumno

  const cargar = async () => {
    if (!alumnoId) return
    try {
      const data = await getSolicitudes({ idAlumno: alumnoId, tipo: 'REINCORPORACION' })
      setSolicitudes(data)
    } catch {
      showToast('Error al cargar trámites de reincorporación', 'danger')
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
        tipo: 'REINCORPORACION',
        motivo: formData.motivo,
        observaciones: formData.observaciones,
      })
      showToast('Solicitud de reincorporación enviada a Secretaría Académica', 'success')
      cargar()
    } catch (err) {
      showToast(err.message || 'Error al solicitar reincorporación', 'danger')
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
    { key: 'motivo', label: 'Motivo de Pérdida de Condición / Justificación' },
    {
      key: 'estado',
      label: 'Estado',
      render: (row) => <StatusBadge status={row.estado} />,
    },
    {
      key: 'respuesta',
      label: 'Resolución',
      render: (row) => <span className="small text-muted">{row.respuesta || 'En evaluación por el Consejo Directivo'}</span>,
    },
  ]

  return (
    <div>
      <PageHeader
        title="Solicitud de Reincorporación"
        subtitle="Pedido de reingreso al padrón activo por vencimiento de regularidad o baja temporal"
        icon="bi-person-plus"
      />

      <SolicitudForm
        tipoPredefinido="REINCORPORACION"
        onSubmit={handleSubmit}
        loading={loading}
      />

      <h5 className="fw-bold mb-3" style={{ color: 'var(--m-primary)' }}>
        Mis Solicitudes de Reincorporación
      </h5>

      <DataTable
        columns={columns}
        data={solicitudes}
        keyField="idSolicitud"
        searchPlaceholder="Buscar en mis solicitudes de reincorporación..."
        searchKeys={['tipo', 'motivo', 'estado']}
      />
    </div>
  )
}

export default ReincorporacionPage
