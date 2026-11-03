import { useState, useEffect } from 'react'
import PageHeader from '../../components/PageHeader'
import SolicitudForm from '../../components/SolicitudForm'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { getSolicitudes, createSolicitud } from '../../api/solicitudes'
import { ETIQUETAS_TIPO_SOLICITUD } from '../../config/etiquetas'

export const PasantiasPage = () => {
  const { user } = useAuth()
  const { showToast } = useToast()
  const [solicitudes, setSolicitudes] = useState([])
  const [loading, setLoading] = useState(false)
  const alumnoId = user?.idAlumno || 1

  const cargar = async () => {
    try {
      const data = await getSolicitudes({ idAlumno: alumnoId, tipo: 'PASANTIA' })
      setSolicitudes(data)
    } catch {
      showToast('Error al cargar solicitudes de pasantías', 'danger')
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
        tipo: 'PASANTIA',
        motivo: formData.motivo,
        observaciones: formData.observaciones,
      })
      showToast('Postulación a pasantías registrada con éxito', 'success')
      cargar()
    } catch (err) {
      showToast(err.message || 'Error al postularse', 'danger')
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
    { key: 'fechaSolicitud', label: 'Fecha Postulación' },
    { key: 'motivo', label: 'Perfil / Empresa / Motivación' },
    {
      key: 'estado',
      label: 'Estado Postulación',
      render: (row) => <StatusBadge status={row.estado} />,
    },
    {
      key: 'respuesta',
      label: 'Dictamen de Coordinación',
      render: (row) => <span className="small text-muted">{row.respuesta || 'En evaluación por comisión'}</span>,
    },
  ]

  return (
    <div>
      <PageHeader
        title="Inscripción a Pasantías"
        subtitle="Postulación a convenios de pasantías laborales y prácticas pre-profesionales rentadas"
        icon="bi-briefcase"
      />

      <SolicitudForm
        tipoPredefinido="PASANTIA"
        onSubmit={handleSubmit}
        loading={loading}
      />

      <h5 className="fw-bold mb-3" style={{ color: 'var(--m-primary)' }}>
        Mis Postulaciones a Pasantías
      </h5>

      <DataTable
        columns={columns}
        data={solicitudes}
        keyField="idSolicitud"
        searchPlaceholder="Buscar en mis postulaciones..."
        searchKeys={['tipo', 'motivo', 'estado']}
      />
    </div>
  )
}

export default PasantiasPage
