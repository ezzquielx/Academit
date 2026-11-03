package com.academit.education.service;

import com.academit.education.dto.calificacion.EvaluacionCreateDTO;
import com.academit.education.dto.calificacion.EvaluacionDTO;
import com.academit.education.security.CustomUserDetails;

import java.util.List;

public interface EvaluacionService {

    List<EvaluacionDTO> listar(CustomUserDetails userDetails, String tipo, Integer idCurso);

    EvaluacionDTO crear(CustomUserDetails userDetails, EvaluacionCreateDTO request);
}
