package com.academit.education.service.impl;

import com.academit.education.dto.asignacion.AsignacionCreateDTO;
import com.academit.education.dto.asignacion.AsignacionDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Curso;
import com.academit.education.model.CursoMateria;
import com.academit.education.model.Docente;
import com.academit.education.model.Materia;
import com.academit.education.repository.CursoMateriaRepository;
import com.academit.education.repository.CursoRepository;
import com.academit.education.repository.DocenteRepository;
import com.academit.education.repository.EvaluacionRepository;
import com.academit.education.repository.MateriaRepository;
import com.academit.education.service.AsignacionService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@Transactional(readOnly = true)
public class AsignacionServiceImpl implements AsignacionService {

    private final CursoMateriaRepository cursoMateriaRepository;
    private final DocenteRepository docenteRepository;
    private final MateriaRepository materiaRepository;
    private final CursoRepository cursoRepository;
    private final EvaluacionRepository evaluacionRepository;

    public AsignacionServiceImpl(
            CursoMateriaRepository cursoMateriaRepository,
            DocenteRepository docenteRepository,
            MateriaRepository materiaRepository,
            CursoRepository cursoRepository,
            EvaluacionRepository evaluacionRepository) {
        this.cursoMateriaRepository = cursoMateriaRepository;
        this.docenteRepository = docenteRepository;
        this.materiaRepository = materiaRepository;
        this.cursoRepository = cursoRepository;
        this.evaluacionRepository = evaluacionRepository;
    }

    @Override
    public List<AsignacionDTO> listarAsignaciones() {
        return cursoMateriaRepository.findAll().stream()
                .map(this::toDTO)
                .toList();
    }

    @Override
    public AsignacionDTO obtenerPorId(Integer id) {
        CursoMateria cm = cursoMateriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + id));
        return toDTO(cm);
    }

    @Override
    @Transactional
    public AsignacionDTO crearAsignacion(AsignacionCreateDTO request) {
        Docente docente = docenteRepository.findById(request.getIdDocente())
                .orElseThrow(() -> new ResourceNotFoundException("Docente no encontrado con ID: " + request.getIdDocente()));

        Materia materia = materiaRepository.findById(request.getIdMateria())
                .orElseThrow(() -> new ResourceNotFoundException("Materia no encontrada con ID: " + request.getIdMateria()));

        Curso curso = cursoRepository.findById(request.getIdCurso())
                .orElseThrow(() -> new ResourceNotFoundException("Curso no encontrado con ID: " + request.getIdCurso()));

        // Regla: El docente debe estar ACTIVO
        if (docente.getEstado() != null && "INACTIVO".equalsIgnoreCase(docente.getEstado().getNombre())) {
            throw new BusinessRuleException("El docente " + docente.getNombre() + " " + docente.getApellido() + " se encuentra INACTIVO y no puede recibir nuevas asignaciones");
        }

        // Regla: Unicidad por (curso, materia, docente)
        if (cursoMateriaRepository.existsByCursoIdAndMateriaIdAndDocenteId(request.getIdCurso(), request.getIdMateria(), request.getIdDocente())) {
            throw new BusinessRuleException("Ya existe una asignación activa para el docente " + docente.getNombre() + " " + docente.getApellido() + " en la materia " + materia.getNombre() + " para el curso seleccionado");
        }

        // Normalización del rol docente a los valores válidos del esquema ('TITULAR', 'ADJUNTO', 'JTP')
        String rol = "TITULAR";
        if (request.getRolDocente() != null && !request.getRolDocente().isBlank()) {
            String upper = request.getRolDocente().trim().toUpperCase();
            if ("TITULAR".equals(upper) || "ADJUNTO".equals(upper) || "JTP".equals(upper)) {
                rol = upper;
            } else if ("AYUDANTE".equals(upper)) {
                rol = "JTP";
            }
        }

        CursoMateria cm = new CursoMateria();
        cm.setDocente(docente);
        cm.setMateria(materia);
        cm.setCurso(curso);
        cm.setRolDocente(rol);

        CursoMateria guardado = cursoMateriaRepository.save(cm);
        return toDTO(guardado);
    }

    @Override
    @Transactional
    public void eliminarAsignacion(Integer id) {
        CursoMateria cm = cursoMateriaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Asignación no encontrada con ID: " + id));

        // Regla: No eliminar asignaciones con evaluaciones cargadas (protección del historial académico)
        if (evaluacionRepository.existsByCursoMateriaId(id)) {
            throw new BusinessRuleException("No se puede eliminar la asignación porque posee evaluaciones históricas asociadas a la cátedra");
        }

        cursoMateriaRepository.delete(cm);
    }

    private AsignacionDTO toDTO(CursoMateria cm) {
        AsignacionDTO dto = new AsignacionDTO();
        dto.setIdAsignacion(cm.getId());

        if (cm.getDocente() != null) {
            dto.setIdDocente(cm.getDocente().getId());
            dto.setDocenteNombre(cm.getDocente().getNombre() + " " + cm.getDocente().getApellido());
        }

        if (cm.getMateria() != null) {
            dto.setIdMateria(cm.getMateria().getId());
            dto.setMateriaNombre(cm.getMateria().getNombre());
        }

        if (cm.getCurso() != null) {
            dto.setIdCurso(cm.getCurso().getId());
            dto.setCursoNombre(cm.getCurso().getNombre());
        }

        dto.setRolDocente(cm.getRolDocente() != null ? cm.getRolDocente() : "TITULAR");
        return dto;
    }
}

