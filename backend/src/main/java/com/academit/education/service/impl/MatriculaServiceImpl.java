package com.academit.education.service.impl;

import com.academit.education.dto.cuota.CuotaDTO;
import com.academit.education.dto.common.ApiResponseDTO;
import com.academit.education.dto.matricula.MatriculaCancelDTO;
import com.academit.education.dto.matricula.MatriculaDTO;
import com.academit.education.dto.matricula.MatriculaUpdateDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ConflictException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.*;
import com.academit.education.model.enums.EstadoPagoEnum;
import com.academit.education.repository.*;
import com.academit.education.service.CuotaDtoAssembler;
import com.academit.education.service.MatriculaService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Ciclo de vida de la matricula anual y generacion del plan de cuotas.
 *
 * Decisiones abiertas (registradas en docs/estado-del-proyecto.md):
 * - Importes: si la matricula define importe, la cuota mensual es importe / 12
 *   y el seguro equivale a un cuarto de la mensual (proporcion de los datos de
 *   prueba); sin importe se usan $12000,00 y $3000,00.
 * - Vencimientos: mensuales el dia 10 de enero a diciembre del anio del ciclo;
 *   seguros el dia 10 de marzo, julio y noviembre del mismo anio.
 * - PENDIENTE a ACTIVA: unico punto de cambio es PUT /api/matriculas/{id},
 *   reservado a ADMINISTRATIVO y ADMINISTRADOR.
 */
@Service
public class MatriculaServiceImpl implements MatriculaService {

    private static final String ESTADO_ACTIVA = "ACTIVA";
    private static final String ESTADO_CANCELADA = "CANCELADA";
    private static final BigDecimal CUOTA_MENSUAL_DEFAULT = new BigDecimal("12000.00");
    private static final BigDecimal SEGURO_DEFAULT = new BigDecimal("3000.00");
    private static final int MESES_DEL_CICLO = 12;
    private static final int SEGUROS_POR_CICLO = 3;

    private final MatriculaRepository matriculaRepository;
    private final CuotaRepository cuotaRepository;
    private final EstadoMatriculaRepository estadoMatriculaRepository;
    private final EstadoCuotaRepository estadoCuotaRepository;
    private final TipoCuotaRepository tipoCuotaRepository;
    private final UsuarioRepository usuarioRepository;
    private final CuotaDtoAssembler cuotaDtoAssembler;
    private final Clock clock;

    public MatriculaServiceImpl(MatriculaRepository matriculaRepository,
                                CuotaRepository cuotaRepository,
                                EstadoMatriculaRepository estadoMatriculaRepository,
                                EstadoCuotaRepository estadoCuotaRepository,
                                TipoCuotaRepository tipoCuotaRepository,
                                UsuarioRepository usuarioRepository,
                                CuotaDtoAssembler cuotaDtoAssembler,
                                Clock clock) {
        this.matriculaRepository = matriculaRepository;
        this.cuotaRepository = cuotaRepository;
        this.estadoMatriculaRepository = estadoMatriculaRepository;
        this.estadoCuotaRepository = estadoCuotaRepository;
        this.tipoCuotaRepository = tipoCuotaRepository;
        this.usuarioRepository = usuarioRepository;
        this.cuotaDtoAssembler = cuotaDtoAssembler;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<MatriculaDTO> listarMatriculas() {
        return matriculaRepository.findAll().stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public MatriculaDTO obtenerPorId(Integer idMatricula) {
        return toDTO(buscarMatricula(idMatricula));
    }

    @Override
    @Transactional
    public MatriculaDTO actualizarMatricula(Integer idMatricula, MatriculaUpdateDTO request) {
        Matricula matricula = buscarMatricula(idMatricula);

        EstadoMatricula nuevoEstado = estadoMatriculaRepository.findByNombre(request.getEstado())
                .orElseThrow(() -> new ResourceNotFoundException("Estado de matrícula no encontrado: " + request.getEstado()));

        matricula.setEstado(nuevoEstado);
        matriculaRepository.save(matricula);

        if (ESTADO_CANCELADA.equals(request.getEstado())) {
            anularCuotasImpagas(idMatricula);
        }
        return toDTO(matricula);
    }

    @Override
    @Transactional
    public ApiResponseDTO cancelarMatricula(Integer idMatricula, String username, MatriculaCancelDTO request) {
        Matricula matricula = buscarMatricula(idMatricula);
        verificarAlumnoPropio(username, matricula);

        EstadoMatricula cancelada = estadoMatriculaRepository.findByNombre(ESTADO_CANCELADA)
                .orElseThrow(() -> new ResourceNotFoundException("Estado CANCELADA no encontrado en el catálogo"));

        matricula.setEstado(cancelada);
        matriculaRepository.save(matricula);

        int anuladas = anularCuotasImpagas(idMatricula);
        String mensaje = "Matrícula cancelada";
        if (request != null && request.getMotivo() != null && !request.getMotivo().isBlank()) {
            mensaje = "Matrícula cancelada. Motivo: " + request.getMotivo();
        }
        return ApiResponseDTO.ok(mensaje + " (" + anuladas + " cuota(s) anulada(s))");
    }

    @Override
    @Transactional
    public List<CuotaDTO> generarCuotas(Integer idMatricula) {
        Matricula matricula = buscarMatricula(idMatricula);

        if (!ESTADO_ACTIVA.equalsIgnoreCase(nombreEstado(matricula))) {
            throw new ConflictException("Solo se pueden generar cuotas para una matrícula ACTIVA; la matrícula "
                    + idMatricula + " está " + nombreEstado(matricula));
        }
        if (!cuotaRepository.findByMatriculaId(idMatricula).isEmpty()) {
            throw new ConflictException("La matrícula " + idMatricula + " ya tiene cuotas generadas");
        }

        TipoCuota tipoMensual = tipoCuotaRepository.findByNombre("CUOTA_MENSUAL")
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de cuota CUOTA_MENSUAL no encontrado"));
        TipoCuota tipoSeguro = tipoCuotaRepository.findByNombre("SEGURO_CUATRIMESTRAL")
                .orElseThrow(() -> new ResourceNotFoundException("Tipo de cuota SEGURO_CUATRIMESTRAL no encontrado"));
        EstadoCuota pendiente = estadoCuotaRepository.findByNombre("PENDIENTE")
                .orElseThrow(() -> new ResourceNotFoundException("Estado PENDIENTE no encontrado en el catálogo"));

        BigDecimal[] importes = calcularImportes(matricula);
        Integer anio = matricula.getCiclo() != null ? matricula.getCiclo().getAnio() : LocalDate.now(clock).getYear();

        List<Cuota> cuotas = new ArrayList<>();
        for (int mes = 1; mes <= MESES_DEL_CICLO; mes++) {
            cuotas.add(nuevaCuota(matricula, tipoMensual, pendiente, mes,
                    LocalDate.of(anio, mes, 10), importes[0]));
        }
        int[] mesesSeguro = {3, 7, 11};
        for (int i = 0; i < SEGUROS_POR_CICLO; i++) {
            cuotas.add(nuevaCuota(matricula, tipoSeguro, pendiente, i + 1,
                    LocalDate.of(anio, mesesSeguro[i], 10), importes[1]));
        }

        List<Cuota> guardadas = cuotaRepository.saveAll(cuotas);
        return guardadas.stream().map(cuotaDtoAssembler::toDto).collect(Collectors.toList());
    }

    @Override
    public String calcularEstadoPago(List<Cuota> cuotas) {
        if (cuotas == null || cuotas.isEmpty()) {
            return EstadoPagoEnum.PENDIENTE.name();
        }

        LocalDate hoy = LocalDate.now(clock);
        List<Cuota> vigentes = cuotas.stream()
                .filter(c -> !"ANULADA".equalsIgnoreCase(nombreEstado(c)))
                .toList();

        if (vigentes.isEmpty()) {
            return EstadoPagoEnum.PENDIENTE.name();
        }

        // EN_MORA: al menos una cuota vencida (persistida o por fecha; la que
        // vence hoy todavia no esta vencida).
        boolean enMora = vigentes.stream().anyMatch(c -> esVencida(c, hoy));
        if (enMora) {
            return EstadoPagoEnum.EN_MORA.name();
        }

        List<Cuota> exigibles = vigentes.stream()
                .filter(c -> c.getFechaVencimiento() != null && !c.getFechaVencimiento().isAfter(hoy))
                .toList();

        List<Cuota> aEvaluar = exigibles.isEmpty() ? vigentes : exigibles;
        boolean todasPagadas = aEvaluar.stream()
                .allMatch(c -> "PAGADA".equalsIgnoreCase(nombreEstado(c)));

        return todasPagadas ? EstadoPagoEnum.AL_DIA.name() : EstadoPagoEnum.PENDIENTE.name();
    }

    private Matricula buscarMatricula(Integer idMatricula) {
        return matriculaRepository.findById(idMatricula)
                .orElseThrow(() -> new ResourceNotFoundException("Matrícula no encontrada con ID: " + idMatricula));
    }

    /** Un alumno (rol ALUMNO) solo puede operar sobre su propia matricula. */
    private void verificarAlumnoPropio(String username, Matricula matricula) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        String rol = usuario.getRol() != null ? usuario.getRol().getNombre() : "";
        if (!"ALUMNO".equalsIgnoreCase(rol)) {
            return;
        }
        Integer idAlumnoPropio = usuario.getAlumno() != null ? usuario.getAlumno().getId() : null;
        Integer idAlumnoMatricula = matricula.getAlumno() != null ? matricula.getAlumno().getId() : null;
        if (idAlumnoPropio == null || !idAlumnoPropio.equals(idAlumnoMatricula)) {
            throw new AccessDeniedException("Solo puede cancelar su propia matrícula");
        }
    }

    /** RN05/RF27: al cancelar, las cuotas PENDIENTE o VENCIDA pasan a ANULADA; las PAGADA no se tocan. */
    private int anularCuotasImpagas(Integer idMatricula) {
        EstadoCuota anulada = estadoCuotaRepository.findByNombre("ANULADA")
                .orElseThrow(() -> new ResourceNotFoundException("Estado ANULADA no encontrado en el catálogo"));

        int anuladas = 0;
        for (Cuota cuota : cuotaRepository.findByMatriculaId(idMatricula)) {
            String estado = nombreEstado(cuota);
            if ("PENDIENTE".equalsIgnoreCase(estado) || "VENCIDA".equalsIgnoreCase(estado)) {
                cuota.setEstado(anulada);
                cuotaRepository.save(cuota);
                anuladas++;
            }
        }
        return anuladas;
    }

    private BigDecimal[] calcularImportes(Matricula matricula) {
        BigDecimal importe = matricula.getImporte();
        if (importe == null || importe.signum() <= 0) {
            return new BigDecimal[]{CUOTA_MENSUAL_DEFAULT, SEGURO_DEFAULT};
        }

        BigDecimal mensual = importe.divide(BigDecimal.valueOf(MESES_DEL_CICLO), 2, RoundingMode.HALF_UP);
        if (mensual.signum() <= 0) {
            throw new BusinessRuleException("El importe de la matrícula es demasiado bajo para emitir cuotas");
        }
        BigDecimal seguro = mensual.divide(BigDecimal.valueOf(4), 2, RoundingMode.HALF_UP);
        return new BigDecimal[]{mensual, seguro};
    }

    private Cuota nuevaCuota(Matricula matricula, TipoCuota tipo, EstadoCuota estado,
                             Integer numero, LocalDate vencimiento, BigDecimal importe) {
        Cuota cuota = new Cuota();
        cuota.setMatricula(matricula);
        cuota.setTipo(tipo);
        cuota.setEstado(estado);
        cuota.setNumero(numero);
        cuota.setFechaVencimiento(vencimiento);
        cuota.setImporte(importe);
        return cuota;
    }

    private boolean esVencida(Cuota cuota, LocalDate hoy) {
        String estado = nombreEstado(cuota);
        return "VENCIDA".equalsIgnoreCase(estado)
                || ("PENDIENTE".equalsIgnoreCase(estado)
                && cuota.getFechaVencimiento() != null
                && cuota.getFechaVencimiento().isBefore(hoy));
    }

    private String nombreEstado(Matricula matricula) {
        return matricula.getEstado() != null ? matricula.getEstado().getNombre() : "";
    }

    private String nombreEstado(Cuota cuota) {
        return cuota.getEstado() != null ? cuota.getEstado().getNombre() : "";
    }

    private MatriculaDTO toDTO(Matricula matricula) {
        Alumno alumno = matricula.getAlumno();
        CicloLectivo ciclo = matricula.getCiclo();
        Carrera carrera = alumno != null && alumno.getPlan() != null ? alumno.getPlan().getCarrera() : null;
        List<Cuota> cuotas = cuotaRepository.findByMatriculaId(matricula.getId());

        return MatriculaDTO.builder()
                .idMatricula(matricula.getId())
                .idAlumno(alumno != null ? alumno.getId() : null)
                .alumnoNombre(alumno != null ? alumno.getNombre() + " " + alumno.getApellido() : null)
                .idCarrera(carrera != null ? carrera.getId() : null)
                .carreraNombre(carrera != null ? carrera.getNombre() : null)
                .idCiclo(ciclo != null ? ciclo.getId() : null)
                .cicloLectivoNombre(ciclo != null ? ciclo.getNombre() : null)
                .cicloLectivo(ciclo != null ? ciclo.getAnio() : null)
                .fecha(matricula.getFecha())
                .estado(nombreEstado(matricula))
                .estadoPago(calcularEstadoPago(cuotas))
                .build();
    }
}
