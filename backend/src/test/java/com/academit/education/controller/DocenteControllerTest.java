package com.academit.education.controller;

import com.academit.education.dto.docente.DocenteDTO;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.service.DocenteService;
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
class DocenteControllerTest {

    private static final String BASE = "/api/docentes";

    private static final String BODY_DOCENTE_VALIDO = """
            {
              "nombre": "Carlos",
              "apellido": "Sánchez",
              "dni": "30111222",
              "email": "carlos.sanchez@academit.edu.ar",
              "telefono": "1144332211",
              "titulo": "Licenciado en Sistemas"
            }
            """;

    private static final String BODY_DOCENTE_INVALIDO = """
            {
              "nombre": "",
              "apellido": "S",
              "dni": "abc",
              "email": "invalido"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocenteService docenteService;

    private DocenteDTO dtoEjemplo() {
        DocenteDTO dto = new DocenteDTO();
        dto.setIdDocente(1);
        dto.setLegajo("DOC-201");
        dto.setNombre("Carlos");
        dto.setApellido("Sánchez");
        dto.setDni("30111222");
        dto.setEmail("carlos.sanchez@academit.edu.ar");
        dto.setTelefono("1144332211");
        dto.setTitulo("Licenciado en Sistemas");
        dto.setEstado("ACTIVO");
        return dto;
    }

    @Test
    @DisplayName("GET /api/docentes sin autenticación retorna 401 Unauthorized")
    void testListarDocentes_SinAuth_Retorna401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    @DisplayName("GET /api/docentes con rol ALUMNO retorna 403 Forbidden")
    void testListarDocentes_RolAlumno_Retorna403() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    @DisplayName("GET /api/docentes con rol ADMINISTRATIVO retorna 200 OK")
    void testListarDocentes_Administrativo_Retorna200() throws Exception {
        when(docenteService.listarDocentes()).thenReturn(List.of(dtoEjemplo()));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idDocente").value(1))
                .andExpect(jsonPath("$[0].legajo").value("DOC-201"))
                .andExpect(jsonPath("$[0].nombre").value("Carlos"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("GET /api/docentes/{id} inexistente retorna 404 Not Found")
    void testObtenerPorId_NoExiste_Retorna404() throws Exception {
        when(docenteService.obtenerPorId(99))
                .thenThrow(new ResourceNotFoundException("Docente no encontrado con ID: 99"));

        mockMvc.perform(get(BASE + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    @DisplayName("POST /api/docentes con rol ADMINISTRATIVO retorna 403 Forbidden")
    void testCrearDocente_Administrativo_Retorna403() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_DOCENTE_VALIDO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("POST /api/docentes con rol ADMINISTRADOR y datos válidos retorna 201 Created")
    void testCrearDocente_Admin_Retorna201() throws Exception {
        when(docenteService.crearDocente(any())).thenReturn(dtoEjemplo());

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_DOCENTE_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idDocente").value(1))
                .andExpect(jsonPath("$.legajo").value("DOC-201"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("POST /api/docentes con datos inválidos retorna 400 Bad Request")
    void testCrearDocente_Invalido_Retorna400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_DOCENTE_INVALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("PUT /api/docentes/{id} con datos válidos retorna 200 OK")
    void testActualizarDocente_Admin_Retorna200() throws Exception {
        when(docenteService.actualizarDocente(eq(1), any())).thenReturn(dtoEjemplo());

        String bodyUpdate = """
                {
                  "nombre": "Carlos Alberto",
                  "telefono": "1199001122"
                }
                """;

        mockMvc.perform(put(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyUpdate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idDocente").value(1));
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    @DisplayName("DELETE /api/docentes/{id} con rol ADMINISTRATIVO retorna 403 Forbidden")
    void testEliminarDocente_Administrativo_Retorna403() throws Exception {
        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("DELETE /api/docentes/{id} con rol ADMINISTRADOR retorna 204 No Content")
    void testEliminarDocente_Admin_Retorna204() throws Exception {
        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isNoContent());
    }
}

