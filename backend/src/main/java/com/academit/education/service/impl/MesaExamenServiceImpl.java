package com.academit.education.service.impl;

import com.academit.education.dto.mesa.MesaExamenCreateDTO;
import com.academit.education.dto.mesa.MesaExamenDTO;
import com.academit.education.dto.mesa.MesaExamenUpdateDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ConflictException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Alumno;
import com.academit.education.model.InscripcionMesa;
import com.academit.education.model.Materia;
import com.academit.education.model.MesaExamen;
import com.academit.education.model.PlanMateria;
import com.academit.education.model.Usuario;
import com.academit.education.repository.AlumnoRepository;
import com.academit.education.repository.CarreraRepository;
import com.academit.education.repository.InscripcionMesaRepository;
import com.academit.education.repository.MateriaRepository;
import com.academit.education.repository.MesaExamenRepository;
import com.academit.education.repository.PlanMateriaRepository;
import com.academit.education.security.CustomUserDetails;
import com.academit.education.service.MesaExamenService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class MesaExamenServiceImpl implements MesaExamenService {

    private static final List<String> TURNOS_VALIDOS = List.of("MANANA", "TARDE", "NOCHE");
    private static final List<String> ESTADOS_VALIDOS = List.of("ABIERTA", "CERRADA", "FINALIZADA");

    private final MesaExamenRepository mesaExamenRepository;
    private final InscripcionMesaRepository inscripcionMesaRepository;
    private final MateriaRepository materiaRepository;
    private final CarreraRepository carreraRepository;
    private final PlanMateriaRepository planMateriaRepository;
    private final AlumnoRepository alumnoRepository;

    public MesaExamenServiceImpl(
            MesaExamenRepository mesaExamenRepository,
            InscripcionMesaRepository inscripcionMesaRepository,
            MateriaRepository materiaRepository,
            CarreraRepository carreraRepository,
            PlanMateriaRepository planMateriaRepository,
            AlumnoRepository alumnoRepository) {
        this.mesaExamenRepository = mesaExamenRepository;
        this.inscripcionMesaRepository = inscripcionMesaRepository;
        this.materiaRepository = materiaRepository;
        this.carreraRepository = carreraRepository;
        this.planMateriaRepository = planMateriaRepository;
        this.alumnoRepository = alumnoRepository;
    }

    @Override
    public List<MesaExamenDTO> listar() {
        return mesaExamenRepository.findAll().stream()
                .sorted(Comparator.comparing(MesaExamen::getId))
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public MesaExamenDTO crear(MesaExamenCreateDTO request) {
        String turno = validarTurno(request.getTurno());
        LocalDate fecha = leerFecha(request.getFecha());

        Materia materia = materiaRepository.findById(request.getIdMateria())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Materia no encontrada con ID: " + request.getIdMateria()));
        carreraRepository.findById(request.getIdCarrera())
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Carrera no encontrada con ID: " + request.getIdCarrera()));

        // La materia debe pertenecer a la carrera indicada (via planes)
        boolean pertenece = planMateriaRepository.findByMateriaId(materia.getId()).stream()
                .anyMatch(pm -> pm.getPlan() != null
                        && pm.getPlan().getCarrera() != null
                        && pm.getPlan().getCarrera().getId().equals(request.getIdCarrera()));
        if (!pertenece) {
            throw new BusinessRuleException(
                    "La materia " + materia.getNombre() + " no pertenece a la carrera indicada");
        }

        MesaExamen mesa = new MesaExamen();
        mesa.setMateria(materia);
        mesa.setFecha(fecha);
        mesa.setCupo(request.getCupoMaximo());
        mesa.setTurno(turno);
        mesa.setTribunal(request.getTribunal() == null || request.getTribunal().isBlank()
                ? null : request.getTribunal().trim());
        mesa.setHora(request.getHora() == null || request.getHora().isBlank()
                ? null : request.getHora().trim());
        mesa.setAula(request.getAula() == null || request.getAula().isBlank()
                ? null : request.getAula().trim());
        mesa.setEstado("ABIERTA");
        return toDto(mesaExamenRepository.save(mesa));
    }

    @Override
    @Transactional
    public MesaExamenDTO actualizar(Integer id, MesaExamenUpdateDTO request) {
        MesaExamen mesa = mesaExamenRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mesa de examen no encontrada con ID: " + id));

        if (request.getFecha() != null && !request.getFecha().isBlank()) {
            mesa.setFecha(leerFecha(request.getFecha()));
        }
        if (request.getHora() != null) {
            mesa.setHora(request.getHora().isBlank() ? null : request.getHora().trim());
        }
        if (request.getTurno() != null) {
            mesa.setTurno(validarTurno(request.getTurno()));
        }
        if (request.getTribunal() != null) {
            mesa.setTribunal(request.getTribunal().isBlank() ? null : request.getTribunal().trim());
        }
        if (request.getAula() != null) {
            mesa.setAula(request.getAula().isBlank() ? null : request.getAula().trim());
        }
        if (request.getEstado() != null) {
            String estado = request.getEstado().trim().toUpperCase();
            if (!ESTADOS_VALIDOS.contains(estado)) {
                throw new BusinessRuleException(
                        "Estado de mesa inválido: " + request.getEstado()
                                + " (valores permitidos: ABIERTA, CERRADA, FINALIZADA)");
            }
            mesa.setEstado(estado);
        }
        if (request.getCupoMaximo() != null) {
            long activos = inscripcionMesaRepository.countByMesaIdAndActivaTrue(id);
            if (request.getCupoMaximo() < activos) {
                throw new BusinessRuleException(
                        "El cupo no puede ser menor que los " + activos
                                + " alumnos inscriptos en la mesa");
            }
            mesa.setCupo(request.getCupoMaximo());
        }
        return toDto(mesaExamenRepository.save(mesa));
    }

    @Override
    @Transactional
    public MesaExamenDTO inscribir(CustomUserDetails userDetails, Integer idMesa) {
        MesaExamen mesa = mesaExamenRepository.findById(idMesa)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mesa de examen no encontrada con ID: " + idMesa));
        Alumno alumno = alumnoDelToken(userDetails);

        // RN51: mesa futura y abierta
        if (!"ABIERTA".equals(mesa.getEstado())) {
            throw new ConflictException(
                    "La mesa de examen no está abierta para inscripciones");
        }
        if (!mesa.getFecha().isAfter(LocalDate.now())) {
            throw new BusinessRuleException(
                    "No se puede inscribir a una mesa con fecha pasada o de hoy (" + mesa.getFecha() + ")");
        }

        // Alumno ACTIVO
        if (alumno.getEstado() == null || !"ACTIVO".equals(alumno.getEstado().getNombre())) {
            throw new BusinessRuleException(
                    "El alumno debe estar ACTIVO para inscribirse a una mesa");
        }

        // Duplicado (una sola inscripción activa por alumno y mesa; RN19)
        if (inscripcionMesaRepository.existsByMesaIdAndAlumnoIdAndActivaTrue(idMesa, alumno.getId())) {
            throw new ConflictException(
                    "Ya se encuentra inscripto en esta mesa de examen");
        }

        // Cupo: inscriptos activos < cupo
        long inscriptos = inscripcionMesaRepository.countByMesaIdAndActivaTrue(idMesa);
        if (inscriptos >= mesa.getCupo()) {
            throw new ConflictException(
                    "Sin cupos disponibles: la mesa ya tiene " + inscriptos + " inscriptos");
        }

        InscripcionMesa inscripcion = new InscripcionMesa();
        inscripcion.setMesa(mesa);
        inscripcion.setAlumno(alumno);
        inscripcion.setActiva(true);
        inscripcionMesaRepository.save(inscripcion);
        return toDto(mesa);
    }

    @Override
    @Transactional
    public MesaExamenDTO darDeBaja(CustomUserDetails userDetails, Integer idMesa, Integer idInscripcion) {
        MesaExamen mesa = mesaExamenRepository.findById(idMesa)
                .orElseThrow(() -> new ResourceNotFoundException(
                        "Mesa de examen no encontrada con ID: " + idMesa));
        Alumno alumno = alumnoDelToken(userDetails);

        InscripcionMesa inscripcion = inscripcionMesaRepository.findById(idInscripcion)
                .orElse(null);
        if (inscripcion == null || !inscripcion.getMesa().getId().equals(idMesa)) {
            // Intentar buscar por mesa y alumno si idInscripcion coincidía con idAlumno
            inscripcion = inscripcionMesaRepository.findFirstByMesaIdAndAlumnoIdAndActivaTrue(idMesa, alumno.getId())
                    .orElseThrow(() -> new ResourceNotFoundException(
                            "Inscripción a mesa no encontrada con ID: " + idInscripcion));
        }

        // El alumno solo anula la suya
        if (!inscripcion.getAlumno().getId().equals(alumno.getId())) {
            throw new AccessDeniedException(
                    "El alumno solo puede darse de baja de su propia inscripción");
        }
        if (!Boolean.TRUE.equals(inscripcion.getActiva())) {
            throw new ConflictException(
                    "La inscripción ya está dada de baja");
        }

        inscripcion.setActiva(false);
        inscripcionMesaRepository.save(inscripcion);
        return toDto(mesa);
    }

    // ------------------------------------------------------------------

    private Alumno alumnoDelToken(CustomUserDetails userDetails) {
        Usuario usuario = userDetails != null ? userDetails.getUsuario() : null;
        if (usuario == null || usuario.getAlumno() == null) {
            throw new AccessDeniedException(
                    "La inscripción a mesas es solo para alumnos");
        }
        return usuario.getAlumno();
    }

    private String validarTurno(String turno) {
        String normalizado = turno.trim().toUpperCase();
        if (!TURNOS_VALIDOS.contains(normalizado)) {
            throw new BusinessRuleException(
                    "Turno inválido: " + turno + " (valores permitidos: MANANA, TARDE, NOCHE)");
        }
        return normalizado;
    }

    private LocalDate leerFecha(String fecha) {
        try {
            return LocalDate.parse(fecha.trim());
        } catch (DateTimeParseException ex) {
            throw new BusinessRuleException(
                    "Fecha inválida: se espera el formato YYYY-MM-DD");
        }
    }

    private MesaExamenDTO toDto(MesaExamen mesa) {
        MesaExamenDTO dto = new MesaExamenDTO();
        dto.setIdMesa(mesa.getId());
        dto.setIdMateria(mesa.getMateria().getId());
        dto.setMateriaNombre(mesa.getMateria().getNombre());
        dto.setFecha(mesa.getFecha().toString());
        dto.setHora(mesa.getHora());
        dto.setTurno(mesa.getTurno());
        dto.setTribunal(mesa.getTribunal());
        dto.setAula(mesa.getAula());
        dto.setCupoMaximo(mesa.getCupo());
        dto.setEstado(mesa.getEstado());

        // Carrera desnormalizada: primer plan de la materia
        planMateriaRepository.findByMateriaId(mesa.getMateria().getId()).stream()
                .map(PlanMateria::getPlan)
                .filter(plan -> plan != null && plan.getCarrera() != null)
                .findFirst()
                .ifPresent(plan -> {
                    dto.setIdCarrera(plan.getCarrera().getId());
                    dto.setCarreraNombre(plan.getCarrera().getNombre());
                });

        List<InscripcionMesa> activas = inscripcionMesaRepository.findByMesaIdAndActivaTrue(mesa.getId());
        dto.setInscriptos(activas.size());
        dto.setCuposDisponibles(Math.max(mesa.getCupo() - activas.size(), 0));
        dto.setAlumnosInscriptosIds(activas.stream()
                .map(ins -> ins.getAlumno().getId())
                .toList());
        return dto;
    }
}
