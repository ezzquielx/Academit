package com.academit.education.service;

import com.academit.education.dto.materia.MateriaCreateDTO;
import com.academit.education.dto.materia.MateriaDTO;
import com.academit.education.dto.materia.MateriaUpdateDTO;

import java.util.List;

public interface MateriaService {

    /** GET /api/materias con filtros opcionales idCarrera e idPlan. */
    List<MateriaDTO> listarMaterias(Integer idCarrera, Integer idPlan);

    MateriaDTO crearMateria(MateriaCreateDTO request);

    MateriaDTO actualizarMateria(Integer idMateria, MateriaUpdateDTO request);
}
