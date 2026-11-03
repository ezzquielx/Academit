package com.academit.education.service;

import com.academit.education.dto.declaracion.DeclaracionJuradaCreateDTO;
import com.academit.education.dto.declaracion.DeclaracionJuradaDTO;
import com.academit.education.security.CustomUserDetails;

import java.util.List;

public interface DeclaracionJuradaService {

    /**
     * GET: ALUMNO ve solo las suyas; ADMINISTRADOR y ADMINISTRATIVO pueden
     * filtrar por idAlumno e idCiclo.
     */
    List<DeclaracionJuradaDTO> listar(CustomUserDetails userDetails, Integer idAlumno, Integer idCiclo);

    /** RF51: presenta una declaración jurada de materias a cursar (RN20). */
    DeclaracionJuradaDTO presentar(CustomUserDetails userDetails, DeclaracionJuradaCreateDTO request);
}
