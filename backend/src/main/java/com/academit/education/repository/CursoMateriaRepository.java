package com.academit.education.repository;

import com.academit.education.model.CursoMateria;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CursoMateriaRepository extends JpaRepository<CursoMateria, Integer> {

    List<CursoMateria> findByCursoId(Integer idCurso);

    List<CursoMateria> findByDocenteId(Integer idDocente);

    List<CursoMateria> findByCursoIdIn(List<Integer> idCursos);

    List<CursoMateria> findByMateriaId(Integer idMateria);

    List<CursoMateria> findByCursoIdAndMateriaId(Integer idCurso, Integer idMateria);

    boolean existsByCursoIdAndMateriaId(Integer idCurso, Integer idMateria);

    boolean existsByCursoIdAndMateriaIdAndDocenteId(Integer idCurso, Integer idMateria, Integer idDocente);
}
