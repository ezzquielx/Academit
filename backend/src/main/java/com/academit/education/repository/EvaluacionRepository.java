package com.academit.education.repository;

import com.academit.education.model.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Integer> {

    List<Evaluacion> findByCursoMateriaId(Integer idCursoMateria);

    /** Evaluaciones de varias cátedras (listado del docente o de la materia). */
    List<Evaluacion> findByCursoMateriaIdIn(List<Integer> idCursoMateria);

    /** Evaluaciones por materia (los cursos de esa materia) para POST /api/calificaciones. */
    List<Evaluacion> findByCursoMateriaMateriaId(Integer idMateria);

    boolean existsByCursoMateriaId(Integer idCursoMateria);
}
