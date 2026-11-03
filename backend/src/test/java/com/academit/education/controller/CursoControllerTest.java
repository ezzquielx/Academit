package com.academit.education.controller;

import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.service.CursoService;
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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CursoControllerTest {

    private static final String BASE = "/api/cursos";

    private static final String BODY_VALIDO = """
            {
              "codigo": "2A-PRO-TN",
              "nombre": "2° Año B",
              "idCarrera": 1,
              "idPlan": 5,
              "turno": "NOCHE",
              "capacidadMaxima": 25,
              "cicloLectivo": 2026
            }
            """;

    private static final String BODY_INVALIDO = """
            {
              "nombre": "2° Año B",
              "turno": "NOCHE",
              "capacidadMaxima": 0
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CursoService cursoService;

    @Test
    void getSinSesionRetorna401() throws Exception {
        when(cursoService.listarCursos()).thenReturn(List.of());

        mockMvc.perform(get(BASE)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void getListadoConDocenteRetorna200() throws Exception {
        when(cursoService.listarCursos()).thenReturn(List.of());

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void postConDocenteRetorna403() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void postRetorna201() throws Exception {
        when(cursoService.crearCurso(any())).thenReturn(cursoEjemplo());

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("1A-PRO-TM"))
                .andExpect(jsonPath("$.cuposDisponibles").value(29));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void postConCapacidadCeroRetorna400() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_INVALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void putRetorna200() throws Exception {
        when(cursoService.actualizarCurso(eq(3), any())).thenReturn(cursoEjemplo());

        mockMvc.perform(put(BASE + "/3").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"turno\": \"NOCHE\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCurso").value(3));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void putInexistenteRetorna404() throws Exception {
        when(cursoService.actualizarCurso(eq(99), any()))
                .thenThrow(new ResourceNotFoundException("Curso no encontrado con ID: 99"));

        mockMvc.perform(put(BASE + "/99").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"turno\": \"NOCHE\"}"))
                .andExpect(status().isNotFound());
    }

    private com.academit.education.dto.curso.CursoDTO cursoEjemplo() {
        com.academit.education.dto.curso.CursoDTO dto =
                new com.academit.education.dto.curso.CursoDTO();
        dto.setIdCurso(3);
        dto.setCodigo("1A-PRO-TM");
        dto.setNombre("1° Año A");
        dto.setTurno("MANANA");
        dto.setCapacidadMaxima(30);
        dto.setInscriptos(1);
        dto.setCuposDisponibles(29);
        dto.setCicloLectivo(2026);
        return dto;
    }
}
