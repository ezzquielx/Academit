package com.academit.education.service;

import com.academit.education.dto.asistencia.AsistenciaDTO;
import com.academit.education.dto.asistencia.AsistenciasLoteDTO;
import com.academit.education.dto.asistencia.SituacionMateriaDTO;
import com.academit.education.dto.asistencia.SuccessResponseDTO;
import com.academit.education.security.CustomUserDetails;

import java.util.List;

public interface AsistenciaService {

    List<AsistenciaDTO> listar(CustomUserDetails userDetails, Integer idAlumno, Integer idCurso, String fecha);

    /** POST /api/asistencias/lote: upsert RN15, atomico. */
    SuccessResponseDTO registrarLote(CustomUserDetails userDetails, AsistenciasLoteDTO request);

    /**
     * Reutilizable por GET /api/alumnos/{id}/situacion-academica: porcentaje
     * de asistencia por alumno+curso+materia y condicion REGULAR
     * (asistencia &gt;= 75 % y sin notas desaprobadas sin recuperar), sobre
     * las cátedras del docente del token o todas si es administrador.
     */
    List<SituacionMateriaDTO> situacionAcademica(CustomUserDetails userDetails, Integer idAlumno);
}
