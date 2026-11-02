package com.academit.education.controller;

import com.academit.education.dto.curso.CursoDTO;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.service.CarreraService;
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
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CarreraControllerTest {

    private static final String BASE = "/api/carreras";

    private static final String BODY_VALIDO = """
            {
              "codigo": "IA",
              "nombre": "Inteligencia Artificial",
              "tituloOtorgado": "Técnico en Inteligencia Artificial",
              "duracionAnios": 4
            }
            """;

    private static final String BODY_INVALIDO = """
            {
              "nombre": "Inteligencia Artificial",
              "duracionAnios": 4
            }
            """;

    private static final String BODY_DURACION_INVALIDA = """
            {
              "codigo": "IA",
              "nombre": "Inteligencia Artificial",
              "tituloOtorgado": "Técnico en Inteligencia Artificial",
              "duracionAnios": 0
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CarreraService carreraService;

    // ------------------------------------------------------------------
    // 401 / 403
    // ------------------------------------------------------------------

    @Test
    void getSinSesionRetorna401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void getConDocenteRetorna403() throws Exception {
        mockMvc.perform(get(BASE)).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void postConDocenteRetorna403() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void deleteConDocenteRetorna403() throws Exception {
        mockMvc.perform(delete(BASE + "/1")).andExpect(status().isForbidden());
    }

    // ------------------------------------------------------------------
    // GET (listado, detalle, planes, cursos)
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRATIVO"})
    void getListadoRetorna200() throws Exception {
        when(carreraService.listarCarreras()).thenReturn(List.of());

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRATIVO"})
    void getDetalleInexistenteRetorna404() throws Exception {
        when(carreraService.obtenerPorId(99))
                .thenThrow(new ResourceNotFoundException("Carrera no encontrada con ID: 99"));

        mockMvc.perform(get(BASE + "/99")).andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRATIVO"})
    void getPlanesRetorna200() throws Exception {
        when(carreraService.listarPlanesDeCarrera(1)).thenReturn(List.of());

        mockMvc.perform(get(BASE + "/1/planes"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRATIVO"})
    void getCursosRetorna200() throws Exception {
        CursoDTO dto = new CursoDTO();
        dto.setIdCurso(3);
        dto.setInscriptos(29);
        dto.setCuposDisponibles(6);
        when(carreraService.listarCursosDeCarrera(1)).thenReturn(List.of(dto));

        mockMvc.perform(get(BASE + "/1/cursos"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idCurso").value(3))
                .andExpect(jsonPath("$[0].cuposDisponibles").value(6));
    }

    // ------------------------------------------------------------------
    // POST / PUT
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void postRetorna201() throws Exception {
        when(carreraService.crearCarrera(any())).thenReturn(carreraEjemplo());

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.codigo").value("TUP"))
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void postSinCodigoRetorna400() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_INVALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void postConDuracionCeroRetorna400() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_DURACION_INVALIDA))
                .andExpect(status().isBadRequest());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void putRetorna200() throws Exception {
        when(carreraService.actualizarCarrera(eq(1), any())).thenReturn(carreraEjemplo());

        mockMvc.perform(put(BASE + "/1").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"ACTIVA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCarrera").value(1));
    }

    // ------------------------------------------------------------------
    // DELETE (baja lógica: 204)
    // ------------------------------------------------------------------

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void deleteRetorna204() throws Exception {
        mockMvc.perform(delete(BASE + "/1")).andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void deleteInexistenteRetorna404() throws Exception {
        doThrow(new ResourceNotFoundException("Carrera no encontrada con ID: 99"))
                .when(carreraService).darDeBajaCarrera(99);

        mockMvc.perform(delete(BASE + "/99")).andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------

    private com.academit.education.dto.carrera.CarreraDTO carreraEjemplo() {
        com.academit.education.dto.carrera.CarreraDTO dto =
                new com.academit.education.dto.carrera.CarreraDTO();
        dto.setIdCarrera(1);
        dto.setCodigo("TUP");
        dto.setNombre("Tecnicatura Universitaria en Programación");
        dto.setTituloOtorgado("Técnico Universitario en Programación");
        dto.setDuracionAnios(3);
        dto.setEstado("ACTIVA");
        return dto;
    }
}
