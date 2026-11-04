package com.academit.education.service.impl;

import com.academit.education.dto.cuota.CuotaDTO;
import com.academit.education.exception.ConflictException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Cuota;
import com.academit.education.model.EstadoCuota;
import com.academit.education.model.Pago;
import com.academit.education.model.Usuario;
import com.academit.education.repository.CuotaRepository;
import com.academit.education.repository.EstadoCuotaRepository;
import com.academit.education.repository.PagoRepository;
import com.academit.education.repository.UsuarioRepository;
import com.academit.education.service.CuotaDtoAssembler;
import com.academit.education.service.CuotaService;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Listado y anulacion de cuotas (RF28-RF31, RN06, RN07).
 * idAlumno y alumnoNombre salen de la matricula; fechaPago sale del pago
 * asociado porque la tabla cuota no guarda fecha de cancelacion.
 */
@Service
public class CuotaServiceImpl implements CuotaService {

    private static final String ROL_ALUMNO = "ALUMNO";
    private static final String ESTADO_PAGADA = "PAGADA";
    private static final String ESTADO_ANULADA = "ANULADA";

    private final CuotaRepository cuotaRepository;
    private final EstadoCuotaRepository estadoCuotaRepository;
    private final PagoRepository pagoRepository;
    private final UsuarioRepository usuarioRepository;
    private final CuotaDtoAssembler cuotaDtoAssembler;

    public CuotaServiceImpl(CuotaRepository cuotaRepository,
                            EstadoCuotaRepository estadoCuotaRepository,
                            PagoRepository pagoRepository,
                            UsuarioRepository usuarioRepository,
                            CuotaDtoAssembler cuotaDtoAssembler) {
        this.cuotaRepository = cuotaRepository;
        this.estadoCuotaRepository = estadoCuotaRepository;
        this.pagoRepository = pagoRepository;
        this.usuarioRepository = usuarioRepository;
        this.cuotaDtoAssembler = cuotaDtoAssembler;
    }

    @Override
    @Transactional(readOnly = true)
    public List<CuotaDTO> listarCuotas(String username, Integer idAlumno) {
        Usuario usuario = buscarUsuario(username);
        List<Cuota> cuotas;

        if (esAlumno(usuario)) {
            Integer idPropio = idAlumnoPropio(usuario);
            if (idPropio == null) {
                return List.of();
            }
            if (idAlumno != null && !idPropio.equals(idAlumno)) {
                throw new AccessDeniedException("Un alumno solo puede consultar sus propias cuotas");
            }
            cuotas = cuotaRepository.findByMatriculaAlumnoId(idPropio);
        } else {
            cuotas = idAlumno != null
                    ? cuotaRepository.findByMatriculaAlumnoId(idAlumno)
                    : cuotaRepository.findAll();
        }

        return mapear(cuotas);
    }

    @Override
    @Transactional
    public CuotaDTO anularCuota(Integer idCuota) {
        Cuota cuota = cuotaRepository.findById(idCuota)
                .orElseThrow(() -> new ResourceNotFoundException("Cuota no encontrada con ID: " + idCuota));

        String estado = nombreEstado(cuota);
        if (ESTADO_PAGADA.equalsIgnoreCase(estado)) {
            throw new ConflictException("No se puede anular una cuota que ya se encuentra pagada");
        }

        if (!ESTADO_ANULADA.equalsIgnoreCase(estado)) {
            EstadoCuota anulada = estadoCuotaRepository.findByNombre(ESTADO_ANULADA)
                    .orElseThrow(() -> new ResourceNotFoundException("Estado ANULADA no encontrado en el catálogo"));
            cuota.setEstado(anulada);
            cuotaRepository.save(cuota);
        }

        return cuotaDtoAssembler.toDto(cuota, fechaPago(cuota.getId()));
    }

    private List<CuotaDTO> mapear(List<Cuota> cuotas) {
        Map<Integer, LocalDate> fechasPago = new HashMap<>();
        if (!cuotas.isEmpty()) {
            List<Integer> ids = cuotas.stream().map(Cuota::getId).toList();
            for (Pago pago : pagoRepository.findByCuotaIdIn(ids)) {
                fechasPago.put(pago.getCuota().getId(), pago.getFecha());
            }
        }

        return cuotas.stream()
                .sorted(Comparator.comparing(Cuota::getFechaVencimiento,
                        Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Cuota::getId, Comparator.nullsLast(Comparator.naturalOrder())))
                .map(c -> cuotaDtoAssembler.toDto(c, fechasPago.get(c.getId())))
                .toList();
    }

    private LocalDate fechaPago(Integer idCuota) {
        return pagoRepository.findByCuotaIdIn(List.of(idCuota)).stream()
                .findFirst()
                .map(Pago::getFecha)
                .orElse(null);
    }

    private Usuario buscarUsuario(String username) {
        return usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));
    }

    private boolean esAlumno(Usuario usuario) {
        return usuario.getRol() != null && ROL_ALUMNO.equalsIgnoreCase(usuario.getRol().getNombre());
    }

    private Integer idAlumnoPropio(Usuario usuario) {
        return usuario.getAlumno() != null ? usuario.getAlumno().getId() : null;
    }

    private String nombreEstado(Cuota cuota) {
        return cuota.getEstado() != null ? cuota.getEstado().getNombre() : "";
    }
}
