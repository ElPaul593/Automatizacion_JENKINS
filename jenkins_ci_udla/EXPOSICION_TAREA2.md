# Guia de exposicion - Tarea 2 | Jenkins CI optimizado

**Proyecto:** HelpDesk UDLA, gestion de incidencias de soporte tecnico. **Rama de la tarea 2:** `feature/pipeline-optimizacion`. **Tarea 1:** permanece en `main`.

## Que demostrar

La primera tarea ya integraba GitHub, Maven, 13 tests JUnit y Telegram. La segunda agrega Checkstyle y PMD y **cuatro estrategias de optimizacion**: (1) ejecucion paralela, (2) cache Maven, (3) ejecucion selectiva y (4) funciones reutilizables (modularidad).

**No hay despliegue a produccion**: Jenkins compila, valida, genera un JAR, publica resultados y notifica.

## Las cuatro etapas principales del Jenkinsfile

| Etapa | Que hace | Conceptos |
|---|---|---|
| 1. Integracion con GitHub | Valida rama, descarga el codigo y muestra commit | Git, checkout, credenciales Jenkins |
| 2. Preparar cache y archivos | Define 2 repositorios Maven persistentes y realiza `stash` | Cache, stash |
| 3. Validaciones paralelas y selectivas | JUnit, Checkstyle/PMD, o README dependiendo de rama | Parallel, when, unstash, load |
| 4. Publicar resultados | Publica reportes JUnit, informes de calidad y JAR | junit, archiveArtifacts |

El bloque **`post` no es una quinta etapa**: informa por Telegram un resultado EXITOSA/FALLIDA y registra el estado del pipeline.

## Reparto entre tres expositores

### Persona 1 - Contexto, proyecto y etapas 1 y 2

- Presentar HelpDesk: registrar incidencias, prioridades, estados, estadisticas y persistencia local.
- Explicar el repositorio de GitHub y las ramas `main` (tarea 1) y `feature/pipeline-optimizacion` (tarea 2).
- Mostrar etapas **1 y 2**: checkout de Git, validacion de rama, caches Maven separadas y `stash`.
- Estrategia que explica: **cache**. Primera ejecucion descarga dependencias; otras pueden reutilizarlas si el agente conserva la cache.

**Guion sugerido:** «Reutilizamos HelpDesk de la primera tarea. Jenkins obtiene el codigo desde GitHub y prepara caches de Maven separadas para pruebas y calidad. Esto puede evitar descargas repetidas y facilita ejecutar las validaciones».

### Persona 2 - Etapa 3, pruebas y paralelizacion

- Explicar las **13 pruebas JUnit**: 11 unitarias y 2 pruebas HTTP de integracion.
- Mostrar el bloque `parallel` y las ramas `3A - Compilacion y JUnit` y `3B - Checkstyle y PMD`.
- Distinguir **JUnit** (comportamiento) de **Checkstyle/PMD** (analisis estatico de codigo).
- Estrategia que explica: **paralelismo**; la duracion real requiere medir builds en Jenkins.
- Mostrar que `load 'jenkins_ci_udla/ci/TareasHelpDesk.groovy'` ejecuta funciones externas.

**Guion sugerido:** «Antes las validaciones se realizaban de manera secuencial. Ahora Jenkins puede compilar y ejecutar JUnit mientras analiza la calidad con Checkstyle y PMD, utilizando carpetas y caches separadas».

### Persona 3 - Ejecucion selectiva, modularidad, etapa 4 y notificaciones

- Explicar `when` y la politica por ramas: `feature/*` (JUnit + calidad), `hotfix/*` (JUnit), `docs/*` (README). La opcion `main` necesita ajustes de compatibilidad para cargar el modulo nuevo sin modificar la tarea 1.
- Estrategia que explica: **ejecucion selectiva** y **modularidad** mediante el archivo `ci/TareasHelpDesk.groovy`.
- Mostrar etapa **4** para publicar reportes y JAR.
- Explicar `post { always, success, failure }` y Telegram: credenciales se guardan en Jenkins, nunca en el repositorio.

**Guion sugerido:** «No todos los cambios requieren las mismas pruebas. Por eso usamos condiciones por ramas. Para evitar duplicar comandos, cargamos funciones Groovy reutilizables. Al finalizar, Jenkins publica los resultados y Telegram recibe el estado».

## Variables y credenciales: configurar en JENKINS (no en GitHub)

El administrador debe definir las siguientes **variables de entorno en Jenkins**, globalmente o para el job:

| Variable Jenkins | Ejemplo de configuracion | ¿Es secreto? |
|---|---|---|
| `CI_REPO_URL` | `https://github.com/ElPaul593/Automatizacion_JENKINS.git` | No |
| `CI_GITHUB_CREDENTIAL_ID` | `github-private` (opcional si el repo es publico) | No: es solo el ID de una credencial |
| `CI_TELEGRAM_TOKEN_CREDENTIAL_ID` | `telegram-bot-token` | No: es solo el ID de un Secret Text |
| `CI_TELEGRAM_CHAT_CREDENTIAL_ID` | `telegram-chat-id` | No: es solo el ID de un Secret Text |

Los **valores reales del token y del chat** se almacenan en **Jenkins > Credentials** como `Secret text`. No deben ponerse como texto plano en el Jenkinsfile, en variables publicas ni en capturas.

Tambien deben existir un agente Windows con PowerShell/Git y una instalacion Maven cuyo nombre en *Global Tool Configuration* sea **Maven3**. En el job **Pipeline script from SCM**, seleccionar la rama `*/feature/pipeline-optimizacion` y `jenkins_ci_udla/Jenkinsfile`.

## Que falta para demostrarlo

- Probar Jenkins con la rama `feature/pipeline-optimizacion` y capturar las cuatro etapas visibles.
- Ejecutar el pipeline dos veces, registrar tiempos y observar la reutilizacion de las caches Maven.
- Confirmar reportes JUnit (13 tests), archivos JAR e informes Checkstyle/PMD.
- Confirmar los mensajes de Telegram en escenarios de exito y fallo controlado.
- Preparar ramas `docs/*` y `hotfix/*` desde una base compatible y demostrar que `when` omite etapas.
- Corregir la incompatibilidad de `main` con el modulo de la tarea 2 antes de seleccionarla en el nuevo pipeline.

**Aviso:** Checkstyle y PMD estan configurados para generar reportes. Todavia no actuan como puerta de calidad que haga fallar el build al detectar una infraccion.

**Estado:** el Jenkinsfile esta organizado, pero el comportamiento completo de la tarea 2 aun no ha sido validado en el servidor Jenkins.
