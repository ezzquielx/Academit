import { useState, useEffect } from 'react'
import { Row, Col, Card, Button, Badge } from 'react-bootstrap'
import { Link } from 'react-router-dom'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import { useAuth } from '../../context/AuthContext'
import { getAsignaciones } from '../../api/asignaciones'
import { getCursos } from '../../api/cursos'

export const MisCursosPage = () => {
  const { idDocente } = useAuth()
  const [asignaciones, setAsignaciones] = useState([])
  const [cursos, setCursos] = useState([])
  const docenteId = idDocente

  useEffect(() => {
    getAsignaciones()
      .then((asigs) => {
        const list = Array.isArray(asigs) ? asigs : []
        setAsignaciones(docenteId ? list.filter((a) => Number(a.idDocente) === Number(docenteId)) : list)
      })
      .catch(() => {})
    getCursos().then(setCursos).catch(() => {})
  }, [docenteId])

  const columns = [
    { key: 'materiaNombre', label: 'Materia' },
    { key: 'cursoNombre', label: 'Curso / Comisión' },
    {
      key: 'rolDocente',
      label: 'Cargo Docente',
      render: (row) => <Badge bg="primary">{row.rolDocente}</Badge>,
    },
    {
      key: 'horarios',
      label: 'Carga Horaria / Horario',
      render: () => <span className="small text-muted">Lunes y Miércoles 08:00 - 11:00 hs</span>,
    },
    {
      key: 'acciones',
      label: 'Acciones',
      align: 'right',
      render: () => (
        <div className="d-flex justify-content-end gap-1">
          <Button
            as={Link}
            to="/docencia/calificaciones"
            variant="outline-primary"
            size="sm"
            title="Calificaciones"
            aria-label="Calificaciones"
          >
            <i className="bi bi-award" />
          </Button>
          <Button
            as={Link}
            to="/docencia/asistencias"
            variant="outline-success"
            size="sm"
            title="Tomar asistencia"
            aria-label="Tomar asistencia"
          >
            <i className="bi bi-check2-square" />
          </Button>
        </div>
      ),
    },
  ]

  return (
    <div>
      <PageHeader
        title="Mis Cursos Asignados"
        subtitle="Cursos, comisiones y materias asignadas al docente en el ciclo lectivo activo"
        icon="bi-collection"
      />

      <Row className="g-3 mb-4">
        {asignaciones.map((asig) => {
          const cursoInfo = cursos.find((c) => c.idCurso === asig.idCurso)
          return (
            <Col key={asig.idAsignacion} md={6}>
              <Card className="border-0  h-100" style={{ backgroundColor: 'var(--m-surface)' }}>
                <Card.Header
                  className="p-3 border-bottom border-secondary border-opacity-25 d-flex justify-content-between align-items-center"
                  style={{ backgroundColor: 'var(--m-surfaceVariant)' }}
                >
                  <strong className="fs-6" style={{ color: 'var(--m-primary)' }}>
                    {asig.materiaNombre}
                  </strong>
                  <Badge bg="secondary">{asig.rolDocente}</Badge>
                </Card.Header>
                <Card.Body className="p-3">
                  <h6 className="fw-bold mb-1" style={{ color: 'var(--m-onSurface)' }}>
                    {asig.cursoNombre}
                  </h6>
                  <p className="small text-muted mb-3">
                    Aula: {cursoInfo?.aula || 'Aula 101'} • Capacidad: {cursoInfo?.inscriptos || 29} alumnos
                  </p>
                  <div className="d-flex flex-wrap gap-2">
                    <Button as={Link} to="/docencia/evaluaciones" variant="outline-info" size="sm">
                      <i className="bi bi-clipboard2-check me-1" /> Evaluaciones
                    </Button>
                    <Button as={Link} to="/docencia/calificaciones" variant="outline-primary" size="sm">
                      <i className="bi bi-award me-1" /> Calificaciones
                    </Button>
                    <Button as={Link} to="/docencia/asistencias" variant="outline-success" size="sm">
                      <i className="bi bi-check2-square me-1" /> Tomar Lista
                    </Button>
                  </div>
                </Card.Body>
              </Card>
            </Col>
          )
        })}
      </Row>

      <h5 className="fw-bold mb-3" style={{ color: 'var(--m-primary)' }}>
        Detalle de Comisiones y Actividades
      </h5>

      <DataTable
        columns={columns}
        data={asignaciones}
        keyField="idAsignacion"
        searchPlaceholder="Buscar materia o curso..."
        searchKeys={['materiaNombre', 'cursoNombre']}
      />
    </div>
  )
}

export default MisCursosPage
