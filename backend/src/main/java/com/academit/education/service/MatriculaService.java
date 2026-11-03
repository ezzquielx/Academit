package com.academit.education.service;

import com.academit.education.dto.cuota.CuotaDTO;
import com.academit.education.dto.common.ApiResponseDTO;
import com.academit.education.dto.matricula.MatriculaCancelDTO;
import com.academit.education.dto.matricula.MatriculaDTO;
import com.academit.education.dto.matricula.MatriculaUpdateDTO;
import com.academit.education.model.Cuota;

import java.util.List;

/**
 * Matriculas anuales (RF25-RF27, RN05, RN18).
 */
public interface MatriculaService {

    List<MatriculaDTO> listarMatriculas();

    MatriculaDTO obtenerPorId(Integer idMatricula);

    /** PUT /api/matriculas/{id}: cambia el estado y, si pasa a CANCELADA, anula cuotas. */
    MatriculaDTO actualizarMatricula(Integer idMatricula, MatriculaUpdateDTO request);

    /** PATCH /api/matriculas/{id}: cancelacion definitiva (RF27); el alumno solo la suya. */
    ApiResponseDTO cancelarMatricula(Integer idMatricula, String username, MatriculaCancelDTO request);

    /** POST /api/matriculas/{id}/cuotas: genera 12 cuotas mensuales y 3 seguros (RN18). */
    List<CuotaDTO> generarCuotas(Integer idMatricula);

    /**
     * Estado de pago calculado (no se persiste): EN_MORA si hay al menos una
     * cuota vencida; AL_DIA si todas las ya vencidas (o todas) estan PAGADA;
     * PENDIENTE en el resto.
     */
    String calcularEstadoPago(List<Cuota> cuotas);
}
