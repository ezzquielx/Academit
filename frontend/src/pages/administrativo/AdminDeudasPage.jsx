import { useState, useEffect } from 'react'
import { Card, Row, Col, Badge } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import { getAlumnos } from '../../api/alumnos'
import { getCuotas } from '../../api/cuotas'

export const AdminDeudasPage = () => {
  const [alumnosConDeuda, setAlumnosConDeuda] = useState([])
  const [totalDeudaGlobal, setTotalDeudaGlobal] = useState(0)

  useEffect(() => {
    Promise.all([getAlumnos(), getCuotas()]).then(([alumnos, cuotas]) => {
      let sumaGlobal = 0
      const lista = alumnos.map((alu) => {
        const cuotasAlu = cuotas.filter((c) => c.idAlumno === alu.idAlumno)
        const vencidas = cuotasAlu.filter((c) => c.estado === 'VENCIDA')
        const pendientes = cuotasAlu.filter((c) => c.estado === 'PENDIENTE')
        const importeVencido = vencidas.reduce((s, c) => s + (Number(c.importe) || 0), 0)
        const importeTotalDeuda = [...vencidas, ...pendientes].reduce((s, c) => s + (Number(c.importe) || 0), 0)

        sumaGlobal += importeVencido

        return {
          idAlumno: alu.idAlumno,
          legajo: alu.legajo,
          nombreCompleto: `${alu.apellido}, ${alu.nombre}`,
          dni: alu.dni,
          email: alu.email,
          cursoNombre: alu.cursoNombre,
          cuotasVencidasCount: vencidas.length,
          importeVencido,
          importeTotalDeuda,
          estadoMatricula: alu.estadoMatricula,
        }
      }).filter((item) => item.importeTotalDeuda > 0)

      setAlumnosConDeuda(lista)
      setTotalDeudaGlobal(sumaGlobal)
    })
  }, [])

  const columns = [
    { key: 'legajo', label: 'Legajo', width: '100px' },
    {
      key: 'nombreCompleto',
      label: 'Estudiante',
      render: (row) => (
        <div>
          <strong style={{ color: 'var(--m-onSurface)' }}>{row.nombreCompleto}</strong>
          <div className="text-muted small">{row.email}</div>
        </div>
      ),
    },
    { key: 'dni', label: 'DNI' },
    { key: 'cursoNombre', label: 'Comisión' },
    {
      key: 'cuotasVencidasCount',
      label: 'Cuotas Vencidas',
      render: (row) => (
        <Badge bg={row.cuotasVencidasCount > 0 ? 'danger' : 'secondary'}>
          {row.cuotasVencidasCount} cuota(s)
        </Badge>
      ),
    },
    {
      key: 'importeVencido',
      label: 'Deuda Exigible (Vencida)',
      render: (row) => (
        <span className="fw-bold text-danger">
          ${row.importeVencido != null ? Number(row.importeVencido).toLocaleString('es-AR') : '0'}
        </span>
      ),
    },
    {
      key: 'importeTotalDeuda',
      label: 'Compromiso Total',
      render: (row) => <span>${row.importeTotalDeuda != null ? Number(row.importeTotalDeuda).toLocaleString('es-AR') : '0'}</span>,
    },
    {
      key: 'estadoMatricula',
      label: 'Matrícula',
      render: (row) => <StatusBadge status={row.estadoMatricula} />,
    },
  ]

  return (
    <div>
      <PageHeader
        title="Reporte de Deuda de Alumnos"
        subtitle="Consolidado financiero: estudiantes con cuotas y aranceles vencidos o pendientes de pago"
        icon="bi-exclamation-octagon"
      />

      <Row className="g-3 mb-4">
        <Col sm={6} md={4}>
          <Card className="border-0 " style={{ backgroundColor: 'var(--m-surface)' }}>
            <Card.Body className="p-3">
              <span className="text-muted small text-uppercase fw-semibold">Alumnos con Deuda</span>
              <h3 className="fw-bold mt-1 mb-0" style={{ color: 'var(--m-onSurface)' }}>
                {alumnosConDeuda.length}
              </h3>
            </Card.Body>
          </Card>
        </Col>

        <Col sm={6} md={4}>
          <Card className="border-0 " style={{ backgroundColor: 'var(--m-surface)' }}>
            <Card.Body className="p-3">
              <span className="text-muted small text-uppercase fw-semibold">Total Deuda Exigible (Vencida)</span>
              <h3 className="fw-bold mt-1 mb-0 text-danger">
                ${(totalDeudaGlobal || 0).toLocaleString('es-AR')}
              </h3>
            </Card.Body>
          </Card>
        </Col>

        <Col sm={6} md={4}>
          <Card className="border-0 " style={{ backgroundColor: 'var(--m-surface)' }}>
            <Card.Body className="p-3">
              <span className="text-muted small text-uppercase fw-semibold">Tasa de Morosidad Estimada</span>
              <h3 className="fw-bold mt-1 mb-0 text-warning">
                25%
              </h3>
            </Card.Body>
          </Card>
        </Col>
      </Row>

      <DataTable
        columns={columns}
        data={alumnosConDeuda}
        keyField="idAlumno"
        searchPlaceholder="Buscar por nombre, legajo o DNI..."
        searchKeys={['nombreCompleto', 'legajo', 'dni']}
      />
    </div>
  )
}

export default AdminDeudasPage
