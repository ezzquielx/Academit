import { useState, useEffect } from 'react'
import { Card, Row, Col, Badge } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import { useAuth } from '../../context/AuthContext'
import { getCalificaciones } from '../../api/calificaciones'
import { ETIQUETAS_VACIO } from '../../config/etiquetas'

export const TrabajosPracticosPage = () => {
  const { idAlumno } = useAuth()
  const [tps, setTps] = useState([])
  const alumnoId = idAlumno

  useEffect(() => {
    if (!alumnoId) return
    getCalificaciones({ idAlumno: alumnoId, tipo: 'TRABAJO_PRACTICO' }).then(setTps).catch(() => {})
  }, [alumnoId])

  const promedioTps = tps.length > 0
    ? (tps.reduce((acc, c) => acc + c.nota, 0) / tps.length).toFixed(1)
    : '0.0'

  const columns = [
    { key: 'materiaNombre', label: 'Materia' },
    { key: 'evaluacionNombre', label: 'Trabajo Práctico' },
    { key: 'fecha', label: 'Fecha de Entrega' },
    {
      key: 'nota',
      label: 'Calificación',
      render: (row) => (
        <span className="fw-bold fs-6">
          <Badge bg={row.nota >= 7 ? 'success' : row.nota >= 4 ? 'warning' : 'danger'}>
            {row.nota} / 10
          </Badge>
        </span>
      ),
    },
    {
      key: 'condicion',
      label: 'Estado',
      render: (row) => <StatusBadge status={row.condicion} />,
    },
    { key: 'observacion', label: 'Devolución del Docente' },
  ]

  return (
    <div>
      <PageHeader
        title="Trabajos Prácticos e Integradores"
        subtitle="Entregas de proyectos, laboratorios y calificaciones obtenidas"
        icon="bi-file-earmark-code"
      />

      <Row className="g-3 mb-4">
        <Col sm={6} md={4}>
          <Card className="border-0 " style={{ backgroundColor: 'var(--m-surface)' }}>
            <Card.Body className="p-3">
              <span className="text-muted small text-uppercase fw-semibold">Promedio Trabajos Prácticos</span>
              <h3 className="fw-bold mt-1 mb-0" style={{ color: 'var(--m-primary)' }}>
                {promedioTps} <small className="fs-6 text-muted">/ 10</small>
              </h3>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} md={4}>
          <Card className="border-0 " style={{ backgroundColor: 'var(--m-surface)' }}>
            <Card.Body className="p-3">
              <span className="text-muted small text-uppercase fw-semibold">TPs Presentados</span>
              <h3 className="fw-bold mt-1 mb-0" style={{ color: 'var(--m-onSurface)' }}>
                {tps.length}
              </h3>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} md={4}>
          <Card className="border-0 " style={{ backgroundColor: 'var(--m-surface)' }}>
            <Card.Body className="p-3">
              <span className="text-muted small text-uppercase fw-semibold">Cumplimiento</span>
              <h3 className="fw-bold mt-1 mb-0 text-success">
                100%
              </h3>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      <DataTable
        columns={columns}
        data={tps}
        keyField="idCalificacion"
        searchPlaceholder="Buscar por materia o trabajo práctico..."
        searchKeys={['materiaNombre', 'evaluacionNombre', 'observacion']}
        emptyTitle={ETIQUETAS_VACIO.SIN_MATERIAS}
        emptyDescription={ETIQUETAS_VACIO.SIN_MATERIAS_DETALLE}
      />
    </div>
  )
}

export default TrabajosPracticosPage
