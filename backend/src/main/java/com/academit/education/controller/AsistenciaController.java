package com.academit.education.controller;

import com.academit.education.dto.asistencia.AsistenciaDTO;
import com.academit.education.dto.asistencia.AsistenciasLoteDTO;
import com.academit.education.dto.asistencia.SuccessResponseDTO;
import com.academit.education.security.CustomUserDetails;
import com.academit.education.service.AsistenciaService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Asistencias (RF41, RN12, RN15). Según el contrato:
 * - GET: DOCENTE (cursos de sus cátedras), ALUMNO (solo las suyas) y
 *   ADMINISTRADOR
 * - POST /lote: DOCENTE (solo cátedras asignadas, lote atómico RN15)
 */
@RestController
@RequestMapping("/api/asistencias")
public class AsistenciaController {

    private final AsistenciaService asistenciaService;

    public AsistenciaController(AsistenciaService asistenciaService) {
        this.asistenciaService = asistenciaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCENTE', 'ALUMNO', 'ADMINISTRADOR')")
    public ResponseEntity<List<AsistenciaDTO>> listarAsistencias(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer idAlumno,
            @RequestParam(required = false) Integer idCurso,
            @RequestParam(required = false) String fecha) {
        return ResponseEntity.ok(
                asistenciaService.listar(userDetails, idAlumno, idCurso, fecha));
    }

    @PostMapping("/lote")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<SuccessResponseDTO> registrarAsistencias(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody AsistenciasLoteDTO request) {
        return ResponseEntity.ok(asistenciaService.registrarLote(userDetails, request));
    }
}
