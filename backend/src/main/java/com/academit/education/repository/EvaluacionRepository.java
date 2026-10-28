package com.academit.education.repository;

import com.academit.education.model.Evaluacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EvaluacionRepository extends JpaRepository<Evaluacion, Integer> {

    List<Evaluacion> findByCursoMateriaId(Integer idCursoMateria);

    boolean existsByCursoMateriaId(Integer idCursoMateria);
}
