package com.academit.education.service;

import com.academit.education.dto.cuota.CuotaDTO;
import com.academit.education.exception.ConflictException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.*;
import com.academit.education.repository.CuotaRepository;
import com.academit.education.repository.EstadoCuotaRepository;
import com.academit.education.repository.PagoRepository;
import com.academit.education.repository.UsuarioRepository;
import com.academit.education.service.impl.CuotaServiceImpl;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CuotaServiceTest {

    /** Reloj fijo: 15 de octubre de 2026. */
    private static final LocalDate HOY = LocalDate.of(2026, 10, 15);
    private static final Clock RELOJ_FIJO = Clock.fixed(
            HOY.atStartOfDay(ZoneId.of("America/Argentina/Tucuman")).toInstant(),
            ZoneId.of("America/Argentina/Tucuman"));

    @Mock
    private CuotaRepository cuotaRepository;
    @Mock
    private EstadoCuotaRepository estadoCuotaRepository;
    @Mock
    private PagoRepository pagoRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    private CuotaService cuotaService;

    @BeforeEach
    void setUp() {
        cuotaService = new CuotaServiceImpl(
                cuotaRepository,
                estadoCuotaRepository,
                pagoRepository,
                usuarioRepository,
                new CuotaDtoAssembler(RELOJ_FIJO));
    }

    private EstadoCuota estado(String nombre) {
        EstadoCuota e = new EstadoCuota();
        e.setNombre(nombre);
        return e;
    }

    private Cuota cuota(Integer id, Integer idAlumno, String estadoNombre, LocalDate vencimiento) {
        Alumno alumno = new Alumno();
        alumno.setId(idAlumno);
        alumno.setNombre("Lucía");
        alumno.setApellido("Benítez");

        Matricula matricula = new Matricula();
        matricula.setId(idAlumno);
        matricula.setAlumno(alumno);

        TipoCuota tipo = new TipoCuota();
        tipo.setNombre("CUOTA_MENSUAL");

        Cuota c = new Cuota();
        c.setId(id);
        c.setMatricula(matricula);
        c.setTipo(tipo);
        c.setNumero(1);
        c.setFechaVencimiento(vencimiento);
        c.setImporte(new BigDecimal("1250.00"));
        c.setEstado(estado(estadoNombre));
        return c;
    }

    private Usuario usuario(String rol, Integer idAlumno) {
        Rol r = new Rol();
        r.setNombre(rol);
        Usuario u = new Usuario();
        u.setUsername("lucia");
        u.setRol(r);
        if (idAlumno != null) {
            Alumno a = new Alumno();
            a.setId(idAlumno);
            u.setAlumno(a);
        }
        return u;
    }

    private Pago pago(Cuota cuota, LocalDate fecha) {
        Pago p = new Pago();
        p.setCuota(cuota);
        p.setFecha(fecha);
        return p;
    }

    // ------------------------------------------------------------------
    // GET /api/cuotas (RF28-RF31)
    // ------------------------------------------------------------------

    @Test
    void listarComoAdministrativoDevuelveTodasConCamposCalculados() {
        Cuota pendiente = cuota(1, 7, "PENDIENTE", LocalDate.of(2026, 1, 10));
        Cuota pagada = cuota(2, 8, "PAGADA", LocalDate.of(2026, 2, 10));

        when(usuarioRepository.findByUsername("administrativo")).thenReturn(Optional.of(usuario("ADMINISTRATIVO", null)));
        when(cuotaRepository.findAll()).thenReturn(List.of(pendiente, pagada));
        when(pagoRepository.findByCuotaIdIn(List.of(1, 2))).thenReturn(List.of(pago(pagada, LocalDate.of(2026, 2, 8))));

        List<CuotaDTO> cuotas = cuotaService.listarCuotas("administrativo", null);

        assertEquals(2, cuotas.size());
        CuotaDTO primera = cuotas.get(0);
        assertEquals(7, primera.getIdAlumno());
        assertEquals("Lucía Benítez", primera.getAlumnoNombre());
        assertEquals("MENSUAL", primera.getTipo());
        // Enero 2026 ya vencio y sigue PENDIENTE en base: el response la muestra VENCIDA.
        assertEquals("VENCIDA", primera.getEstado());
        assertNull(primera.getFechaPago());
        assertEquals(LocalDate.of(2026, 2, 8), cuotas.get(1).getFechaPago());
    }

    @Test
    void listarConFiltroDeAlumno() {
        Cuota cuota = cuota(1, 7, "PENDIENTE", LocalDate.of(2027, 1, 10));

        when(usuarioRepository.findByUsername("administrativo")).thenReturn(Optional.of(usuario("ADMINISTRATIVO", null)));
        when(cuotaRepository.findByMatriculaAlumnoId(7)).thenReturn(List.of(cuota));
        when(pagoRepository.findByCuotaIdIn(List.of(1))).thenReturn(List.of());

        List<CuotaDTO> cuotas = cuotaService.listarCuotas("administrativo", 7);

        assertEquals(1, cuotas.size());
        assertEquals("PENDIENTE", cuotas.get(0).getEstado());
        verify(cuotaRepository, never()).findAll();
    }

    @Test
    void alumnoSoloVeSusCuotas() {
        Cuota propia = cuota(1, 7, "PENDIENTE", LocalDate.of(2027, 1, 10));

        when(usuarioRepository.findByUsername("alumno1")).thenReturn(Optional.of(usuario("ALUMNO", 7)));
        when(cuotaRepository.findByMatriculaAlumnoId(7)).thenReturn(List.of(propia));
        when(pagoRepository.findByCuotaIdIn(List.of(1))).thenReturn(List.of());

        List<CuotaDTO> cuotas = cuotaService.listarCuotas("alumno1", null);

        assertEquals(1, cuotas.size());
        assertEquals(7, cuotas.get(0).getIdAlumno());
        verify(cuotaRepository, never()).findAll();
    }

    @Test
    void alumnoNoPuedePedirLasCuotasDeOtro() {
        when(usuarioRepository.findByUsername("alumno1")).thenReturn(Optional.of(usuario("ALUMNO", 7)));

        assertThrows(AccessDeniedException.class, () -> cuotaService.listarCuotas("alumno1", 8));
        verify(cuotaRepository, never()).findAll();
    }

    // ------------------------------------------------------------------
    // PATCH /api/cuotas/{id}/anular (RF31)
    // ------------------------------------------------------------------

    @Test
    void anularCuotaPendienteLaPasaAAnulada() {
        Cuota cuota = cuota(1, 7, "PENDIENTE", LocalDate.of(2027, 1, 10));
        when(cuotaRepository.findById(1)).thenReturn(Optional.of(cuota));
        when(estadoCuotaRepository.findByNombre("ANULADA")).thenReturn(Optional.of(estado("ANULADA")));
        when(pagoRepository.findByCuotaIdIn(List.of(1))).thenReturn(List.of());

        CuotaDTO dto = cuotaService.anularCuota(1);

        assertEquals("ANULADA", dto.getEstado());
        verify(cuotaRepository).save(cuota);
    }

    @Test
    void anularCuotaPagadaDevuelve409() {
        when(cuotaRepository.findById(1)).thenReturn(Optional.of(cuota(1, 7, "PAGADA", LocalDate.of(2027, 1, 10))));

        assertThrows(ConflictException.class, () -> cuotaService.anularCuota(1));
        verify(cuotaRepository, never()).save(any());
    }

    @Test
    void anularCuotaYaAnuladaNoVuelveAGuardar() {
        Cuota cuota = cuota(1, 7, "ANULADA", LocalDate.of(2027, 1, 10));
        when(cuotaRepository.findById(1)).thenReturn(Optional.of(cuota));
        when(pagoRepository.findByCuotaIdIn(List.of(1))).thenReturn(List.of());

        CuotaDTO dto = cuotaService.anularCuota(1);

        assertEquals("ANULADA", dto.getEstado());
        verify(cuotaRepository, never()).save(any());
    }

    @Test
    void anularCuotaInexistenteLanza404() {
        when(cuotaRepository.findById(44)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> cuotaService.anularCuota(44));
    }

    @Test
    void listarSinAlumnoVinculadoNoExponeDatos() {
        Usuario alumnoSinPadron = usuario("ALUMNO", null);
        when(usuarioRepository.findByUsername("alumno9")).thenReturn(Optional.of(alumnoSinPadron));

        assertTrue(cuotaService.listarCuotas("alumno9", null).isEmpty());
        verify(cuotaRepository, never()).findAll();
        verify(cuotaRepository, never()).findByMatriculaAlumnoId(anyInt());
    }
}
