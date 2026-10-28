package com.academit.education.service;

import com.academit.education.dto.pago.PagoCreateDTO;
import com.academit.education.dto.pago.PagoDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ConflictException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.*;
import com.academit.education.repository.*;
import com.academit.education.service.impl.PagoServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    /** Reloj fijo: 15 de octubre de 2026. */
    private static final LocalDate HOY = LocalDate.of(2026, 10, 15);
    private static final Clock RELOJ_FIJO = Clock.fixed(
            HOY.atStartOfDay(ZoneId.of("America/Argentina/Tucuman")).toInstant(),
            ZoneId.of("America/Argentina/Tucuman"));

    @Mock
    private PagoRepository pagoRepository;
    @Mock
    private CuotaRepository cuotaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;
    @Mock
    private MedioPagoRepository medioPagoRepository;
    @Mock
    private EstadoCuotaRepository estadoCuotaRepository;

    private PagoService pagoService;

    @BeforeEach
    void setUp() {
        pagoService = new PagoServiceImpl(
                pagoRepository,
                cuotaRepository,
                usuarioRepository,
                medioPagoRepository,
                estadoCuotaRepository,
                new CuotaDtoAssembler(RELOJ_FIJO),
                RELOJ_FIJO);
    }

    private EstadoCuota estadoCuota(String nombre) {
        EstadoCuota e = new EstadoCuota();
        e.setNombre(nombre);
        return e;
    }

    private Cuota cuota(Integer id, String estado, String estadoMatricula) {
        Alumno alumno = new Alumno();
        alumno.setId(7);
        alumno.setNombre("Lucía");
        alumno.setApellido("Benítez");

        EstadoMatricula estadoMat = new EstadoMatricula();
        estadoMat.setNombre(estadoMatricula);

        Matricula matricula = new Matricula();
        matricula.setId(1);
        matricula.setAlumno(alumno);
        matricula.setEstado(estadoMat);

        TipoCuota tipo = new TipoCuota();
        tipo.setNombre("CUOTA_MENSUAL");

        Cuota c = new Cuota();
        c.setId(id);
        c.setMatricula(matricula);
        c.setTipo(tipo);
        c.setNumero(1);
        c.setFechaVencimiento(LocalDate.of(2027, 1, 10));
        c.setImporte(new BigDecimal("12000.00"));
        c.setEstado(estadoCuota(estado));
        return c;
    }

    private Usuario usuario(String rol, Integer idAlumno) {
        Rol r = new Rol();
        r.setNombre(rol);
        Usuario u = new Usuario();
        u.setUsername("usuario");
        u.setRol(r);
        if (idAlumno != null) {
            Alumno a = new Alumno();
            a.setId(idAlumno);
            u.setAlumno(a);
        }
        return u;
    }

    private MedioPago medio(String nombre) {
        MedioPago m = new MedioPago();
        m.setId(1);
        m.setNombre(nombre);
        return m;
    }

    private Pago pago(Integer id, Cuota c, LocalDate fecha) {
        Pago p = new Pago();
        p.setId(id);
        p.setCuota(c);
        p.setFecha(fecha);
        p.setImporte(c.getImporte());
        p.setComprobante(String.format("2026-%06d", id));
        p.setMedioPago(medio("EFECTIVO"));
        return p;
    }

    private PagoCreateDTO request(Integer idCuota, BigDecimal importe, String medio) {
        return PagoCreateDTO.builder().idCuota(idCuota).importe(importe).medioPago(medio).build();
    }

    // ------------------------------------------------------------------
    // POST /api/pagos (RF32, RN08, RN09, RN10)
    // ------------------------------------------------------------------

    @Test
    void registrarPagoCompletoAsignaComprobanteYCuotaPagada() {
        Cuota c = cuota(5, "PENDIENTE", "ACTIVA");
        when(cuotaRepository.findById(5)).thenReturn(Optional.of(c));
        when(pagoRepository.existsByCuotaId(5)).thenReturn(false);
        when(usuarioRepository.findByUsername("administrativo")).thenReturn(Optional.of(usuario("ADMINISTRATIVO", null)));
        when(medioPagoRepository.findByNombre("EFECTIVO")).thenReturn(Optional.of(medio("EFECTIVO")));
        when(pagoRepository.saveAndFlush(any(Pago.class))).thenAnswer(inv -> {
            Pago p = inv.getArgument(0);
            p.setId(42);
            return p;
        });
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(estadoCuotaRepository.findByNombre("PAGADA")).thenReturn(Optional.of(estadoCuota("PAGADA")));

        PagoDTO dto = pagoService.registrarPago("administrativo",
                request(5, new BigDecimal("12000.00"), "EFECTIVO"));

        assertEquals("2026-000042", dto.getComprobante());
        assertTrue(dto.getComprobante().matches("\\d{4}-\\d{6}"), "comprobante fuera del formato AAAA-NNNNNN");
        assertEquals("ACREDITADO", dto.getEstado());
        assertEquals(HOY, dto.getFechaPago());
        assertEquals("Cuota 1 - Enero 2027", dto.getConcepto());
        assertEquals("Lucía Benítez", dto.getAlumnoNombre());
        assertEquals("PAGADA", c.getEstado().getNombre());
        verify(cuotaRepository).save(c);
    }

    @Test
    void importeDistintoRechaza400YLaCuotaSigueSinPagar() {
        Cuota c = cuota(5, "PENDIENTE", "ACTIVA");
        when(cuotaRepository.findById(5)).thenReturn(Optional.of(c));

        assertThrows(BusinessRuleException.class, () -> pagoService.registrarPago("administrativo",
                request(5, new BigDecimal("6000.00"), "EFECTIVO")));

        assertEquals("PENDIENTE", c.getEstado().getNombre());
        verify(pagoRepository, never()).saveAndFlush(any());
        verify(cuotaRepository, never()).save(any());
    }

    @Test
    void cuotaPagadaDevuelve409() {
        when(cuotaRepository.findById(5)).thenReturn(Optional.of(cuota(5, "PAGADA", "ACTIVA")));

        assertThrows(ConflictException.class, () -> pagoService.registrarPago("administrativo",
                request(5, new BigDecimal("12000.00"), null)));
        verify(pagoRepository, never()).saveAndFlush(any());
    }

    @Test
    void cuotaAnuladaDevuelve409() {
        when(cuotaRepository.findById(5)).thenReturn(Optional.of(cuota(5, "ANULADA", "ACTIVA")));

        assertThrows(ConflictException.class, () -> pagoService.registrarPago("administrativo",
                request(5, new BigDecimal("12000.00"), null)));
        verify(pagoRepository, never()).saveAndFlush(any());
    }

    @Test
    void matriculaCanceladaDevuelve409() {
        when(cuotaRepository.findById(5)).thenReturn(Optional.of(cuota(5, "PENDIENTE", "CANCELADA")));

        assertThrows(ConflictException.class, () -> pagoService.registrarPago("administrativo",
                request(5, new BigDecimal("12000.00"), null)));
        verify(pagoRepository, never()).saveAndFlush(any());
    }

    @Test
    void segundoPagoDeLaMismaCuotaDevuelve409() {
        when(cuotaRepository.findById(5)).thenReturn(Optional.of(cuota(5, "PENDIENTE", "ACTIVA")));
        when(pagoRepository.existsByCuotaId(5)).thenReturn(true);

        assertThrows(ConflictException.class, () -> pagoService.registrarPago("administrativo",
                request(5, new BigDecimal("12000.00"), null)));
        verify(pagoRepository, never()).saveAndFlush(any());
    }

    @Test
    void cuotaInexistenteDevuelve404() {
        when(cuotaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> pagoService.registrarPago("administrativo",
                request(99, new BigDecimal("12000.00"), null)));
    }

    @Test
    void medioTarjetaDeCreditoSeResuelveComoTarjetaDelCatalogo() {
        Cuota c = cuota(5, "PENDIENTE", "ACTIVA");
        when(cuotaRepository.findById(5)).thenReturn(Optional.of(c));
        when(pagoRepository.existsByCuotaId(5)).thenReturn(false);
        when(usuarioRepository.findByUsername("administrativo")).thenReturn(Optional.of(usuario("ADMINISTRATIVO", null)));
        when(medioPagoRepository.findByNombre("TARJETA")).thenReturn(Optional.of(medio("TARJETA")));
        when(pagoRepository.saveAndFlush(any(Pago.class))).thenAnswer(inv -> {
            Pago p = inv.getArgument(0);
            p.setId(7);
            return p;
        });
        when(pagoRepository.save(any(Pago.class))).thenAnswer(inv -> inv.getArgument(0));
        when(estadoCuotaRepository.findByNombre("PAGADA")).thenReturn(Optional.of(estadoCuota("PAGADA")));

        PagoDTO dto = pagoService.registrarPago("administrativo",
                request(5, new BigDecimal("12000.00"), "TARJETA_CREDITO"));

        assertEquals("TARJETA", dto.getMedioPago());
        verify(medioPagoRepository).findByNombre("TARJETA");
    }

    @Test
    void medioPagoDesconocidoRechaza400() {
        Cuota c = cuota(5, "PENDIENTE", "ACTIVA");
        when(cuotaRepository.findById(5)).thenReturn(Optional.of(c));
        when(pagoRepository.existsByCuotaId(5)).thenReturn(false);
        when(usuarioRepository.findByUsername("administrativo")).thenReturn(Optional.of(usuario("ADMINISTRATIVO", null)));
        when(medioPagoRepository.findByNombre("CHEQUE")).thenReturn(Optional.empty());

        assertThrows(BusinessRuleException.class, () -> pagoService.registrarPago("administrativo",
                request(5, new BigDecimal("12000.00"), "CHEQUE")));
        verify(pagoRepository, never()).saveAndFlush(any());
    }

    // ------------------------------------------------------------------
    // GET /api/pagos (RF33)
    // ------------------------------------------------------------------

    @Test
    void listarComoAdminDevuelveTodoOrdenadoDelMasReciente() {
        Cuota enero = cuota(1, "PAGADA", "ACTIVA");
        Cuota marzo = cuota(2, "PAGADA", "ACTIVA");
        when(usuarioRepository.findByUsername("administrativo")).thenReturn(Optional.of(usuario("ADMINISTRATIVO", null)));
        when(pagoRepository.findAll()).thenReturn(List.of(
                pago(1, enero, LocalDate.of(2026, 1, 8)),
                pago(2, marzo, LocalDate.of(2026, 3, 5))));

        List<PagoDTO> pagos = pagoService.listarPagos("administrativo", null);

        assertEquals(2, pagos.size());
        assertEquals(LocalDate.of(2026, 3, 5), pagos.get(0).getFechaPago());
        assertEquals(LocalDate.of(2026, 1, 8), pagos.get(1).getFechaPago());
        assertEquals("ACREDITADO", pagos.get(0).getEstado());
        assertEquals("Lucía Benítez", pagos.get(0).getAlumnoNombre());
    }

    @Test
    void listarConFiltroDeAlumno() {
        when(usuarioRepository.findByUsername("administrativo")).thenReturn(Optional.of(usuario("ADMINISTRATIVO", null)));
        when(pagoRepository.findByCuotaMatriculaAlumnoId(7)).thenReturn(List.of(
                pago(3, cuota(2, "PAGADA", "ACTIVA"), LocalDate.of(2026, 3, 5))));

        List<PagoDTO> pagos = pagoService.listarPagos("administrativo", 7);

        assertEquals(1, pagos.size());
        assertEquals(7, pagos.get(0).getIdAlumno());
        verify(pagoRepository, never()).findAll();
    }

    @Test
    void alumnoSoloVeSusPagos() {
        when(usuarioRepository.findByUsername("alumno1")).thenReturn(Optional.of(usuario("ALUMNO", 7)));
        when(pagoRepository.findByCuotaMatriculaAlumnoId(7)).thenReturn(List.of(
                pago(3, cuota(2, "PAGADA", "ACTIVA"), LocalDate.of(2026, 3, 5))));

        List<PagoDTO> pagos = pagoService.listarPagos("alumno1", null);

        assertEquals(1, pagos.size());
        verify(pagoRepository, never()).findAll();
    }

    @Test
    void alumnoNoPuedePedirLosPagosDeOtro() {
        when(usuarioRepository.findByUsername("alumno1")).thenReturn(Optional.of(usuario("ALUMNO", 7)));

        assertThrows(AccessDeniedException.class, () -> pagoService.listarPagos("alumno1", 8));
        verify(pagoRepository, never()).findAll();
    }

    @Test
    void alumnoSinPadronNoExponeDatos() {
        when(usuarioRepository.findByUsername("alumno9")).thenReturn(Optional.of(usuario("ALUMNO", null)));

        assertTrue(pagoService.listarPagos("alumno9", null).isEmpty());
        verify(pagoRepository, never()).findAll();
    }
}
