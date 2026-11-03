package com.academit.education.controller;

import com.academit.education.dto.materia.MateriaCreateDTO;
import com.academit.education.dto.materia.MateriaDTO;
import com.academit.education.dto.materia.MateriaUpdateDTO;
import com.academit.education.service.MateriaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Materias (RF47-RF48). Según el contrato:
 * - GET /api/materias: ADMINISTRADOR, DOCENTE y ALUMNO
 * - POST y PUT /api/materias: solo ADMINISTRADOR (sin DELETE en el contrato)
 */
@RestController
@RequestMapping("/api/materias")
public class MateriaController {

    private final MateriaService materiaService;

    public MateriaController(MateriaService materiaService) {
        this.materiaService = materiaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'DOCENTE', 'ALUMNO')")
    public ResponseEntity<List<MateriaDTO>> listarMaterias(
            @RequestParam(required = false) Integer idCarrera,
            @RequestParam(required = false) Integer idPlan) {
        return ResponseEntity.ok(materiaService.listarMaterias(idCarrera, idPlan));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MateriaDTO> crearMateria(@Valid @RequestBody MateriaCreateDTO request) {
        MateriaDTO creada = materiaService.crearMateria(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MateriaDTO> actualizarMateria(
            @PathVariable Integer id,
            @Valid @RequestBody MateriaUpdateDTO request) {
        return ResponseEntity.ok(materiaService.actualizarMateria(id, request));
    }
}
