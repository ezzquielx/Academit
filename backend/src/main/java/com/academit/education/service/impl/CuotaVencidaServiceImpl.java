package com.academit.education.service.impl;

import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.EstadoCuota;
import com.academit.education.repository.CuotaRepository;
import com.academit.education.repository.EstadoCuotaRepository;
import com.academit.education.service.CuotaVencidaService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDate;

/**
 * Marcado de cuotas vencidas (RF30, RN07).
 * La consulta actualiza solo las cuotas en estado PENDIENTE con
 * fecha de vencimiento estrictamente anterior a hoy, por lo que es
 * idempotente: una cuota que vence hoy todavia no esta vencida.
 */
@Service
public class CuotaVencidaServiceImpl implements CuotaVencidaService {

    private static final Logger log = LoggerFactory.getLogger(CuotaVencidaServiceImpl.class);

    private static final String ESTADO_PENDIENTE = "PENDIENTE";
    private static final String ESTADO_VENCIDA = "VENCIDA";

    private final CuotaRepository cuotaRepository;
    private final EstadoCuotaRepository estadoCuotaRepository;
    private final Clock clock;

    public CuotaVencidaServiceImpl(CuotaRepository cuotaRepository,
                                   EstadoCuotaRepository estadoCuotaRepository,
                                   Clock clock) {
        this.cuotaRepository = cuotaRepository;
        this.estadoCuotaRepository = estadoCuotaRepository;
        this.clock = clock;
    }

    @Override
    @Transactional
    public int marcarVencidas() {
        LocalDate hoy = LocalDate.now(clock);

        EstadoCuota pendiente = estadoCuotaRepository.findByNombre(ESTADO_PENDIENTE)
                .orElseThrow(() -> new ResourceNotFoundException("Estado PENDIENTE no encontrado en el catálogo"));
        EstadoCuota vencida = estadoCuotaRepository.findByNombre(ESTADO_VENCIDA)
                .orElseThrow(() -> new ResourceNotFoundException("Estado VENCIDA no encontrado en el catálogo"));

        int actualizadas = cuotaRepository.marcarVencidas(hoy, pendiente, vencida);
        log.info("Cuotas vencidas marcadas al {}: {}", hoy, actualizadas);
        return actualizadas;
    }
}
