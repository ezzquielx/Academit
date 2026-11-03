package com.academit.education.repository;

import com.academit.education.model.Usuario;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UsuarioRepository extends JpaRepository<Usuario, Integer> {

    @EntityGraph(attributePaths = {"rol", "alumno", "alumno.estado", "docente", "docente.estado"})
    Optional<Usuario> findByUsername(String username);

    boolean existsByUsername(String username);
}
