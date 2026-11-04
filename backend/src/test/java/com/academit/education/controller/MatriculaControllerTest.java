package com.academit.education.controller;

import com.academit.education.dto.cuota.CuotaDTO;
import com.academit.education.dto.common.ApiResponseDTO;
import com.academit.education.dto.matricula.MatriculaDTO;
import com.academit.education.exception.ConflictException;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.service.MatriculaService;
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
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class MatriculaControllerTest {

    private static final String BASE = "/api/matriculas";

    private static final String PUT_VALIDO = """
            { "estado": "ACTIVA" }
            """;

    private static final String PUT_INVALIDO = """
            { "estado": "BAJA" }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MatriculaService matriculaService;

    private MatriculaDTO matriculaEjemplo() {
        return MatriculaDTO.builder()
                .idMatricula(1)
                .idAlumno(7)
                .alumnoNombre("Lucía Benítez")
                .idCarrera(3)
                .carreraNombre("Tecnicatura Universitaria en Programación")
                .idCiclo(2)
                .cicloLectivoNombre("Ciclo Lectivo 2026")
                .cicloLectivo(2026)
                .fecha(LocalDate.of(2026, 3, 2))
                .estado("ACTIVA")
                .estadoPago("EN_MORA")
                .build();
    }

    private CuotaDTO cuotaEjemplo() {
        return CuotaDTO.builder()
                .idCuota(1)
                .idMatricula(1)
                .idAlumno(7)
                .alumnoNombre("Lucía Benítez")
                .concepto("Cuota 1 - Enero 2026")
                .mes(1)
                .anio(2026)
                .fechaVencimiento(LocalDate.of(2026, 1, 10))
                .importe(new BigDecimal("1250.00"))
                .tipo("MENSUAL")
                .estado("PENDIENTE")
                .build();
    }

    // ------------------------------------------------------------------
    // GET /api/matriculas
    // ------------------------------------------------------------------

    @Test
    void getSinSesionRetorna401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void getConRolNoPermitidoRetorna403() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void getAdministrativoRetornaPadron() throws Exception {
        when(matriculaService.listarMatriculas()).thenReturn(List.of(matriculaEjemplo()));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idMatricula").value(1))
                .andExpect(jsonPath("$[0].carreraNombre").value("Tecnicatura Universitaria en Programación"))
                .andExpect(jsonPath("$[0].estadoPago").value("EN_MORA"));
    }

    // ------------------------------------------------------------------
    // PUT /api/matriculas/{id}
    // ------------------------------------------------------------------

    @Test
    void putSinSesionRetorna401() throws Exception {
        mockMvc.perform(put(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PUT_VALIDO))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void putConAlumnoRetorna403() throws Exception {
        mockMvc.perform(put(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PUT_VALIDO))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void putEstadoValidoRetorna200() throws Exception {
        when(matriculaService.actualizarMatricula(eq(1), any())).thenReturn(matriculaEjemplo());

        mockMvc.perform(put(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PUT_VALIDO))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.estado").value("ACTIVA"));
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void putEstadoInvalidoRetorna400() throws Exception {
        mockMvc.perform(put(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PUT_INVALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("estado"));
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void putInexistenteRetorna404() throws Exception {
        when(matriculaService.actualizarMatricula(eq(99), any()))
                .thenThrow(new ResourceNotFoundException("Matrícula no encontrada con ID: 99"));

        mockMvc.perform(put(BASE + "/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(PUT_VALIDO))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // PATCH /api/matriculas/{id} (RF27)
    // ------------------------------------------------------------------

    @Test
    void patchSinSesionRetorna401() throws Exception {
        mockMvc.perform(patch(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"Baja\"}"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    void patchConRolNoPermitidoRetorna403() throws Exception {
        mockMvc.perform(patch(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void patchAlumnoCancelaSuMatriculaRetorna200() throws Exception {
        when(matriculaService.cancelarMatricula(eq(1), eq("alumno1"), any()))
                .thenReturn(ApiResponseDTO.ok("Matrícula cancelada"));

        mockMvc.perform(patch(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"estado\": \"CANCELADA\"}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void patchMotivoLargoRetorna400() throws Exception {
        String motivoLargo = "x".repeat(300);

        mockMvc.perform(patch(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"motivo\": \"" + motivoLargo + "\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0].campo").value("motivo"));
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void patchInexistenteRetorna404() throws Exception {
        when(matriculaService.cancelarMatricula(eq(99), eq("administrativo"), any()))
                .thenThrow(new ResourceNotFoundException("Matrícula no encontrada con ID: 99"));

        mockMvc.perform(patch(BASE + "/99")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isNotFound());
    }

    // ------------------------------------------------------------------
    // POST /api/matriculas/{id}/cuotas (RN18)
    // ------------------------------------------------------------------

    @Test
    void postCuotasSinSesionRetorna401() throws Exception {
        mockMvc.perform(post(BASE + "/1/cuotas"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    void postCuotasConAlumnoRetorna403() throws Exception {
        mockMvc.perform(post(BASE + "/1/cuotas"))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void postCuotasRetorna200Con15Cuotas() throws Exception {
        when(matriculaService.generarCuotas(1)).thenReturn(List.of(cuotaEjemplo()));

        mockMvc.perform(post(BASE + "/1/cuotas"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].concepto").value("Cuota 1 - Enero 2026"))
                .andExpect(jsonPath("$[0].tipo").value("MENSUAL"));
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void postCuotasInexistenteRetorna404() throws Exception {
        when(matriculaService.generarCuotas(44))
                .thenThrow(new ResourceNotFoundException("Matrícula no encontrada con ID: 44"));

        mockMvc.perform(post(BASE + "/44/cuotas"))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    void postCuotasYaGeneradasRetorna409() throws Exception {
        when(matriculaService.generarCuotas(1))
                .thenThrow(new ConflictException("La matrícula 1 ya tiene cuotas generadas"));

        mockMvc.perform(post(BASE + "/1/cuotas"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409));
    }
}
