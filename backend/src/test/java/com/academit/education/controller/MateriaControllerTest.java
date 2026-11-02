package com.academit.education.controller;

import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.service.MateriaService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MateriaControllerTest {

    private static final String BASE = "/api/materias";

    private static final String BODY_VALIDO = """
            {
              "codigo": "MAT-I",
              "nombre": "Matemática",
              "idCarrera": 1,
              "idPlan": 5,
              "anio": 1,
              "regimen": "CUATRIMESTRAL",
              "horasSemanales": 4,
              "correlativas": []
            }
            """;

    private static final String BODY_INVALIDO = """
            {
              "nombre": "Matemática",
              "regimen": "CUATRIMESTRAL",
              "horasSemanales": 4
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MateriaService materiaService;

    @Test
    void getSinSesionRetorna401() throws Exception {
        when(materiaService.listarMaterias(null, null)).thenReturn(List.of());

        mockMvc.perform(get(BASE)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void listarMateriasRetorna200() throws Exception {
        when(materiaService.listarMaterias(null, null)).thenReturn(List.of());

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void listarMateriasConFiltroPlanRetorna200() throws Exception {
        when(materiaService.listarMaterias(null, 5)).thenReturn(List.of());

        mockMvc.perform(get(BASE + "?idPlan=5")).andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void postRetorna201() throws Exception {
        when(materiaService.crearMateria(any()))
                .thenReturn(new com.academit.education.dto.materia.MateriaDTO());

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isCreated());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void postSinCamposObligatoriosRetorna400() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_INVALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void putRetorna200() throws Exception {
        when(materiaService.actualizarMateria(any(), any()))
                .thenReturn(new com.academit.education.dto.materia.MateriaDTO());

        mockMvc.perform(put(BASE + "/10").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"regimen\": \"ANUAL\"}"))
                .andExpect(status().isOk());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void putInexistenteRetorna404() throws Exception {
        when(materiaService.actualizarMateria(any(), any()))
                .thenThrow(new ResourceNotFoundException("Materia no encontrada con ID: 99"));

        mockMvc.perform(put(BASE + "/99").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"regimen\": \"ANUAL\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void postConDocenteRetorna403() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isForbidden());
    }
}
