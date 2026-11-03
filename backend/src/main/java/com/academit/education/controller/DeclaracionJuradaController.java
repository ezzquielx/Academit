package com.academit.education.controller;

import com.academit.education.dto.declaracion.DeclaracionJuradaCreateDTO;
import com.academit.education.dto.declaracion.DeclaracionJuradaDTO;
import com.academit.education.security.CustomUserDetails;
import com.academit.education.service.DeclaracionJuradaService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

/**
 * Declaraciones juradas de materias a cursar (RF51, RN20). Según el
 * contrato:
 * - GET: ALUMNO (solo las suyas), ADMINISTRADOR y ADMINISTRATIVO
 * - POST: ALUMNO
 */
@RestController
@RequestMapping("/api/declaraciones-juradas")
public class DeclaracionJuradaController {

    private final DeclaracionJuradaService declaracionJuradaService;

    public DeclaracionJuradaController(DeclaracionJuradaService declaracionJuradaService) {
        this.declaracionJuradaService = declaracionJuradaService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ALUMNO', 'ADMINISTRATIVO', 'ADMINISTRADOR')")
    public ResponseEntity<List<DeclaracionJuradaDTO>> listarDeclaraciones(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @RequestParam(required = false) Integer idAlumno,
            @RequestParam(required = false) Integer idCiclo) {
        return ResponseEntity.ok(
                declaracionJuradaService.listar(userDetails, idAlumno, idCiclo));
    }

    @PostMapping
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<DeclaracionJuradaDTO> presentarDeclaracion(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @Valid @RequestBody DeclaracionJuradaCreateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(declaracionJuradaService.presentar(userDetails, request));
    }
}
