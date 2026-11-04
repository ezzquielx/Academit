package com.academit.education.controller;

import com.academit.education.dto.solicitud.SolicitudDTO;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.service.SolicitudService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración de los endpoints de solicitudes (RF53-RF55).
 * Seguridad real habilitada y service mockeado con @MockitoBean.
 */
@SpringBootTest
@AutoConfigureMockMvc
class SolicitudControllerTest {

    private static final String BASE = "/api/solicitudes";

    private static final String CREATE_VALIDO = """
            { "idAlumno": 7, "tipo": "CONSTANCIA", "motivo": "Necesito una constancia" }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SolicitudService solicitudService;

    private SolicitudDTO solicitudEjemplo() {
        return SolicitudDTO.builder()
                .idSolicitud(1)
                .idAlumno(7)
                .alumnoNombre("Lucía Benítez")
                .tipo("CONSTANCIA")
                .fechaSolicitud(LocalDate.of(2026, 10, 15))
                .motivo("Necesito una constancia")
                .estado("PENDIENTE")
                .build();
    }

    // ------------------------------------------------------------------
    // GET /api/solicitudes (RF53)
    // ------------------------------------------------------------------

    @Test
    void getSinSesionRetorna401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        verify(solicitudService, never()).listarSolicitudes(any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void getConRolNoPermitidoRetorna403() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verify(solicitudService, never()).listarSolicitudes(any(), any(), any(), any());
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void getAdministrativoRetornaConFiltros() throws Exception {
        when(solicitudService.listarSolicitudes(eq("administrativo"), isNull(), eq("CONSTANCIA"), eq("PENDIENTE")))
                .thenReturn(List.of(solicitudEjemplo()));

        mockMvc.perform(get(BASE)
                        .param("tipo", "CONSTANCIA")
                        .param("estado", "PENDIENTE"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("CONSTANCIA"))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void getAlumnoRetorna200() throws Exception {
        when(solicitudService.listarSolicitudes(eq("alumno1"), any(), any(), any()))
                .thenReturn(List.of(solicitudEjemplo()));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].alumnoNombre").value("Lucía Benítez"));
    }

    @Test
    @WithMockUser(username = "administrador", roles = {"ADMINISTRADOR"})
    void getConIdAlumnoNoNumericoRetorna400() throws Exception {
        mockMvc.perform(get(BASE).param("idAlumno", "abc"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400));
        verify(solicitudService, never()).listarSolicitudes(any(), any(), any(), any());
    }

    // ------------------------------------------------------------------
    // POST /api/solicitudes (RF54)
    // ------------------------------------------------------------------

    @Test
    void postSinSesionRetorna401() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_VALIDO))
                .andExpect(status().isUnauthorized());
        verify(solicitudService, never()).crearSolicitud(any(), any());
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void postConRolAdministrativoRetorna403() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_VALIDO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verify(solicitudService, never()).crearSolicitud(any(), any());
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void postSolicitudValidaRetorna201() throws Exception {
        when(solicitudService.crearSolicitud(eq("alumno1"), any())).thenReturn(solicitudEjemplo());

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(CREATE_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.estado").value("PENDIENTE"))
                .andExpect(jsonPath("$.tipo").value("CONSTANCIA"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void postSinMotivoRetorna400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"idAlumno\": 7, \"tipo\": \"CONSTANCIA\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("motivo"));
        verify(solicitudService, never()).crearSolicitud(any(), any());
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void postConTipoInvalidoRetorna400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"idAlumno\": 7, \"tipo\": \"DDJJ\", \"motivo\": \"Trámite\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("tipo"));
        verify(solicitudService, never()).crearSolicitud(any(), any());
    }

    // ------------------------------------------------------------------
    // PATCH /api/solicitudes/{id} (RF55)
    // ------------------------------------------------------------------

    @Test
    void patchSinSesionRetorna401() throws Exception {
        mockMvc.perform(patch(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"APROBADA\"}"))
                .andExpect(status().isUnauthorized());
        verify(solicitudService, never()).resolverSolicitud(any(), any());
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void patchConAlumnoRetorna403() throws Exception {
        mockMvc.perform(patch(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"APROBADA\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verify(solicitudService, never()).resolverSolicitud(any(), any());
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void patchResolverRetorna200ConFecha() throws Exception {
        SolicitudDTO resuelta = SolicitudDTO.builder()
                .idSolicitud(1).idAlumno(7).alumnoNombre("Lucía Benítez")
                .tipo("CONSTANCIA").fechaSolicitud(LocalDate.of(2026, 10, 1))
                .motivo("Necesito una constancia").estado("APROBADA")
                .fechaResolucion(LocalDate.of(2026, 10, 15))
                .respuesta("Solicitud aprobada por el área administrativa")
                .build();
        when(solicitudService.resolverSolicitud(eq(1), any())).thenReturn(resuelta);

        mockMvc.perform(patch(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"APROBADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("APROBADA"))
                .andExpect(jsonPath("$.fechaResolucion").value("2026-10-15"));
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void patchConEstadoInvalidoRetorna400() throws Exception {
        mockMvc.perform(patch(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"BAJA\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("estado"));
        verify(solicitudService, never()).resolverSolicitud(any(), any());
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void patchSolicitudInexistenteRetorna404() throws Exception {
        when(solicitudService.resolverSolicitud(eq(44), any()))
                .thenThrow(new ResourceNotFoundException("Solicitud no encontrada con ID: 44"));

        mockMvc.perform(patch(BASE + "/44")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"APROBADA\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
