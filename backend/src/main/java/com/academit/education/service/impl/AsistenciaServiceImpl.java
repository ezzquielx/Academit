package com.academit.education.service.impl;

import com.academit.education.dto.asistencia.AsistenciaDTO;
import com.academit.education.dto.asistencia.AsistenciasLoteDTO;
import com.academit.education.dto.asistencia.SituacionMateriaDTO;
import com.academit.education.dto.asistencia.SuccessResponseDTO;
import com.academit.education.dto.common.FieldErrorDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Alumno;
import com.academit.education.model.Asistencia;
import com.academit.education.model.CursoMateria;
import com.academit.education.model.Curso;
import com.academit.education.model.EstadoAsistencia;
import com.academit.education.model.Inscripcion;
import com.academit.education.model.Materia;
import com.academit.education.repository.AlumnoRepository;
import com.academit.education.repository.AsistenciaRepository;
import com.academit.education.repository.CalificacionRepository;
import com.academit.education.repository.CursoMateriaRepository;
import com.academit.education.repository.CursoRepository;
import com.academit.education.repository.EstadoAsistenciaRepository;
import com.academit.education.repository.InscripcionRepository;
import com.academit.education.repository.MateriaRepository;
import com.academit.education.security.CustomUserDetails;
import com.academit.education.service.AsignacionDocenteService;
import com.academit.education.service.AsistenciaService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
public class AsistenciaServiceImpl implements AsistenciaService {

    private static final List<String> ESTADOS_VALIDOS =
            List.of("PRESENTE", "AUSENTE", "TARDANZA", "JUSTIFICADO");
    private static final int PORCENTAJE_REGULARIDAD = 75;

    private final AsistenciaRepository asistenciaRepository;
    private final AlumnoRepository alumnoRepository;
    private final InscripcionRepository inscripcionRepository;
    private final CursoMateriaRepository cursoMateriaRepository;
    private final CursoRepository cursoRepository;
    private final MateriaRepository materiaRepository;
    private final EstadoAsistenciaRepository estadoAsistenciaRepository;
    private final CalificacionRepository calificacionRepository;
    private final AsignacionDocenteService asignacionDocenteService;

    public AsistenciaServiceImpl(
            AsistenciaRepository asistenciaRepository,
            AlumnoRepository alumnoRepository,
            InscripcionRepository inscripcionRepository,
            CursoMateriaRepository cursoMateriaRepository,
            CursoRepository cursoRepository,
            MateriaRepository materiaRepository,
            EstadoAsistenciaRepository estadoAsistenciaRepository,
            CalificacionRepository calificacionRepository,
            AsignacionDocenteService asignacionDocenteService) {
        this.asistenciaRepository = asistenciaRepository;
        this.alumnoRepository = alumnoRepository;
        this.inscripcionRepository = inscripcionRepository;
        this.cursoMateriaRepository = cursoMateriaRepository;
        this.cursoRepository = cursoRepository;
        this.materiaRepository = materiaRepository;
        this.estadoAsistenciaRepository = estadoAsistenciaRepository;
        this.calificacionRepository = calificacionRepository;
        this.asignacionDocenteService = asignacionDocenteService;
    }

    @Override
    public List<AsistenciaDTO> listar(CustomUserDetails userDetails,
            Integer idAlumno, Integer idCurso, String fecha) {
        LocalDate fechaFiltro = null;
        if (fecha != null && !fecha.isBlank()) {
            try {
                fechaFiltro = LocalDate.parse(fecha.trim());
            } catch (DateTimeParseException ex) {
                throw new BusinessRuleException(
                        "Fecha inválida: se espera el formato YYYY-MM-DD");
            }
        }
        final LocalDate fechaParseada = fechaFiltro;

        boolean esAlumno = userDetails != null && userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ALUMNO"));

        List<Asistencia> asistencias;
        if (esAlumno) {
            Integer propio = alumnoDeSesion(userDetails);
            if (propio == null) {
                return List.of();
            }
            if (idAlumno != null && !idAlumno.equals(propio)) {
                throw new AccessDeniedException(
                        "El alumno solo puede consultar sus propias asistencias");
            }
            asistencias = asistenciaRepository.findByAlumnoId(propio).stream()
                    .filter(a -> idCurso == null || a.getCurso().getId().equals(idCurso))
                    .filter(a -> fechaParseada == null || a.getFecha().equals(fechaParseada))
                    .toList();
        } else if (asignacionDocenteService.esAdministrador(userDetails)) {
            asistencias = filtrar(asistenciaRepository.findAll(), idAlumno, idCurso, fechaFiltro);
        } else {
            // DOCENTE: asistencias de los cursos de sus cátedras
            Set<Integer> cursosDeDocente = new HashSet<>();
            Integer idDocente = asignacionDocenteService.idDocente(userDetails);
            if (idDocente != null) {
                cursoMateriaRepository.findByDocenteId(idDocente)
                        .forEach(cm -> cursosDeDocente.add(cm.getCurso().getId()));
            }
            if (cursosDeDocente.isEmpty()) {
                return List.of();
            }
            List<Asistencia> base = idAlumno != null
                    ? asistenciaRepository.findByAlumnoId(idAlumno)
                    : idCurso != null
                            ? asistenciaRepository.findByCursoId(idCurso)
                            : asistenciaRepository.findAll();
            asistencias = filtrar(base, idAlumno, idCurso, fechaFiltro).stream()
                    .filter(a -> cursosDeDocente.contains(a.getCurso().getId()))
                    .toList();
        }

        List<Asistencia> ordenadas = new ArrayList<>(asistencias);
        ordenadas.sort(java.util.Comparator.comparing(Asistencia::getId));
        return ordenadas.stream()
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public SuccessResponseDTO registrarLote(CustomUserDetails userDetails, AsistenciasLoteDTO request) {
        List<FieldErrorDTO> errores = new ArrayList<>();

        // Cachés de la base para el lote
        Set<Integer> alumnosIds = request.getAsistencias().stream()
                .map(AsistenciasLoteDTO.Item::getIdAlumno).collect(Collectors.toSet());
        Map<Integer, Alumno> alumnos = alumnoRepository.findAllById(alumnosIds).stream()
                .collect(Collectors.toMap(Alumno::getId, Function.identity()));

        Set<String> fechas = request.getAsistencias().stream()
                .map(AsistenciasLoteDTO.Item::getFecha).collect(Collectors.toSet());
        Map<String, LocalDate> fechasParseadas = new LinkedHashMap<>();
        for (String f : fechas) {
            try {
                fechasParseadas.put(f, LocalDate.parse(f.trim()));
            } catch (DateTimeParseException ex) {
                /* fecha inválida: se reporta por item */
            }
        }

        for (int i = 0; i < request.getAsistencias().size(); i++) {
            AsistenciasLoteDTO.Item item = request.getAsistencias().get(i);
            Alumno alumno = alumnos.get(item.getIdAlumno());
            if (alumno == null) {
                errores.add(new FieldErrorDTO("asistencias[" + i + "].idAlumno",
                        "El alumno ID " + item.getIdAlumno() + " no existe"));
                continue;
            }
            LocalDate fechaItem = item.getFecha() != null ? fechasParseadas.get(item.getFecha()) : null;
            if (fechaItem == null) {
                errores.add(new FieldErrorDTO("asistencias[" + i + "].fecha",
                        "Fecha inválida: se espera el formato YYYY-MM-DD"));
            } else if (fechaItem.isAfter(LocalDate.now())) {
                errores.add(new FieldErrorDTO("asistencias[" + i + "].fecha",
                        "La fecha no puede ser futura (RN15)"));
            }
            String estado = item.getEstado() != null ? item.getEstado().trim().toUpperCase() : "";
            if (!ESTADOS_VALIDOS.contains(estado)) {
                errores.add(new FieldErrorDTO("asistencias[" + i + "].estado",
                        "Estado inválido: " + item.getEstado()
                                + " (valores permitidos: PRESENTE, AUSENTE, TARDANZA, JUSTIFICADO)"));
            }
            if (!inscripcionRepository.existsByAlumnoIdAndCursoIdAndActivaTrue(
                    item.getIdAlumno(), item.getIdCurso())) {
                errores.add(new FieldErrorDTO("asistencias[" + i + "].idAlumno",
                        "El alumno " + alumno.getApellido() + ", " + alumno.getNombre()
                                + " no está inscripto en el curso " + item.getIdCurso()));
            }
            if (item.getIdMateria() != null
                    && !cursoMateriaRepository.existsByCursoIdAndMateriaId(item.getIdCurso(), item.getIdMateria())) {
                errores.add(new FieldErrorDTO("asistencias[" + i + "].idMateria",
                        "No existe una cátedra para el curso " + item.getIdCurso()
                                + " y la materia " + item.getIdMateria()));
            }
        }
        if (!errores.isEmpty()) {
            throw new BusinessRuleException(
                    "Se detectaron errores en el lote de asistencias; no se registró ninguna", errores);
        }

        // RN12: solo el docente asignado a cada cátedra del lote registra
        if (!asignacionDocenteService.esAdministrador(userDetails)) {
            Set<String> paresCatedra = request.getAsistencias().stream()
                    .map(it -> it.getIdCurso() + ":" + it.getIdMateria())
                    .collect(Collectors.toSet());
            for (String par : paresCatedra) {
                String[] id = par.split(":");
                asignacionDocenteService.verificarAsignado(userDetails,
                        Integer.valueOf(id[0]), Integer.valueOf(id[1]));
            }
        }

        Map<String, EstadoAsistencia> estados = new LinkedHashMap<>();
        for (String estado : ESTADOS_VALIDOS) {
            estadoAsistenciaRepository.findByNombre(estado)
                    .ifPresent(e -> estados.put(estado, e));
        }

        for (AsistenciasLoteDTO.Item item : request.getAsistencias()) {
            LocalDate fechaItem = fechasParseadas.get(item.getFecha());
            String estadoNombre = item.getEstado().trim().toUpperCase();
            EstadoAsistencia estado = estados.get(estadoNombre);
            Optional<Asistencia> existente = asistenciaRepository
                    .findByAlumnoIdAndCursoIdAndMateriaIdAndFecha(
                            item.getIdAlumno(), item.getIdCurso(), item.getIdMateria(), fechaItem);
            Asistencia asistencia;
            if (existente.isPresent()) {
                asistencia = existente.get();
                asistencia.setEstado(estado);
            } else {
                asistencia = new Asistencia();
                asistencia.setAlumno(alumnos.get(item.getIdAlumno()));
                asistencia.setCurso(cursoRepository.findById(item.getIdCurso())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Curso no encontrado con ID: " + item.getIdCurso())));
                asistencia.setMateria(materiaRepository.findById(item.getIdMateria())
                        .orElseThrow(() -> new ResourceNotFoundException(
                                "Materia no encontrada con ID: " + item.getIdMateria())));
                asistencia.setFecha(fechaItem);
                asistencia.setEstado(estado);
            }
            asistenciaRepository.save(asistencia);
        }

        return SuccessResponseDTO.ok(
                "Asistencias registradas: " + request.getAsistencias().size());
    }

    @Override
    public List<SituacionMateriaDTO> situacionAcademica(CustomUserDetails userDetails, Integer idAlumno) {
        boolean esAlumno = userDetails != null && userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ALUMNO"));
        if (esAlumno) {
            Integer propio = alumnoDeSesion(userDetails);
            if (!idAlumno.equals(propio)) {
                throw new AccessDeniedException(
                        "El alumno solo puede consultar su propia situación académica");
            }
        }
        if (alumnoDeSesionById(idAlumno) == null) {
            throw new ResourceNotFoundException("Alumno no encontrado con ID: " + idAlumno);
        }

        // Cátedras relevantes: docente → sus cátedras; alumno y administrador
        // → cátedras de los cursos en que el alumno está inscripto
        List<CursoMateria> catedras;
        if (!esAlumno && !asignacionDocenteService.esAdministrador(userDetails)) {
            Integer idDocente = asignacionDocenteService.idDocente(userDetails);
            catedras = idDocente == null
                    ? List.of()
                    : cursoMateriaRepository.findByDocenteId(idDocente);
        } else {
            List<Integer> cursosIds = inscripcionRepository.findByAlumnoIdAndActivaTrue(idAlumno)
                    .stream().map(ins -> ins.getCurso().getId()).distinct().toList();
            catedras = cursosIds.isEmpty()
                    ? List.of()
                    : cursoMateriaRepository.findByCursoIdIn(cursosIds);
        }

        List<SituacionMateriaDTO> salida = new ArrayList<>();
        for (CursoMateria cm : catedras) {
            Integer idCurso = cm.getCurso().getId();
            Integer idMateria = cm.getMateria().getId();
            List<Asistencia> registros = asistenciaRepository
                    .findByAlumnoIdAndCursoIdAndMateriaId(idAlumno, idCurso, idMateria);
            if (registros.isEmpty()) {
                continue;
            }
            long conAsistencia = registros.stream()
                    .filter(a -> !"AUSENTE".equals(a.getEstado().getNombre()))
                    .count();
            int porcentaje = (int) Math.round(conAsistencia * 100.0 / registros.size());

            boolean notasSinDesaprobados = calificacionRepository.findByAlumnoId(idAlumno).stream()
                    .filter(cal -> cal.getEvaluacion().getCursoMateria().getMateria()
                            .getId().equals(idMateria))
                    .allMatch(cal -> cal.getNota() >= 4);

            SituacionMateriaDTO dto = new SituacionMateriaDTO();
            dto.setIdMateria(idMateria);
            dto.setMateriaNombre(cm.getMateria().getNombre());
            dto.setClasesRegistradas(registros.size());
            dto.setClasesConAsistencia((int) conAsistencia);
            dto.setPorcentajeAsistencia(porcentaje);
            dto.setNotasSinDesaprobados(notasSinDesaprobados);
            dto.setRegular(porcentaje >= PORCENTAJE_REGULARIDAD && notasSinDesaprobados);
            salida.add(dto);
        }
        return salida;
    }

    // ------------------------------------------------------------------

    private List<Asistencia> filtrar(List<Asistencia> base,
            Integer idAlumno, Integer idCurso, LocalDate fecha) {
        return base.stream()
                .filter(a -> idAlumno == null || a.getAlumno().getId().equals(idAlumno))
                .filter(a -> idCurso == null || a.getCurso().getId().equals(idCurso))
                .filter(a -> fecha == null || a.getFecha().equals(fecha))
                .toList();
    }

    private Alumno alumnoDeSesionById(Integer idAlumno) {
        return alumnoRepository.findById(idAlumno).orElse(null);
    }

    private Integer alumnoDeSesion(CustomUserDetails userDetails) {
        if (userDetails == null || userDetails.getUsuario() == null
                || userDetails.getUsuario().getAlumno() == null) {
            return null;
        }
        return userDetails.getUsuario().getAlumno().getId();
    }

    private AsistenciaDTO toDto(Asistencia a) {
        AsistenciaDTO dto = new AsistenciaDTO();
        dto.setIdAsistencia(a.getId());
        dto.setIdAlumno(a.getAlumno().getId());
        dto.setAlumnoNombre(a.getAlumno().getNombre() + " " + a.getAlumno().getApellido());
        dto.setIdMateria(a.getMateria().getId());
        dto.setMateriaNombre(a.getMateria().getNombre());
        dto.setIdCurso(a.getCurso().getId());
        dto.setFecha(a.getFecha().toString());
        dto.setEstado(a.getEstado().getNombre());
        return dto;
    }
}
