package com.academit.education.service;

import com.academit.education.dto.cuota.CuotaDTO;

import java.util.List;

/**
 * Cuotas y aranceles (RF28-RF31, RN06, RN07, RN18).
 */
public interface CuotaService {

    /**
     * GET /api/cuotas. El filtro idAlumno es opcional; un alumno (rol ALUMNO)
     * solo puede ver sus propias cuotas.
     */
    List<CuotaDTO> listarCuotas(String username, Integer idAlumno);

    /** PATCH /api/cuotas/{id}/anular: anula una cuota no pagada (solo ADMINISTRADOR). */
    CuotaDTO anularCuota(Integer idCuota);
}
