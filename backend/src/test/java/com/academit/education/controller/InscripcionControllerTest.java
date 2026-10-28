package com.academit.education.controller;

import com.academit.education.dto.inscripcion.InscripcionDTO;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.service.InscripcionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class InscripcionControllerTest {

    private static final String BASE = "/api/inscripciones";

    private static final String BODY_INSCRIPCION_VALIDO = """
            {
              "idAlumno": 1,
              "idCurso": 1,
              "cicloLectivo": 2026
            }
            """;

    private static final String BODY_INSCRIPCION_INVALIDO = """
            {
              "idAlumno": null,
              "idCurso": null
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private InscripcionService inscripcionService;

    private InscripcionDTO dtoEjemplo() {
        InscripcionDTO dto = new InscripcionDTO();
        dto.setIdInscripcion(1);
        dto.setIdAlumno(1);
        dto.setAlumnoNombre("Lucía Benítez");
        dto.setIdCurso(1);
        dto.setCursoNombre("1° Año - Turno Mañana");
        dto.setCicloLectivo(2026);
        dto.setFechaInscripcion(LocalDate.of(2026, 3, 2));
        dto.setEstado("CONFIRMADA");
        return dto;
    }

    @Test
    @DisplayName("GET /api/inscripciones sin autenticación retorna 401 Unauthorized")
    void testListarInscripciones_SinAuth_Retorna401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    @DisplayName("GET /api/inscripciones con rol ADMINISTRATIVO retorna 200 OK")
    void testListarInscripciones_Administrativo_Retorna200() throws Exception {
        when(inscripcionService.listarInscripciones(any())).thenReturn(List.of(dtoEjemplo()));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idInscripcion").value(1))
                .andExpect(jsonPath("$[0].alumnoNombre").value("Lucía Benítez"))
                .andExpect(jsonPath("$[0].cursoNombre").value("1° Año - Turno Mañana"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("GET /api/inscripciones/{id} inexistente retorna 404 Not Found")
    void testObtenerPorId_NoExiste_Retorna404() throws Exception {
        when(inscripcionService.obtenerPorId(eq(99), any()))
                .thenThrow(new ResourceNotFoundException("Inscripción no encontrada con ID: 99"));

        mockMvc.perform(get(BASE + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    @DisplayName("POST /api/inscripciones con rol ALUMNO retorna 403 Forbidden")
    void testCrearInscripcion_Alumno_Retorna403() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_INSCRIPCION_VALIDO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    @DisplayName("POST /api/inscripciones con rol ADMINISTRATIVO y datos válidos retorna 201 Created")
    void testCrearInscripcion_Administrativo_Retorna201() throws Exception {
        when(inscripcionService.crearInscripcion(any())).thenReturn(dtoEjemplo());

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_INSCRIPCION_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idInscripcion").value(1))
                .andExpect(jsonPath("$.alumnoNombre").value("Lucía Benítez"))
                .andExpect(jsonPath("$.estado").value("CONFIRMADA"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("POST /api/inscripciones con datos inválidos retorna 400 Bad Request")
    void testCrearInscripcion_Invalido_Retorna400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_INSCRIPCION_INVALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    @DisplayName("DELETE /api/inscripciones/{id} con rol ALUMNO retorna 403 Forbidden")
    void testCancelarInscripcion_Alumno_Retorna403() throws Exception {
        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    @DisplayName("DELETE /api/inscripciones/{id} con rol ADMINISTRATIVO retorna 204 No Content")
    void testCancelarInscripcion_Administrativo_Retorna204() throws Exception {
        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isNoContent());
    }
}

