package com.academit.education.repository;

import com.academit.education.model.Alumno;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AlumnoRepository extends JpaRepository<Alumno, Integer> {

    boolean existsByDni(String dni);

    boolean existsByEmail(String email);

    boolean existsByDniAndIdNot(String dni, Integer id);

    boolean existsByEmailAndIdNot(String email, Integer id);

    Optional<Alumno> findByDni(String dni);

    Optional<Alumno> findByEmail(String email);

    /** Busqueda por DNI, apellido o nombre (RF08). */
    Page<Alumno> findByDniContainingOrApellidoContainingIgnoreCaseOrNombreContainingIgnoreCase(
            String dni, String apellido, String nombre, Pageable pageable);
}
