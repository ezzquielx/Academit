package com.academit.education.controller;

import com.academit.education.dto.cuota.CuotaDTO;
import com.academit.education.dto.common.ApiResponseDTO;
import com.academit.education.dto.matricula.MatriculaCancelDTO;
import com.academit.education.dto.matricula.MatriculaDTO;
import com.academit.education.dto.matricula.MatriculaUpdateDTO;
import com.academit.education.service.MatriculaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Matriculas anuales (RF25-RF27, RN05, RN18).
 * Permisos tomados de x-roles en docs/frontend/openapi.yaml.
 */
@RestController
@RequestMapping("/api/matriculas")
public class MatriculaController {

    private final MatriculaService matriculaService;

    public MatriculaController(MatriculaService matriculaService) {
        this.matriculaService = matriculaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<List<MatriculaDTO>> listarMatriculas() {
        return ResponseEntity.ok(matriculaService.listarMatriculas());
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'ADMINISTRADOR')")
    public ResponseEntity<MatriculaDTO> actualizarMatricula(
            @PathVariable Integer id,
            @Valid @RequestBody MatriculaUpdateDTO request) {
        return ResponseEntity.ok(matriculaService.actualizarMatricula(id, request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ALUMNO', 'ADMINISTRATIVO', 'ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDTO> cancelarMatricula(
            @PathVariable Integer id,
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody MatriculaCancelDTO request) {
        return ResponseEntity.ok(matriculaService.cancelarMatricula(id, userDetails.getUsername(), request));
    }

    @PostMapping("/{id}/cuotas")
    @PreAuthorize("hasAnyRole('ADMINISTRATIVO', 'ADMINISTRADOR')")
    public ResponseEntity<List<CuotaDTO>> generarCuotas(@PathVariable Integer id) {
        // El contrato define 200 para esta operacion (no 201).
        return ResponseEntity.ok(matriculaService.generarCuotas(id));
    }
}
