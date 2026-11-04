# Backend de Academit — Sistema de Gestión Académica

> Núcleo de servicios REST, persistencia relacional, reglas de negocio y control de acceso del sistema **Academit** (TP Integrador - Taller de Lenguajes de Programación III).

---

## 1. Stack Tecnológico y Versiones

El backend está construido sobre la plataforma Java moderna, siguiendo las mejores prácticas de la industria en arquitectura en capas desacopladas:

- **Lenguaje**: Java 21 (OpenJDK 21 LTS).
- **Framework principal**: Spring Boot 4.1.1 (`org.springframework.boot`).
- **Seguridad**: Spring Security 6 con autenticación stateless mediante JWT (`HS256`).
- **Persistencia**: Spring Data JPA / Hibernate 6 con estrategia `spring.jpa.hibernate.ddl-auto=validate`.
- **Base de Datos**: PostgreSQL 16+ con esquema estricto (32 tablas relacionales).
- **Validación**: Jakarta Bean Validation (`spring-boot-starter-validation`).
- **Herramienta de Construcción**: Gradle 9.7.1 (con wrapper `gradlew`).
- **Productividad**: Project Lombok (reducción de código repetitivo de getters, setters y builders).
- **Testing**: JUnit 5, Mockito y Spring Boot Test (MockMvc).

---

## 2. Cómo Levantar y Probar el Backend

El proyecto cuenta con un entorno reproducible basado en `nix-shell` que provee Java 21, PostgreSQL y Node.js sin necesidad de instalar dependencias globales.

### Paso 1: Inicializar y arrancar el entorno Nix-Shell
Desde la raíz del repositorio:
```bash
nix-shell
```

### Paso 2: Administrar la Base de Datos Local
Dentro de la consola interactiva de `nix-shell` disponés de los siguientes alias:
- `db-init`: Inicializa el clúster local en `.pgdata/`, crea el usuario `academit` y la base de datos `academit`.
- `db-start`: Inicia el motor PostgreSQL en segundo plano (puerto `5432`).
- `db-reset`: Limpia la base y carga en orden estricto `database/01-schema.sql` y `database/02-datos-iniciales.sql`.
- `db-psql`: Abre una sesión interactiva de línea de comandos en PostgreSQL.
- `db-stop`: Detiene el servidor PostgreSQL.

### Paso 3: Iniciar el Servidor de Backend
En una terminal con PostgreSQL corriendo:
```bash
cd backend
./gradlew bootRun
```
El servidor queda operativo en `http://localhost:8080` listo para atender peticiones del frontend o herramientas HTTP.

### Paso 4: Ejecutar la Suite de Pruebas
Para correr los 428 tests automatizados unitarios y de integración:
```bash
cd backend
./gradlew test
```
Para más detalles sobre cómo filtrar pruebas o escribir nuevos casos, consultá la [Guía de Pruebas](pruebas.md).

---

## 3. Estructura de Paquetes y Responsabilidad por Capa

El código fuente se organiza bajo el paquete base `com.academit.education`:

```
backend/src/main/java/com/academit/education/
├── config/         # Configuración global (SecurityConfig, CORS, SchedulingConfig, TimeConfig)
├── controller/     # Controladores REST: exponen endpoints, validan DTOs (@Valid) y declaran @PreAuthorize
├── dto/            # Objetos de Transferencia de Datos desacoplados del modelo de BD
│   ├── auth/       # DTOs de login, sesión (UsuarioSesionDTO) y credenciales
│   ├── common/     # DTOs compartidos (ApiResponseDTO, FieldErrorDTO)
│   └── <módulo>/   # Request y Response DTOs por entidad de negocio
├── exception/      # Excepciones de dominio y GlobalExceptionHandler (RFC 7807)
├── model/          # Entidades JPA correspondientes a las 32 tablas del esquema relacional
├── repository/     # Interfaces Spring Data JPA con métodos de consulta y @Query
├── security/       # Filtro JwtAuthenticationFilter, JwtUtil, CustomUserDetails y servicio de auth
├── service/        # Interfaces de lógica de negocio pura
│   └── impl/       # Implementaciones transaccionales (@Service, @Transactional)
└── task/           # Tareas programadas en background (CuotaVencidaTask)
```

### Responsabilidad de cada Capa:
1. **Controlador (`controller`)**: Solo atiende peticiones HTTP, mapea rutas, verifica la autorización del rol con `@PreAuthorize`, delega en la capa de servicio y retorna respuestas `ResponseEntity` con códigos de estado HTTP semánticos (200, 201, 204).
2. **Servicio (`service`)**: Núcleo de la aplicación. Aplica las 27 reglas de negocio institucionales (RN01 a RN27), coordina los repositorios necesarios y orquesta transacciones con `@Transactional`.
3. **Repositorio (`repository`)**: Abstracción de acceso a datos sin acoplarse al driver SQL. Valida restricciones de existencia y ejecuta consultas optimizadas.
4. **Modelo (`model`)**: Representación en objetos de las tablas de PostgreSQL. Mantiene relaciones `@ManyToOne`, `@OneToMany` sin mutar el esquema en caliente (`ddl-auto=validate`).
5. **DTO (`dto`)**: Previene la exposición directa de las entidades de base de datos protegiendo el contrato con el frontend.

---

## 4. Convenciones de Desarrollo

- **Nomenclatura**:
  - Clases de entidad y servicios en español (por ejemplo `CicloLectivo`, `InscripcionServiceImpl`, `MesaExamenRepository`) para mantener estricta fidelidad con el vocabulario del dominio académico.
  - Métodos y variables en camelCase (`obtenerPorId`, `validarCupoMaximo`).
  - Constantes en UPPER_SNAKE_CASE.
- **DTOs Específicos**:
  - Nunca retornar entidades JPA en los controladores; usar siempre objetos `*RequestDTO` y `*ResponseDTO`.
- **Validación Declarativa**:
  - Validar datos de entrada en los DTOs usando anotaciones estándar de Jakarta (`@NotNull`, `@NotBlank`, `@Size`, `@Min`, `@Max`, `@Email`).
- **Manejo de Transacciones**:
  - `@Transactional(readOnly = true)` a nivel clase en implementaciones de servicio para optimizar lecturas en Hibernate.
  - `@Transactional` explícito en métodos de escritura (creación, modificación, anulación) garantizando atomicidad y rollback automático ante excepciones.

---

## 5. Manejo Centralizado de Errores (RFC 7807)

Todas las excepciones no controladas o de negocio son interceptadas por `GlobalExceptionHandler` (`@RestControllerAdvice`), transformándolas en el estándar industrial **RFC 7807 Problem Details for HTTP APIs** (`application/problem+json`).

### Estructura de Respuesta de Error
```json
{
  "type": "about:blank",
  "title": "Error de validación",
  "status": 400,
  "detail": "Error de validación en los datos enviados",
  "message": "Error de validación en los datos enviados",
  "errors": [
    {
      "campo": "dni",
      "mensaje": "El DNI es obligatorio y debe tener entre 7 y 10 caracteres"
    }
  ]
}
```

### Mapeo de Excepciones y Códigos HTTP

| Excepción Capturada | Código HTTP | Título | Motivo |
| :--- | :---: | :--- | :--- |
| `MethodArgumentNotValidException` | `400 Bad Request` | Error de validación | Fallo de anotación `@Valid` en DTO |
| `BusinessRuleException` | `400 Bad Request` | Regla de negocio violada | Violación de lógica de negocio (RN02, RN09, etc.) |
| `BadCredentialsException` | `401 Unauthorized` | Unauthorized | Usuario o contraseña erróneos |
| `UnauthorizedException` | `401 Unauthorized` | Unauthorized | Token ausente, corrupto o expirado |
| `AccessDeniedException` | `403 Forbidden` | Forbidden | Rol insuficiente para ejecutar la acción |
| `ResourceNotFoundException` | `404 Not Found` | Recurso no encontrado | Entidad solicitada inexistente por ID |
| `ConflictException` | `409 Conflict` | Conflicto | Clave duplicada, cupo agotado o estado no permitido |
| `DataIntegrityViolationException` | `409 Conflict` / `400` | Conflicto / Integridad | Violación de clave única (`UNIQUE`) o foránea |
| `Exception` (cualquier otra) | `500 Internal Error`| Error interno | Excepción inesperada del servidor |

---

## 6. Limitaciones Conocidas del Backend Actual

1. **Tokens JWT sin revocación distribuida (Blacklist)**:
   - Al ser una arquitectura JWT estrictamente sin estado, el logout (`POST /api/auth/logout`) responde `204 No Content` para que el cliente descarte el token localmente, pero no almacena los tokens invalidados en una lista negra compartida (e.g. Redis).
2. **Endpoint de Cambio de Carrera (RN26)**:
   - El endpoint `/api/alumnos/{id}/cambio-carrera` aún no está implementado en `AlumnoController`; actualmente la vista del frontend utiliza mocks de demostración.
3. **DTO de Sesión (`UsuarioSesionDTO`)**:
   - Expone `idUsuario`, `username`, `nombre` y `rol`. Aunque en la base de datos ya existen las claves foráneas `id_alumno` e `id_docente`, las mismas se consultan actualmente a nivel de servicio y no están proyectadas directamente en el payload inicial de login.

---

## 7. Documentación Complementaria del Backend

Para profundizar en áreas específicas del backend, consultá los siguientes documentos dedicados:

- [Seguridad, JWT y Matriz de Permisos](seguridad.md): Flujo de autenticación, configuración de roles y matriz de 71 endpoints.
- [Configuración y Variables de Entorno](configuracion.md): Perfiles `dev`/`prod`, validación de esquema y monitoreo en Render.
- [Catálogo de Reglas de Negocio (RN01 a RN27)](reglas-de-negocio.md): Trazabilidad de cada regla contra clases, métodos y pruebas.
- [Guía y Estrategia de Pruebas](pruebas.md): Ejecución de suites Mockito y MockMvc y cómo escribir nuevos tests.
- [Guía de Módulos del Backend](guia-de-modulos.md): Fichas técnicas detalladas de los 21 módulos institucionales.
- [Diccionario de Datos](../base-de-datos/diccionario-de-datos.md): Especificación exhaustiva de las 32 tablas del esquema relacional.
- [Modelo Relacional](../base-de-datos/modelo-relacional.md): Diagrama Entidad-Relación visual en Mermaid validado con `mmdc`.
