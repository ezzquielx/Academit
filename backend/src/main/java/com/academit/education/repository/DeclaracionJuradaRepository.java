package com.academit.education.repository;

import com.academit.education.model.DeclaracionJurada;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DeclaracionJuradaRepository extends JpaRepository<DeclaracionJurada, Integer> {

    boolean existsByAlumnoIdAndCicloId(Integer idAlumno, Integer idCiclo);

    List<DeclaracionJurada> findByAlumnoId(Integer idAlumno);

    List<DeclaracionJurada> findByCicloId(Integer idCiclo);

    List<DeclaracionJurada> findByAlumnoIdAndCicloId(Integer idAlumno, Integer idCiclo);
}
