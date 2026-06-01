# SmartCart — Despliegue con Docker

Guía paso a paso para levantar la aplicación completa (Spring Boot + Angular + PostgreSQL) usando Docker.

---

## 1. Instalar Docker Desktop

Descargar e instalar **Docker Desktop** desde la página oficial:

- Windows / Mac: https://www.docker.com/products/docker-desktop/
- Linux: https://docs.docker.com/engine/install/

Después de instalar:

1. Abrir Docker Desktop.
2. Esperar a que el ícono diga **"Docker Desktop is running"** (abajo a la izquierda).
3. Verificar que funciona abriendo una terminal y ejecutando:

   ```bash
   docker --version
   docker compose version
   ```

   Ambos deben mostrar una versión sin errores.

---

## 2. Clonar el proyecto

```bash
git clone <URL_DEL_REPOSITORIO>
cd SmartCart
```

---

## 3. Levantar la aplicación

Desde la raíz del proyecto (`C:\FPSWI\SmartCart`), ejecutar:

```bash
docker compose up --build
```

Esto va a:

1. Descargar las imágenes base (`node`, `maven`, `eclipse-temurin`, `postgres`).
2. Compilar Angular dentro de un contenedor.
3. Compilar Spring Boot dentro de un contenedor.
4. Levantar PostgreSQL y la app.

**La primera vez tarda varios minutos** (descarga e instala todo). Las próximas son mucho más rápidas.

Cuando veas en consola algo como:

```
smartcart-app  | Started EcommerceApplication in X.X seconds
```

…la app está lista.

---

## 4. Abrir la aplicación

En el navegador:

```
http://localhost:8080
```

Eso es todo. Angular y el backend corren juntos en el mismo puerto.

---

## 5. Detener la aplicación

En la terminal donde está corriendo, presionar:

```
Ctrl + C
```

O desde otra terminal:

```bash
docker compose down
```

Para detener **y borrar la base de datos** (empezar desde cero):

```bash
docker compose down -v
```

---

## 6. Volver a levantarla (sin recompilar)

```bash
docker compose up
```

Si cambiaste código y querés recompilar:

```bash
docker compose up --build
```

---

## Variables de entorno opcionales

El `docker-compose.yml` tiene defaults para todo. Si querés personalizar (por ejemplo, configurar el correo SMTP para los recibos), creá un archivo `.env` en la raíz:

```env
DB_PASSWORD=tu_password
JWT_SECRET=otra_clave_super_secreta
MAIL_USERNAME=tucorreo@gmail.com
MAIL_PASSWORD=tu_app_password_de_gmail
MAIL_FROM=tucorreo@gmail.com
```

Docker Compose lo carga automáticamente al hacer `up`.

---

## Comandos útiles

| Acción | Comando |
|--------|---------|
| Ver logs en vivo | `docker compose logs -f app` |
| Ver estado de los contenedores | `docker compose ps` |
| Reiniciar solo la app | `docker compose restart app` |
| Entrar a la base de datos | `docker compose exec db psql -U postgres -d smartcart` |
| Borrar imágenes antiguas | `docker image prune` |

---

## Problemas comunes

**"Port 8080 is already allocated"** → Hay algo corriendo en el puerto 8080. Cerrá lo que esté usando ese puerto (otra instancia de la app, IntelliJ, etc.) o cambiá el puerto en `docker-compose.yml` (ej. `"8081:8080"`).

**"Port 5432 is already allocated"** → Tenés PostgreSQL instalado localmente corriendo. Detenelo o cambiá el puerto en `docker-compose.yml`.

**El build tarda mucho** → Normal la primera vez. Maven descarga todas las dependencias dentro del contenedor.

---

# Integración Continua (CI/CD) — GitHub Actions

El pipeline está definido en [`.github/workflows/ci.yml`](.github/workflows/ci.yml) y se ejecuta
automáticamente en cada `push` y cada Pull Request hacia `main`. Consta de cuatro etapas:

```
unit-tests ──┬──► sonarqube
             ├──► selenium
             └──► jmeter
```

| Etapa | Herramienta | Qué hace |
|-------|-------------|----------|
| **unit-tests** | JUnit 5 + Mockito + JaCoCo | Compila, ejecuta las pruebas unitarias (H2 en memoria), genera el reporte de cobertura y empaqueta el `.jar`. |
| **sonarqube** | SonarQube / SonarCloud | Análisis estático de calidad y cobertura. Se omite si no hay token configurado. |
| **selenium** | Selenium WebDriver | Levanta la app (con PostgreSQL) y valida la interfaz web en un Chrome headless. |
| **jmeter** | Apache JMeter | Levanta la app y ejecuta una prueba de carga sobre los endpoints públicos. |

Los resultados (reportes de tests, cobertura JaCoCo, reporte HTML de JMeter, logs) se publican como
**artifacts** descargables en la pestaña *Actions* de cada ejecución.

## 1. Pruebas unitarias

Ya existen en `src/test/java`. Para ejecutarlas en local:

```bash
./mvnw verify          # tests + cobertura JaCoCo en target/site/jacoco/index.html
```

## 2. SonarQube

El análisis requiere dos secretos del repositorio
(*Settings → Secrets and variables → Actions*):

| Nombre | Tipo | Valor |
|--------|------|-------|
| `SONAR_TOKEN` | Secret | Token de tu instancia de SonarQube / SonarCloud |
| `SONAR_HOST_URL` | Secret | URL del servidor (ej. `https://sonarcloud.io`) |
| `SONAR_ORGANIZATION` | Variable | (Solo SonarCloud) la organización |

Si `SONAR_TOKEN` no está definido, la etapa **no falla**: simplemente se omite con un aviso.

En local (con un SonarQube en `http://localhost:9000`):

```bash
./mvnw verify org.sonarsource.scanner.maven:sonar-maven-plugin:sonar \
  -Dsonar.host.url=http://localhost:9000 -Dsonar.token=TU_TOKEN
```

## 3. Selenium (E2E)

Las pruebas están en `src/test/java/com/tienda/ecommerce/e2e` y llevan la etiqueta `@Tag("e2e")`,
por lo que **no** corren en la build normal (necesitan la app levantada y un navegador).

Para ejecutarlas en local, con la app corriendo en `http://localhost:8080` y Chrome instalado:

```bash
./mvnw test -Dgroups=e2e -DexcludedGroups= -De2e.base-url=http://localhost:8080
```

## 4. JMeter (carga)

El plan de prueba está en [`src/test/jmeter/smartcart_load_test.jmx`](src/test/jmeter/smartcart_load_test.jmx).
El plugin de Maven descarga JMeter automáticamente. Con la app corriendo:

```bash
./mvnw clean -Pjmeter -DskipTests verify
```

> El `clean` evita el error *"folder is not empty"* de JMeter al re-ejecutar
> (JMeter no sobreescribe una carpeta de reporte que ya tiene contenido).

El reporte HTML se genera en `target/jmeter/reports/`. Parámetros ajustables (con `-D`):
`threads` (usuarios, 10), `loops` (iteraciones, 5), `rampup` (segundos, 5), `host` (por
defecto `127.0.0.1`), `port` (8080).
