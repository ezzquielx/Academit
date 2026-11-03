import { useState, useEffect } from 'react'
import { Alert, Badge } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import { useAuth } from '../../context/AuthContext'
import { getDocumentacion } from '../../api/documentacion'
import { ETIQUETAS_TIPO_DOCUMENTACION } from '../../config/etiquetas'

export const DocumentacionPage = () => {
  const { user } = useAuth()
  const [docs, setDocs] = useState([])
  const alumnoId = user?.idAlumno || 1

  useEffect(() => {
    getDocumentacion(alumnoId).then(setDocs).catch(() => {})
  }, [])

  const columns = [
    {
      key: 'tipo',
      label: 'Documento Requerido',
      render: (row) => ETIQUETAS_TIPO_DOCUMENTACION[row.tipo] || row.tipo,
    },
    {
      key: 'obligatorio',
      label: 'Carácter',
      render: (row) => (
        <Badge bg={row.obligatorio ? 'danger' : 'secondary'}>
          {row.obligatorio ? 'Obligatorio' : 'Opcional'}
        </Badge>
      ),
    },
    {
      key: 'estado',
      label: 'Estado de Presentación',
      render: (row) => <StatusBadge status={row.estado} />,
    },
    {
      key: 'fechaEntrega',
      label: 'Fecha de Recepción',
      render: (row) => row.fechaEntrega || <span className="text-muted small">No presentado</span>,
    },
    { key: 'observaciones', label: 'Observaciones de Legajo' },
  ]

  const pendientesCount = docs.filter((d) => d.estado === 'PENDIENTE').length

  return (
    <div>
      <PageHeader
        title="Documentación Académica y Legal"
        subtitle="Legajo personal: estado de entrega de documentación respaldatoria obligatoria"
        icon="bi-folder-check"
      />

      {pendientesCount > 0 ? (
        <Alert variant="warning" className="small mb-4 d-flex align-items-center gap-2">
          <i className="bi bi-exclamation-triangle-fill fs-5" />
          <div>
            <strong>Documentación pendiente de entrega:</strong> Posee {pendientesCount} documento(s) pendiente(s) de presentación física en el departamento de Alumnos.
          </div>
        </Alert>
      ) : (
        <Alert variant="success" className="small mb-4 d-flex align-items-center gap-2">
          <i className="bi bi-check-circle-fill fs-5" />
          <div>Legajo al día: todos los documentos requeridos han sido entregados y certificados.</div>
        </Alert>
      )}

      <DataTable
        columns={columns}
        data={docs}
        keyField="idDocumento"
        searchPlaceholder="Buscar documento..."
        searchKeys={['tipo', 'observaciones', 'estado']}
      />
    </div>
  )
}

export default DocumentacionPage
