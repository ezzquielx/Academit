#!/usr/bin/env python3
"""
tools/prompts-modulos.py
Genera un prompt por cada módulo CRUD del backend que no tenga archivo
dedicado, a partir de docs/backend/prompts/_plantilla-crud.md.

El diccionario MODULOS se edita a mano: si cambia una regla o dependencia,
se actualiza acá y se vuelve a correr el script (los prompts dedicados se
incluyeron como archivos sueltos y este script nunca los toca).

Uso: python3 tools/prompts-modulos.py
"""

from pathlib import Path

RAIZ = Path(__file__).resolve().parent.parent
PROMPTS = RAIZ / 'docs' / 'backend' / 'prompts'
ENDPOINTS_CSV = RAIZ / 'docs' / 'frontend' / 'endpoints.csv'

# Diccionario editable: módulos CRUD sin prompt dedicado.
# cada entrada: rama, refs, depende, reglas. La clave es el prefijo de ruta en
# endpoints.csv y varios prefijos pueden compartir módulo (planes + materias).
MODULOS = {
    'ciclos-lectivos': {
        'rama': 'feature/back-ciclos-lectivos',
        'refs': 'RF20, RN16',
        'depende': 'nada (módulo base)',
        'reglas': (
            '- Año único: no dos ciclos con el mismo anio (uk_ciclo_anio; mensaje legible al usuario).\n'
            '- fechaFin posterior a fechaInicio (ck_ciclo_fechas).\n'
            '- Sin DELETE: un ciclo no se borra, se cierra con estado CERRADO.\n'
            '- Estados del catálogo: ACTIVO, CERRADO, PLANIFICADO; si no se envía estado, nace PLANIFICADO.\n'
            '- nombre y estado son persistidos según el contrato (columnas ya agregadas al esquema).\n'
            '- Ya implementado e integrado en develop (PR #10): sirve de referencia de estilo para los demás módulos CRUD.'
        ),
    },
    'carreras': {
        'rama': 'feature/back-carreras',
        'refs': 'RF53, RF55, RN22',
        'depende': 'nada (módulo base)',
        'reglas': (
            '- Nombre de carrera único (mensaje legible si se repite).\n'
            '- duracionAnios mayor que 0.\n'
            '- DELETE es baja lógica (activa=false); no borra la fila. Antes de aplicar la baja, si hay alumnos activos o cursos vigentes de esa carrera, rechazar con conflicto.\n'
            '- GET /api/carreras/{id}/planes y GET /api/carreras/{id}/cursos devuelven solo los planes y cursos de esa carrera.\n'
            '- Roles según x-roles de openapi.yaml (consultar contra el contrato; no inventar).'
        ),
    },
    'planes-estudio y materias': {
        'rama': 'feature/back-planes-materias',
        'refs': 'RF47, RF48, RN24, RN22',
        'depende': 'carreras',
        'reglas': (
            '- Módulo doble: listados del plan (endpoints del contrato más arriba) y alta/edición de materias con POST y PUT /api/materias.\n'
            '- GET /api/planes-estudio/{id}/materias devuelve las materias del plan con anioPlan y sus correlativas.\n'
            '- Nombre de materia único; baja lógica (activa=false) si tiene historial: nunca DELETE físico.\n'
            '- Una correlativa no puede apuntar a la misma materia y solo puede vincular materias del mismo plan (RN24).\n'
            '- Las correlativas no forman un ciclo (circularidad no permitida).\n'
            '- El plan pertenece a una única carrera (RN22).'
        ),
    },
    'cursos': {
        'rama': 'feature/back-cursos',
        'refs': 'RF14, RF15, RF16, RF21, RN03, RN23',
        'depende': 'ciclos-lectivos y carreras',
        'reglas': (
            '- capacidadMaxima mayor que 0 (ck_curso_capacidad).\n'
            '- turno del catálogo MANANA, TARDE o NOCHE (ck_curso_turno; sin tilde).\n'
            '- Nombre único por (nombre, ciclo lectivo, carrera) (uk_curso_nombre); mensaje legible.\n'
            '- cuposDisponibles es campo calculado del response (capacidadMaxima - inscriptos activos); nunca se persiste.\n'
            '- Validar cupo al inscriptir es del módulo inscripciones (RN03), no de cursos.\n'
            '- El curso pertenece a una única carrera y a un único ciclo lectivo (RN23).'
        ),
    },
    'alumnos': {
        'rama': 'feature/back-alumnos',
        'refs': 'RF06, RF07, RF08, RF09, RF34, RF44, RN13, RN22',
        'depende': (
            'carreras y planes-estudio. La situación académica y la administrativa '
            'consultan asistencias, calificaciones, matrículas y cuotas: hacelas con lo '
            'que exista integrado y dejá lo faltante como TODO documentado en '
            'docs/estado-del-proyecto.md (sección decision abierta)'
        ),
        'reglas': (
            '- DNI único; email con formato válido.\n'
            '- Estado ACTIVO (default) o INACTIVO; DELETE es baja lógica (INACTIVO) y conflicto si tiene historial académico que no admite baja (RN13).\n'
            '- Plan de estudios obligatorio (RN22: un único plan vigente de una carrera).\n'
            '- Un alumno autenticado solo consulta sus propios datos (comparar id del token).\n'
            '- GET /api/alumnos/{id}/situacion-academica: materias con notas y porcentaje de asistencia, y condición REGULAR calculada (asistencia >= 75 % y notas >= 4), nunca persistida.\n'
            '- GET /api/alumnos/{id}/situacion-administrativa: matrícula vigente, cuotas, estadoPago calculado (no guardado) e importe adeudado.\n'
            '- Roles: ADMINISTRADOR y ADMINISTRATIVO gestionan todo; DOCENTE lectura básica; ALUMNO solo lo propio (verificar x-roles del contrato).'
        ),
    },
    'docentes': {
        'rama': 'feature/back-docentes',
        'refs': 'RF10, RF11, RF12, RF13',
        'depende': 'nada (módulo base)',
        'reglas': (
            '- DNI único; estado ACTIVO o INACTIVO.\n'
            '- Baja lógica (INACTIVO): no borrar el docente si tiene asignaciones o evaluaciones históricas.\n'
            '- ADMINISTRATIVO solo consulta; alta, edición y baja son de ADMINISTRADOR.'
        ),
    },
    'asignaciones': {
        'rama': 'feature/back-asignaciones',
        'refs': 'RF19, RF58',
        'depende': 'docentes, cursos y materias',
        'reglas': (
            '- Atributos: idDocente, idCurso, idMateria y rolDocente del catálogo TITULAR, ADJUNTO o JTP.\n'
            '- Unicidad por (curso, materia, docente) (uk_cursomateria): repetida = conflicto con mensaje legible.\n'
            '- El docente debe estar ACTIVO.\n'
            '- No se elimina una asignación que tenga evaluaciones cargadas (conflicto: protege el historial).\n'
            '- ADMINISTRATIVO solo consulta; alta y baja son de ADMINISTRADOR.'
        ),
    },
    'solicitudes': {
        'rama': 'feature/back-solicitudes',
        'refs': 'RF56, RF57',
        'depende': 'alumnos',
        'reglas': (
            '- Tipos y estados EXACTOS de docs/frontend/entidades.json: no inventar códigos ni valores.\n'
            '- El alumno crea solicitudes propias y consulta solo las suyas.\n'
            '- PATCH /api/solicitudes/{id} resuelve (estado, respuesta y fechaResolucion obligatorios al resolver) y es de ADMINISTRATIVO o ADMINISTRADOR.\n'
            '- La declaración jurada NO es un tipo de solicitud (módulo propio en openapi.yaml).\n'
            '- El efecto de resolver un cambio de carrera sobre el plan del alumno: si el análisis no lo define, dejalo como decisión abierta en docs/estado-del-proyecto.md y seguí.'
        ),
    },
    'documentacion': {
        'rama': 'feature/back-documentacion',
        'refs': 'RF34 (el análisis referencia situación administrativa; si el legajo no tiene RF propia, usá los RF y RN del análisis)',
        'depende': 'alumnos',
        'reglas': (
            '- Legajo del alumno con tipos exactos de entidades.json (DNI, TITULO_SECUNDARIO, APTO_MEDICO, FOTOS o los que estén en el catálogo).\n'
            '- Cada registro lleva documento obligatorio, estado y fechaEntrega.\n'
            '- El alumno consulta solo su legajo; ADMINISTRATIVO y ADMINISTRADOR ven todo y actualizan con PUT /api/documentacion/{id}.\n'
            '- El PUT es la única escritura del contrato: no existe POST ni DELETE; no los inventes.'
        ),
    },
}

# Prefijo de ruta -> clave del diccionario (varios prefijos pueden compartir módulo)
PREFIJOS = [
    ('/api/ciclos-lectivos', 'ciclos-lectivos'),
    ('/api/carreras', 'carreras'),
    ('/api/planes-estudio', 'planes-estudio y materias'),
    ('/api/materias', 'planes-estudio y materias'),
    ('/api/cursos', 'cursos'),
    ('/api/alumnos', 'alumnos'),
    ('/api/docentes', 'docentes'),
    ('/api/asignaciones', 'asignaciones'),
    ('/api/solicitudes', 'solicitudes'),
    ('/api/documentacion', 'documentacion'),
]


def endpoints_por_modulo():
    bloques = {clave: [] for clave in MODULOS}
    with ENDPOINTS_CSV.open(encoding='utf-8') as f:
        for line in f:
            partes = [p.strip() for p in line.strip().split(',')]
            if len(partes) < 3:
                continue
            metodo, ruta = partes[0], partes[1]
            for prefijo, clave in PREFIJOS:
                if ruta.startswith(prefijo):
                    bloques[clave].append(f'- {metodo} {ruta}')
                    break
    return bloques


def generar():
    plantilla = (PROMPTS / '_plantilla-crud.md').read_text(encoding='utf-8')
    bloques = endpoints_por_modulo()
    for clave, config in MODULOS.items():
        destino = PROMPTS / (config['rama'].replace('feature/back-', '') + '.md')
        # Nunca pisar los prompts dedicados: solo los archivos que derivan de estas ramas
        contenido = (
            plantilla
            .replace('{MODULO}', clave)
            .replace('{RAMA}', config['rama'])
            .replace('{REFS}', config['refs'])
            .replace('{ENDPOINTS}', '\n'.join(bloques[clave]))
            .replace('{DEPENDE}', config['depende'])
            .replace('{REGLAS}', config['reglas'])
        )
        destino.write_text(contenido, encoding='utf-8')
        print(f'generado: docs/backend/prompts/{destino.name}')


if __name__ == '__main__':
    generar()
