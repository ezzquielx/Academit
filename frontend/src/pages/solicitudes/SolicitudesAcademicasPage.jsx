import { useState, useEffect } from 'react'
import PageHeader from '../../components/PageHeader'
import SolicitudForm from '../../components/SolicitudForm'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { getSolicitudes, createSolicitud } from '../../api/solicitudes'
import { ETIQUETAS_TIPO_SOLICITUD } from '../../config/etiquetas'

export const SolicitudesAcademicasPage = () => {
  const { idAlumno } = useAuth()
  const { showToast } = useToast()
  const [solicitudes, setSolicitudes] = useState([])
  const [loading, setLoading] = useState(false)
  const alumnoId = idAlumno

  const cargar = async () => {
    if (!alumnoId) return
    try {
      const data = await getSolicitudes({ idAlumno: alumnoId, tipo: 'ACADEMICA' })
      setSolicitudes(data)
    } catch {
      showToast('Error al cargar solicitudes académicas', 'danger')
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
        tipo: 'ACADEMICA',
        motivo: formData.motivo,
        observaciones: formData.observaciones,
      })
      showToast('Solicitud académica presentada exitosamente', 'success')
      cargar()
    } catch (err) {
      showToast(err.message || 'Error al enviar solicitud', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    {
      key: 'tipo',
      label: 'Tipo de Pedido',
      render: (row) => ETIQUETAS_TIPO_SOLICITUD[row.tipo] || row.tipo,
    },
    { key: 'fechaSolicitud', label: 'Fecha Presentación' },
    { key: 'motivo', label: 'Motivo / Argumentación' },
    {
      key: 'estado',
      label: 'Estado',
      render: (row) => <StatusBadge status={row.estado} />,
    },
    {
      key: 'respuesta',
      label: 'Resolución de Secretaría',
      render: (row) => <span className="small text-muted">{row.respuesta || 'En evaluación'}</span>,
    },
  ]

  return (
    <div>
      <PageHeader
        title="Solicitudes Académicas Generales"
        subtitle="Pedidos de excepción de correlatividad, revisión de notas, prórroga y justificaciones"
        icon="bi-send"
      />

      <SolicitudForm
        tipoPredefinido="ACADEMICA"
        onSubmit={handleSubmit}
        loading={loading}
      />

      <h5 className="fw-bold mb-3" style={{ color: 'var(--m-primary)' }}>
        Mis Solicitudes Académicas
      </h5>

      <DataTable
        columns={columns}
        data={solicitudes}
        keyField="idSolicitud"
        searchPlaceholder="Buscar en solicitudes..."
        searchKeys={['tipo', 'motivo', 'estado']}
      />
    </div>
  )
}

export default SolicitudesAcademicasPage
