package com.academit.education.service.impl;

import com.academit.education.dto.carrera.CarreraCreateDTO;
import com.academit.education.dto.carrera.CarreraDTO;
import com.academit.education.dto.carrera.CarreraUpdateDTO;
import com.academit.education.dto.curso.CursoDTO;
import com.academit.education.dto.planestudio.PlanEstudioDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Carrera;
import com.academit.education.model.Curso;
import com.academit.education.model.Inscripcion;
import com.academit.education.model.PlanEstudio;
import com.academit.education.model.enums.EstadoCarreraEnum;
import com.academit.education.model.enums.EstadoCicloEnum;
import com.academit.education.model.enums.EstadoPlanEstudioEnum;
import com.academit.education.repository.AlumnoRepository;
import com.academit.education.repository.CarreraRepository;
import com.academit.education.repository.CursoRepository;
import com.academit.education.repository.InscripcionRepository;
import com.academit.education.repository.PlanEstudioRepository;
import com.academit.education.repository.PlanMateriaRepository;
import com.academit.education.service.CarreraService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CarreraServiceImpl implements CarreraService {

    private final CarreraRepository carreraRepository;
    private final PlanEstudioRepository planEstudioRepository;
    private final PlanMateriaRepository planMateriaRepository;
    private final CursoRepository cursoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final AlumnoRepository alumnoRepository;

    public CarreraServiceImpl(
            CarreraRepository carreraRepository,
            PlanEstudioRepository planEstudioRepository,
            PlanMateriaRepository planMateriaRepository,
            CursoRepository cursoRepository,
            InscripcionRepository inscripcionRepository,
            AlumnoRepository alumnoRepository) {
        this.carreraRepository = carreraRepository;
        this.planEstudioRepository = planEstudioRepository;
        this.planMateriaRepository = planMateriaRepository;
        this.cursoRepository = cursoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.alumnoRepository = alumnoRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CarreraDTO> listarCarreras() {
        return carreraRepository.findAll().stream()
                .sorted(Comparator.comparing(Carrera::getNombre))
                .map(this::toDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public CarreraDTO obtenerPorId(Integer id) {
        return toDto(obtenerCarrera(id));
    }

    @Override
    @Transactional
    public CarreraDTO crearCarrera(CarreraCreateDTO request) {
        validarDuracion(request.getDuracionAnios());
        String codigo = request.getCodigo().trim();
        String nombre = request.getNombre().trim();

        if (carreraRepository.existsByCodigo(codigo)) {
            throw new BusinessRuleException(
                    "Ya existe una carrera con el código " + codigo);
        }
        if (carreraRepository.existsByNombre(nombre)) {
            throw new BusinessRuleException(
                    "Ya existe una carrera con el nombre " + nombre);
        }

        Carrera carrera = new Carrera();
        carrera.setCodigo(codigo);
        carrera.setNombre(nombre);
        carrera.setTituloOtorgado(request.getTituloOtorgado().trim());
        carrera.setDuracionAnios(request.getDuracionAnios());
        carrera.setResolucion(request.getResolucion() != null
                ? request.getResolucion().trim() : null);
        // Una carrera recién registrada nace activa (estado del contrato)
        carrera.setEstado(EstadoCarreraEnum.ACTIVA.name());

        return toDto(carreraRepository.save(carrera));
    }

    @Override
    @Transactional
    public CarreraDTO actualizarCarrera(Integer id, CarreraUpdateDTO request) {
        Carrera carrera = obtenerCarrera(id);

        // Actualización parcial: solo se cambian los campos enviados
        if (request.getCodigo() != null && !request.getCodigo().isBlank()) {
            String codigo = request.getCodigo().trim();
            if (carreraRepository.existsByCodigoAndIdNot(codigo, id)) {
                throw new BusinessRuleException(
                        "Ya existe una carrera con el código " + codigo);
            }
            carrera.setCodigo(codigo);
        }
        if (request.getNombre() != null && !request.getNombre().isBlank()) {
            String nombre = request.getNombre().trim();
            if (carreraRepository.existsByNombreAndIdNot(nombre, id)) {
                throw new BusinessRuleException(
                        "Ya existe una carrera con el nombre " + nombre);
            }
            carrera.setNombre(nombre);
        }
        if (request.getTituloOtorgado() != null && !request.getTituloOtorgado().isBlank()) {
            carrera.setTituloOtorgado(request.getTituloOtorgado().trim());
        }
        if (request.getDuracionAnios() != null) {
            validarDuracion(request.getDuracionAnios());
            carrera.setDuracionAnios(request.getDuracionAnios());
        }
        if (request.getResolucion() != null) {
            carrera.setResolucion(request.getResolucion().isBlank()
                    ? null : request.getResolucion().trim());
        }
        if (request.getEstado() != null) {
            carrera.setEstado(validarEstado(request.getEstado()));
        }

        return toDto(carreraRepository.save(carrera));
    }

    @Override
    @Transactional
    public void darDeBajaCarrera(Integer id) {
        Carrera carrera = obtenerCarrera(id);

        if (EstadoCarreraEnum.INACTIVA.name().equals(carrera.getEstado())) {
            throw new BusinessRuleException(
                    "La carrera " + carrera.getNombre() + " ya está inactiva");
        }

        // Baja lógica: la carrera queda INACTIVA, no se borra la fila.
        if (alumnoRepository.existsByPlanCarreraIdAndEstadoNombre(id, "ACTIVO")) {
            throw new BusinessRuleException(
                    "No se puede dar de baja la carrera " + carrera.getNombre()
                            + " porque tiene alumnos activos");
        }
        if (cursoRepository.existsByCarreraIdAndCicloEstado(
                id, EstadoCicloEnum.ACTIVO.name())) {
            throw new BusinessRuleException(
                    "No se puede dar de baja la carrera " + carrera.getNombre()
                            + " porque tiene cursos en un ciclo lectivo activo");
        }

        carrera.setEstado(EstadoCarreraEnum.INACTIVA.name());
        carreraRepository.save(carrera);
    }

    @Override
    @Transactional(readOnly = true)
    public List<PlanEstudioDTO> listarPlanesDeCarrera(Integer idCarrera) {
        obtenerCarrera(idCarrera);
        return planEstudioRepository.findByCarreraId(idCarrera).stream()
                .sorted(Comparator.comparing(PlanEstudio::getAnioAprobacion).reversed())
                .map(this::toPlanDto)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public List<CursoDTO> listarCursosDeCarrera(Integer idCarrera) {
        obtenerCarrera(idCarrera);
        List<Curso> cursos = cursoRepository.findByCarreraId(idCarrera).stream()
                .sorted(Comparator.comparing(Curso::getId))
                .collect(Collectors.toList());

        // Inscriptos por curso en una sola consulta (evita N+1)
        List<Integer> cursosIds = cursos.stream()
                .map(Curso::getId).collect(Collectors.toList());
        Map<Integer, Long> inscriptosPorCurso = cursosIds.isEmpty() ? Map.of()
                : inscripcionRepository.findByCursoIdIn(cursosIds).stream()
                        .filter(i -> Boolean.TRUE.equals(i.getActiva()))
                        .collect(Collectors.groupingBy(
                                i -> i.getCurso().getId(), Collectors.counting()));

        return cursos.stream()
                .map(curso -> toCursoDto(curso, inscriptosPorCurso))
                .collect(Collectors.toList());
    }

    // ------------------------------------------------------------------

    private Carrera obtenerCarrera(Integer id) {
        return carreraRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Carrera no encontrada con ID: " + id));
    }

    private void validarDuracion(Integer duracionAnios) {
        if (duracionAnios == null || duracionAnios <= 0) {
            throw new BusinessRuleException(
                    "La duración de la carrera debe ser mayor que 0 años");
        }
    }

    private String validarEstado(String estado) {
        try {
            return EstadoCarreraEnum.valueOf(estado.trim()).name();
        } catch (IllegalArgumentException | NullPointerException ex) {
            throw new BusinessRuleException(
                    "Estado de carrera inválido: " + estado
                            + " (valores permitidos: ACTIVA, INACTIVA)");
        }
    }

    private CarreraDTO toDto(Carrera carrera) {
        CarreraDTO dto = new CarreraDTO();
        dto.setIdCarrera(carrera.getId());
        dto.setCodigo(carrera.getCodigo());
        dto.setNombre(carrera.getNombre());
        dto.setTituloOtorgado(carrera.getTituloOtorgado());
        dto.setDuracionAnios(carrera.getDuracionAnios());
        dto.setResolucion(carrera.getResolucion());
        dto.setEstado(carrera.getEstado());
        return dto;
    }

    private PlanEstudioDTO toPlanDto(PlanEstudio plan) {
        PlanEstudioDTO dto = new PlanEstudioDTO();
        dto.setIdPlan(plan.getId());
        dto.setIdCarrera(plan.getCarrera().getId());
        dto.setCarreraNombre(plan.getCarrera().getNombre());
        dto.setCodigo(plan.getCodigo());
        dto.setNombre(plan.getNombre());
        dto.setAnioAprobacion(plan.getAnioAprobacion());
        // Campo calculado: materias del plan, nunca persistido
        dto.setCantidadMaterias(planMateriaRepository.findByPlanId(plan.getId()).size());
        dto.setEstado(plan.getEstado() != null
                ? plan.getEstado() : EstadoPlanEstudioEnum.VIGENTE.name());
        return dto;
    }

    private CursoDTO toCursoDto(Curso curso, Map<Integer, Long> inscriptosPorCurso) {
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
        int inscriptos = inscriptosPorCurso
                .getOrDefault(curso.getId(), 0L).intValue();
        dto.setInscriptos(inscriptos);
        dto.setCuposDisponibles(curso.getCapacidadMaxima() - inscriptos);
        return dto;
    }
}
