import { useState, useEffect } from 'react'
import PageHeader from '../../components/PageHeader'
import SolicitudForm from '../../components/SolicitudForm'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { getSolicitudes, createSolicitud } from '../../api/solicitudes'

import { ETIQUETAS_TIPO_SOLICITUD, ETIQUETAS_VACIO } from '../../config/etiquetas'
export const ConstanciasPage = () => {
  const { idAlumno } = useAuth()
  const { showToast } = useToast()
  const [solicitudes, setSolicitudes] = useState([])
  const [loading, setLoading] = useState(false)
  const alumnoId = idAlumno

  const cargar = async () => {
    if (!alumnoId) return
    try {
      const data = await getSolicitudes({ idAlumno: alumnoId, tipo: 'CONSTANCIA' })
      setSolicitudes(data)
    } catch {
      showToast('Error al cargar solicitudes', 'danger')
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
        tipo: 'CONSTANCIA',
        motivo: formData.motivo,
        observaciones: formData.observaciones,
      })
      showToast('Solicitud de constancia enviada exitosamente', 'success')
      cargar()
    } catch (err) {
      showToast(err.message || 'Error al enviar la solicitud', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    {
      key: 'tipo',
      label: 'Tipo de Solicitud',
      render: (row) => ETIQUETAS_TIPO_SOLICITUD[row.tipo] || row.tipo,
    },
    { key: 'fechaSolicitud', label: 'Fecha Solicitud' },
    { key: 'motivo', label: 'Motivo' },
    {
      key: 'estado',
      label: 'Estado',
      render: (row) => <StatusBadge status={row.estado} />,
    },
    {
      key: 'respuesta',
      label: 'Respuesta / Descarga',
      render: (row) =>
        row.estado === 'APROBADA' ? (
          <span className="text-success small">
            <i className="bi bi-file-earmark-pdf me-1" /> Lista para descargar
          </span>
        ) : (
          <span className="text-muted small">{row.respuesta || 'En trámite'}</span>
        ),
    },
  ]

  return (
    <div>
      <PageHeader
        title="Solicitud de Constancias"
        subtitle="Constancia de alumno regular, constancia de examen y promedio académico"
        icon="bi-file-earmark-text"
      />

      <SolicitudForm
        tipoPredefinido="CONSTANCIA"
        onSubmit={handleSubmit}
        loading={loading}
      />

      <h5 className="fw-bold mb-3" style={{ color: 'var(--m-primary)' }}>
        Mis Solicitudes de Constancias
      </h5>

      <DataTable
        columns={columns}
        data={solicitudes}
        keyField="idSolicitud"
        searchPlaceholder="Buscar en mis constancias..."
        searchKeys={['tipo', 'motivo', 'estado']}
        emptyTitle={ETIQUETAS_VACIO.SIN_SOLICITUDES}
        emptyDescription={ETIQUETAS_VACIO.SIN_SOLICITUDES_DETALLE}
      />
    </div>
  )
}

export default ConstanciasPage
