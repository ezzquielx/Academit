package com.academit.education.service.impl;

import com.academit.education.dto.inscripcion.InscripcionCreateDTO;
import com.academit.education.dto.inscripcion.InscripcionDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Alumno;
import com.academit.education.model.CicloLectivo;
import com.academit.education.model.Curso;
import com.academit.education.model.CursoMateria;
import com.academit.education.model.Inscripcion;
import com.academit.education.model.Usuario;
import com.academit.education.repository.AlumnoRepository;
import com.academit.education.repository.CursoMateriaRepository;
import com.academit.education.repository.CursoRepository;
import com.academit.education.repository.InscripcionRepository;
import com.academit.education.security.CustomUserDetails;
import com.academit.education.service.InscripcionService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class InscripcionServiceImpl implements InscripcionService {

    private final InscripcionRepository inscripcionRepository;
    private final AlumnoRepository alumnoRepository;
    private final CursoRepository cursoRepository;
    private final CursoMateriaRepository cursoMateriaRepository;

    public InscripcionServiceImpl(
            InscripcionRepository inscripcionRepository,
            AlumnoRepository alumnoRepository,
            CursoRepository cursoRepository,
            CursoMateriaRepository cursoMateriaRepository) {
        this.inscripcionRepository = inscripcionRepository;
        this.alumnoRepository = alumnoRepository;
        this.cursoRepository = cursoRepository;
        this.cursoMateriaRepository = cursoMateriaRepository;
    }

    @Override
    public List<InscripcionDTO> listarInscripciones(CustomUserDetails userDetails) {
        if (userDetails != null) {
            boolean esAlumno = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ALUMNO"));
            if (esAlumno) {
                Usuario u = userDetails.getUsuario();
                if (u != null && u.getAlumno() != null) {
                    return inscripcionRepository.findByAlumnoId(u.getAlumno().getId()).stream()
                            .map(this::toDTO)
                            .toList();
                }
                return List.of();
            }

            boolean esDocente = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_DOCENTE"));
            if (esDocente) {
                Usuario u = userDetails.getUsuario();
                if (u != null && u.getDocente() != null) {
                    List<CursoMateria> asignaciones = cursoMateriaRepository.findByDocenteId(u.getDocente().getId());
                    List<Integer> cursosIds = asignaciones.stream()
                            .filter(cm -> cm.getCurso() != null)
                            .map(cm -> cm.getCurso().getId())
                            .distinct()
                            .toList();
                    if (!cursosIds.isEmpty()) {
                        return inscripcionRepository.findByCursoIdIn(cursosIds).stream()
                                .map(this::toDTO)
                                .toList();
                    }
                    return List.of();
                }
            }
        }

        return inscripcionRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public InscripcionDTO obtenerPorId(Integer id, CustomUserDetails userDetails) {
        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada con ID: " + id));

        if (userDetails != null) {
            boolean esAlumno = userDetails.getAuthorities().stream()
                    .anyMatch(a -> a.getAuthority().equals("ROLE_ALUMNO"));
            if (esAlumno) {
                Usuario u = userDetails.getUsuario();
                if (u == null || u.getAlumno() == null || !u.getAlumno().getId().equals(inscripcion.getAlumno().getId())) {
                    throw new AccessDeniedException("Acceso denegado: un alumno solo puede consultar sus propias inscripciones");
                }
            }
        }

        return toDTO(inscripcion);
    }

    @Override
    @Transactional
    public InscripcionDTO crearInscripcion(InscripcionCreateDTO request) {
        // Bloqueo pesimista o consulta del curso para evitar condiciones de carrera en cupo (RN03)
        Curso curso = cursoRepository.findByIdForUpdate(request.getIdCurso())
                .orElseGet(() -> cursoRepository.findById(request.getIdCurso())
                        .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado con ID: " + request.getIdCurso())));

        Alumno alumno = alumnoRepository.findById(request.getIdAlumno())
                .orElseThrow(() -> new ResourceNotFoundException("Alumno no encontrado con ID: " + request.getIdAlumno()));

        // Regla 3: El alumno debe encontrarse en estado ACTIVO
        if (alumno.getEstado() != null && "INACTIVO".equalsIgnoreCase(alumno.getEstado().getNombre())) {
            throw new BusinessRuleException("El alumno debe encontrarse en estado ACTIVO para poder inscribirse a una comisión");
        }

        // Regla 3: El curso debe pertenecer a un ciclo lectivo válido
        CicloLectivo ciclo = curso.getCiclo();
        if (ciclo == null) {
            throw new BusinessRuleException("El curso seleccionado no posee un ciclo lectivo asociado");
        }

        if (request.getCicloLectivo() != null && !ciclo.getAnio().equals(request.getCicloLectivo())) {
            throw new BusinessRuleException("El curso no pertenece al ciclo lectivo indicado (" + request.getCicloLectivo() + ")");
        }

        // Regla 1: Una sola inscripción ACTIVA por alumno y ciclo lectivo (RN02)
        if (inscripcionRepository.existsByAlumnoIdAndCicloIdAndActivaTrue(alumno.getId(), ciclo.getId())) {
            throw new BusinessRuleException("El alumno ya posee una inscripción activa en el ciclo lectivo " + ciclo.getAnio() + " (RN02)");
        }

        // Regla 2: Cupo del curso (RN03)
        long inscriptos = inscripcionRepository.countByCursoIdAndActivaTrue(curso.getId());
        if (inscriptos >= curso.getCapacidadMaxima()) {
            throw new BusinessRuleException("El curso \"" + curso.getNombre() + "\" no cuenta con cupos disponibles (capacidad máxima: " + curso.getCapacidadMaxima() + ") (RN03)");
        }

        Inscripcion inscripcion = new Inscripcion();
        inscripcion.setAlumno(alumno);
        inscripcion.setCurso(curso);
        inscripcion.setCiclo(ciclo);
        inscripcion.setFecha(LocalDate.now());
        inscripcion.setActiva(true);

        Inscripcion guardada = inscripcionRepository.save(inscripcion);
        return toDTO(guardada);
    }

    @Override
    @Transactional
    public void cancelarInscripcion(Integer id) {
        Inscripcion inscripcion = inscripcionRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Inscripción no encontrada con ID: " + id));

        // Regla 4: Anular la inscripción (activa = false) liberando el cupo sin borrar físicamente
        inscripcion.setActiva(false);
        inscripcionRepository.save(inscripcion);
    }

    private InscripcionDTO toDTO(Inscripcion inscripcion) {
        InscripcionDTO dto = new InscripcionDTO();
        dto.setIdInscripcion(inscripcion.getId());

        if (inscripcion.getAlumno() != null) {
            dto.setIdAlumno(inscripcion.getAlumno().getId());
            dto.setAlumnoNombre(inscripcion.getAlumno().getNombre() + " " + inscripcion.getAlumno().getApellido());
        }

        if (inscripcion.getCurso() != null) {
            dto.setIdCurso(inscripcion.getCurso().getId());
            dto.setCursoNombre(inscripcion.getCurso().getNombre());
        }

        if (inscripcion.getCiclo() != null) {
            dto.setCicloLectivo(inscripcion.getCiclo().getAnio());
        } else {
            dto.setCicloLectivo(LocalDate.now().getYear());
        }

        dto.setFechaInscripcion(inscripcion.getFecha());
        dto.setEstado(Boolean.TRUE.equals(inscripcion.getActiva()) ? "CONFIRMADA" : "CANCELADA");
        return dto;
    }
}

