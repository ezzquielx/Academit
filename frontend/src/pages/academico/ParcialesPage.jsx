import { useState, useEffect } from 'react'
import { Card, Row, Col, Badge } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import { useAuth } from '../../context/AuthContext'
import { getCalificaciones } from '../../api/calificaciones'
import { ETIQUETAS_VACIO } from '../../config/etiquetas'

export const ParcialesPage = () => {
  const { idAlumno } = useAuth()
  const [calificaciones, setCalificaciones] = useState([])
  const alumnoId = idAlumno

  useEffect(() => {
    if (!alumnoId) return
    getCalificaciones({ idAlumno: alumnoId, tipo: 'PARCIAL' }).then(setCalificaciones).catch(() => {})
  }, [alumnoId])

  const promedioParciales = calificaciones.length > 0
    ? (calificaciones.reduce((acc, c) => acc + c.nota, 0) / calificaciones.length).toFixed(1)
    : '0.0'

  const columns = [
    { key: 'materiaNombre', label: 'Materia' },
    { key: 'evaluacionNombre', label: 'Instancia' },
    { key: 'fecha', label: 'Fecha' },
    {
      key: 'nota',
      label: 'Nota Obtenida',
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
      label: 'Condición',
      render: (row) => <StatusBadge status={row.condicion} />,
    },
    { key: 'observacion', label: 'Devolución Docente' },
  ]

  return (
    <div>
      <PageHeader
        title="Exámenes Parciales"
        subtitle="Registro oficial de evaluaciones parciales y notas de cursada"
        icon="bi-journal-text"
      />

      <Row className="g-3 mb-4">
        <Col sm={6} md={4}>
          <Card className="border-0 " style={{ backgroundColor: 'var(--m-surface)' }}>
            <Card.Body className="p-3">
              <span className="text-muted small text-uppercase fw-semibold">Promedio de Parciales</span>
              <h3 className="fw-bold mt-1 mb-0" style={{ color: 'var(--m-primary)' }}>
                {promedioParciales} <small className="fs-6 text-muted">/ 10</small>
              </h3>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} md={4}>
          <Card className="border-0 " style={{ backgroundColor: 'var(--m-surface)' }}>
            <Card.Body className="p-3">
              <span className="text-muted small text-uppercase fw-semibold">Parciales Rendidos</span>
              <h3 className="fw-bold mt-1 mb-0" style={{ color: 'var(--m-onSurface)' }}>
                {calificaciones.length}
              </h3>
            </Card.Body>
          </Card>
        </Col>
        <Col sm={6} md={4}>
          <Card className="border-0 " style={{ backgroundColor: 'var(--m-surface)' }}>
            <Card.Body className="p-3">
              <span className="text-muted small text-uppercase fw-semibold">Aprobados (Nota &gt;= 4)</span>
              <h3 className="fw-bold mt-1 mb-0 text-success">
                {calificaciones.filter((c) => c.nota >= 4).length}
              </h3>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      <DataTable
        columns={columns}
        data={calificaciones}
        keyField="idCalificacion"
        searchPlaceholder="Buscar por materia o instancia..."
        searchKeys={['materiaNombre', 'evaluacionNombre', 'observacion']}
        emptyTitle={ETIQUETAS_VACIO.SIN_MATERIAS}
        emptyDescription={ETIQUETAS_VACIO.SIN_MATERIAS_DETALLE}
      />
    </div>
  )
}

export default ParcialesPage
