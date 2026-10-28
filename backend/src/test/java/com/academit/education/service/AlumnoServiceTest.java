package com.academit.education.service;

import com.academit.education.dto.alumno.AlumnoCreateDTO;
import com.academit.education.dto.alumno.AlumnoDTO;
import com.academit.education.dto.alumno.AlumnoUpdateDTO;
import com.academit.education.dto.alumno.SituacionAcademicaDTO;
import com.academit.education.dto.alumno.SituacionAdministrativaDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.*;
import com.academit.education.repository.*;
import com.academit.education.security.CustomUserDetails;
import com.academit.education.service.impl.AlumnoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlumnoServiceTest {

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private PlanEstudioRepository planEstudioRepository;

    @Mock
    private EstadoPersonaRepository estadoPersonaRepository;

    @Mock
    private InscripcionRepository inscripcionRepository;

    @Mock
    private MatriculaRepository matriculaRepository;

    @Mock
    private CalificacionRepository calificacionRepository;

    @Mock
    private AsistenciaRepository asistenciaRepository;

    @Mock
    private CuotaRepository cuotaRepository;

    @InjectMocks
    private AlumnoServiceImpl alumnoService;

    private Alumno alumno;
    private Carrera carrera;
    private PlanEstudio plan;
    private EstadoPersona estadoActivo;
    private EstadoPersona estadoInactivo;

    @BeforeEach
    void setUp() {
        carrera = new Carrera();
        carrera.setId(1);
        carrera.setNombre("Tecnicatura Superior en Desarrollo de Software");
        carrera.setDuracionAnios(3);
        carrera.setActiva(true);

        plan = new PlanEstudio();
        plan.setId(1);
        plan.setNombre("Plan 2024");
        plan.setAnioVigencia(2024);
        plan.setCarrera(carrera);

        estadoActivo = new EstadoPersona();
        estadoActivo.setId(1);
        estadoActivo.setNombre("ACTIVO");

        estadoInactivo = new EstadoPersona();
        estadoInactivo.setId(2);
        estadoInactivo.setNombre("INACTIVO");

        alumno = new Alumno();
        alumno.setId(1);
        alumno.setDni("42111222");
        alumno.setNombre("Lucía");
        alumno.setApellido("Benítez");
        alumno.setEmail("lucia.benitez@academit.edu.ar");
        alumno.setTelefono("1144556677");
        alumno.setDomicilio("Av. Siempreviva 742");
        alumno.setFechaNacimiento(LocalDate.of(2001, 5, 14));
        alumno.setFechaIngreso(LocalDate.of(2024, 3, 1));
        alumno.setPlan(plan);
        alumno.setEstado(estadoActivo);
    }

    @Test
    @DisplayName("Listar alumnos devuelve colección completa con campos calculados")
    void testListarAlumnos() {
        when(alumnoRepository.findAll()).thenReturn(List.of(alumno));
        when(inscripcionRepository.findByAlumnoIdAndActivaTrue(1)).thenReturn(List.of());
        when(matriculaRepository.findByAlumnoId(1)).thenReturn(List.of());

        List<AlumnoDTO> resultado = alumnoService.listarAlumnos();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        AlumnoDTO dto = resultado.get(0);
        assertEquals(1, dto.getIdAlumno());
        assertEquals("ALU-1001", dto.getLegajo());
        assertEquals("Lucía", dto.getNombre());
        assertEquals("Tecnicatura Superior en Desarrollo de Software", dto.getCarrera());
        assertEquals("Plan 2024", dto.getPlanNombre());
        assertEquals("ACTIVO", dto.getEstado());
        assertEquals("PENDIENTE", dto.getEstadoMatricula());
    }

    @Test
    @DisplayName("Obtener por ID devuelve alumno existente")
    void testObtenerPorId_Exitoso() {
        when(alumnoRepository.findById(1)).thenReturn(Optional.of(alumno));
        when(inscripcionRepository.findByAlumnoIdAndActivaTrue(1)).thenReturn(List.of());
        when(matriculaRepository.findByAlumnoId(1)).thenReturn(List.of());

        AlumnoDTO dto = alumnoService.obtenerPorId(1, null);

        assertNotNull(dto);
        assertEquals(1, dto.getIdAlumno());
        assertEquals("42111222", dto.getDni());
    }

    @Test
    @DisplayName("Obtener por ID lanza ResourceNotFoundException cuando no existe")
    void testObtenerPorId_NoExiste() {
        when(alumnoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> alumnoService.obtenerPorId(99, null));
    }

    @Test
    @DisplayName("Alumno autenticado puede consultar su propio perfil")
    void testObtenerPorId_AlumnoAccedeASuPropioPerfil() {
        Usuario usuarioAlumno = new Usuario();
        usuarioAlumno.setId(10);
        Rol rolAlumno = new Rol();
        rolAlumno.setNombre("ALUMNO");
        usuarioAlumno.setRol(rolAlumno);
        usuarioAlumno.setAlumno(alumno);
        CustomUserDetails userDetails = new CustomUserDetails(usuarioAlumno);

        when(alumnoRepository.findById(1)).thenReturn(Optional.of(alumno));
        when(inscripcionRepository.findByAlumnoIdAndActivaTrue(1)).thenReturn(List.of());
        when(matriculaRepository.findByAlumnoId(1)).thenReturn(List.of());

        AlumnoDTO dto = alumnoService.obtenerPorId(1, userDetails);

        assertNotNull(dto);
        assertEquals(1, dto.getIdAlumno());
    }

    @Test
    @DisplayName("Alumno autenticado recibe 403 Forbidden al intentar consultar datos de otro alumno")
    void testObtenerPorId_AlumnoAccedeAOtroPerfil_LanzaAccessDenied() {
        Alumno otroAlumno = new Alumno();
        otroAlumno.setId(2);

        Usuario usuarioAlumno = new Usuario();
        usuarioAlumno.setId(10);
        Rol rolAlumno = new Rol();
        rolAlumno.setNombre("ALUMNO");
        usuarioAlumno.setRol(rolAlumno);
        usuarioAlumno.setAlumno(otroAlumno);
        CustomUserDetails userDetails = new CustomUserDetails(usuarioAlumno);

        assertThrows(AccessDeniedException.class, () -> alumnoService.obtenerPorId(1, userDetails));
        verify(alumnoRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Crear alumno exitoso asigna estado ACTIVO y persiste")
    void testCrearAlumno_Exitoso() {
        AlumnoCreateDTO request = new AlumnoCreateDTO();
        request.setNombre("Martín");
        request.setApellido("Gómez");
        request.setDni("40123456");
        request.setEmail("martin.gomez@academit.edu.ar");
        request.setFechaNacimiento(LocalDate.of(2000, 1, 15));
        request.setIdCarrera(1);
        request.setIdPlan(1);

        when(alumnoRepository.existsByDni("40123456")).thenReturn(false);
        when(alumnoRepository.existsByEmail("martin.gomez@academit.edu.ar")).thenReturn(false);
        when(planEstudioRepository.findById(1)).thenReturn(Optional.of(plan));
        when(estadoPersonaRepository.findByNombre("ACTIVO")).thenReturn(Optional.of(estadoActivo));
        when(alumnoRepository.save(any(Alumno.class))).thenAnswer(invocation -> {
            Alumno a = invocation.getArgument(0);
            a.setId(2);
            return a;
        });

        AlumnoDTO creado = alumnoService.crearAlumno(request);

        assertNotNull(creado);
        assertEquals(2, creado.getIdAlumno());
        assertEquals("ALU-1002", creado.getLegajo());
        assertEquals("Martín", creado.getNombre());
        assertEquals("ACTIVO", creado.getEstado());
        verify(alumnoRepository).save(any(Alumno.class));
    }

    @Test
    @DisplayName("Crear alumno lanza BusinessRuleException si el DNI ya existe")
    void testCrearAlumno_DniDuplicado() {
        AlumnoCreateDTO request = new AlumnoCreateDTO();
        request.setDni("42111222");

        when(alumnoRepository.existsByDni("42111222")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> alumnoService.crearAlumno(request));
        verify(alumnoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear alumno lanza BusinessRuleException si el plan no pertenece a la carrera (RN22)")
    void testCrearAlumno_PlanNoPerteneceACarrera_RN22() {
        AlumnoCreateDTO request = new AlumnoCreateDTO();
        request.setDni("39999888");
        request.setEmail("nuevo@academit.edu.ar");
        request.setIdCarrera(2); // Carrera distinta a la del plan (id=1)
        request.setIdPlan(1);

        when(alumnoRepository.existsByDni("39999888")).thenReturn(false);
        when(alumnoRepository.existsByEmail("nuevo@academit.edu.ar")).thenReturn(false);
        when(planEstudioRepository.findById(1)).thenReturn(Optional.of(plan));

        assertThrows(BusinessRuleException.class, () -> alumnoService.crearAlumno(request));
        verify(alumnoRepository, never()).save(any());
    }

    @Test
    @DisplayName("Actualizar alumno modifica campos y persiste")
    void testActualizarAlumno_Exitoso() {
        AlumnoUpdateDTO request = new AlumnoUpdateDTO();
        request.setNombre("Lucía Belén");
        request.setTelefono("1199887766");

        when(alumnoRepository.findById(1)).thenReturn(Optional.of(alumno));
        when(alumnoRepository.save(any(Alumno.class))).thenReturn(alumno);

        AlumnoDTO actualizado = alumnoService.actualizarAlumno(1, request);

        assertNotNull(actualizado);
        assertEquals("Lucía Belén", alumno.getNombre());
        assertEquals("1199887766", alumno.getTelefono());
        verify(alumnoRepository).save(alumno);
    }

    @Test
    @DisplayName("Eliminar alumno realiza baja lógica pasando estado a INACTIVO (RN13)")
    void testEliminarAlumno_BajaLogica() {
        when(alumnoRepository.findById(1)).thenReturn(Optional.of(alumno));
        when(estadoPersonaRepository.findByNombre("INACTIVO")).thenReturn(Optional.of(estadoInactivo));
        when(alumnoRepository.save(any(Alumno.class))).thenReturn(alumno);

        alumnoService.eliminarAlumno(1);

        assertEquals("INACTIVO", alumno.getEstado().getNombre());
        verify(alumnoRepository).save(alumno);
        verify(alumnoRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Obtener situación académica calcula regularidades, promedio y materias aprobadas")
    void testObtenerSituacionAcademica() {
        when(alumnoRepository.findById(1)).thenReturn(Optional.of(alumno));

        Materia materia1 = new Materia();
        materia1.setId(1);
        materia1.setNombre("Programación I");

        CursoMateria cm1 = new CursoMateria();
        cm1.setMateria(materia1);

        Evaluacion eval1 = new Evaluacion();
        eval1.setId(1);
        eval1.setCursoMateria(cm1);
        eval1.setDescripcion("Primer Parcial");
        TipoEvaluacion tipoEval = new TipoEvaluacion();
        tipoEval.setNombre("PARCIAL");
        eval1.setTipo(tipoEval);
        eval1.setFecha(LocalDate.of(2026, 5, 10));

        Calificacion calif1 = new Calificacion();
        calif1.setId(1);
        calif1.setAlumno(alumno);
        calif1.setEvaluacion(eval1);
        calif1.setNota(8);

        when(calificacionRepository.findByAlumnoId(1)).thenReturn(List.of(calif1));
        when(asistenciaRepository.findByAlumnoId(1)).thenReturn(List.of());

        SituacionAcademicaDTO situacion = alumnoService.obtenerSituacionAcademica(1, null);

        assertNotNull(situacion);
        assertEquals(1, situacion.getMateriasAprobadas());
        assertEquals(8.0, situacion.getPromedio());
        assertEquals(1, situacion.getCalificaciones().size());
        assertEquals("REGULAR", situacion.getCalificaciones().get(0).getCondicion());
    }

    @Test
    @DisplayName("Obtener situación administrativa calcula cuotas, mora y total adeudado")
    void testObtenerSituacionAdministrativa() {
        when(alumnoRepository.findById(1)).thenReturn(Optional.of(alumno));
        when(matriculaRepository.findByAlumnoId(1)).thenReturn(List.of());

        Cuota c1 = new Cuota();
        c1.setId(1);
        c1.setNumero(1);
        c1.setImporte(BigDecimal.valueOf(15000));
        EstadoCuota estadoPagada = new EstadoCuota();
        estadoPagada.setNombre("PAGADA");
        c1.setEstado(estadoPagada);

        Cuota c2 = new Cuota();
        c2.setId(2);
        c2.setNumero(2);
        c2.setImporte(BigDecimal.valueOf(15000));
        EstadoCuota estadoVencida = new EstadoCuota();
        estadoVencida.setNombre("VENCIDA");
        c2.setEstado(estadoVencida);

        when(cuotaRepository.findByMatriculaAlumnoId(1)).thenReturn(List.of(c1, c2));

        SituacionAdministrativaDTO situacion = alumnoService.obtenerSituacionAdministrativa(1, null);

        assertNotNull(situacion);
        assertEquals(1, situacion.getCuotasPagas());
        assertEquals(1, situacion.getCuotasPendientes());
        assertEquals(15000.0, situacion.getTotalDeuda());
        assertEquals(15000.0, situacion.getTotalAbonado());
        assertEquals("EN_MORA", situacion.getEstadoPago());
    }
}

