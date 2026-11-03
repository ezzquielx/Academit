package com.academit.education.controller;

import com.academit.education.dto.alumno.AlumnoDTO;
import com.academit.education.dto.alumno.SituacionAcademicaDTO;
import com.academit.education.dto.alumno.SituacionAdministrativaDTO;
import com.academit.education.exception.ResourceNotFoundException;
import com.academit.education.service.AlumnoService;
import org.junit.jupiter.api.DisplayName;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
class AlumnoControllerTest {

    private static final String BASE = "/api/alumnos";

    private static final String BODY_ALUMNO_VALIDO = """
            {
              "nombre": "Lucía",
              "apellido": "Benítez",
              "dni": "42111222",
              "email": "lucia.benitez@academit.edu.ar",
              "telefono": "1144556677",
              "domicilio": "Av. Siempreviva 742",
              "fechaNacimiento": "2001-05-14",
              "idCarrera": 1,
              "idPlan": 1
            }
            """;

    private static final String BODY_ALUMNO_INVALIDO = """
            {
              "nombre": "",
              "apellido": "B",
              "dni": "abc",
              "email": "email-invalido",
              "idCarrera": 1
            }
            """;

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlumnoService alumnoService;

    private AlumnoDTO dtoEjemplo() {
        AlumnoDTO dto = new AlumnoDTO();
        dto.setIdAlumno(1);
        dto.setLegajo("ALU-1001");
        dto.setNombre("Lucía");
        dto.setApellido("Benítez");
        dto.setDni("42111222");
        dto.setEmail("lucia.benitez@academit.edu.ar");
        dto.setFechaNacimiento(LocalDate.of(2001, 5, 14));
        dto.setIdCarrera(1);
        dto.setCarrera("Tecnicatura Superior en Software");
        dto.setIdPlan(1);
        dto.setPlanNombre("Plan 2024");
        dto.setEstado("ACTIVO");
        dto.setEstadoMatricula("ACTIVA");
        return dto;
    }

    @Test
    @DisplayName("GET /api/alumnos sin autenticación retorna 401 Unauthorized")
    void testListarAlumnos_SinAuth_Retorna401() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.status").value(401));
    }

    @Test
    @WithMockUser(username = "docente1", roles = {"DOCENTE"})
    @DisplayName("GET /api/alumnos con rol DOCENTE retorna 403 Forbidden")
    void testListarAlumnos_RolDocente_Retorna403() throws Exception {
        mockMvc.perform(get(BASE))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("GET /api/alumnos con rol ADMINISTRADOR retorna 200 OK y listado")
    void testListarAlumnos_Admin_Retorna200() throws Exception {
        when(alumnoService.listarAlumnos()).thenReturn(List.of(dtoEjemplo()));

        mockMvc.perform(get(BASE))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].idAlumno").value(1))
                .andExpect(jsonPath("$[0].legajo").value("ALU-1001"))
                .andExpect(jsonPath("$[0].nombre").value("Lucía"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("GET /api/alumnos/{id} inexistente retorna 404 Not Found")
    void testObtenerPorId_NoExiste_Retorna404() throws Exception {
        when(alumnoService.obtenerPorId(eq(99), any()))
                .thenThrow(new ResourceNotFoundException("Alumno no encontrado con ID: 99"));

        mockMvc.perform(get(BASE + "/99"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("POST /api/alumnos con datos válidos retorna 201 Created")
    void testCrearAlumno_Valido_Retorna201() throws Exception {
        when(alumnoService.crearAlumno(any())).thenReturn(dtoEjemplo());

        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_ALUMNO_VALIDO))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.idAlumno").value(1))
                .andExpect(jsonPath("$.legajo").value("ALU-1001"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("POST /api/alumnos con datos inválidos retorna 400 Bad Request")
    void testCrearAlumno_Invalido_Retorna400() throws Exception {
        mockMvc.perform(post(BASE)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(BODY_ALUMNO_INVALIDO))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    @DisplayName("PUT /api/alumnos/{id} con rol ADMINISTRATIVO retorna 200 OK")
    void testActualizarAlumno_Adminitrativo_Retorna200() throws Exception {
        when(alumnoService.actualizarAlumno(eq(1), any())).thenReturn(dtoEjemplo());

        String bodyUpdate = """
                {
                  "nombre": "Lucía Belén",
                  "telefono": "1144556677"
                }
                """;

        mockMvc.perform(put(BASE + "/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(bodyUpdate))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.idAlumno").value(1));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("DELETE /api/alumnos/{id} con rol ADMINISTRADOR retorna 204 No Content")
    void testEliminarAlumno_Admin_Retorna204() throws Exception {
        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    @WithMockUser(username = "administrativo", roles = {"ADMINISTRATIVO"})
    @DisplayName("DELETE /api/alumnos/{id} con rol ADMINISTRATIVO retorna 403 Forbidden")
    void testEliminarAlumno_Administrativo_Retorna403() throws Exception {
        mockMvc.perform(delete(BASE + "/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403));
    }

    @Test
    @WithMockUser(username = "alumno1", roles = {"ALUMNO"})
    @DisplayName("GET /api/alumnos/{id}/situacion-academica con rol ALUMNO retorna 200 OK")
    void testObtenerSituacionAcademica_Alumno_Retorna200() throws Exception {
        SituacionAcademicaDTO situacion = new SituacionAcademicaDTO();
        situacion.setIdAlumno(1);
        situacion.setMateriasAprobadas(3);
        situacion.setPromedio(8.5);
        situacion.setRegularidades(List.of("Programación I - REGULAR"));

        when(alumnoService.obtenerSituacionAcademica(eq(1), any())).thenReturn(situacion);

        mockMvc.perform(get(BASE + "/1/situacion-academica"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.materiasAprobadas").value(3))
                .andExpect(jsonPath("$.promedio").value(8.5))
                .andExpect(jsonPath("$.regularidades[0]").value("Programación I - REGULAR"));
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMINISTRADOR"})
    @DisplayName("GET /api/alumnos/{id}/situacion-administrativa retorna 200 OK")
    void testObtenerSituacionAdministrativa_Admin_Retorna200() throws Exception {
        SituacionAdministrativaDTO situacion = new SituacionAdministrativaDTO();
        situacion.setIdAlumno(1);
        situacion.setMatricula("ACTIVA");
        situacion.setCuotasPagas(5);
        situacion.setCuotasPendientes(7);
        situacion.setTotalDeuda(35000.0);
        situacion.setEstadoPago("PENDIENTE");

        when(alumnoService.obtenerSituacionAdministrativa(eq(1), any())).thenReturn(situacion);

        mockMvc.perform(get(BASE + "/1/situacion-administrativa"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.matricula").value("ACTIVA"))
                .andExpect(jsonPath("$.cuotasPagas").value(5))
                .andExpect(jsonPath("$.totalDeuda").value(35000.0))
                .andExpect(jsonPath("$.estadoPago").value("PENDIENTE"));
    }
}

