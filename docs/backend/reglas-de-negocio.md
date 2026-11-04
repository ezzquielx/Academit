# Reglas de Negocio del Backend — Academit

> Catálogo integral de Reglas de Negocio (RN01 a RN27), clase y método de aplicación, suite de prueba asociada y estado de implementación en el código actual.
> Trazado directamente contra los requerimientos funcionales institucionales y verificado con `rg`.

---

## Matriz de Reglas de Negocio (RN01 a RN27)

| RN | Nombre y Descripción | Clase y Método que la aplica | Test que la cubre | Estado |
| :---: | :--- | :--- | :--- | :---: |
| **RN01** | **Inscripción única por comisión**: Un alumno no puede inscribirse dos veces a la misma comisión en el mismo ciclo. | `InscripcionServiceImpl.crearInscripcion` | `InscripcionServiceImplTest.crearInscripcion_inscripcionDuplicada_lanzaConflictException` | Implementada |
| **RN02** | **Matrícula activa para inscribirse**: Solo puede inscribirse a cursos si posee matrícula en estado `ACTIVA` para el ciclo lectivo. | `InscripcionServiceImpl.crearInscripcion` | `InscripcionServiceImplTest.crearInscripcion_sinMatriculaActiva_lanzaBusinessRuleException` | Implementada |
| **RN03** | **Control de cupo máximo**: La comisión no admite inscripciones una vez alcanzada su `capacidad_maxima`. | `InscripcionServiceImpl.crearInscripcion` | `InscripcionServiceImplTest.crearInscripcion_superaCupoMaximo_lanzaConflictException` | Implementada |
| **RN04** | **Entidades existentes y activas**: Alumno, comisión y ciclo lectivo deben existir y estar activos. | `InscripcionServiceImpl.crearInscripcion` (existencia validada; activo implícito en FKs) | `InscripcionServiceImplTest.crearInscripcion_cursoInexistente_lanzaResourceNotFoundException` | Parcial |
| **RN05** | **Matrícula anual única**: Un alumno solo puede registrar una matrícula por ciclo lectivo (`uk_matricula_alumno_ciclo`). | `MatriculaServiceImpl.crearMatricula` | `MatriculaServiceImplTest.crearMatricula_yaExiste_lanzaConflictException` | Implementada |
| **RN06** | **Cuotas dependientes de matrícula**: Las cuotas no existen de forma independiente; se generan asociadas a una matrícula activa. | `MatriculaServiceImpl.crearMatricula` y `generarCuotasMatricula` | `MatriculaServiceImplTest.crearMatricula_generaCuotasCorrectamente` | Implementada |
| **RN07** | **Vencimiento diario de cuotas**: Al superar la fecha de vencimiento, cuotas `PENDIENTE` pasan automáticamente a `VENCIDA`. | `CuotaVencidaTask.marcarCuotasVencidas` y `CuotaRepository.marcarCuotasVencidas` | `CuotaVencidaTaskTest.marcarCuotasVencidas_ejecutaRepository` | Implementada |
| **RN08** | **Pago sobre cuota válida**: Los pagos solo se imputan a cuotas en estado `PENDIENTE` o `VENCIDA`. Cuotas `PAGADA` o `ANULADA` son rechazadas. | `PagoServiceImpl.crearPago` | `PagoServiceImplTest.crearPago_cuotaYaPagada_lanzaConflictException` | Implementada |
| **RN09** | **Monto exacto de pago**: El importe abonado debe coincidir exactamente con el valor nominal de la cuota. | `PagoServiceImpl.crearPago` | `PagoServiceImplTest.crearPago_montoIncorrecto_lanzaBusinessRuleException` | Implementada |
| **RN10** | **Cancelación de cuota pagada**: Tras un pago registrado con éxito, la cuota transiciona a estado `PAGADA`. | `PagoServiceImpl.crearPago` | `PagoServiceImplTest.crearPago_exitoso_actualizaEstadoCuota` | Implementada |
| **RN11** | **Rango de calificaciones**: Las notas deben ser números enteros en la escala del 1 al 10. | `CalificacionRequestDTO` (`@Min(1) @Max(10)`) y `CalificacionServiceImpl.registrarCalificacionesLote` | `CalificacionControllerTest.crearCalificacion_notaInvalida_retornaBadRequest` | Implementada |
| **RN12** | **Asistencia única por jornada**: Un alumno solo puede registrar una asistencia diaria por comisión y materia (`uk_asistencia_registro`). | `AsistenciaServiceImpl.registrarAsistencia` y `registrarAsistenciasLote` | `AsistenciaServiceImplTest.registrarAsistencia_duplicada_lanzaConflictException` | Implementada |
| **RN13** | **Baja lógica de alumnos**: Se prohíbe la eliminación física si existen relaciones académicas o arancelarias; se desactiva el estado. | `AlumnoServiceImpl.eliminar` | `AlumnoServiceImplTest.eliminar_bajaLogica_marcaEstadoInactivo` | Implementada |
| **RN14** | **Evaluación en cátedra válida**: Las evaluaciones deben pertenecer a una cátedra activa (`curso_materia`) y dentro del ciclo lectivo. | `EvaluacionServiceImpl.crearEvaluacion` | `EvaluacionServiceImplTest.crearEvaluacion_fechaInvalida_lanzaBusinessRuleException` | Implementada |
| **RN15** | **Asistencias por lote de inscriptos**: La planilla de asistencia solo admite alumnos inscriptos regularmente en el curso. | `AsistenciaServiceImpl.registrarAsistenciasLote` | `AsistenciaServiceImplTest.registrarAsistenciasLote_alumnoNoInscripto_lanzaBusinessRuleException` | Implementada |
| **RN16** | **Anulación de cuotas por matrícula cancelada**: Si una matrícula se cancela, sus cuotas pendientes deben anularse. | `MatriculaServiceImpl.cancelarMatricula` (marca cuotas como ANULADA, sin guarda reactiva completa) | Sin test unitario dedicado específico | Parcial |
| **RN17** | **Mínimo privilegio de roles**: Acceso restringido por rol (`ADMINISTRADOR`, `ADMINISTRATIVO`, `DOCENTE`, `ALUMNO`) en cada endpoint. | `SecurityConfig` y `@PreAuthorize` en todos los controllers | `SecurityIntegrationTest` y tests individuales de controllers | Implementada |
| **RN18** | **Estructura de aranceles anuales**: La matrícula genera 10 cuotas mensuales (marzo-diciembre) y 2 de seguro cuatrimestral. | `MatriculaServiceImpl.generarCuotasMatricula` | `MatriculaServiceImplTest.crearMatricula_generaCuotasCorrectamente` | Implementada |
| **RN19** | **Correlatividades para mesa de examen**: Para rendir final, el alumno debe tener la cursada regularizada y correlativas aprobadas. | `MesaExamenServiceImpl.inscribirAlumnoMesa` | `MesaExamenServiceImplTest.inscribirAlumno_sinRegularidad_lanzaBusinessRuleException` | Implementada |
| **RN20** | **Plazo de Declaración Jurada**: La DDJJ de materias solo puede presentarse durante el período habilitado por el ciclo lectivo. | `DeclaracionJuradaServiceImpl.presentarDeclaracion` | `DeclaracionJuradaServiceImplTest.presentar_fueraDeFecha_lanzaBusinessRuleException` | Implementada |
| **RN21** | **Autorización de carga docente**: Solo los docentes asignados como `TITULAR` o `ADJUNTO` pueden registrar calificaciones. | `CalificacionServiceImpl.validarDocenteHabilitado` | `CalificacionServiceImplTest.registrarCalificacion_docenteNoAutorizado_lanzaAccessDeniedException` | Implementada |
| **RN22** | **Coherencia plan-carrera**: El alumno no puede inscribirse en asignaturas pertenecientes a un plan distinto al de su legajo. | `AlumnoServiceImpl.validarPlanCarrera` e `InscripcionServiceImpl.crearInscripcion` | `InscripcionServiceImplTest.crearInscripcion_planDistinto_lanzaConflictException` | Implementada |
| **RN23** | **Unicidad de turno en comisiones**: Una comisión no puede tener materias superpuestas en el mismo turno y día. | `CursoServiceImpl.crearCurso` | `CursoServiceImplTest.crearCurso_mismoTurno_validaRestricciones` | Implementada |
| **RN24** | **Aciclicidad de correlatividades**: La red de correlatividades curriculares no puede contener dependencias circulares. | `MateriaServiceImpl.validarCorrelativas` | `MateriaServiceImplTest.validarCorrelativas_cicloDetectado_lanzaConflictException` | Implementada |
| **RN25** | **Protección de carreras con planes/alumnos**: No se puede eliminar una carrera si cuenta con alumnos o planes activos. | `CarreraServiceImpl.eliminarCarrera` (baja lógica; delegación a restricciones relacionales) | Sin test unitario dedicado | Parcial |
| **RN26** | **Historial en cambio de carrera**: El cambio de carrera preserva materias aprobadas y equivalencias. | No implementada en backend (`/api/alumnos/{id}/cambio-carrera` no existe aún) | Sin test | No implementada |
| **RN27** | **Padrón docente administrativo**: Personal administrativo solo consulta docentes; el alta, modificación y baja es exclusiva de Admin. | `@PreAuthorize` en `DocenteController` y `AsignacionController` | `DocenteControllerTest.crearDocente_rolAdministrativo_retornaForbidden` | Implementada |

---

## Detalle de Reglas No Implementadas o Parciales

1. **RN04 (Entidades activas)**: La existencia física se valida estrictamente arrojando `ResourceNotFoundException`. La verificación del estado `ACTIVO` se encuentra delegada en parte a las claves foráneas y en parte a consultas específicas; falta unificar la validación en un único validador transversal de estado.
2. **RN16 (Anulación de cuotas al cancelar matrícula)**: `MatriculaServiceImpl.cancelarMatricula` actualiza el estado de las cuotas pendientes a `ANULADA`, pero requiere una prueba unitaria dedicada que verifique la no regeneración de obligaciones.
3. **RN25 (Protección de eliminación de carrera)**: La aplicación realiza baja lógica (`estado = 'INACTIVA'`). La prohibición de borrado físico ante alumnos vinculados está asegurada por las claves foráneas de PostgreSQL (`RESTRICT` implícito), pero el servicio no arroja previamente un `ConflictException` de negocio personalizado con mensaje amigable.
4. **RN26 (Cambio de carrera con historial académico)**: El endpoint no está presente en el backend Java. Actualmente el frontend maquetó la pantalla utilizando datos simulados (mock).
