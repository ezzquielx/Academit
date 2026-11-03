package com.academit.education.service;

import com.academit.education.dto.carrera.CarreraCreateDTO;
import com.academit.education.dto.carrera.CarreraDTO;
import com.academit.education.dto.curso.CursoDTO;
import com.academit.education.dto.planestudio.PlanEstudioDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Carrera;
import com.academit.education.model.CicloLectivo;
import com.academit.education.model.Curso;
import com.academit.education.model.Inscripcion;
import com.academit.education.model.PlanEstudio;
import com.academit.education.repository.AlumnoRepository;
import com.academit.education.repository.CarreraRepository;
import com.academit.education.repository.CursoRepository;
import com.academit.education.repository.InscripcionRepository;
import com.academit.education.repository.PlanEstudioRepository;
import com.academit.education.repository.PlanMateriaRepository;
import com.academit.education.service.impl.CarreraServiceImpl;
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
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CarreraServiceImplTest {

    @Mock
    private CarreraRepository carreraRepository;

    @Mock
    private PlanEstudioRepository planEstudioRepository;

    @Mock
    private PlanMateriaRepository planMateriaRepository;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private InscripcionRepository inscripcionRepository;

    @Mock
    private AlumnoRepository alumnoRepository;

    @InjectMocks
    private CarreraServiceImpl service;

    private Carrera carrera;

    @BeforeEach
    void setUp() {
        carrera = new Carrera();
        carrera.setId(1);
        carrera.setCodigo("TUP");
        carrera.setNombre("Tecnicatura Universitaria en Programación");
        carrera.setTituloOtorgado("Técnico Universitario en Programación");
        carrera.setDuracionAnios(3);
        carrera.setResolucion("Res. Ministerial N° 1420/22");
        carrera.setEstado("ACTIVA");
    }

    // ------------------------------------------------------------------
    // GET (listado y detalle)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("listarCarreras: devuelve el catálogo")
    void listarCarreras() {
        when(carreraRepository.findAll()).thenReturn(List.of(carrera));

        List<CarreraDTO> lista = service.listarCarreras();

        assertEquals(1, lista.size());
        assertEquals("TUP", lista.get(0).getCodigo());
        assertEquals("ACTIVA", lista.get(0).getEstado());
    }

    @Test
    @DisplayName("obtenerPorId: 404 si la carrera no existe")
    void obtenerPorIdInexistente() {
        when(carreraRepository.findById(9)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.obtenerPorId(9));
    }

    // ------------------------------------------------------------------
    // POST
    // ------------------------------------------------------------------

    @Test
    @DisplayName("crearCarrera: caso feliz, nace ACTIVA")
    void crearCarrera() {
        CarreraCreateDTO request = new CarreraCreateDTO();
        request.setCodigo("IA");
        request.setNombre("Inteligencia Artificial");
        request.setTituloOtorgado("Técnico en Inteligencia Artificial");
        request.setDuracionAnios(4);

        when(carreraRepository.existsByCodigo("IA")).thenReturn(false);
        when(carreraRepository.existsByNombre("Inteligencia Artificial")).thenReturn(false);
        when(carreraRepository.save(any(Carrera.class))).thenAnswer(inv -> inv.getArgument(0));

        CarreraDTO dto = service.crearCarrera(request);

        assertEquals("IA", dto.getCodigo());
        assertEquals("ACTIVA", dto.getEstado());
        assertTrue(dto.getDuracionAnios() > 0);
    }

    @Test
    @DisplayName("crearCarrera: rechaza código repetido")
    void crearCarreraCodigoRepetido() {
        CarreraCreateDTO request = new CarreraCreateDTO();
        request.setCodigo("TUP");
        request.setNombre("Otra Carrera");
        request.setTituloOtorgado("Otro título");
        request.setDuracionAnios(3);

        when(carreraRepository.existsByCodigo("TUP")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> service.crearCarrera(request));
        verify(carreraRepository, never()).save(any(Carrera.class));
    }

    @Test
    @DisplayName("crearCarrera: rechaza nombre repetido")
    void crearCarreraNombreRepetido() {
        CarreraCreateDTO request = new CarreraCreateDTO();
        request.setCodigo("NUEVA");
        request.setNombre("Tecnicatura Universitaria en Programación");
        request.setTituloOtorgado("Título");
        request.setDuracionAnios(3);

        when(carreraRepository.existsByCodigo("NUEVA")).thenReturn(false);
        when(carreraRepository.existsByNombre("Tecnicatura Universitaria en Programación"))
                .thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> service.crearCarrera(request));
    }

    @Test
    @DisplayName("crearCarrera: rechaza duración menor o igual a cero")
    void crearCarreraDuracionInvalida() {
        CarreraCreateDTO request = new CarreraCreateDTO();
        request.setCodigo("X1");
        request.setNombre("Carrera X");
        request.setTituloOtorgado("Título X");
        request.setDuracionAnios(0);

        assertThrows(BusinessRuleException.class, () -> service.crearCarrera(request));
    }

    // ------------------------------------------------------------------
    // PUT
    // ------------------------------------------------------------------

    @Test
    @DisplayName("actualizarCarrera: actualización parcial caso feliz")
    void actualizarCarrera() {
        when(carreraRepository.findById(1)).thenReturn(Optional.of(carrera));
        when(carreraRepository.existsByNombreAndIdNot("Programación", 1)).thenReturn(false);
        when(carreraRepository.save(any(Carrera.class))).thenAnswer(inv -> inv.getArgument(0));

        CarreraDTO dto = service.actualizarCarrera(1, nombre("Programación"));

        assertEquals("Programación", dto.getNombre());
        assertEquals("TUP", dto.getCodigo());
    }

    @Test
    @DisplayName("actualizarCarrera: rechaza estado fuera del catálogo")
    void actualizarCarreraEstadoInvalido() {
        when(carreraRepository.findById(1)).thenReturn(Optional.of(carrera));

        com.academit.education.dto.carrera.CarreraUpdateDTO request =
                new com.academit.education.dto.carrera.CarreraUpdateDTO();
        request.setEstado("SUSPENDIDA");

        assertThrows(BusinessRuleException.class, () -> service.actualizarCarrera(1, request));
    }

    private com.academit.education.dto.carrera.CarreraUpdateDTO nombre(String nombre) {
        com.academit.education.dto.carrera.CarreraUpdateDTO r =
                new com.academit.education.dto.carrera.CarreraUpdateDTO();
        r.setNombre(nombre);
        return r;
    }

    // ------------------------------------------------------------------
    // DELETE (baja lógica)
    // ------------------------------------------------------------------

    @Test
    @DisplayName("darDeBajaCarrera: caso feliz, queda INACTIVA sin borrar la fila")
    void darDeBajaCarrera() {
        when(carreraRepository.findById(1)).thenReturn(Optional.of(carrera));
        when(alumnoRepository.existsByPlanCarreraIdAndEstadoNombre(1, "ACTIVO")).thenReturn(false);
        when(cursoRepository.existsByCarreraIdAndCicloEstado(1, "ACTIVO")).thenReturn(false);
        when(carreraRepository.save(any(Carrera.class))).thenAnswer(inv -> inv.getArgument(0));

        service.darDeBajaCarrera(1);

        assertEquals("INACTIVA", carrera.getEstado());
        verify(carreraRepository).save(carrera);
    }

    @Test
    @DisplayName("darDeBajaCarrera: rechaza si la carrera ya está inactiva")
    void darDeBajaCarreraYaInactiva() {
        carrera.setEstado("INACTIVA");
        when(carreraRepository.findById(1)).thenReturn(Optional.of(carrera));

        assertThrows(BusinessRuleException.class, () -> service.darDeBajaCarrera(1));
    }

    @Test
    @DisplayName("darDeBajaCarrera: rechaza si tiene alumnos activos")
    void darDeBajaCarreraConAlumnos() {
        when(carreraRepository.findById(1)).thenReturn(Optional.of(carrera));
        when(alumnoRepository.existsByPlanCarreraIdAndEstadoNombre(1, "ACTIVO")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> service.darDeBajaCarrera(1));
        verify(carreraRepository, never()).save(any(Carrera.class));
    }

    @Test
    @DisplayName("darDeBajaCarrera: rechaza si tiene cursos en ciclo activo")
    void darDeBajaCarreraConCursos() {
        when(carreraRepository.findById(1)).thenReturn(Optional.of(carrera));
        when(alumnoRepository.existsByPlanCarreraIdAndEstadoNombre(1, "ACTIVO")).thenReturn(false);
        when(cursoRepository.existsByCarreraIdAndCicloEstado(1, "ACTIVO")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> service.darDeBajaCarrera(1));
        verify(carreraRepository, never()).save(any(Carrera.class));
    }

    // ------------------------------------------------------------------
    // GET /planes y /cursos
    // ------------------------------------------------------------------

    @Test
    @DisplayName("listarPlanesDeCarrera: 404 si la carrera no existe")
    void listarPlanesCarreraInexistente() {
        when(carreraRepository.findById(9)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> service.listarPlanesDeCarrera(9));
    }

    @Test
    @DisplayName("listarPlanesDeCarrera: devuelve planes con cantidadMaterias calculada")
    void listarPlanesDeCarrera() {
        when(carreraRepository.findById(1)).thenReturn(Optional.of(carrera));

        PlanEstudio plan = new PlanEstudio();
        plan.setId(5);
        plan.setCarrera(carrera);
        plan.setCodigo("PLAN-2024-TUP");
        plan.setNombre("Plan 2024");
        plan.setAnioAprobacion(2024);
        plan.setEstado("VIGENTE");
        when(planEstudioRepository.findByCarreraId(1)).thenReturn(List.of(plan));
        when(planMateriaRepository.findByPlanId(5)).thenReturn(
                List.of(new com.academit.education.model.PlanMateria()));

        List<PlanEstudioDTO> planes = service.listarPlanesDeCarrera(1);

        assertEquals(1, planes.size());
        assertEquals(1, planes.get(0).getCantidadMaterias());
        assertEquals("Tecnicatura Universitaria en Programación", planes.get(0).getCarreraNombre());
    }

    @Test
    @DisplayName("listarCursosDeCarrera: devuelve inscriptos y cuposDisponibles calculados")
    void listarCursosDeCarrera() {
        when(carreraRepository.findById(1)).thenReturn(Optional.of(carrera));

        Carrera carrera2 = new Carrera();
        carrera2.setId(1);
        CicloLectivo ciclo = new CicloLectivo();
        ciclo.setId(2);
        ciclo.setAnio(2026);

        PlanEstudio plan = new PlanEstudio();
        plan.setId(5);
        plan.setNombre("Plan 2024");
        plan.setCarrera(carrera2);

        Curso curso = new Curso();
        curso.setId(3);
        curso.setCodigo("1A-PRO-TM");
        curso.setNombre("1° Año A");
        curso.setTurno("MANANA");
        curso.setCapacidadMaxima(35);
        curso.setAula("Aula 101");
        curso.setCiclo(ciclo);
        curso.setCarrera(carrera2);
        curso.setPlan(plan);
        when(cursoRepository.findByCarreraId(1)).thenReturn(List.of(curso));

        Inscripcion inscripto = new Inscripcion();
        inscripto.setId(10);
        inscripto.setActiva(true);
        inscripto.setCurso(curso);
        when(inscripcionRepository.findByCursoIdIn(List.of(3))).thenReturn(List.of(inscripto));

        List<CursoDTO> cursos = service.listarCursosDeCarrera(1);

        assertEquals(1, cursos.size());
        assertEquals(1, cursos.get(0).getInscriptos());
        assertEquals(34, cursos.get(0).getCuposDisponibles());
        assertEquals(2026, cursos.get(0).getCicloLectivo());
        assertEquals("Plan 2024", cursos.get(0).getPlanNombre());
    }
}
