package com.academit.education.controller;

import com.academit.education.dto.documentacion.DocumentacionDTO;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.service.DocumentacionService;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración de los endpoints de documentación (RF56-RF58).
 * Seguridad real habilitada y service mockeado con @MockitoBean.
 */
@SpringBootTest
@AutoConfigureMockMvc
class DocumentacionControllerTest {

    private static final String BASE = "/api/documentacion";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private DocumentacionService documentacionService;

    private DocumentacionDTO documentoEjemplo() {
        return DocumentacionDTO.builder()
                .idDocumento(1)
                .idAlumno(7)
                .alumnoNombre("Lucía Benítez")
                .tipo("DNI")
                .obligatorio(true)
                .estado("PENDIENTE")
                .build();
    }

    // ------------------------------------------------------------------
    // GET /api/documentacion (RF56)
    // ------------------------------------------------------------------

    @Test
    void getSinSesionRetorna401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        verify(documentacionService, never()).listarDocumentacion(any(), any());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void getConRolNoPermitidoRetorna403() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verify(documentacionService, never()).listarDocumentacion(any(), any());
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void getAdministrativoRetornaLegajoConFiltro() throws Exception {
        when(documentacionService.listarDocumentacion(eq("administrativo"), eq(7)))
                .thenReturn(List.of(documentoEjemplo()));

        mockMvc.perform(get(BASE).param("idAlumno", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].tipo").value("DNI"))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void getAlumnoRetorna200() throws Exception {
        when(documentacionService.listarDocumentacion(eq("alumno1"), any()))
                .thenReturn(List.of(documentoEjemplo()));

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
        verify(documentacionService, never()).listarDocumentacion(any(), any());
    }

    // ------------------------------------------------------------------
    // PUT /api/documentacion/{id} (RF57, RF58)
    // ------------------------------------------------------------------

    @Test
    void putSinSesionRetorna401() throws Exception {
        mockMvc.perform(put(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"ENTREGADO\"}"))
                .andExpect(status().isUnauthorized());
        verify(documentacionService, never()).actualizarDocumentacion(any(), any());
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void putConAlumnoRetorna403() throws Exception {
        mockMvc.perform(put(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"ENTREGADO\"}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verify(documentacionService, never()).actualizarDocumentacion(any(), any());
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void putEstadoValidoRetorna200() throws Exception {
        DocumentacionDTO actualizado = DocumentacionDTO.builder()
                .idDocumento(1).idAlumno(7).alumnoNombre("Lucía Benítez")
                .tipo("DNI").obligatorio(true).estado("ENTREGADO")
                .fechaEntrega(LocalDate.of(2026, 10, 15))
                .build();
        when(documentacionService.actualizarDocumentacion(eq(1), any())).thenReturn(actualizado);

        mockMvc.perform(put(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"ENTREGADO\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ENTREGADO"))
                .andExpect(jsonPath("$.fechaEntrega").value("2026-10-15"));
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void putSinEstadoRetorna400() throws Exception {
        mockMvc.perform(put(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("estado"));
        verify(documentacionService, never()).actualizarDocumentacion(any(), any());
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void putEstadoInvalidoRetorna400() throws Exception {
        mockMvc.perform(put(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"VENCIDO\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("estado"));
        verify(documentacionService, never()).actualizarDocumentacion(any(), any());
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void putDocumentoInexistenteRetorna404() throws Exception {
        when(documentacionService.actualizarDocumentacion(eq(44), any()))
                .thenThrow(new ResourceNotFoundException("Documento no encontrado con ID: 44"));

        mockMvc.perform(put(BASE + "/44")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"ENTREGADO\"}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }
}
