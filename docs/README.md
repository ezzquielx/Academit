# Documentación del Proyecto — Academit

Índice central de la documentación técnica, arquitectónica y metodológica de **Academit** (TP Integrador - Taller de Lenguajes de Programación III).

---

## Índice General de Documentos

### Equipo y Gestión
| Documento | Para qué sirve |
| :--- | :--- |
| [flujo-de-trabajo.md](flujo-de-trabajo.md) | Convenciones de Git, flujo de ramas (`develop`, `feature/*`, `fix/*`), formato de commits (Conventional Commits), proceso de Pull Request y lista de control. |
| [estado-del-proyecto.md](estado-del-proyecto.md) | Diagnóstico integral del sistema: estado por módulo, reglas implementadas, decisiones arquitectónicas del grupo y reparto de tareas. |
| [pendientes.md](pendientes.md) | Lista de trabajo con casillas de todo lo que falta para terminar el proyecto, backend, integración, entregables y defensa oral. |

### Entregables de Cátedra
| Documento | Para qué sirve |
| :--- | :--- |
| [entregables/README.md](entregables/README.md) | Índice central de los entregables de la materia. |
| [entregables/entregable-1-analisis.md](entregables/entregable-1-analisis.md) | Entregable 1: Descripción del problema, objetivos, actores, 45 requisitos funcionales (RF01 a RF45) y catálogo de reglas de negocio (RN01 a RN14). |
| [entregables/entregable-2/casos-de-uso.md](entregables/entregable-2/casos-de-uso.md) | Diagramas y especificación de casos de uso por módulo institucional. |
| [entregables/entregable-2/diagrama-de-clases.md](entregables/entregable-2/diagrama-de-clases.md) | Modelo de dominio orientado a objetos y relaciones estructurales entre clases. |
| [entregables/entregable-2/der.md](entregables/entregable-2/der.md) | Diagrama Entidad-Relación y diseño de tablas relacionales en PostgreSQL. |
| [entregables/entregable-2/secuencia-inscripcion.md](entregables/entregable-2/secuencia-inscripcion.md) | Diagrama de secuencia del proceso de inscripción a cursada y validación de cupo. |
| [entregables/entregable-2/secuencia-pagos.md](entregables/entregable-2/secuencia-pagos.md) | Diagrama de secuencia del registro y cobro de aranceles y emisión de comprobante. |
| [entregables/entregable-2/secuencia-calificacion.md](entregables/entregable-2/secuencia-calificacion.md) | Diagrama de secuencia de carga de evaluaciones y calificaciones docentes. |

### Frontend
| Documento | Para qué sirve |
| :--- | :--- |
| [frontend/README.md](frontend/README.md) | Índice principal de la arquitectura frontend React + Vite, estándares de código y catálogo de vistas. |
| [frontend/GUIA-PARA-BACKEND.md](frontend/GUIA-PARA-BACKEND.md) | Guía técnica para conectar la API con la maqueta, proxy de desarrollo y checklist pantalla por pantalla. |
| [frontend/entidades-y-endpoints.md](frontend/entidades-y-endpoints.md) | Tablas de especificación de entidades persistidas/calculadas y catálogo de 71 endpoints con notas de dominio. |
| [frontend/contrato-api.md](frontend/contrato-api.md) | Convenciones REST, manejo estandarizado de errores (`ProblemDetail` adaptado) y autenticación JWT. |
| [frontend/mapa-de-pantallas.md](frontend/mapa-de-pantallas.md) | Matriz completa de pantallas, rutas, roles autorizados y trazabilidad con requisitos funcionales. |
| [frontend/referencia-estetica.html](frontend/referencia-estetica.html) | Prototipo estático HTML de referencia visual institucional aprobado por el equipo. |

### Backend
| Documento | Para qué sirve |
| :--- | :--- |
| [backend/README.md](backend/README.md) | Visión general del backend: stack tecnológico, versiones, instrucciones de arranque local (`nix-shell`), arquitectura en capas, convenciones, manejo de errores (RFC 7807) y limitaciones conocidas. |
| [backend/seguridad.md](backend/seguridad.md) | Arquitectura de seguridad: flujo de login, tokens JWT, claims, datos de sesión (`UsuarioSesionDTO`, `id_alumno`, `id_docente`), endpoints públicos, anotaciones `@PreAuthorize` y matriz de permisos completa para 71 endpoints. |
| [backend/configuracion.md](backend/configuracion.md) | Perfiles de ejecución `dev` y `prod`, tabla completa de variables de entorno, justificación de `spring.jpa.hibernate.ddl-auto=validate` y monitoreo de salud para Render/cloud. |
| [backend/reglas-de-negocio.md](backend/reglas-de-negocio.md) | Catálogo trazable de Reglas de Negocio (RN01 a RN27), clase y método que aplica cada una, suite de prueba asociada y estado de implementación. |
| [backend/pruebas.md](backend/pruebas.md) | Estrategia de testing en Spring Boot: qué se prueba (MockMvc, Mockito, tareas cron), comandos para ejecutar la suite y plantillas paso a paso para escribir nuevos tests de servicios y controladores. |
| [backend/guia-de-modulos.md](backend/guia-de-modulos.md) | Fichas técnicas detalladas de los 21 módulos del backend: endpoints, entidades JPA, servicios, dependencias y reglas de negocio por módulo. |
| [backend/prompts/](backend/prompts/) | Prompts preparados por módulo para desarrollo asistido y generación de código contextualizada. |

### Base de Datos
| Documento | Para qué sirve |
| :--- | :--- |
| [base-de-datos/diccionario-de-datos.md](base-de-datos/diccionario-de-datos.md) | Diccionario de datos de las 32 tablas del esquema relacional: columnas, tipos de datos, nulos, claves primarias, claves foráneas, restricciones de unicidad y checks. |
| [base-de-datos/modelo-relacional.md](base-de-datos/modelo-relacional.md) | Diagrama Entidad-Relación gráfico en Mermaid de las 32 tablas con cardinalidades y claves, validado formalmente con `@mermaid-js/mermaid-cli` (`mmdc`). |
| [database/README.md](../database/README.md) | Documentación de administración de PostgreSQL, inicialización y comandos de entorno (`db-init`, `db-start`, `db-reset`, `db-psql`). |
| [estado/desajustes-db.md](estado/desajustes-db.md) | Matriz histórica de correspondencia y desajustes resueltos entre el esquema de base de datos (`01-schema.sql`) y el contrato API frontend. |
