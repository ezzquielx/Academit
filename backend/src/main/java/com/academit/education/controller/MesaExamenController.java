package com.academit.education.controller;

import com.academit.education.dto.mesa.MesaExamenCreateDTO;
import com.academit.education.dto.mesa.MesaExamenDTO;
import com.academit.education.dto.mesa.MesaExamenUpdateDTO;
import com.academit.education.security.CustomUserDetails;
import com.academit.education.service.MesaExamenService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Mesas de examen final (RF48? segun contrato RF49-RF50, RN19). Según el
 * contrato:
 * - GET: ALUMNO, DOCENTE, ADMINISTRATIVO y ADMINISTRADOR
 * - POST y PUT: ADMINISTRADOR
 * - POST /{id}/inscripciones y DELETE /{id}/inscripciones/{idInscripcion}: ALUMNO
 */
@RestController
@RequestMapping("/api/mesas-examen")
public class MesaExamenController {

    private final MesaExamenService mesaExamenService;

    public MesaExamenController(MesaExamenService mesaExamenService) {
        this.mesaExamenService = mesaExamenService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ALUMNO', 'DOCENTE', 'ADMINISTRATIVO', 'ADMINISTRADOR')")
    public ResponseEntity<List<MesaExamenDTO>> listarMesas() {
        return ResponseEntity.ok(mesaExamenService.listar());
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MesaExamenDTO> crearMesa(@Valid @RequestBody MesaExamenCreateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(mesaExamenService.crear(request));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<MesaExamenDTO> actualizarMesa(
            @PathVariable Integer id,
            @Valid @RequestBody MesaExamenUpdateDTO request) {
        return ResponseEntity.ok(mesaExamenService.actualizar(id, request));
    }

    /** Inscripción del alumno del token (RF49). */
    @PostMapping("/{id}/inscripciones")
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<MesaExamenDTO> inscribirse(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id) {
        return ResponseEntity.ok(mesaExamenService.inscribir(userDetails, id));
    }

    /** Baja lógica de la inscripción (RF50); el alumno solo anula la suya. */
    @DeleteMapping("/{id}/inscripciones/{idInscripcion}")
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<MesaExamenDTO> darseDeBaja(
            @AuthenticationPrincipal CustomUserDetails userDetails,
            @PathVariable Integer id,
            @PathVariable Integer idInscripcion) {
        return ResponseEntity.ok(
                mesaExamenService.darDeBaja(userDetails, id, idInscripcion));
    }
}
