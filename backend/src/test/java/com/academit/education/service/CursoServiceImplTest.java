package com.academit.education.service;

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
import com.academit.education.service.impl.CursoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CursoServiceImplTest {

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private CarreraRepository carreraRepository;

    @Mock
    private PlanEstudioRepository planEstudioRepository;

    @Mock
    private CicloLectivoRepository cicloLectivoRepository;

    @Mock
    private InscripcionRepository inscripcionRepository;

    @InjectMocks
    private CursoServiceImpl service;

    private Carrera carrera;
    private PlanEstudio plan;
    private CicloLectivo ciclo;
    private Curso curso;

    @BeforeEach
    void setUp() {
        carrera = new Carrera();
        carrera.setId(1);
        carrera.setCodigo("TUP");
        carrera.setNombre("Tecnicatura Universitaria en Programación");

        plan = new PlanEstudio();
        plan.setId(5);
        plan.setCarrera(carrera);
        plan.setCodigo("PLAN-2024-TUP");
        plan.setNombre("Plan 2024");
        plan.setAnioAprobacion(2024);
        plan.setEstado("VIGENTE");

        ciclo = new CicloLectivo();
        ciclo.setId(2);
        ciclo.setAnio(2026);
        ciclo.setNombre("Ciclo Lectivo 2026");
        ciclo.setEstado("ACTIVO");

        curso = new Curso();
        curso.setId(3);
        curso.setCodigo("1A-PRO-TM");
        curso.setNombre("1° Año A");
        curso.setTurno("MANANA");
        curso.setCapacidadMaxima(30);
        curso.setAula("Aula 101");
        curso.setCiclo(ciclo);
        curso.setCarrera(carrera);
        curso.setPlan(plan);
    }

    private CursoCreateDTO createDTO() {
        CursoCreateDTO r = new CursoCreateDTO();
        r.setCodigo("2A-PRO-TN");
        r.setNombre("2° Año B");
        r.setIdCarrera(1);
        r.setIdPlan(5);
        r.setTurno("NOCHE");
        r.setCapacidadMaxima(25);
        r.setAula("Aula 204");
        r.setCicloLectivo(2026);
        return r;
    }

    @Test
    @DisplayName("crearCurso: caso feliz con cupos calculados")
    void crearCurso() {
        when(cicloLectivoRepository.findByAnio(2026)).thenReturn(Optional.of(ciclo));
        when(carreraRepository.findById(1)).thenReturn(Optional.of(carrera));
        when(planEstudioRepository.findById(5)).thenReturn(Optional.of(plan));
        when(cursoRepository.save(any(Curso.class))).thenAnswer(inv -> {
            Curso c = inv.getArgument(0);
            c.setId(6);
            return c;
        });
        when(inscripcionRepository.findByCursoIdIn(anyList())).thenReturn(List.of());

        CursoDTO dto = service.crearCurso(createDTO());

        assertEquals(25, dto.getCapacidadMaxima());
        assertEquals(0, dto.getInscriptos());
        assertEquals(25, dto.getCuposDisponibles());
        assertEquals(2026, dto.getCicloLectivo());
        assertEquals("Plan 2024", dto.getPlanNombre());
    }

    @Test
    @DisplayName("crearCurso: 404 si no existe el ciclo lectivo del anio")
    void crearCursoCicloInexistente() {
        when(cicloLectivoRepository.findByAnio(2031)).thenReturn(Optional.empty());

        CursoCreateDTO r = createDTO();
        r.setCicloLectivo(2031);

        assertThrows(ResourceNotFoundException.class, () -> service.crearCurso(r));
    }

    @Test
    @DisplayName("crearCurso: rechaza plan de otra carrera (RN22)")
    void crearCursoPlanDeOtraCarrera() {
        when(cicloLectivoRepository.findByAnio(2026)).thenReturn(Optional.of(ciclo));
        Carrera otra = new Carrera();
        otra.setId(7);
        when(carreraRepository.findById(7)).thenReturn(Optional.of(otra));
        when(planEstudioRepository.findById(5)).thenReturn(Optional.of(plan));

        CursoCreateDTO r = createDTO();
        r.setIdPlan(5);
        r.setIdCarrera(7);

        assertThrows(BusinessRuleException.class, () -> service.crearCurso(r));
    }

    @Test
    @DisplayName("crearCurso: rechaza nombre repetido en el mismo ciclo y carrera (RN23)")
    void crearCursoNombreRepetido() {
        when(cicloLectivoRepository.findByAnio(2026)).thenReturn(Optional.of(ciclo));
        when(carreraRepository.findById(1)).thenReturn(Optional.of(carrera));
        when(planEstudioRepository.findById(5)).thenReturn(Optional.of(plan));
        when(cursoRepository.existsByNombreAndCicloIdAndCarreraId("1° Año A", 2, 1))
                .thenReturn(true);

        CursoCreateDTO r = createDTO();
        r.setNombre("1° Año A");

        assertThrows(BusinessRuleException.class, () -> service.crearCurso(r));
        verify(cursoRepository, never()).save(any(Curso.class));
    }

    @Test
    @DisplayName("crearCurso: rechaza turno fuera del catalogo")
    void crearCursoTurnoInvalido() {
        CursoCreateDTO r = createDTO();
        r.setTurno("VESPERTINO");

        assertThrows(BusinessRuleException.class, () -> service.crearCurso(r));
    }

    // ------------------------------------------------------------------
    // PUT
    // ------------------------------------------------------------------

    @Test
    @DisplayName("actualizarCurso: rechaza capacidad menor que los activos inscriptos")
    void actualizarCursoCapacidadMenorQueInscriptos() {
        when(cursoRepository.findById(3)).thenReturn(Optional.of(curso));

        Inscripcion i = new Inscripcion();
        i.setActiva(true);
        i.setCurso(curso);
        when(inscripcionRepository.findByCursoIdIn(List.of(3))).thenReturn(List.of(i));

        CursoUpdateDTO r = new CursoUpdateDTO();
        r.setCapacidadMaxima(0);

        assertThrows(BusinessRuleException.class, () -> service.actualizarCurso(3, r));
        verify(cursoRepository, never()).save(any(Curso.class));
    }

    @Test
    @DisplayName("actualizarCurso: actualiza turno y capacidad caso feliz")
    void actualizarCurso() {
        when(cursoRepository.findById(3)).thenReturn(Optional.of(curso));
        when(inscripcionRepository.findByCursoIdIn(List.of(3))).thenReturn(List.of());
        when(cursoRepository.save(any(Curso.class))).thenAnswer(inv -> inv.getArgument(0));

        CursoUpdateDTO r = new CursoUpdateDTO();
        r.setTurno("TARDE");
        r.setCapacidadMaxima(40);

        CursoDTO dto = service.actualizarCurso(3, r);

        assertEquals("TARDE", dto.getTurno());
        assertEquals(40, dto.getCapacidadMaxima());
    }

    @Test
    @DisplayName("actualizarCurso: 404 si el curso no existe")
    void actualizarCursoInexistente() {
        when(cursoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.actualizarCurso(99, new CursoUpdateDTO()));
    }

    // ------------------------------------------------------------------
    // GET
    // ------------------------------------------------------------------

    @Test
    @DisplayName("listarCursos: devuelve inscriptos y cupos calculados por curso")
    void listarCursos() {
        when(cursoRepository.findAll()).thenReturn(List.of(curso));

        Inscripcion i = new Inscripcion();
        i.setActiva(true);
        i.setCurso(curso);
        when(inscripcionRepository.findByCursoIdIn(List.of(3))).thenReturn(List.of(i));

        List<CursoDTO> cursos = service.listarCursos();

        assertEquals(1, cursos.size());
        assertEquals(1, cursos.get(0).getInscriptos());
        assertEquals(29, cursos.get(0).getCuposDisponibles());
    }
}
