package com.academit.education.controller;

import com.academit.education.service.PlanEstudioService;
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
class PlanEstudioControllerTest {

    private static final String BASE = "/api/planes-estudio";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PlanEstudioService planEstudioService;

    @Test
    void getSinSesionRetorna401() throws Exception {
        when(planEstudioService.listarPlanes()).thenReturn(List.of());

        mockMvc.perform(get(BASE)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRATIVO"})
    void listarPlanesRetorna200() throws Exception {
        when(planEstudioService.listarPlanes()).thenReturn(List.of());

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void listarPlanesConDocenteRetorna403() throws Exception {
        mockMvc.perform(get(BASE)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void materiasDePlanConAlumnoRetorna200() throws Exception {
        when(planEstudioService.listarMateriasDePlan(5)).thenReturn(List.of());

        mockMvc.perform(get(BASE + "/5/materias"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void materiasDePlanConDocenteRetorna200() throws Exception {
        when(planEstudioService.listarMateriasDePlan(5)).thenReturn(List.of());

        mockMvc.perform(get(BASE + "/5/materias")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void listarPlanesConAlumnoRetorna403() throws Exception {
        mockMvc.perform(get(BASE)).andExpect(status().isForbidden());
    }
}
