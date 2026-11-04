package com.academit.education.service;

import com.academit.education.dto.asignacion.AsignacionCreateDTO;
import com.academit.education.dto.asignacion.AsignacionDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.*;
import com.academit.education.repository.*;
import com.academit.education.service.impl.AsignacionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AsignacionServiceTest {

    @Mock
    private CursoMateriaRepository cursoMateriaRepository;

    @Mock
    private DocenteRepository docenteRepository;

    @Mock
    private MateriaRepository materiaRepository;

    @Mock
    private CursoRepository cursoRepository;

    @Mock
    private EvaluacionRepository evaluacionRepository;

    @InjectMocks
    private AsignacionServiceImpl asignacionService;

    private Docente docenteActivo;
    private Docente docenteInactivo;
    private Materia materia;
    private Curso curso;
    private CursoMateria asignacion;

    @BeforeEach
    void setUp() {
        EstadoPersona estadoActivo = new EstadoPersona();
        estadoActivo.setId(1);
        estadoActivo.setNombre("ACTIVO");

        EstadoPersona estadoInactivo = new EstadoPersona();
        estadoInactivo.setId(2);
        estadoInactivo.setNombre("INACTIVO");

        docenteActivo = new Docente();
        docenteActivo.setId(1);
        docenteActivo.setNombre("Carlos");
        docenteActivo.setApellido("Sánchez");
        docenteActivo.setDni("30111222");
        docenteActivo.setEmail("carlos.sanchez@academit.edu.ar");
        docenteActivo.setEstado(estadoActivo);

        docenteInactivo = new Docente();
        docenteInactivo.setId(2);
        docenteInactivo.setNombre("Laura");
        docenteInactivo.setApellido("García");
        docenteInactivo.setEstado(estadoInactivo);

        materia = new Materia();
        materia.setId(1);
        materia.setNombre("Programación I");
        materia.setActiva(true);

        curso = new Curso();
        curso.setId(1);
        curso.setNombre("1° Año - Turno Mañana");
        curso.setTurno("MANANA");
        curso.setCapacidadMaxima(30);

        asignacion = new CursoMateria();
        asignacion.setId(1);
        asignacion.setDocente(docenteActivo);
        asignacion.setMateria(materia);
        asignacion.setCurso(curso);
        asignacion.setRolDocente("TITULAR");
    }

    @Test
    @DisplayName("Listar asignaciones devuelve colección completa con nombres desnormalizados")
    void testListarAsignaciones() {
        when(cursoMateriaRepository.findAll()).thenReturn(List.of(asignacion));

        List<AsignacionDTO> resultado = asignacionService.listarAsignaciones();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        AsignacionDTO dto = resultado.get(0);
        assertEquals(1, dto.getIdAsignacion());
        assertEquals("Carlos Sánchez", dto.getDocenteNombre());
        assertEquals("Programación I", dto.getMateriaNombre());
        assertEquals("1° Año - Turno Mañana", dto.getCursoNombre());
        assertEquals("TITULAR", dto.getRolDocente());
    }

    @Test
    @DisplayName("Obtener por ID devuelve asignación existente")
    void testObtenerPorId_Exitoso() {
        when(cursoMateriaRepository.findById(1)).thenReturn(Optional.of(asignacion));

        AsignacionDTO dto = asignacionService.obtenerPorId(1);

        assertNotNull(dto);
        assertEquals(1, dto.getIdAsignacion());
        assertEquals("Carlos Sánchez", dto.getDocenteNombre());
    }

    @Test
    @DisplayName("Obtener por ID inexistente lanza ResourceNotFoundException")
    void testObtenerPorId_NoExiste() {
        when(cursoMateriaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> asignacionService.obtenerPorId(99));
    }

    @Test
    @DisplayName("Crear asignación exitosa persiste con rol TITULAR por defecto")
    void testCrearAsignacion_Exitoso() {
        AsignacionCreateDTO request = new AsignacionCreateDTO();
        request.setIdDocente(1);
        request.setIdMateria(1);
        request.setIdCurso(1);
        request.setRolDocente("Titular");

        when(docenteRepository.findById(1)).thenReturn(Optional.of(docenteActivo));
        when(materiaRepository.findById(1)).thenReturn(Optional.of(materia));
        when(cursoRepository.findById(1)).thenReturn(Optional.of(curso));
        when(cursoMateriaRepository.existsByCursoIdAndMateriaIdAndDocenteId(1, 1, 1)).thenReturn(false);
        when(cursoMateriaRepository.save(any(CursoMateria.class))).thenAnswer(inv -> {
            CursoMateria cm = inv.getArgument(0);
            cm.setId(2);
            return cm;
        });

        AsignacionDTO creada = asignacionService.crearAsignacion(request);

        assertNotNull(creada);
        assertEquals(2, creada.getIdAsignacion());
        assertEquals("Carlos Sánchez", creada.getDocenteNombre());
        assertEquals("TITULAR", creada.getRolDocente());
        verify(cursoMateriaRepository).save(any(CursoMateria.class));
    }

    @Test
    @DisplayName("Crear asignación rechaza docente en estado INACTIVO")
    void testCrearAsignacion_DocenteInactivo_LanzaBusinessRuleException() {
        AsignacionCreateDTO request = new AsignacionCreateDTO();
        request.setIdDocente(2);
        request.setIdMateria(1);
        request.setIdCurso(1);

        when(docenteRepository.findById(2)).thenReturn(Optional.of(docenteInactivo));
        when(materiaRepository.findById(1)).thenReturn(Optional.of(materia));
        when(cursoRepository.findById(1)).thenReturn(Optional.of(curso));

        assertThrows(BusinessRuleException.class, () -> asignacionService.crearAsignacion(request));
        verify(cursoMateriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear asignación duplicada para el mismo curso, materia y docente lanza BusinessRuleException")
    void testCrearAsignacion_Duplicada_LanzaBusinessRuleException() {
        AsignacionCreateDTO request = new AsignacionCreateDTO();
        request.setIdDocente(1);
        request.setIdMateria(1);
        request.setIdCurso(1);

        when(docenteRepository.findById(1)).thenReturn(Optional.of(docenteActivo));
        when(materiaRepository.findById(1)).thenReturn(Optional.of(materia));
        when(cursoRepository.findById(1)).thenReturn(Optional.of(curso));
        when(cursoMateriaRepository.existsByCursoIdAndMateriaIdAndDocenteId(1, 1, 1)).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> asignacionService.crearAsignacion(request));
        verify(cursoMateriaRepository, never()).save(any());
    }

    @Test
    @DisplayName("Eliminar asignación exitosa cuando no tiene evaluaciones asociadas")
    void testEliminarAsignacion_Exitoso() {
        when(cursoMateriaRepository.findById(1)).thenReturn(Optional.of(asignacion));
        when(evaluacionRepository.existsByCursoMateriaId(1)).thenReturn(false);

        asignacionService.eliminarAsignacion(1);

        verify(cursoMateriaRepository).delete(asignacion);
    }

    @Test
    @DisplayName("Eliminar asignación con evaluaciones históricas asociadas lanza BusinessRuleException")
    void testEliminarAsignacion_ConEvaluaciones_LanzaBusinessRuleException() {
        when(cursoMateriaRepository.findById(1)).thenReturn(Optional.of(asignacion));
        when(evaluacionRepository.existsByCursoMateriaId(1)).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> asignacionService.eliminarAsignacion(1));
        verify(cursoMateriaRepository, never()).delete(any());
    }
}

