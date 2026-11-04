package com.academit.education.service.impl;

import com.academit.education.dto.calificacion.CalificacionDTO;
import com.academit.education.dto.calificacion.CalificacionesLoteDTO;
import com.academit.education.dto.calificacion.CalificacionSaveDTO;
import com.academit.education.dto.common.FieldErrorDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Alumno;
import com.academit.education.model.Calificacion;
import com.academit.education.model.CursoMateria;
import com.academit.education.model.Evaluacion;
import com.academit.education.repository.AlumnoRepository;
import com.academit.education.repository.CalificacionRepository;
import com.academit.education.repository.EvaluacionRepository;
import com.academit.education.repository.InscripcionRepository;
import com.academit.education.security.CustomUserDetails;
import com.academit.education.service.AsignacionDocenteService;
import com.academit.education.service.CalificacionService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class CalificacionServiceImpl implements CalificacionService {

    private final CalificacionRepository calificacionRepository;
    private final EvaluacionRepository evaluacionRepository;
    private final AlumnoRepository alumnoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final AsignacionDocenteService asignacionDocenteService;

    public CalificacionServiceImpl(
            CalificacionRepository calificacionRepository,
            EvaluacionRepository evaluacionRepository,
            AlumnoRepository alumnoRepository,
            InscripcionRepository inscripcionRepository,
            AsignacionDocenteService asignacionDocenteService) {
        this.calificacionRepository = calificacionRepository;
        this.evaluacionRepository = evaluacionRepository;
        this.alumnoRepository = alumnoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.asignacionDocenteService = asignacionDocenteService;
    }

    @Override
    public List<CalificacionDTO> listar(CustomUserDetails userDetails, Integer idAlumno, String tipo,
            Integer idMateria, Integer idEvaluacion) {
        boolean esAlumno = userDetails != null && userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ALUMNO"));

        List<Calificacion> calificaciones;
        if (esAlumno) {
            Integer propio = alumnoDeSesion(userDetails);
            if (propio == null) {
                return List.of();
            }
            if (idAlumno != null && !idAlumno.equals(propio)) {
                throw new AccessDeniedException(
                        "El alumno solo puede consultar sus propias calificaciones");
            }
            calificaciones = calificacionRepository.findByAlumnoId(propio);
        } else if (asignacionDocenteService.esAdministrador(userDetails)) {
            calificaciones = calificacionRepository.findAll();
        } else {
            // DOCENTE: solo las calificaciones de sus cátedras
            List<Integer> catedras = asignacionDocenteService.catedrasDelDocente(userDetails);
            List<Integer> evalIds = catedras.isEmpty()
                    ? List.of()
                    : evaluacionRepository.findByCursoMateriaIdIn(catedras).stream()
                            .map(Evaluacion::getId)
                            .toList();
            calificaciones = evalIds.isEmpty()
                    ? List.of()
                    : calificacionRepository.findByEvaluacionIdIn(evalIds);
        }

        return calificaciones.stream()
                .filter(calcal -> matchesTipo(
                        calcal.getEvaluacion() != null && calcal.getEvaluacion().getTipo() != null
                                ? calcal.getEvaluacion().getTipo().getNombre()
                                : null,
                        tipo))
                .filter(calcal -> idMateria == null
                        || calcal.getEvaluacion().getCursoMateria().getMateria().getId().equals(idMateria))
                .filter(calcal -> idEvaluacion == null
                        || calcal.getEvaluacion().getId().equals(idEvaluacion))
                .map(this::toDto)
                .toList();
    }

    private boolean matchesTipo(String evalTipo, String tipoFiltro) {
        if (tipoFiltro == null || tipoFiltro.isBlank()) {
            return true;
        }
        if (evalTipo == null) {
            return false;
        }
        String t = tipoFiltro.trim().toUpperCase();
        String e = evalTipo.trim().toUpperCase();
        if (e.equals(t)) {
            return true;
        }
        return "PARCIAL".equals(t) && e.contains("PARCIAL");
    }

    @Override
    @Transactional
    public CalificacionDTO guardar(CustomUserDetails userDetails, CalificacionSaveDTO request) {
        Evaluacion evaluacion = resolverEvaluacion(request);
        verificarCatedra(userDetails, evaluacion);
        Alumno alumno = validarAlumnoInscripto(evaluacion, request.getIdAlumno());

        Calificacion calificacion = calificacionRepository
                .findByEvaluacionIdAndAlumnoId(evaluacion.getId(), request.getIdAlumno());
        if (calificacion == null) {
            calificacion = new Calificacion();
            calificacion.setEvaluacion(evaluacion);
            calificacion.setAlumno(alumno);
        }
        calificacion.setNota(request.getNota());
        if (request.getObservacion() != null && !request.getObservacion().isBlank()) {
            calificacion.setObservacion(request.getObservacion().trim());
        }
        return toDto(calificacionRepository.save(calificacion));
    }

    @Override
    @Transactional
    public List<CalificacionDTO> guardarLote(CustomUserDetails userDetails, Integer idEvaluacion,
            CalificacionesLoteDTO request) {
        Evaluacion evaluacion = evaluacionRepository.findById(idEvaluacion)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Evaluación no encontrada con ID: " + idEvaluacion));
        verificarCatedra(userDetails, evaluacion);

        // Lote atómico: se validan todos los items y si alguno falla, 400 con
        // detalle por posición y no se guarda nada.
        List<Integer> alumnosIds = request.getCalificaciones().stream()
                .map(CalificacionesLoteDTO.Item::getIdAlumno)
                .distinct()
                .toList();
        Map<Integer, Alumno> alumnos = alumnoRepository.findAllById(alumnosIds).stream()
                .collect(Collectors.toMap(Alumno::getId, Function.identity()));

        List<FieldErrorDTO> errores = new ArrayList<>();
        for (int i = 0; i < request.getCalificaciones().size(); i++) {
            CalificacionesLoteDTO.Item item = request.getCalificaciones().get(i);
            Alumno alumno = alumnos.get(item.getIdAlumno());
            if (alumno == null) {
                errores.add(new FieldErrorDTO("calificaciones[" + i + "].idAlumno",
                        "El alumno ID " + item.getIdAlumno() + " no existe"));
            } else if (!inscripcionRepository.existsByAlumnoIdAndCursoIdAndActivaTrue(
                    item.getIdAlumno(), evaluacion.getCursoMateria().getCurso().getId())) {
                errores.add(new FieldErrorDTO("calificaciones[" + i + "].idAlumno",
                        "El alumno " + alumno.getApellido() + ", " + alumno.getNombre()
                                + " no está inscripto en el curso de la evaluación"));
            }
        }
        if (!errores.isEmpty()) {
            throw new BusinessRuleException(
                    "Se detectaron errores en el lote de calificaciones; no se guardó ninguna nota",
                    errores);
        }

        List<CalificacionDTO> resultados = new ArrayList<>();
        for (CalificacionesLoteDTO.Item item : request.getCalificaciones()) {
            Calificacion calificacion = calificacionRepository
                    .findByEvaluacionIdAndAlumnoId(evaluacion.getId(), item.getIdAlumno());
            if (calificacion == null) {
                calificacion = new Calificacion();
                calificacion.setEvaluacion(evaluacion);
                calificacion.setAlumno(alumnos.get(item.getIdAlumno()));
            }
            calificacion.setNota(item.getNota());
            if (item.getObservacion() != null && !item.getObservacion().isBlank()) {
                calificacion.setObservacion(item.getObservacion().trim());
            }
            resultados.add(toDto(calificacionRepository.save(calificacion)));
        }
        return resultados;
    }

    // ------------------------------------------------------------------

    private Integer alumnoDeSesion(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUsuario() == null
                || userDetails.getUsuario().getAlumno() == null) {
            return null;
        }
        return userDetails.getUsuario().getAlumno().getId();
    }

    /**
     * POST /api/calificaciones identifica la evaluación por (idMateria,
     * evaluacionNombre = descripcion de la evaluación) y, si viene, tipo. Si
     * hay varias, toma la de fecha más reciente.
     */
    private Evaluacion resolverEvaluacion(CalificacionSaveDTO request) {
        List<Evaluacion> candidatas = evaluacionRepository
                .findByCursoMateriaMateriaId(request.getIdMateria()).stream()
                .filter(ev -> ev.getDescripcion() != null
                        && ev.getDescripcion().trim()
                                .equalsIgnoreCase(request.getEvaluacionNombre().trim()))
                .filter(ev -> request.getTipo() == null || request.getTipo().isBlank()
                        || ev.getTipo().getNombre().equalsIgnoreCase(request.getTipo().trim()))
                .toList();
        return candidatas.stream()
                .max(Comparator.comparing(Evaluacion::getFecha)
                        .thenComparing(Evaluacion::getId))
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe una evaluación '" + request.getEvaluacionNombre()
                                + "' para la materia " + request.getIdMateria()));
    }

    private void verificarCatedra(CustomUserDetails userDetails, Evaluacion evaluacion) {
        CursoMateria cm = evaluacion.getCursoMateria();
        asignacionDocenteService.verificarAsignado(userDetails, cm.getCurso().getId(),
                cm.getMateria().getId());
    }

    private Alumno validarAlumnoInscripto(Evaluacion evaluacion, Integer idAlumno) {
        Alumno alumno = alumnoRepository.findById(idAlumno)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alumno no encontrado con ID: " + idAlumno));
        if (!inscripcionRepository.existsByAlumnoIdAndCursoIdAndActivaTrue(
                idAlumno, evaluacion.getCursoMateria().getCurso().getId())) {
            throw new BusinessRuleException(
                    "El alumno debe estar inscripto en el curso de la evaluación");
        }
        return alumno;
    }

    private CalificacionDTO toDto(Calificacion cal) {
        Evaluacion ev = cal.getEvaluacion();
        Alumno a = cal.getAlumno();
        CalificacionDTO dto = new CalificacionDTO();
        dto.setIdCalificacion(cal.getId());
        dto.setIdAlumno(a.getId());
        dto.setAlumnoNombre(a.getNombre() + " " + a.getApellido());
        dto.setIdMateria(ev.getCursoMateria().getMateria().getId());
        dto.setMateriaNombre(ev.getCursoMateria().getMateria().getNombre());
        dto.setEvaluacionNombre(ev.getDescripcion());
        dto.setTipo(ev.getTipo().getNombre());
        dto.setNota(cal.getNota());
        dto.setFecha(ev.getFecha().toString());
        dto.setCondicion(cal.getNota() >= 4 ? "APROBADO" : "DESAPROBADO");
        dto.setObservacion(cal.getObservacion());
        return dto;
    }
}
