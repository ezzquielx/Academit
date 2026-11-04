{ pkgs ? import <nixpkgs> { } }:

# Entorno de desarrollo de Academit: Java, Gradle, Node (React), PostgreSQL (servidor y cliente).
# Uso, desde la raiz del proyecto:  nix-shell
# La primera vez descomprime el .zip de Spring Initializr en backend/ y crea el resto de la estructura.

pkgs.mkShell {
  packages = with pkgs; [
    jdk21
    gradle
    nodejs
    unzip
    postgresql   # psql, initdb, pg_ctl, pg_isready y utilidades de PostgreSQL
    git
    tree
  ];

  JAVA_HOME = "${pkgs.jdk21.home}";

  shellHook = ''
    mkdir -p backend frontend database docs

    # ---- Backend: descomprime el .zip de Spring la primera vez ----
    if [ ! -f backend/build.gradle ] && [ ! -f backend/build.gradle.kts ]; then
      zip=$(ls -1 *.zip 2>/dev/null | head -n 1)
      if [ -n "$zip" ]; then
        echo ">> Descomprimiendo $zip en backend/"
        tmp=$(mktemp -d)
        unzip -q "$zip" -d "$tmp"
        if [ "$(ls -1A "$tmp" | wc -l)" -eq 1 ] && [ -d "$tmp/$(ls -1A "$tmp")" ]; then
          cp -a "$tmp/$(ls -1A "$tmp")/." backend/
        else
          cp -a "$tmp/." backend/
        fi
        rm -rf "$tmp"
        chmod +x backend/gradlew 2>/dev/null || true
      else
        echo ">> No encontre ningun .zip de Spring en la raiz; backend/ queda vacio."
      fi
    fi

    # ---- Recordatorio del driver de PostgreSQL ----
    if [ -f backend/build.gradle ] && ! grep -q 'org.postgresql' backend/build.gradle; then
      echo ">> Falta el driver de PostgreSQL. Agregalo en backend/build.gradle, dentro de dependencies:"
      echo "     runtimeOnly 'org.postgresql:postgresql'"
    fi

    # ---- Base de datos: servidor local y funciones de administracion ----
    export PGDATA="$PWD/.pgdata"
    export PGSOCKET="/tmp"
    export PGLOG="$PGDATA/server.log"
    export PGPORT=5432
    export PGHOST="127.0.0.1"

    db-start() {
      if [ ! -d "$PGDATA" ] || [ ! -f "$PGDATA/PG_VERSION" ]; then
        echo "El cluster no esta inicializado. Ejecuta primero: db-init"
        return 1
      fi

      if pg_ctl -D "$PGDATA" status >/dev/null 2>&1; then
        echo "El servidor PostgreSQL ya se encuentra en ejecucion."
        return 0
      fi

      if (echo > /dev/tcp/127.0.0.1/5432) 2>/dev/null; then
        echo "Error: el puerto 5432 ya esta ocupado por otro proceso. No se puede iniciar PostgreSQL."
        return 1
      fi

      echo "Iniciando servidor PostgreSQL..."
      pg_ctl -D "$PGDATA" -l "$PGLOG" -o "-h 127.0.0.1 -p 5432 -k /tmp" start

      local timeout=10
      while [ $timeout -gt 0 ] && ! pg_isready -h 127.0.0.1 -p 5432 >/dev/null 2>&1; do
        sleep 0.5
        timeout=$((timeout - 1))
      done

      if pg_isready -h 127.0.0.1 -p 5432 >/dev/null 2>&1; then
        echo "PostgreSQL iniciado correctamente en 127.0.0.1:5432."
      else
        echo "Error: no se pudo iniciar PostgreSQL. Revisa el log en $PGLOG."
        return 1
      fi
    }

    db-stop() {
      if [ ! -d "$PGDATA" ]; then
        echo "No existe el cluster en $PGDATA."
        return 0
      fi

      if ! pg_ctl -D "$PGDATA" status >/dev/null 2>&1; then
        echo "El servidor PostgreSQL no esta en ejecucion."
        return 0
      fi

      echo "Deteniendo servidor PostgreSQL..."
      pg_ctl -D "$PGDATA" -m fast stop
    }

    db-init() {
      if [ ! -d "$PGDATA" ] || [ ! -f "$PGDATA/PG_VERSION" ]; then
        echo "Inicializando cluster PostgreSQL en $PGDATA..."
        initdb -D "$PGDATA" --no-locale --encoding=UTF8 -A trust
      else
        echo "El cluster PostgreSQL en $PGDATA ya existe."
      fi

      # Iniciar el servidor si no esta corriendo
      if ! pg_isready -h 127.0.0.1 -p 5432 >/dev/null 2>&1; then
        db-start || return 1
      fi

      echo "Configurando rol y base de datos academit..."
      # Crear rol si no existe y asegurar contrasenia
      psql -h 127.0.0.1 -p 5432 postgres -tc "SELECT 1 FROM pg_roles WHERE rolname = 'academit'" | grep -q 1 || \
        psql -h 127.0.0.1 -p 5432 postgres -c "CREATE ROLE academit WITH LOGIN PASSWORD 'academit' CREATEDB;"
      psql -h 127.0.0.1 -p 5432 postgres -c "ALTER ROLE academit WITH LOGIN PASSWORD 'academit' CREATEDB;" >/dev/null 2>&1

      # Crear base de datos si no existe
      psql -h 127.0.0.1 -p 5432 postgres -tc "SELECT 1 FROM pg_database WHERE datname = 'academit'" | grep -q 1 || \
        psql -h 127.0.0.1 -p 5432 postgres -c "CREATE DATABASE academit OWNER academit;"

      # Asegurar permisos sobre la base y el esquema public
      psql -h 127.0.0.1 -p 5432 postgres -c "GRANT ALL PRIVILEGES ON DATABASE academit TO academit;" >/dev/null 2>&1
      psql -h 127.0.0.1 -p 5432 -d academit -c "GRANT ALL ON SCHEMA public TO academit;" >/dev/null 2>&1

      if [ -f database/01-schema.sql ]; then
        echo "Ejecutando database/01-schema.sql..."
        PGPASSWORD=academit psql -h 127.0.0.1 -p 5432 -U academit -d academit -f database/01-schema.sql
      fi

      if [ -f database/02-datos-iniciales.sql ]; then
        echo "Ejecutando database/02-datos-iniciales.sql..."
        PGPASSWORD=academit psql -h 127.0.0.1 -p 5432 -U academit -d academit -f database/02-datos-iniciales.sql
      fi

      echo "Inicializacion completada exitosamente."
    }

    db-psql() {
      PGPASSWORD=academit psql -h 127.0.0.1 -p 5432 -U academit -d academit "$@"
    }

    db-reset() {
      if ! pg_isready -h 127.0.0.1 -p 5432 >/dev/null 2>&1; then
        echo "PostgreSQL no esta en ejecucion. Iniciando..."
        db-start || return 1
      fi

      echo "Reiniciando base de datos academit..."
      # Cerrar conexiones previas a academit
      psql -h 127.0.0.1 -p 5432 postgres -c "SELECT pg_terminate_backend(pid) FROM pg_stat_activity WHERE datname = 'academit' AND pid <> pg_backend_pid();" >/dev/null 2>&1 || true
      psql -h 127.0.0.1 -p 5432 postgres -c "DROP DATABASE IF EXISTS academit;"
      psql -h 127.0.0.1 -p 5432 postgres -c "CREATE DATABASE academit OWNER academit;"
      psql -h 127.0.0.1 -p 5432 postgres -c "GRANT ALL PRIVILEGES ON DATABASE academit TO academit;" >/dev/null 2>&1
      psql -h 127.0.0.1 -p 5432 -d academit -c "GRANT ALL ON SCHEMA public TO academit;" >/dev/null 2>&1

      if [ -f database/01-schema.sql ]; then
        echo "Ejecutando database/01-schema.sql..."
        PGPASSWORD=academit psql -h 127.0.0.1 -p 5432 -U academit -d academit -f database/01-schema.sql
      fi

      if [ -f database/02-datos-iniciales.sql ]; then
        echo "Ejecutando database/02-datos-iniciales.sql..."
        PGPASSWORD=academit psql -h 127.0.0.1 -p 5432 -U academit -d academit -f database/02-datos-iniciales.sql
      fi

      echo "Base de datos academit reiniciada correctamente."
    }

    # ---- Base de datos: archivos vacios para el script y los datos de prueba ----
    [ -f database/01-schema.sql ] || printf -- '-- Script de creacion de la base de datos (PostgreSQL)\n' > database/01-schema.sql
    [ -f database/02-datos-iniciales.sql ] || printf -- '-- Datos iniciales de prueba (PostgreSQL)\n' > database/02-datos-iniciales.sql

    # ---- Archivos de la raiz ----
    if [ ! -f .gitignore ]; then
      cat > .gitignore <<'EOF'
    *.zip
    node_modules/
    dist/
    build/
    .gradle/
    .idea/
    .vscode/
    .pgdata/
    *.log
    .env
    EOF
    fi

    if [ ! -f README.md ]; then
      printf '# Academit\n\nSistema web de gestion de un instituto educativo (Taller 3 - TP integrador).\n\n- backend/: Spring Boot + Gradle\n- frontend/: React (Vite)\n- database/: scripts SQL (PostgreSQL)\n- docs/: analisis y diagramas UML\n' > README.md
    fi

    # ---- Frontend: se crea a pedido con el comando init-frontend ----
    init-frontend() {
      if [ -f frontend/package.json ]; then
        echo "frontend/ ya tiene un proyecto."
        return 0
      fi
      npm create vite@latest frontend -- --template react \
        && (cd frontend && npm install && npm install bootstrap react-bootstrap react-router-dom axios)
    }

    if [ ! -f frontend/package.json ]; then
      echo ">> El frontend todavia no existe. Para crearlo ejecuta:  init-frontend"
    fi

    echo ""
    echo "Java:   $(java -version 2>&1 | head -n 1)"
    echo "Node:   $(node --version)"
    echo "Gradle: usa ./gradlew dentro de backend/"
    echo "PostgreSQL: usa db-init, db-start, db-stop, db-psql o db-reset"
  '';
}
