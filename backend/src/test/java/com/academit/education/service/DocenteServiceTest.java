package com.academit.education.service;

import com.academit.education.dto.docente.DocenteCreateDTO;
import com.academit.education.dto.docente.DocenteDTO;
import com.academit.education.dto.docente.DocenteUpdateDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Docente;
import com.academit.education.model.EstadoPersona;
import com.academit.education.repository.DocenteRepository;
import com.academit.education.repository.EstadoPersonaRepository;
import com.academit.education.service.impl.DocenteServiceImpl;
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
class DocenteServiceTest {

    @Mock
    private DocenteRepository docenteRepository;

    @Mock
    private EstadoPersonaRepository estadoPersonaRepository;

    @InjectMocks
    private DocenteServiceImpl docenteService;

    private Docente docente;
    private EstadoPersona estadoActivo;
    private EstadoPersona estadoInactivo;

    @BeforeEach
    void setUp() {
        estadoActivo = new EstadoPersona();
        estadoActivo.setId(1);
        estadoActivo.setNombre("ACTIVO");

        estadoInactivo = new EstadoPersona();
        estadoInactivo.setId(2);
        estadoInactivo.setNombre("INACTIVO");

        docente = new Docente();
        docente.setId(1);
        docente.setDni("30111222");
        docente.setNombre("Carlos");
        docente.setApellido("Sánchez");
        docente.setEmail("carlos.sanchez@academit.edu.ar");
        docente.setTelefono("1144332211");
        docente.setTitulo("Licenciado en Sistemas");
        docente.setEstado(estadoActivo);
    }

    @Test
    @DisplayName("Listar docentes devuelve colección completa con legajo formateado")
    void testListarDocentes() {
        when(docenteRepository.findAll()).thenReturn(List.of(docente));

        List<DocenteDTO> resultado = docenteService.listarDocentes();

        assertNotNull(resultado);
        assertEquals(1, resultado.size());
        DocenteDTO dto = resultado.get(0);
        assertEquals(1, dto.getIdDocente());
        assertEquals("DOC-201", dto.getLegajo());
        assertEquals("Carlos", dto.getNombre());
        assertEquals("ACTIVO", dto.getEstado());
    }

    @Test
    @DisplayName("Obtener por ID devuelve docente existente")
    void testObtenerPorId_Exitoso() {
        when(docenteRepository.findById(1)).thenReturn(Optional.of(docente));

        DocenteDTO dto = docenteService.obtenerPorId(1);

        assertNotNull(dto);
        assertEquals(1, dto.getIdDocente());
        assertEquals("30111222", dto.getDni());
    }

    @Test
    @DisplayName("Obtener por ID inexistente lanza ResourceNotFoundException")
    void testObtenerPorId_NoExiste() {
        when(docenteRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> docenteService.obtenerPorId(99));
    }

    @Test
    @DisplayName("Crear docente exitoso persiste con estado ACTIVO")
    void testCrearDocente_Exitoso() {
        DocenteCreateDTO request = new DocenteCreateDTO();
        request.setNombre("Mariana");
        request.setApellido("López");
        request.setDni("31222333");
        request.setEmail("mariana.lopez@academit.edu.ar");
        request.setTelefono("1155667788");
        request.setTitulo("Ingeniera en Software");

        when(docenteRepository.existsByDni("31222333")).thenReturn(false);
        when(docenteRepository.existsByEmail("mariana.lopez@academit.edu.ar")).thenReturn(false);
        when(estadoPersonaRepository.findByNombre("ACTIVO")).thenReturn(Optional.of(estadoActivo));
        when(docenteRepository.save(any(Docente.class))).thenAnswer(inv -> {
            Docente d = inv.getArgument(0);
            d.setId(2);
            return d;
        });

        DocenteDTO creado = docenteService.crearDocente(request);

        assertNotNull(creado);
        assertEquals(2, creado.getIdDocente());
        assertEquals("DOC-202", creado.getLegajo());
        assertEquals("Mariana", creado.getNombre());
        assertEquals("ACTIVO", creado.getEstado());
        verify(docenteRepository).save(any(Docente.class));
    }

    @Test
    @DisplayName("Crear docente lanza BusinessRuleException si el DNI ya existe")
    void testCrearDocente_DniDuplicado() {
        DocenteCreateDTO request = new DocenteCreateDTO();
        request.setDni("30111222");

        when(docenteRepository.existsByDni("30111222")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> docenteService.crearDocente(request));
        verify(docenteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Crear docente lanza BusinessRuleException si el email ya existe")
    void testCrearDocente_EmailDuplicado() {
        DocenteCreateDTO request = new DocenteCreateDTO();
        request.setDni("35999888");
        request.setEmail("carlos.sanchez@academit.edu.ar");

        when(docenteRepository.existsByDni("35999888")).thenReturn(false);
        when(docenteRepository.existsByEmail("carlos.sanchez@academit.edu.ar")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> docenteService.crearDocente(request));
        verify(docenteRepository, never()).save(any());
    }

    @Test
    @DisplayName("Actualizar docente modifica campos y persiste")
    void testActualizarDocente_Exitoso() {
        DocenteUpdateDTO request = new DocenteUpdateDTO();
        request.setNombre("Carlos Alberto");
        request.setTelefono("1199001122");

        when(docenteRepository.findById(1)).thenReturn(Optional.of(docente));
        when(docenteRepository.save(any(Docente.class))).thenReturn(docente);

        DocenteDTO actualizado = docenteService.actualizarDocente(1, request);

        assertNotNull(actualizado);
        assertEquals("Carlos Alberto", docente.getNombre());
        assertEquals("1199001122", docente.getTelefono());
        verify(docenteRepository).save(docente);
    }

    @Test
    @DisplayName("Actualizar docente inexistente lanza ResourceNotFoundException")
    void testActualizarDocente_NoExiste() {
        DocenteUpdateDTO request = new DocenteUpdateDTO();
        when(docenteRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> docenteService.actualizarDocente(99, request));
    }

    @Test
    @DisplayName("Eliminar docente realiza baja lógica pasando estado a INACTIVO")
    void testEliminarDocente_BajaLogica() {
        when(docenteRepository.findById(1)).thenReturn(Optional.of(docente));
        when(estadoPersonaRepository.findByNombre("INACTIVO")).thenReturn(Optional.of(estadoInactivo));
        when(docenteRepository.save(any(Docente.class))).thenReturn(docente);

        docenteService.eliminarDocente(1);

        assertEquals("INACTIVO", docente.getEstado().getNombre());
        verify(docenteRepository).save(docente);
        verify(docenteRepository, never()).delete(any());
    }
}

