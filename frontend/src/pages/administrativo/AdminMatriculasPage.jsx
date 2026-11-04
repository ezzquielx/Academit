import { useState, useEffect } from 'react'
import { Button, Form } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import FormModal from '../../components/FormModal'
import { useToast } from '../../context/ToastContext'
import { useAuth } from '../../context/AuthContext'
import { getMatriculas, updateMatricula, generarCuotas } from '../../api/matriculas'

export const AdminMatriculasPage = () => {
  const { showToast } = useToast()
  const { rol } = useAuth()
  const [matriculas, setMatriculas] = useState([])
  const [editingMat, setEditingMat] = useState(null)
  const [nuevoEstado, setNuevoEstado] = useState('ACTIVA')
  const [loading, setLoading] = useState(false)

  const puedeGenerarCuotas = rol === 'ADMINISTRATIVO' || rol === 'ADMINISTRADOR'

  const cargar = async () => {
    try {
      const data = await getMatriculas()
      setMatriculas(data)
    } catch {
      showToast('Error al cargar matrículas', 'danger')
    }
  }

  useEffect(() => {
    cargar()
  }, [])

  const handleOpenCambiar = (m) => {
    setEditingMat(m)
    setNuevoEstado(m.estado)
  }

  const handleGenerarCuotas = async (matricula) => {
    setLoading(true)
    try {
      const creadas = await generarCuotas(matricula.idMatricula)
      showToast(`Se generaron ${creadas.length} cuotas y seguros para la matrícula #${matricula.idMatricula}`, 'success')
      cargar()
    } catch (err) {
      showToast(err, 'danger')
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!editingMat) return
    setLoading(true)
    try {
      await updateMatricula(editingMat.idMatricula, { estado: nuevoEstado })
      showToast(`Estado de matrícula actualizado a ${nuevoEstado}`, 'success')
      setEditingMat(null)
      cargar()
    } catch (err) {
      showToast(err, 'danger')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    { key: 'idMatricula', label: '# Matrícula', width: '100px' },
    {
      key: 'alumnoNombre',
      label: 'Estudiante',
      render: (row) => <strong style={{ color: 'var(--m-onSurface)' }}>{row.alumnoNombre}</strong>,
    },
    { key: 'cicloLectivo', label: 'Ciclo' },
    {
      key: 'importe',
      label: 'Arancel Anual',
      render: (row) => (row.importe != null ? `$${Number(row.importe).toLocaleString('es-AR')}` : '-'),
    },
    { key: 'fechaAlta', label: 'Fecha Alta' },
    {
      key: 'estadoPago',
      label: 'Estado Pago',
      render: (row) => <StatusBadge status={row.estadoPago} />,
    },
    {
      key: 'estado',
      label: 'Estado Matrícula',
      render: (row) => <StatusBadge status={row.estado} />,
    },
    {
      key: 'acciones',
      label: 'Acciones',
      align: 'right',
      render: (row) => (
        <div className="d-flex justify-content-end gap-2">
          {puedeGenerarCuotas && (
            <Button
              variant="outline-success"
              size="sm"
              disabled={loading || row.estado === 'CANCELADA'}
              onClick={() => handleGenerarCuotas(row)}
              title={row.estado === 'CANCELADA' ? 'No se pueden generar cuotas para matrícula cancelada' : 'Generar 12 cuotas y 3 seguros'}
            >
              <i className="bi bi-calendar-plus me-1" /> Generar cuotas
            </Button>
          )}
          <Button variant="outline-primary" size="sm" onClick={() => handleOpenCambiar(row)}>
            <i className="bi bi-arrow-left-right me-1" /> Cambiar Estado
          </Button>
        </div>
      ),
    },
  ]

  return (
    <div>
      <PageHeader
        title="Administración de Matrículas"
        subtitle="Control y seguimiento de matriculaciones anuales (estados PENDIENTE, ACTIVA, CANCELADA)"
        icon="bi-card-heading"
      />

      <DataTable
        columns={columns}
        data={matriculas}
        keyField="idMatricula"
        searchPlaceholder="Buscar por alumno o estado..."
        searchKeys={['alumnoNombre', 'estado', 'estadoPago']}
      />

      <FormModal
        show={Boolean(editingMat)}
        onHide={() => setEditingMat(null)}
        onSubmit={handleSubmit}
        title={`Cambiar Estado de Matrícula - ${editingMat?.alumnoNombre}`}
        submitText="Guardar Estado"
        loading={loading}
      >
        <p className="small mb-3">
          Seleccione el nuevo estado reglamentario para la matrícula del ciclo <strong>{editingMat?.cicloLectivo}</strong>:
        </p>
        <Form.Group controlId="matNuevoEstado">
          <Form.Label className="small fw-semibold">Estado de Matrícula</Form.Label>
          <Form.Select
            value={nuevoEstado}
            onChange={(e) => setNuevoEstado(e.target.value)}
            style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)' }}
          >
            <option value="PENDIENTE">PENDIENTE</option>
            <option value="ACTIVA">ACTIVA</option>
            <option value="CANCELADA">CANCELADA</option>
          </Form.Select>
        </Form.Group>
      </FormModal>
    </div>
  )
}

export default AdminMatriculasPage
