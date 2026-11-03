package com.academit.education.controller;

import com.academit.education.dto.solicitud.SolicitudCreateDTO;
import com.academit.education.dto.solicitud.SolicitudDTO;
import com.academit.education.dto.solicitud.SolicitudResolverDTO;
import com.academit.education.service.SolicitudService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Solicitudes y trámites (RF53-RF55, RN17).
 * Permisos tomados de x-roles en docs/frontend/openapi.yaml.
 * La declaración jurada no es un tipo de solicitud.
 */
@RestController
@RequestMapping("/api/solicitudes")
public class SolicitudController {

    private final SolicitudService solicitudService;

    public SolicitudController(SolicitudService solicitudService) {
        this.solicitudService = solicitudService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO', 'ALUMNO')")
    public ResponseEntity<List<SolicitudDTO>> listarSolicitudes(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer idAlumno,
            @RequestParam(required = false) String tipo,
            @RequestParam(required = false) String estado) {
        return ResponseEntity.ok(
                solicitudService.listarSolicitudes(userDetails.getUsername(), idAlumno, tipo, estado));
    }

    @PostMapping
    @PreAuthorize("hasRole('ALUMNO')")
    public ResponseEntity<SolicitudDTO> crearSolicitud(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody SolicitudCreateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(solicitudService.crearSolicitud(userDetails.getUsername(), request));
    }

    @PatchMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<SolicitudDTO> resolverSolicitud(
            @PathVariable Integer id,
            @Valid @RequestBody SolicitudResolverDTO request) {
        return ResponseEntity.ok(solicitudService.resolverSolicitud(id, request));
    }
}
