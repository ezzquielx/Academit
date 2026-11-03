package com.academit.education.service.impl;

import com.academit.education.dto.calificacion.EvaluacionCreateDTO;
import com.academit.education.dto.calificacion.EvaluacionDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.CursoMateria;
import com.academit.education.model.Evaluacion;
import com.academit.education.model.TipoEvaluacion;
import com.academit.education.repository.CursoMateriaRepository;
import com.academit.education.repository.EvaluacionRepository;
import com.academit.education.repository.TipoEvaluacionRepository;
import com.academit.education.security.CustomUserDetails;
import com.academit.education.service.AsignacionDocenteService;
import com.academit.education.service.EvaluacionService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class EvaluacionServiceImpl implements EvaluacionService {

    private static final List<String> TIPOS_VALIDOS = List.of("PARCIAL", "TRABAJO_PRACTICO");

    private final EvaluacionRepository evaluacionRepository;
    private final CursoMateriaRepository cursoMateriaRepository;
    private final TipoEvaluacionRepository tipoEvaluacionRepository;
    private final AsignacionDocenteService asignacionDocenteService;

    public EvaluacionServiceImpl(
            EvaluacionRepository evaluacionRepository,
            CursoMateriaRepository cursoMateriaRepository,
            TipoEvaluacionRepository tipoEvaluacionRepository,
            AsignacionDocenteService asignacionDocenteService) {
        this.evaluacionRepository = evaluacionRepository;
        this.cursoMateriaRepository = cursoMateriaRepository;
        this.tipoEvaluacionRepository = tipoEvaluacionRepository;
        this.asignacionDocenteService = asignacionDocenteService;
    }

    @Override
    public List<EvaluacionDTO> listar(CustomUserDetails userDetails, String tipo, Integer idCurso) {
        List<Integer> catedras;
        if (asignacionDocenteService.esAdministrador(userDetails)) {
            // ADMINISTRADOR consulta todo (x-roles); DOCENTE solo sus cátedras
            catedras = null;
        } else {
            catedras = asignacionDocenteService.catedrasDelDocente(userDetails);
            if (catedras.isEmpty()) {
                return List.of();
            }
        }

        List<Evaluacion> evaluaciones = catedras != null
                ? evaluacionRepository.findByCursoMateriaIdIn(catedras)
                : evaluacionRepository.findAll();

        return evaluaciones.stream()
                .filter(ev -> tipo == null || tipo.isBlank()
                        || ev.getTipo().getNombre().equalsIgnoreCase(tipo.trim()))
                .filter(ev -> idCurso == null || ev.getCursoMateria().getCurso().getId().equals(idCurso))
                .sorted(Comparator.comparing(Evaluacion::getId))
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public EvaluacionDTO crear(CustomUserDetails userDetails, EvaluacionCreateDTO request) {
        String tipo = validarTipo(request.getTipo());
        LocalDate fecha = leerFecha(request.getFecha());

        // Cátedra destino: debe existir el par (curso, materia) con idCurso e
        // idMateria del contrato; si hay varias cátedras del mismo curso y
        // materia, usa la del docente del token (RN11).
        List<CursoMateria> catedras = cursoMateriaRepository
                .findByCursoIdAndMateriaId(request.getIdCurso(), request.getIdMateria());
        if (catedras.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No existe una cátedra para el curso " + request.getIdCurso()
                            + " y la materia " + request.getIdMateria());
        }
        Integer idDocente = asignacionDocenteService.idDocente(userDetails);
        if (idDocente == null) {
            throw new AccessDeniedException(
                    "Solo un docente puede crear evaluaciones de su cátedra");
        }
        if (!asignacionDocenteService.estaAsignado(idDocente,
                request.getIdCurso(), request.getIdMateria())) {
            throw new AccessDeniedException(
                    "Solo el docente asignado a la cátedra puede crear evaluaciones (RN11)");
        }
        CursoMateria catedra = catedras.stream()
                .filter(cm -> cm.getDocente().getId().equals(idDocente))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException(
                        "Solo el docente asignado a la cátedra puede crear evaluaciones (RN11)"));

        TipoEvaluacion tipoEval = tipoEvaluacionRepository.findByNombre(tipo)
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de evaluación desconocido: " + tipo));

        Evaluacion evaluacion = new Evaluacion();
        evaluacion.setCursoMateria(catedra);
        evaluacion.setFecha(fecha);
        evaluacion.setTipo(tipoEval);
        evaluacion.setDescripcion(request.getDescripcion().trim());
        return toDto(evaluacionRepository.save(evaluacion));
    }

    // ------------------------------------------------------------------

    private String validarTipo(String tipo) {
        String normalizado = tipo.trim().toUpperCase();
        if (!TIPOS_VALIDOS.contains(normalizado)) {
            throw new BusinessRuleException(
                    "Tipo de evaluación inválido: " + tipo
                            + " (valores permitidos: PARCIAL, TRABAJO_PRACTICO)");
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

    public EvaluacionDTO toDto(Evaluacion ev) {
        CursoMateria cm = ev.getCursoMateria();
        EvaluacionDTO dto = new EvaluacionDTO();
        dto.setIdEvaluacion(ev.getId());
        dto.setIdMateria(cm.getMateria().getId());
        dto.setMateriaNombre(cm.getMateria().getNombre());
        dto.setIdCurso(cm.getCurso().getId());
        dto.setCursoNombre(cm.getCurso().getNombre());
        dto.setFecha(ev.getFecha().toString());
        dto.setTipo(ev.getTipo().getNombre());
        dto.setDescripcion(ev.getDescripcion());
        return dto;
    }
}
