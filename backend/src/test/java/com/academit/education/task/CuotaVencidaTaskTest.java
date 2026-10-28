package com.academit.education.task;

import com.academit.education.service.CuotaVencidaService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.scheduling.annotation.Scheduled;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CuotaVencidaTaskTest {

    @Mock
    private CuotaVencidaService cuotaVencidaService;

    @Test
    void scheduledInvocaAlService() {
        when(cuotaVencidaService.marcarVencidas()).thenReturn(0);

        new CuotaVencidaTask(cuotaVencidaService).ejecutar();

        verify(cuotaVencidaService, times(1)).marcarVencidas();
    }

    @Test
    void elCronEsConfigurable() throws Exception {
        Scheduled anotacion = CuotaVencidaTask.class
                .getMethod("ejecutar")
                .getAnnotation(Scheduled.class);

        assertNotNull(anotacion, "ejecutar debe estar anotado con @Scheduled");
        assertTrue(anotacion.cron().contains("academit.cuotas.cron"),
                "el cron debe salir de application.properties");
        assertTrue(anotacion.zone().contains("academit.zone-id"),
                "la zona horaria debe ser configurable");
    }
}
