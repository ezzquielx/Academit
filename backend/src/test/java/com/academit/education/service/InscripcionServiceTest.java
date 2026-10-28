package com.academit.education.service;

import com.academit.education.dto.inscripcion.InscripcionCreateDTO;
import com.academit.education.dto.inscripcion.InscripcionDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.*;
import com.academit.education.repository.*;
import com.academit.education.security.CustomUserDetails;
import com.academit.education.service.impl.InscripcionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class InscripcionServiceTest {

    @Mock
    private InscripcionRepository inscripcionRepository;

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private CursoMateriaRepository cursoMateriaRepository;

    @InjectMocks
    private InscripcionServiceImpl inscripcionService;

    private Alumno alumnoActivo;
    private Alumno alumnoInactivo;
    private Curso curso;
    private CicloLectivo ciclo2026;
    private Inscripcion inscripcionActiva;

    @BeforeEach
    void setUp() {
        EstadoPersona estadoActivo = new EstadoPersona();
        estadoActivo.setId(1);
        estadoActivo.setNombre("ACTIVO");

        EstadoPersona estadoInactivo = new EstadoPersona();
        estadoInactivo.setId(2);
        estadoInactivo.setNombre("INACTIVO");

        alumnoActivo = new Alumno();
        alumnoActivo.setId(1);
        alumnoActivo.setNombre("Lucía");
        alumnoActivo.setApellido("Benítez");
        alumnoActivo.setDni("42111222");
        alumnoActivo.setEstado(estadoActivo);

        alumnoInactivo = new Alumno();
        alumnoInactivo.setId(2);
        alumnoInactivo.setNombre("Mario");
        alumnoInactivo.setApellido("Rossi");
        alumnoInactivo.setEstado(estadoInactivo);

        ciclo2026 = new CicloLectivo();
        ciclo2026.setId(1);
        ciclo2026.setAnio(2026);
        ciclo2026.setNombre("Ciclo Lectivo 2026");

        curso = new Curso();
        curso.setId(1);
        curso.setNombre("1° Año - Turno Mañana");
        curso.setCapacidadMaxima(30);
        curso.setCiclo(ciclo2026);

        inscripcionActiva = new Inscripcion();
        inscripcionActiva.setId(1);
        inscripcionActiva.setAlumno(alumnoActivo);
        inscripcionActiva.setCurso(curso);
        inscripcionActiva.setCiclo(ciclo2026);
        inscripcionActiva.setFecha(LocalDate.now());
        inscripcionActiva.setActiva(true);
    }

    @Test
    @DisplayName("Listar inscripciones devuelve colección completa con datos desnormalizados")
    void testListarInscripciones() {
        when(inscripcionRepository.findAll()).thenReturn(List.of(inscripcionActiva));

        List<InscripcionDTO> resultado = inscripcionService.listarInscripciones(null);

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        InscripcionDTO dto = resultado.get(0);
        assertEquals(1, dto.getIdInscripcion());
        assertEquals("Lucía Benítez", dto.getAlumnoNombre());
        assertEquals("1° Año - Turno Mañana", dto.getCursoNombre());
        assertEquals(2026, dto.getCicloLectivo());
        assertEquals("CONFIRMADA", dto.getEstado());
    }

    @Test
    @DisplayName("Crear inscripción exitosa cuando hay cupo y el alumno no tiene inscripción previa en el ciclo")
    void testCrearInscripcion_Exitoso() {
        InscripcionCreateDTO request = new InscripcionCreateDTO();
        request.setIdAlumno(1);
        request.setIdCurso(1);
        request.setCicloLectivo(2026);

        when(cursoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(curso));
        when(alumnoRepository.findById(1)).thenReturn(Optional.of(alumnoActivo));
        when(inscripcionRepository.existsByAlumnoIdAndCicloIdAndActivaTrue(1, 1)).thenReturn(false);
        when(inscripcionRepository.countByCursoIdAndActivaTrue(1)).thenReturn(10L); // Capacidad 30
        when(inscripcionRepository.save(any(Inscripcion.class))).thenAnswer(inv -> {
            Inscripcion i = inv.getArgument(0);
            i.setId(2);
            return i;
        });

        InscripcionDTO creada = inscripcionService.crearInscripcion(request);

        assertNotNull(creada);
        assertEquals(2, creada.getIdInscripcion());
        assertEquals("CONFIRMADA", creada.getEstado());
        verify(inscripcionRepository).save(any(Inscripcion.class));
    }

    @Test
    @DisplayName("Crear inscripción rechaza por cupo lleno (RN03)")
    void testCrearInscripcion_CupoLleno_RN03() {
        InscripcionCreateDTO request = new InscripcionCreateDTO();
        request.setIdAlumno(1);
        request.setIdCurso(1);

        when(cursoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(curso));
        when(alumnoRepository.findById(1)).thenReturn(Optional.of(alumnoActivo));
        when(inscripcionRepository.existsByAlumnoIdAndCicloIdAndActivaTrue(1, 1)).thenReturn(false);
        when(inscripcionRepository.countByCursoIdAndActivaTrue(1)).thenReturn(30L); // Capacidad 30 completa

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> inscripcionService.crearInscripcion(request));
        assertTrue(ex.getMessage().contains("cupos"));
        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear inscripción rechaza segunda inscripción activa en el mismo ciclo (RN02)")
    void testCrearInscripcion_SegundaInscripcionMismoCiclo_RN02() {
        InscripcionCreateDTO request = new InscripcionCreateDTO();
        request.setIdAlumno(1);
        request.setIdCurso(1);

        when(cursoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(curso));
        when(alumnoRepository.findById(1)).thenReturn(Optional.of(alumnoActivo));
        when(inscripcionRepository.existsByAlumnoIdAndCicloIdAndActivaTrue(1, 1)).thenReturn(true);

        BusinessRuleException ex = assertThrows(BusinessRuleException.class, () -> inscripcionService.crearInscripcion(request));
        assertTrue(ex.getMessage().contains("RN02"));
        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear inscripción rechaza alumno en estado INACTIVO")
    void testCrearInscripcion_AlumnoInactivo() {
        InscripcionCreateDTO request = new InscripcionCreateDTO();
        request.setIdAlumno(2);
        request.setIdCurso(1);

        when(cursoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(curso));
        when(alumnoRepository.findById(2)).thenReturn(Optional.of(alumnoInactivo));

        assertThrows(BusinessRuleException.class, () -> inscripcionService.crearInscripcion(request));
        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear inscripción rechaza cuando el curso no pertenece al ciclo solicitado")
    void testCrearInscripcion_CicloIncompatible() {
        InscripcionCreateDTO request = new InscripcionCreateDTO();
        request.setIdAlumno(1);
        request.setIdCurso(1);
        request.setCicloLectivo(2027); // Curso tiene ciclo 2026

        when(cursoRepository.findByIdForUpdate(1)).thenReturn(Optional.of(curso));
        when(alumnoRepository.findById(1)).thenReturn(Optional.of(alumnoActivo));

        assertThrows(BusinessRuleException.class, () -> inscripcionService.crearInscripcion(request));
        verify(inscripcionRepository, never()).save(any());
    }

    @Test
    @DisplayName("Cancelar inscripción anula la inscripción (activa = false) liberando el cupo")
    void testCancelarInscripcion_Exitoso() {
        when(inscripcionRepository.findById(1)).thenReturn(Optional.of(inscripcionActiva));
        when(inscripcionRepository.save(any(Inscripcion.class))).thenReturn(inscripcionActiva);

        inscripcionService.cancelarInscripcion(1);

        assertFalse(inscripcionActiva.getActiva());
        verify(inscripcionRepository).save(inscripcionActiva);
        verify(inscripcionRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Alumno autenticado puede consultar solo sus propias inscripciones")
    void testObtenerPorId_AlumnoAccedeAOtroPerfil_LanzaAccessDenied() {
        Alumno otroAlumno = new Alumno();
        otroAlumno.setId(99);

        Usuario usuarioAlumno = new Usuario();
        usuarioAlumno.setId(10);
        Rol rolAlumno = new Rol();
        rolAlumno.setNombre("ALUMNO");
        usuarioAlumno.setRol(rolAlumno);
        usuarioAlumno.setAlumno(otroAlumno);
        CustomUserDetails userDetails = new CustomUserDetails(usuarioAlumno);

        when(inscripcionRepository.findById(1)).thenReturn(Optional.of(inscripcionActiva));

        assertThrows(AccessDeniedException.class, () -> inscripcionService.obtenerPorId(1, userDetails));
    }
}

