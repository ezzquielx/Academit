package com.academit.education.service.impl;

import com.academit.education.dto.pago.PagoCreateDTO;
import com.academit.education.dto.pago.PagoDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ConflictException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Cuota;
import com.academit.education.model.EstadoCuota;
import com.academit.education.model.MedioPago;
import com.academit.education.model.Matricula;
import com.academit.education.model.Pago;
import com.academit.education.model.Usuario;
import com.academit.education.repository.CuotaRepository;
import com.academit.education.repository.EstadoCuotaRepository;
import com.academit.education.repository.MedioPagoRepository;
import com.academit.education.repository.PagoRepository;
import com.academit.education.repository.UsuarioRepository;
import com.academit.education.service.CuotaDtoAssembler;
import com.academit.education.service.PagoService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Clock;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

/**
 * Registro y consulta de pagos (RF32, RF33, RN08, RN09, RN10).
 * - RN09: el pago es siempre por el importe completo de la cuota.
 * - RN08: un unico pago por cuota (restriccion unique uk_pago_cuota).
 * - El comprobante AAAA-NNNNNN se genera con el id obtenido, tras persistir.
 */
@Service
public class PagoServiceImpl implements PagoService {

    private static final String ESTADO_PAGADA = "PAGADA";
    private static final String ESTADO_ANULADA = "ANULADA";
    private static final String ESTADO_CANCELADA = "CANCELADA";
    private static final String ESTADO_ACREDITADO = "ACREDITADO";
    private static final String MEDIO_POR_DEFECTO = "EFECTIVO";

    private final PagoRepository pagoRepository;
    private final CuotaRepository cuotaRepository;
    private final UsuarioRepository usuarioRepository;
    private final MedioPagoRepository medioPagoRepository;
    private final EstadoCuotaRepository estadoCuotaRepository;
    private final CuotaDtoAssembler cuotaDtoAssembler;
    private final Clock clock;

    public PagoServiceImpl(PagoRepository pagoRepository,
                           CuotaRepository cuotaRepository,
                           UsuarioRepository usuarioRepository,
                           MedioPagoRepository medioPagoRepository,
                           EstadoCuotaRepository estadoCuotaRepository,
                           CuotaDtoAssembler cuotaDtoAssembler,
                           Clock clock) {
        this.pagoRepository = pagoRepository;
        this.cuotaRepository = cuotaRepository;
        this.usuarioRepository = usuarioRepository;
        this.medioPagoRepository = medioPagoRepository;
        this.estadoCuotaRepository = estadoCuotaRepository;
        this.cuotaDtoAssembler = cuotaDtoAssembler;
        this.clock = clock;
    }

    @Override
    @Transactional(readOnly = true)
    public List<PagoDTO> listarPagos(String username, Integer idAlumno) {
        Usuario usuario = buscarUsuario(username);
        List<Pago> pagos;

        if (esAlumno(usuario)) {
            Integer idPropio = idAlumnoPropio(usuario);
            if (idPropio == null) {
                return List.of();
            }
            if (idAlumno != null && !idPropio.equals(idAlumno)) {
                throw new AccessDeniedException("Un alumno solo puede consultar sus propios pagos");
            }
            pagos = pagoRepository.findByCuotaMatriculaAlumnoId(idPropio);
        } else {
            pagos = idAlumno != null
                    ? pagoRepository.findByCuotaMatriculaAlumnoId(idAlumno)
                    : pagoRepository.findAll();
        }

        return pagos.stream()
                .sorted(Comparator.comparing(Pago::getFecha, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Pago::getId, Comparator.nullsLast(Comparator.naturalOrder()))
                        .reversed())
                .map(this::toDto)
                .toList();
    }

    @Override
    @Transactional
    public PagoDTO registrarPago(String username, PagoCreateDTO request) {
        Cuota cuota = cuotaRepository.findById(request.getIdCuota())
                .orElseThrow(() -> new ResourceNotFoundException("Cuota no encontrada con ID: " + request.getIdCuota()));

        // 409: estados que impiden pagar la cuota o su matricula.
        String estadoCuota = nombreEstado(cuota);
        if (ESTADO_PAGADA.equalsIgnoreCase(estadoCuota)) {
            throw new ConflictException("La cuota ya se encuentra pagada");
        }
        if (ESTADO_ANULADA.equalsIgnoreCase(estadoCuota)) {
            throw new ConflictException("No se puede registrar pago sobre una cuota anulada");
        }
        Matricula matricula = cuota.getMatricula();
        if (matricula != null && matricula.getEstado() != null
                && ESTADO_CANCELADA.equalsIgnoreCase(matricula.getEstado().getNombre())) {
            throw new ConflictException("No se puede registrar pagos sobre una matricula cancelada");
        }

        // RN08: un pago por cuota.
        if (pagoRepository.existsByCuotaId(cuota.getId())) {
            throw new ConflictException("La cuota ya tiene un pago registrado");
        }

        // RN09: sin pagos parciales, el importe debe coincidir con el de la cuota.
        BigDecimal importe = request.getImporte();
        if (importe.compareTo(cuota.getImporte()) != 0) {
            throw new BusinessRuleException(String.format(
                    "El importe del pago ($%s) debe ser igual al importe de la cuota ($%s). No se permiten pagos parciales (RN09).",
                    importe, cuota.getImporte()));
        }

        Usuario usuario = buscarUsuario(username);
        MedioPago medioPago = resolverMedioPago(request.getMedioPago());
        LocalDate fechaPago = LocalDate.now(clock);

        Pago pago = new Pago();
        pago.setCuota(cuota);
        pago.setUsuario(usuario);
        pago.setFecha(fechaPago);
        pago.setImporte(importe);
        pago.setMedioPago(medioPago);
        // Marcador unico para poder persistir y obtener el id generado.
        pago.setComprobante("TMP-" + UUID.randomUUID().toString().substring(0, 7));
        pago = pagoRepository.saveAndFlush(pago);

        // RN10: comprobante oficial AAAA-NNNNNN (anio del pago e id con 6 digitos).
        pago.setComprobante(String.format("%04d-%06d", fechaPago.getYear(), pago.getId()));
        pago = pagoRepository.save(pago);

        EstadoCuota pagada = estadoCuotaRepository.findByNombre(ESTADO_PAGADA)
                .orElseThrow(() -> new ResourceNotFoundException("Estado PAGADA no encontrado en el catálogo"));
        cuota.setEstado(pagada);
        cuotaRepository.save(cuota);

        return toDto(pago);
    }

    private PagoDTO toDto(Pago pago) {
        Cuota cuota = pago.getCuota();
        Matricula matricula = cuota != null ? cuota.getMatricula() : null;
        var alumno = matricula != null ? matricula.getAlumno() : null;

        return PagoDTO.builder()
                .idPago(pago.getId())
                .idCuota(cuota != null ? cuota.getId() : null)
                .idAlumno(alumno != null ? alumno.getId() : null)
                .alumnoNombre(alumno != null ? alumno.getNombre() + " " + alumno.getApellido() : null)
                .concepto(cuota != null ? cuotaDtoAssembler.concepto(cuota) : null)
                .importe(pago.getImporte())
                .fechaPago(pago.getFecha())
                .medioPago(pago.getMedioPago() != null ? pago.getMedioPago().getNombre() : null)
                .comprobante(pago.getComprobante())
                // Los pagos registrados siempre quedan acreditados.
                .estado(ESTADO_ACREDITADO)
                .build();
    }

    /**
     * El contrato enumera TARJETA_CREDITO / TARJETA_DEBITO pero el catalogo de la
     * base guarda TARJETA; se resuelve por nombre con ese fallback.
     */
    private MedioPago resolverMedioPago(String nombre) {
        String buscado = (nombre == null || nombre.isBlank()) ? MEDIO_POR_DEFECTO : nombre.trim();
        if (buscado.startsWith("TARJETA")) {
            buscado = "TARJETA";
        }
        String finalNombre = buscado;
        return medioPagoRepository.findByNombre(buscado)
                .orElseThrow(() -> new BusinessRuleException("Medio de pago no reconocido: " + finalNombre));
    }

    private Usuario buscarUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private boolean esAlumno(Usuario usuario) {
        return usuario.getRol() != null
                && "ALUMNO".equalsIgnoreCase(usuario.getRol().getNombre());
    }

    private Integer idAlumnoPropio(Usuario usuario) {
        return usuario.getAlumno() != null ? usuario.getAlumno().getId() : null;
    }

    private String nombreEstado(Cuota cuota) {
        return cuota.getEstado() != null ? cuota.getEstado().getNombre() : "";
    }
}
