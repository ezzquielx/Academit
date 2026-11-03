package com.academit.education.controller;

import com.academit.education.dto.calificacion.CalificacionDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.service.CalificacionService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CalificacionControllerTest {

    private static final String BASE = "/api/calificaciones";

    private static final String BODY_VALIDO = """
            {
              "idAlumno": 7,
              "idMateria": 9,
              "evaluacionNombre": "Primer Parcial",
              "tipo": "PARCIAL",
              "nota": 8
            }
            """;

    private static final String BODY_NOTA_INVALIDA = """
            {
              "idAlumno": 7,
              "idMateria": 9,
              "evaluacionNombre": "Primer Parcial",
              "nota": 11
            }
            """;

    private static final String BODY_NOTA_CERO = """
            {
              "idAlumno": 7,
              "idMateria": 9,
              "evaluacionNombre": "Primer Parcial",
              "nota": 0
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CalificacionService calificacionService;

    private CalificacionDTO calificacionEjemplo() {
        CalificacionDTO dto = new CalificacionDTO();
        dto.setIdCalificacion(55);
        dto.setIdAlumno(7);
        dto.setAlumnoNombre("Lucía Pérez");
        dto.setIdMateria(9);
        dto.setMateriaNombre("Programación II");
        dto.setEvaluacionNombre("Primer Parcial");
        dto.setTipo("PARCIAL");
        dto.setNota(8);
        dto.setFecha("2026-09-15");
        dto.setCondicion("APROBADO");
        return dto;
    }

    @Test
    void getSinSesionRetorna401() throws Exception {
        when(calificacionService.listar(any(), any(), any(), any(), any())).thenReturn(List.of());
        mockMvc.perform(get(BASE)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void getConAlumnoRetorna200() throws Exception {
        when(calificacionService.listar(any(), any(), any(), any(), any()))
                .thenReturn(List.of(calificacionEjemplo()));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idCalificacion").value(55))
                .andExpect(jsonPath("$[0].condicion").value("APROBADO"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void getConAdminRetorna200() throws Exception {
        when(calificacionService.listar(any(), any(), any(), any(), any())).thenReturn(List.of());
        mockMvc.perform(get(BASE).param("idAlumno", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void postConAdminRetorna200() throws Exception {
        when(calificacionService.guardar(any(), any())).thenReturn(calificacionEjemplo());
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(BODY_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.nota").value(8));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void postConAlumnoRetorna403() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(BODY_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void postNotaOnceRetorna400() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(BODY_NOTA_INVALIDA))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void postNotaCeroRetorna400() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(BODY_NOTA_CERO))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void postAlumnoNoInscritoRetorna400() throws Exception {
        when(calificacionService.guardar(any(), any())).thenThrow(
                new BusinessRuleException("El alumno debe estar inscripto en el curso de la evaluación"));
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(BODY_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists());
    }
}
