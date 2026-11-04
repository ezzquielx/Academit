package com.academit.education.repository;

import com.academit.education.model.Docente;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DocenteRepository extends JpaRepository<Docente, Integer> {

    boolean existsByDni(String dni);

    boolean existsByEmail(String email);

    boolean existsByDniAndIdNot(String dni, Integer id);

    boolean existsByEmailAndIdNot(String email, Integer id);

    Optional<Docente> findByDni(String dni);

    Optional<Docente> findByEmail(String email);

    Page<Docente> findByDniContainingOrApellidoContainingIgnoreCaseOrNombreContainingIgnoreCase(
            String dni, String apellido, String nombre, Pageable pageable);
}
