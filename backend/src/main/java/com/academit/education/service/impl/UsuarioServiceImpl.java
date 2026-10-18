package com.academit.education.service.impl;

import com.academit.education.dto.common.ApiResponseDTO;
import com.academit.education.dto.usuario.PerfilUsuarioDTO;
import com.academit.education.dto.usuario.UsuarioRequestDTO;
import com.academit.education.dto.usuario.UsuarioResponseDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Alumno;
import com.academit.education.model.Docente;
import com.academit.education.model.Rol;
import com.academit.education.model.Usuario;
import com.academit.education.repository.AlumnoRepository;
import com.academit.education.repository.DocenteRepository;
import com.academit.education.repository.RolRepository;
import com.academit.education.repository.UsuarioRepository;
import com.academit.education.service.UsuarioService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class UsuarioServiceImpl implements UsuarioService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final AlumnoRepository alumnoRepository;
    private final DocenteRepository docenteRepository;
    private final PasswordEncoder passwordEncoder;

    public UsuarioServiceImpl(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            AlumnoRepository alumnoRepository,
            DocenteRepository docenteRepository,
            PasswordEncoder passwordEncoder) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.alumnoRepository = alumnoRepository;
        this.docenteRepository = docenteRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional(readOnly = true)
    public PerfilUsuarioDTO getPerfil(String username) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        return toPerfilDTO(usuario);
    }

    @Override
    @Transactional
    public PerfilUsuarioDTO actualizarPerfil(String username, PerfilUsuarioDTO request) {
        Usuario usuario = usuarioRepository.findByUsername(username)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado: " + username));

        if (usuario.getAlumno() != null) {
            Alumno alumno = usuario.getAlumno();
            if (request.getTelefono() != null) alumno.setTelefono(request.getTelefono());
            if (request.getDomicilio() != null) alumno.setDomicilio(request.getDomicilio());
            if (request.getEmail() != null) alumno.setEmail(request.getEmail());
            alumnoRepository.save(alumno);
        } else if (usuario.getDocente() != null) {
            Docente docente = usuario.getDocente();
            if (request.getTelefono() != null) docente.setTelefono(request.getTelefono());
            if (request.getEmail() != null) docente.setEmail(request.getEmail());
            docenteRepository.save(docente);
        }

        return toPerfilDTO(usuario);
    }

    @Override
    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarUsuarios(String rol, String estado) {
        return usuarioRepository.findAll().stream()
                .filter(u -> {
                    if (rol == null || rol.isBlank()) return true;
                    return u.getRol() != null && u.getRol().getNombre().equalsIgnoreCase(rol.trim());
                })
                .filter(u -> {
                    if (estado == null || estado.isBlank()) return true;
                    boolean activoEsperado = "ACTIVO".equalsIgnoreCase(estado.trim());
                    return Boolean.TRUE.equals(u.getActivo()) == activoEsperado;
                })
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public UsuarioResponseDTO obtenerPorId(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));
        return toResponseDTO(usuario);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO crearUsuario(UsuarioRequestDTO request) {
        String username = request.getUsername().trim();
        if (usuarioRepository.existsByUsername(username)) {
            throw new BusinessRuleException("El nombre de usuario '" + username + "' ya se encuentra registrado");
        }

        String rolNombre = request.getRol() != null ? request.getRol().trim().toUpperCase() : "ADMINISTRATIVO";
        Rol rol = rolRepository.findByNombre(rolNombre)
                .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + rolNombre));

        String rawPassword = (request.getPassword() != null && !request.getPassword().isBlank())
                ? request.getPassword()
                : "Academit123";

        Usuario usuario = new Usuario();
        usuario.setUsername(username);
        usuario.setPasswordHash(passwordEncoder.encode(rawPassword));
        usuario.setRol(rol);
        usuario.setActivo(request.getEstado() == null || "ACTIVO".equalsIgnoreCase(request.getEstado()));

        Usuario guardado = usuarioRepository.save(usuario);
        return toResponseDTO(guardado);
    }

    @Override
    @Transactional
    public UsuarioResponseDTO actualizarUsuario(Integer id, UsuarioRequestDTO request) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        if (request.getRol() != null && !request.getRol().isBlank()) {
            String rolNombre = request.getRol().trim().toUpperCase();
            Rol rol = rolRepository.findByNombre(rolNombre)
                    .orElseThrow(() -> new ResourceNotFoundException("Rol no encontrado: " + rolNombre));
            usuario.setRol(rol);
        }

        if (request.getEstado() != null && !request.getEstado().isBlank()) {
            usuario.setActivo("ACTIVO".equalsIgnoreCase(request.getEstado().trim()));
        }

        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            usuario.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }

        Usuario guardado = usuarioRepository.save(usuario);
        return toResponseDTO(guardado);
    }

    @Override
    @Transactional
    public ApiResponseDTO darDeBaja(Integer id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con ID: " + id));

        usuario.setActivo(false);
        usuarioRepository.save(usuario);

        return ApiResponseDTO.ok("Usuario dado de baja exitosamente");
    }

    private PerfilUsuarioDTO toPerfilDTO(Usuario usuario) {
        String nombre = usuario.getUsername();
        String email = usuario.getUsername() + "@academit.edu.ar";
        String telefono = null;
        String domicilio = null;

        if (usuario.getAlumno() != null) {
            Alumno a = usuario.getAlumno();
            nombre = a.getNombre() + " " + a.getApellido();
            email = a.getEmail();
            telefono = a.getTelefono();
            domicilio = a.getDomicilio();
        } else if (usuario.getDocente() != null) {
            Docente d = usuario.getDocente();
            nombre = d.getNombre() + " " + d.getApellido();
            email = d.getEmail();
            telefono = d.getTelefono();
        } else if ("admin".equalsIgnoreCase(usuario.getUsername())) {
            nombre = "Administrador General";
            email = "admin@academit.edu.ar";
        } else if ("administrativo".equalsIgnoreCase(usuario.getUsername())) {
            nombre = "Personal Administrativo";
            email = "administracion@academit.edu.ar";
        }

        return PerfilUsuarioDTO.builder()
                .username(usuario.getUsername())
                .nombre(nombre)
                .email(email)
                .telefono(telefono)
                .domicilio(domicilio)
                .rol(usuario.getRol() != null ? usuario.getRol().getNombre() : "")
                .build();
    }

    private UsuarioResponseDTO toResponseDTO(Usuario usuario) {
        PerfilUsuarioDTO perfil = toPerfilDTO(usuario);
        return UsuarioResponseDTO.builder()
                .idUsuario(usuario.getId())
                .username(usuario.getUsername())
                .nombre(perfil.getNombre())
                .email(perfil.getEmail())
                .rol(usuario.getRol() != null ? usuario.getRol().getNombre() : "")
                .estado(Boolean.TRUE.equals(usuario.getActivo()) ? "ACTIVO" : "INACTIVO")
                .build();
    }
}

