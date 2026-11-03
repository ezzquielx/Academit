import { useState, useEffect } from 'react'
import { Card, Row, Col, ProgressBar } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import { useAuth } from '../../context/AuthContext'
import { getAsistencias } from '../../api/asistencias'

export const AsistenciasAlumnoPage = () => {
  const { user } = useAuth()
  const [asistencias, setAsistencias] = useState([])
  const alumnoId = user?.idAlumno || 1 // Alumno de sesión o referencia

  useEffect(() => {
    getAsistencias({ idAlumno: alumnoId }).then(setAsistencias).catch(() => {})
  }, [alumnoId])

  const totalClases = asistencias.length
  const presentes = asistencias.filter((a) => a.estado === 'PRESENTE').length
  const tardanzas = asistencias.filter((a) => a.estado === 'TARDANZA').length
  const justificados = asistencias.filter((a) => a.estado === 'JUSTIFICADO').length
  const ausentes = asistencias.filter((a) => a.estado === 'AUSENTE').length

  // Asistencia efectiva = (Presentes + Justificados + Tardanzas * 0.5) / Total
  const porcentaje = totalClases > 0
    ? Math.round(((presentes + justificados + tardanzas * 0.5) / totalClases) * 100)
    : 100

  const columns = [
    { key: 'fecha', label: 'Fecha' },
    { key: 'materiaNombre', label: 'Materia' },
    {
      key: 'estado',
      label: 'Estado de Asistencia',
      render: (row) => <StatusBadge status={row.estado} />,
    },
    {
      key: 'detalle',
      label: 'Observación',
      render: (row) => {
        if (row.estado === 'PRESENTE') return <span style={{ color: 'var(--suave)', fontSize: '13.5px' }}>Puntual</span>
        if (row.estado === 'TARDANZA') return <span style={{ color: 'var(--acento)', fontSize: '13.5px' }}>Demora de 15 min</span>
        if (row.estado === 'JUSTIFICADO') return <span style={{ color: 'var(--suave)', fontSize: '13.5px' }}>Certificado médico</span>
        return <span style={{ color: 'var(--error)', fontSize: '13.5px' }}>Falta injustificada</span>
      },
    },
  ]

  return (
    <div>
      <PageHeader
        title="Historial de Asistencias"
        subtitle="Registro de presentismo en clases presenciales y porcentaje acumulado"
      />

      <Row className="g-3 mb-4">
        <Col md={6} lg={4}>
          <Card className="h-100" style={{ backgroundColor: 'var(--papel)', border: '1px solid var(--linea)' }}>
            <Card.Body className="p-3">
              <span style={{ color: 'var(--suave)', fontSize: '13px' }}>Porcentaje de Asistencia</span>
              <div className="d-flex align-items-baseline gap-2 mt-1 mb-2">
                <h3 className="fw-bold mb-0 num" style={{ color: porcentaje >= 75 ? 'var(--primario)' : 'var(--error)' }}>
                  {porcentaje}%
                </h3>
                <small style={{ color: 'var(--suave)' }}>(Mínimo requerido: 75%)</small>
              </div>
              <ProgressBar
                now={porcentaje}
                style={{ height: '6px', backgroundColor: 'var(--linea)' }}
              />
            </Card.Body>
          </Card>
        </Col>

        <Col md={6} lg={8}>
          <Card className="h-100" style={{ backgroundColor: 'var(--papel)', border: '1px solid var(--linea)' }}>
            <Card.Body className="p-3">
              <span style={{ color: 'var(--suave)', fontSize: '13px' }}>Desglose de Clases ({totalClases} registradas)</span>
              <Row className="mt-2 text-center g-2">
                <Col xs={3}>
                  <div className="p-2" style={{ backgroundColor: 'var(--hover)', border: '1px solid var(--linea)', borderRadius: '3px' }}>
                    <div className="fw-bold fs-5 num" style={{ color: 'var(--primario)' }}>{presentes}</div>
                    <small style={{ color: 'var(--suave)' }}>Presentes</small>
                  </div>
                </Col>
                <Col xs={3}>
                  <div className="p-2" style={{ backgroundColor: 'var(--hover)', border: '1px solid var(--linea)', borderRadius: '3px' }}>
                    <div className="fw-bold fs-5 num" style={{ color: 'var(--acento)' }}>{tardanzas}</div>
                    <small style={{ color: 'var(--suave)' }}>Tardanzas</small>
                  </div>
                </Col>
                <Col xs={3}>
                  <div className="p-2" style={{ backgroundColor: 'var(--hover)', border: '1px solid var(--linea)', borderRadius: '3px' }}>
                    <div className="fw-bold fs-5 num" style={{ color: 'var(--texto)' }}>{justificados}</div>
                    <small style={{ color: 'var(--suave)' }}>Justificados</small>
                  </div>
                </Col>
                <Col xs={3}>
                  <div className="p-2" style={{ backgroundColor: 'var(--hover)', border: '1px solid var(--linea)', borderRadius: '3px' }}>
                    <div className="fw-bold fs-5 num" style={{ color: 'var(--error)' }}>{ausentes}</div>
                    <small style={{ color: 'var(--suave)' }}>Ausentes</small>
                  </div>
                </Col>
              </Row>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      <DataTable
        columns={columns}
        data={asistencias}
        keyField="idAsistencia"
        searchPlaceholder="Buscar por fecha o materia..."
        searchKeys={['fecha', 'materiaNombre', 'estado']}
      />
    </div>
  )
}

export default AsistenciasAlumnoPage
