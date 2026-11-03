package com.academit.education.service;

import com.academit.education.dto.pago.PagoCreateDTO;
import com.academit.education.dto.pago.PagoDTO;

import java.util.List;

/**
 * Pagos arancelarios (RF32, RF33, RN08, RN09, RN10).
 */
public interface PagoService {

    /** Historial de pagos; un alumno solo ve los suyos. */
    List<PagoDTO> listarPagos(String username, Integer idAlumno);

    /** Registra el pago total de una cuota y emite el comprobante (201). */
    PagoDTO registrarPago(String username, PagoCreateDTO request);
}
