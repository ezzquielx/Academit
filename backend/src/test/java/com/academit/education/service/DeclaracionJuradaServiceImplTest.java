package com.academit.education.service;

import com.academit.education.dto.common.FieldErrorDTO;
import com.academit.education.dto.declaracion.DeclaracionJuradaCreateDTO;
import com.academit.education.dto.declaracion.DeclaracionJuradaDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ConflictException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Alumno;
import com.academit.education.model.Calificacion;
import com.academit.education.model.Carrera;
import com.academit.education.model.CicloLectivo;
import com.academit.education.model.Correlativa;
import com.academit.education.model.CorrelativaId;
import com.academit.education.model.CursoMateria;
import com.academit.education.model.DeclaracionJurada;
import com.academit.education.model.DeclaracionMateria;
import com.academit.education.model.Evaluacion;
import com.academit.education.model.Materia;
import com.academit.education.model.PlanEstudio;
import com.academit.education.model.Rol;
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
import com.academit.education.service.impl.DeclaracionJuradaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.mockito.stubbing.Answer;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class DeclaracionJuradaServiceImplTest {

    @Mock
    private DeclaracionJuradaRepository declaracionJuradaRepository;
    @Mock
    private DeclaracionMateriaRepository declaracionMateriaRepository;
    @Mock
    private AlumnoRepository alumnoRepository;
    @Mock
    private MateriaRepository materiaRepository;
    @Mock
    private CicloLectivoRepository cicloLectivoRepository;
    @Mock
    private PlanMateriaRepository planMateriaRepository;
    @Mock
    private CorrelativaRepository correlativaRepository;
    @Mock
    private CalificacionRepository calificacionRepository;

    private DeclaracionJuradaServiceImpl service;

    private Alumno alumno;
    private PlanEstudio plan;
    private Materia materia1;
    private Materia materia2;
    private CicloLectivo ciclo;
    private CustomUserDetails alumnoSesion;
    private CustomUserDetails adminSesion;

    private Usuario usuarioDeRol(String rol, Alumno alu) {
        Rol r = new Rol();
        r.setNombre(rol);
        Usuario u = new Usuario();
        u.setRol(r);
        u.setAlumno(alu);
        return u;
    }

    private Calificacion nota(Integer id, Integer valor, Integer idMateria) {
        Materia materia = new Materia();
        materia.setId(idMateria);
        CursoMateria cm = new CursoMateria();
        cm.setMateria(materia);
        Evaluacion ev = new Evaluacion();
        ev.setCursoMateria(cm);
        Calificacion c = new Calificacion();
        c.setId(id);
        c.setEvaluacion(ev);
        c.setNota(valor);
        return c;
    }

    private Correlativa correlativa(Integer idMateria, Integer idRequerida) {
        CorrelativaId cid = new CorrelativaId();
        cid.setIdPlan(plan.getId());
        cid.setIdMateria(idMateria);
        cid.setIdMateriaRequerida(idRequerida);
        Correlativa c = new Correlativa();
        c.setId(cid);
        return c;
    }

    private DeclaracionJuradaCreateDTO createDTO() {
        DeclaracionJuradaCreateDTO r = new DeclaracionJuradaCreateDTO();
        r.setIdAlumno(7);
        r.setIdCiclo(3);
        r.setMateriasDeclaradasIds(List.of(1));
        r.setObservaciones("Comprometo la cursada de materias del plan");
        return r;
    }

    private DeclaracionJurada declaracion(Integer id, List<DeclaracionMateria> materias) {
        DeclaracionJurada d = new DeclaracionJurada();
        d.setId(id);
        d.setAlumno(alumno);
        d.setCiclo(ciclo);
        d.setFecha(LocalDate.now());
        return d;
    }

    @BeforeEach
    void setUp() {
        service = new DeclaracionJuradaServiceImpl(declaracionJuradaRepository,
                declaracionMateriaRepository, alumnoRepository, materiaRepository,
                cicloLectivoRepository, planMateriaRepository, correlativaRepository,
                calificacionRepository);

        Carrera carrera = new Carrera();
        carrera.setId(1);
        carrera.setNombre("Tecnicatura Universitaria en Programación");

        plan = new PlanEstudio();
        plan.setId(5);
        plan.setCarrera(carrera);

        alumno = new Alumno();
        alumno.setId(7);
        alumno.setNombre("Lucía");
        alumno.setApellido("Pérez");
        alumno.setPlan(plan);

        materia1 = new Materia();
        materia1.setId(1);
        materia1.setNombre("Programación I");
        materia1.setActiva(true);

        materia2 = new Materia();
        materia2.setId(2);
        materia2.setNombre("Programación II");
        materia2.setActiva(true);

        ciclo = new CicloLectivo();
        ciclo.setId(3);
        ciclo.setAnio(2026);
        ciclo.setNombre("Ciclo Lectivo 2026");

        alumnoSesion = new CustomUserDetails(usuarioDeRol("ALUMNO", alumno));
        adminSesion = new CustomUserDetails(usuarioDeRol("ADMINISTRADOR", null));

        when(alumnoRepository.findById(7)).thenReturn(Optional.of(alumno));
        when(cicloLectivoRepository.findById(3)).thenReturn(Optional.of(ciclo));
        when(materiaRepository.findById(1)).thenReturn(Optional.of(materia1));
        when(materiaRepository.findById(2)).thenReturn(Optional.of(materia2));
        when(planMateriaRepository.existsByIdIdPlanAndIdIdMateria(5, 1)).thenReturn(true);
        when(planMateriaRepository.existsByIdIdPlanAndIdIdMateria(5, 2)).thenReturn(true);
        when(correlativaRepository.findByIdIdPlanAndIdIdMateria(5, 1)).thenReturn(List.of());
        when(correlativaRepository.findByIdIdPlanAndIdIdMateria(5, 2))
                .thenReturn(List.of(correlativa(2, 1)));
        when(calificacionRepository.findByAlumnoId(7)).thenReturn(List.of());
        when(declaracionJuradaRepository.existsByAlumnoIdAndCicloId(7, 3)).thenReturn(false);
        when(declaracionJuradaRepository.save(any(DeclaracionJurada.class)))
                .thenAnswer((Answer<DeclaracionJurada>) inv -> {
                    DeclaracionJurada d = inv.getArgument(0);
                    d.setId(30);
                    return d;
                });
        when(declaracionMateriaRepository.save(any(DeclaracionMateria.class)))
                .thenAnswer(inv -> inv.getArgument(0));
        when(declaracionMateriaRepository.findByDeclaracionId(30))
                .thenAnswer(inv -> {
                    DeclaracionMateria dm = new DeclaracionMateria();
                    dm.setMateria(materia1);
                    return List.of(dm);
                });
    }

    // ------------------------------------------------------------------
    // GET
    // ------------------------------------------------------------------

    @Test
    @DisplayName("listar: ALUMNO ve solo sus declaraciones")
    void listarAlumnoSoloSuyas() {
        DeclaracionJurada d = declaracion(30, List.of());
        when(declaracionJuradaRepository.findByAlumnoId(7)).thenReturn(List.of(d));

        List<DeclaracionJuradaDTO> lista = service.listar(alumnoSesion, null, null);

        assertEquals(1, lista.size());
        assertEquals(30, lista.get(0).getIdDeclaracion());
        assertEquals("Pérez, Lucía", lista.get(0).getAlumnoNombre());
        assertEquals("Ciclo Lectivo 2026", lista.get(0).getCicloNombre());
        assertEquals(List.of(1), lista.get(0).getMateriasDeclaradasIds());
        assertEquals(List.of("Programación I"), lista.get(0).getMateriasNombres());
    }

    @Test
    @DisplayName("listar: ADMINISTRADOR filtra por alumno")
    void listarAdminFiltroAlumno() {
        when(declaracionJuradaRepository.findByAlumnoId(7))
                .thenReturn(List.of(declaracion(30, List.of())));

        List<DeclaracionJuradaDTO> lista = service.listar(adminSesion, 7, null);
        assertEquals(1, lista.size());
    }

    @Test
    @DisplayName("listar: ADMINISTRATIVO filtra por alumno y ciclo")
    void listarAdminFiltroAlumnoCiclo() {
        CustomUserDetails administrativo = new CustomUserDetails(
                usuarioDeRol("ADMINISTRATIVO", null));
        when(declaracionJuradaRepository.findByAlumnoIdAndCicloId(7, 3))
                .thenReturn(List.of(declaracion(30, List.of())));

        assertEquals(1, service.listar(administrativo, 7, 3).size());
    }

    // ------------------------------------------------------------------
    // POST (RF51)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("presentar: declaración válida 201 con materias guardadas")
    void presentar() {
        DeclaracionJuradaDTO dto = service.presentar(alumnoSesion, createDTO());

        assertEquals(30, dto.getIdDeclaracion());
        assertEquals("PENDIENTE", dto.getEstado());
        verify(declaracionJuradaRepository).save(any(DeclaracionJurada.class));
        verify(declaracionMateriaRepository).save(any(DeclaracionMateria.class));
    }

    @Test
    @DisplayName("presentar: segunda declaración del mismo ciclo → 409 (RN20)")
    void presentarDuplicada() {
        when(declaracionJuradaRepository.existsByAlumnoIdAndCicloId(7, 3)).thenReturn(true);
        assertThrows(ConflictException.class,
                () -> service.presentar(alumnoSesion, createDTO()));
    }

    @Test
    @DisplayName("presentar: alumno presentando por otro → 403")
    void presentarPorOtro() {
        Alumno otro = new Alumno();
        otro.setId(8);
        otro.setNombre("Otros");
        otro.setApellido("Alumno");
        otro.setPlan(plan);
        when(alumnoRepository.findById(8)).thenReturn(Optional.of(otro));

        CustomUserDetails otroSesion = new CustomUserDetails(usuarioDeRol("ALUMNO", otro));
        assertThrows(AccessDeniedException.class,
                () -> service.presentar(otroSesion, createDTO()));
    }

    @Test
    @DisplayName("presentar: materia fuera del plan → 400 con posición indicada")
    void presentarMateriaFueraDelPlan() {
        when(planMateriaRepository.existsByIdIdPlanAndIdIdMateria(5, 1)).thenReturn(false);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.presentar(alumnoSesion, createDTO()));

        List<FieldErrorDTO> errors = ex.getErrors();
        assertEquals(1, errors.size());
        assertEquals("materiasDeclaradasIds[0]", errors.get(0).getCampo());
        assertTrue(errors.get(0).getMensaje().contains("Programación I"));
        verify(declaracionJuradaRepository, never()).save(any(DeclaracionJurada.class));
    }

    @Test
    @DisplayName("presentar: materia inactiva → 400")
    void presentarMateriaInactiva() {
        Materia inactiva = new Materia();
        inactiva.setId(1);
        inactiva.setNombre("Programación I");
        inactiva.setActiva(false);
        when(materiaRepository.findById(1)).thenReturn(Optional.of(inactiva));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.presentar(alumnoSesion, createDTO()));
        assertTrue(ex.getErrors().get(0).getMensaje().contains("no está activa"));
    }

    @Test
    @DisplayName("presentar: materia inexistente → 400 con posición")
    void presentarMateriaInexistente() {
        when(materiaRepository.findById(1)).thenReturn(Optional.empty());
        assertThrows(BusinessRuleException.class,
                () -> service.presentar(alumnoSesion, createDTO()));
    }

    @Test
    @DisplayName("presentar: correlativa no aprobada → 400 con la materia indicada")
    void presentarCorrelativaNoAprobada() {
        // Declara Programación II (id 2), que requiere Programación I (id 1)
        DeclaracionJuradaCreateDTO r = createDTO();
        r.setMateriasDeclaradasIds(List.of(2));

        BusinessRuleException ex = assertThrows(BusinessRuleException.class,
                () -> service.presentar(alumnoSesion, r));

        List<FieldErrorDTO> errors = ex.getErrors();
        assertEquals(1, errors.size());
        assertTrue(errors.get(0).getMensaje().contains("Correlativa no aprobada"));
        assertTrue(errors.get(0).getMensaje().contains("Programación I"));
        assertTrue(errors.get(0).getMensaje().contains("Programación II"));
        verify(declaracionJuradaRepository, never()).save(any(DeclaracionJurada.class));
    }

    @Test
    @DisplayName("presentar: correlativa aprobada con todas las notas >= 4")
    void presentarCorrelativaAprobada() {
        when(calificacionRepository.findByAlumnoId(7))
                .thenReturn(List.of(nota(90, 4, 1)));

        DeclaracionJuradaCreateDTO r = createDTO();
        r.setMateriasDeclaradasIds(List.of(2));

        DeclaracionJuradaDTO dto = service.presentar(alumnoSesion, r);
        assertEquals(30, dto.getIdDeclaracion());
    }

    @Test
    @DisplayName("presentar: correlativa con nota < 4 no está aprobada")
    void presentarCorrelativaConNotaInsuficiente() {
        when(calificacionRepository.findByAlumnoId(7))
                .thenReturn(List.of(nota(90, 3, 1)));

        DeclaracionJuradaCreateDTO r = createDTO();
        r.setMateriasDeclaradasIds(List.of(2));

        assertThrows(BusinessRuleException.class, () -> service.presentar(alumnoSesion, r));
    }

    @Test
    @DisplayName("presentar: correlativa sin notas cargadas no está aprobada")
    void presentarCorrelativaSinNotas() {
        when(calificacionRepository.findByAlumnoId(7))
                .thenReturn(List.of(nota(90, 1, 9)));

        DeclaracionJuradaCreateDTO r = createDTO();
        r.setMateriasDeclaradasIds(List.of(2));

        assertThrows(BusinessRuleException.class, () -> service.presentar(alumnoSesion, r));
    }

    @Test
    @DisplayName("presentar: ciclo inexistente → 404")
    void presentarCicloInexistente() {
        when(cicloLectivoRepository.findById(99)).thenReturn(Optional.empty());
        DeclaracionJuradaCreateDTO r = createDTO();
        r.setIdCiclo(99);
        assertThrows(ResourceNotFoundException.class, () -> service.presentar(alumnoSesion, r));
    }

    @Test
    @DisplayName("presentar: alumno inexistente → 404")
    void presentarAlumnoInexistente() {
        when(alumnoRepository.findById(99)).thenReturn(Optional.empty());
        DeclaracionJuradaCreateDTO r = createDTO();
        r.setIdAlumno(99);
        assertThrows(ResourceNotFoundException.class,
                () -> service.presentar(adminSesion, r));
    }

    @Test
    @DisplayName("presentar: null en materiasDeclaradasIds → 400 por Bean Validation, no NPE")
    void presentarMateriasNull() {
        DeclaracionJuradaCreateDTO r = createDTO();
        r.setMateriasDeclaradasIds(null);
        assertThrows(BusinessRuleException.class, () -> service.presentar(alumnoSesion, r));
    }
}
