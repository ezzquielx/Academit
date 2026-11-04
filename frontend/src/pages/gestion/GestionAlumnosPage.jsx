import { useState, useEffect } from 'react'
import { Button, Form, Row, Col } from 'react-bootstrap'
import PageHeader from '../../components/PageHeader'
import DataTable from '../../components/DataTable'
import StatusBadge from '../../components/StatusBadge'
import FormModal from '../../components/FormModal'
import ConfirmModal from '../../components/ConfirmModal'
import { useToast } from '../../context/ToastContext'
import { getAlumnos, createAlumno, updateAlumno, deleteAlumno } from '../../api/alumnos'
import { getCarreras, getPlanesEstudio } from '../../api/carreras'

export const GestionAlumnosPage = () => {
  const { showToast } = useToast()
  const [alumnos, setAlumnos] = useState([])
  const [carreras, setCarreras] = useState([])
  const [planes, setPlanes] = useState([])
  const [loading, setLoading] = useState(false)

  // Estado del modal de alta/edición
  const [showModal, setShowModal] = useState(false)
  const [editingAlumno, setEditingAlumno] = useState(null)
  const [formData, setFormData] = useState({
    nombre: '',
    apellido: '',
    dni: '',
    email: '',
    telefono: '',
    domicilio: '',
    fechaNacimiento: '',
    idCarrera: 1,
    carrera: 'Tecnicatura Superior en Desarrollo de Software',
    idPlan: 1,
    planNombre: 'Plan de Estudios 2024',
  })
  const [errors, setErrors] = useState({})

  // Estado del modal de confirmación de baja lógica
  const [alumnoToDelete, setAlumnoToDelete] = useState(null)

  const cargarAlumnos = async () => {
    try {
      const data = await getAlumnos()
      setAlumnos(data)
    } catch {
      showToast('Error al cargar alumnos', 'danger')
    }
  }

  useEffect(() => {
    cargarAlumnos()
    getCarreras().then(setCarreras).catch(() => {})
    getPlanesEstudio().then(setPlanes).catch(() => {})
  }, [])

  // Validaciones del formulario
  const validarFormulario = () => {
    const newErrors = {}
    if (!formData.nombre.trim()) {
      newErrors.nombre = 'El nombre es obligatorio'
    } else if (formData.nombre.trim().length < 2) {
      newErrors.nombre = 'El nombre debe tener al menos 2 caracteres'
    }

    if (!formData.apellido.trim()) {
      newErrors.apellido = 'El apellido es obligatorio'
    } else if (formData.apellido.trim().length < 2) {
      newErrors.apellido = 'El apellido debe tener al menos 2 caracteres'
    }

    const dniLimpio = formData.dni.replace(/\D/g, '')
    if (!dniLimpio) {
      newErrors.dni = 'El DNI es obligatorio'
    } else if (dniLimpio.length < 7 || dniLimpio.length > 8) {
      newErrors.dni = 'El DNI debe tener entre 7 y 8 dígitos numéricos'
    }

    const emailRegex = /^[^\s@]+@[^\s@]+\.[^\s@]+$/
    if (!formData.email.trim()) {
      newErrors.email = 'El correo electrónico es obligatorio'
    } else if (!emailRegex.test(formData.email.trim())) {
      newErrors.email = 'El formato del correo electrónico es inválido'
    }

    setErrors(newErrors)
    return Object.keys(newErrors).length === 0
  }

  const handleOpenNuevo = () => {
    setEditingAlumno(null)
    const primeraCarrera = carreras[0] || { idCarrera: 1, nombre: 'Tecnicatura Superior en Desarrollo de Software' }
    const primerPlan = planes.find((p) => p.idCarrera === primeraCarrera.idCarrera) || { idPlan: 1, nombre: 'Plan de Estudios 2024' }
    setFormData({
      nombre: '',
      apellido: '',
      dni: '',
      email: '',
      telefono: '',
      domicilio: '',
      fechaNacimiento: '',
      idCarrera: primeraCarrera.idCarrera,
      carrera: primeraCarrera.nombre,
      idPlan: primerPlan.idPlan,
      planNombre: primerPlan.nombre,
    })
    setErrors({})
    setShowModal(true)
  }

  const handleOpenEditar = (alumno) => {
    setEditingAlumno(alumno)
    setFormData({
      nombre: alumno.nombre || '',
      apellido: alumno.apellido || '',
      dni: alumno.dni || '',
      email: alumno.email || '',
      telefono: alumno.telefono || '',
      domicilio: alumno.domicilio || '',
      fechaNacimiento: alumno.fechaNacimiento || '',
      idCarrera: alumno.idCarrera || 1,
      carrera: alumno.carrera || 'Tecnicatura Superior en Desarrollo de Software',
      idPlan: alumno.idPlan || 1,
      planNombre: alumno.planNombre || 'Plan de Estudios 2024',
      estado: alumno.estado || 'ACTIVO',
    })
    setErrors({})
    setShowModal(true)
  }

  const handleValidarAlumno = async (alumno) => {
    setLoading(true)
    try {
      await updateAlumno(alumno.idAlumno, {
        nombre: alumno.nombre,
        apellido: alumno.apellido,
        dni: alumno.dni,
        email: alumno.email,
        telefono: alumno.telefono,
        domicilio: alumno.domicilio,
        fechaNacimiento: alumno.fechaNacimiento,
        idCarrera: alumno.idCarrera,
        idPlan: alumno.idPlan,
        estado: 'ACTIVO',
      })
      showToast(`¡Alumno/a ${alumno.apellido}, ${alumno.nombre} validado/a con éxito! Su cuenta ya está activa para ingresar.`, 'success')
      cargarAlumnos()
    } catch (err) {
      showToast(err.message || 'Error al validar alumno', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const handleSubmit = async (e) => {
    e.preventDefault()
    if (!validarFormulario()) return

    setLoading(true)
    try {
      if (editingAlumno) {
        await updateAlumno(editingAlumno.idAlumno, formData)
        showToast(`Alumno/a ${formData.apellido}, ${formData.nombre} modificado exitosamente`, 'success')
      } else {
        await createAlumno(formData)
        showToast(`Alumno/a ${formData.apellido}, ${formData.nombre} registrado con éxito`, 'success')
      }
      setShowModal(false)
      cargarAlumnos()
    } catch (err) {
      showToast(err.message || 'Error al guardar alumno', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const handleConfirmBaja = async () => {
    if (!alumnoToDelete) return
    setLoading(true)
    try {
      await deleteAlumno(alumnoToDelete.idAlumno)
      showToast(`Baja lógica efectuada para ${alumnoToDelete.apellido}, ${alumnoToDelete.nombre}`, 'warning')
      setAlumnoToDelete(null)
      cargarAlumnos()
    } catch (err) {
      showToast(err.message || 'Error al dar de baja', 'danger')
    } finally {
      setLoading(false)
    }
  }

  const columns = [
    { key: 'legajo', label: 'Legajo', width: '110px' },
    {
      key: 'nombreCompleto',
      label: 'Apellido y Nombre',
      render: (row) => (
        <div>
          <strong style={{ color: 'var(--m-onSurface)' }}>
            {row.apellido}, {row.nombre}
          </strong>
          <div className="text-muted small">{row.email}</div>
        </div>
      ),
    },
    { key: 'dni', label: 'DNI' },
    {
      key: 'carrera',
      label: 'Carrera y Plan',
      render: (row) => (
        <div>
          <span style={{ color: 'var(--m-onSurface)' }}>{row.carrera}</span>
          <div className="small text-muted">{row.planNombre || 'Plan 2024'}</div>
        </div>
      ),
    },
    { key: 'cursoNombre', label: 'Comisión Actual' },
    {
      key: 'estadoMatricula',
      label: 'Matrícula',
      render: (row) => <StatusBadge status={row.estadoMatricula} />,
    },
    {
      key: 'estado',
      label: 'Condición',
      render: (row) => (
        <div>
          <StatusBadge status={row.estado} />
          {row.estado === 'INACTIVO' && (
            <div className="small text-warning fw-semibold mt-1">
              <i className="bi bi-clock-history me-1" />Pendiente
            </div>
          )}
        </div>
      ),
    },
    {
      key: 'acciones',
      label: 'Acciones',
      align: 'right',
      render: (row) => (
        <div className="d-flex justify-content-end gap-1">
          {row.estado === 'INACTIVO' && (
            <Button
              variant="outline-success"
              size="sm"
              onClick={() => handleValidarAlumno(row)}
              title="Validar y activar cuenta del alumno"
              aria-label="Validar y activar cuenta del alumno"
            >
              <i className="bi bi-person-check" />
            </Button>
          )}
          <Button
            variant="outline-primary"
            size="sm"
            onClick={() => handleOpenEditar(row)}
            title="Editar alumno"
            aria-label="Editar alumno"
          >
            <i className="bi bi-pencil" />
          </Button>
          {row.estado !== 'INACTIVO' && (
            <Button
              variant="outline-danger"
              size="sm"
              onClick={() => setAlumnoToDelete(row)}
              title="Baja lógica"
              aria-label="Baja lógica de alumno"
            >
              <i className="bi bi-person-x" />
            </Button>
          )}
        </div>
      ),
    },
  ]

  return (
    <div>
      <PageHeader
        title="Gestión de Alumnos"
        subtitle="Pantalla de referencia: listado, búsqueda, alta/modificación con validación y baja lógica"
        icon="bi-people"
        actions={
          <Button variant="primary" size="sm" onClick={handleOpenNuevo} className="d-flex align-items-center gap-1">
            <i className="bi bi-person-plus-fill" />
            <span>Nuevo Alumno</span>
          </Button>
        }
      />

      <DataTable
        columns={columns}
        data={alumnos}
        keyField="idAlumno"
        searchPlaceholder="Buscar por nombre, apellido, DNI o legajo..."
        searchKeys={['nombre', 'apellido', 'dni', 'legajo', 'email']}
      />

      {/* Modal de Alta y Modificación */}
      <FormModal
        show={showModal}
        onHide={() => setShowModal(false)}
        onSubmit={handleSubmit}
        title={editingAlumno ? `Editar Alumno - ${editingAlumno.legajo}` : 'Registrar Nuevo Alumno'}
        submitText={editingAlumno ? 'Guardar Cambios' : 'Registrar Alumno'}
        loading={loading}
      >
        <Row className="g-3">
          <Col md={6}>
            <Form.Group controlId="aluNombre">
              <Form.Label className="small fw-semibold">Nombre <span className="text-danger">*</span></Form.Label>
              <Form.Control
                type="text"
                placeholder="Nombre"
                value={formData.nombre}
                onChange={(e) => setFormData({ ...formData, nombre: e.target.value })}
                isInvalid={Boolean(errors.nombre)}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)', borderColor: 'var(--m-outline)' }}
              />
              <Form.Control.Feedback type="invalid">{errors.nombre}</Form.Control.Feedback>
            </Form.Group>
          </Col>

          <Col md={6}>
            <Form.Group controlId="aluApellido">
              <Form.Label className="small fw-semibold">Apellido <span className="text-danger">*</span></Form.Label>
              <Form.Control
                type="text"
                placeholder="Apellido"
                value={formData.apellido}
                onChange={(e) => setFormData({ ...formData, apellido: e.target.value })}
                isInvalid={Boolean(errors.apellido)}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)', borderColor: 'var(--m-outline)' }}
              />
              <Form.Control.Feedback type="invalid">{errors.apellido}</Form.Control.Feedback>
            </Form.Group>
          </Col>

          <Col md={6}>
            <Form.Group controlId="aluDni">
              <Form.Label className="small fw-semibold">DNI (7 u 8 dígitos) <span className="text-danger">*</span></Form.Label>
              <Form.Control
                type="text"
                placeholder="Ej. 42111222"
                value={formData.dni}
                onChange={(e) => setFormData({ ...formData, dni: e.target.value })}
                isInvalid={Boolean(errors.dni)}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)', borderColor: 'var(--m-outline)' }}
              />
              <Form.Control.Feedback type="invalid">{errors.dni}</Form.Control.Feedback>
            </Form.Group>
          </Col>

          <Col md={6}>
            <Form.Group controlId="aluEmail">
              <Form.Label className="small fw-semibold">Correo Electrónico <span className="text-danger">*</span></Form.Label>
              <Form.Control
                type="email"
                placeholder="ejemplo@instituto.edu.ar"
                value={formData.email}
                onChange={(e) => setFormData({ ...formData, email: e.target.value })}
                isInvalid={Boolean(errors.email)}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)', borderColor: 'var(--m-outline)' }}
              />
              <Form.Control.Feedback type="invalid">{errors.email}</Form.Control.Feedback>
            </Form.Group>
          </Col>

          <Col md={6}>
            <Form.Group controlId="aluTelefono">
              <Form.Label className="small fw-semibold">Teléfono</Form.Label>
              <Form.Control
                type="text"
                placeholder="11-XXXX-XXXX"
                value={formData.telefono}
                onChange={(e) => setFormData({ ...formData, telefono: e.target.value })}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)', borderColor: 'var(--m-outline)' }}
              />
            </Form.Group>
          </Col>

          <Col md={6}>
            <Form.Group controlId="aluFechaNac">
              <Form.Label className="small fw-semibold">Fecha de Nacimiento</Form.Label>
              <Form.Control
                type="date"
                value={formData.fechaNacimiento}
                onChange={(e) => setFormData({ ...formData, fechaNacimiento: e.target.value })}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)', borderColor: 'var(--m-outline)' }}
              />
            </Form.Group>
          </Col>

          <Col md={12}>
            <Form.Group controlId="aluDomicilio">
              <Form.Label className="small fw-semibold">Domicilio</Form.Label>
              <Form.Control
                type="text"
                placeholder="Calle, número, localidad"
                value={formData.domicilio}
                onChange={(e) => setFormData({ ...formData, domicilio: e.target.value })}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)', borderColor: 'var(--m-outline)' }}
              />
            </Form.Group>
          </Col>

          {/* Selectores de Carrera y Plan de Estudios */}
          <Col md={6}>
            <Form.Group controlId="aluCarreraSelect">
              <Form.Label className="small fw-semibold">Carrera Académica <span className="text-danger">*</span></Form.Label>
              <Form.Select
                value={formData.idCarrera}
                onChange={(e) => {
                  const idC = Number(e.target.value)
                  const car = carreras.find((c) => c.idCarrera === idC)
                  const planRel = planes.find((p) => p.idCarrera === idC)
                  setFormData({
                    ...formData,
                    idCarrera: idC,
                    carrera: car ? car.nombre : formData.carrera,
                    idPlan: planRel ? planRel.idPlan : formData.idPlan,
                    planNombre: planRel ? planRel.nombre : formData.planNombre,
                  })
                }}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)', borderColor: 'var(--m-outline)' }}
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
            <Form.Group controlId="aluPlanSelect">
              <Form.Label className="small fw-semibold">Plan de Estudios <span className="text-danger">*</span></Form.Label>
              <Form.Select
                value={formData.idPlan}
                onChange={(e) => {
                  const idP = Number(e.target.value)
                  const pl = planes.find((p) => p.idPlan === idP)
                  setFormData({
                    ...formData,
                    idPlan: idP,
                    planNombre: pl ? pl.nombre : formData.planNombre,
                  })
                }}
                style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)', borderColor: 'var(--m-outline)' }}
              >
                {planes
                  .filter((p) => p.idCarrera === Number(formData.idCarrera))
                  .map((p) => (
                    <option key={p.idPlan} value={p.idPlan}>
                      {p.nombre} ({p.codigo})
                    </option>
                  ))}
              </Form.Select>
            </Form.Group>
          </Col>

          {editingAlumno && (
            <Col md={12}>
              <Form.Group controlId="aluEstadoSelect">
                <Form.Label className="small fw-semibold">Condición del Alumno / Estado de Cuenta <span className="text-danger">*</span></Form.Label>
                <Form.Select
                  value={formData.estado || 'ACTIVO'}
                  onChange={(e) => setFormData({ ...formData, estado: e.target.value })}
                  style={{ backgroundColor: 'var(--m-surfaceVariant)', color: 'var(--m-onSurface)', borderColor: 'var(--m-outline)' }}
                >
                  <option value="ACTIVO">ACTIVO (Cuenta habilitada para cursar e ingresar)</option>
                  <option value="INACTIVO">INACTIVO (Pendiente de validación o Baja)</option>
                </Form.Select>
              </Form.Group>
            </Col>
          )}
        </Row>
      </FormModal>

      {/* Modal de Baja Lógica */}
      <ConfirmModal
        show={Boolean(alumnoToDelete)}
        onHide={() => setAlumnoToDelete(null)}
        onConfirm={handleConfirmBaja}
        title="Confirmar Baja Lógica de Alumno"
        message={`¿Está seguro de realizar la baja lógica del estudiante ${alumnoToDelete?.apellido}, ${alumnoToDelete?.nombre} (Legajo: ${alumnoToDelete?.legajo})? Su condición pasará a INACTIVO.`}
        confirmText="Confirmar Baja"
        variant="danger"
        loading={loading}
      />
    </div>
  )
}

export default GestionAlumnosPage
