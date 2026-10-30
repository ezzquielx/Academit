package com.academit.education.controller;

import com.academit.education.dto.carrera.CarreraCreateDTO;
import com.academit.education.dto.carrera.CarreraDTO;
import com.academit.education.dto.carrera.CarreraUpdateDTO;
import com.academit.education.dto.curso.CursoDTO;
import com.academit.education.dto.planestudio.PlanEstudioDTO;
import com.academit.education.service.CarreraService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Gestión de carreras (RF53-RF55). Según el contrato:
 * - GET (listado, detalle, planes y cursos): ADMINISTRADOR y ADMINISTRATIVO
 * - POST, PUT y DELETE (baja lógica): solo ADMINISTRADOR
 */
@RestController
@RequestMapping("/api/carreras")
public class CarreraController {

    private final CarreraService carreraService;

    public CarreraController(CarreraService carreraService) {
        this.carreraService = carreraService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<List<CarreraDTO>> listarCarreras() {
        return ResponseEntity.ok(carreraService.listarCarreras());
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<CarreraDTO> obtenerCarrera(@PathVariable Integer id) {
        return ResponseEntity.ok(carreraService.obtenerPorId(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CarreraDTO> crearCarrera(@Valid @RequestBody CarreraCreateDTO request) {
        CarreraDTO creada = carreraService.crearCarrera(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CarreraDTO> actualizarCarrera(
            @PathVariable Integer id,
            @Valid @RequestBody CarreraUpdateDTO request) {
        return ResponseEntity.ok(carreraService.actualizarCarrera(id, request));
    }

    /** Baja lógica del contrato: 204 sin cuerpo (la carrera queda INACTIVA). */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<Void> darDeBajaCarrera(@PathVariable Integer id) {
        carreraService.darDeBajaCarrera(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}/planes")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<List<PlanEstudioDTO>> listarPlanesDeCarrera(@PathVariable Integer id) {
        return ResponseEntity.ok(carreraService.listarPlanesDeCarrera(id));
    }

    @GetMapping("/{id}/cursos")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<List<CursoDTO>> listarCursosDeCarrera(@PathVariable Integer id) {
        return ResponseEntity.ok(carreraService.listarCursosDeCarrera(id));
    }
}
