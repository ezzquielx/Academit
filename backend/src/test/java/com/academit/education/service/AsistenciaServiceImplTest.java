package com.academit.education.service;

import com.academit.education.dto.asistencia.AsistenciasLoteDTO;
import com.academit.education.dto.asistencia.SituacionMateriaDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.model.Alumno;
import com.academit.education.model.Asistencia;
import com.academit.education.model.Calificacion;
import com.academit.education.model.Curso;
import com.academit.education.model.CursoMateria;
import com.academit.education.model.Docente;
import com.academit.education.model.EstadoAsistencia;
import com.academit.education.model.Evaluacion;
import com.academit.education.model.Inscripcion;
import com.academit.education.model.Materia;
import com.academit.education.model.Usuario;
import com.academit.education.repository.AlumnoRepository;
import com.academit.education.repository.AsistenciaRepository;
import com.academit.education.repository.CalificacionRepository;
import com.academit.education.repository.CursoMateriaRepository;
import com.academit.education.repository.CursoRepository;
import com.academit.education.repository.EstadoAsistenciaRepository;
import com.academit.education.repository.InscripcionRepository;
import com.academit.education.repository.MateriaRepository;
import com.academit.education.security.CustomUserDetails;
import com.academit.education.service.impl.AsignacionDocenteServiceImpl;
import com.academit.education.service.impl.AsistenciaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class AsistenciaServiceImplTest {

    @Mock
    private AsistenciaRepository asistenciaRepository;
    @Mock
    private AlumnoRepository alumnoRepository;
    @Mock
    private InscripcionRepository inscripcionRepository;
    @Mock
    private CursoMateriaRepository cursoMateriaRepository;
    @Mock
    private CursoRepository cursoRepository;
    @Mock
    private MateriaRepository materiaRepository;
    @Mock
    private EstadoAsistenciaRepository estadoAsistenciaRepository;
    @Mock
    private CalificacionRepository calificacionRepository;

    private AsistenciaServiceImpl service;
    private AsignacionDocenteServiceImpl asignacionService;

    private Materia materia;
    private Curso curso;
    private CursoMateria catedra;
    private Alumno alumno;
    private CustomUserDetails docenteSesion;
    private CustomUserDetails adminSesion;
    private CustomUserDetails alumnoSesion;

    private EstadoAsistencia estado(String nombre) {
        EstadoAsistencia e = new EstadoAsistencia();
        e.setId(1);
        e.setNombre(nombre);
        return e;
    }

    private Usuario usuarioDeRol(String rol, Docente docente, Alumno alu) {
        Usuario u = new Usuario();
        u.setRol(new com.academit.education.model.Rol());
        u.getRol().setNombre(rol);
        u.setDocente(docente);
        u.setAlumno(alu);
        return u;
    }

    @BeforeEach
    void setUp() {
        asignacionService = new AsignacionDocenteServiceImpl(cursoMateriaRepository);
        service = new AsistenciaServiceImpl(asistenciaRepository, alumnoRepository,
                inscripcionRepository, cursoMateriaRepository, cursoRepository,
                materiaRepository, estadoAsistenciaRepository, calificacionRepository,
                asignacionService);

        materia = new Materia();
        materia.setId(9);
        materia.setNombre("Programación II");

        curso = new Curso();
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

        docenteSesion = new CustomUserDetails(usuarioDeRol("DOCENTE", docente, null));
        adminSesion = new CustomUserDetails(usuarioDeRol("ADMINISTRADOR", null, null));
        alumnoSesion = new CustomUserDetails(usuarioDeRol("ALUMNO", null, alumno));

        when(cursoMateriaRepository.existsByCursoIdAndMateriaIdAndDocenteId(3, 9, 5))
                .thenReturn(true);
        when(cursoMateriaRepository.existsByCursoIdAndMateriaId(3, 9)).thenReturn(true);
        when(cursoMateriaRepository.save(any(CursoMateria.class))).thenAnswer(inv -> inv.getArgument(0));
    }

    private AsistenciasLoteDTO lote(AsistenciasLoteDTO.Item... items) {
        AsistenciasLoteDTO r = new AsistenciasLoteDTO();
        r.setAsistencias(List.of(items));
        return r;
    }

    private AsistenciasLoteDTO.Item item(String estado, String fecha) {
        AsistenciasLoteDTO.Item i = new AsistenciasLoteDTO.Item();
        i.setIdAlumno(7);
        i.setIdMateria(9);
        i.setIdCurso(3);
        i.setFecha(fecha);
        i.setEstado(estado);
        return i;
    }

    private void stubFelicidad() {
        when(alumnoRepository.findAllById(anyList()))
                .thenAnswer(inv -> {
                    List<Integer> ids = inv.getArgument(0);
                    return ids.contains(7) ? List.of(alumno) : List.of();
                });
        when(inscripcionRepository.existsByAlumnoIdAndCursoIdAndActivaTrue(7, 3)).thenReturn(true);
        when(estadoAsistenciaRepository.findByNombre("PRESENTE")).thenReturn(Optional.of(estado("PRESENTE")));
        when(estadoAsistenciaRepository.findByNombre("AUSENTE")).thenReturn(Optional.of(estado("AUSENTE")));
        when(estadoAsistenciaRepository.findByNombre("TARDANZA")).thenReturn(Optional.of(estado("TARDANZA")));
        when(estadoAsistenciaRepository.findByNombre("JUSTIFICADO")).thenReturn(Optional.of(estado("JUSTIFICADO")));
        when(cursoRepository.findById(3)).thenReturn(Optional.of(curso));
        when(materiaRepository.findById(9)).thenReturn(Optional.of(materia));
        when(asistenciaRepository.findByAlumnoIdAndCursoIdAndMateriaIdAndFecha(7, 3, 9,
                LocalDate.parse("2026-10-20"))).thenReturn(Optional.empty());
    }

    // ------------------------------------------------------------------
    // Lote
    // ------------------------------------------------------------------

    @Test
    @DisplayName("registrarLote: caso feliz guarda la asistencia")
    void registrarLote() {
        stubFelicidad();
        when(asistenciaRepository.save(any(Asistencia.class))).thenAnswer(inv -> {
            Asistencia a = inv.getArgument(0);
            if (a.getId() == null) {
                a.setId(80);
            }
            return a;
        });

        service.registrarLote(docenteSesion, lote(item("PRESENTE", "2026-10-20")));

        verify(asistenciaRepository).save(any(Asistencia.class));
    }

    @Test
    @DisplayName("registrarLote: fecha futura → 400 y no guarda")
    void registrarLoteFechaFutura() {
        stubFelicidad();

        AsistenciasLoteDTO r = lote(item("PRESENTE", "2099-01-01"));
        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.registrarLote(docenteSesion, r));

        assertTrue(ex.getMessage().contains("no se registró ninguna"));
        verify(asistenciaRepository, never()).save(any(Asistencia.class));
    }

    @Test
    @DisplayName("registrarLote: estado inválido (TARDE) → 400")
    void registrarLoteEstadoInvalido() {
        stubFelicidad();
        assertThrows(BusinessRuleException.class,
                () -> service.registrarLote(docenteSesion, lote(item("TARDE", "2026-10-20"))));
    }

    @Test
    @DisplayName("registrarLote: alumno no inscripto en el curso → 400 atómico")
    void registrarLoteNoInscrito() {
        stubFelicidad();
        when(inscripcionRepository.existsByAlumnoIdAndCursoIdAndActivaTrue(7, 3)).thenReturn(false);

        assertThrows(BusinessRuleException.class,
                () -> service.registrarLote(docenteSesion, lote(item("PRESENTE", "2026-10-20"))));
        verify(asistenciaRepository, never()).save(any(Asistencia.class));
    }

    @Test
    @DisplayName("registrarLote misma clave alumno/curso/materia/fecha → upsert")
    void registrarLoteUpsert() {
        stubFelicidad();
        Asistencia previa = new Asistencia();
        previa.setId(80);
        previa.setAlumno(alumno);
        previa.setCurso(curso);
        previa.setMateria(materia);
        previa.setFecha(LocalDate.parse("2026-10-20"));
        previa.setEstado(estado("AUSENTE"));
        when(asistenciaRepository.findByAlumnoIdAndCursoIdAndMateriaIdAndFecha(7, 3, 9,
                LocalDate.parse("2026-10-20"))).thenReturn(Optional.of(previa));
        when(asistenciaRepository.save(any(Asistencia.class))).thenAnswer(inv -> inv.getArgument(0));

        service.registrarLote(docenteSesion, lote(item("PRESENTE", "2026-10-20")));

        assertEquals("PRESENTE", previa.getEstado().getNombre());
        verify(asistenciaRepository).save(previa);
    }

    @Test
    @DisplayName("registrarLote: docente sin asignación a la cátedra → 403 (RN12)")
    void registrarLoteDocenteNoAsignado() {
        stubFelicidad();
        when(cursoMateriaRepository.existsByCursoIdAndMateriaIdAndDocenteId(3, 9, 5))
                .thenReturn(false);

        assertThrows(AccessDeniedException.class,
                () -> service.registrarLote(docenteSesion, lote(item("PRESENTE", "2026-10-20"))));
        verify(asistenciaRepository, never()).save(any(Asistencia.class));
    }

    // ------------------------------------------------------------------
    // Listados
    // ------------------------------------------------------------------

    @Test
    @DisplayName("listar: alumno pidiendo asistencias ajenas → 403")
    void listarAlumnoAjeno() {
        assertThrows(AccessDeniedException.class,
                () -> service.listar(alumnoSesion, 77, null, null));
    }

    @Test
    @DisplayName("listar: admin con filtros devuelve DTO completo")
    void listarAdmin() {
        Asistencia a = new Asistencia();
        a.setId(80);
        a.setAlumno(alumno);
        a.setCurso(curso);
        a.setMateria(materia);
        a.setFecha(LocalDate.parse("2026-10-20"));
        a.setEstado(estado("PRESENTE"));
        when(asistenciaRepository.findAll()).thenReturn(List.of(a));

        List<com.academit.education.dto.asistencia.AsistenciaDTO> lista =
                service.listar(adminSesion, 7, 3, "2026-10-20");

        assertEquals(1, lista.size());
        assertEquals("Lucía Pérez", lista.get(0).getAlumnoNombre());
        assertEquals(9, lista.get(0).getIdMateria());
        assertEquals("Programación II", lista.get(0).getMateriaNombre());
    }

    @Test
    @DisplayName("listar: docente solo ve asistencias de sus cátedras")
    void listarDocente() {
        when(cursoMateriaRepository.findByDocenteId(5)).thenReturn(List.of(catedra));
        when(asistenciaRepository.findAll()).thenReturn(List.of());

        List<com.academit.education.dto.asistencia.AsistenciaDTO> lista =
                service.listar(docenteSesion, null, null, null);
        assertTrue(lista.isEmpty());
    }

    // ------------------------------------------------------------------
    // Situación académica (reutilizable)
    // ------------------------------------------------------------------

    private void stubSituacion() {
        when(alumnoRepository.findById(7)).thenReturn(Optional.of(alumno));
        Inscripcion insc = new Inscripcion();
        insc.setId(1);
        insc.setCurso(curso);
        insc.setAlumno(alumno);
        insc.setActiva(true);
        when(inscripcionRepository.findByAlumnoIdAndActivaTrue(7))
                .thenReturn(List.of(insc));
        when(cursoMateriaRepository.findByCursoIdIn(anyList()))
                .thenReturn(List.of(catedra));
        List<Asistencia> registros;
        Asistencia presente = new Asistencia();
        presente.setId(1);
        presente.setEstado(estado("PRESENTE"));
        Asistencia presente2 = new Asistencia();
        presente2.setId(2);
        presente2.setEstado(estado("PRESENTE"));
        Asistencia presente3 = new Asistencia();
        presente3.setId(3);
        presente3.setEstado(estado("PRESENTE"));
        Asistencia ausente = new Asistencia();
        ausente.setId(4);
        ausente.setEstado(estado("AUSENTE"));
        registros = List.of(presente, presente2, presente3, ausente);
        when(asistenciaRepository.findByAlumnoIdAndCursoIdAndMateriaId(7, 3, 9))
                .thenReturn(registros);
        when(calificacionRepository.findByAlumnoId(7)).thenReturn(List.of());
    }

    @Test
    @DisplayName("situacionAcademica: 3 presentes de 4 clases → 75% → REGULAR")
    void situacion75Regular() {
        stubSituacion();
        List<SituacionMateriaDTO> lista = service.situacionAcademica(alumnoSesion, 7);

        assertEquals(1, lista.size());
        assertEquals(75, lista.get(0).getPorcentajeAsistencia());
        assertTrue(lista.get(0).getRegular());
    }

    @Test
    @DisplayName("situacionAcademica: 2 presentes de 4 clases → 50% → NO regular")
    void situacion50NoRegular() {
        stubSituacion();
        List<Asistencia> registros;
        Asistencia presente = new Asistencia();
        presente.setId(1);
        presente.setEstado(estado("PRESENTE"));
        Asistencia presente2 = new Asistencia();
        presente2.setId(2);
        presente2.setEstado(estado("PRESENTE"));
        Asistencia ausente = new Asistencia();
        ausente.setId(3);
        ausente.setEstado(estado("AUSENTE"));
        Asistencia ausente2 = new Asistencia();
        ausente2.setId(4);
        ausente2.setEstado(estado("AUSENTE"));
        when(asistenciaRepository.findByAlumnoIdAndCursoIdAndMateriaId(7, 3, 9))
                .thenReturn(List.of(presente, presente2, ausente, ausente2));

        List<SituacionMateriaDTO> lista = service.situacionAcademica(alumnoSesion, 7);

        assertEquals(50, lista.get(0).getPorcentajeAsistencia());
        assertFalse(lista.get(0).getRegular());
    }

    @Test
    @DisplayName("situacionAcademica: nota desaprobada → NO regular aunque asista")
    void situacionNotasDesaprobadas() {
        stubSituacion();
        Calificacion cal = new Calificacion();
        cal.setId(1);
        cal.setNota(2);
        Evaluacion ev = new Evaluacion();
        ev.setId(30);
        ev.setCursoMateria(catedra);
        cal.setEvaluacion(ev);
        when(calificacionRepository.findByAlumnoId(7)).thenReturn(List.of(cal));

        List<SituacionMateriaDTO> lista = service.situacionAcademica(alumnoSesion, 7);

        assertFalse(lista.get(0).getNotasSinDesaprobados());
        assertFalse(lista.get(0).getRegular());
    }

    @Test
    @DisplayName("situacionAcademica: alumno inexistente → 404")
    void situacionAlumnoInexistente() {
        when(alumnoRepository.findById(99)).thenReturn(Optional.empty());
        assertThrows(com.academit.education.exception.ResourceNotFoundException.class,
                () -> service.situacionAcademica(adminSesion, 99));
    }
}
