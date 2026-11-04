import { useState, useEffect } from 'react'
import { Button, Form, ButtonGroup } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import FormModal from '../../components/FormModal'
import { useToast } from '../../context/ToastContext'
import { getSolicitudes, resolverSolicitud } from '../../api/solicitudes'
import { ETIQUETAS_TIPO_SOLICITUD } from '../../config/etiquetas'

export const BandejaSolicitudesPage = () => {
  const { showToast } = useToast()
  const [solicitudes, setSolicitudes] = useState([])
  const [filtroEstado, setFiltroEstado] = useState('TODOS')
  const [resolviendo, setResolviendo] = useState(null) // { solicitud, nuevoEstado }
  const [respuestaTexto, setRespuestaTexto] = useState('')
  const [loading, setLoading] = useState(false)

  const cargar = async () => {
    try {
      const data = await getSolicitudes()
      setSolicitudes(data)
    } catch {
      showToast('Error al cargar la bandeja de solicitudes', 'danger')
    }
  }

  useEffect(() => {
    cargar()
  }, [])

  const handleOpenResolver = (solicitud, nuevoEstado) => {
    setResolviendo({ solicitud, nuevoEstado })
    setRespuestaTexto(
      nuevoEstado === 'APROBADA'
        ? 'Solicitud evaluada y aprobada satisfactoriamente.'
        : 'Solicitud desestimada por no cumplir con los requisitos reglamentarios.'
    )
  }

  const handleConfirmResolucion = async (e) => {
    e.preventDefault()
    if (!resolviendo) return

    setLoading(true)
    try {
      await resolverSolicitud(resolviendo.solicitud.idSolicitud, {
        estado: resolviendo.nuevoEstado,
        observacion: respuestaTexto,
      })
      showToast(
        `Solicitud ${resolviendo.nuevoEstado === 'APROBADA' ? 'aprobada' : 'rechazada'} exitosamente`,
        resolviendo.nuevoEstado === 'APROBADA' ? 'success' : 'warning'
      )
      setResolviendo(null)
      cargar()
    } catch (err) {
      showToast(err.message || 'Error al procesar la solicitud', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const solicitudesFiltradas = solicitudes.filter((s) => {
    if (filtroEstado === 'TODOS') return true
    return s.estado === filtroEstado
  })

  const columns = [
    { key: 'idSolicitud', label: '# ID', width: '70px' },
    { key: 'alumnoNombre', label: 'Alumno' },
    {
      key: 'tipo',
      label: 'Tipo de Solicitud',
      render: (row) => ETIQUETAS_TIPO_SOLICITUD[row.tipo] || row.tipo,
    },
    { key: 'fechaSolicitud', label: 'Fecha' },
    { key: 'motivo', label: 'Motivo / Detalle' },
    {
      key: 'estado',
      label: 'Estado',
      render: (row) => <StatusBadge status={row.estado} />,
    },
    { key: 'respuesta', label: 'Resolución' },
    {
      key: 'acciones',
      label: 'Acciones',
      align: 'right',
      render: (row) => (
        <div className="d-flex justify-content-end gap-1">
          {row.estado === 'PENDIENTE' || row.estado === 'EN_PROCESO' ? (
            <>
              <Button
                variant="success"
                size="sm"
                onClick={() => handleOpenResolver(row, 'APROBADA')}
                title="Aprobar solicitud"
                aria-label="Aprobar solicitud"
              >
                <i className="bi bi-check-lg" />
              </Button>
              <Button
                variant="outline-danger"
                size="sm"
                onClick={() => handleOpenResolver(row, 'RECHAZADA')}
                title="Rechazar solicitud"
                aria-label="Rechazar solicitud"
              >
                <i className="bi bi-x-lg" />
              </Button>
            </>
          ) : (
            <span className="text-muted small fst-italic">Resuelta</span>
          )}
        </div>
      ),
    },
  ]

  return (
    <div>
      <PageHeader
        title="Bandeja de Solicitudes de Alumnos"
        subtitle="Administración y resolución de constancias, títulos, equivalencias y trámites estudiantiles"
        icon="bi-inbox"
      />

      {/* Filtros rápidos por estado */}
      <div className="mb-3 d-flex flex-wrap align-items-center gap-2">
        <span className="small text-muted fw-semibold me-2">Filtrar por estado:</span>
        <ButtonGroup size="sm">
          {['TODOS', 'PENDIENTE', 'EN_PROCESO', 'APROBADA', 'RECHAZADA'].map((est) => (
            <Button
              key={est}
              variant={filtroEstado === est ? 'primary' : 'outline-secondary'}
              onClick={() => setFiltroEstado(est)}
            >
              {est === 'TODOS' ? 'Todas' : est}
            </Button>
          ))}
        </ButtonGroup>
      </div>

      <DataTable
        columns={columns}
        data={solicitudesFiltradas}
        keyField="idSolicitud"
        searchPlaceholder="Buscar por alumno, tipo o motivo..."
        searchKeys={['alumnoNombre', 'tipo', 'motivo', 'estado']}
      />

      <FormModal
        show={Boolean(resolviendo)}
        onHide={() => setResolviendo(null)}
        onSubmit={handleConfirmResolucion}
        title={
          resolviendo?.nuevoEstado === 'APROBADA'
            ? `Aprobar Solicitud #${resolviendo?.solicitud?.idSolicitud}`
            : `Rechazar Solicitud #${resolviendo?.solicitud?.idSolicitud}`
        }
        submitText={resolviendo?.nuevoEstado === 'APROBADA' ? 'Confirmar Aprobación' : 'Confirmar Rechazo'}
        loading={loading}
      >
        <p className="small mb-3">
          Estudiante: <strong>{resolviendo?.solicitud?.alumnoNombre}</strong> <br />
          Tipo de Solicitud: <strong>{resolviendo?.solicitud?.tipo}</strong> <br />
          Motivo presentado: <em>"{resolviendo?.solicitud?.motivo}"</em>
        </p>

        <Form.Group controlId="respuestaTexto">
          <Form.Label className="small fw-semibold">
            Dictamen o Devolución al Alumno <span className="text-danger">*</span>
          </Form.Label>
          <Form.Control
            as="textarea"
            rows={3}
            value={respuestaTexto}
            onChange={(e) => setRespuestaTexto(e.target.value)}
            required
            style={{
              backgroundColor: 'var(--m-surfaceVariant)',
              color: 'var(--m-onSurface)',
              borderColor: 'var(--m-outline)',
            }}
          />
        </Form.Group>
      </FormModal>
    </div>
  )
}

export default BandejaSolicitudesPage
