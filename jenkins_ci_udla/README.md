# HelpDesk UDLA: Gestión de incidencias con Jenkins CI

**Proyecto funcional, no una calculadora.** Aplicación web para registrar, consultar, filtrar, actualizar, eliminar y dar seguimiento a incidencias de soporte técnico. Guarda los datos localmente y muestra estadísticas por estado.

## Tecnologías
- Java 17, servidor HTTP incluido en el JDK, interfaz HTML/CSS/JavaScript.
- Maven para compilar y generar un archivo JAR ejecutable.
- JUnit 5 para **13 pruebas automatizadas** (11 unitarias y 2 de integración HTTP).
- Jenkins con **tres etapas**: checkout privado, compilación/pruebas, notificación por Telegram.
- Sin dependencias de ejecución externas (facilita instalarlo y verificarlo).

## Funcionalidades
1. Crear incidencias con título, descripción y prioridad: baja, media, alta, crítica.
2. Listar todas o filtrar por abiertas, en progreso o resueltas.
3. Cambiar el estado de una incidencia y eliminarla.
4. Mostrar métricas de incidencias por estado.
5. Conservar información en `data/incidencias.properties` cuando reinicias la aplicación.

## Cómo ejecutarlo
Requisitos: JDK 17+ y Maven 3.8+.

```sh
cd jenkins_ci_udla
mvn clean verify
java -jar target/gestion-incidencias-1.0.0.jar
```

En el navegador abre **http://localhost:8080**. Para cambiar el puerto define la variable de entorno `HELPDESK_PORT`; para cambiar dónde guardar los datos define `HELPDESK_DATA`.

## API usada por el navegador

| Método | Ruta | Operación |
|---|---|---|
| GET | `/` | Interfaz web |
| GET | `/api/incidencias` | Listar incidencias |
| GET | `/api/incidencias?estado=ABIERTO` | Filtrar |
| POST | `/api/incidencias` | Crear (form-urlencoded: `titulo`, `descripcion`, `prioridad`) |
| POST | `/api/incidencias/{id}/estado` | Cambiar estado (form-urlencoded: `estado`) |
| DELETE | `/api/incidencias/{id}` | Eliminar |
| GET | `/api/estadisticas` | Métricas |

Es un prototipo académico local: el servidor solo escucha en `127.0.0.1` y no implementa autenticación. No usar directamente en producción ni abrirlo a Internet.

## Configuración de Jenkins: PRIMERA TAREA

### Paso 1 — Subir los archivos al repositorio existente
El repositorio GitHub ya existente es:
`https://github.com/ElPaul593/Automatizacion_JENKINS`

La carpeta de este ZIP llamada **`jenkins_ci_udla`** debe sustituir la carpeta del mismo nombre en la **raíz** del repositorio GitHub. Elimina los archivos `Calculadora.java` y `CalculadoraTest.java` anteriores (para que no quede la calculadora).

No cambies el nombre de la carpeta, porque el Jenkinsfile utiliza `jenkins_ci_udla/pom.xml`.

### Paso 2 — Mantener la integración privada
El repo debe volver a modo **Private** al realizar la demostración de ese requisito. En Jenkins debe existir una credencial de tipo *Username with password* con ID `github-private`: el usuario GitHub y un token PAT con permiso de lectura del repositorio (Contents: Read-only). No subas el token a GitHub.

### Paso 3 — Configurar Jenkins (Windows)
Este Jenkinsfile conserva los comandos Windows (`bat` y `powershell`) del proyecto original. El agente de Jenkins debe usar Windows y disponer de Git, Java 17+, Maven (en Jenkins configurado con el nombre exacto `Maven3`) y PowerShell.

En el job, si usas **Pipeline script from SCM**:
- Repository URL: `https://github.com/ElPaul593/Automatizacion_JENKINS.git`
- Credentials: `github-private`
- Branch: `*/main`
- **Script Path: `jenkins_ci_udla/Jenkinsfile`** (no `Jenkinsfile` a secas).

Si en cambio tu job usa **Pipeline script** pegado directamente en la interfaz, actualiza el código ahí en lugar de depender del archivo del repositorio.

### Paso 4 — Verificar la compilación y los tests
Ejecuta `Build Now`. La etapa 2 usa:

```bat
mvn -B -ntp -f jenkins_ci_udla/pom.xml clean verify
```

En caso de éxito, Jenkins publica las pruebas JUnit y archiva el JAR de `target`.

### Paso 5 — Configurar y verificar Telegram
Los secretos se guardan en credenciales Jenkins de tipo *Secret text*, con IDs:
- `telegram-bot-token`
- `telegram-chat-id`

En la etapa 3 se envía un mensaje `EXITOSA` o `FALLIDA`. Para demostrar el caso de fallo, modifica temporalmente la expectativa de una prueba JUnit, haz commit y push, y ejecuta el pipeline; después revierte el cambio y vuelve a ejecutar. **No dejes las pruebas rotas como versión final.**

## Evidencias sugeridas
- Repositorio privado con código del HelpDesk.
- Aplicación en navegador creando y resolviendo incidencias.
- Tres etapas del pipeline en Jenkins.
- Log de `mvn clean verify` y reporte JUnit con 13 pruebas.
- JAR generado y archivado.
- Telegram notificando ejecución exitosa y fallida.

## Pensado para la SEGUNDA tarea (sin implementarla aún)
Se puede añadir posteriormente análisis estático, reglas por ramas, ejecución paralela, cache Maven y librerías compartidas. El proyecto base y las pruebas se conservarán.
