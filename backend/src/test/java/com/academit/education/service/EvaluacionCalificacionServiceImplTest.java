package com.academit.education.service;

import com.academit.education.dto.calificacion.CalificacionesLoteDTO;
import com.academit.education.dto.calificacion.CalificacionSaveDTO;
import com.academit.education.dto.calificacion.EvaluacionCreateDTO;
import com.academit.education.dto.calificacion.EvaluacionDTO;
import com.academit.education.dto.calificacion.CalificacionDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.*;
import com.academit.education.repository.AlumnoRepository;
import com.academit.education.repository.CalificacionRepository;
import com.academit.education.repository.CursoMateriaRepository;
import com.academit.education.repository.EvaluacionRepository;
import com.academit.education.repository.InscripcionRepository;
import com.academit.education.repository.TipoEvaluacionRepository;
import com.academit.education.security.CustomUserDetails;
import com.academit.education.service.impl.AsignacionDocenteServiceImpl;
import com.academit.education.service.impl.CalificacionServiceImpl;
import com.academit.education.service.impl.EvaluacionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Tests del modulo evaluaciones y calificaciones y del componente
 * AsignacionDocenteService (RN11, RN14, RN21).
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class EvaluacionCalificacionServiceImplTest {

    @Mock
    private EvaluacionRepository evaluacionRepository;
    @Mock
    private CursoMateriaRepository cursoMateriaRepository;
    @Mock
    private TipoEvaluacionRepository tipoEvaluacionRepository;
    @Mock
    private CalificacionRepository calificacionRepository;
    @Mock
    private AlumnoRepository alumnoRepository;
    @Mock
    private InscripcionRepository inscripcionRepository;

    private EvaluacionServiceImpl evaluacionService;
    private CalificacionServiceImpl calificacionService;
    private AsignacionDocenteServiceImpl asignacionService;

    private CursoMateria catedra;
    private Evaluacion evaluacion;
    private Alumno alumno;
    private CustomUserDetails docenteSesion;
    private CustomUserDetails adminSesion;
    private CustomUserDetails alumnoSesion;

    private Usuario usuarioDeRol(String rol, Docente docente, Alumno alu) {
        Usuario u = new Usuario();
        u.setRol(new Rol());
        u.getRol().setNombre(rol);
        u.setDocente(docente);
        u.setAlumno(alu);
        return u;
    }

    @BeforeEach
    void setUp() {
        asignacionService = new AsignacionDocenteServiceImpl(cursoMateriaRepository);
        evaluacionService = new EvaluacionServiceImpl(evaluacionRepository,
                cursoMateriaRepository, tipoEvaluacionRepository, asignacionService);
        calificacionService = new CalificacionServiceImpl(calificacionRepository,
                evaluacionRepository, alumnoRepository, inscripcionRepository,
                asignacionService);

        Materia materia = new Materia();
        materia.setId(9);
        materia.setNombre("Programación II");

        Curso curso = new Curso();
        curso.setId(3);
        curso.setNombre("1° Año A");

        Docente docente = new Docente();
        docente.setId(5);

        alumno = new Alumno();
        alumno.setId(7);
        alumno.setNombre("Lucía");
        alumno.setApellido("Pérez");

        catedra = new CursoMateria();
        catedra.setId(11);
        catedra.setCurso(curso);
        catedra.setMateria(materia);
        catedra.setDocente(docente);
        catedra.setRolDocente("TITULAR");

        TipoEvaluacion tipoPARCIAL = new TipoEvaluacion();
        tipoPARCIAL.setId(1);
        tipoPARCIAL.setNombre("PARCIAL");

        evaluacion = new Evaluacion();
        evaluacion.setId(30);
        evaluacion.setCursoMateria(catedra);
        evaluacion.setFecha(LocalDate.parse("2026-09-15"));
        evaluacion.setTipo(tipoPARCIAL);
        evaluacion.setDescripcion("Primer Parcial");

        docenteSesion = new CustomUserDetails(usuarioDeRol("DOCENTE", docente, null));
        adminSesion = new CustomUserDetails(usuarioDeRol("ADMINISTRADOR", null, null));
        alumnoSesion = new CustomUserDetails(usuarioDeRol("ALUMNO", null, alumno));

        // pongo aprobado/estado en lenient stubs comunes
        lenient().when(cursoMateriaRepository.findByCursoIdAndMateriaId(3, 9))
                .thenReturn(List.of(catedra));
        lenient().when(cursoMateriaRepository.existsByCursoIdAndMateriaIdAndDocenteId(3, 9, 5))
                .thenReturn(true);
        lenient().when(tipoEvaluacionRepository.findByNombre("PARCIAL"))
                .thenReturn(Optional.of(tipoPARCIAL));
    }

    private EvaluacionCreateDTO createDTO() {
        EvaluacionCreateDTO r = new EvaluacionCreateDTO();
        r.setIdMateria(9);
        r.setIdCurso(3);
        r.setFecha("2026-09-15");
        r.setTipo("PARCIAL");
        r.setDescripcion("Primer Parcial");
        r.setPonderacion("40%");
        return r;
    }

    // ------------------------------------------------------------------
    // RN11: asignación de docente
    // ------------------------------------------------------------------

    @Test
    @DisplayName("AsignacionDocenteService: verifico docente asignado a la cátedra")
    void asignacionDocente() {
        assertTrue(asignacionService.estaAsignado(5, 3, 9));
        assertFalse(asignacionService.estaAsignado(6, 3, 9));

        when(cursoMateriaRepository.existsByCursoIdAndMateriaIdAndDocenteId(3, 9, 6))
                .thenReturn(false);
        AccessDeniedException ex = assertThrows(AccessDeniedException.class,
                () -> asignacionService.verificarAsignado(docenteSesionOtro(6), 3, 9));
        assertTrue(ex.getMessage().contains("RN11"));
    }

    private CustomUserDetails docenteSesionOtro(Integer idDocente) {
        Docente d = new Docente();
        d.setId(idDocente);
        return new CustomUserDetails(usuarioDeRol("DOCENTE", d, null));
    }

    // ------------------------------------------------------------------
    // Evaluaciones
    // ------------------------------------------------------------------

    @Test
    @DisplayName("crearEvaluacion: docente asignado → 201 con nombres desnormalizados")
    void crearEvaluacion() {
        evaluacion.setId(null);
        lenient().when(evaluacionRepository.save(any(Evaluacion.class))).thenAnswer(inv -> {
            Evaluacion ev = inv.getArgument(0);
            ev.setId(30);
            return ev;
        });

        EvaluacionDTO dto = evaluacionService.crear(docenteSesion, createDTO());

        assertEquals(30, dto.getIdEvaluacion());
        assertEquals("Programación II", dto.getMateriaNombre());
        assertEquals("1° Año A", dto.getCursoNombre());
        assertEquals("PARCIAL", dto.getTipo());
        assertEquals("Primer Parcial", dto.getDescripcion());
    }

    @Test
    @DisplayName("crearEvaluacion: docente no asignado a la cátedra → 403 (RN11)")
    void crearEvaluacionSinAsignacion() {
        when(cursoMateriaRepository.existsByCursoIdAndMateriaIdAndDocenteId(3, 9, 5))
                .thenReturn(false);
        assertThrows(AccessDeniedException.class,
                () -> evaluacionService.crear(docenteSesion, createDTO()));
        verify(evaluacionRepository, never()).save(any(Evaluacion.class));
    }

    @Test
    @DisplayName("crearEvaluacion: cátedra sin docente asignado (curso + materia) → 404")
    void crearEvaluacionCatedraInexistente() {
        when(cursoMateriaRepository.findByCursoIdAndMateriaId(3, 9)).thenReturn(List.of());
        assertThrows(ResourceNotFoundException.class,
                () -> evaluacionService.crear(docenteSesion, createDTO()));
    }

    @Test
    @DisplayName("crearEvaluacion: tipo inválido → 400")
    void crearEvaluacionTipoInvalido() {
        EvaluacionCreateDTO r = createDTO();
        r.setTipo("EXAMEN");
        assertThrows(BusinessRuleException.class, () -> evaluacionService.crear(docenteSesion, r));
    }

    @Test
    @DisplayName("crearEvaluacion: fecha con formato inválido → 400")
    void crearEvaluacionFechaInvalida() {
        EvaluacionCreateDTO r = createDTO();
        r.setFecha("15/09/2026");
        assertThrows(BusinessRuleException.class, () -> evaluacionService.crear(docenteSesion, r));
    }

    @Test
    @DisplayName("listarEvaluaciones: docente solo ve evaluaciones de sus cátedras")
    void listarEvaluaciones() {
        when(cursoMateriaRepository.findByDocenteId(5)).thenReturn(List.of(catedra));
        when(evaluacionRepository.findByCursoMateriaIdIn(List.of(11))).thenReturn(List.of(evaluacion));

        List<EvaluacionDTO> lista = evaluacionService.listar(docenteSesion, null, null);

        assertEquals(1, lista.size());
        assertEquals("Primer Parcial", lista.get(0).getDescripcion());
    }

    @Test
    @DisplayName("listarEvaluaciones: ADMINISTRADOR ve todas y filtra por tipo")
    void listarEvaluacionesAdmin() {
        when(evaluacionRepository.findAll()).thenReturn(List.of(evaluacion));

        List<EvaluacionDTO> todas = evaluacionService.listar(adminSesion, null, null);
        assertEquals(1, todas.size());

        List<EvaluacionDTO> filtradas = evaluacionService.listar(adminSesion, "TRABAJO_PRACTICO", null);
        assertTrue(filtradas.isEmpty());
    }

    // ------------------------------------------------------------------
    // Calificaciones: lote atómico
    // ------------------------------------------------------------------

    private CalificacionesLoteDTO lote(CalificacionesLoteDTO.Item... items) {
        CalificacionesLoteDTO r = new CalificacionesLoteDTO();
        r.setCalificaciones(List.of(items));
        return r;
    }

    private CalificacionesLoteDTO.Item item(Integer idAlumno, Integer nota) {
        CalificacionesLoteDTO.Item i = new CalificacionesLoteDTO.Item();
        i.setIdAlumno(idAlumno);
        i.setNota(nota);
        return i;
    }

    @Test
    @DisplayName("guardarLote: caso feliz guarda ambas notas y calcula condición")
    void guardarLote() {
        when(evaluacionRepository.findById(30)).thenReturn(Optional.of(evaluacion));
        when(cursoMateriaRepository.existsByCursoIdAndMateriaIdAndDocenteId(3, 9, 5)).thenReturn(true);
        alumno = alumno != null ? new Alumno() : alumno;
        Alumno alu = new Alumno();
        alu.setId(7);
        alu.setNombre("Lucía");
        alu.setApellido("Pérez");
        Alumno alu2 = new Alumno();
        alu2.setId(8);
        alu2.setNombre("Martín");
        alu2.setApellido("López");
        when(alumnoRepository.findAllById(List.of(7, 8))).thenReturn(List.of(alu, alu2));
        when(inscripcionRepository.existsByAlumnoIdAndCursoIdAndActivaTrue(7, 3)).thenReturn(true);
        when(inscripcionRepository.existsByAlumnoIdAndCursoIdAndActivaTrue(8, 3)).thenReturn(true);
        when(calificacionRepository.findByEvaluacionIdAndAlumnoId(30, 7)).thenReturn(null);
        when(calificacionRepository.findByEvaluacionIdAndAlumnoId(30, 8)).thenReturn(null);
        when(calificacionRepository.save(any(Calificacion.class))).thenAnswer(inv -> {
            Calificacion c = inv.getArgument(0);
            if (c.getId() == null) {
                c.setId(55);
            }
            return c;
        });

        List<CalificacionDTO> resultados = calificacionService.guardarLote(
                docenteSesion, 30, lote(item(7, 6), item(8, 2)));

        assertEquals(2, resultados.size());
        assertEquals("APROBADO", resultados.get(0).getCondicion());
        assertEquals("DESAPROBADO", resultados.get(1).getCondicion());
        assertEquals("Programación II", resultados.get(0).getMateriaNombre());
        assertEquals("Lucía Pérez", resultados.get(0).getAlumnoNombre());
    }

    @Test
    @DisplayName("guardarLote: alumno no inscripto → 400 y NO guarda nada")
    void guardarLoteNoInscritoNoGuardaNada() {
        when(evaluacionRepository.findById(30)).thenReturn(Optional.of(evaluacion));
        when(cursoMateriaRepository.existsByCursoIdAndMateriaIdAndDocenteId(3, 9, 5)).thenReturn(true);
        Alumno alu = new Alumno();
        alu.setId(7);
        alu.setNombre("Lucía");
        alu.setApellido("Pérez");
        when(alumnoRepository.findAllById(anyList())).thenAnswer(inv -> {
            List<Integer> ids = inv.getArgument(0);
            return ids.contains(7) ? List.of(alu) : List.of();
        });
        when(inscripcionRepository.existsByAlumnoIdAndCursoIdAndActivaTrue(7, 3)).thenReturn(true);
        when(inscripcionRepository.existsByAlumnoIdAndCursoIdAndActivaTrue(8, 3)).thenReturn(false);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> calificacionService.guardarLote(docenteSesion, 30,
                        lote(item(7, 6), item(8, 3))));

        assertTrue(ex.getErrors() != null && !ex.getErrors().isEmpty());
        assertTrue(ex.getMessage().contains("no se guardó ninguna nota"));
        verify(calificacionRepository, never()).save(any(Calificacion.class));
    }

    @Test
    @DisplayName("guardarLote: docente sin asignación en la cátedra de la evaluación → 403")
    void guardarLoteSinAsignacion() {
        when(evaluacionRepository.findById(30)).thenReturn(Optional.of(evaluacion));
        when(cursoMateriaRepository.existsByCursoIdAndMateriaIdAndDocenteId(3, 9, 5)).thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> calificacionService.guardarLote(docenteSesion, 30, lote(item(7, 6))));
    }

    @Test
    @DisplayName("guardarLote: evaluación inexistente → 404")
    void guardarLoteEvaluacionInexistente() {
        when(evaluacionRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(ResourceNotFoundException.class,
                () -> calificacionService.guardarLote(docenteSesion, 99, lote(item(7, 6))));
    }

    // ------------------------------------------------------------------
    // Calificaciones individual (upsert)
    // ------------------------------------------------------------------

    private CalificacionSaveDTO saveDTO() {
        CalificacionSaveDTO r = new CalificacionSaveDTO();
        r.setIdAlumno(7);
        r.setIdMateria(9);
        r.setEvaluacionNombre("Primer Parcial");
        r.setNota(9);
        return r;
    }

    @Test
    @DisplayName("guardar (individual): upsert por evaluación + alumno, condición >= 4")
    void guardarCalificacionIndividual() {
        when(evaluacionRepository.findByCursoMateriaMateriaId(9)).thenReturn(List.of(evaluacion));
        when(cursoMateriaRepository.existsByCursoIdAndMateriaIdAndDocenteId(3, 9, 5)).thenReturn(true);
        when(alumnoRepository.findById(7)).thenReturn(Optional.of(alumno));
        when(inscripcionRepository.existsByAlumnoIdAndCursoIdAndActivaTrue(7, 3)).thenReturn(true);
        when(calificacionRepository.findByEvaluacionIdAndAlumnoId(30, 7)).thenReturn(null);
        when(calificacionRepository.save(any(Calificacion.class))).thenAnswer(inv -> {
            Calificacion c = inv.getArgument(0);
            if (c.getId() == null) {
                c.setId(56);
            }
            if (c.getNota() == null) {
                c.setNota(9);
            }
            return c;
        });

        CalificacionDTO dto = calificacionService.guardar(docenteSesion, saveDTO());

        assertEquals("APROBADO", dto.getCondicion());
        assertEquals(9, dto.getNota());
        assertEquals("2026-09-15", dto.getFecha());
    }

    @Test
    @DisplayName("guardar (individual): nota 3 → DESAPROBADO (RN21)")
    void guardarCalificacionDesaprobado() {
        when(evaluacionRepository.findByCursoMateriaMateriaId(9)).thenReturn(List.of(evaluacion));
        when(cursoMateriaRepository.existsByCursoIdAndMateriaIdAndDocenteId(3, 9, 5)).thenReturn(true);
        when(alumnoRepository.findById(7)).thenReturn(Optional.of(alumno));
        when(inscripcionRepository.existsByAlumnoIdAndCursoIdAndActivaTrue(7, 3)).thenReturn(true);
        when(calificacionRepository.findByEvaluacionIdAndAlumnoId(30, 7)).thenReturn(null);
        when(calificacionRepository.save(any(Calificacion.class))).thenAnswer(inv -> {
            Calificacion c = inv.getArgument(0);
            if (c.getId() == null) {
                c.setId(57);
            }
            if (c.getNota() == null) {
                c.setNota(3);
            }
            return c;
        });

        CalificacionSaveDTO r = saveDTO();
        r.setNota(3);
        CalificacionDTO dto = calificacionService.guardar(docenteSesion, r);
        assertEquals("DESAPROBADO", dto.getCondicion());
    }

    @Test
    @DisplayName("guardar (individual): alumno no inscripto → 400")
    void guardarCalificacionNoInscrito() {
        when(evaluacionRepository.findByCursoMateriaMateriaId(9)).thenReturn(List.of(evaluacion));
        when(cursoMateriaRepository.existsByCursoIdAndMateriaIdAndDocenteId(3, 9, 5)).thenReturn(true);
        when(alumnoRepository.findById(7)).thenReturn(Optional.of(alumno));
        when(inscripcionRepository.existsByAlumnoIdAndCursoIdAndActivaTrue(7, 3)).thenReturn(false);

        assertThrows(BusinessRuleException.class,
                () -> calificacionService.guardar(docenteSesion, saveDTO()));
    }

    @Test
    @DisplayName("guardar (individual): evaluación con ese nombre no existe → 404")
    void guardarCalificacionEvaluacionInexistente() {
        when(evaluacionRepository.findByCursoMateriaMateriaId(9)).thenReturn(List.of(evaluacion));
        CalificacionSaveDTO r = saveDTO();
        r.setEvaluacionNombre("Segundo Parcial");
        assertThrows(ResourceNotFoundException.class,
                () -> calificacionService.guardar(docenteSesion, r));
    }

    // ------------------------------------------------------------------
    // Listados por rol
    // ------------------------------------------------------------------

    @Test
    @DisplayName("listarCalificaciones: ALUMNO pidiendo notas ajenas → 403")
    void listarCalificacionesAlumnoAjeno() {
        assertThrows(AccessDeniedException.class,
                () -> calificacionService.listar(alumnoSesion, 66, null, null, null));
    }

    @Test
    @DisplayName("listarCalificaciones: ALUMNO hijo ve solo las suyas")
    void listarCalificacionesAlumno() {
        Calificacion cal = new Calificacion();
        cal.setId(55);
        cal.setNota(6);
        cal.setEvaluacion(evaluacion);
        cal.setAlumno(alumno);
        when(calificacionRepository.findByAlumnoId(7)).thenReturn(List.of(cal));

        List<CalificacionDTO> lista = calificacionService.listar(alumnoSesion, null, null, null, null);

        assertEquals(1, lista.size());
        assertEquals("Lucía Pérez", lista.get(0).getAlumnoNombre());
        assertEquals(7, lista.get(0).getIdAlumno());
    }
}
