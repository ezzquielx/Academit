package com.academit.education.repository;

import com.academit.education.model.PlanMateria;
import com.academit.education.model.PlanMateriaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PlanMateriaRepository extends JpaRepository<PlanMateria, PlanMateriaId> {

    List<PlanMateria> findByPlanId(Integer idPlan);

    List<PlanMateria> findByMateriaId(Integer idMateria);

    /** GET /api/materias?idCarrera=...: filtra por la carrera del plan. */
    List<PlanMateria> findByPlanCarreraId(Integer idCarrera);

    boolean existsByIdPlanAndIdMateria(Integer idPlan, Integer idMateria);
}
