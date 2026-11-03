package com.academit.education.repository;

import com.academit.education.model.Asistencia;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AsistenciaRepository extends JpaRepository<Asistencia, Integer> {

    List<Asistencia> findByAlumnoIdAndCursoId(Integer idAlumno, Integer idCurso);

    List<Asistencia> findByAlumnoId(Integer idAlumno);

    List<Asistencia> findByCursoId(Integer idCurso);

    List<Asistencia> findByFecha(LocalDate fecha);

    /** RN15: la asistencia vive por alumno, curso, materia y fecha (upsert). */
    Optional<Asistencia> findByAlumnoIdAndCursoIdAndMateriaIdAndFecha(
            Integer idAlumno, Integer idCurso, Integer idMateria, LocalDate fecha);

    List<Asistencia> findByAlumnoIdAndCursoIdAndMateriaId(Integer idAlumno, Integer idCurso, Integer idMateria);

    long countByAlumnoIdAndCursoIdAndMateriaIdAndEstadoNombre(
            Integer idAlumno, Integer idCurso, Integer idMateria, String estado);

    boolean existsByAlumnoIdAndCursoIdAndMateriaIdAndFecha(
            Integer idAlumno, Integer idCurso, Integer idMateria, LocalDate fecha);

    boolean existsByAlumnoIdAndCursoIdAndFecha(Integer idAlumno, Integer idCurso, LocalDate fecha);

    long countByAlumnoIdAndCursoIdAndEstadoNombre(Integer idAlumno, Integer idCurso, String estado);
}
