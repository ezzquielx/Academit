package com.academit.education.service;

import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.EstadoCuota;
import com.academit.education.repository.CuotaRepository;
import com.academit.education.repository.EstadoCuotaRepository;
import com.academit.education.service.impl.CuotaVencidaServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuotaVencidaServiceTest {

    /** Reloj fijo: 15 de octubre de 2026. */
    private static final LocalDate HOY = LocalDate.of(2026, 10, 15);
    private static final Clock RELOJ_FIJO = Clock.fixed(
            HOY.atStartOfDay(ZoneId.of("America/Argentina/Tucuman")).toInstant(),
            ZoneId.of("America/Argentina/Tucuman"));

    @Mock
    private CuotaRepository cuotaRepository;
    @Mock
    private EstadoCuotaRepository estadoCuotaRepository;

    private CuotaVencidaService cuotaVencidaService;

    @BeforeEach
    void setUp() {
        cuotaVencidaService = new CuotaVencidaServiceImpl(cuotaRepository, estadoCuotaRepository, RELOJ_FIJO);
    }

    private EstadoCuota estado(String nombre) {
        EstadoCuota e = new EstadoCuota();
        e.setNombre(nombre);
        return e;
    }

    private void catalogoDeEstados() {
        when(estadoCuotaRepository.findByNombre("PENDIENTE")).thenReturn(Optional.of(estado("PENDIENTE")));
        when(estadoCuotaRepository.findByNombre("VENCIDA")).thenReturn(Optional.of(estado("VENCIDA")));
    }

    @Test
    void marcarVencidasUsaLaFechaDelRelojYDevuelveLaCantidad() throws Exception {
        EstadoCuota pendiente = estado("PENDIENTE");
        EstadoCuota vencida = estado("VENCIDA");
        when(estadoCuotaRepository.findByNombre("PENDIENTE")).thenReturn(Optional.of(pendiente));
        when(estadoCuotaRepository.findByNombre("VENCIDA")).thenReturn(Optional.of(vencida));
        when(cuotaRepository.marcarVencidas(HOY, pendiente, vencida)).thenReturn(3);

        int actualizadas = cuotaVencidaService.marcarVencidas();

        assertEquals(3, actualizadas);
        verify(cuotaRepository).marcarVencidas(eq(HOY), eq(pendiente), eq(vencida));
    }

    @Test
    void ejecucionRepetidaEsIdempotenteYDevuelve0() {
        catalogoDeEstados();
        when(cuotaRepository.marcarVencidas(eq(HOY), any(EstadoCuota.class), any(EstadoCuota.class)))
                .thenReturn(2, 0);

        assertEquals(2, cuotaVencidaService.marcarVencidas());
        // La segunda pasada no encuentra PENDIENTE vencidas: no cambia nada más.
        assertEquals(0, cuotaVencidaService.marcarVencidas());
        verify(cuotaRepository, times(2)).marcarVencidas(eq(HOY), any(EstadoCuota.class), any(EstadoCuota.class));
    }

    @Test
    void elMetodoEsTransaccional() throws Exception {
        assertTrue(CuotaVencidaServiceImpl.class
                .getMethod("marcarVencidas")
                .isAnnotationPresent(Transactional.class),
                "marcarVencidas debe ser @Transactional para ser atómica");
    }

    @Test
    void laConsultaSoloMarcaPendientesAnterioresAHoy() throws Exception {
        // Sin base local accesible se valida la semántica de RF30/RN07 en la consulta:
        // estrictamente anterior a hoy (la cuota que vence hoy no está vencida) y
        // solo el estado indicado (PAGADA y ANULADA nunca se tocan).
        var query = CuotaRepository.class
                .getMethod("marcarVencidas", LocalDate.class, EstadoCuota.class, EstadoCuota.class)
                .getAnnotation(org.springframework.data.jpa.repository.Query.class);

        assertNotNull(query, "el repository debe exponer la consulta de RF30");
        String jpql = query.value().replaceAll("\\s+", " ").toLowerCase();
        assertTrue(jpql.contains("c.fecha_vencimiento < :hoy") || jpql.contains("c.fechavencimiento < :hoy"),
                "el vencimiento debe ser estrictamente anterior a hoy: " + jpql);
        assertTrue(jpql.contains("c.estado = :actual"),
                "solo cambia el estado indicado (PENDIENTE): " + jpql);
    }

    @Test
    void catalogoDeEstadosIncompletoLanza404() {
        when(estadoCuotaRepository.findByNombre("PENDIENTE")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cuotaVencidaService.marcarVencidas());
        verify(cuotaRepository, never()).marcarVencidas(any(), any(), any());
    }
}
