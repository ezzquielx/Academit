package com.academit.education.controller;

import com.academit.education.dto.asignacion.AsignacionCreateDTO;
import com.academit.education.dto.asignacion.AsignacionDTO;
import com.academit.education.service.AsignacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión de asignaciones de docentes a cursos y materias.
 * Rutas y permisos alineados con openapi.yaml y endpoints.csv.
 */
@RestController
@RequestMapping("/api/asignaciones")
public class AsignacionController {

    private final AsignacionService asignacionService;

    public AsignacionController(AsignacionService asignacionService) {
        this.asignacionService = asignacionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<List<AsignacionDTO>> listarAsignaciones() {
        return ResponseEntity.ok(asignacionService.listarAsignaciones());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<AsignacionDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(asignacionService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<AsignacionDTO> crearAsignacion(@Valid @RequestBody AsignacionCreateDTO request) {
        AsignacionDTO creada = asignacionService.crearAsignacion(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarAsignacion(@PathVariable Integer id) {
        asignacionService.eliminarAsignacion(id);
        return ResponseEntity.noContent().build();
    }
}

