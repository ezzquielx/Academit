package com.academit.education.service.impl;

import com.academit.education.dto.common.FieldErrorDTO;
import com.academit.education.dto.declaracion.DeclaracionJuradaCreateDTO;
import com.academit.education.dto.declaracion.DeclaracionJuradaDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ConflictException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Alumno;
import com.academit.education.model.Calificacion;
import com.academit.education.model.CicloLectivo;
import com.academit.education.model.Correlativa;
import com.academit.education.model.DeclaracionJurada;
import com.academit.education.model.DeclaracionMateria;
import com.academit.education.model.Materia;
import com.academit.education.model.PlanEstudio;
import com.academit.education.model.Usuario;
import com.academit.education.repository.AlumnoRepository;
import com.academit.education.repository.CalificacionRepository;
import com.academit.education.repository.CicloLectivoRepository;
import com.academit.education.repository.CorrelativaRepository;
import com.academit.education.repository.DeclaracionJuradaRepository;
import com.academit.education.repository.DeclaracionMateriaRepository;
import com.academit.education.repository.MateriaRepository;
import com.academit.education.repository.PlanMateriaRepository;
import com.academit.education.security.CustomUserDetails;
import com.academit.education.service.DeclaracionJuradaService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Declaraciones juradas de materias a cursar (RF51, RN20):
 * una por alumno y ciclo; cada materia debe estar activa y pertenecer al
 * plan del alumno, y todas sus correlativas deben estar aprobadas.
 */
@Service
@Transactional(readOnly = true)
public class DeclaracionJuradaServiceImpl implements DeclaracionJuradaService {

    private static final String ROL_ALUMNO = "ALUMNO";
    private static final String ESTADO_INICIAL = "PENDIENTE";

    private final DeclaracionJuradaRepository declaracionJuradaRepository;
    private final DeclaracionMateriaRepository declaracionMateriaRepository;
    private final AlumnoRepository alumnoRepository;
    private final MateriaRepository materiaRepository;
    private final CicloLectivoRepository cicloLectivoRepository;
    private final PlanMateriaRepository planMateriaRepository;
    private final CorrelativaRepository correlativaRepository;
    private final CalificacionRepository calificacionRepository;

    public DeclaracionJuradaServiceImpl(
            DeclaracionJuradaRepository declaracionJuradaRepository,
            DeclaracionMateriaRepository declaracionMateriaRepository,
            AlumnoRepository alumnoRepository,
            MateriaRepository materiaRepository,
            CicloLectivoRepository cicloLectivoRepository,
            PlanMateriaRepository planMateriaRepository,
            CorrelativaRepository correlativaRepository,
            CalificacionRepository calificacionRepository) {
        this.declaracionJuradaRepository = declaracionJuradaRepository;
        this.declaracionMateriaRepository = declaracionMateriaRepository;
        this.alumnoRepository = alumnoRepository;
        this.materiaRepository = materiaRepository;
        this.cicloLectivoRepository = cicloLectivoRepository;
        this.planMateriaRepository = planMateriaRepository;
        this.correlativaRepository = correlativaRepository;
        this.calificacionRepository = calificacionRepository;
    }

    @Override
    public List<DeclaracionJuradaDTO> listar(CustomUserDetails userDetails, Integer idAlumno, Integer idCiclo) {
        List<DeclaracionJurada> declaraciones;
        if (ROL_ALUMNO.equals(userDetails.getRolNombre())) {
            Alumno alumno = alumnoDelToken(userDetails);
            declaraciones = declaracionJuradaRepository.findByAlumnoId(alumno.getId());
        } else if (idAlumno != null && idCiclo != null) {
            declaraciones = declaracionJuradaRepository.findByAlumnoIdAndCicloId(idAlumno, idCiclo);
        } else if (idAlumno != null) {
            declaraciones = declaracionJuradaRepository.findByAlumnoId(idAlumno);
        } else if (idCiclo != null) {
            declaraciones = declaracionJuradaRepository.findByCicloId(idCiclo);
        } else {
            declaraciones = declaracionJuradaRepository.findAll();
        }
        return declaraciones.stream()
                .sorted(Comparator.comparing(DeclaracionJurada::getId))
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public DeclaracionJuradaDTO presentar(CustomUserDetails userDetails, DeclaracionJuradaCreateDTO request) {
        Alumno alumno = alumnoRepository.findById(request.getIdAlumno())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Alumno no encontrado con ID: " + request.getIdAlumno()));

        // El ALUMNO presenta solo la suya (compara con el id del token)
        if (ROL_ALUMNO.equals(userDetails.getRolNombre())) {
            if (!alumno.getId().equals(alumnoDelToken(userDetails).getId())) {
                throw new AccessDeniedException(
                        "El alumno solo puede presentar su propia declaración jurada");
            }
        }

        CicloLectivo ciclo = cicloLectivoRepository.findById(request.getIdCiclo())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Ciclo lectivo no encontrado con ID: " + request.getIdCiclo()));

        // RN20: una declaración por alumno y ciclo
        if (declaracionJuradaRepository.existsByAlumnoIdAndCicloId(alumno.getId(), ciclo.getId())) {
            throw new ConflictException(
                    "El alumno ya presentó una declaración jurada para el ciclo " + ciclo.getAnio());
        }

        PlanEstudio plan = alumno.getPlan();
        if (plan == null) {
            throw new BusinessRuleException("El alumno no tiene un plan de estudios asignado");
        }

        List<FieldErrorDTO> errores = new ArrayList<>();
        List<Integer> materiasDeclaradas = withDefaults(request.getMateriasDeclaradasIds());
        if (materiasDeclaradas.isEmpty()) {
            throw new BusinessRuleException("Debe declarar al menos una materia a cursar");
        }
        for (int i = 0; i < materiasDeclaradas.size(); i++) {
            Integer idMateria = materiasDeclaradas.get(i);
            Materia materia = materiaRepository.findById(idMateria).orElse(null);
            String posicion = "materiasDeclaradasIds[" + i + "]";
            if (materia == null) {
                errores.add(new FieldErrorDTO(posicion,
                        "Materia no encontrada con ID: " + idMateria));
                continue;
            }
            // Activa y perteneciente al plan del alumno
            if (!Boolean.TRUE.equals(materia.getActiva())) {
                errores.add(new FieldErrorDTO(posicion,
                        "La materia " + materia.getNombre() + " no está activa"));
                continue;
            }
            if (!planMateriaRepository.existsByIdIdPlanAndIdIdMateria(plan.getId(), idMateria)) {
                errores.add(new FieldErrorDTO(posicion,
                        "La materia " + materia.getNombre()
                                + " no pertenece al plan de estudios del alumno"));
                continue;
            }
            // Todas las correlativas del plan deben estar aprobadas
            for (Correlativa correlative : correlativaRepository.findByIdIdPlanAndIdIdMateria(
                    plan.getId(), idMateria)) {
                Integer requerida = correlative.getId().getIdMateriaRequerida();
                if (!materiaAprobada(alumno.getId(), requerida)) {
                    Materia req = materiaRepository.findById(requerida).orElse(null);
                    errores.add(new FieldErrorDTO(posicion,
                            "Correlativa no aprobada: "
                                    + (req != null ? req.getNombre() : "materia " + requerida)
                                    + " es requisito de " + materia.getNombre()));
                }
            }
        }
        if (!errores.isEmpty()) {
            throw new BusinessRuleException(
                    "La declaración jurada no pudo validarse; no se registró ninguna materia",
                    errores);
        }

        DeclaracionJurada declaracion = new DeclaracionJurada();
        declaracion.setAlumno(alumno);
        declaracion.setCiclo(ciclo);
        declaracion.setFecha(LocalDate.now());
        declaracion = declaracionJuradaRepository.save(declaracion);

        for (Integer idMateria : materiasDeclaradas) {
            DeclaracionMateria dm = new DeclaracionMateria();
            dm.setDeclaracion(declaracion);
            dm.setMateria(materiaRepository.findById(idMateria).orElseThrow());
            declaracionMateriaRepository.save(dm);
        }
        return toDto(declaracion);
    }

    // ------------------------------------------------------------------

    private List<Integer> withDefaults(List<Integer> materias) {
        return materias != null ? materias : List.of();
    }

    private Alumno alumnoDelToken(CustomUserDetails userDetails) {
        Usuario usuario = userDetails != null ? userDetails.getUsuario() : null;
        if (usuario == null || usuario.getAlumno() == null) {
            throw new AccessDeniedException(
                    "La declaración jurada solo puede presentarla un alumno");
        }
        return usuario.getAlumno();
    }

    /**
     * DECISIÓN ABIERTA: «materia aprobada» se define como el mínimo del
     * análisis: el alumno tiene notas cargadas en esa materia (via las
     * evaluaciones de sus cursos) y todas son >= 4. Si el análisis la
     * define de otra forma, se ajusta solo este método.
     */
    private boolean materiaAprobada(Integer idAlumno, Integer idMateria) {
        List<Calificacion> notas = calificacionRepository.findByAlumnoId(idAlumno).stream()
                .filter(c -> c.getEvaluacion() != null
                        && c.getEvaluacion().getCursoMateria() != null
                        && c.getEvaluacion().getCursoMateria().getMateria() != null
                        && idMateria.equals(c.getEvaluacion().getCursoMateria().getMateria().getId()))
                .toList();
        return !notas.isEmpty() && notas.stream().allMatch(c -> c.getNota() != null && c.getNota() >= 4);
    }

    private DeclaracionJuradaDTO toDto(DeclaracionJurada declaracion) {
        DeclaracionJuradaDTO dto = new DeclaracionJuradaDTO();
        dto.setIdDeclaracion(declaracion.getId());
        dto.setIdAlumno(declaracion.getAlumno().getId());
        dto.setAlumnoNombre(declaracion.getAlumno().getApellido() + ", "
                + declaracion.getAlumno().getNombre());
        dto.setIdCiclo(declaracion.getCiclo().getId());
        dto.setCicloNombre(declaracion.getCiclo().getNombre() != null
                ? declaracion.getCiclo().getNombre() : String.valueOf(declaracion.getCiclo().getAnio()));
        dto.setFecha(declaracion.getFecha().toString());
        dto.setEstado(ESTADO_INICIAL);

        List<DeclaracionMateria> declaradas = declaracion.getId() != null
                ? declaracionMateriaRepository.findByDeclaracionId(declaracion.getId())
                : List.of();
        List<Integer> ids = declaradas.stream()
                .map(dm -> dm.getMateria().getId())
                .toList();
        dto.setMateriasDeclaradasIds(ids);
        dto.setMateriasNombres(declaradas.stream()
                .map(dm -> dm.getMateria().getNombre())
                .toList());
        return dto;
    }
}
