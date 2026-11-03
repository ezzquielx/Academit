package com.academit.education.controller;

import com.academit.education.dto.calificacion.CalificacionesLoteDTO;
import com.academit.education.dto.calificacion.CalificacionDTO;
import com.academit.education.dto.calificacion.EvaluacionCreateDTO;
import com.academit.education.dto.calificacion.EvaluacionDTO;
import com.academit.education.security.CustomUserDetails;
import com.academit.education.service.CalificacionService;
import com.academit.education.service.EvaluacionService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Evaluaciones y calificaciones (RF36, RF38-RF40, RN11, RN14, RN21). Según
 * el contrato:
 * - GET /api/evaluaciones: DOCENTE y ADMINISTRADOR
 * - POST /api/evaluaciones y POST /api/evaluaciones/{id}/calificaciones: DOCENTE
 */
@RestController
@RequestMapping("/api/evaluaciones")
public class EvaluacionController {

    private final EvaluacionService evaluacionService;
    private final CalificacionService calificacionService;

    public EvaluacionController(EvaluacionService evaluacionService,
            CalificacionService calificacionService) {
        this.evaluacionService = evaluacionService;
        this.calificacionService = calificacionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('DOCENTE', 'ADMINISTRADOR')")
    public ResponseEntity<List<EvaluacionDTO>> listarEvaluaciones(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) Integer idCurso) {
        return ResponseEntity.ok(evaluacionService.listar(userDetails, tipo, idCurso));
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<EvaluacionDTO> crearEvaluacion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody EvaluacionCreateDTO request) {
        EvaluacionDTO creada = evaluacionService.crear(userDetails, request);
        return ResponseEntity.status(HttpStatus.CREATED).body(creada);
    }

    /** Carga de notas en lote, atómica: si un item falla, no se guarda nada. */
    @PostMapping("/{idEvaluacion}/calificaciones")
    @PreAuthorize("hasRole('DOCENTE')")
    public ResponseEntity<List<CalificacionDTO>> guardarCalificaciones(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer idEvaluacion,
            @Valid @RequestBody CalificacionesLoteDTO request) {
        return ResponseEntity.ok(
                calificacionService.guardarLote(userDetails, idEvaluacion, request));
    }
}
