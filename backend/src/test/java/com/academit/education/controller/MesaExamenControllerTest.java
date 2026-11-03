package com.academit.education.controller;

import com.academit.education.dto.mesa.MesaExamenDTO;
import com.academit.education.exception.ConflictException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.service.MesaExamenService;
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
class MesaExamenControllerTest {

    private static final String BASE = "/api/mesas-examen";

    private static final String BODY_VALIDO = """
            {
              "idMateria": 9,
              "idCarrera": 1,
              "fecha": "2026-12-10",
              "hora": "09:00 hs",
              "turno": "MANANA",
              "tribunal": "Prof. Carlos Gómez",
              "aula": "Aula Magna 1",
              "cupoMaximo": 30
            }
            """;

    private static final String BODY_INVALIDO = """
            {
              "idMateria": null,
              "idCarrera": null,
              "fecha": null,
              "turno": "MANANA",
              "cupoMaximo": 0
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MesaExamenService mesaExamenService;

    private MesaExamenDTO mesaEjemplo() {
        MesaExamenDTO dto = new MesaExamenDTO();
        dto.setIdMesa(3);
        dto.setIdMateria(9);
        dto.setMateriaNombre("Programación II");
        dto.setIdCarrera(1);
        dto.setCarreraNombre("Tecnicatura Universitaria en Programación");
        dto.setFecha("2026-12-10");
        dto.setHora("09:00 hs");
        dto.setTurno("MANANA");
        dto.setCupoMaximo(30);
        dto.setInscriptos(12);
        dto.setCuposDisponibles(18);
        dto.setEstado("ABIERTA");
        return dto;
    }

    @Test
    void getSinSesionRetorna401() throws Exception {
        when(mesaExamenService.listar()).thenReturn(List.of());
        mockMvc.perform(get(BASE)).andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void getConAlumnoRetorna200() throws Exception {
        when(mesaExamenService.listar()).thenReturn(List.of(mesaEjemplo()));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idMesa").value(3))
                .andExpect(jsonPath("$[0].cuposDisponibles").value(18));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void postRetorna201() throws Exception {
        when(mesaExamenService.crear(any())).thenReturn(mesaEjemplo());

        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(BODY_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("ABIERTA"));
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void postConDocenteRetorna403() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(BODY_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void postInvalidoRetorna400() throws Exception {
        mockMvc.perform(post(BASE).contentType(MediaType.APPLICATION_JSON).content(BODY_INVALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors").exists());
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    void putInexistenteRetorna404() throws Exception {
        when(mesaExamenService.actualizar(eq(99), any()))
                .thenThrow(new ResourceNotFoundException("Mesa de examen no encontrada con ID: 99"));

        mockMvc.perform(put(BASE + "/99").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"CERRADA\"}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void postInscripcionRetorna200() throws Exception {
        when(mesaExamenService.inscribir(any(), eq(3))).thenReturn(mesaEjemplo());

        mockMvc.perform(post(BASE + "/3/inscripciones"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.inscriptos").value(12));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void postInscripcionMesaLlenaRetorna409() throws Exception {
        when(mesaExamenService.inscribir(any(), eq(3)))
                .thenThrow(new ConflictException("Sin cupos disponibles: la mesa ya tiene 30 inscriptos"));

        mockMvc.perform(post(BASE + "/3/inscripciones"))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void deleteInscripcionRetorna200() throws Exception {
        when(mesaExamenService.darDeBaja(any(), eq(3), eq(50))).thenReturn(mesaEjemplo());

        mockMvc.perform(delete(BASE + "/3/inscripciones/50"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idMesa").value(3));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void deleteInscripcionDeOtroRetorna403() throws Exception {
        when(mesaExamenService.darDeBaja(any(), eq(3), eq(51)))
                .thenThrow(new org.springframework.security.access.AccessDeniedException(
                        "El alumno solo puede darse de baja de su propia inscripción"));

        mockMvc.perform(delete(BASE + "/3/inscripciones/51"))
                .andExpect(status().isForbidden());
    }

    @Test
    void postInscripcionSinSesionRetorna401() throws Exception {
        doThrow(new RuntimeException()).when(mesaExamenService).inscribir(any(), eq(3));
        // sin sesión: el filtro de seguridad antecede al controller
        mockMvc.perform(post(BASE + "/3/inscripciones"))
                .andExpect(status().isUnauthorized());
    }
}
