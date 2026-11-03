package com.academit.education.controller;

import com.academit.education.dto.asistencia.AsistenciaDTO;
import com.academit.education.service.AsistenciaService;
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
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AsistenciaControllerTest {

    private static final String BASE = "/api/asistencias";

    private static final String BODY_LOTE_VALIDO = """
            {
              "asistencias": [
                { "idAlumno": 7, "idMateria": 9, "idCurso": 3,
                  "fecha": "2026-10-20", "estado": "PRESENTE" },
                { "idAlumno": 8, "idMateria": 9, "idCurso": 3,
                  "fecha": "2026-10-20", "estado": "AUSENTE" }
              ]
            }
            """;

    private static final String BODY_LOTE_INVALIDO = """
            {
              "asistencias": [
                { "idAlumno": 7, "idMateria": 9, "idCurso": 3,
                  "fecha": "2099-01-01", "estado": "TARDE" }
              ]
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AsistenciaService asistenciaService;

    private AsistenciaDTO asistenciaEjemplo() {
        AsistenciaDTO dto = new AsistenciaDTO();
        dto.setIdAsistencia(80);
        dto.setIdAlumno(7);
        dto.setAlumnoNombre("Lucía Pérez");
        dto.setIdMateria(9);
        dto.setMateriaNombre("Programación II");
        dto.setIdCurso(3);
        dto.setFecha("2026-10-20");
        dto.setEstado("PRESENTE");
        return dto;
    }

    @Test
    void getSinSesionRetorna401() throws Exception {
        when(asistenciaService.listar(any(), any(), any(), any())).thenReturn(List.of());
        mockMvc.perform(get(BASE)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void getConDocenteRetorna200() throws Exception {
        when(asistenciaService.listar(any(), any(), any(), any()))
                .thenReturn(List.of(asistenciaEjemplo()));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idAsistencia").value(80))
                .andExpect(jsonPath("$[0].estado").value("PRESENTE"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void getConAlumnoRetorna200() throws Exception {
        when(asistenciaService.listar(any(), anyInt(), any(), any()))
                .thenReturn(List.of(asistenciaEjemplo()));

        mockMvc.perform(get(BASE).param("idCurso", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void postLoteConDocenteRetorna200() throws Exception {
        when(asistenciaService.registrarLote(any(), any()))
                .thenReturn(com.academit.education.dto.asistencia.SuccessResponseDTO
                        .ok("Asistencias registradas: 2"));

        mockMvc.perform(post(BASE + "/lote").contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_LOTE_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.message").value("Asistencias registradas: 2"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void postLoteConAlumnoRetorna403() throws Exception {
        mockMvc.perform(post(BASE + "/lote").contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_LOTE_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void postLoteInvalidoRetorna400() throws Exception {
        when(asistenciaService.registrarLote(any(), any()))
                .thenThrow(new com.academit.education.exception.BusinessRuleException(
                        "Se detectaron errores en el lote de asistencias; no se registró ninguna"));

        mockMvc.perform(post(BASE + "/lote").contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_LOTE_INVALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    void postLoteSinSesionRetorna401() throws Exception {
        mockMvc.perform(post(BASE + "/lote").contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_LOTE_VALIDO))
                .andExpect(status().isUnauthorized());
    }
}
