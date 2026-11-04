#!/usr/bin/env python3
"""
tools/matriz-permisos.py
Genera la matriz de roles y permisos a partir de docs/frontend/endpoints.csv
y documenta la seguridad del backend en docs/backend/seguridad.md.
"""

import csv
import os
import sys

CSV_PATH = "docs/frontend/endpoints.csv"
OUTPUT_PATH = "docs/backend/seguridad.md"

def parse_endpoints_csv(csv_path=CSV_PATH):
    if not os.path.exists(csv_path):
        print(f"Error: No se encontró {csv_path}", file=sys.stderr)
        sys.exit(1)

    endpoints = []
    with open(csv_path, mode="r", encoding="utf-8") as f:
        reader = csv.reader(f)
        header = next(reader)
        for row in reader:
            if not row or len(row) < 4:
                continue
            metodo = row[0].strip().upper()
            ruta = row[1].strip()
            desc = row[2].strip()
            roles_raw = row[3].strip()
            pantalla = row[4].strip() if len(row) > 4 else ""
            funcion = row[5].strip() if len(row) > 5 else ""
            estado = row[6].strip() if len(row) > 6 else ""

            roles_list = [r.strip() for r in roles_raw.split(",") if r.strip()]
            endpoints.append({
                "metodo": metodo,
                "ruta": ruta,
                "desc": desc,
                "roles": roles_list,
                "roles_raw": roles_raw,
                "pantalla": pantalla,
                "funcion": funcion,
                "estado": estado
            })
    return endpoints

def generar_tabla_markdown(endpoints):
    lineas = [
        "| Método | Ruta | Descripción | ADMINISTRADOR | ADMINISTRATIVO | DOCENTE | ALUMNO |",
        "| :--- | :--- | :--- | :---: | :---: | :---: | :---: |"
    ]

    for ep in endpoints:
        metodo = ep["metodo"]
        ruta = f"`{ep['ruta']}`"
        desc = ep["desc"].replace("|", "\\|")
        roles = ep["roles"]

        es_publico = False
        if not roles or "PUBLICO" in [r.upper() for r in roles] or ep["ruta"].startswith("/api/auth/login"):
            es_publico = True

        admin = "✓" if "ADMINISTRADOR" in roles else ("Público" if es_publico else "—")
        adminis = "✓" if "ADMINISTRATIVO" in roles else ("Público" if es_publico else "—")
        docente = "✓" if "DOCENTE" in roles else ("Público" if es_publico else "—")
        alumno = "✓" if "ALUMNO" in roles else ("Público" if es_publico else "—")

        lineas.append(f"| **{metodo}** | {ruta} | {desc} | {admin} | {adminis} | {docente} | {alumno} |")

    return "\n".join(lineas)

def generar_documento_seguridad(endpoints):
    tabla_roles = generar_tabla_markdown(endpoints)

    doc = f"""# Seguridad, Autenticación y Control de Acceso — Academit

> Documento de referencia de la arquitectura de seguridad del backend en Spring Security 6 y JWT.
> La matriz de permisos se sincroniza automáticamente con `docs/frontend/endpoints.csv` mediante `tools/matriz-permisos.py`.

---

## 1. Esquema de Autenticación y JWT

El backend implementa un esquema de autenticación sin estado (**stateless**) fundamentado en JSON Web Tokens (JWT). Toda la configuración reside en `SecurityConfig` y el paquete `com.academit.education.security`.

### Flujo de Login
1. El cliente envía credenciales mediante `POST /api/auth/login` con payload `{{"username": "...", "password": "..."}}`.
2. `AuthServiceImpl` valida las credenciales delegando en `AuthenticationManager` y `BCryptPasswordEncoder`.
3. Si la verificación es exitosa, `JwtUtil` genera un token firmado con algoritmo **HMAC-SHA256 (HS256)**.
4. El backend responde con código `200 OK` y el payload de sesión:
```json
{{
  "usuario": {{
    "idUsuario": 1,
    "username": "admin",
    "nombre": "Administrador General",
    "rol": "ADMINISTRADOR"
  }},
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}}
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
{{
  "usuario": {{
    "idUsuario": 3,
    "username": "docente1",
    "nombre": "Laura Pérez",
    "rol": "DOCENTE"
  }}
}}
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
{{
  "type": "about:blank",
  "title": "Unauthorized",
  "status": 401,
  "detail": "Credenciales no provistas o inválidas",
  "message": "Credenciales no provistas o inválidas",
  "errors": []
}}
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
public class UsuarioController {{
    // Todos los métodos heredan la restricción exclusiva para ADMINISTRADOR
}}
```

#### 2. Protección granular por Método
```java
@RestController
@RequestMapping("/api/alumnos")
public class AlumnoController {{

    // Consulta permitida a administradores y administrativos
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMINISTRADOR', 'ADMINISTRATIVO')")
    public ResponseEntity<List<AlumnoDTO>> listarAlumnos() {{ ... }}

    // Eliminación restringida estrictamente a administradores
    @DeleteMapping("/{{id}}")
    @PreAuthorize("hasRole('ADMINISTRADOR')")
    public ResponseEntity<ApiResponseDTO> eliminarAlumno(@PathVariable Integer id) {{ ... }}
}}
```

#### 3. Inyección del Usuario Autenticado en el Método
```java
@GetMapping("/api/auth/me")
public ResponseEntity<AuthMeResponseDTO> me(@AuthenticationPrincipal UserDetails userDetails) {{
    String username = userDetails.getUsername();
    return ResponseEntity.ok(authService.getMe(username));
}}
```

Si el usuario autenticado intenta ejecutar una operación para la cual su rol no está autorizado, Spring Security emite un `AccessDeniedException` que el `GlobalExceptionHandler` captura devolviendo `403 Forbidden`:
```json
{{
  "type": "about:blank",
  "title": "Forbidden",
  "status": 403,
  "detail": "Acceso denegado: no posee los permisos requeridos",
  "message": "Acceso denegado: no posee los permisos requeridos",
  "errors": []
}}
```

---

## 5. Matriz de Roles y Permisos por Endpoint

A continuación se detalla la matriz integral de autorización para cada uno de los endpoints de la API REST institucional:

{tabla_roles}
"""
    return doc

def main():
    endpoints = parse_endpoints_csv()
    if len(sys.argv) > 1 and sys.argv[1] == "--write":
        doc_completo = generar_documento_seguridad(endpoints)
        with open(OUTPUT_PATH, "w", encoding="utf-8") as f:
            f.write(doc_completo)
        print(f"✓ Generado exitosamente {OUTPUT_PATH} ({len(endpoints)} endpoints documentados)")
    else:
        tabla = generar_tabla_markdown(endpoints)
        print(tabla)

if __name__ == "__main__":
    main()
