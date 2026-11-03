package com.academit.education.service;

import com.academit.education.dto.mesa.MesaExamenCreateDTO;
import com.academit.education.dto.mesa.MesaExamenDTO;
import com.academit.education.dto.mesa.MesaExamenUpdateDTO;
import com.academit.education.security.CustomUserDetails;

import java.util.List;

public interface MesaExamenService {

    List<MesaExamenDTO> listar();

    MesaExamenDTO crear(MesaExamenCreateDTO request);

    MesaExamenDTO actualizar(Integer id, MesaExamenUpdateDTO request);

    /** RF49: el alumno del token se inscribe a la mesa futura con cupo. */
    MesaExamenDTO inscribir(CustomUserDetails userDetails, Integer idMesa);

    /** RF50: baja lógica de la inscripción (libera el cupo). */
    MesaExamenDTO darDeBaja(CustomUserDetails userDetails, Integer idMesa, Integer idInscripcion);
}
