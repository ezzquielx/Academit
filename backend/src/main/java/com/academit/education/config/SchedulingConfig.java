package com.academit.education.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Habilita las tareas programadas (RN07, RF30).
 * El cron y la zona de la tarea de cuotas vencidas son configurables
 * en application.properties (academit.cuotas.cron / academit.zone-id).
 */
@Configuration
@EnableScheduling
public class SchedulingConfig {
}
