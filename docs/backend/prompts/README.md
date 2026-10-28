# Prompts por Módulo del Backend

## Cómo usar

**Cada integrante pasa a su agente el contenido del prompt de su módulo, en una sesión nueva, una rama por módulo.** El prompt ya lleva el contexto, la rama sugerida, los endpoints del contrato y las reglas; no hace falta completar nada a mano.

## Prompts disponibles

| Módulo | Rama sugerida | Ola | Prompt |
| --- | --- | :---: | --- |
| Ciclos lectivos | `feature/back-ciclos-lectivos` | 1 (integrado) | [ciclos-lectivos.md](ciclos-lectivos.md) |
| Carreras | `feature/back-carreras` | 1 | [carreras.md](carreras.md) |
| Planes de estudio y materias | `feature/back-planes-materias` | 1 | [planes-materias.md](planes-materias.md) |
| Alumnos | `feature/back-alumnos` | 2 | [alumnos.md](alumnos.md) |
| Docentes | `feature/back-docentes` | 2 | [docentes.md](docentes.md) |
| Cursos | `feature/back-cursos` | 3 | [cursos.md](cursos.md) |
| Asignaciones | `feature/back-asignaciones` | 3 | [asignaciones.md](asignaciones.md) |
| Inscripciones | `feature/back-inscripciones` | 4 | [inscripciones.md](inscripciones.md) |
| Matrículas y cuotas | `feature/back-matriculas` | 5-6 | [matriculas-cuotas.md](matriculas-cuotas.md) |
| Pagos | `feature/back-pagos` | 6 | [pagos.md](pagos.md) |
| Tarea cuotas vencidas | `feature/back-cuotas-vencidas` | 6 | [cuotas-vencidas.md](cuotas-vencidas.md) |
| Asistencias | `feature/back-asistencias` | 7 | [asistencias.md](asistencias.md) |
| Evaluaciones y calificaciones | `feature/back-calificaciones` | 7 | [calificaciones.md](calificaciones.md) |
| Mesas de examen | `feature/back-mesas-examen` | 7 | [mesas-examen.md](mesas-examen.md) |
| Declaraciones juradas | `feature/back-declaraciones-juradas` | 8 | [declaraciones-juradas.md](declaraciones-juradas.md) |
| Documentación y legajos | `feature/back-documentacion` | 8 | [documentacion.md](documentacion.md) |
| Solicitudes y trámites | `feature/back-solicitudes` | 8 | [solicitudes.md](solicitudes.md) |

## Origen de los archivos

- Los prompts de módulos "grandes" (asistencias, calificaciones, inscripciones, matriculas-cuotas, pagos, mesas-examen, declaraciones-juradas y cuotas-vencidas) son **dedicados**: se escriben a mano y no los toca ningún script.
- Los del resto se **generan** con [`tools/prompts-modulos.py`](../../../tools/prompts-modulos.py) a partir de [`_plantilla-crud.md`](_plantilla-crud.md); si cambia una regla o dependencia, se edita el diccionario `MODULOS` del script y se corre de nuevo.
