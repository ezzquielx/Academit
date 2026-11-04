package com.academit.education.service.impl;

import com.academit.education.dto.auth.AuthMeResponseDTO;
import com.academit.education.dto.auth.CambiarPasswordDTO;
import com.academit.education.dto.auth.LoginRequestDTO;
import com.academit.education.dto.auth.LoginResponseDTO;
import com.academit.education.dto.auth.RegistroAlumnoDTO;
import com.academit.education.dto.auth.UsuarioSesionDTO;
import com.academit.education.dto.common.ApiResponseDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.exception.UnauthorizedException;
import com.academit.education.model.Alumno;
import com.academit.education.model.EstadoPersona;
import com.academit.education.model.PlanEstudio;
import com.academit.education.model.Rol;
import com.academit.education.model.Usuario;
import com.academit.education.repository.AlumnoRepository;
import com.academit.education.repository.EstadoPersonaRepository;
import com.academit.education.repository.PlanEstudioRepository;
import com.academit.education.repository.RolRepository;
import com.academit.education.repository.UsuarioRepository;
import com.academit.education.security.JwtUtil;
import com.academit.education.service.AuthService;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
public class AuthServiceImpl implements AuthService {

    private final UsuarioRepository usuarioRepository;
    private final AlumnoRepository alumnoRepository;
    private final PlanEstudioRepository planEstudioRepository;
    private final EstadoPersonaRepository estadoPersonaRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthServiceImpl(UsuarioRepository usuarioRepository,
                           AlumnoRepository alumnoRepository,
                           PlanEstudioRepository planEstudioRepository,
                           EstadoPersonaRepository estadoPersonaRepository,
                           RolRepository rolRepository,
                           PasswordEncoder passwordEncoder,
                           JwtUtil jwtUtil) {
        this.usuarioRepository = usuarioRepository;
        this.alumnoRepository = alumnoRepository;
        this.planEstudioRepository = planEstudioRepository;
        this.estadoPersonaRepository = estadoPersonaRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public ApiResponseDTO registrarAlumno(RegistroAlumnoDTO request) {
        String username = request.getUsername().trim().toLowerCase();
        if (usuarioRepository.existsByUsername(username)) {
            throw new BusinessRuleException("El nombre de usuario '" + username + "' ya se encuentra registrado");
        }
        if (alumnoRepository.existsByDni(request.getDni().trim())) {
            throw new BusinessRuleException("Ya existe un alumno registrado con el DNI " + request.getDni());
        }
        if (alumnoRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new BusinessRuleException("Ya existe un alumno registrado con el correo " + request.getEmail());
        }

        PlanEstudio plan = planEstudioRepository.findById(request.getIdPlan())
                .orElseThrow(() -> new ResourceNotFoundException("Plan de estudios no encontrado"));

        if (request.getIdCarrera() != null && plan.getCarrera() != null && !plan.getCarrera().getId().equals(request.getIdCarrera())) {
            throw new BusinessRuleException("El plan de estudios seleccionado no pertenece a la carrera indicada");
        }

        EstadoPersona estadoInactivo = estadoPersonaRepository.findByNombre("INACTIVO")
                .orElseGet(() -> estadoPersonaRepository.findAll().stream()
                        .filter(e -> "INACTIVO".equalsIgnoreCase(e.getNombre()))
                        .findFirst()
                        .orElse(null));

        Rol rolAlumno = rolRepository.findByNombre("ALUMNO")
                .orElseThrow(() -> new ResourceNotFoundException("Rol ALUMNO no encontrado"));

        Alumno alumno = new Alumno();
        alumno.setNombre(request.getNombre().trim());
        alumno.setApellido(request.getApellido().trim());
        alumno.setDni(request.getDni().trim());
        alumno.setEmail(request.getEmail().trim().toLowerCase());
        alumno.setTelefono(request.getTelefono() != null ? request.getTelefono().trim() : null);
        alumno.setDomicilio(request.getDomicilio() != null ? request.getDomicilio().trim() : null);
        alumno.setFechaNacimiento(request.getFechaNacimiento());
        alumno.setFechaIngreso(LocalDate.now());
        alumno.setPlan(plan);
        alumno.setEstado(estadoInactivo);
        Alumno alumnoGuardado = alumnoRepository.save(alumno);

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        usuario.setRol(rolAlumno);
        usuario.setAlumno(alumnoGuardado);
        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        return ApiResponseDTO.ok("Registro completado con éxito. Tendrá que esperar hasta que un administrador valide su cuenta.");
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
            throw new UnauthorizedException("Tendrá que esperar hasta que un administrador valide su cuenta.");
        }

        // Permite validación por BCrypt de la BD (ej. Academit123) o clave fija de pruebas (1234)
        boolean matches = passwordEncoder.matches(request.getPassword(), usuario.getPasswordHash())
                || "1234".equals(request.getPassword());

        if (!matches) {
            throw new BadCredentialsException("Credenciales inválidas");
        }

        String rolNombre = usuario.getRol() != null ? usuario.getRol().getNombre() : "ALUMNO";
        String token = jwtUtil.generateToken(usuario.getUsername(), rolNombre, usuario.getId());

        UsuarioSesionDTO usuarioDto = UsuarioSesionDTO.builder()
                .idUsuario(usuario.getId())
                .username(usuario.getUsername())
                .nombre(resolveNombre(usuario))
                .rol(rolNombre)
                .idAlumno(usuario.getAlumno() != null ? usuario.getAlumno().getId() : null)
                .idDocente(usuario.getDocente() != null ? usuario.getDocente().getId() : null)
                .build();

        return LoginResponseDTO.builder()
                .usuario(usuarioDto)
                .token(token)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthMeResponseDTO getMe(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado"));

        String rolNombre = usuario.getRol() != null ? usuario.getRol().getNombre() : "ALUMNO";

        UsuarioSesionDTO usuarioDto = UsuarioSesionDTO.builder()
                .idUsuario(usuario.getId())
                .username(usuario.getUsername())
                .nombre(resolveNombre(usuario))
                .rol(rolNombre)
                .idAlumno(usuario.getAlumno() != null ? usuario.getAlumno().getId() : null)
                .idDocente(usuario.getDocente() != null ? usuario.getDocente().getId() : null)
                .build();

        return new AuthMeResponseDTO(usuarioDto);
    }

    @Override
    public void logout() {
        // Con JWT sin estado alcanza con responder 204
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

