package com.academit.education.service;

import com.academit.education.dto.asignacion.AsignacionCreateDTO;
import com.academit.education.dto.asignacion.AsignacionDTO;

import java.util.List;

public interface AsignacionService {

    List<AsignacionDTO> listarAsignaciones();

    AsignacionDTO obtenerPorId(Integer id);

    AsignacionDTO crearAsignacion(AsignacionCreateDTO request);

    void eliminarAsignacion(Integer id);
}

