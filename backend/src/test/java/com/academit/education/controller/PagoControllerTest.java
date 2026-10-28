package com.academit.education.controller;

import com.academit.education.dto.pago.PagoDTO;
import com.academit.education.exception.ConflictException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.service.PagoService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración de los endpoints de pagos (RF32, RF33, RN09).
 * Seguridad real habilitada y service mockeado con @MockitoBean.
 */
@SpringBootTest
@AutoConfigureMockMvc
class PagoControllerTest {

    private static final String BASE = "/api/pagos";

    private static final String PAGO_VALIDO = """
            { "idCuota": 10, "importe": 1250.00, "medioPago": "TARJETA" }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private PagoService pagoService;

    private PagoDTO pagoEjemplo() {
        return PagoDTO.builder()
                .idPago(3)
                .idCuota(10)
                .idAlumno(7)
                .alumnoNombre("Lucía Benítez")
                .concepto("Cuota 1 - Enero 2027")
                .importe(new BigDecimal("1250.00"))
                .fechaPago(LocalDate.of(2026, 10, 15))
                .medioPago("TARJETA")
                .comprobante("2026-000003")
                .estado("ACREDITADO")
                .build();
    }

    // ------------------------------------------------------------------
    // GET /api/pagos (RF33)
    // ------------------------------------------------------------------

    @Test
    void getSinSesionRetorna401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        verify(pagoService, never()).listarPagos(any(), any());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void getConRolNoPermitidoRetorna403() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verify(pagoService, never()).listarPagos(any(), any());
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void getAdministrativoRetornaHistorialConFiltro() throws Exception {
        when(pagoService.listarPagos(eq("administrativo"), eq(7))).thenReturn(List.of(pagoEjemplo()));

        mockMvc.perform(get(BASE).param("idAlumno", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comprobante").value("2026-000003"))
                .andExpect(jsonPath("$[0].estado").value("ACREDITADO"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void getAlumnoRetorna200() throws Exception {
        when(pagoService.listarPagos(eq("alumno1"), any())).thenReturn(List.of(pagoEjemplo()));

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
        verify(pagoService, never()).listarPagos(any(), any());
    }

    // ------------------------------------------------------------------
    // POST /api/pagos (RF32, RN09, RN10)
    // ------------------------------------------------------------------

    @Test
    void postSinSesionRetorna401() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAGO_VALIDO))
                .andExpect(status().isUnauthorized());
        verify(pagoService, never()).registrarPago(any(), any());
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void postConAlumnoRetorna403() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAGO_VALIDO))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verify(pagoService, never()).registrarPago(any(), any());
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void postPagoValidoRetorna201ConComprobante() throws Exception {
        when(pagoService.registrarPago(eq("administrativo"), any())).thenReturn(pagoEjemplo());

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAGO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.comprobante").value("2026-000003"))
                .andExpect(jsonPath("$.estado").value("ACREDITADO"));
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void postSinImporteRetorna400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"idCuota\": 10 }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("importe"));
        verify(pagoService, never()).registrarPago(any(), any());
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void postMedioPagoInvalidoRetorna400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"idCuota\": 10, \"importe\": 1250.00, \"medioPago\": \"BITCOIN\" }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("medioPago"));
        verify(pagoService, never()).registrarPago(any(), any());
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void postImporteDistintoDeLaCuotaRetorna400() throws Exception {
        when(pagoService.registrarPago(eq("administrativo"), any()))
                .thenThrow(new com.academit.education.exception.BusinessRuleException(
                        "El importe del pago ($600.00) debe ser igual al importe de la cuota ($1250.00). No se permiten pagos parciales (RN09)."));

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"idCuota\": 10, \"importe\": 600.00 }"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(org.hamcrest.Matchers.containsString("No se permiten pagos parciales")));
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void postCuotaInexistenteRetorna404() throws Exception {
        when(pagoService.registrarPago(eq("administrativo"), any()))
                .thenThrow(new ResourceNotFoundException("Cuota no encontrada con ID: 44"));

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{ \"idCuota\": 44, \"importe\": 1250.00 }"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void postCuotaYaPagadaRetorna409() throws Exception {
        when(pagoService.registrarPago(eq("administrativo"), any()))
                .thenThrow(new ConflictException("La cuota ya se encuentra pagada"));

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PAGO_VALIDO))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
