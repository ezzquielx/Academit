package com.academit.education.controller;

import com.academit.education.dto.cuota.CuotaDTO;
import com.academit.education.exception.ConflictException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.service.CuotaService;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Pruebas de integración de los endpoints de cuotas (RF28-RF31).
 * Seguridad real habilitada y service mockeado con @MockitoBean.
 */
@SpringBootTest
@AutoConfigureMockMvc
class CuotaControllerTest {

    private static final String BASE = "/api/cuotas";

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CuotaService cuotaService;

    private CuotaDTO cuotaEjemplo() {
        return CuotaDTO.builder()
                .idCuota(10)
                .idMatricula(1)
                .idAlumno(7)
                .alumnoNombre("Lucía Benítez")
                .concepto("Cuota 1 - Enero 2026")
                .mes(1)
                .anio(2026)
                .fechaVencimiento(LocalDate.of(2027, 1, 10))
                .importe(new BigDecimal("1250.00"))
                .tipo("MENSUAL")
                .estado("PENDIENTE")
                .build();
    }

    // ------------------------------------------------------------------
    // GET /api/cuotas (RF28-RF31)
    // ------------------------------------------------------------------

    @Test
    void getSinSesionRetorna401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
        verify(cuotaService, never()).listarCuotas(any(), any());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void getConRolNoPermitidoRetorna403() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verify(cuotaService, never()).listarCuotas(any(), any());
    }

    @Test
    @WithMockUser(username = "administrador", roles = {"ADMINISTRADOR"})
    void getAdministradorRetornaTodasConFiltroDeAlumno() throws Exception {
        when(cuotaService.listarCuotas(eq("administrador"), eq(7))).thenReturn(List.of(cuotaEjemplo()));

        mockMvc.perform(get(BASE).param("idAlumno", "7"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idCuota").value(10))
                .andExpect(jsonPath("$[0].estado").value("PENDIENTE"));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void getAlumnoRetorna200() throws Exception {
        when(cuotaService.listarCuotas(eq("alumno1"), any())).thenReturn(List.of(cuotaEjemplo()));

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
        verify(cuotaService, never()).listarCuotas(any(), any());
    }

    // ------------------------------------------------------------------
    // PATCH /api/cuotas/{id}/anular (RF31, RN07)
    // ------------------------------------------------------------------

    @Test
    void patchSinSesionRetorna401() throws Exception {
        mockMvc.perform(patch(BASE + "/10/anular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isUnauthorized());
        verify(cuotaService, never()).anularCuota(any());
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void patchConRolNoPermitidoRetorna403() throws Exception {
        mockMvc.perform(patch(BASE + "/10/anular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
        verify(cuotaService, never()).anularCuota(any());
    }

    @Test
    @WithMockUser(username = "administrador", roles = {"ADMINISTRADOR"})
    void patchAnularRetorna200() throws Exception {
        when(cuotaService.anularCuota(10)).thenReturn(cuotaEjemplo());

        mockMvc.perform(patch(BASE + "/10/anular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idCuota").value(10))
                .andExpect(jsonPath("$.estado").value("PENDIENTE"));
    }

    @Test
    @WithMockUser(username = "administrador", roles = {"ADMINISTRADOR"})
    void patchCuotaInexistenteRetorna404() throws Exception {
        when(cuotaService.anularCuota(44))
                .thenThrow(new ResourceNotFoundException("Cuota no encontrada con ID: 44"));

        mockMvc.perform(patch(BASE + "/44/anular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(username = "administrador", roles = {"ADMINISTRADOR"})
    void patchCuotaPagadaRetorna409() throws Exception {
        when(cuotaService.anularCuota(10))
                .thenThrow(new ConflictException("No se puede anular una cuota que ya se encuentra pagada"));

        mockMvc.perform(patch(BASE + "/10/anular")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
