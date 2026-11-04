#!/usr/bin/env python3
"""
tools/diccionario-datos.py
Parsea database/01-schema.sql y genera:
1. docs/base-de-datos/diccionario-de-datos.md (tabla de columnas, tipos, nulos, PK, FK, únicos, checks)
2. docs/base-de-datos/modelo-relacional.md (diagrama Mermaid erDiagram validado con mmdc)
"""

import os
import re
import subprocess
import sys

SCHEMA_PATH = "database/01-schema.sql"
OUTPUT_DIR = "docs/base-de-datos"
DICCIONARIO_PATH = os.path.join(OUTPUT_DIR, "diccionario-de-datos.md")
MODELO_PATH = os.path.join(OUTPUT_DIR, "modelo-relacional.md")

CATEGORIAS = {
    "Usuarios y Roles": ["rol", "usuario"],
    "Catálogos": [
        "estado_persona", "estado_matricula", "estado_cuota",
        "tipo_cuota", "medio_pago", "tipo_evaluacion", "estado_asistencia"
    ],
    "Estructura Académica": [
        "carrera", "plan_estudio", "materia", "plan_materia",
        "correlativa", "ciclo_lectivo", "curso"
    ],
    "Personas y Asignaciones": ["alumno", "docente", "curso_materia"],
    "Inscripciones, Matrículas y Aranceles": ["inscripcion", "matricula", "cuota", "pago"],
    "Desempeño Académico": ["evaluacion", "calificacion", "asistencia", "mesa_examen", "inscripcion_mesa"],
    "Trámites y Declaraciones": ["declaracion_jurada", "declaracion_materia", "solicitud", "documentacion"]
}

def parse_schema():
    if not os.path.exists(SCHEMA_PATH):
        print(f"Error: No existe {SCHEMA_PATH}", file=sys.stderr)
        sys.exit(1)

    with open(SCHEMA_PATH, "r", encoding="utf-8") as f:
        sql = f.read()

    # Quitar comentarios de línea e inline
    clean_lines = []
    for line in sql.split("\n"):
        line_clean = re.sub(r"--.*$", "", line).strip()
        if line_clean:
            clean_lines.append(line_clean)
    clean_sql = "\n".join(clean_lines)

    # Capturar bloques CREATE TABLE <nombre> ( ... );
    table_matches = re.findall(r"CREATE TABLE\s+(\w+)\s*\((.*?)\);", clean_sql, re.DOTALL | re.IGNORECASE)

    tables = {}

    for tname, body in table_matches:
        tname = tname.lower()
        # Normalizar líneas del cuerpo
        raw_items = []
        current_item = []
        for line in body.split("\n"):
            line_str = line.strip()
            if not line_str:
                continue
            current_item.append(line_str)
            if line_str.endswith(",") or line_str.endswith(";"):
                combined = " ".join(current_item).rstrip(",").rstrip(";")
                raw_items.append(combined)
                current_item = []
        if current_item:
            combined = " ".join(current_item).rstrip(",").rstrip(";")
            raw_items.append(combined)

        columns = []
        pks = []
        fks = {} # col -> "ref_table(ref_col)"
        uks = set()
        checks = {} # col or table -> check_expr

        # Primera pasada: constraints
        for item in raw_items:
            # PK
            pk_match = re.search(r"CONSTRAINT\s+\w+\s+PRIMARY KEY\s*\(([^)]+)\)", item, re.IGNORECASE)
            if pk_match:
                cols = [c.strip().lower() for c in pk_match.group(1).split(",")]
                pks.extend(cols)
                continue

            # FK
            fk_match = re.search(r"CONSTRAINT\s+\w+\s+FOREIGN KEY\s*\(([^)]+)\)\s+REFERENCES\s+(\w+)\s*\(([^)]+)\)", item, re.IGNORECASE)
            if fk_match:
                from_cols = [c.strip().lower() for c in fk_match.group(1).split(",")]
                to_table = fk_match.group(2).lower()
                to_cols = [c.strip().lower() for c in fk_match.group(3).split(",")]
                if len(from_cols) == 1 and len(to_cols) == 1:
                    fks[from_cols[0]] = f"{to_table}({to_cols[0]})"
                else:
                    ref_str = f"{to_table}({', '.join(to_cols)})"
                    for fc in from_cols:
                        fks[fc] = ref_str
                continue

            # UK
            uk_match = re.search(r"CONSTRAINT\s+\w+\s+UNIQUE\s*\(([^)]+)\)", item, re.IGNORECASE)
            if uk_match:
                cols = [c.strip().lower() for c in uk_match.group(1).split(",")]
                for c in cols:
                    uks.add(c)
                continue

            # CHECK
            ck_match = re.search(r"CONSTRAINT\s+\w+\s+CHECK\s*\((.+)\)", item, re.IGNORECASE)
            if ck_match:
                expr = ck_match.group(1).strip()
                # Asociar a columna si la expresión menciona una columna única
                # o guardarla como restricción general
                found_col = None
                for c_cand in re.findall(r"\b([a-zA-Z_]\w*)\b", expr):
                    if c_cand.lower() not in ("in", "not", "null", "and", "or", "like"):
                        found_col = c_cand.lower()
                        break
                if found_col:
                    checks[found_col] = expr
                else:
                    checks["_table_"] = expr
                continue

        # Segunda pasada: columnas
        for item in raw_items:
            if re.match(r"^CONSTRAINT\b", item, re.IGNORECASE):
                continue
            # Columna: nombre tipo [NOT NULL] [DEFAULT ...]
            col_match = re.match(r"^(\w+)\s+([A-Za-z0-9_()]+(?:\s+GENERATED\s+ALWAYS\s+AS\s+IDENTITY)?)(.*)$", item, re.IGNORECASE)
            if not col_match:
                continue

            col_name = col_match.group(1).lower()
            col_type = col_match.group(2).strip()
            rest = col_match.group(3).strip()

            if "GENERATED ALWAYS AS IDENTITY" in item.upper():
                col_type = "INTEGER (IDENTITY)"
            elif col_type.upper().startswith("INTEGER"):
                col_type = "INTEGER"

            is_not_null = "NOT NULL" in rest.upper() or "GENERATED ALWAYS AS IDENTITY" in item.upper()
            
            columns.append({
                "name": col_name,
                "type": col_type,
                "not_null": is_not_null,
                "rest": rest
            })

        tables[tname] = {
            "columns": columns,
            "pks": pks,
            "fks": fks,
            "uks": uks,
            "checks": checks
        }

    return tables

def generar_diccionario_md(tables):
    lines = [
        "# Diccionario de Datos — Academit",
        "",
        "> Documento generado automáticamente a partir de `database/01-schema.sql` mediante `tools/diccionario-datos.py`.",
        "> Modela las 32 tablas del esquema relacional institucional en PostgreSQL 16+.",
        "",
        "Para ver el diagrama gráfico entidad-relación y sus cardinalidades, consultá el [Modelo Relacional](modelo-relacional.md).",
        "Los datos maestros y registros de desarrollo se describen en [database/README.md](../../database/README.md) y [02-datos-iniciales.sql](../../database/02-datos-iniciales.sql).",
        "",
        "---",
        ""
    ]

    for cat_nombre, table_names in CATEGORIAS.items():
        lines.append(f"## {cat_nombre}")
        lines.append("")
        for tname in table_names:
            if tname not in tables:
                continue
            tdata = tables[tname]
            lines.append(f"### Tabla `{tname}`")
            lines.append("")
            lines.append("| Columna | Tipo | Nulo | PK | FK | Único | Check | Descripción |")
            lines.append("| :--- | :--- | :---: | :---: | :--- | :---: | :--- | :---: |")

            for col in tdata["columns"]:
                cname = col["name"]
                ctype = f"`{col['type']}`"
                nulo = "NO" if col["not_null"] else "SÍ"
                is_pk = "SÍ" if cname in tdata["pks"] else "NO"
                fk = f"`{tdata['fks'][cname]}`" if cname in tdata["fks"] else "—"
                is_uk = "SÍ" if cname in tdata["uks"] else "NO"
                check = f"`{tdata['checks'][cname]}`" if cname in tdata["checks"] else "—"
                desc = "—"

                lines.append(f"| **{cname}** | {ctype} | {nulo} | {is_pk} | {fk} | {is_uk} | {check} | {desc} |")
            lines.append("")

    return "\n".join(lines)

def generar_modelo_relacional_md(tables):
    # Genera diagrama Mermaid erDiagram
    # Mapeo de tipos de SQL a tipos Mermaid
    def map_mermaid_type(sql_type):
        st = sql_type.upper()
        if "INT" in st:
            return "int"
        if "VARCHAR" in st or "TEXT" in st:
            return "string"
        if "DATE" in st or "TIME" in st:
            return "date"
        if "NUMERIC" in st or "DECIMAL" in st:
            return "numeric"
        if "BOOL" in st:
            return "boolean"
        return "string"

    mermaid_lines = ["erDiagram"]

    # Relaciones entre tablas (a partir de las claves foráneas)
    # Sintaxis: parent ||--o{ child : "relación"
    relations_seen = set()

    # Mapeo de relaciones explícitas basadas en el esquema
    for tname, tdata in tables.items():
        for cname, fk_ref in tdata["fks"].items():
            # fk_ref tiene formato "tabla(col)" o "tabla(col1, col2)"
            ref_match = re.match(r"^(\w+)\(([^)]+)\)", fk_ref)
            if not ref_match:
                continue
            parent_table = ref_match.group(1).lower()
            rel_key = (parent_table, tname, cname)
            if rel_key in relations_seen:
                continue
            relations_seen.add(rel_key)

            # Determinar si es 1:1 o 1:N
            # Si la columna FK en tname es PK o UNIQUE, es 1:1 o 0..1:1
            is_unique = (cname in tdata["pks"] and len(tdata["pks"]) == 1) or (cname in tdata["uks"])
            
            # Etiqueta legible
            label = cname.replace("id_", "")
            if is_unique:
                rel_str = f'    {parent_table} ||--|| {tname} : "{label}"'
            else:
                rel_str = f'    {parent_table} ||--o{{ {tname} : "{label}"'
            mermaid_lines.append(rel_str)

    mermaid_lines.append("")

    # Definición de entidades y atributos principales
    for tname, tdata in tables.items():
        mermaid_lines.append(f"    {tname} {{")
        for col in tdata["columns"]:
            cname = col["name"]
            mtype = map_mermaid_type(col["type"])
            flags = []
            if cname in tdata["pks"]:
                flags.append("PK")
            if cname in tdata["fks"]:
                flags.append("FK")
            flag_str = f" {','.join(flags)}" if flags else ""
            mermaid_lines.append(f"        {mtype} {cname}{flag_str}")
        mermaid_lines.append("    }")

    diagram_text = "\n".join(mermaid_lines)

    lines = [
        "# Modelo Relacional — Academit",
        "",
        "> Diagrama Entidad-Relación institucional en Mermaid generado automáticamente a partir de `database/01-schema.sql` mediante `tools/diccionario-datos.py`.",
        "> Validado formalmente con `@mermaid-js/mermaid-cli` (mmdc).",
        "",
        "Para ver la especificación exhaustiva de cada campo, tipo de dato y restricciones, consultá el [Diccionario de Datos](diccionario-de-datos.md).",
        "Los datos de demostración y catálogos iniciales se encuentran documentados en [database/README.md](../../database/README.md) y [database/02-datos-iniciales.sql](../../database/02-datos-iniciales.sql).",
        "",
        "---",
        "",
        "## Diagrama Entidad-Relación (32 tablas)",
        "",
        "```mermaid",
        diagram_text,
        "```",
        ""
    ]

    return "\n".join(lines), diagram_text

def validar_mermaid_con_mmdc(diagram_text):
    import shutil
    temp_mmd = "/tmp/academit-er-check.mmd"
    temp_svg = "/tmp/academit-er-check.svg"
    try:
        with open(temp_mmd, "w", encoding="utf-8") as f:
            f.write(diagram_text)

        # Chequear si mmdc o npx están directamente en PATH o si usamos nix-shell
        if shutil.which("mmdc"):
            cmd = ["mmdc", "-i", temp_mmd, "-o", temp_svg]
            res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
            if res.returncode == 0:
                print("✓ Diagrama Mermaid validado exitosamente con mmdc.")
                return True
        elif shutil.which("npx"):
            cmd = ["npx", "--yes", "@mermaid-js/mermaid-cli", "-i", temp_mmd, "-o", temp_svg]
            res = subprocess.run(cmd, stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True)
            if res.returncode == 0:
                print("✓ Diagrama Mermaid validado exitosamente con npx @mermaid-js/mermaid-cli.")
                return True

        # Fallback a nix-shell
        print("Ejecutando validación mmdc en nix-shell...")
        res_nix = subprocess.run(
            ["nix-shell", "--run", f"npx --yes @mermaid-js/mermaid-cli -i {temp_mmd} -o {temp_svg}"],
            stdout=subprocess.PIPE, stderr=subprocess.PIPE, text=True
        )
        if res_nix.returncode == 0:
            print("✓ Diagrama Mermaid validado exitosamente con mmdc en nix-shell.")
            return True
        else:
            print(f"Error al validar Mermaid con mmdc:\n{res_nix.stderr}\n{res_nix.stdout}", file=sys.stderr)
            return False
    finally:
        for p in (temp_mmd, temp_svg):
            if os.path.exists(p):
                try:
                    os.remove(p)
                except OSError:
                    pass

def main():
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    tables = parse_schema()
    print(f"Tablas parseadas de {SCHEMA_PATH}: {len(tables)}")

    # 1. Diccionario
    diccionario_md = generar_diccionario_md(tables)
    with open(DICCIONARIO_PATH, "w", encoding="utf-8") as f:
        f.write(diccionario_md)
    print(f"✓ Generado {DICCIONARIO_PATH}")

    # 2. Modelo Relacional
    modelo_md, diagram_text = generar_modelo_relacional_md(tables)
    with open(MODELO_PATH, "w", encoding="utf-8") as f:
        f.write(modelo_md)
    print(f"✓ Generado {MODELO_PATH}")

    # 3. Validación mmdc
    if "--no-validate" not in sys.argv:
        valido = validar_mermaid_con_mmdc(diagram_text)
        if not valido:
            print("Error: El diagrama no pasó la validación mmdc.", file=sys.stderr)
            sys.exit(1)

if __name__ == "__main__":
    main()
