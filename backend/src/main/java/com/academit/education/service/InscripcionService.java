package com.academit.education.service;

import com.academit.education.dto.inscripcion.InscripcionCreateDTO;
import com.academit.education.dto.inscripcion.InscripcionDTO;
import com.academit.education.security.CustomUserDetails;

import java.util.List;

public interface InscripcionService {

    List<InscripcionDTO> listarInscripciones(CustomUserDetails userDetails);

    InscripcionDTO obtenerPorId(Integer id, CustomUserDetails userDetails);

    InscripcionDTO crearInscripcion(InscripcionCreateDTO request);

    void cancelarInscripcion(Integer id);
}

