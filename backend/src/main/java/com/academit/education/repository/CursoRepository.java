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

    List<Curso> findByCarreraId(Integer idCarrera);

    /** RN23/uk: nombre único por (nombre, ciclo, carrera). */
    boolean existsByNombreAndCicloIdAndCarreraId(String nombre, Integer idCiclo, Integer idCarrera);

    boolean existsByNombreAndCicloIdAndCarreraIdAndIdNot(
            String nombre, Integer idCiclo, Integer idCarrera, Integer id);

    boolean existsByCodigoAndIdNot(String codigo, Integer id);

    boolean existsByCodigo(String codigo);

    /** RN/baja de carrera: tiene cursos en ciclos ACTIVOS. */
    boolean existsByCarreraIdAndCicloEstado(Integer idCarrera, String estadoCiclo);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select c from Curso c where c.id = :id")
    Optional<Curso> findByIdForUpdate(@Param("id") Integer id);
}
