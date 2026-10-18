package com.academit.education.service;

import com.academit.education.dto.auth.CambiarPasswordDTO;
import com.academit.education.dto.auth.LoginRequestDTO;
import com.academit.education.dto.auth.LoginResponseDTO;
import com.academit.education.dto.common.ApiResponseDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.UnauthorizedException;
import com.academit.education.model.Rol;
import com.academit.education.model.Usuario;
import com.academit.education.repository.UsuarioRepository;
import com.academit.education.security.JwtUtil;
import com.academit.education.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(usuarioRepository, passwordEncoder, jwtUtil);
    }

    @Test
    void testLoginSuccess() {
        Rol rol = new Rol();
        rol.setId(1);
        rol.setNombre("ADMINISTRADOR");

        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setUsername("admin");
        usuario.setPasswordHash("$2b$10$hashed");
        usuario.setActivo(true);
        usuario.setRol(rol);

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Academit123", "$2b$10$hashed")).thenReturn(true);
        when(jwtUtil.generateToken("admin", "ADMINISTRADOR", 1)).thenReturn("fake-jwt-token");

        LoginRequestDTO request = new LoginRequestDTO("admin", "Academit123");
        LoginResponseDTO response = authService.login(request);

        assertNotNull(response);
        assertEquals("admin", response.getUsuario());
        assertEquals("ADMINISTRADOR", response.getRol());
        assertEquals("fake-jwt-token", response.getToken());
    }

    @Test
    void testLoginInvalidPasswordThrowsBadCredentials() {
        Usuario usuario = new Usuario();
        usuario.setUsername("admin");
        usuario.setPasswordHash("$2b$10$hashed");
        usuario.setActivo(true);

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("wrongpass", "$2b$10$hashed")).thenReturn(false);

        LoginRequestDTO request = new LoginRequestDTO("admin", "wrongpass");
        assertThrows(BadCredentialsException.class, () -> authService.login(request));
    }

    @Test
    void testLoginInactiveUserThrowsUnauthorized() {
        Usuario usuario = new Usuario();
        usuario.setUsername("admin");
        usuario.setActivo(false);

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));

        LoginRequestDTO request = new LoginRequestDTO("admin", "Academit123");
        assertThrows(UnauthorizedException.class, () -> authService.login(request));
    }

    @Test
    void testLogoutReturnsSuccess() {
        ApiResponseDTO response = authService.logout();
        assertTrue(response.isSuccess());
    }

    @Test
    void testCambiarPasswordSuccess() {
        Usuario usuario = new Usuario();
        usuario.setUsername("admin");
        usuario.setPasswordHash("$2b$10$oldHash");

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Academit123", "$2b$10$oldHash")).thenReturn(true);
        when(passwordEncoder.encode("NewPassword123")).thenReturn("$2b$10$newHash");

        CambiarPasswordDTO request = new CambiarPasswordDTO("Academit123", "NewPassword123");
        ApiResponseDTO response = authService.cambiarPassword("admin", request);

        assertTrue(response.isSuccess());
        verify(usuarioRepository).save(usuario);
        assertEquals("$2b$10$newHash", usuario.getPasswordHash());
    }

    @Test
    void testCambiarPasswordWrongActualThrowsBusinessRuleException() {
        Usuario usuario = new Usuario();
        usuario.setUsername("admin");
        usuario.setPasswordHash("$2b$10$oldHash");

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("WrongActual", "$2b$10$oldHash")).thenReturn(false);

        CambiarPasswordDTO request = new CambiarPasswordDTO("WrongActual", "NewPassword123");
        assertThrows(BusinessRuleException.class, () -> authService.cambiarPassword("admin", request));
    }
}

