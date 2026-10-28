# Documentación del Proyecto - Academit

Índice central de la documentación técnica, arquitectónica y metodológica de **Academit** (TP Integrador - Taller de Lenguajes de Programación III).

---

## Índice General de Documentos

### Equipo
| Documento | Para qué sirve |
| :--- | :--- |
| [flujo-de-trabajo.md](flujo-de-trabajo.md) | Convenciones de Git, flujo de ramas (`develop`, `feature/*`, `fix/*`), formato de commits (Conventional Commits), proceso de Pull Request y lista de control. |
| [estado-del-proyecto.md](estado-del-proyecto.md) | Diagnóstico integral del sistema: estado por módulo, reglas implementadas, decisiones arquitectónicas del grupo y reparto de tareas. |
| [pendientes.md](pendientes.md) | Lista de trabajo con casillas de todo lo que falta para terminar el proyecto, backend, integración, entregables y defensa oral. |

### Entregables
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
| [backend/guia-de-modulos.md](backend/guia-de-modulos.md) | Guía de módulos del backend: arquitectura en capas, entidades JPA, servicios, reglas de negocio, orden por olas y fichas técnicas por módulo. |
| [backend/prompts/](backend/prompts/) | Prompts listos por módulo del backend (dedicados y generados con `tools/prompts-modulos.py`): contexto, rama, endpoints del contrato y reglas por módulo. |

### Referencias
| Documento | Para qué sirve |
| :--- | :--- |
| [estado/desajustes-db.md](estado/desajustes-db.md) | Informe comparativo y matriz de desajustes entre el esquema de base de datos (`01-schema.sql`) y el contrato del frontend. |
| *Documento Colaborativo de Análisis* | Documento de trabajo compartido en Drive/Docs con el relevamiento inicial de requisitos institucionales (mencionado en el análisis del sistema). |
