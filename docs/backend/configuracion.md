# Configuración del Backend — Academit

> Documento de referencia de perfiles de ejecución, variables de entorno, validación de esquema relacional y monitoreo de salud.

---

## 1. Perfiles de Ejecución (`dev` y `prod`)

La aplicación Spring Boot soporta perfiles de configuración basados en el archivo maestro `application.properties` y variables de entorno estándar.

- **Perfil `dev` (Desarrollo local)**:
  - Activo por defecto al levantar localmente o desde `nix-shell`.
  - Conexión a PostgreSQL local en `localhost:5432` con usuario `academit` y contraseña `academit`.
  - `spring.jpa.show-sql=true` habilitado para inspeccionar sentencias SQL generadas por Hibernate en consola.
  - Clave JWT de desarrollo predeterminada para simplificar pruebas entre frontend y backend.
- **Perfil `prod` (Producción / Despliegue en la nube)**:
  - Se activa configurando `SPRING_PROFILES_ACTIVE=prod`.
  - Desactiva el log de SQL en consola (`spring.jpa.show-sql=false`).
  - Requiere obligatoriamente inyectar secretos mediante variables de entorno (`DB_URL`, `DB_USER`, `DB_PASSWORD`, `JWT_SECRET`).

---

## 2. Tabla de Variables de Entorno

Todas las propiedades sensibles y configurables del sistema se parametrizan mediante variables de entorno:

| Variable | Descripción | Valor por defecto (`dev`) | Ejemplo en producción (`prod`) |
| :--- | :--- | :--- | :--- |
| `DB_URL` | URL JDBC de conexión al servidor PostgreSQL | `jdbc:postgresql://localhost:5432/academit` | `jdbc:postgresql://ep-xyz.aws.neon.tech/academit_prod?sslmode=require` |
| `DB_USER` | Nombre de usuario de la base de datos | `academit` | `academit_admin` |
| `DB_PASSWORD` | Contraseña del usuario de base de datos | `academit` | `* * * (secreto generado)* * *` |
| `JWT_SECRET` | Clave secreta simétrica para firma HMAC-SHA256 | `academit-super-secret-key-for-jwt-signing-2026-very-secure` | `* * * (cadena de al menos 64 bytes)* * *` |
| `JWT_EXPIRATION` | Vigencia de los tokens JWT en milisegundos | `86400000` (24 horas) | `86400000` |
| `ACADEMIT_CUOTAS_CRON`| Expresión cron para marcar cuotas vencidas | `0 0 4 * * *` (todos los días a las 04:00 AM) | `0 0 4 * * *` |
| `ACADEMIT_ZONE_ID` | Zona horaria del sistema para fechas y cron | `America/Argentina/Tucuman` | `America/Argentina/Tucuman` |
| `PORT` | Puerto HTTP de escucha del servidor web | `8080` | `10000` (o el asignado por Render/PaaS) |

---

## 3. Validación de Esquema Relacional (`ddl-auto=validate`)

En `backend/src/main/resources/application.properties` se fija estrictamente:
```properties
spring.jpa.hibernate.ddl-auto=validate
```

### Por qué se usa `validate` y nunca `update` ni `create`:
1. **Control de versiones del esquema**: La base de datos es gobernada exclusivamente por los scripts SQL versionados en el repositorio (`database/01-schema.sql` y `database/02-datos-iniciales.sql`).
2. **Prevención de mutaciones destructivas**: Hibernate no altera, crea ni borra columnas o tablas en tiempo de ejecución.
3. **Validación temprana al arrancar**: Si una entidad `@Entity` JPA o alguno de sus mapeos (`@Column`, `@JoinColumn`, tipos de datos) no coincide exactamente con el esquema de PostgreSQL, la aplicación falla inmediatamente durante el inicio (`SchemaManagementException`), evitando inconsistencias en caliente.

---

## 4. Monitoreo y Salud para Render y Plataformas Cloud

Para despliegues en plataformas PaaS como Render, Railway o entornos contenerizados:

1. **Detección de Puerto**: La aplicación escucha en el puerto indicado por la variable estándar `PORT` (usada por Render). Si no está presente, utiliza el puerto `8080`.
2. **Health Check**:
   - Para verificar que el servicio está levantado y listo para recibir tráfico, se puede consultar `POST /api/auth/login` o `OPTIONS /api/auth/login` (responde de inmediato sin requerir token).
   - En caso de incorporar `spring-boot-starter-actuator`, el endpoint estándar es `GET /actuator/health`.
3. **Arranque en Contenedor / Render**:
   - Comando de compilación: `./gradlew bootJar -x test`
   - Comando de ejecución: `java -Dserver.port=$PORT -jar build/libs/academit-0.0.1-SNAPSHOT.jar`
   - La base de datos asociada en la nube debe inicializarse ejecutando `database/01-schema.sql` y `database/02-datos-iniciales.sql`.
