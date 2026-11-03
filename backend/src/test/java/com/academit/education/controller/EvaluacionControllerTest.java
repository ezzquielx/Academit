package com.academit.education.controller;

import com.academit.education.dto.calificacion.CalificacionDTO;
import com.academit.education.dto.calificacion.EvaluacionDTO;
import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.service.CalificacionService;
import com.academit.education.service.EvaluacionService;
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
class EvaluacionControllerTest {

    private static final String BASE = "/api/evaluaciones";

    private static final String BODY_EVALUACION_VALIDO = """
            {
              "idMateria": 9,
              "idCurso": 3,
              "fecha": "2026-09-15",
              "tipo": "PARCIAL",
              "descripcion": "Primer Parcial",
              "ponderacion": "40%"
            }
            """;

    private static final String BODY_EVALUACION_INVALIDO = """
            {
              "idMateria": null,
              "idCurso": null,
              "fecha": null,
              "tipo": "",
              "descripcion": ""
            }
            """;

    private static final String BODY_LOTE_VALIDO = """
            {
              "calificaciones": [
                { "idAlumno": 7, "nota": 8 },
                { "idAlumno": 8, "nota": 3, "observacion": "Le falta estudio" }
              ]
            }
            """;

    private static final String BODY_LOTE_INVALIDO = """
            {
              "calificaciones": [
                { "idAlumno": 7, "nota": 0 }
              ]
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private EvaluacionService evaluacionService;

    @MockitoBean
    private CalificacionService calificacionService;

    private EvaluacionDTO evaluacionEjemplo() {
        EvaluacionDTO dto = new EvaluacionDTO();
        dto.setIdEvaluacion(30);
        dto.setIdMateria(9);
        dto.setMateriaNombre("Programación II");
        dto.setIdCurso(3);
        dto.setCursoNombre("1° Año A");
        dto.setFecha("2026-09-15");
        dto.setTipo("PARCIAL");
        dto.setDescripcion("Primer Parcial");
        return dto;
    }

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
        when(evaluacionService.listar(any(), any(), any())).thenReturn(List.of());
        mockMvc.perform(get(BASE)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void getEvaluacionesConDocenteRetorna200() throws Exception {
        when(evaluacionService.listar(any(), any(), any()))
                .thenReturn(List.of(evaluacionEjemplo()));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idEvaluacion").value(30))
                .andExpect(jsonPath("$[0].materiaNombre").value("Programación II"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void getEvaluacionesConAlumnoRetorna403() throws Exception {
        mockMvc.perform(get(BASE)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void postEvaluacionRetorna201() throws Exception {
        when(evaluacionService.crear(any(), any())).thenReturn(evaluacionEjemplo());

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_EVALUACION_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.descripcion").value("Primer Parcial"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void postEvaluacionConAlumnoRetorna403() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_EVALUACION_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void postEvaluacionInvalidaRetorna400() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_EVALUACION_INVALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void postCalificacionesLoteRetorna200() throws Exception {
        when(calificacionService.guardarLote(any(), eq(30), any()))
                .thenReturn(List.of(calificacionEjemplo()));

        mockMvc.perform(post(BASE + "/30/calificaciones")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY_LOTE_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].condicion").value("APROBADO"));
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void postCalificacionesLoteInvalidoRetorna400() throws Exception {
        mockMvc.perform(post(BASE + "/30/calificaciones")
                        .contentType(MediaType.APPLICATION_JSON).content(BODY_LOTE_INVALIDO))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void postEvaluacionCatedraInexistenteRetorna404() throws Exception {
        when(evaluacionService.crear(any(), any()))
                .thenThrow(new ResourceNotFoundException("No existe una cátedra para el curso 3"));

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_EVALUACION_VALIDO))
                .andExpect(status().isNotFound());
    }
}
