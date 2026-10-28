package com.academit.education.controller;

import com.academit.education.dto.pago.PagoCreateDTO;
import com.academit.education.dto.pago.PagoDTO;
import com.academit.education.service.PagoService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Pagos y comprobantes (RF32, RF33, RN08, RN09, RN10).
 * Permisos tomados de x-roles en docs/frontend/openapi.yaml.
 */
@RestController
@RequestMapping("/api/pagos")
public class PagoController {

    private final PagoService pagoService;

    public PagoController(PagoService pagoService) {
        this.pagoService = pagoService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO', 'ALUMNO')")
    public ResponseEntity<List<PagoDTO>> listarPagos(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer idAlumno) {
        return ResponseEntity.ok(pagoService.listarPagos(userDetails.getUsername(), idAlumno));
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<PagoDTO> registrarPago(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody PagoCreateDTO request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(pagoService.registrarPago(userDetails.getUsername(), request));
    }
}
