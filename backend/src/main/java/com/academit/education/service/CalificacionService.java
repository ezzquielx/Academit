package com.academit.education.service;

import com.academit.education.dto.calificacion.CalificacionDTO;
import com.academit.education.dto.calificacion.CalificacionesLoteDTO;
import com.academit.education.dto.calificacion.CalificacionSaveDTO;
import com.academit.education.security.CustomUserDetails;

import java.util.List;

public interface CalificacionService {

    List<CalificacionDTO> listar(CustomUserDetails userDetails, Integer idAlumno, String tipo,
            Integer idMateria, Integer idEvaluacion);

    CalificacionDTO guardar(CustomUserDetails userDetails, CalificacionSaveDTO request);

    List<CalificacionDTO> guardarLote(CustomUserDetails userDetails, Integer idEvaluacion,
            CalificacionesLoteDTO request);
}
