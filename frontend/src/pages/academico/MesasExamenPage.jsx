import { useState, useEffect } from 'react'
import { Button, Alert, Badge } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import ConfirmModal from '../../components/ConfirmModal'
import { useAuth } from '../../context/AuthContext'
import { useToast } from '../../context/ToastContext'
import { getMesasExamen, inscribirseMesa, darseDeBajaMesa } from '../../api/mesasExamen'
import { ETIQUETAS_TURNO } from '../../config/etiquetas'

export const MesasExamenPage = () => {
  const { rol, idAlumno } = useAuth()
  const { showToast } = useToast()
  const [mesas, setMesas] = useState([])
  const [loading, setLoading] = useState(false)
  const [mesaToBaja, setMesaToBaja] = useState(null)
  const alumnoId = idAlumno

  const cargarMesas = async () => {
    try {
      const data = await getMesasExamen()
      setMesas(data)
    } catch {
      showToast('Error al cargar mesas de examen', 'danger')
    }
  }

  useEffect(() => {
    cargarMesas()
  }, [])

  const handleInscribirse = async (mesa) => {
    setLoading(true)
    try {
      await inscribirseMesa(mesa.idMesa, alumnoId)
      showToast(`Inscripción confirmada para ${mesa.materiaNombre}`, 'success')
      cargarMesas()
    } catch (err) {
      showToast(err.message || 'Error al inscribirse', 'warning')
    } finally {
      setLoading(false)
    }
  }

  const handleConfirmBaja = async () => {
    if (!mesaToBaja) return
    setLoading(true)
    try {
      await darseDeBajaMesa(mesaToBaja.idMesa, alumnoId)
      showToast(`Baja efectuada para ${mesaToBaja.materiaNombre}`, 'info')
      setMesaToBaja(null)
      cargarMesas()
    } catch (err) {
      showToast(err.message || 'Error al darse de baja', 'warning')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    { key: 'materiaNombre', label: 'Materia' },
    { key: 'fecha', label: 'Fecha' },
    { key: 'hora', label: 'Horario' },
    { key: 'turno', label: 'Turno', render: (row) => <Badge bg="secondary">{ETIQUETAS_TURNO[row.turno] || row.turno}</Badge> },
    { key: 'aula', label: 'Aula' },
    { key: 'tribunal', label: 'Tribunal Evaluador' },
    {
      key: 'cupos',
      label: 'Cupo',
      render: (row) => (
        <span>
          {row.inscriptos} / {row.cupoMaximo}
        </span>
      ),
    },
    {
      key: 'estado',
      label: 'Estado Mesa',
      render: (row) => <StatusBadge status={row.estadoMesa || row.estado} />,
    },
    {
      key: 'miEstado',
      label: 'Mi Inscripción',
      render: (row) => {
        const inscripto = Array.isArray(row.alumnosInscriptosIds) && row.alumnosInscriptosIds.includes(alumnoId)
        return inscripto ? (
          <StatusBadge status="CONFIRMADA" />
        ) : (
          <span className="text-muted small">No inscripto</span>
        )
      },
    },
    {
      key: 'acciones',
      label: 'Acciones',
      align: 'right',
      render: (row) => {
        if (rol !== 'ALUMNO') {
          return <small className="text-muted fst-italic">Solo consulta</small>
        }

        const inscripto = Array.isArray(row.alumnosInscriptosIds) && row.alumnosInscriptosIds.includes(alumnoId)
        if (inscripto) {
          return (
            <Button
              variant="outline-danger"
              size="sm"
              disabled={loading || !alumnoId}
              onClick={() => setMesaToBaja(row)}
              title="Darse de baja de la mesa"
              aria-label="Darse de baja de la mesa"
            >
              <i className="bi bi-x-circle" />
            </Button>
          )
        }

        const cupoDisponible = row.inscriptos < row.cupoMaximo
        return (
          <Button
            variant="primary"
            size="sm"
            disabled={loading || !cupoDisponible || !alumnoId}
            onClick={() => handleInscribirse(row)}
            title={cupoDisponible ? 'Inscribirme a la mesa' : 'Sin cupo disponible'}
            aria-label={cupoDisponible ? 'Inscribirme a la mesa' : 'Sin cupo disponible'}
          >
            <i className="bi bi-check2-circle" />
          </Button>
        )
      },
    },
  ]

  return (
    <div>
      <PageHeader
        title="Mesas de Examen Final"
        subtitle="Turno Mayo - Junio 2026 | Inscripción y cancelación de exámenes finales (RF49-RF50)"
        icon="bi-calendar2-check"
      />

      <Alert variant="info" className="d-flex align-items-center gap-2 small mb-4">
        <i className="bi bi-info-circle-fill fs-5" />
        <div>
          <strong>Información sobre el turno de exámenes:</strong> Podés inscribirte hasta 48 horas antes de la fecha establecida. Recordá contar con la correlatividad aprobada y la cuota del mes regularizada.
        </div>
      </Alert>

      <DataTable
        columns={columns}
        data={mesas}
        keyField="idMesa"
        searchPlaceholder="Buscar materia, tribunal o fecha..."
        searchKeys={['materiaNombre', 'tribunal', 'aula', 'fecha']}
      />

      <ConfirmModal
        show={Boolean(mesaToBaja)}
        onHide={() => setMesaToBaja(null)}
        onConfirm={handleConfirmBaja}
        title="¿Darse de baja de la mesa de examen?"
        message={`Estás a punto de cancelar tu inscripción a la mesa de final de "${mesaToBaja?.materiaNombre}" programada para el ${mesaToBaja?.fecha}. Esta acción liberará tu cupo para otros estudiantes.`}
        confirmText="Confirmar baja"
        variant="danger"
        loading={loading}
      />
    </div>
  )
}

export default MesasExamenPage
