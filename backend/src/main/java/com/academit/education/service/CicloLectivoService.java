package com.academit.education.service;

import com.academit.education.dto.ciclo.CicloLectivoCreateDTO;
import com.academit.education.dto.ciclo.CicloLectivoDTO;
import com.academit.education.dto.ciclo.CicloLectivoUpdateDTO;

import java.util.List;

public interface CicloLectivoService {

    List<CicloLectivoDTO> listarCiclos();

    CicloLectivoDTO crearCiclo(CicloLectivoCreateDTO request);

    CicloLectivoDTO actualizarCiclo(Integer id, CicloLectivoUpdateDTO request);
}
