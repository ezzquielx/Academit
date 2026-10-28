package com.academit.education.repository;

import com.academit.education.model.Curso;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CursoRepository extends JpaRepository<Curso, Integer> {

    List<Curso> findByCicloId(Integer idCiclo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Curso c where c.id = :id")
    Optional<Curso> findByIdForUpdate(@Param("id") Integer id);
}
