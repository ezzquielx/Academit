package com.academit.education.repository;

import com.academit.education.model.Calificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CalificacionRepository extends JpaRepository<Calificacion, Integer> {

    List<Calificacion> findByAlumnoId(Integer idAlumno);

    List<Calificacion> findByEvaluacionId(Integer idEvaluacion);

    List<Calificacion> findByEvaluacionIdIn(List<Integer> idEvaluacion);

    Calificacion findByEvaluacionIdAndAlumnoId(Integer idEvaluacion, Integer idAlumno);

    boolean existsByEvaluacionIdAndAlumnoId(Integer idEvaluacion, Integer idAlumno);
}
