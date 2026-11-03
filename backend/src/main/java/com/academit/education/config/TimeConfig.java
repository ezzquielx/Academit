package com.academit.education.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Clock;
import java.time.ZoneId;

/**
 * Reloj inyectable para toda la logica que depende de "hoy"
 * (estado de pago de matriculas, cuotas vencidas, fecha de pagos).
 * Inyectar Clock en los services permite probar con fechas fijas.
 */
@Configuration
public class TimeConfig {

    @Bean
    public Clock clock(@Value("${academit.zone-id:America/Argentina/Tucuman}") String zonaHoraria) {
        return Clock.system(ZoneId.of(zonaHoraria));
    }
}
