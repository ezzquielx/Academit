package com.academit.education.controller;

import com.academit.education.dto.cuota.CuotaDTO;
import com.academit.education.service.CuotaService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Cuotas y aranceles (RF28-RF31, RN06, RN07).
 * Permisos tomados de x-roles en docs/frontend/openapi.yaml.
 */
@RestController
@RequestMapping("/api/cuotas")
public class CuotaController {

    private final CuotaService cuotaService;

    public CuotaController(CuotaService cuotaService) {
        this.cuotaService = cuotaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO', 'ALUMNO')")
    public ResponseEntity<List<CuotaDTO>> listarCuotas(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer idAlumno) {
        return ResponseEntity.ok(cuotaService.listarCuotas(userDetails.getUsername(), idAlumno));
    }

    @PatchMapping("/{id}/anular")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<CuotaDTO> anularCuota(@PathVariable Integer id) {
        return ResponseEntity.ok(cuotaService.anularCuota(id));
    }
}
