package com.academit.education.controller;

import com.academit.education.dto.alumno.AlumnoCreateDTO;
import com.academit.education.dto.alumno.AlumnoDTO;
import com.academit.education.dto.alumno.AlumnoUpdateDTO;
import com.academit.education.dto.alumno.SituacionAcademicaDTO;
import com.academit.education.dto.alumno.SituacionAdministrativaDTO;
import com.academit.education.security.CustomUserDetails;
import com.academit.education.service.AlumnoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de alumnos y consulta de situaciones académica y administrativa.
 * Rutas y permisos alineados con openapi.yaml y endpoints.csv.
 */
@RestController
@RequestMapping("/api/alumnos")
public class AlumnoController {

    private final AlumnoService alumnoService;

    public AlumnoController(AlumnoService alumnoService) {
        this.alumnoService = alumnoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<List<AlumnoDTO>> listarAlumnos() {
        return ResponseEntity.ok(alumnoService.listarAlumnos());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO', 'ALUMNO')")
    public ResponseEntity<AlumnoDTO> obtenerPorId(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(alumnoService.obtenerPorId(id, userDetails));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<AlumnoDTO> crearAlumno(@Valid @RequestBody AlumnoCreateDTO request) {
        AlumnoDTO creado = alumnoService.crearAlumno(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<AlumnoDTO> actualizarAlumno(
            @PathVariable Integer id,
            @Valid @RequestBody AlumnoUpdateDTO request) {
        AlumnoDTO actualizado = alumnoService.actualizarAlumno(id, request);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarAlumno(@PathVariable Integer id) {
        alumnoService.eliminarAlumno(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/situacion-academica")
    @PreAuthorize("hasAnyRole('ALUMNO', 'ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<SituacionAcademicaDTO> obtenerSituacionAcademica(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(alumnoService.obtenerSituacionAcademica(id, userDetails));
    }

    @GetMapping("/{id}/situacion-administrativa")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'ADMINISTRADOR', 'ALUMNO')")
    public ResponseEntity<SituacionAdministrativaDTO> obtenerSituacionAdministrativa(
            @PathVariable Integer id,
            @AuthenticationPrincipal CustomUserDetails userDetails) {
        return ResponseEntity.ok(alumnoService.obtenerSituacionAdministrativa(id, userDetails));
    }
}

