package com.academit.education.controller;

import com.academit.education.dto.materia.MateriaDTO;
import com.academit.education.dto.planestudio.PlanEstudioDTO;
import com.academit.education.service.PlanEstudioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Planes de estudio (RF47). Según el contrato:
 * - GET /api/planes-estudio: ADMINISTRADOR y ADMINISTRATIVO
 * - GET /api/planes-estudio/{id}/materias: todos los roles con sesión
 */
@RestController
@RequestMapping("/api/planes-estudio")
public class PlanEstudioController {

    private final PlanEstudioService planEstudioService;

    public PlanEstudioController(PlanEstudioService planEstudioService) {
        this.planEstudioService = planEstudioService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<List<PlanEstudioDTO>> listarPlanes() {
        return ResponseEntity.ok(planEstudioService.listarPlanes());
    }

    @GetMapping("/{id}/materias")
    @PreAuthorize("hasAnyRole('ALUMNO', 'DOCENTE', 'ADMINISTRATIVO', 'ADMINISTRADOR')")
    public ResponseEntity<List<MateriaDTO>> listarMateriasDePlan(@PathVariable Integer id) {
        return ResponseEntity.ok(planEstudioService.listarMateriasDePlan(id));
    }
}
