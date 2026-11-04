import { useState, useEffect } from 'react'
import { Button, Form, Row, Col, Badge, ProgressBar } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import FormModal from '../../components/FormModal'
import { useToast } from '../../context/ToastContext'
import { getCursos, createCurso, updateCurso } from '../../api/cursos'
import { getCarreras } from '../../api/carreras'
import { ETIQUETAS_TURNO } from '../../config/etiquetas'

export const GestionCursosPage = () => {
  const { showToast } = useToast()
  const [cursos, setCursos] = useState([])
  const [carreras, setCarreras] = useState([])
  const [loading, setLoading] = useState(false)
  const [showModal, setShowModal] = useState(false)
  const [editingCurso, setEditingCurso] = useState(null)

  const [form, setForm] = useState({
    nombre: '',
    codigo: '',
    idCarrera: 1,
    turno: 'MANANA',
    capacidadMaxima: 35,
    inscriptos: 0,
    aula: '',
    cicloLectivo: 2026,
  })

  const cargar = async () => {
    try {
      const [dataCursos, dataCarreras] = await Promise.all([
        getCursos(),
        getCarreras(),
      ])
      setCursos(dataCursos)
      setCarreras(dataCarreras)
    } catch {
      showToast('Error al cargar cursos o carreras', 'danger')
    }
  }

  useEffect(() => {
    cargar()
  }, [])

  const handleOpenNuevo = () => {
    setEditingCurso(null)
    setForm({
      nombre: '',
      codigo: '',
      idCarrera: carreras[0]?.idCarrera || 1,
      turno: 'MANANA',
      capacidadMaxima: 35,
      inscriptos: 0,
      aula: '',
      cicloLectivo: 2026,
    })
    setShowModal(true)
  }

  const handleOpenEditar = (c) => {
    setEditingCurso(c)
    setForm({
      nombre: c.nombre,
      codigo: c.codigo,
      idCarrera: c.idCarrera || 1,
      turno: c.turno,
      capacidadMaxima: c.capacidadMaxima,
      inscriptos: c.inscriptos,
      aula: c.aula,
      cicloLectivo: c.cicloLectivo,
    })
    setShowModal(true)
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    setLoading(true)
    try {
      if (editingCurso) {
        await updateCurso(editingCurso.idCurso, form)
        showToast('Curso actualizado con éxito', 'success')
      } else {
        await createCurso(form)
        showToast('Comisión / Curso dado de alta con éxito', 'success')
      }
      setShowModal(false)
      cargar()
    } catch (err) {
      showToast(err.message || 'Error al guardar curso', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    { key: 'codigo', label: 'Código', width: '110px' },
    {
      key: 'nombre',
      label: 'Comisión y Carrera',
      render: (row) => (
        <div>
          <strong style={{ color: 'var(--m-onSurface)' }}>{row.nombre}</strong>
          <div className="small text-muted">{row.carreraNombre || 'Tecnicatura en Software'}</div>
        </div>
      ),
    },
    { key: 'cicloLectivo', label: 'Ciclo' },
    {
      key: 'turno',
      label: 'Turno',
      render: (row) => <Badge bg="secondary">{ETIQUETAS_TURNO[row.turno] || row.turno}</Badge>,
    },
    { key: 'aula', label: 'Aula / Laboratorio' },
    {
      key: 'ocupacion',
      label: 'Inscriptos / Capacidad',
      render: (row) => {
        const pct = Math.round((row.inscriptos / row.capacidadMaxima) * 100) || 0
        return (
          <div style={{ minWidth: '130px' }}>
            <div className="d-flex justify-content-between small mb-1">
              <span>
                {row.inscriptos} / {row.capacidadMaxima}
              </span>
              <span className="text-muted">{pct}%</span>
            </div>
            <ProgressBar
              now={pct}
              variant={pct >= 100 ? 'danger' : pct >= 80 ? 'warning' : 'success'}
              style={{ height: '5px' }}
            />
          </div>
        )
      },
    },
    {
      key: 'cuposDisponibles',
      label: 'Cupos Disponibles',
      render: (row) => (
        <span className={`fw-bold ${row.cuposDisponibles === 0 ? 'text-danger' : 'text-success'}`}>
          {row.cuposDisponibles === 0 ? 'Sin cupo' : `${row.cuposDisponibles} vacantes`}
        </span>
      ),
    },
    {
      key: 'acciones',
      label: 'Acciones',
      align: 'right',
      render: (row) => (
        <Button
          variant="outline-primary"
          size="sm"
          onClick={() => handleOpenEditar(row)}
          title="Editar curso"
          aria-label="Editar curso"
        >
          <i className="bi bi-pencil" />
        </Button>
      ),
    },
  ]

  return (
    <div>
      <PageHeader
        title="Gestión de Cursos"
        subtitle="Comisiones por carrera, ciclo lectivo, control de capacidad máxima y cupos disponibles"
        icon="bi-building"
        actions={
          <Button variant="primary" size="sm" onClick={handleOpenNuevo}>
            <i className="bi bi-plus-lg me-1" /> Nuevo Curso
          </Button>
        }
      />

      <DataTable
        columns={columns}
        data={cursos}
        keyField="idCurso"
        searchPlaceholder="Buscar por código, turno o aula..."
        searchKeys={['codigo', 'nombre', 'carreraNombre', 'turno', 'aula']}
      />

      <FormModal
        show={showModal}
        onHide={() => setShowModal(false)}
        onSubmit={handleSubmit}
        title={editingCurso ? `Editar Curso - ${editingCurso.codigo}` : 'Crear Nuevo Curso'}
        submitText={editingCurso ? 'Guardar Cambios' : 'Crear Curso'}
        loading={loading}
      >
        <Row className="g-3">
          <Col md={6}>
            <Form.Group controlId="cursoNombre">
              <Form.Label className="small fw-semibold">
                Nombre de la Comisión <span className="text-danger">*</span>
              </Form.Label>
              <Form.Control
                type="text"
                value={form.nombre}
                onChange={(e) => setForm({ ...form, nombre: e.target.value })}
                required
                placeholder="Ej. 1° Año - Turno Mañana"
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>

          <Col md={6}>
            <Form.Group controlId="cursoCodigo">
              <Form.Label className="small fw-semibold">Código Único</Form.Label>
              <Form.Control
                type="text"
                value={form.codigo}
                onChange={(e) => setForm({ ...form, codigo: e.target.value })}
                placeholder="Ej. 1A-INF-TM"
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>

          <Col md={6}>
            <Form.Group controlId="cursoCarreraSelect">
              <Form.Label className="small fw-semibold">Carrera Asociada</Form.Label>
              <Form.Select
                value={form.idCarrera}
                onChange={(e) => setForm({ ...form, idCarrera: Number(e.target.value) })}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              >
                {carreras.map((c) => (
                  <option key={c.idCarrera} value={c.idCarrera}>
                    {c.nombre} ({c.codigo})
                  </option>
                ))}
              </Form.Select>
            </Form.Group>
          </Col>

          <Col md={6}>
            <Form.Group controlId="cursoCicloSelect">
              <Form.Label className="small fw-semibold">Ciclo Lectivo</Form.Label>
              <Form.Select
                value={form.cicloLectivo}
                onChange={(e) => setForm({ ...form, cicloLectivo: Number(e.target.value) })}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              >
                <option value={2026}>2026</option>
                <option value={2025}>2025</option>
              </Form.Select>
            </Form.Group>
          </Col>

          <Col md={4}>
            <Form.Group controlId="cursoTurno">
              <Form.Label className="small fw-semibold">Turno</Form.Label>
              <Form.Select
                value={form.turno}
                onChange={(e) => setForm({ ...form, turno: e.target.value })}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              >
                <option value="MANANA">Mañana</option>
                <option value="TARDE">Tarde</option>
                <option value="NOCHE">Noche</option>
              </Form.Select>
            </Form.Group>
          </Col>

          <Col md={4}>
            <Form.Group controlId="cursoCapacidad">
              <Form.Label className="small fw-semibold">
                Capacidad Máxima <span className="text-danger">*</span>
              </Form.Label>
              <Form.Control
                type="number"
                min="1"
                value={form.capacidadMaxima}
                onChange={(e) => setForm({ ...form, capacidadMaxima: e.target.value })}
                required
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>

          <Col md={4}>
            <Form.Group controlId="cursoAula">
              <Form.Label className="small fw-semibold">Aula / Espacio Físico</Form.Label>
              <Form.Control
                type="text"
                value={form.aula}
                onChange={(e) => setForm({ ...form, aula: e.target.value })}
                placeholder="Ej. Aula 101"
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
              />
            </Form.Group>
          </Col>
        </Row>
      </FormModal>
    </div>
  )
}

export default GestionCursosPage
