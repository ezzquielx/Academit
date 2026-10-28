package com.academit.education.service.impl;

import com.academit.education.dto.alumno.AlumnoCreateDTO;
import com.academit.education.dto.alumno.AlumnoDTO;
import com.academit.education.dto.alumno.AlumnoUpdateDTO;
import com.academit.education.dto.alumno.CalificacionDetalleDTO;
import com.academit.education.dto.alumno.CuotaDetalleDTO;
import com.academit.education.dto.alumno.SituacionAcademicaDTO;
import com.academit.education.dto.alumno.SituacionAdministrativaDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Alumno;
import com.academit.education.model.Asistencia;
import com.academit.education.model.Calificacion;
import com.academit.education.model.Cuota;
import com.academit.education.model.EstadoPersona;
import com.academit.education.model.Inscripcion;
import com.academit.education.model.Matricula;
import com.academit.education.model.PlanEstudio;
import com.academit.education.model.Usuario;
import com.academit.education.repository.AlumnoRepository;
import com.academit.education.repository.AsistenciaRepository;
import com.academit.education.repository.CalificacionRepository;
import com.academit.education.repository.CuotaRepository;
import com.academit.education.repository.EstadoPersonaRepository;
import com.academit.education.repository.InscripcionRepository;
import com.academit.education.repository.MatriculaRepository;
import com.academit.education.repository.PlanEstudioRepository;
import com.academit.education.security.CustomUserDetails;
import com.academit.education.service.AlumnoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class AlumnoServiceImpl implements AlumnoService {

    private final AlumnoRepository alumnoRepository;
    private final PlanEstudioRepository planEstudioRepository;
    private final EstadoPersonaRepository estadoPersonaRepository;
    private final InscripcionRepository inscripcionRepository;
    private final MatriculaRepository matriculaRepository;
    private final CalificacionRepository calificacionRepository;
    private final AsistenciaRepository asistenciaRepository;
    private final CuotaRepository cuotaRepository;

    public AlumnoServiceImpl(
            AlumnoRepository alumnoRepository,
            PlanEstudioRepository planEstudioRepository,
            EstadoPersonaRepository estadoPersonaRepository,
            InscripcionRepository inscripcionRepository,
            MatriculaRepository matriculaRepository,
            CalificacionRepository calificacionRepository,
            AsistenciaRepository asistenciaRepository,
            CuotaRepository cuotaRepository) {
        this.alumnoRepository = alumnoRepository;
        this.planEstudioRepository = planEstudioRepository;
        this.estadoPersonaRepository = estadoPersonaRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.matriculaRepository = matriculaRepository;
        this.calificacionRepository = calificacionRepository;
        this.asistenciaRepository = asistenciaRepository;
        this.cuotaRepository = cuotaRepository;
    }

    @Override
    public List<AlumnoDTO> listarAlumnos() {
        return alumnoRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public AlumnoDTO obtenerPorId(Integer id, CustomUserDetails userDetails) {
        validarAccesoAlumno(id, userDetails);
        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alumno no encontrado con ID: " + id));
        return toDTO(alumno);
    }

    @Override
    @Transactional
    public AlumnoDTO crearAlumno(AlumnoCreateDTO request) {
        if (alumnoRepository.existsByDni(request.getDni())) {
            throw new BusinessRuleException("Ya existe un alumno registrado con el DNI " + request.getDni());
        }

        if (alumnoRepository.existsByEmail(request.getEmail())) {
            throw new BusinessRuleException("Ya existe un alumno registrado con el correo electrónico " + request.getEmail());
        }

        PlanEstudio plan = planEstudioRepository.findById(request.getIdPlan())
                .orElseThrow(() -> new ResourceNotFoundException("Plan de estudios no encontrado con ID: " + request.getIdPlan()));

        if (request.getIdCarrera() != null && !plan.getCarrera().getId().equals(request.getIdCarrera())) {
            throw new BusinessRuleException("El plan de estudios indicado no pertenece a la carrera seleccionada (RN22)");
        }

        LocalDate hoy = LocalDate.now();
        if (request.getFechaNacimiento().isAfter(hoy) || request.getFechaNacimiento().isEqual(hoy)) {
            throw new BusinessRuleException("La fecha de nacimiento debe ser anterior a la fecha actual");
        }

        EstadoPersona estadoActivo = estadoPersonaRepository.findByNombre("ACTIVO")
                .orElseGet(() -> estadoPersonaRepository.findAll().stream()
                        .filter(e -> "ACTIVO".equalsIgnoreCase(e.getNombre()))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Estado de persona ACTIVO no encontrado en el sistema")));

        Alumno alumno = new Alumno();
        alumno.setNombre(request.getNombre().trim());
        alumno.setApellido(request.getApellido().trim());
        alumno.setDni(request.getDni().trim());
        alumno.setEmail(request.getEmail().trim().toLowerCase());
        alumno.setTelefono(request.getTelefono());
        alumno.setDomicilio(request.getDomicilio());
        alumno.setFechaNacimiento(request.getFechaNacimiento());
        alumno.setFechaIngreso(hoy);
        alumno.setPlan(plan);
        alumno.setEstado(estadoActivo);

        Alumno guardado = alumnoRepository.save(alumno);
        return toDTO(guardado);
    }

    @Override
    @Transactional
    public AlumnoDTO actualizarAlumno(Integer id, AlumnoUpdateDTO request) {
        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alumno no encontrado con ID: " + id));

        if (request.getEmail() != null && !request.getEmail().trim().equalsIgnoreCase(alumno.getEmail())) {
            if (alumnoRepository.existsByEmailAndIdNot(request.getEmail().trim(), id)) {
                throw new BusinessRuleException("Ya existe otro alumno registrado con el correo electrónico " + request.getEmail());
            }
            alumno.setEmail(request.getEmail().trim().toLowerCase());
        }

        if (request.getIdPlan() != null && !request.getIdPlan().equals(alumno.getPlan().getId())) {
            PlanEstudio nuevoPlan = planEstudioRepository.findById(request.getIdPlan())
                    .orElseThrow(() -> new ResourceNotFoundException("Plan de estudios no encontrado con ID: " + request.getIdPlan()));
            if (request.getIdCarrera() != null && !nuevoPlan.getCarrera().getId().equals(request.getIdCarrera())) {
                throw new BusinessRuleException("El plan de estudios no corresponde a la carrera seleccionada (RN22)");
            }
            alumno.setPlan(nuevoPlan);
        }

        if (request.getEstado() != null) {
            EstadoPersona nuevoEstado = estadoPersonaRepository.findByNombre(request.getEstado())
                    .orElseThrow(() -> new ResourceNotFoundException("Estado no encontrado: " + request.getEstado()));
            alumno.setEstado(nuevoEstado);
        }

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            alumno.setNombre(request.getNombre().trim());
        }

        if (request.getApellido() != null && !request.getApellido().isBlank()) {
            alumno.setApellido(request.getApellido().trim());
        }

        if (request.getTelefono() != null) {
            alumno.setTelefono(request.getTelefono());
        }

        if (request.getDomicilio() != null) {
            alumno.setDomicilio(request.getDomicilio());
        }

        if (request.getFechaNacimiento() != null) {
            if (request.getFechaNacimiento().isAfter(LocalDate.now())) {
                throw new BusinessRuleException("La fecha de nacimiento debe ser anterior a la fecha actual");
            }
            alumno.setFechaNacimiento(request.getFechaNacimiento());
        }

        Alumno actualizado = alumnoRepository.save(alumno);
        return toDTO(actualizado);
    }

    @Override
    @Transactional
    public void eliminarAlumno(Integer id) {
        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alumno no encontrado con ID: " + id));

        // RN13 / RF09: Baja lógica en lugar de borrado físico para preservar integridad histórica
        EstadoPersona estadoInactivo = estadoPersonaRepository.findByNombre("INACTIVO")
                .orElseGet(() -> estadoPersonaRepository.findAll().stream()
                        .filter(e -> "INACTIVO".equalsIgnoreCase(e.getNombre()))
                        .findFirst()
                        .orElseThrow(() -> new ResourceNotFoundException("Estado de persona INACTIVO no configurado")));

        alumno.setEstado(estadoInactivo);
        alumnoRepository.save(alumno);
    }

    @Override
    public SituacionAcademicaDTO obtenerSituacionAcademica(Integer id, CustomUserDetails userDetails) {
        validarAccesoAlumno(id, userDetails);
        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alumno no encontrado con ID: " + id));

        List<Calificacion> calificaciones = calificacionRepository.findByAlumnoId(id);
        List<Asistencia> asistencias = asistenciaRepository.findByAlumnoId(id);

        double pctAsistencia = 100.0;
        if (!asistencias.isEmpty()) {
            long total = asistencias.size();
            long presentes = asistencias.stream()
                    .filter(a -> a.getEstado() != null &&
                            ("PRESENTE".equalsIgnoreCase(a.getEstado().getNombre()) ||
                             "JUSTIFICADA".equalsIgnoreCase(a.getEstado().getNombre())))
                    .count();
            pctAsistencia = (double) presentes * 100.0 / total;
        }

        SituacionAcademicaDTO situacion = new SituacionAcademicaDTO();
        situacion.setIdAlumno(id);

        int aprobadas = 0;
        int sumaNotas = 0;
        List<CalificacionDetalleDTO> detalles = new ArrayList<>();
        List<String> regularidades = new ArrayList<>();

        for (Calificacion c : calificaciones) {
            int nota = c.getNota() != null ? c.getNota() : 0;
            sumaNotas += nota;
            boolean aprobada = nota >= 4;
            if (aprobada) {
                aprobadas++;
            }

            CalificacionDetalleDTO detalle = new CalificacionDetalleDTO();
            detalle.setIdCalificacion(c.getId());
            detalle.setNota(nota);

            String materiaNombre = "Materia";
            if (c.getEvaluacion() != null && c.getEvaluacion().getCursoMateria() != null
                    && c.getEvaluacion().getCursoMateria().getMateria() != null) {
                materiaNombre = c.getEvaluacion().getCursoMateria().getMateria().getNombre();
            }
            detalle.setMateriaNombre(materiaNombre);

            String evalNombre = "Evaluación";
            String tipoEval = "PARCIAL";
            LocalDate fecha = LocalDate.now();
            if (c.getEvaluacion() != null) {
                if (c.getEvaluacion().getDescripcion() != null) {
                    evalNombre = c.getEvaluacion().getDescripcion();
                }
                if (c.getEvaluacion().getTipo() != null) {
                    tipoEval = c.getEvaluacion().getTipo().getNombre();
                }
                if (c.getEvaluacion().getFecha() != null) {
                    fecha = c.getEvaluacion().getFecha();
                }
            }
            detalle.setEvaluacionNombre(evalNombre);
            detalle.setTipo(tipoEval);
            detalle.setFecha(fecha);

            // Condición académica calculada dinámicamente (RN14, RN21, prompt: regular si nota >= 4 y asist >= 75%)
            String condicion;
            if (aprobada && pctAsistencia >= 75.0) {
                condicion = "REGULAR";
            } else if (aprobada) {
                condicion = "APROBADO";
            } else {
                condicion = "DESAPROBADO";
            }
            detalle.setCondicion(condicion);
            detalle.setObservacion("Asistencia estimada: " + Math.round(pctAsistencia) + "%");
            detalles.add(detalle);

            if (!regularidades.contains(materiaNombre + " - " + condicion)) {
                regularidades.add(materiaNombre + " - " + condicion);
            }
        }

        situacion.setMateriasAprobadas(aprobadas);
        if (!calificaciones.isEmpty()) {
            double prom = (double) sumaNotas / calificaciones.size();
            situacion.setPromedio(BigDecimal.valueOf(prom).setScale(2, RoundingMode.HALF_UP).doubleValue());
        } else {
            situacion.setPromedio(0.0);
        }
        situacion.setCalificaciones(detalles);
        situacion.setRegularidades(regularidades);

        return situacion;
    }

    @Override
    public SituacionAdministrativaDTO obtenerSituacionAdministrativa(Integer id, CustomUserDetails userDetails) {
        validarAccesoAlumno(id, userDetails);
        Alumno alumno = alumnoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Alumno no encontrado con ID: " + id));

        List<Matricula> matriculas = matriculaRepository.findByAlumnoId(id);
        List<Cuota> cuotas = cuotaRepository.findByMatriculaAlumnoId(id);

        SituacionAdministrativaDTO situacion = new SituacionAdministrativaDTO();
        situacion.setIdAlumno(id);

        String estadoMatriculaStr = "PENDIENTE";
        if (!matriculas.isEmpty()) {
            Matricula ultima = matriculas.get(matriculas.size() - 1);
            if (ultima.getEstado() != null) {
                estadoMatriculaStr = ultima.getEstado().getNombre();
            }
        }
        situacion.setMatricula(estadoMatriculaStr);

        int pagas = 0;
        int pendientes = 0;
        BigDecimal deuda = BigDecimal.ZERO;
        BigDecimal abonado = BigDecimal.ZERO;
        boolean tieneVencidas = false;
        List<CuotaDetalleDTO> detalleCuotas = new ArrayList<>();

        for (Cuota c : cuotas) {
            String est = c.getEstado() != null ? c.getEstado().getNombre() : "PENDIENTE";
            BigDecimal imp = c.getImporte() != null ? c.getImporte() : BigDecimal.ZERO;

            if ("PAGADA".equalsIgnoreCase(est)) {
                pagas++;
                abonado = abonado.add(imp);
            } else if ("PENDIENTE".equalsIgnoreCase(est) || "VENCIDA".equalsIgnoreCase(est)) {
                pendientes++;
                deuda = deuda.add(imp);
                if ("VENCIDA".equalsIgnoreCase(est)) {
                    tieneVencidas = true;
                }
            }

            CuotaDetalleDTO d = new CuotaDetalleDTO();
            d.setIdCuota(c.getId());
            d.setConcepto("Cuota " + c.getNumero());
            d.setTipo(c.getTipo() != null ? c.getTipo().getNombre() : "CUOTA_MENSUAL");
            d.setPeriodo("Cuota #" + c.getNumero());
            d.setImporte(imp);
            d.setFechaVencimiento(c.getFechaVencimiento());
            d.setEstado(est);
            detalleCuotas.add(d);
        }

        situacion.setCuotasPagas(pagas);
        situacion.setCuotasPendientes(pendientes);
        situacion.setTotalDeuda(deuda.setScale(2, RoundingMode.HALF_UP).doubleValue());
        situacion.setTotalAbonado(abonado.setScale(2, RoundingMode.HALF_UP).doubleValue());
        situacion.setCuotas(detalleCuotas);

        // Regla de decisión 4b: estadoPago calculado dinámicamente
        if (tieneVencidas) {
            situacion.setEstadoPago("EN_MORA");
        } else if (pendientes == 0 && pagas > 0) {
            situacion.setEstadoPago("AL_DIA");
        } else {
            situacion.setEstadoPago("PENDIENTE");
        }

        return situacion;
    }

    private void validarAccesoAlumno(Integer idAlumno, CustomUserDetails userDetails) {
        if (userDetails == null) {
            return;
        }
        boolean esAlumno = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ALUMNO"));

        if (esAlumno) {
            Usuario usuario = userDetails.getUsuario();
            if (usuario == null || usuario.getAlumno() == null || !usuario.getAlumno().getId().equals(idAlumno)) {
                throw new AccessDeniedException("Acceso denegado: un alumno solo puede consultar su propia información");
            }
        }
    }

    private AlumnoDTO toDTO(Alumno alumno) {
        AlumnoDTO dto = new AlumnoDTO();
        dto.setIdAlumno(alumno.getId());
        dto.setLegajo(String.format("ALU-%04d", 1000 + alumno.getId()));
        dto.setNombre(alumno.getNombre());
        dto.setApellido(alumno.getApellido());
        dto.setDni(alumno.getDni());
        dto.setEmail(alumno.getEmail());
        dto.setTelefono(alumno.getTelefono());
        dto.setDomicilio(alumno.getDomicilio());
        dto.setFechaNacimiento(alumno.getFechaNacimiento());

        if (alumno.getEstado() != null) {
            dto.setEstado(alumno.getEstado().getNombre());
        }

        if (alumno.getPlan() != null) {
            dto.setIdPlan(alumno.getPlan().getId());
            dto.setPlanNombre(alumno.getPlan().getNombre());
            if (alumno.getPlan().getCarrera() != null) {
                dto.setIdCarrera(alumno.getPlan().getCarrera().getId());
                dto.setCarrera(alumno.getPlan().getCarrera().getNombre());
            }
        }

        List<Inscripcion> inscripciones = inscripcionRepository.findByAlumnoIdAndActivaTrue(alumno.getId());
        if (!inscripciones.isEmpty()) {
            Inscripcion insc = inscripciones.get(0);
            if (insc.getCurso() != null) {
                dto.setIdCurso(insc.getCurso().getId());
                dto.setCursoNombre(insc.getCurso().getNombre());
            }
            if (insc.getCiclo() != null) {
                dto.setCicloLectivo(insc.getCiclo().getAnio());
            }
        }

        List<Matricula> matriculas = matriculaRepository.findByAlumnoId(alumno.getId());
        if (!matriculas.isEmpty()) {
            Matricula mat = matriculas.get(matriculas.size() - 1);
            dto.setIdMatricula(mat.getId());
            if (mat.getEstado() != null) {
                dto.setEstadoMatricula(mat.getEstado().getNombre());
            }
            if (dto.getCicloLectivo() == null && mat.getCiclo() != null) {
                dto.setCicloLectivo(mat.getCiclo().getAnio());
            }
        } else {
            dto.setEstadoMatricula("PENDIENTE");
            if (dto.getCicloLectivo() == null) {
                dto.setCicloLectivo(LocalDate.now().getYear());
            }
        }

        return dto;
    }
}

