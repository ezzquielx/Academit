package com.academit.education.service;

import com.academit.education.dto.common.ApiResponseDTO;
import com.academit.education.dto.usuario.PerfilUsuarioDTO;
import com.academit.education.dto.usuario.UsuarioRequestDTO;
import com.academit.education.dto.usuario.UsuarioResponseDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.model.Rol;
import com.academit.education.model.Usuario;
import com.academit.education.repository.AlumnoRepository;
import com.academit.education.repository.DocenteRepository;
import com.academit.education.repository.RolRepository;
import com.academit.education.repository.UsuarioRepository;
import com.academit.education.service.impl.UsuarioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private AlumnoRepository alumnoRepository;

    @Mock
    private DocenteRepository docenteRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    private UsuarioService usuarioService;

    @BeforeEach
    void setUp() {
        usuarioService = new UsuarioServiceImpl(usuarioRepository, rolRepository, alumnoRepository, docenteRepository, passwordEncoder);
    }

    @Test
    void testGetPerfilAdmin() {
        Rol rol = new Rol();
        rol.setNombre("ADMINISTRADOR");

        Usuario usuario = new Usuario();
        usuario.setUsername("admin");
        usuario.setRol(rol);
        usuario.setActivo(true);

        when(usuarioRepository.findByUsername("admin")).thenReturn(Optional.of(usuario));

        PerfilUsuarioDTO perfil = usuarioService.getPerfil("admin");

        assertNotNull(perfil);
        assertEquals("admin", perfil.getUsername());
        assertEquals("Administrador General", perfil.getNombre());
        assertEquals("ADMINISTRADOR", perfil.getRol());
    }

    @Test
    void testCrearUsuarioSuccess() {
        Rol rol = new Rol();
        rol.setId(2);
        rol.setNombre("ADMINISTRATIVO");

        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .username("nuevoUser")
                .password("Password123")
                .rol("ADMINISTRATIVO")
                .estado("ACTIVO")
                .build();

        when(usuarioRepository.existsByUsername("nuevoUser")).thenReturn(false);
        when(rolRepository.findByNombre("ADMINISTRATIVO")).thenReturn(Optional.of(rol));
        when(passwordEncoder.encode("Password123")).thenReturn("$2b$10$hashed");
        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(invocation -> {
            Usuario u = invocation.getArgument(0);
            u.setId(10);
            return u;
        });

        UsuarioResponseDTO response = usuarioService.crearUsuario(request);

        assertNotNull(response);
        assertEquals(10, response.getIdUsuario());
        assertEquals("nuevoUser", response.getUsername());
        assertEquals("ADMINISTRATIVO", response.getRol());
    }

    @Test
    void testCrearUsuarioDuplicateUsernameThrowsException() {
        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
                .username("existingUser")
                .build();

        when(usuarioRepository.existsByUsername("existingUser")).thenReturn(true);

        assertThrows(BusinessRuleException.class, () -> usuarioService.crearUsuario(request));
    }

    @Test
    void testDarDeBajaSetsActivoFalse() {
        Usuario usuario = new Usuario();
        usuario.setId(5);
        usuario.setActivo(true);

        when(usuarioRepository.findById(5)).thenReturn(Optional.of(usuario));

        ApiResponseDTO response = usuarioService.darDeBaja(5);

        assertTrue(response.isSuccess());
        assertFalse(usuario.getActivo());
        verify(usuarioRepository).save(usuario);
    }
}

