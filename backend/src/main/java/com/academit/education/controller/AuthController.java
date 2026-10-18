package com.academit.education.controller;

import com.academit.education.dto.auth.CambiarPasswordDTO;
import com.academit.education.dto.auth.LoginRequestDTO;
import com.academit.education.dto.auth.LoginResponseDTO;
import com.academit.education.dto.common.ApiResponseDTO;
import com.academit.education.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponseDTO> logout() {
        ApiResponseDTO response = authService.logout();
        return ResponseEntity.ok(response);
    }

    @PostMapping("/cambiar-password")
    public ResponseEntity<ApiResponseDTO> cambiarPassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody CambiarPasswordDTO request) {
        String username = userDetails != null ? userDetails.getUsername() : "";
        ApiResponseDTO response = authService.cambiarPassword(username, request);
        return ResponseEntity.ok(response);
    }
}

