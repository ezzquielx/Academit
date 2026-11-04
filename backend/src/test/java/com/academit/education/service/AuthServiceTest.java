package com.academit.education.service;

import com.academit.education.dto.auth.CambiarPasswordDTO;
import com.academit.education.dto.auth.LoginRequestDTO;
import com.academit.education.dto.auth.LoginResponseDTO;
import com.academit.education.dto.auth.RegistroAlumnoDTO;
import com.academit.education.dto.common.ApiResponseDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.UnauthorizedException;
import com.academit.education.model.Alumno;
import com.academit.education.model.Docente;
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
import com.academit.education.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private PlanEstudioRepository planEstudioRepository;

    @Mock
    private EstadoPersonaRepository estadoPersonaRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    private AuthService authService;

    @BeforeEach
    void setUp() {
        authService = new AuthServiceImpl(
                usuarioRepository,
                alumnoRepository,
                planEstudioRepository,
                estadoPersonaRepository,
                rolRepository,
                passwordEncoder,
                jwtUtil
        );
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
        assertNotNull(response.getUsuario());
        assertEquals(1, response.getUsuario().getIdUsuario());
        assertEquals("admin", response.getUsuario().getUsername());
        assertEquals("ADMINISTRADOR", response.getUsuario().getRol());
        assertNull(response.getUsuario().getIdAlumno());
        assertNull(response.getUsuario().getIdDocente());
        assertEquals("fake-jwt-token", response.getToken());
    }

    @Test
    void testLoginAlumnoReturnsIdAlumnoAndNullDocente() {
        Rol rol = new Rol();
        rol.setId(3);
        rol.setNombre("ALUMNO");

        Alumno alumno = new Alumno();
        alumno.setId(5);
        alumno.setNombre("Lucía");
        alumno.setApellido("Benítez");

        Usuario usuario = new Usuario();
        usuario.setId(10);
        usuario.setUsername("alumno1");
        usuario.setPasswordHash("$2b$10$hashed");
        usuario.setActivo(true);
        usuario.setRol(rol);
        usuario.setAlumno(alumno);

        when(usuarioRepository.findByUsername("alumno1")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Academit123", "$2b$10$hashed")).thenReturn(true);
        when(jwtUtil.generateToken("alumno1", "ALUMNO", 10)).thenReturn("fake-jwt-token");

        LoginResponseDTO response = authService.login(new LoginRequestDTO("alumno1", "Academit123"));

        assertNotNull(response);
        assertEquals(5, response.getUsuario().getIdAlumno());
        assertNull(response.getUsuario().getIdDocente());
        assertEquals("ALUMNO", response.getUsuario().getRol());
    }

    @Test
    void testLoginDocenteReturnsIdDocenteAndNullAlumno() {
        Rol rol = new Rol();
        rol.setId(2);
        rol.setNombre("DOCENTE");

        Docente docente = new Docente();
        docente.setId(8);
        docente.setNombre("Martín");
        docente.setApellido("González");

        Usuario usuario = new Usuario();
        usuario.setId(20);
        usuario.setUsername("docente1");
        usuario.setPasswordHash("$2b$10$hashed");
        usuario.setActivo(true);
        usuario.setRol(rol);
        usuario.setDocente(docente);

        when(usuarioRepository.findByUsername("docente1")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Academit123", "$2b$10$hashed")).thenReturn(true);
        when(jwtUtil.generateToken("docente1", "DOCENTE", 20)).thenReturn("fake-jwt-token");

        LoginResponseDTO response = authService.login(new LoginRequestDTO("docente1", "Academit123"));

        assertNotNull(response);
        assertEquals(8, response.getUsuario().getIdDocente());
        assertNull(response.getUsuario().getIdAlumno());
        assertEquals("DOCENTE", response.getUsuario().getRol());
    }

    @Test
    void testLoginAdministrativoReturnsNullForBoth() {
        Rol rol = new Rol();
        rol.setId(4);
        rol.setNombre("ADMINISTRATIVO");

        Usuario usuario = new Usuario();
        usuario.setId(30);
        usuario.setUsername("administrativo");
        usuario.setPasswordHash("$2b$10$hashed");
        usuario.setActivo(true);
        usuario.setRol(rol);

        when(usuarioRepository.findByUsername("administrativo")).thenReturn(Optional.of(usuario));
        when(passwordEncoder.matches("Academit123", "$2b$10$hashed")).thenReturn(true);
        when(jwtUtil.generateToken("administrativo", "ADMINISTRATIVO", 30)).thenReturn("fake-jwt-token");

        LoginResponseDTO response = authService.login(new LoginRequestDTO("administrativo", "Academit123"));

        assertNotNull(response);
        assertNull(response.getUsuario().getIdAlumno());
        assertNull(response.getUsuario().getIdDocente());
        assertEquals("ADMINISTRATIVO", response.getUsuario().getRol());
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
        UnauthorizedException ex = assertThrows(UnauthorizedException.class, () -> authService.login(request));
        assertTrue(ex.getMessage().contains("Tendrá que esperar hasta que un administrador valide su cuenta"));
    }

    @Test
    void testLogoutSuccess() {
        assertDoesNotThrow(() -> authService.logout());
    }

    @Test
    void testMeSuccess() {
        Rol rol = new Rol();
        rol.setId(1);
        rol.setNombre("ADMINISTRADOR");

        Usuario usuario = new Usuario();
        usuario.setId(1);
        usuario.setUsername("admin");
        usuario.setRol(rol);

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));

        com.academit.education.dto.auth.AuthMeResponseDTO me = authService.getMe("admin");
        assertNotNull(me);
        assertNotNull(me.getUsuario());
        assertEquals(1, me.getUsuario().getIdUsuario());
        assertEquals("admin", me.getUsuario().getUsername());
        assertEquals("ADMINISTRADOR", me.getUsuario().getRol());
        assertNull(me.getUsuario().getIdAlumno());
        assertNull(me.getUsuario().getIdDocente());
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

    @Test
    void testRegistrarAlumnoSuccess() {
        RegistroAlumnoDTO dto = new RegistroAlumnoDTO();
        dto.setUsername("nuevalumno");
        dto.setPassword("Secret123");
        dto.setDni("45123456");
        dto.setNombre("Carlos");
        dto.setApellido("Perez");
        dto.setEmail("carlos.perez@example.com");
        dto.setFechaNacimiento(LocalDate.of(2003, 5, 10));
        dto.setIdCarrera(1);
        dto.setIdPlan(1);

        when(usuarioRepository.existsByUsername("nuevalumno")).thenReturn(false);
        when(alumnoRepository.existsByDni("45123456")).thenReturn(false);
        when(alumnoRepository.existsByEmail("carlos.perez@example.com")).thenReturn(false);

        PlanEstudio plan = new PlanEstudio();
        plan.setId(1);
        plan.setNombre("Plan 2023");
        com.academit.education.model.Carrera carrera = new com.academit.education.model.Carrera();
        carrera.setId(1);
        carrera.setNombre("Ingeniería en Sistemas");
        plan.setCarrera(carrera);
        when(planEstudioRepository.findById(1)).thenReturn(Optional.of(plan));

        EstadoPersona estado = new EstadoPersona();
        estado.setId(2);
        estado.setNombre("INACTIVO");
        when(estadoPersonaRepository.findByNombre("INACTIVO")).thenReturn(Optional.of(estado));

        Rol rol = new Rol();
        rol.setId(3);
        rol.setNombre("ALUMNO");
        when(rolRepository.findByNombre("ALUMNO")).thenReturn(Optional.of(rol));

        when(passwordEncoder.encode("Secret123")).thenReturn("$2b$10$encoded");

        Alumno savedAlumno = new Alumno();
        savedAlumno.setId(10);
        savedAlumno.setDni("45123456");
        savedAlumno.setNombre("Carlos");
        savedAlumno.setApellido("Perez");
        when(alumnoRepository.save(any(Alumno.class))).thenReturn(savedAlumno);

        Usuario savedUsuario = new Usuario();
        savedUsuario.setId(10);
        savedUsuario.setUsername("nuevalumno");
        savedUsuario.setRol(rol);
        savedUsuario.setAlumno(savedAlumno);
        savedUsuario.setActivo(false);
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(savedUsuario);

        ApiResponseDTO response = authService.registrarAlumno(dto);

        assertNotNull(response);
        assertTrue(response.isSuccess());
        assertTrue(response.getMessage().contains("Tendrá que esperar hasta que un administrador valide su cuenta"));
        verify(alumnoRepository).save(any(Alumno.class));
        verify(usuarioRepository).save(argThat(u -> !Boolean.TRUE.equals(u.getActivo())));
    }
}

