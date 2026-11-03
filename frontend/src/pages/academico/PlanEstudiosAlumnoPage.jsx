import { useState, useEffect } from 'react'
import { Card, Row, Col, ListGroup } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import StatusBadge from '../../components/StatusBadge'
import { getMateriasDePlan } from '../../api/planes'

export const PlanEstudiosAlumnoPage = () => {
  const [materias, setMaterias] = useState([])

  useEffect(() => {
    getMateriasDePlan(1).then(setMaterias).catch(() => {})
  }, [])

  // Agrupar materias por año
  const anios = [1, 2, 3]

  // Estado mockeado de las materias para Lucía Benítez (1° Año)
  const getEstadoMateria = (materia) => {
    if (materia.idMateria === 1) return { estado: 'APROBADA', nota: 8 }
    if (materia.idMateria === 2) return { estado: 'APROBADA', nota: 9 }
    if (materia.idMateria === 3 || materia.idMateria === 4) return { estado: 'REGULAR', nota: null }
    return { estado: 'PENDIENTE', nota: null }
  }

  return (
    <div>
      <PageHeader
        title="Plan de Estudios y Correlatividades"
        subtitle="Tecnicatura Superior en Desarrollo de Software | Materias por año y régimen de correlatividades (RF48)"
        icon="bi-diagram-3"
      />

      <Row className="g-4">
        {anios.map((anio) => {
          const materiasAnio = materias.filter((m) => m.anio === anio)
          return (
            <Col key={anio} lg={4}>
              <Card className="h-100" style={{ backgroundColor: 'var(--papel)', border: '1px solid var(--linea)' }}>
                <Card.Header
                  className="p-3 border-bottom d-flex justify-content-between align-items-center"
                  style={{ backgroundColor: 'var(--hover)', borderColor: 'var(--linea)' }}
                >
                  <strong className="fs-6" style={{ color: 'var(--texto)', fontFamily: 'var(--serif)' }}>
                    {anio}° Año
                  </strong>
                  <span style={{ fontSize: '12px', color: 'var(--suave)' }}>{materiasAnio.length} materias</span>
                </Card.Header>
                <Card.Body className="p-2">
                  <ListGroup variant="flush">
                    {materiasAnio.map((m) => {
                      const est = getEstadoMateria(m)
                      return (
                        <ListGroup.Item
                          key={m.idMateria}
                          className="p-3 border-bottom"
                          style={{ backgroundColor: 'transparent', borderColor: 'var(--linea)' }}
                        >
                          <div className="d-flex justify-content-between align-items-start mb-1">
                            <div>
                              <strong style={{ color: 'var(--texto)' }}>{m.nombre}</strong>
                              <div style={{ color: 'var(--suave)', fontSize: '13px' }}>
                                {m.codigo} • {m.regimen} • {m.horasSemanales} hs/sem
                              </div>
                            </div>
                            <StatusBadge status={est.estado} />
                          </div>

                          {/* Correlativas */}
                          <div className="mt-2 pt-2 border-top small" style={{ borderColor: 'var(--linea)' }}>
                            <span style={{ color: 'var(--suave)' }}>Correlativas requeridas: </span>
                            {m.correlativas && m.correlativas.length > 0 ? (
                              m.correlativas.map((c, idx) => {
                                const nombre = typeof c === 'number' ? (materias.find((mat) => mat.idMateria === c)?.nombre || `Materia #${c}`) : c
                                return (
                                  <span key={idx} className="badge" style={{ backgroundColor: 'var(--hover)', color: 'var(--texto)', border: '1px solid var(--linea)', marginRight: '4px' }}>
                                    {nombre}
                                  </span>
                                )
                              })
                            ) : (
                              <span style={{ color: 'var(--primario)', fontStyle: 'italic' }}>Ninguna (Inicial)</span>
                            )}
                          </div>

                          {est.nota && (
                            <div className="small mt-1" style={{ color: 'var(--primario)' }}>
                              <i className="bi bi-check2-circle me-1" />
                              Nota final: <strong>{est.nota}</strong>
                            </div>
                          )}
                        </ListGroup.Item>
                      )
                    })}
                  </ListGroup>
                </Card.Body>
              </Card>
            </Col>
          )
        })}
      </Row>
    </div>
  )
}

export default PlanEstudiosAlumnoPage
