package com.academit.education.controller;

import com.academit.education.dto.usuario.PerfilUsuarioDTO;
import com.academit.education.security.JwtUtil;
import com.academit.education.service.UsuarioService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class UsuarioControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UsuarioService usuarioService;

    @Test
    void testGetUsuariosUnauthenticatedReturns403Or401() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void testGetUsuariosForbiddenForDocente() throws Exception {
        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.message").value("Acceso denegado: no posee los permisos requeridos"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void testGetUsuariosAllowedForAdmin() throws Exception {
        when(usuarioService.listarUsuarios(null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/usuarios"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void testGetMiPerfilReturnsUserData() throws Exception {
        PerfilUsuarioDTO perfil = PerfilUsuarioDTO.builder()
                .username("admin")
                .nombre("Administrador General")
                .email("admin@academit.edu.ar")
                .rol("ADMINISTRADOR")
                .build();

        when(usuarioService.getPerfil("admin")).thenReturn(perfil);

        mockMvc.perform(get("/api/usuarios/me"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("admin"))
                .andExpect(jsonPath("$.nombre").value("Administrador General"));
    }
}
