import { useState, useEffect } from 'react'
import PageHeader from '../../components/PageHeader'
import SolicitudForm from '../../components/SolicitudForm'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { getSolicitudes, createSolicitud } from '../../api/solicitudes'
import { ETIQUETAS_TIPO_SOLICITUD } from '../../config/etiquetas'

export const SolicitudTituloPage = () => {
  const { idAlumno } = useAuth()
  const { showToast } = useToast()
  const [solicitudes, setSolicitudes] = useState([])
  const [loading, setLoading] = useState(false)
  const alumnoId = idAlumno

  const cargar = async () => {
    if (!alumnoId) return
    try {
      const data = await getSolicitudes({ idAlumno: alumnoId, tipo: 'TITULO_INTERMEDIO' })
      setSolicitudes(data)
    } catch {
      showToast('Error al cargar trámites de título', 'danger')
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
        tipo: 'TITULO_INTERMEDIO',
        motivo: formData.motivo,
        observaciones: formData.observaciones,
      })
      showToast('Inicio de trámite de título registrado con éxito', 'success')
      cargar()
    } catch (err) {
      showToast(err.message || 'Error al iniciar trámite', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    {
      key: 'tipo',
      label: 'Tipo de Trámite',
      render: (row) => ETIQUETAS_TIPO_SOLICITUD[row.tipo] || row.tipo,
    },
    { key: 'fechaSolicitud', label: 'Fecha de Inicio' },
    { key: 'motivo', label: 'Detalle del Trámite' },
    {
      key: 'estado',
      label: 'Estado del Trámite',
      render: (row) => <StatusBadge status={row.estado} />,
    },
    {
      key: 'respuesta',
      label: 'Dictamen de Títulos y Egresados',
      render: (row) => <span className="small text-muted">{row.respuesta || 'Verificando analítico y libre deuda'}</span>,
    },
  ]

  return (
    <div>
      <PageHeader
        title="Solicitud de Título"
        subtitle="Inicio de trámite de titulación intermedia o título final oficial"
        icon="bi-award"
      />

      <SolicitudForm
        tipoPredefinido="TITULO_INTERMEDIO"
        onSubmit={handleSubmit}
        loading={loading}
      />

      <h5 className="fw-bold mb-3" style={{ color: 'var(--m-primary)' }}>
        Mis Trámites de Titulación
      </h5>

      <DataTable
        columns={columns}
        data={solicitudes}
        keyField="idSolicitud"
        searchPlaceholder="Buscar en trámites de título..."
        searchKeys={['tipo', 'motivo', 'estado']}
      />
    </div>
  )
}

export default SolicitudTituloPage
