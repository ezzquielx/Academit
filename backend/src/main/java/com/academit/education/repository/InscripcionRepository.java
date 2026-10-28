package com.academit.education.repository;

import com.academit.education.model.Inscripcion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InscripcionRepository extends JpaRepository<Inscripcion, Integer> {

    /** RN01: ya inscripto en ese curso y ciclo. */
    boolean existsByAlumnoIdAndCursoIdAndCicloIdAndActivaTrue(Integer idAlumno, Integer idCurso, Integer idCiclo);

    /** RN02: una sola inscripcion activa por ciclo. */
    boolean existsByAlumnoIdAndCicloIdAndActivaTrue(Integer idAlumno, Integer idCiclo);

    /** RN03: cupo usado de un curso. */
    long countByCursoIdAndActivaTrue(Integer idCurso);

    List<Inscripcion> findByCursoIdAndActivaTrue(Integer idCurso);

    List<Inscripcion> findByAlumnoId(Integer idAlumno);

    List<Inscripcion> findByAlumnoIdAndActivaTrue(Integer idAlumno);

    List<Inscripcion> findByCursoId(Integer idCurso);

    List<Inscripcion> findByCursoIdIn(List<Integer> cursosIds);
    List<Inscripcion> findByAlumnoIdAndActivaTrue(Integer idAlumno);

    List<Inscripcion> findByAlumnoId(Integer idAlumno);
}
