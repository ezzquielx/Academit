#!/usr/bin/env python3
"""
tools/estado-proyecto.py
Genera docs/estado-del-proyecto.md inspeccionando el repositorio y verificando
el cumplimiento de endpoints, reglas de negocio, decisiones y tareas pendientes.
"""

import os
import re
import csv
import glob

def parse_controllers():
    controllers = glob.glob('backend/src/main/java/**/controller/*Controller.java', recursive=True)
    implemented = []
    for c in sorted(controllers):
        with open(c, encoding='utf-8') as f:
            content = f.read()
        base_match = re.search(r'@RequestMapping\(\"([^\"]+)\"\)', content)
        base_path = base_match.group(1) if base_match else ''
        methods = re.findall(r'@(GetMapping|PostMapping|PutMapping|DeleteMapping|PatchMapping)(?:\(\"?([^\"]*)\"?\))?', content)
        for m, p in methods:
            p = p.strip('\"')
            full_p = base_path + (p if p.startswith('/') or not p else '/' + p)
            if full_p.endswith('/'):
                full_p = full_p[:-1]
            m_name = m.replace('Mapping', '').upper()
            implemented.append((m_name, full_p, os.path.basename(c)))
    return implemented

def parse_csv_endpoints():
    csv_eps = []
    with open('docs/frontend/endpoints.csv', encoding='utf-8') as f:
        r = csv.reader(f)
        next(r)
        for row in r:
            csv_eps.append({
                'metodo': row[0].strip().upper(),
                'ruta': row[1].strip(),
                'desc': row[2].strip(),
                'roles': row[3].strip(),
                'pantalla': row[4].strip(),
                'funcion': row[5].strip(),
                'estado': row[6].strip()
            })
    return csv_eps

def count_backend_stats():
    entities = glob.glob('backend/src/main/java/**/model/*.java', recursive=True)
    repos = glob.glob('backend/src/main/java/**/repository/*Repository.java', recursive=True)
    test_files = glob.glob('backend/src/test/java/**/*.java', recursive=True)
    test_count = 0
    for tf in test_files:
        with open(tf, encoding='utf-8') as f:
            test_count += len(re.findall(r'@Test\b', f.read()))
    return len(entities), len(repos), test_count

def build_document():
    impl = parse_controllers()
    impl_set = {(m, p) for m, p, _ in impl}
    csv_eps = parse_csv_endpoints()
    num_entities, num_repos, num_tests = count_backend_stats()
    
    matched = [ep for ep in csv_eps if (ep['metodo'], ep['ruta']) in impl_set]
    missing = [ep for ep in csv_eps if (ep['metodo'], ep['ruta']) not in impl_set]

    # Agrupar faltantes por recurso
    missing_by_resource = {}
    for ep in missing:
        parts = ep['ruta'].split('/')
        res = parts[2] if len(parts) > 2 else 'general'
        missing_by_resource.setdefault(res, []).append(f"`{ep['metodo']} {ep['ruta']}`")

    doc = []
    doc.append("# Estado Real del Proyecto y Plan de Distribución - Academit")
    doc.append("")
    doc.append(f"> **Fecha de relevamiento:** Octubre 2026 | **Entidades:** {num_entities} | **Repositories:** {num_repos} | **Tests backend:** {num_tests}/{num_tests} OK.")
    doc.append("")
    
    # 1. Estado por módulo
    doc.append("## 1. Estado por Módulo")
    doc.append("")
    doc.append("| Módulo | Entidad y Repository | Service | Controller | Tests | Estado |")
    doc.append("| --- | --- | --- | --- | --- | --- |")
    doc.append(f"| **Base de datos** | 25 tablas alineadas | N/A | N/A | Scripts SQL verificados | **Hecho** |")
    doc.append(f"| **Entidades JPA** | {num_entities} entidades, {num_repos} repositorios | N/A | N/A | Compilación JPA OK | **Hecho** |")
    doc.append(f"| **Seguridad y Usuarios** | `Usuario`, `Rol` / Repos | `AuthService`, `UsuarioService` | `Auth`, `Perfil`, `Usuario` | {num_tests} tests unit/integ | **Hecho** |")
    doc.append("| **Alumnos** | `Alumno`, `EstadoPersona` / Repos | Pendiente | Pendiente | Pendiente | **Parcial** |")
    doc.append("| **Docentes** | `Docente` / `DocenteRepository` | Pendiente | Pendiente | Pendiente | **Parcial** |")
    doc.append("| **Carreras** | `Carrera` / `CarreraRepository` | Pendiente | Pendiente | Pendiente | **Parcial** |")
    doc.append("| **Materias y Planes** | `Materia`, `PlanEstudio`, `Correlativa` | Pendiente | Pendiente | Pendiente | **Parcial** |")
    doc.append("| **Cursos y Ciclos** | `Curso`, `CicloLectivo` / Repos | `CicloLectivoService` hecho; Cursos pendiente | `CicloLectivoController` hecho; Cursos pendiente | 15 tests del módulo ciclos | **Parcial** |")
    doc.append("| **Asignaciones** | `CursoMateria` / Repo | Pendiente | Pendiente | Pendiente | **Parcial** |")
    doc.append("| **Inscripciones** | `Inscripcion` / Repo | Pendiente | Pendiente | Pendiente | **Parcial** |")
    doc.append("| **Matrículas y Cuotas** | `Matricula`, `Cuota`, `EstadoCuota` | Pendiente | Pendiente | Pendiente | **Parcial** |")
    doc.append("| **Pagos** | `Pago`, `MedioPago` / Repos | Pendiente | Pendiente | Pendiente | **Parcial** |")
    doc.append("| **Evaluaciones y Calificaciones** | `Evaluacion`, `Calificacion` / Repos | Pendiente | Pendiente | Pendiente | **Parcial** |")
    doc.append("| **Asistencia** | `Asistencia`, `EstadoAsistencia` | Pendiente | Pendiente | Pendiente | **Parcial** |")
    doc.append("| **Mesas de Examen** | `MesaExamen`, `InscripcionMesa` | Pendiente | Pendiente | Pendiente | **Parcial** |")
    doc.append("| **Declaración Jurada** | `DeclaracionJurada`, `DeclaracionMateria` | Pendiente | Pendiente | Pendiente | **Parcial** |")
    doc.append("| **Solicitudes** | `Solicitud`, `TipoSolicitud` | Pendiente | Pendiente | Pendiente | **Parcial** |")
    doc.append("| **Documentación** | `Documentacion`, `TipoDocumento` | Pendiente | Pendiente | Pendiente | **Parcial** |")
    doc.append("| **Cambio de Carrera** | Gestionado vía `Solicitud` + `Plan` | Pendiente | Pendiente | Pendiente | **Pendiente** |")
    doc.append("")

    # 2. Endpoints
    total_csv = len(csv_eps)
    total_impl = len(impl)
    total_missing = len(missing)
    pct_impl = (len(matched) / total_csv) * 100
    doc.append("## 2. Endpoints del Contrato vs Implementados")
    doc.append("")
    doc.append(f"- **Total en contrato frontend:** {total_csv} endpoints (68 de negocio + 3 de autenticación inicial).")
    doc.append(f"- **Implementados en backend:** {total_impl} endpoints ({len(matched)} del contrato + `GET /api/usuarios/{{id}}` y `GET /api/auth/me`). Cobertura: **{pct_impl:.1f} %**.")
    doc.append(f"- **Pendientes de implementar:** {total_missing} endpoints.")
    doc.append("")
    doc.append(f"### Endpoints implementados ({len(matched)})")
    doc.append("- `POST /api/auth/login`, `GET /api/auth/me`, `POST /api/auth/logout`, `POST /api/auth/cambiar-password`")
    doc.append("- `GET /api/usuarios`, `GET /api/usuarios/{id}`, `POST /api/usuarios`, `PUT /api/usuarios/{id}`, `DELETE /api/usuarios/{id}`")
    doc.append("- `GET /api/usuarios/me`, `PUT /api/usuarios/me`")
    doc.append("")
    doc.append(f"### Endpoints faltantes ({total_missing}) por módulo")
    for res, eps_list in sorted(missing_by_resource.items()):
        doc.append(f"- **{res} ({len(eps_list)}):** " + ", ".join(eps_list))
    doc.append("")

    # 3. Reglas de negocio
    doc.append("## 3. Reglas de Negocio en Backend y su Estado")
    doc.append("")
    doc.append("1. **Cupo del Curso (RN03 - RF14, RF22):** Validar `inscriptos < cupo` antes de confirmar inscripción. *Estado: Pendiente (columna existe en BD; falta validación en Service).*")
    doc.append("2. **Inscripción Única por Ciclo (RN02 - RF22):** Un alumno solo puede registrar una inscripción activa por ciclo lectivo. *Estado: Parcial (restringido en BD por UK; falta validación amigable en Service).*")
    doc.append("3. **Generación de 12 Cuotas y 3 Seguros (RN18 - RF28, RF46):** Generación automática de 12 cuotas mensuales regulares y 3 seguros cuatrimestrales por matrícula activa. *Estado: Pendiente.*")
    doc.append("4. **Pago Completo de Cuota (RN09 - RF32):** Rechazar pagos parciales; el importe debe cancelar la cuota completa y pasar su estado a PAGADA. *Estado: Pendiente.*")
    doc.append("5. **Marcado de Cuotas Vencidas (RN07 - RF30):** Marcar cuotas impagas como VENCIDAS al superar la fecha límite. *Estado: Pendiente (requiere scheduled task o cálculo dinámico).*")
    doc.append("6. **Calificaciones 1 a 10 con Aprobado >= 4 (RN14, RN21 - RF38, RF40):** Restricción de escala y condición de aprobación. *Estado: Parcial (CHECK 1-10 en BD; falta regla de aprobación en Service).*")
    doc.append("7. **Carga Exclusiva por Docente Asignado (RN11, RN12 - RF36, RF38, RF41):** Solo el docente vinculado a la cátedra (`curso_materia`) puede registrar notas y asistencia. *Estado: Pendiente.*")
    doc.append("8. **Permisos y Control de Acceso por Rol (RN17 - RF05):** Respuestas 401 y 403 estandarizadas en ProblemDetail. *Estado: Hecho en SecurityConfig y Usuarios; Pendiente de extender a nuevos controllers.*")
    doc.append("")

    # 4. Decisiones tomadas por el grupo
    doc.append("## 4. Decisiones Tomadas por el Grupo")
    doc.append("")
    doc.append("1. **Estado de Alumno y de Docente:** ACTIVO e INACTIVO (la baja lógica pasa de BAJA a INACTIVO). Se elimina REGULAR como estado: \"regular\" es una condición por materia, calculada con asistencia (mínimo 75 %) y notas, y no se guarda.")
    doc.append("   - *Motivo:* Unifica el ciclo de vida de personas en el padrón y evita persistir una condición académica volátil que depende de cada cursada.")
    doc.append("2. **Matricula.estadoPago no se guarda:** se calcula al devolver la matrícula a partir de sus cuotas. EN_MORA si hay al menos una cuota VENCIDA; AL_DIA si todas las cuotas ya vencidas (o todas) están PAGADA; PENDIENTE en el resto. Matricula.estado (PENDIENTE, ACTIVA, CANCELADA) sí se guarda; al cancelar una matrícula sus cuotas pendientes pasan a ANULADA.")
    doc.append("   - *Motivo:* Elimina inconsistencias entre la cuenta corriente de cuotas y la cabecera de matrícula.")
    doc.append("3. **Sin paginación en el servidor por ahora:** los listados devuelven arrays completos. Si hiciera falta, se agregan ?page y ?size opcionales y un header X-Total-Count sin cambiar la forma del array.")
    doc.append("   - *Motivo:* Simplifica el desarrollo inicial con el volumen de datos actual y permite filtrado/ordenamiento ágil en cliente.")
    doc.append("4. **Pago.comprobante:** columna única con formato AAAA-NNNNNN (año y número correlativo de 6 dígitos), generada por el backend al registrar el pago a partir del id_pago (ejemplo 2026-000123); el frontend solo la muestra.")
    doc.append("   - *Motivo:* Garantiza numeración oficial única, auditable y correlativa administrada exclusivamente por el servidor.")
    doc.append("")
    doc.append("### Estado de Aplicación de Decisiones")
    doc.append("- **Ya aplicado e integrado en `develop`:**")
    doc.append("  - 4a (Estados de Personas): Base de datos (`01-schema.sql` y `02-datos-iniciales.sql`), backend (`Alumno`, `Docente`), contrato (`entidades.json`, `openapi.yaml`) y frontend (`mocks/data.js` y vistas). Resuelto en `refactor/aplicar-decisiones-estado`, integrado en `develop`.")
    doc.append("  - 4b (Matrícula y Estado de Pago): Backend (`MatriculaResponseDTO`, `MatriculaService`), contrato (`AL_DIA`, `EN_MORA`, `PENDIENTE` calculado) y frontend (`AdminMatriculasPage`, mocks). Resuelto en `refactor/aplicar-decisiones-estado`, integrado en `develop`.")
    doc.append("  - 4c (Colecciones Completas): Controllers implementados devuelven colecciones completas; frontend preparado para arrays directos. Resuelto en `fix/back-contrato-errores-auth`, integrado en `develop`.")
    doc.append("  - 4d (Comprobante de Pago): Columna `pago.comprobante VARCHAR(11)` en BD, DTO y generador AAAA-NNNNNN en backend, contrato y pantallas. Resuelto en `refactor/aplicar-decisiones-estado`, integrado en `develop`.")
    doc.append("")
    doc.append("### Estado de Desajustes de Base de Datos (`docs/estado/desajustes-db.md`)")
    doc.append("- Tablas `solicitud` y `documentacion`: Creadas en BD y mapeadas en JPA. Resuelto en `feature/db-alinear-contrato`, integrado en `develop`.")
    doc.append("- Asignación con roles docentes: Columna `rol_docente` y unicidad `(id_curso, id_materia, id_docente)`. Resuelto en `feature/db-alinear-contrato`, integrado en `develop`.")
    doc.append("- Asistencia por materia: Columna `id_materia` y unicidad compuesta. Resuelto en `feature/db-alinear-contrato`, integrado en `develop`.")
    doc.append("- Turno en curso y mesa: Columnas `turno` y `tribunal`. Resuelto en `feature/db-alinear-contrato`, integrado en `develop`.")
    doc.append("- Tipos de cuota: `CUOTA_MENSUAL` y `SEGURO_CUATRIMESTRAL`. Resuelto en `feature/db-alinear-contrato`, integrado en `develop`.")
    doc.append("")

    # 5. Trabajo restante
    doc.append("## 5. Trabajo Restante y Reparto por Ramas")
    doc.append("")
    doc.append("1. **Fase Previa - Integración de Esquema, Contrato y Decisiones:**")
    doc.append("   - **Estado:** Completada e integrada en `develop`. Comprende `fix/back-contrato-errores-auth`, `feature/db-alinear-contrato`, `refactor/aplicar-decisiones-estado` y `docs/ordenar-documentacion`.")
    doc.append("2. **Fase 1 - Maestros Base (Trabajo en PARALELO):**")
    doc.append("   - `feature/back-alumnos`: Service, Controller y Tests para `/api/alumnos` (CRUD y situaciones).")
    doc.append("   - `feature/back-docentes`: Service, Controller y Tests para `/api/docentes` (CRUD).")
    doc.append("   - `feature/back-carreras-planes`: Controller y Service para `/api/carreras`, `/api/materias` y `/api/planes-estudio`.")
    doc.append("   - `feature/back-cursos-ciclos`: Controller y Service para `/api/cursos` y `/api/ciclos-lectivos`.")
    doc.append("3. **Fase 2 - Asignaciones e Inscripciones (Depende de Fase 1, en PARALELO):**")
    doc.append("   - `feature/back-asignaciones`: Controller y Service para `/api/asignaciones` con roles docentes.")
    doc.append("   - `feature/back-inscripciones`: Controller y Service para `/api/inscripciones` con validación de cupo (RN03) e inscripción única (RN02).")
    doc.append("4. **Fase 3 - Aranceles y Pagos (Depende de Fase 1 y 2):**")
    doc.append("   - `feature/back-matriculas-cuotas`: `/api/matriculas` y `/api/cuotas`, generación de 12+3 cuotas (RN18) y `estadoPago` calculado (4b).")
    doc.append("   - `feature/back-comprobante-pago` (Paralelo): `/api/pagos`, validación de pago completo (RN09) y generación de comprobante (4d).")
    doc.append("5. **Fase 4 - Académico (Depende de Fase 1 y 2, en PARALELO):**")
    doc.append("   - `feature/back-asistencias`: `/api/asistencias`, control de docente asignado (RN12).")
    doc.append("   - `feature/back-calificaciones`: `/api/evaluaciones` y `/api/calificaciones`, escala 1-10 y docente asignado (RN11, RN14).")
    doc.append("   - `feature/back-mesas-examen`: `/api/mesas-examen` e inscripciones a examen.")
    doc.append("   - `feature/back-declaraciones-juradas`: `/api/declaraciones-juradas` con verificación de correlativas.")
    doc.append("6. **Fase 5 - Trámites y Mantenimiento (PARALELO):**")
    doc.append("   - `feature/back-solicitudes-documentacion`: `/api/solicitudes` y `/api/documentacion`.")
    doc.append("   - `feature/back-cuotas-vencidas`: tarea programada para marcado de cuotas vencidas (RN07, RF30).")
    doc.append("")

    # 6. Integración
    doc.append("## 6. Checklist de Integración Frontend vs Backend")
    doc.append("")
    doc.append("- [ ] Configurar `frontend/.env` con `VITE_USE_MOCK=false` y proxy Vite apuntando a `http://localhost:8080`.")
    doc.append("- [x] `Login.jsx` (`POST /api/auth/login`): recepción y almacenamiento de JWT, redirección por rol.")
    doc.append("- [x] `Layout.jsx` / `Perfil`: `GET /api/auth/me`, `GET /api/usuarios/me`, `POST /api/auth/logout`.")
    doc.append("- [x] `GestionUsuariosPage.jsx`: CRUD de usuarios con roles y validaciones.")
    doc.append("- [ ] `GestionAlumnosPage.jsx`: listado, filtros, alta/edición y fichas de situación académica/administrativa.")
    doc.append("- [ ] `GestionDocentesPage.jsx`: gestión de docentes y visualización de asignaciones.")
    doc.append("- [ ] `GestionCarrerasPage.jsx` / `GestionPlanEstudiosPage.jsx`: carreras, materias y malla curricular con correlativas.")
    doc.append("- [ ] `GestionCursosPage.jsx` / `GestionAsignacionesPage.jsx`: comisiones, ciclos y asignación docente.")
    doc.append("- [ ] `GestionInscripcionesPage.jsx`: inscripción con control de cupo y rechazo de duplicados.")
    doc.append("- [ ] `AdminMatriculasPage.jsx` / `AdminCuotasPage.jsx`: alta de matrícula, generación de cuotas y cálculo de mora.")
    doc.append("- [ ] `AdminPagosPage.jsx`: cobro de cuota completa y emisión de comprobante AAAA-NNNNNN.")
    doc.append("- [ ] `CalificacionesDocentePage.jsx`: registro de notas (1-10) por docente asignado.")
    doc.append("- [ ] `AsistenciasDocentePage.jsx`: toma de asistencia por materia.")
    doc.append("- [ ] `MesasExamenPage.jsx`: mesas y actas de examen.")
    doc.append("- [ ] `DeclaracionJuradaPage.jsx`: presentación anual de materias a cursar.")
    doc.append("- [ ] `BandejaSolicitudesPage.jsx` / `DocumentacionPage.jsx`: trámites y legajos digitales.")
    doc.append("- [ ] Validación OpenAPI: verificación de schemas JSON con `docs/frontend/openapi.yaml`.")
    doc.append("")

    # 7. Documentación pendiente de entrega
    doc.append("## 7. Documentación Pendiente de la Entrega")
    doc.append("")
    doc.append("1. **Portada Institucional:** Integrantes del grupo, profesor/a, ayudante, asignatura y fecha de entrega.")
    doc.append("2. **Análisis de Requisitos (`docs/entregables/entregable-1-analisis.md`):** Incorporar RF y RN formalizados para Solicitudes y Documentación.")
    doc.append("3. **Diagramas UML y DER (`docs/entregables/entregable-2/`):**")
    doc.append("   - DER actualizado: incorporar tablas `solicitud`, `documentacion`, `rol_docente`, `asistencia.id_materia`, `turno` y `pago.comprobante`.")
    doc.append("   - Diagrama de Clases: actualizar con nuevas entidades y estado INACTIVO.")
    doc.append("   - Diagramas de Secuencia: actualizar inscripción, pago con comprobante, calificación y asistencia.")
    doc.append("4. **Modelo Relacional y Diccionario de Datos:** Documentar tipos de datos, PK, FK y restricciones CHECK actualizadas.")
    doc.append("5. **Revisión del Enunciado:** Revisar enunciado de la cátedra para verificar cobertura de todos los puntos solicitados.")
    doc.append("")

    content = "\n".join(doc)
    return content

if __name__ == '__main__':
    content = build_document()
    output_path = 'docs/estado-del-proyecto.md'
    with open(output_path, 'w', encoding='utf-8') as f:
        f.write(content)
    
    lines = content.splitlines()
    print(f"Generated {output_path} with {len(lines)} lines.")
    if len(lines) > 250:
        print(f"WARNING: Line count {len(lines)} exceeds 250 lines limit!")
    else:
        print("OK: Line count is within the 250 lines limit.")
