package com.academit.education.controller;

import com.academit.education.dto.inscripcion.InscripcionCreateDTO;
import com.academit.education.dto.inscripcion.InscripcionDTO;
import com.academit.education.security.CustomUserDetails;
import com.academit.education.service.InscripcionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para el registro y cancelación de inscripciones a comisiones y cursos.
 * Rutas y permisos alineados con openapi.yaml y endpoints.csv.
 */
@RestController
@RequestMapping("/api/inscripciones")
public class InscripcionController {

    private final InscripcionService inscripcionService;

    public InscripcionController(InscripcionService inscripcionService) {
        this.inscripcionService = inscripcionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO', 'ALUMNO', 'DOCENTE')")
    public ResponseEntity<List<InscripcionDTO>> listarInscripciones(
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(inscripcionService.listarInscripciones(userDetails));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO', 'ALUMNO', 'DOCENTE')")
    public ResponseEntity<InscripcionDTO> obtenerPorId(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(inscripcionService.obtenerPorId(id, userDetails));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<InscripcionDTO> crearInscripcion(@Valid @RequestBody InscripcionCreateDTO request) {
        InscripcionDTO creada = inscripcionService.crearInscripcion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<Void> cancelarInscripcion(@PathVariable Integer id) {
        inscripcionService.cancelarInscripcion(id);
        return ResponseEntity.noContent().build();
    }
}

