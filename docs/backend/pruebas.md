# Estrategia y Guía de Pruebas — Academit

> Documento de referencia sobre la arquitectura de testing del backend, ejecución de suites y lineamientos para incorporar nuevas pruebas unitarias y de integración.

---

## 1. Qué se prueba en el Sistema

La suite de pruebas automatizadas de Academit garantiza la robustez de las reglas de negocio, la seguridad de los endpoints y la consistencia de los contratos REST sin depender de un servidor externo levantado.

### Niveles de Prueba Implementados
1. **Capa de Controladores (MockMvc / `@WebMvcTest`)**:
   - Mapeo de rutas HTTP (`GET`, `POST`, `PUT`, `DELETE`).
   - Autorización por roles mediante Spring Security y `@WithMockUser` (verificando `200 OK`, `401 Unauthorized` y `403 Forbidden`).
   - Validación de payloads entrantes con Jakarta Bean Validation (`@NotNull`, `@Size`, `@Min`, `@Max`, `@Email`), comprobando que devuelvan `400 Bad Request` con estructura `ProblemDetail`.
   - Serialización y deserialización JSON de DTOs.
2. **Capa de Servicios (`@ExtendWith(MockitoExtension.class)`)**:
   - Lógica de negocio pura (RN01 a RN27).
   - Simulación aislada de repositorios JPA con Mockito (`when(...)`, `verify(...)`).
   - Lanzamiento de excepciones controladas de dominio (`ResourceNotFoundException`, `ConflictException`, `BusinessRuleException`).
   - Mapeo bidireccional entre entidades de base de datos y DTOs de salida.
3. **Tareas Programadas en Background**:
   - Verificación de ejecución periódica del cron (`CuotaVencidaTaskTest`), asegurando la actualización masiva de cuotas vencidas.

### Estado Actual de la Suite
- **Archivos de prueba**: 42 clases de test.
- **Pruebas ejecutadas**: 428 tests.
- **Tasa de éxito**: 100% (0 fallos, 0 omitidos).

---

## 2. Cómo Correr las Pruebas

Todas las pruebas se ejecutan mediante el wrapper de Gradle (`./gradlew`) desde el directorio `backend/`.

### Ejecutar toda la suite completa
```bash
cd backend
./gradlew test
```

### Ejecutar una clase de prueba específica
```bash
cd backend
./gradlew test --tests AlumnoServiceImplTest
```

### Ejecutar un método de prueba puntual
```bash
cd backend
./gradlew test --tests "com.academit.education.service.InscripcionServiceImplTest.crearInscripcion_superaCupoMaximo_lanzaConflictException"
```

### Ejecutar pruebas de un paquete completo
```bash
cd backend
./gradlew test --tests "com.academit.education.controller.*"
```

### Ver el reporte HTML de resultados
Gradle genera automáticamente un informe detallado en:
`backend/build/reports/tests/test/index.html`

---

## 3. Cómo Escribir una Nueva Prueba

Seguí el patrón estructural estándar de la aplicación: **Arrange - Act - Assert** (Dado, Cuando, Entonces).

### A. Plantilla para un Test de Servicio (Lógica de Negocio)
Ubicación: `backend/src/test/java/com/academit/education/service/`

```java
package com.academit.education.service;

import com.academit.education.exception.BusinessRuleException;
import com.academit.education.exception.ConflictException;
import com.academit.education.repository.AlumnoRepository;
import com.academit.education.service.impl.AlumnoServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AlumnoServiceImplTest {

    @Mock
    private AlumnoRepository alumnoRepository;

    @InjectMocks
    private AlumnoServiceImpl alumnoService;

    @Test
    @DisplayName("Debe lanzar ConflictException si el DNI del alumno ya está registrado")
    void crearAlumno_dniDuplicado_lanzaConflictException() {
        // 1. Arrange (Preparación)
        String dniExistente = "40123456";
        when(alumnoRepository.existsByDni(dniExistente)).thenReturn(true);

        AlumnoRequestDTO request = AlumnoRequestDTO.builder()
                .dni(dniExistente)
                .nombre("Juan")
                .apellido("Pérez")
                .build();

        // 2. Act & 3. Assert (Ejecución y Verificación)
        assertThrows(ConflictException.class, () -> alumnoService.crear(request));
        verify(alumnoRepository, never()).save(any());
    }
}
```

### B. Plantilla para un Test de Controlador (Endpoint HTTP y Seguridad)
Ubicación: `backend/src/test/java/com/academit/education/controller/`

```java
package com.academit.education.controller;

import com.academit.education.security.JwtAuthenticationFilter;
import com.academit.education.security.JwtUtil;
import com.academit.education.service.AlumnoService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AlumnoController.class)
class AlumnoControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private AlumnoService alumnoService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Test
    @WithMockUser(roles = "ALUMNO")
    @DisplayName("Rol ALUMNO no puede eliminar alumnos: debe retornar 403 Forbidden")
    void eliminarAlumno_rolAlumno_retornaForbidden() throws Exception {
        mockMvc.perform(delete("/api/alumnos/1"))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.status").value(403))
                .andExpect(jsonPath("$.title").value("Forbidden"));
    }

    @Test
    @WithMockUser(roles = "ADMINISTRADOR")
    @DisplayName("Payload inválido sin apellido: debe retornar 400 Bad Request con ProblemDetail")
    void crearAlumno_sinApellido_retornaBadRequest() throws Exception {
        String jsonPayload = """
            {
                "dni": "40123456",
                "nombre": "Juan"
            }
        """;

        mockMvc.perform(post("/api/alumnos")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonPayload))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value(400))
                .andExpect(jsonPath("$.errors[0].campo").value("apellido"));
    }
}
```
