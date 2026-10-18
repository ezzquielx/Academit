package com.academit.education.controller;

import com.academit.education.dto.usuario.PerfilUsuarioDTO;
import com.academit.education.service.UsuarioService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/usuarios/me")
public class PerfilController {

    private final UsuarioService usuarioService;

    public PerfilController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    public ResponseEntity<PerfilUsuarioDTO> getMiPerfil(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails.getUsername();
        PerfilUsuarioDTO perfil = usuarioService.getPerfil(username);
        return ResponseEntity.ok(perfil);
    }

    @PutMapping
    public ResponseEntity<PerfilUsuarioDTO> actualizarMiPerfil(
            @AuthenticationPrincipal UserDetails userDetails,
            @RequestBody PerfilUsuarioDTO request) {
        String username = userDetails.getUsername();
        PerfilUsuarioDTO perfilActualizado = usuarioService.actualizarPerfil(username, request);
        return ResponseEntity.ok(perfilActualizado);
    }
}

