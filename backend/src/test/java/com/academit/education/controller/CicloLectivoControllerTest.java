package com.academit.education.controller;

import com.academit.education.dto.ciclo.CicloLectivoDTO;
import com.academit.education.service.CicloLectivoService;
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

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CicloLectivoControllerTest {

    private static final String BASE = "/api/ciclos-lectivos";

    private static final String BODY_VALIDO = """
            {
              "anio": 2027,
              "nombre": "Ciclo Lectivo 2027",
              "fechaInicio": "2027-03-01",
              "fechaFin": "2027-12-17"
            }
            """;

    private static final String BODY_INVALIDO = """
            {
              "nombre": "Ciclo Lectivo 2027",
              "fechaInicio": "2027-03-01",
              "fechaFin": "2027-12-17"
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CicloLectivoService cicloLectivoService;

    private CicloLectivoDTO dtoEjemplo() {
        CicloLectivoDTO dto = new CicloLectivoDTO();
        dto.setIdCiclo(1);
        dto.setAnio(2026);
        dto.setNombre("Ciclo Lectivo 2026");
        dto.setFechaInicio(LocalDate.of(2026, 3, 2));
        dto.setFechaFin(LocalDate.of(2026, 12, 18));
        dto.setEstado("ACTIVO");
        return dto;
    }

    @Test
    void getSinSesionRetorna401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401))
                .andExpect(jsonPath("$.message").value("Credenciales no provistas o inválidas"));
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void getConRolNoPermitidoRetorna403() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "administrador", roles = {"ADMINISTRATIVO"})
    void getAdministrativoRetornaListado() throws Exception {
        when(cicloLectivoService.listarCiclos()).thenReturn(List.of(dtoEjemplo()));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].anio").value(2026))
                .andExpect(jsonPath("$[0].estado").value("ACTIVO"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void postAdminRetorna201() throws Exception {
        when(cicloLectivoService.crearCiclo(org.mockito.ArgumentMatchers.any()))
                .thenReturn(dtoEjemplo());

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.anio").value(2026));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void postSinAnioRetorna400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_INVALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").isArray())
                .andExpect(jsonPath("$.errors[0].campo").value("anio"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void putInexistenteRetorna404() throws Exception {
        when(cicloLectivoService.actualizarCiclo(org.mockito.ArgumentMatchers.eq(99), org.mockito.ArgumentMatchers.any()))
                .thenThrow(new com.academit.education.exception.ResourceNotFoundException("Ciclo lectivo no encontrado: 99"));

        mockMvc.perform(put(BASE + "/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"CERRADO\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
