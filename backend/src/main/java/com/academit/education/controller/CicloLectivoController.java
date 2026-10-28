package com.academit.education.controller;

import com.academit.education.dto.ciclo.CicloLectivoCreateDTO;
import com.academit.education.dto.ciclo.CicloLectivoDTO;
import com.academit.education.dto.ciclo.CicloLectivoUpdateDTO;
import com.academit.education.service.CicloLectivoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Catálogo de ciclos lectivos. Según el contrato:
 * - GET  consulta con ADMINISTRADOR y ADMINISTRATIVO
 * - POST y PUT solo ADMINISTRADOR
 */
@RestController
@RequestMapping("/api/ciclos-lectivos")
public class CicloLectivoController {

    private final CicloLectivoService cicloLectivoService;

    public CicloLectivoController(CicloLectivoService cicloLectivoService) {
        this.cicloLectivoService = cicloLectivoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<List<CicloLectivoDTO>> listarCiclos() {
        return ResponseEntity.ok(cicloLectivoService.listarCiclos());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CicloLectivoDTO> crearCiclo(@Valid @RequestBody CicloLectivoCreateDTO request) {
        CicloLectivoDTO creado = cicloLectivoService.crearCiclo(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creado);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CicloLectivoDTO> actualizarCiclo(
            @PathVariable Integer id,
            @Valid @RequestBody CicloLectivoUpdateDTO request) {
        return ResponseEntity.ok(cicloLectivoService.actualizarCiclo(id, request));
    }
}
