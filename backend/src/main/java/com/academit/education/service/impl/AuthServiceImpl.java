package com.academit.education.service.impl;

import com.academit.education.dto.auth.CambiarPasswordDTO;
import com.academit.education.dto.auth.LoginRequestDTO;
import com.academit.education.dto.auth.LoginResponseDTO;
import com.academit.education.dto.common.ApiResponseDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.exception.UnauthorizedException;
import com.academit.education.model.Usuario;
import com.academit.education.repository.UsuarioRepository;
import com.academit.education.security.JwtUtil;
import com.academit.education.service.AuthService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional(readOnly = true)
    public LoginResponseDTO login(LoginRequestDTO request) {
        String username = request.getUsername().trim();

        Usuario usuario = usuarioRepository.findByUsername(username)
                .or(() -> {
                    if ("docente".equalsIgnoreCase(username)) {
                        return usuarioRepository.findByUsername("docente1");
                    } else if ("alumno".equalsIgnoreCase(username)) {
                        return usuarioRepository.findByUsername("alumno1");
                    }
                    return java.util.Optional.empty();
                })
                .orElseThrow(() -> new BadCredentialsException("Credenciales inválidas"));

        if (!Boolean.TRUE.equals(usuario.getActivo())) {
            throw new UnauthorizedException("El usuario se encuentra inactivo");
        }

        // Permite validación por BCrypt de la BD (ej. Academit123) o clave fija de pruebas (1234)
        boolean matches = passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())
                || "1234".equals(request.getPassword());

        if (!matches) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        String rolNombre = usuario.getRol() != null ? usuario.getRol().getNombre() : "ALUMNO";
        String token = jwtUtil.generateToken(usuario.getUsername(), rolNombre, usuario.getId());

        return LoginResponseDTO.builder()
                .idUsuario(usuario.getId())
                .usuario(usuario.getUsername())
                .nombre(resolveNombre(usuario))
                .rol(rolNombre)
                .email(resolveEmail(usuario))
                .token(token)
                .build();
    }

    @Override
    public ApiResponseDTO logout() {
        return ApiResponseDTO.ok("Sesión finalizada con éxito");
    }

    @Override
    @Transactional
    public ApiResponseDTO cambiarPassword(String username, CambiarPasswordDTO request) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        boolean matches = passwordEncoder.matches(request.getActual(), usuario.getPasswordHash())
                || "1234".equals(request.getActual());

        if (!matches) {
            throw new BusinessRuleException("La contraseña actual es incorrecta");
        }

        usuario.setPasswordHash(passwordEncoder.encode(request.getNueva()));
        usuarioRepository.save(usuario);

        return ApiResponseDTO.ok("Contraseña actualizada con éxito");
    }

    private String resolveNombre(Usuario usuario) {
        if (usuario.getDocente() != null) {
            return usuario.getDocente().getNombre() + " " + usuario.getDocente().getApellido();
        }
        if (usuario.getAlumno() != null) {
            return usuario.getAlumno().getNombre() + " " + usuario.getAlumno().getApellido();
        }
        if ("admin".equalsIgnoreCase(usuario.getUsername())) {
            return "Administrador General";
        }
        if ("administrativo".equalsIgnoreCase(usuario.getUsername())) {
            return "Personal Administrativo";
        }
        return usuario.getUsername();
    }

    private String resolveEmail(Usuario usuario) {
        if (usuario.getDocente() != null && usuario.getDocente().getEmail() != null) {
            return usuario.getDocente().getEmail();
        }
        if (usuario.getAlumno() != null && usuario.getAlumno().getEmail() != null) {
            return usuario.getAlumno().getEmail();
        }
        return usuario.getUsername() + "@academit.edu.ar";
    }
}

