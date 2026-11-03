package com.academit.education.service;

import com.academit.education.dto.materia.MateriaDTO;
import com.academit.education.dto.planestudio.PlanEstudioDTO;

import java.util.List;

public interface PlanEstudioService {

    /** GET /api/planes-estudio: todos los planes del instituto. */
    List<PlanEstudioDTO> listarPlanes();

    /** GET /api/planes-estudio/{id}/materias: malla del plan con año y correlativas. */
    List<MateriaDTO> listarMateriasDePlan(Integer idPlan);
}
