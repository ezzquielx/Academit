package com.academit.education.task;

import com.academit.education.service.CuotaVencidaService;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

/**
 * Invocador de la tarea diaria de cuotas vencidas (RN07, RF30).
 * Solo delega en el service: toda la logica (y el log del resultado)
 * vive en {@link CuotaVencidaService#marcarVencidas()}.
 */
@Component
public class CuotaVencidaTask {

    private final CuotaVencidaService cuotaVencidaService;

    public CuotaVencidaTask(CuotaVencidaService cuotaVencidaService) {
        this.cuotaVencidaService = cuotaVencidaService;
    }

    @Scheduled(
            cron = "${academit.cuotas.cron:0 0 4 * * *}",
            zone = "${academit.zone-id:America/Argentina/Tucuman}")
    public void ejecutar() {
        cuotaVencidaService.marcarVencidas();
    }
}
