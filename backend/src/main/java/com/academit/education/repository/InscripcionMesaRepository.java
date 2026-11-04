package com.academit.education.repository;

import com.academit.education.model.InscripcionMesa;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface InscripcionMesaRepository extends JpaRepository<InscripcionMesa, Integer> {

    long countByMesaIdAndActivaTrue(Integer idMesa);

    boolean existsByMesaIdAndAlumnoIdAndActivaTrue(Integer idMesa, Integer idAlumno);

    Optional<InscripcionMesa> findFirstByMesaIdAndAlumnoIdAndActivaTrue(Integer idMesa, Integer idAlumno);

    List<InscripcionMesa> findByMesaIdAndActivaTrue(Integer idMesa);

    List<InscripcionMesa> findByAlumnoId(Integer idAlumno);
}
