package com.academit.education.service;

import com.academit.education.dto.ciclo.CicloLectivoCreateDTO;
import com.academit.education.dto.ciclo.CicloLectivoDTO;
import com.academit.education.dto.ciclo.CicloLectivoUpdateDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.CicloLectivo;
import com.academit.education.repository.CicloLectivoRepository;
import com.academit.education.service.impl.CicloLectivoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CicloLectivoServiceImplTest {

    @Mock
    private CicloLectivoRepository cicloLectivoRepository;

    @InjectMocks
    private CicloLectivoServiceImpl service;

    private CicloLectivo ciclo2026;

    @BeforeEach
    void setUp() {
        ciclo2026 = new CicloLectivo();
        ciclo2026.setId(1);
        ciclo2026.setAnio(2026);
        ciclo2026.setNombre("Ciclo Lectivo 2026");
        ciclo2026.setEstado("ACTIVO");
        ciclo2026.setFechaInicio(LocalDate.of(2026, 3, 2));
        ciclo2026.setFechaFin(LocalDate.of(2026, 12, 18));
    }

    private CicloLectivoCreateDTO createDTO(Integer anio, LocalDate inicio, LocalDate fin, String estado) {
        CicloLectivoCreateDTO dto = new CicloLectivoCreateDTO();
        dto.setAnio(anio);
        dto.setNombre("Ciclo Lectivo " + anio);
        dto.setFechaInicio(inicio);
        dto.setFechaFin(fin);
        dto.setEstado(estado);
        return dto;
    }

    private CicloLectivoUpdateDTO updateDTO(String nombre, LocalDate inicio, LocalDate fin, String estado) {
        CicloLectivoUpdateDTO dto = new CicloLectivoUpdateDTO();
        dto.setNombre(nombre);
        dto.setFechaInicio(inicio);
        dto.setFechaFin(fin);
        dto.setEstado(estado);
        return dto;
    }

    @Test
    @DisplayName("listarCiclos devuelve todos los ciclos ordenados por año descendente")
    void listarOrdenadoPorAnioDesc() {
        CicloLectivo ciclo2027 = new CicloLectivo();
        ciclo2027.setId(2);
        ciclo2027.setAnio(2027);
        ciclo2027.setNombre("Ciclo Lectivo 2027");
        ciclo2027.setEstado("PLANIFICADO");
        ciclo2027.setFechaInicio(LocalDate.of(2027, 3, 1));
        ciclo2027.setFechaFin(LocalDate.of(2027, 12, 17));
        when(cicloLectivoRepository.findAll()).thenReturn(List.of(ciclo2026, ciclo2027));

        List<CicloLectivoDTO> respuesta = service.listarCiclos();

        assertEquals(2, respuesta.size());
        assertEquals(2027, respuesta.get(0).getAnio());
        assertEquals(2026, respuesta.get(1).getAnio());
    }

    @Test
    @DisplayName("crearCiclo con estado ausente nace PLANIFICADO caso feliz")
    void crearSinEstadoNacePlanificado() {
        CicloLectivoCreateDTO request = createDTO(2027, LocalDate.of(2027, 3, 1), LocalDate.of(2027, 12, 17), null);
        CicloLectivo guardado = new CicloLectivo();
        guardado.setId(2);
        guardado.setAnio(2027);
        guardado.setNombre("Ciclo Lectivo 2027");
        guardado.setEstado("PLANIFICADO");
        guardado.setFechaInicio(request.getFechaInicio());
        guardado.setFechaFin(request.getFechaFin());
        when(cicloLectivoRepository.existsByAnio(2027)).thenReturn(false);
        when(cicloLectivoRepository.save(any(CicloLectivo.class))).thenReturn(guardado);

        CicloLectivoDTO respuesta = service.crearCiclo(request);

        assertEquals("PLANIFICADO", respuesta.getEstado());
        assertEquals(2027, respuesta.getAnio());
    }

    @Test
    @DisplayName("crearCiclo rechaza fechas con fin anterior o igual al inicio")
    void crearConFechasInvalidasLanzaRegla() {
        CicloLectivoCreateDTO request = createDTO(2027, LocalDate.of(2027, 3, 1), LocalDate.of(2027, 3, 1), null);

        BusinessRuleException error = assertThrows(BusinessRuleException.class,
                () -> service.crearCiclo(request));
        assertEquals("La fecha de fin debe ser posterior a la fecha de inicio", error.getMessage());
        verify(cicloLectivoRepository, never()).save(any(CicloLectivo.class));
    }    @Test
    @DisplayName("crearCiclo rechaza un año ya registrado")
    void crearConAnioDuplicadoLanzaRegla() {
        when(cicloLectivoRepository.existsByAnio(2026)).thenReturn(true);
        CicloLectivoCreateDTO request = createDTO(2026, LocalDate.of(2026, 3, 2), LocalDate.of(2026, 12, 18), null);

        assertThrows(BusinessRuleException.class, () -> service.crearCiclo(request));
        verify(cicloLectivoRepository, never()).save(any(CicloLectivo.class));
    }

    @Test
    @DisplayName("crearCiclo rechaza un estado fuera del catálogo")
    void crearConEstadoInvalidoLanzaRegla() {
        CicloLectivoCreateDTO request = createDTO(2027, LocalDate.of(2027, 3, 1), LocalDate.of(2027, 12, 17), "SUSPENDIDO");

        assertThrows(BusinessRuleException.class, () -> service.crearCiclo(request));
        verify(cicloLectivoRepository, never()).save(any(CicloLectivo.class));
    }

    @Test
    @DisplayName("actualizarCiclo aplica solo los campos enviados caso feliz")
    void actualizarCasoFeliz() {
        when(cicloLectivoRepository.findById(1)).thenReturn(Optional.of(ciclo2026));
        when(cicloLectivoRepository.save(any(CicloLectivo.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        CicloLectivoDTO respuesta = service.actualizarCiclo(1, updateDTO("Ciclo 2026 Renombrado", null, null, "CERRADO"));

        assertEquals("Ciclo 2026 Renombrado", respuesta.getNombre());
        assertEquals("CERRADO", respuesta.getEstado());
        // las fechas sin enviar no se tocan
        assertEquals(LocalDate.of(2026, 3, 2), respuesta.getFechaInicio());
    }

    @Test
    @DisplayName("actualizarCiclo con id inexistente lanza 404")
    void actualizarInexistenteLanza404() {
        when(cicloLectivoRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> service.actualizarCiclo(99, updateDTO(null, null, null, null)));
    }

    @Test
    @DisplayName("actualizarCiclo rechaza la combinación de fechas que deja fin < inicio")
    void actualizarConFechasInvalidasLanzaRegla() {
        when(cicloLectivoRepository.findById(1)).thenReturn(Optional.of(ciclo2026));
        CicloLectivoUpdateDTO request = updateDTO(null, LocalDate.of(2026, 12, 20), null, null);

        assertThrows(BusinessRuleException.class, () -> service.actualizarCiclo(1, request));
        verify(cicloLectivoRepository, never()).save(any(CicloLectivo.class));
    }

    @Test
    @DisplayName("actualizarCiclo rechaza un estado fuera del catálogo")
    void actualizarConEstadoInvalidoLanzaRegla() {
        when(cicloLectivoRepository.findById(1)).thenReturn(Optional.of(ciclo2026));
        CicloLectivoUpdateDTO request = updateDTO(null, null, null, "VENCIDO");

        assertThrows(BusinessRuleException.class, () -> service.actualizarCiclo(1, request));
        verify(cicloLectivoRepository, never()).save(any(CicloLectivo.class));
    }
}
