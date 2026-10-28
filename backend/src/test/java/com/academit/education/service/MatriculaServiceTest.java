package com.academit.education.service;

import com.academit.education.dto.cuota.CuotaDTO;
import com.academit.education.dto.matricula.MatriculaCancelDTO;
import com.academit.education.dto.matricula.MatriculaDTO;
import com.academit.education.dto.matricula.MatriculaUpdateDTO;
import com.academit.education.exception.ConflictException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.*;
import com.academit.education.repository.*;
import com.academit.education.service.impl.MatriculaServiceImpl;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MatriculaServiceTest {

    /** Reloj fijo: jueves 15 de octubre de 2026 (America/Argentina/Tucuman). */
    private static final LocalDate HOY = LocalDate.of(2026, 10, 15);
    private static final String[] MESES = {
            "Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio",
            "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"
    };
    private static final Clock RELOJ_FIJO = Clock.fixed(
            HOY.atStartOfDay(ZoneId.of("America/Argentina/Tucuman")).toInstant(),
            ZoneId.of("America/Argentina/Tucuman"));

    @Mock
    private MatriculaRepository matriculaRepository;
    @Mock
    private CuotaRepository cuotaRepository;
    @Mock
    private EstadoMatriculaRepository estadoMatriculaRepository;
    @Mock
    private EstadoCuotaRepository estadoCuotaRepository;
    @Mock
    private TipoCuotaRepository tipoCuotaRepository;
    @Mock
    private UsuarioRepository usuarioRepository;

    private MatriculaService matriculaService;

    @BeforeEach
    void setUp() {
        matriculaService = new MatriculaServiceImpl(
                matriculaRepository,
                cuotaRepository,
                estadoMatriculaRepository,
                estadoCuotaRepository,
                tipoCuotaRepository,
                usuarioRepository,
                new CuotaDtoAssembler(RELOJ_FIJO),
                RELOJ_FIJO
        );
    }

    private EstadoCuota estadoCuota(String nombre) {
        EstadoCuota estado = new EstadoCuota();
        estado.setNombre(nombre);
        return estado;
    }

    private Cuota cuota(String estado, LocalDate vencimiento) {
        Cuota c = new Cuota();
        c.setEstado(estadoCuota(estado));
        c.setFechaVencimiento(vencimiento);
        return c;
    }

    private Matricula matricula(Integer id, String estadoMatricula, BigDecimal importe) {
        Alumno alumno = new Alumno();
        alumno.setId(7);
        alumno.setNombre("Lucía");
        alumno.setApellido("Benítez");

        Carrera carrera = new Carrera();
        carrera.setId(3);
        carrera.setNombre("Tecnicatura Universitaria en Programación");

        PlanEstudio plan = new PlanEstudio();
        plan.setCarrera(carrera);
        alumno.setPlan(plan);

        CicloLectivo ciclo = new CicloLectivo();
        ciclo.setId(2);
        ciclo.setAnio(2026);
        ciclo.setNombre("Ciclo Lectivo 2026");

        Matricula matricula = new Matricula();
        matricula.setId(id);
        matricula.setAlumno(alumno);
        matricula.setCiclo(ciclo);
        matricula.setFecha(LocalDate.of(2026, 3, 2));
        matricula.setImporte(importe);
        matricula.setEstado(estadoMatricula(estadoMatricula));
        return matricula;
    }

    private EstadoMatricula estadoMatricula(String nombre) {
        EstadoMatricula estado = new EstadoMatricula();
        estado.setNombre(nombre);
        return estado;
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

    // ------------------------------------------------------------------
    // estadoPago calculado con Clock inyectable (RF25, RN05)
    // ------------------------------------------------------------------

    @Test
    void estadoPagoEnMoraCuandoHayCuotaVencida() {
        List<Cuota> cuotas = List.of(
                cuota("PAGADA", HOY.minusMonths(4)),
                cuota("VENCIDA", HOY.minusMonths(3)),
                cuota("PENDIENTE", HOY.plusDays(20)));

        assertEquals("EN_MORA", matriculaService.calcularEstadoPago(cuotas));
    }

    @Test
    void estadoPagoEnMoraCuandoHayPendienteVencidaPorFecha() {
        // La tarea todavia no la persistio como VENCIDA: el calculo la detecta igual.
        List<Cuota> cuotas = List.of(
                cuota("PAGADA", HOY.minusMonths(2)),
                cuota("PENDIENTE", HOY.minusDays(1)),
                cuota("PENDIENTE", HOY.plusMonths(1)));

        assertEquals("EN_MORA", matriculaService.calcularEstadoPago(cuotas));
    }

    @Test
    void estadoPagoAlDiaCuandoTodasLasExigiblesEstanPagadas() {
        List<Cuota> cuotas = List.of(
                cuota("PAGADA", HOY.minusMonths(2)),
                cuota("PAGADA", HOY.minusMonths(1)),
                cuota("PENDIENTE", HOY.plusMonths(1)));

        assertEquals("AL_DIA", matriculaService.calcularEstadoPago(cuotas));
    }

    @Test
    void estadoPagoPendienteCuandoNadaVencio() {
        List<Cuota> cuotas = List.of(
                cuota("PENDIENTE", HOY.plusDays(1)),
                cuota("PENDIENTE", HOY.plusMonths(2)));

        assertEquals("PENDIENTE", matriculaService.calcularEstadoPago(cuotas));
    }

    @Test
    void estadoPagoPendienteCuandoTodasEstanAnuladas() {
        List<Cuota> cuotas = List.of(cuota("ANULADA", HOY.minusMonths(1)));

        assertEquals("PENDIENTE", matriculaService.calcularEstadoPago(cuotas));
    }

    // ------------------------------------------------------------------
    // Consulta (RF25)
    // ------------------------------------------------------------------

    @Test
    void obtenerPorIdDevuelveCamposCalculados() {
        Matricula matricula = matricula(10, "ACTIVA", null);
        when(matriculaRepository.findById(10)).thenReturn(Optional.of(matricula));
        when(cuotaRepository.findByMatriculaId(10)).thenReturn(List.of(cuota("PAGADA", HOY.minusMonths(1))));

        MatriculaDTO dto = matriculaService.obtenerPorId(10);

        assertEquals(10, dto.getIdMatricula());
        assertEquals(7, dto.getIdAlumno());
        assertEquals("Lucía Benítez", dto.getAlumnoNombre());
        assertEquals(3, dto.getIdCarrera());
        assertEquals("Tecnicatura Universitaria en Programación", dto.getCarreraNombre());
        assertEquals(2026, dto.getCicloLectivo());
        assertEquals("Ciclo Lectivo 2026", dto.getCicloLectivoNombre());
        assertEquals("ACTIVA", dto.getEstado());
        assertEquals("AL_DIA", dto.getEstadoPago());
    }

    @Test
    void obtenerPorIdInexistenteLanza404() {
        when(matriculaRepository.findById(99)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> matriculaService.obtenerPorId(99));
    }

    // ------------------------------------------------------------------
    // Cancelacion (RF27) y cambio de estado (RF26)
    // ------------------------------------------------------------------

    @Test
    void cancelarAnulaPendientesYVencidasYNoTocaLasPagadas() {
        Matricula matricula = matricula(10, "ACTIVA", null);
        Cuota pendiente = cuota("PENDIENTE", HOY.plusMonths(1));
        Cuota vencida = cuota("VENCIDA", HOY.minusMonths(1));
        Cuota pagada = cuota("PAGADA", HOY.minusMonths(2));

        when(matriculaRepository.findById(10)).thenReturn(Optional.of(matricula));
        when(usuarioRepository.findByUsername("administrativo")).thenReturn(Optional.of(usuario("ADMINISTRATIVO", null)));
        when(estadoMatriculaRepository.findByNombre("CANCELADA")).thenReturn(Optional.of(estadoMatricula("CANCELADA")));
        when(estadoCuotaRepository.findByNombre("ANULADA")).thenReturn(Optional.of(estadoCuota("ANULADA")));
        when(cuotaRepository.findByMatriculaId(10)).thenReturn(new ArrayList<>(List.of(pendiente, vencida, pagada)));

        var respuesta = matriculaService.cancelarMatricula(10, "administrativo", new MatriculaCancelDTO("Baja voluntaria"));

        assertEquals("CANCELADA", matricula.getEstado().getNombre());
        assertEquals("ANULADA", pendiente.getEstado().getNombre());
        assertEquals("ANULADA", vencida.getEstado().getNombre());
        assertEquals("PAGADA", pagada.getEstado().getNombre());
        assertTrue(respuesta.isSuccess());
        verify(cuotaRepository, never()).save(pagada);
    }

    @Test
    void alumnoNoPuedeCancelarLaMatriculaDeOtro() {
        when(matriculaRepository.findById(10)).thenReturn(Optional.of(matricula(10, "ACTIVA", null)));
        when(usuarioRepository.findByUsername("alumno2")).thenReturn(Optional.of(usuario("ALUMNO", 99)));

        assertThrows(AccessDeniedException.class,
                () -> matriculaService.cancelarMatricula(10, "alumno2", new MatriculaCancelDTO(null)));
    }

    @Test
    void alumnoPuedeCancelarSuPropiaMatricula() {
        Matricula matricula = matricula(10, "ACTIVA", null);
        when(matriculaRepository.findById(10)).thenReturn(Optional.of(matricula));
        when(usuarioRepository.findByUsername("alumno1")).thenReturn(Optional.of(usuario("ALUMNO", 7)));
        when(estadoMatriculaRepository.findByNombre("CANCELADA")).thenReturn(Optional.of(estadoMatricula("CANCELADA")));
        when(estadoCuotaRepository.findByNombre("ANULADA")).thenReturn(Optional.of(estadoCuota("ANULADA")));
        when(cuotaRepository.findByMatriculaId(10)).thenReturn(new ArrayList<>(List.of(cuota("PENDIENTE", HOY.plusMonths(1)))));

        var respuesta = matriculaService.cancelarMatricula(10, "alumno1", new MatriculaCancelDTO(null));

        assertTrue(respuesta.isSuccess());
        assertEquals("CANCELADA", matricula.getEstado().getNombre());
    }

    @Test
    void actualizarACanceladaAnulaCuotasPendientes() {
        Matricula matricula = matricula(10, "ACTIVA", null);
        Cuota pendiente = cuota("PENDIENTE", HOY.plusMonths(1));

        when(matriculaRepository.findById(10)).thenReturn(Optional.of(matricula));
        when(estadoMatriculaRepository.findByNombre("CANCELADA")).thenReturn(Optional.of(estadoMatricula("CANCELADA")));
        when(estadoCuotaRepository.findByNombre("ANULADA")).thenReturn(Optional.of(estadoCuota("ANULADA")));
        when(cuotaRepository.findByMatriculaId(10)).thenReturn(new ArrayList<>(List.of(pendiente)));

        MatriculaDTO dto = matriculaService.actualizarMatricula(10, new MatriculaUpdateDTO("CANCELADA"));

        assertEquals("CANCELADA", dto.getEstado());
        assertEquals("ANULADA", pendiente.getEstado().getNombre());
    }

    @Test
    void actualizarConEstadoFueraDelCatalogoLanza404() {
        when(matriculaRepository.findById(10)).thenReturn(Optional.of(matricula(10, "ACTIVA", null)));
        when(estadoMatriculaRepository.findByNombre("BAJA")).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class,
                () -> matriculaService.actualizarMatricula(10, new MatriculaUpdateDTO("BAJA")));
    }

    // ------------------------------------------------------------------
    // Generacion de cuotas (RF28, RF46, RN18)
    // ------------------------------------------------------------------

    @Test
    void generarCuotasCrea12CuotasY3Seguros() {
        Matricula matricula = matricula(10, "ACTIVA", new BigDecimal("15000.00"));
        when(matriculaRepository.findById(10)).thenReturn(Optional.of(matricula));
        when(cuotaRepository.findByMatriculaId(10)).thenReturn(List.of());
        when(tipoCuotaRepository.findByNombre("CUOTA_MENSUAL")).thenReturn(Optional.of(tipo("CUOTA_MENSUAL")));
        when(tipoCuotaRepository.findByNombre("SEGURO_CUATRIMESTRAL")).thenReturn(Optional.of(tipo("SEGURO_CUATRIMESTRAL")));
        when(estadoCuotaRepository.findByNombre("PENDIENTE")).thenReturn(Optional.of(estadoCuota("PENDIENTE")));
        when(cuotaRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<CuotaDTO> cuotas = matriculaService.generarCuotas(10);

        assertEquals(15, cuotas.size());

        List<CuotaDTO> mensuales = cuotas.stream().filter(c -> "MENSUAL".equals(c.getTipo())).toList();
        List<CuotaDTO> seguros = cuotas.stream().filter(c -> "SEGURO_CUATRIMESTRAL".equals(c.getTipo())).toList();

        assertEquals(12, mensuales.size());
        assertEquals(3, seguros.size());

        for (int i = 1; i <= 12; i++) {
            CuotaDTO cuota = mensuales.get(i - 1);
            assertEquals(i, cuota.getMes());
            assertEquals(LocalDate.of(2026, i, 10), cuota.getFechaVencimiento());
            assertEquals(new BigDecimal("1250.00"), cuota.getImporte());
            assertEquals("Cuota " + i + " - " + MESES[i - 1] + " 2026", cuota.getConcepto());
            // Con el reloj fijo en 15/10/2026: hasta octubre ya vencieron.
            assertEquals(i <= 10 ? "VENCIDA" : "PENDIENTE", cuota.getEstado());
            assertEquals(7, cuota.getIdAlumno());
        }

        assertEquals(List.of(LocalDate.of(2026, 3, 10), LocalDate.of(2026, 7, 10), LocalDate.of(2026, 11, 10)),
                seguros.stream().map(CuotaDTO::getFechaVencimiento).toList());
        assertEquals(new BigDecimal("312.50"), seguros.get(0).getImporte());
        assertEquals("Seguro Cuatrimestral 1/3", seguros.get(0).getConcepto());
    }

    @Test
    void generarCuotasSinImporteUsaArancelPorDefecto() {
        Matricula matricula = matricula(10, "ACTIVA", null);
        when(matriculaRepository.findById(10)).thenReturn(Optional.of(matricula));
        when(cuotaRepository.findByMatriculaId(10)).thenReturn(List.of());
        when(tipoCuotaRepository.findByNombre("CUOTA_MENSUAL")).thenReturn(Optional.of(tipo("CUOTA_MENSUAL")));
        when(tipoCuotaRepository.findByNombre("SEGURO_CUATRIMESTRAL")).thenReturn(Optional.of(tipo("SEGURO_CUATRIMESTRAL")));
        when(estadoCuotaRepository.findByNombre("PENDIENTE")).thenReturn(Optional.of(estadoCuota("PENDIENTE")));
        when(cuotaRepository.saveAll(anyList())).thenAnswer(inv -> inv.getArgument(0));

        List<CuotaDTO> cuotas = matriculaService.generarCuotas(10);

        assertEquals(new BigDecimal("12000.00"), cuotas.get(0).getImporte());
        assertEquals(new BigDecimal("3000.00"), cuotas.get(12).getImporte());
    }

    @Test
    void generarCuotasSegundaVezDevuelve409() {
        when(matriculaRepository.findById(10)).thenReturn(Optional.of(matricula(10, "ACTIVA", null)));
        when(cuotaRepository.findByMatriculaId(10)).thenReturn(List.of(cuota("PENDIENTE", HOY.plusMonths(1))));

        assertThrows(ConflictException.class, () -> matriculaService.generarCuotas(10));
        verify(cuotaRepository, never()).saveAll(anyList());
    }

    @Test
    void generarCuotasEnMatriculaNoActivaDevuelve409() {
        when(matriculaRepository.findById(10)).thenReturn(Optional.of(matricula(10, "PENDIENTE", null)));

        assertThrows(ConflictException.class, () -> matriculaService.generarCuotas(10));
    }

    @Test
    void generarCuotasDeMatriculaInexistenteLanza404() {
        when(matriculaRepository.findById(44)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> matriculaService.generarCuotas(44));
    }

    private TipoCuota tipo(String nombre) {
        TipoCuota tipo = new TipoCuota();
        tipo.setNombre(nombre);
        return tipo;
    }
}
