package com.academit.education.controller;

import com.academit.education.dto.declaracion.DeclaracionJuradaDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ConflictException;
import com.academit.education.service.DeclaracionJuradaService;
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
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class DeclaracionJuradaControllerTest {

    private static final String BASE = "/api/declaraciones-juradas";

    private static final String BODY_VALIDO = """
            {
              "idAlumno": 7,
              "idCiclo": 3,
              "materiasDeclaradasIds": [1, 2],
              "observaciones": "Comprometo la cursada"
            }
            """;

    private static final String BODY_INVALIDO = """
            {
              "idAlumno": null,
              "idCiclo": null,
              "materiasDeclaradasIds": []
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DeclaracionJuradaService declaracionJuradaService;

    private DeclaracionJuradaDTO declaracionEjemplo() {
        DeclaracionJuradaDTO dto = new DeclaracionJuradaDTO();
        dto.setIdDeclaracion(30);
        dto.setIdAlumno(7);
        dto.setAlumnoNombre("Pérez, Lucía");
        dto.setIdCiclo(3);
        dto.setCicloNombre("Ciclo Lectivo 2026");
        dto.setFecha("2026-10-30");
        dto.setMateriasDeclaradasIds(List.of(1, 2));
        dto.setMateriasNombres(List.of("Programación I", "Programación II"));
        dto.setEstado("PENDIENTE");
        return dto;
    }

    @Test
    void getSinSesionRetorna401() throws Exception {
        when(declaracionJuradaService.listar(any(), any(), any())).thenReturn(List.of());
        mockMvc.perform(get(BASE)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void getConAlumnoRetorna200() throws Exception {
        when(declaracionJuradaService.listar(any(), any(), any()))
                .thenReturn(List.of(declaracionEjemplo()));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idDeclaracion").value(30))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"))
                .andExpect(jsonPath("$[0].materiasDeclaradasIds[1]").value(2));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void getConAdminConFiltrosRetorna200() throws Exception {
        when(declaracionJuradaService.listar(any(), eq(7), eq(3)))
                .thenReturn(List.of(declaracionEjemplo()));

        mockMvc.perform(get(BASE).param("idAlumno", "7").param("idCiclo", "3"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].alumnoNombre").value("Pérez, Lucía"));
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void getConDocenteRetorna403() throws Exception {
        mockMvc.perform(get(BASE)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void postRetorna201() throws Exception {
        when(declaracionJuradaService.presentar(any(), any())).thenReturn(declaracionEjemplo());

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(BODY_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDeclaracion").value(30))
                .andExpect(jsonPath("$.materiasNombres[0]").value("Programación I"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void postInvalidoRetorna400() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(BODY_INVALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void postMateriaFueraDelPlanRetorna400ConPosicion() throws Exception {
        when(declaracionJuradaService.presentar(any(), any()))
                .thenThrow(new BusinessRuleException(
                        "La declaración jurada no pudo validarse; no se registró ninguna",
                        List.of(new com.academit.education.dto.common.FieldErrorDTO(
                                "materiasDeclaradasIds[1]",
                                "La materia Programación II no pertenece al plan de estudios del alumno"))));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(BODY_VALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").exists())
                .andExpect(jsonPath("$.errors[0].campo").value("materiasDeclaradasIds[1]"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void postSinCupoRetorna409() throws Exception {
        when(declaracionJuradaService.presentar(any(), any()))
                .thenThrow(new ConflictException(
                        "El alumno ya presentó una declaración jurada para el ciclo 2026"));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(BODY_VALIDO))
                .andExpect(status().isConflict());
    }

    @Test
    void postSinSesionRetorna401() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(BODY_VALIDO))
                .andExpect(status().isUnauthorized());
    }
}
