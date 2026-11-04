import { useState, useEffect } from 'react'
import { Card, Form, Row, Col, Badge } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import { getAlumnos } from '../../api/alumnos'
import { getCuotas } from '../../api/cuotas'
import { getPagos } from '../../api/pagos'
import { getMatriculas } from '../../api/matriculas'

export const AdminSituacionAlumnoPage = () => {
  const [alumnos, setAlumnos] = useState([])
  const [selectedAlumnoId, setSelectedAlumnoId] = useState(1)
  const [cuotas, setCuotas] = useState([])
  const [pagos, setPagos] = useState([])
  const [matricula, setMatricula] = useState(null)

  useEffect(() => {
    getAlumnos().then(setAlumnos).catch(() => {})
  }, [])

  useEffect(() => {
    if (!selectedAlumnoId) return
    getCuotas(selectedAlumnoId).then(setCuotas).catch(() => {})
    getPagos({ idAlumno: selectedAlumnoId }).then(setPagos).catch(() => {})
    getMatriculas().then((mats) => {
      const encontrada = mats.find((m) => m.idAlumno === Number(selectedAlumnoId))
      setMatricula(encontrada || null)
    }).catch(() => {})
  }, [selectedAlumnoId])

  const alumnoActual = alumnos.find((a) => a.idAlumno === Number(selectedAlumnoId))

  const cuotasVencidas = cuotas.filter((c) => c.estado === 'VENCIDA')
  const cuotasPendientes = cuotas.filter((c) => c.estado === 'PENDIENTE')
  const totalAdeudado = [...cuotasVencidas, ...cuotasPendientes].reduce((sum, c) => sum + (Number(c.importe) || 0), 0)
  const totalAbonado = pagos.reduce((sum, p) => sum + (Number(p.importe) || 0), 0)

  const columns = [
    { key: 'concepto', label: 'Concepto' },
    {
      key: 'tipo',
      label: 'Tipo',
      render: (row) => (
        <Badge bg={row.tipo === 'SEGURO_CUATRIMESTRAL' ? 'info' : 'secondary'}>
          {row.tipo === 'SEGURO_CUATRIMESTRAL' ? 'Seguro Cuatrimestral' : 'Cuota'}
        </Badge>
      ),
    },
    { key: 'periodo', label: 'Período' },
    {
      key: 'importe',
      label: 'Importe',
      render: (row) => (row.importe != null ? `$${Number(row.importe).toLocaleString('es-AR')}` : '$0'),
    },
    { key: 'fechaVencimiento', label: 'Vencimiento' },
    {
      key: 'estado',
      label: 'Estado',
      render: (row) => <StatusBadge status={row.estado} />,
    },
    {
      key: 'comprobante',
      label: 'Recibo',
      render: (row) => row.comprobante ? <span className="font-monospace small">{row.comprobante}</span> : <span className="text-muted small">—</span>,
    },
  ]

  return (
    <div>
      <PageHeader
        title="Situación Administrativa por Alumno"
        subtitle="Búsqueda individual de estudiante: verificación de legajo, aranceles adeudados y total abonado"
        icon="bi-search"
      />

      {/* Selector / Buscador de Alumno */}
      <Card className="border-0  mb-4" style={{ backgroundColor: 'var(--m-surface)' }}>
        <Card.Body className="p-3">
          <Row className="align-items-center g-3">
            <Col md={6}>
              <Form.Group controlId="selAluAdmin">
                <Form.Label className="small fw-semibold">Seleccionar Estudiante para Auditoría</Form.Label>
                <Form.Select
                  value={selectedAlumnoId}
                  onChange={(e) => setSelectedAlumnoId(Number(e.target.value))}
                  style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
                >
                  {alumnos.map((a) => (
                    <option key={a.idAlumno} value={a.idAlumno}>
                      {a.apellido}, {a.nombre} (Legajo: {a.legajo} - DNI: {a.dni})
                    </option>
                  ))}
                </Form.Select>
              </Form.Group>
            </Col>
            <Col md={6}>
              {alumnoActual && (
                <div className="small text-muted pt-md-3">
                  <div><strong>Carrera:</strong> {alumnoActual.carrera}</div>
                  <div><strong>Email:</strong> {alumnoActual.email} | <strong>Condición:</strong> <StatusBadge status={alumnoActual.estado} /></div>
                </div>
              )}
            </Col>
          </Row>
        </Card.Body>
      </Card>

      {/* Resumen Financiero del Alumno */}
      <Row className="g-3 mb-4">
        <Col sm={6} md={3}>
          <Card className="border-0  h-100" style={{ backgroundColor: 'var(--m-surface)' }}>
            <Card.Body className="p-3">
              <span className="text-muted small text-uppercase fw-semibold">Matrícula 2026</span>
              <h5 className="fw-bold mt-2 mb-1" style={{ color: 'var(--m-onSurface)' }}>
                ${matricula?.importe != null ? Number(matricula.importe).toLocaleString('es-AR') : '35.000'}
              </h5>
              <div className="mt-2">
                <StatusBadge status={matricula?.estado || 'ACTIVA'} />
              </div>
            </Card.Body>
          </Card>
        </Col>

        <Col sm={6} md={3}>
          <Card className="border-0  h-100" style={{ backgroundColor: 'var(--m-surface)' }}>
            <Card.Body className="p-3">
              <span className="text-muted small text-uppercase fw-semibold">Total Adeudado</span>
              <h4 className="fw-bold mt-2 mb-1 text-danger">
                ${(totalAdeudado || 0).toLocaleString('es-AR')}
              </h4>
              <small className="text-muted">
                {cuotasVencidas.length} cuotas vencidas
              </small>
            </Card.Body>
          </Card>
        </Col>

        <Col sm={6} md={3}>
          <Card className="border-0  h-100" style={{ backgroundColor: 'var(--m-surface)' }}>
            <Card.Body className="p-3">
              <span className="text-muted small text-uppercase fw-semibold">Total Abonado</span>
              <h4 className="fw-bold mt-2 mb-1 text-success">
                ${(totalAbonado || 0).toLocaleString('es-AR')}
              </h4>
              <small className="text-muted">
                {pagos.length} operaciones registradas
              </small>
            </Card.Body>
          </Card>
        </Col>

        <Col sm={6} md={3}>
          <Card className="border-0  h-100" style={{ backgroundColor: 'var(--m-surface)' }}>
            <Card.Body className="p-3">
              <span className="text-muted small text-uppercase fw-semibold">Estado Arancelario</span>
              <div className="mt-2 mb-1">
                <StatusBadge status={cuotasVencidas.length > 0 ? 'VENCIDO' : totalAdeudado > 0 ? 'PENDIENTE' : 'PAGADO'} />
              </div>
              <small className="text-muted">
                {cuotasVencidas.length > 0 ? 'Posee deuda exigible' : 'Al día con vencimientos'}
              </small>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      <h5 className="fw-bold mb-3" style={{ color: 'var(--m-primary)' }}>
        Estado de Cuotas y Seguros del Alumno
      </h5>

      <DataTable
        columns={columns}
        data={cuotas}
        keyField="idCuota"
        searchPlaceholder="Buscar concepto..."
        searchKeys={['concepto', 'periodo', 'estado']}
      />
    </div>
  )
}

export default AdminSituacionAlumnoPage
