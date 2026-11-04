# Seguridad, Autenticación y Control de Acceso — Academit

> Documento de referencia de la arquitectura de seguridad del backend en Spring Security 6 y JWT.
> La matriz de permisos se sincroniza automáticamente con `docs/frontend/endpoints.csv` mediante `tools/matriz-permisos.py`.

---

## 1. Esquema de Autenticación y JWT

El backend implementa un esquema de autenticación sin estado (**stateless**) fundamentado en JSON Web Tokens (JWT). Toda la configuración reside en `SecurityConfig` y el paquete `com.academit.education.security`.

### Flujo de Login
1. El cliente envía credenciales mediante `POST /api/auth/login` con payload `{"username": "...", "password": "..."}`.
2. `AuthServiceImpl` valida las credenciales delegando en `AuthenticationManager` y `BCryptPasswordEncoder`.
3. Si la verificación es exitosa, `JwtUtil` genera un token firmado con algoritmo **HMAC-SHA256 (HS256)**.
4. El backend responde con código `200 OK` y el payload de sesión:
```json
{
  "usuario": {
    "idUsuario": 1,
    "username": "admin",
    "nombre": "Administrador General",
    "rol": "ADMINISTRADOR"
  },
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### Estructura del Token JWT
El token JWT incluye en su carga útil (*payload*) los siguientes claims esenciales:
- `sub`: Identificador principal de inicio de sesión (`username`).
- `rol`: Rol del usuario en el sistema (`ADMINISTRADOR`, `ADMINISTRATIVO`, `DOCENTE`, `ALUMNO`).
- `idUsuario`: Clave primaria de la tabla `usuario`.
- `iat`: Timestamp Unix de emisión (*issued at* en segundos).
- `exp`: Timestamp Unix de expiración (*expiration time* en segundos).

### Envío del Token en Solicitudes
Para acceder a los endpoints protegidos, el cliente HTTP debe adjuntar el token en la cabecera estándar:
```http
Authorization: Bearer <token_jwt>
```
El filtro `JwtAuthenticationFilter` intercepta cada petición entrante, verifica la firma criptográfica con `jwt.secret` y valida la vigencia temporal. Si el token es válido, carga el objeto `CustomUserDetails` en el `SecurityContextHolder`.

---

## 2. Datos de la Sesión y Contexto de Usuario

### Endpoint de Verificación de Sesión (`GET /api/auth/me`)
Permite al cliente autenticado consultar en cualquier momento su información de sesión activa. Responde:
```json
{
  "usuario": {
    "idUsuario": 3,
    "username": "docente1",
    "nombre": "Laura Pérez",
    "rol": "DOCENTE"
  }
}
```

### Asociación con Alumno y Docente en Base de Datos
En la tabla `usuario` (y la entidad JPA `Usuario`), existen claves foráneas opcionales:
- `id_alumno`: Vinculado si el usuario tiene rol `ALUMNO`.
- `id_docente`: Vinculado si el usuario tiene rol `DOCENTE`.
- Restricción de integridad: `CONSTRAINT ck_usuario_persona CHECK (id_alumno IS NULL OR id_docente IS NULL)`.

Actualmente el DTO de sesión `UsuarioSesionDTO` expone `idUsuario`, `username`, `nombre` y `rol`. Cuando un servicio de negocio requiere operar sobre el perfil específico del usuario autenticado (por ejemplo, para filtrar inscripciones de un alumno o cursos asignados a un docente), consulta la entidad `Usuario` mediante `usuarioRepository.findByUsername(username)` y obtiene `usuario.getAlumno().getId()` o `usuario.getDocente().getId()`.

---

## 3. Endpoints Públicos vs. Protegidos

Por diseño de `SecurityConfig`, todas las rutas requieren autenticación salvo las declaradas explícitamente como públicas:

| Método | Ruta | Propósito |
| :--- | :--- | :--- |
| **POST** | `/api/auth/login` | Inicio de sesión y emisión de token JWT |
| **POST** | `/api/auth/logout` | Cierre de sesión (enfoque stateless, responde `204 No Content`) |
| **OPTIONS** | `/**` | Pre-flight checks de CORS para navegadores web |

Cualquier intento de acceso a un endpoint protegido sin credenciales o con un token inválido o expirado devuelve inmediatamente `401 Unauthorized` con formato RFC 7807:
```json
{
  "type": "about:blank",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Credenciales no provistas o inválidas",
  "message": "Credenciales no provistas o inválidas",
  "errors": []
}
```

---

## 4. Declaración de Permisos en Controladores

La seguridad granular a nivel método está habilitada mediante la anotación `@EnableMethodSecurity` en `SecurityConfig`. Cada controlador o método restringe su acceso usando `@PreAuthorize`.

### Convenciones de Nomenclatura de Roles
En `CustomUserDetails`, los roles se mapean con el prefijo estándar de Spring Security (`ROLE_`):
- `ROLE_ADMINISTRADOR`
- `ROLE_ADMINISTRATIVO`
- `ROLE_DOCENTE`
- `ROLE_ALUMNO`

Las expresiones `hasRole('ADMINISTRADOR')` o `hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')` omiten automáticamente el prefijo `ROLE_`.

### Ejemplos en Código

#### 1. Protección a nivel de Clase Completa
```java
@RestController
@RequestMapping("/api/usuarios")
@PreAuthorize("hasRole('ADMINISTRADOR')")
public class UsuarioController {
    // Todos los métodos heredan la restricción exclusiva para ADMINISTRADOR
}
```

#### 2. Protección granular por Método
```java
@RestController
@RequestMapping("/api/alumnos")
public class AlumnoController {

    // Consulta permitida a administradores y administrativos
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<List<AlumnoDTO>> listarAlumnos() { ... }

    // Eliminación restringida estrictamente a administradores
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDTO> eliminarAlumno(@PathVariable Integer id) { ... }
}
```

#### 3. Inyección del Usuario Autenticado en el Método
```java
@GetMapping("/api/auth/me")
public ResponseEntity<AuthMeResponseDTO> me(@AuthenticationPrincipal UserDetails userDetails) {
    String username = userDetails.getUsername();
    return ResponseEntity.ok(authService.getMe(username));
}
```

Si el usuario autenticado intenta ejecutar una operación para la cual su rol no está autorizado, Spring Security emite un `AccessDeniedException` que el `GlobalExceptionHandler` captura devolviendo `403 Forbidden`:
```json
{
  "type": "about:blank",
  "title": "Forbidden",
  "status": 403,
  "detail": "Acceso denegado: no posee los permisos requeridos",
  "message": "Acceso denegado: no posee los permisos requeridos",
  "errors": []
}
```

---

## 5. Matriz de Roles y Permisos por Endpoint

A continuación se detalla la matriz integral de autorización para cada uno de los endpoints de la API REST institucional:

| Método | Ruta | Descripción | ADMINISTRADOR | ADMINISTRATIVO | DOCENTE | ALUMNO |
| :--- | :--- | :--- | :---: | :---: | :---: | :---: |
| **GET** | `/api/alumnos` | Obtiene el listado completo de alumnos. | ✓ | ✓ | — | — |
| **GET** | `/api/alumnos/{id}` | Obtiene un alumno por su ID. | ✓ | ✓ | — | — |
| **POST** | `/api/alumnos` | Crea un nuevo alumno en el sistema. | ✓ | ✓ | — | — |
| **PUT** | `/api/alumnos/{id}` | Actualiza los datos de un alumno existente. | ✓ | ✓ | — | — |
| **DELETE** | `/api/alumnos/{id}` | Realiza la baja lógica de un alumno. | ✓ | — | — | — |
| **GET** | `/api/alumnos/{id}/situacion-academica` | Obtiene la situación académica consolidada del alumno (materias cursadas, regularizadas y notas finales). | ✓ | ✓ | — | ✓ |
| **GET** | `/api/alumnos/{id}/situacion-administrativa` | Obtiene la situación arancelaria y administrativa del alumno (matrícula, cuotas, deudas y pagos). | ✓ | ✓ | — | ✓ |
| **GET** | `/api/docentes` | Obtiene el padrón completo de docentes institucionales. | ✓ | ✓ | — | — |
| **POST** | `/api/docentes` | Registra un nuevo docente en el cuerpo académico. | ✓ | — | — | — |
| **PUT** | `/api/docentes/{id}` | Actualiza los datos de legajo o contacto de un docente. | ✓ | — | — | — |
| **DELETE** | `/api/docentes/{id}` | Realiza la baja lógica de un docente. | ✓ | — | — | — |
| **GET** | `/api/carreras` | Obtiene el listado de carreras institucionales. | ✓ | ✓ | — | — |
| **GET** | `/api/carreras/{id}` | Obtiene los detalles de una carrera específica por su identificador. | ✓ | ✓ | — | — |
| **POST** | `/api/carreras` | Registra una nueva carrera en la oferta académica. | ✓ | — | — | — |
| **PUT** | `/api/carreras/{id}` | Actualiza la información de una carrera existente. | ✓ | — | — | — |
| **DELETE** | `/api/carreras/{id}` | Realiza la baja lógica de una carrera académica. | ✓ | — | — | — |
| **GET** | `/api/carreras/{id}/planes` | Obtiene los planes de estudio correspondientes a una carrera. | ✓ | ✓ | — | — |
| **GET** | `/api/carreras/{id}/cursos` | Obtiene los cursos o comisiones vinculados a una carrera. | ✓ | ✓ | — | — |
| **GET** | `/api/planes-estudio` | Obtiene todos los planes de estudio del instituto. | ✓ | ✓ | — | — |
| **GET** | `/api/planes-estudio/{id}/materias` | Obtiene las materias de un plan de estudio estructuradas con año y correlativas. | ✓ | ✓ | ✓ | ✓ |
| **GET** | `/api/materias` | Obtiene el catálogo de materias curriculares, con filtros opcionales (idCarrera, idPlan). | ✓ | — | ✓ | ✓ |
| **POST** | `/api/materias` | Registra una nueva materia en el plan de estudios. | ✓ | — | — | — |
| **PUT** | `/api/materias/{id}` | Actualiza los datos o régimen de correlatividades de una materia. | ✓ | — | — | — |
| **GET** | `/api/cursos` | Obtiene el listado de comisiones y cursos habilitados. | ✓ | ✓ | ✓ | — |
| **POST** | `/api/cursos` | Registra una nueva comisión / curso con control de cupos y carrera asociada. | ✓ | — | — | — |
| **PUT** | `/api/cursos/{id}` | Actualiza los datos de un curso o comisión. | ✓ | — | — | — |
| **GET** | `/api/ciclos-lectivos` | Obtiene el catálogo de ciclos lectivos. | ✓ | ✓ | — | — |
| **POST** | `/api/ciclos-lectivos` | Registra un nuevo ciclo lectivo anual. | ✓ | — | — | — |
| **PUT** | `/api/ciclos-lectivos/{id}` | Actualiza los datos o estado de un ciclo lectivo existente. | ✓ | — | — | — |
| **GET** | `/api/asignaciones` | Obtiene el listado de asignaciones docentes a cursos y materias. | ✓ | — | — | — |
| **POST** | `/api/asignaciones` | Registra una nueva asignación docente a una cátedra y curso. | ✓ | — | — | — |
| **DELETE** | `/api/asignaciones/{id}` | Elimina una asignación docente existente. | ✓ | — | — | — |
| **GET** | `/api/inscripciones` | Obtiene el registro de inscripciones de alumnos a comisiones y cursos. | ✓ | ✓ | — | — |
| **POST** | `/api/inscripciones` | Realiza la inscripción de un alumno a una comisión/curso con control de cupos disponibles. | ✓ | ✓ | — | — |
| **DELETE** | `/api/inscripciones/{id}` | Cancela una inscripción de un alumno y libera el cupo correspondiente en la comisión. | ✓ | ✓ | — | — |
| **GET** | `/api/matriculas` | Obtiene el padrón de matrículas anuales registradas. | ✓ | ✓ | — | — |
| **PUT** | `/api/matriculas/{id}` | Actualiza el estado o información de una matrícula anual. | ✓ | ✓ | — | — |
| **PATCH** | `/api/matriculas/{id}` | Registra la cancelación definitiva de matrícula de un alumno (RF27). | ✓ | ✓ | — | ✓ |
| **POST** | `/api/matriculas/{id}/cuotas` | Genera el plan de 12 cuotas mensuales y 3 seguros cuatrimestrales para una matrícula individual. | ✓ | ✓ | — | — |
| **GET** | `/api/cuotas` | Enriquecer cuota con campos calculados derivados de la matrícula (idAlumno, alumnoNombre) | ✓ | ✓ | — | ✓ |
| **PATCH** | `/api/cuotas/{id}/anular` | Anula una cuota administrativa (solo para administradores). | ✓ | — | — | — |
| **GET** | `/api/pagos` | Obtiene el historial de pagos registrados con filtros opcionales. | ✓ | ✓ | — | ✓ |
| **POST** | `/api/pagos` | Registra un nuevo pago arancelario por el importe total de la cuota (RN09). | ✓ | ✓ | — | — |
| **GET** | `/api/evaluaciones` | Obtiene el listado de evaluaciones planificadas por cátedra (parciales, trabajos prácticos). | ✓ | — | ✓ | — |
| **POST** | `/api/evaluaciones` | Registra una nueva evaluación creada por un docente (parcial o TP integrador). | — | — | ✓ | — |
| **POST** | `/api/evaluaciones/{idEvaluacion}/calificaciones` | Guarda las calificaciones de una evaluación en lote (atómico, todo o nada). | — | — | ✓ | — |
| **GET** | `/api/calificaciones` | Obtiene el listado de calificaciones según filtros opcionales (idAlumno, tipo, idMateria). | ✓ | — | ✓ | ✓ |
| **POST** | `/api/calificaciones` | Registra o actualiza la calificación individual de un alumno. | ✓ | — | ✓ | — |
| **GET** | `/api/asistencias` | Obtiene el registro de asistencias con filtros opcionales (idAlumno, idCurso, fecha). | ✓ | — | ✓ | ✓ |
| **POST** | `/api/asistencias/lote` | Registra o actualiza la asistencia de una lista de alumnos en lote. | — | — | ✓ | — |
| **GET** | `/api/mesas-examen` | Obtiene el listado de mesas de examen final programadas. | ✓ | ✓ | ✓ | ✓ |
| **POST** | `/api/mesas-examen/{id}/inscripciones` | Inscribe a un alumno en una mesa de examen final (RF49). | — | — | — | ✓ |
| **DELETE** | `/api/mesas-examen/{id}/inscripciones/{idInscripcion}` | Da de baja la inscripción a una mesa de examen final (RF50). | — | — | — | ✓ |
| **POST** | `/api/mesas-examen` | Programa una nueva mesa de examen final en secretaría académica. | ✓ | — | — | — |
| **PUT** | `/api/mesas-examen/{id}` | Actualiza los datos o estado de una mesa de examen final existente. | ✓ | — | — | — |
| **GET** | `/api/solicitudes` | Obtiene el listado de solicitudes y trámites institucionales con filtros opcionales. | ✓ | ✓ | — | ✓ |
| **POST** | `/api/solicitudes` | Genera una nueva solicitud o trámite del estudiante (constancias, DDJJ, pasantías, título, académicas). | — | — | — | ✓ |
| **PATCH** | `/api/solicitudes/{id}` | Resuelve una solicitud en la bandeja administrativa (APROBADA o RECHAZADA). | ✓ | ✓ | — | — |
| **GET** | `/api/declaraciones-juradas` | Obtiene el listado de declaraciones juradas de materias a cursar. | ✓ | ✓ | — | ✓ |
| **POST** | `/api/declaraciones-juradas` | Presenta una nueva declaración jurada de materias (RF51). | — | — | — | ✓ |
| **GET** | `/api/documentacion` | Obtiene el estado de presentación de la documentación requerida para el legajo. | ✓ | ✓ | — | ✓ |
| **PUT** | `/api/documentacion/{id}` | Actualiza el estado de entrega o verificación de un requisito documental. | ✓ | ✓ | — | — |
| **POST** | `/api/auth/login` | Inicia sesión con usuario y contraseña. | ✓ | ✓ | ✓ | ✓ |
| **POST** | `/api/auth/logout` | Cierra la sesión activa en el sistema. | ✓ | ✓ | ✓ | ✓ |
| **POST** | `/api/auth/cambiar-password` | Modifica la contraseña del usuario en sesión. | ✓ | ✓ | ✓ | ✓ |
| **GET** | `/api/usuarios` | Obtiene el listado de cuentas de usuario y roles del sistema. | ✓ | — | — | — |
| **POST** | `/api/usuarios` | Registra un nuevo usuario con credenciales y asignación de rol. | ✓ | — | — | — |
| **PUT** | `/api/usuarios/{id}` | Actualiza los datos de perfil o rol de un usuario existente. | ✓ | — | — | — |
| **DELETE** | `/api/usuarios/{id}` | Realiza la baja lógica de un usuario del sistema. | ✓ | — | — | — |
| **GET** | `/api/usuarios/me` | Obtiene los datos del perfil del usuario en sesión. | ✓ | ✓ | ✓ | ✓ |
| **PUT** | `/api/usuarios/me` | Actualiza los datos de contacto y filiación del perfil en sesión. | ✓ | ✓ | ✓ | ✓ |
