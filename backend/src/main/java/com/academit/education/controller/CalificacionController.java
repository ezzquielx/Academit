package com.academit.education.controller;

import com.academit.education.dto.calificacion.CalificacionDTO;
import com.academit.education.dto.calificacion.CalificacionSaveDTO;
import com.academit.education.security.CustomUserDetails;
import com.academit.education.service.CalificacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Calificaciones (RF40). Según el contrato:
 * - GET: ALUMNO (solo las suyas), DOCENTE (sus cátedras) y ADMINISTRADOR
 * - POST: DOCENTE (solo cátedras asignadas, RN11) y ADMINISTRADOR
 */
@RestController
@RequestMapping("/api/calificaciones")
public class CalificacionController {

    private final CalificacionService calificacionService;

    public CalificacionController(CalificacionService calificacionService) {
        this.calificacionService = calificacionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ALUMNO', 'DOCENTE', 'ADMINISTRADOR')")
    public ResponseEntity<List<CalificacionDTO>> listarCalificaciones(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer idAlumno,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer idMateria,
            @RequestParam(required = false) Integer idEvaluacion) {
        return ResponseEntity.ok(
                calificacionService.listar(userDetails, idAlumno, tipo, idMateria, idEvaluacion));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('DOCENTE', 'ADMINISTRADOR')")
    public ResponseEntity<CalificacionDTO> guardarCalificacion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody CalificacionSaveDTO request) {
        return ResponseEntity.ok(calificacionService.guardar(userDetails, request));
    }
}
