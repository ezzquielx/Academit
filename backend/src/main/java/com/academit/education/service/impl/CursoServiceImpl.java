package com.academit.education.service.impl;

import com.academit.education.dto.curso.CursoCreateDTO;
import com.academit.education.dto.curso.CursoDTO;
import com.academit.education.dto.curso.CursoUpdateDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Carrera;
import com.academit.education.model.CicloLectivo;
import com.academit.education.model.Curso;
import com.academit.education.model.Inscripcion;
import com.academit.education.model.PlanEstudio;
import com.academit.education.repository.CarreraRepository;
import com.academit.education.repository.CicloLectivoRepository;
import com.academit.education.repository.CursoRepository;
import com.academit.education.repository.InscripcionRepository;
import com.academit.education.repository.PlanEstudioRepository;
import com.academit.education.service.CursoService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CursoServiceImpl implements CursoService {

    private final CursoRepository cursoRepository;
    private final CarreraRepository carreraRepository;
    private final PlanEstudioRepository planEstudioRepository;
    private final CicloLectivoRepository cicloLectivoRepository;
    private final InscripcionRepository inscripcionRepository;

    public CursoServiceImpl(
            CursoRepository cursoRepository,
            CarreraRepository carreraRepository,
            PlanEstudioRepository planEstudioRepository,
            CicloLectivoRepository cicloLectivoRepository,
            InscripcionRepository inscripcionRepository) {
        this.cursoRepository = cursoRepository;
        this.carreraRepository = carreraRepository;
        this.planEstudioRepository = planEstudioRepository;
        this.cicloLectivoRepository = cicloLectivoRepository;
        this.inscripcionRepository = inscripcionRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CursoDTO> listarCursos() {
        List<Curso> cursos = cursoRepository.findAll().stream()
                .sorted(Comparator.comparing(Curso::getId))
                .collect(Collectors.toList());
        return toDtosConCupo(cursos);
    }

    @Override
    @Transactional
    public CursoDTO crearCurso(CursoCreateDTO request) {
        String turno = validarTurno(request.getTurno());

        // RN23: el curso pertenece a un único ciclo lectivo y a una carrera,
        // ambos deben existir. cicloLectivo llega como año (mock del frontend).
        CicloLectivo ciclo = cicloLectivoRepository.findByAnio(request.getCicloLectivo())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "No existe un ciclo lectivo para el año " + request.getCicloLectivo()));
        Carrera carrera = carreraRepository.findById(request.getIdCarrera())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Carrera no encontrada con ID: " + request.getIdCarrera()));
        PlanEstudio plan = planEstudioRepository.findById(request.getIdPlan())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Plan de estudios no encontrado con ID: " + request.getIdPlan()));

        if (!plan.getCarrera().getId().equals(carrera.getId())) {
            throw new BusinessRuleException(
                    "El plan de estudios indicado no pertenece a la carrera seleccionada (RN22)");
        }

        String nombre = request.getNombre().trim();
        if (cursoRepository.existsByNombreAndCicloIdAndCarreraId(
                nombre, ciclo.getId(), carrera.getId())) {
            throw new BusinessRuleException(
                    "Ya existe un curso con el nombre " + nombre
                            + " para esa carrera y ciclo lectivo (RN23)");
        }
        if (cursoRepository.existsByCodigo(request.getCodigo().trim())) {
            throw new BusinessRuleException(
                    "Ya existe un curso con el código " + request.getCodigo().trim());
        }

        Curso curso = new Curso();
        curso.setCodigo(request.getCodigo().trim());
        curso.setNombre(nombre);
        curso.setTurno(turno);
        curso.setCapacidadMaxima(request.getCapacidadMaxima());
        curso.setAula(request.getAula() != null && !request.getAula().isBlank()
                ? request.getAula().trim() : null);
        curso.setCiclo(ciclo);
        curso.setCarrera(carrera);
        curso.setPlan(plan);

        return toDtoConCupo(cursoRepository.save(curso), null);
    }

    @Override
    @Transactional
    public CursoDTO actualizarCurso(Integer id, CursoUpdateDTO request) {
        Curso curso = cursoRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Curso no encontrado con ID: " + id));

        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            String nombre = request.getNombre().trim();
            if (!nombre.equals(curso.getNombre())
                    && cursoRepository.existsByNombreAndCicloIdAndCarreraIdAndIdNot(
                            nombre, curso.getCiclo().getId(), curso.getCarrera().getId(), id)) {
                throw new BusinessRuleException(
                        "Ya existe un curso con el nombre " + nombre
                                + " para esa carrera y ciclo lectivo (RN23)");
            }
            curso.setNombre(nombre);
        }
        if (request.getTurno() != null) {
            curso.setTurno(validarTurno(request.getTurno()));
        }
        if (request.getAula() != null) {
            curso.setAula(request.getAula().isBlank() ? null : request.getAula().trim());
        }
        if (request.getCapacidadMaxima() != null) {
            // RN03 en reverse: no dejar la capacidad por debajo de los inscriptos
            List<Inscripcion> inscripciones = inscripcionRepository.findByCursoIdIn(List.of(id));
            long activos = inscripciones.stream()
                    .filter(i -> Boolean.TRUE.equals(i.getActiva())).count();
            if (request.getCapacidadMaxima() < activos) {
                throw new BusinessRuleException(
                        "La capacidad no puede ser menor que los " + activos
                                + " alumnos activos inscriptos en el curso");
            }
            curso.setCapacidadMaxima(request.getCapacidadMaxima());
        }

        // Reload with inscriptos for the response
        return toDtoConCupo(cursoRepository.save(curso),
                Map.of(id, activosInscriptos(curso.getId())));
    }

    // ------------------------------------------------------------------

    private long activosInscriptos(Integer idCurso) {
        return inscripcionRepository.findByCursoIdIn(List.of(idCurso)).stream()
                .filter(i -> Boolean.TRUE.equals(i.getActiva())).count();
    }

    private String validarTurno(String turno) {
        if (turno == null) {
            throw new BusinessRuleException("El turno es obligatorio");
        }
        String normalizado = turno.trim().toUpperCase();
        if (!List.of("MANANA", "TARDE", "NOCHE").contains(normalizado)) {
            throw new BusinessRuleException(
                    "Turno inválido: " + turno
                            + " (valores permitidos: MANANA, TARDE, NOCHE)");
        }
        return normalizado;
    }

    private List<CursoDTO> toDtosConCupo(List<Curso> cursos) {
        List<Integer> ids = cursos.stream().map(Curso::getId).collect(Collectors.toList());
        Map<Integer, Long> inscriptos = ids.isEmpty() ? Map.of()
                : inscripcionRepository.findByCursoIdIn(ids).stream()
                        .filter(i -> Boolean.TRUE.equals(i.getActiva()))
                        .collect(Collectors.groupingBy(
                                i -> i.getCurso().getId(), Collectors.counting()));
        return cursos.stream()
                .map(c -> toDtoConCupo(c, inscriptos))
                .collect(Collectors.toList());
    }

    private CursoDTO toDtoConCupo(Curso curso, Map<Integer, Long> inscriptosPorCurso) {
        long inscriptos = inscriptosPorCurso == null
                ? activosInscriptos(curso.getId())
                : inscriptosPorCurso.getOrDefault(curso.getId(), 0L);
        CursoDTO dto = new CursoDTO();
        dto.setIdCurso(curso.getId());
        dto.setCodigo(curso.getCodigo());
        dto.setNombre(curso.getNombre());
        dto.setIdCarrera(curso.getCarrera().getId());
        dto.setCarreraNombre(curso.getCarrera().getNombre());
        dto.setIdPlan(curso.getPlan().getId());
        dto.setPlanNombre(curso.getPlan().getNombre());
        dto.setTurno(curso.getTurno());
        dto.setCapacidadMaxima(curso.getCapacidadMaxima());
        dto.setAula(curso.getAula());
        dto.setCicloLectivo(curso.getCiclo().getAnio());
        // Campos calculados del contrato, nunca persistidos
        dto.setInscriptos((int) inscriptos);
        dto.setCuposDisponibles(curso.getCapacidadMaxima() - (int) inscriptos);
        return dto;
    }
}
