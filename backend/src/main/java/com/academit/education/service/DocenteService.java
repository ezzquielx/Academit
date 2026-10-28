package com.academit.education.service;

import com.academit.education.dto.docente.DocenteCreateDTO;
import com.academit.education.dto.docente.DocenteDTO;
import com.academit.education.dto.docente.DocenteUpdateDTO;

import java.util.List;

public interface DocenteService {

    List<DocenteDTO> listarDocentes();

    DocenteDTO obtenerPorId(Integer id);

    DocenteDTO crearDocente(DocenteCreateDTO request);

    DocenteDTO actualizarDocente(Integer id, DocenteUpdateDTO request);

    void eliminarDocente(Integer id);
}

