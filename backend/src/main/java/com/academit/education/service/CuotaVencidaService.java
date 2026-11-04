package com.academit.education.service;

/**
 * Tarea programada de cuotas vencidas (RF30, RN07).
 */
public interface CuotaVencidaService {

    /**
     * Pasa a VENCIDA toda cuota PENDIENTE con vencimiento anterior a hoy.
     * Idempotente: una cuota ya VENCIDA, PAGADA o ANULADA no se toca.
     *
     * @return cantidad de cuotas actualizadas
     */
    int marcarVencidas();
}
