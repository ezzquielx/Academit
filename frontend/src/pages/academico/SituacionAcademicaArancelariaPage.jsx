import { useState, useEffect } from 'react'
import { Card, Row, Col, Tab, Nav, Badge, ProgressBar } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import { useAuth } from '../../context/AuthContext'
import { getCalificaciones } from '../../api/calificaciones'
import { getCuotas } from '../../api/cuotas'
import { getPagos } from '../../api/pagos'
import { getMatriculas } from '../../api/matriculas'

export const SituacionAcademicaArancelariaPage = () => {
  const { user } = useAuth()
  const [activeTab, setActiveTab] = useState('arancelaria')
  const [calificaciones, setCalificaciones] = useState([])
  const [cuotas, setCuotas] = useState([])
  const [pagos, setPagos] = useState([])
  const [matricula, setMatricula] = useState(null)
  const alumnoId = user?.idAlumno || 1 // Alumno de sesión o referencia

  useEffect(() => {
    getCalificaciones({ idAlumno: alumnoId }).then(setCalificaciones).catch(() => {})
    getCuotas(alumnoId).then(setCuotas).catch(() => {})
    getPagos({ idAlumno: alumnoId }).then(setPagos).catch(() => {})
    getMatriculas().then((mats) => {
      const encontrada = mats.find((m) => m.idAlumno === alumnoId)
      if (encontrada) setMatricula(encontrada)
    }).catch(() => {})
  }, [alumnoId])

  // Cálculos arancelarios
  const cuotasPagadas = cuotas.filter((c) => c.estado === 'PAGADA')
  const cuotasPendientes = cuotas.filter((c) => c.estado === 'PENDIENTE')
  const cuotasVencidas = cuotas.filter((c) => c.estado === 'VENCIDA')

  const totalAbonado = pagos.reduce((sum, p) => sum + (Number(p.importe) || 0), 0)
  const totalAdeudado = [...cuotasPendientes, ...cuotasVencidas].reduce((sum, c) => sum + (Number(c.importe) || 0), 0)

  // Cálculos académicos
  const materiasAprobadas = calificaciones.filter((c) => c.nota >= 4)
  const promedioGeneral = calificaciones.length > 0
    ? (calificaciones.reduce((sum, c) => sum + (Number(c.nota) || 0), 0) / calificaciones.length).toFixed(2)
    : '0.00'

  const columnsArancelaria = [
    { key: 'concepto', label: 'Concepto' },
    {
      key: 'tipo',
      label: 'Tipo',
      render: (row) => (
        <Badge bg={row.tipo === 'SEGURO_CUATRIMESTRAL' ? 'info' : 'secondary'}>
          {row.tipo === 'SEGURO_CUATRIMESTRAL' ? 'Seguro Cuatrimestral' : 'Cuota Mensual'}
        </Badge>
      ),
    },
    { key: 'periodo', label: 'Período' },
    {
      key: 'importe',
      label: 'Importe',
      render: (row) => <span className="fw-semibold">${row.importe != null ? Number(row.importe).toLocaleString('es-AR') : '0'}</span>,
    },
    { key: 'fechaVencimiento', label: 'Vencimiento' },
    {
      key: 'estado',
      label: 'Estado',
      render: (row) => <StatusBadge status={row.estado} />,
    },
    {
      key: 'comprobante',
      label: 'Comprobante / Pago',
      render: (row) =>
        row.comprobante ? (
          <span className="small text-muted font-monospace">{row.comprobante}</span>
        ) : (
          <span className="text-muted small">—</span>
        ),
    },
  ]

  const columnsAcademica = [
    { key: 'materiaNombre', label: 'Materia' },
    { key: 'evaluacionNombre', label: 'Instancia' },
    {
      key: 'tipo',
      label: 'Tipo Evaluación',
      render: (row) => <Badge bg="secondary">{row.tipo}</Badge>,
    },
    { key: 'fecha', label: 'Fecha' },
    {
      key: 'nota',
      label: 'Calificación',
      render: (row) => (
        <Badge bg={row.nota >= 7 ? 'success' : row.nota >= 4 ? 'warning' : 'danger'} className="fs-6">
          {row.nota} / 10
        </Badge>
      ),
    },
    {
      key: 'condicion',
      label: 'Condición',
      render: (row) => <StatusBadge status={row.condicion} />,
    },
    { key: 'observacion', label: 'Observación' },
  ]

  return (
    <div>
      <PageHeader
        title="Situación Académica y Arancelaria"
        subtitle="Estado integral del alumno: materias, evaluaciones, matrícula, 12 cuotas y 3 seguros cuatrimestrales"
        icon="bi-card-checklist"
      />

      <Tab.Container activeKey={activeTab} onSelect={(k) => setActiveTab(k)}>
        <Nav variant="pills" className="mb-4 gap-2 border-bottom pb-3 border-secondary border-opacity-25">
          <Nav.Item>
            <Nav.Link eventKey="arancelaria" className="d-flex align-items-center gap-2">
              <i className="bi bi-cash-coin" />
              <span>Situación Arancelaria</span>
            </Nav.Link>
          </Nav.Item>
          <Nav.Item>
            <Nav.Link eventKey="academica" className="d-flex align-items-center gap-2">
              <i className="bi bi-mortarboard" />
              <span>Situación Académica</span>
            </Nav.Link>
          </Nav.Item>
        </Nav>

        <Tab.Content>
          {/* SECCIÓN ARANCELARIA */}
          <Tab.Pane eventKey="arancelaria">
            {/* Tarjetas resumen arancelario */}
            <Row className="g-3 mb-4">
              <Col sm={6} lg={3}>
                <Card className="border-0  h-100" style={{ backgroundColor: 'var(--m-surface)' }}>
                  <Card.Body className="p-3">
                    <span className="text-muted small text-uppercase fw-semibold">Matrícula Anual 2026</span>
                    <h4 className="fw-bold mt-1 mb-1" style={{ color: 'var(--m-onSurface)' }}>
                      ${matricula?.importe != null ? Number(matricula.importe).toLocaleString('es-AR') : '35.000'}
                    </h4>
                    <div className="d-flex align-items-center gap-2 mt-2">
                      <StatusBadge status={matricula?.estado || 'ACTIVA'} />
                      <StatusBadge status={matricula?.estadoPago || 'AL_DIA'} />
                    </div>
                  </Card.Body>
                </Card>
              </Col>

              <Col sm={6} lg={3}>
                <Card className="border-0  h-100" style={{ backgroundColor: 'var(--m-surface)' }}>
                  <Card.Body className="p-3">
                    <span className="text-muted small text-uppercase fw-semibold">Total Abonado</span>
                    <h4 className="fw-bold mt-1 mb-1 text-success">
                      ${(totalAbonado || 0).toLocaleString('es-AR')}
                    </h4>
                    <small className="text-muted">{cuotasPagadas.length} cuotas/seguros pagados</small>
                  </Card.Body>
                </Card>
              </Col>

              <Col sm={6} lg={3}>
                <Card className="border-0  h-100" style={{ backgroundColor: 'var(--m-surface)' }}>
                  <Card.Body className="p-3">
                    <span className="text-muted small text-uppercase fw-semibold">Importe Adeudado</span>
                    <h4 className="fw-bold mt-1 mb-1 text-danger">
                      ${(totalAdeudado || 0).toLocaleString('es-AR')}
                    </h4>
                    <small className="text-danger">
                      {cuotasVencidas.length} cuota(s) vencida(s)
                    </small>
                  </Card.Body>
                </Card>
              </Col>

              <Col sm={6} lg={3}>
                <Card className="border-0  h-100" style={{ backgroundColor: 'var(--m-surface)' }}>
                  <Card.Body className="p-3">
                    <span className="text-muted small text-uppercase fw-semibold">Resumen de Cuotas</span>
                    <div className="d-flex justify-content-between small mt-2">
                      <span>Pagadas:</span>
                      <strong className="text-success">{cuotasPagadas.length}</strong>
                    </div>
                    <div className="d-flex justify-content-between small">
                      <span>Vencidas:</span>
                      <strong className="text-danger">{cuotasVencidas.length}</strong>
                    </div>
                    <div className="d-flex justify-content-between small">
                      <span>Pendientes a vencer:</span>
                      <strong className="text-warning">{cuotasPendientes.length}</strong>
                    </div>
                  </Card.Body>
                </Card>
              </Col>
            </Row>

            <h5 className="fw-bold mb-3" style={{ color: 'var(--m-primary)' }}>
              Detalle Completo: 12 Cuotas Mensuales + 3 Seguros Cuatrimestrales
            </h5>

            <DataTable
              columns={columnsArancelaria}
              data={cuotas}
              keyField="idCuota"
              pageSize={10}
              searchPlaceholder="Buscar por concepto o período..."
              searchKeys={['concepto', 'periodo', 'tipo', 'estado']}
            />
          </Tab.Pane>

          {/* SECCIÓN ACADÉMICA */}
          <Tab.Pane eventKey="academica">
            <Row className="g-3 mb-4">
              <Col sm={6} md={4}>
                <Card className="border-0 " style={{ backgroundColor: 'var(--m-surface)' }}>
                  <Card.Body className="p-3">
                    <span className="text-muted small text-uppercase fw-semibold">Materias Aprobadas</span>
                    <h3 className="fw-bold mt-1 mb-0 text-success">
                      {materiasAprobadas.length}
                    </h3>
                    <small className="text-muted">Calificación &gt;= 4</small>
                  </Card.Body>
                </Card>
              </Col>

              <Col sm={6} md={4}>
                <Card className="border-0 " style={{ backgroundColor: 'var(--m-surface)' }}>
                  <Card.Body className="p-3">
                    <span className="text-muted small text-uppercase fw-semibold">Promedio General</span>
                    <h3 className="fw-bold mt-1 mb-0" style={{ color: 'var(--m-primary)' }}>
                      {promedioGeneral} <small className="fs-6 text-muted">/ 10</small>
                    </h3>
                    <small className="text-muted">Sin aplazos</small>
                  </Card.Body>
                </Card>
              </Col>

              <Col sm={6} md={4}>
                <Card className="border-0 " style={{ backgroundColor: 'var(--m-surface)' }}>
                  <Card.Body className="p-3">
                    <span className="text-muted small text-uppercase fw-semibold">Avance en la Carrera</span>
                    <h3 className="fw-bold mt-1 mb-1" style={{ color: 'var(--m-onSurface)' }}>
                      35%
                    </h3>
                    <ProgressBar now={35} variant="success" style={{ height: '6px' }} />
                  </Card.Body>
                </Card>
              </Col>
            </Row>

            <h5 className="fw-bold mb-3" style={{ color: 'var(--m-primary)' }}>
              Historial de Calificaciones y Evaluaciones
            </h5>

            <DataTable
              columns={columnsAcademica}
              data={calificaciones}
              keyField="idCalificacion"
              searchPlaceholder="Buscar materia o instancia..."
              searchKeys={['materiaNombre', 'evaluacionNombre', 'condicion']}
            />
          </Tab.Pane>
        </Tab.Content>
      </Tab.Container>
    </div>
  )
}

export default SituacionAcademicaArancelariaPage
