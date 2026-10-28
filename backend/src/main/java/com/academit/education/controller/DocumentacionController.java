package com.academit.education.controller;

import com.academit.education.dto.documentacion.DocumentacionDTO;
import com.academit.education.dto.documentacion.DocumentacionUpdateDTO;
import com.academit.education.service.DocumentacionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Documentación y legajos (RF56-RF58, RN17).
 * Permisos tomados de x-roles en docs/frontend/openapi.yaml.
 * El contrato no tiene POST ni DELETE: solo consulta y PUT.
 */
@RestController
@RequestMapping("/api/documentacion")
public class DocumentacionController {

    private final DocumentacionService documentacionService;

    public DocumentacionController(DocumentacionService documentacionService) {
        this.documentacionService = documentacionService;
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO', 'ALUMNO')")
    public ResponseEntity<List<DocumentacionDTO>> listarDocumentacion(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestParam(required = false) Integer idAlumno) {
        return ResponseEntity.ok(documentacionService.listarDocumentacion(userDetails.getUsername(), idAlumno));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<DocumentacionDTO> actualizarDocumentacion(
            @PathVariable Integer id,
            @Valid @RequestBody DocumentacionUpdateDTO request) {
        return ResponseEntity.ok(documentacionService.actualizarDocumentacion(id, request));
    }
}
