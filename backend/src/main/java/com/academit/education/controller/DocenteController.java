package com.academit.education.controller;

import com.academit.education.dto.docente.DocenteCreateDTO;
import com.academit.education.dto.docente.DocenteDTO;
import com.academit.education.dto.docente.DocenteUpdateDTO;
import com.academit.education.service.DocenteService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Controlador REST para la gestión del cuerpo docente institucional.
 * Mapeo de rutas y permisos alineados con openapi.yaml y endpoints.csv.
 */
@RestController
@RequestMapping("/api/docentes")
public class DocenteController {

    private final DocenteService docenteService;

    public DocenteController(DocenteService docenteService) {
        this.docenteService = docenteService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<List<DocenteDTO>> listarDocentes() {
        return ResponseEntity.ok(docenteService.listarDocentes());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<DocenteDTO> obtenerPorId(@PathVariable Integer id) {
        return ResponseEntity.ok(docenteService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<DocenteDTO> crearDocente(@Valid @RequestBody DocenteCreateDTO request) {
        DocenteDTO creado = docenteService.crearDocente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<DocenteDTO> actualizarDocente(
            @PathVariable Integer id,
            @Valid @RequestBody DocenteUpdateDTO request) {
        DocenteDTO actualizado = docenteService.actualizarDocente(id, request);
        return ResponseEntity.ok(actualizado);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> eliminarDocente(@PathVariable Integer id) {
        docenteService.eliminarDocente(id);
        return ResponseEntity.noContent().build();
    }
}

