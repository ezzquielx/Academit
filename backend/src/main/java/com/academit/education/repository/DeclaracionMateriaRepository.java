package com.academit.education.repository;

import com.academit.education.model.DeclaracionMateria;
import com.academit.education.model.DeclaracionMateriaId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeclaracionMateriaRepository extends JpaRepository<DeclaracionMateria, DeclaracionMateriaId> {

    /** Materias declaradas de una declaración (id es embebido). */
    List<DeclaracionMateria> findByDeclaracionId(Integer idDeclaracion);
}
