package com.academit.education.controller;

import com.academit.education.dto.asignacion.AsignacionDTO;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.service.AsignacionService;
import org.junit.jupiter.api.DisplayName;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AsignacionControllerTest {

    private static final String BASE = "/api/asignaciones";

    private static final String BODY_ASIGNACION_VALIDO = """
            {
              "idDocente": 1,
              "idMateria": 1,
              "idCurso": 1,
              "rolDocente": "TITULAR"
            }
            """;

    private static final String BODY_ASIGNACION_INVALIDO = """
            {
              "idDocente": null,
              "idMateria": null
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AsignacionService asignacionService;

    private AsignacionDTO dtoEjemplo() {
        AsignacionDTO dto = new AsignacionDTO();
        dto.setIdAsignacion(1);
        dto.setIdDocente(1);
        dto.setDocenteNombre("Carlos Sánchez");
        dto.setIdMateria(1);
        dto.setMateriaNombre("Programación I");
        dto.setIdCurso(1);
        dto.setCursoNombre("1° Año - Turno Mañana");
        dto.setRolDocente("TITULAR");
        return dto;
    }

    @Test
    @DisplayName("GET /api/asignaciones sin autenticación retorna 401 Unauthorized")
    void testListarAsignaciones_SinAuth_Retorna401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    @DisplayName("GET /api/asignaciones con rol ALUMNO retorna 403 Forbidden")
    void testListarAsignaciones_RolAlumno_Retorna403() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("GET /api/asignaciones con rol ADMINISTRADOR retorna 200 OK y listado")
    void testListarAsignaciones_Admin_Retorna200() throws Exception {
        when(asignacionService.listarAsignaciones()).thenReturn(List.of(dtoEjemplo()));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idAsignacion").value(1))
                .andExpect(jsonPath("$[0].docenteNombre").value("Carlos Sánchez"))
                .andExpect(jsonPath("$[0].materiaNombre").value("Programación I"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("GET /api/asignaciones/{id} inexistente retorna 404 Not Found")
    void testObtenerPorId_NoExiste_Retorna404() throws Exception {
        when(asignacionService.obtenerPorId(99))
                .thenThrow(new ResourceNotFoundException("Asignación no encontrada con ID: 99"));

        mockMvc.perform(get(BASE + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    @DisplayName("POST /api/asignaciones con rol ADMINISTRATIVO retorna 403 Forbidden")
    void testCrearAsignacion_Administrativo_Retorna403() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_ASIGNACION_VALIDO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("POST /api/asignaciones con rol ADMINISTRADOR y datos válidos retorna 201 Created")
    void testCrearAsignacion_Admin_Retorna201() throws Exception {
        when(asignacionService.crearAsignacion(any())).thenReturn(dtoEjemplo());

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_ASIGNACION_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAsignacion").value(1))
                .andExpect(jsonPath("$.docenteNombre").value("Carlos Sánchez"))
                .andExpect(jsonPath("$.materiaNombre").value("Programación I"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("POST /api/asignaciones con datos inválidos retorna 400 Bad Request")
    void testCrearAsignacion_Invalido_Retorna400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_ASIGNACION_INVALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    @DisplayName("DELETE /api/asignaciones/{id} con rol ADMINISTRATIVO retorna 403 Forbidden")
    void testEliminarAsignacion_Administrativo_Retorna403() throws Exception {
        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("DELETE /api/asignaciones/{id} con rol ADMINISTRADOR retorna 204 No Content")
    void testEliminarAsignacion_Admin_Retorna204() throws Exception {
        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isNoContent());
    }
}

