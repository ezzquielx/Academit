package com.academit.education.repository;

import com.academit.education.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer> {

    List<Asistencia> findByAlumnoIdAndCursoId(Integer idAlumno, Integer idCurso);

    List<Asistencia> findByAlumnoId(Integer idAlumno);

    boolean existsByAlumnoIdAndCursoIdAndFecha(Integer idAlumno, Integer idCurso, LocalDate fecha);

    long countByAlumnoIdAndCursoIdAndEstadoNombre(Integer idAlumno, Integer idCurso, String estado);
}
