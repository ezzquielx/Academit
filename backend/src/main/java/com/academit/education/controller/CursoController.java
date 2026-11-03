package com.academit.education.controller;

import com.academit.education.dto.curso.CursoCreateDTO;
import com.academit.education.dto.curso.CursoDTO;
import com.academit.education.dto.curso.CursoUpdateDTO;
import com.academit.education.service.CursoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Cursos y comisiones (RF14-RF16, RN23). Según el contrato:
 * - GET /api/cursos: ADMINISTRADOR, DOCENTE y ADMINISTRATIVO
 * - POST y PUT: solo ADMINISTRADOR (sin DELETE en el contrato)
 */
@RestController
@RequestMapping("/api/cursos")
public class CursoController {

    private final CursoService cursoService;

    public CursoController(CursoService cursoService) {
        this.cursoService = cursoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DOCENTE', 'ADMINISTRATIVO')")
    public ResponseEntity<List<CursoDTO>> listarCursos() {
        return ResponseEntity.ok(cursoService.listarCursos());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CursoDTO> crearCurso(@Valid @RequestBody CursoCreateDTO request) {
        CursoDTO creado = cursoService.crearCurso(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CursoDTO> actualizarCurso(
            @PathVariable Integer id,
            @Valid @RequestBody CursoUpdateDTO request) {
        return ResponseEntity.ok(cursoService.actualizarCurso(id, request));
    }
}
