package com.academit.education.controller;

import com.academit.education.dto.auth.AuthMeResponseDTO;
import com.academit.education.dto.auth.CambiarPasswordDTO;
import com.academit.education.dto.auth.LoginRequestDTO;
import com.academit.education.dto.auth.LoginResponseDTO;
import com.academit.education.dto.auth.RegistroAlumnoDTO;
import com.academit.education.dto.carrera.CarreraDTO;
import com.academit.education.dto.common.ApiResponseDTO;
import com.academit.education.dto.planestudio.PlanEstudioDTO;
import com.academit.education.service.AuthService;
import com.academit.education.service.CarreraService;
import com.academit.education.service.PlanEstudioService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final CarreraService carreraService;
    private final PlanEstudioService planEstudioService;

    public AuthController(AuthService authService,
                          CarreraService carreraService,
                          PlanEstudioService planEstudioService) {
        this.authService = authService;
        this.carreraService = carreraService;
        this.planEstudioService = planEstudioService;
    }

    @PostMapping("/login")
    public ResponseEntity<LoginResponseDTO> login(@Valid @RequestBody LoginRequestDTO request) {
        LoginResponseDTO response = authService.login(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/registro")
    public ResponseEntity<LoginResponseDTO> registrar(@Valid @RequestBody RegistroAlumnoDTO request) {
        LoginResponseDTO response = authService.registrarAlumno(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/carreras")
    public ResponseEntity<List<CarreraDTO>> listarCarrerasPublicas() {
        return ResponseEntity.ok(carreraService.listarCarreras());
    }

    @GetMapping("/planes-estudio")
    public ResponseEntity<List<PlanEstudioDTO>> listarPlanesPublicos() {
        return ResponseEntity.ok(planEstudioService.listarPlanes());
    }

    @GetMapping("/me")
    public ResponseEntity<AuthMeResponseDTO> me(@AuthenticationPrincipal UserDetails userDetails) {
        String username = userDetails != null ? userDetails.getUsername() : "";
        AuthMeResponseDTO response = authService.getMe(username);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout() {
        authService.logout();
        return ResponseEntity.noContent().build();
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

