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

### Roberto - Contexto, aplicacion y etapas 1 y 2

- Presentar HelpDesk: registrar incidencias, prioridades, estados, estadisticas y persistencia local.
- Explicar el repositorio de GitHub y las ramas `main` (tarea 1) y `feature/pipeline-optimizacion` (tarea 2).
- Mostrar etapas **1 y 2**: checkout de Git, validacion de rama, caches Maven separadas y `stash`.
- Estrategia que explica: **cache**. Primera ejecucion descarga dependencias; otras pueden reutilizarlas si el agente conserva la cache.

**Guion sugerido:** «Reutilizamos HelpDesk de la primera tarea. Jenkins obtiene el codigo desde GitHub y prepara caches de Maven separadas para pruebas y calidad. Esto puede evitar descargas repetidas y facilita ejecutar las validaciones».

### Escobar - Etapa 3, pruebas y paralelizacion

- Explicar las **13 pruebas JUnit**: 11 unitarias y 2 pruebas HTTP de integracion.
- Mostrar el bloque `parallel` y las ramas `3A - Compilacion y JUnit` y `3B - Checkstyle y PMD`.
- Distinguir **JUnit** (comportamiento) de **Checkstyle/PMD** (analisis estatico de codigo).
- Estrategia que explica: **paralelismo**; la duracion real requiere medir builds en Jenkins.
- Mostrar que `load 'jenkins_ci_udla/ci/TareasHelpDesk.groovy'` ejecuta funciones externas.

**Guion sugerido:** «Antes las validaciones se realizaban de manera secuencial. Ahora Jenkins puede compilar y ejecutar JUnit mientras analiza la calidad con Checkstyle y PMD, utilizando carpetas y caches separadas».

### Paul - Seleccion, modularidad, etapa 4 y Telegram

- Explicar `when` y la politica por ramas: `main` (JUnit de la tarea 1), `feature/*` (JUnit + calidad), `hotfix/*` (JUnit), `docs/*` (README). `main` ahora tiene compatibilidad mediante Maven directo sin necesidad de agregar el modulo de la tarea 2 a esa rama.
- Estrategia que explica: **ejecucion selectiva** y **modularidad** mediante el archivo `ci/TareasHelpDesk.groovy`.
- Mostrar etapa **4** para publicar reportes y JAR.
- Explicar `post { always, success, failure }` y Telegram: credenciales se guardan en Jenkins, nunca en el repositorio.

**Guion sugerido:** «No todos los cambios requieren las mismas pruebas. Por eso usamos condiciones por ramas. Para evitar duplicar comandos, cargamos funciones Groovy reutilizables. Al finalizar, Jenkins publica los resultados y Telegram recibe el estado».

## Conexion GitHub y Telegram: reutilizar las credenciales ya existentes

El Jenkinsfile de la tarea 2 utiliza el mismo repositorio y los **mismos IDs de credenciales que la tarea 1**, sin variables nuevas obligatorias. El job debe apuntar a `*/feature/pipeline-optimizacion` con script path `jenkins_ci_udla/Jenkinsfile`.

| Concepto | Configuracion existente |
|---|---|
| Repositorio (URL publica, no secreta) | `https://github.com/ElPaul593/Automatizacion_JENKINS.git` |
| ID credencial GitHub | `github-private` |
| ID token bot Telegram | `telegram-bot-token` |
| ID chat Telegram | `telegram-chat-id` |

Los **valores reales** del token, del chat y de GitHub permanecen en **Jenkins > Credentials**. Los identificadores NO son claves reales, pueden aparecer en el Jenkinsfile y ya se utilizan en `main`. No configurar `CI_REPO_URL`, `CI_GITHUB_CREDENTIAL_ID`, `CI_TELEGRAM_TOKEN_CREDENTIAL_ID` ni `CI_TELEGRAM_CHAT_CREDENTIAL_ID`: esas variables se eliminaron del Jenkinsfile nuevo tras diagnosticar un build fallido por falta de configuracion.

Tambien deben existir un agente Windows con PowerShell/Git y una instalacion Maven cuyo nombre en *Global Tool Configuration* sea **Maven3**.

## Que falta para demostrarlo

- Probar Jenkins con la rama `feature/pipeline-optimizacion` y capturar las cuatro etapas visibles.
- Ejecutar el pipeline dos veces, registrar tiempos y observar la reutilizacion de las caches Maven.
- Confirmar reportes JUnit (13 tests), archivos JAR e informes Checkstyle/PMD.
- Confirmar los mensajes de Telegram en escenarios de exito y fallo controlado.
- Preparar ramas `docs/*` y `hotfix/*` desde una base compatible y demostrar que `when` omite etapas.
- La compatibilidad de `main` con el Jenkinsfile de la tarea 2 ya esta implementada. En `main` solo se ejecuta JUnit (no tiene el perfil de calidad de la tarea 2).

**Aviso:** No se utiliza SonarQube. Checkstyle y PMD estan configurados para generar reportes. Todavia no actuan como puerta de calidad que haga fallar el build al detectar una infraccion.

**Estado:** el Jenkinsfile esta organizado, pero el comportamiento completo de la tarea 2 aun no ha sido validado en el servidor Jenkins.

## Ramas existentes y terminologia

Al revisar GitHub, solo existen `main` y `feature/pipeline-optimizacion`. No hay rama `deploy` y tampoco una etapa de despliegue. No afirmar que se despliega a produccion. Si se usa la palabra "deploy" en clase, aclarar que en este proyecto solo se genera y publica el JAR, sin desplegarlo.

## Como medir tiempos reales para la exposicion

El Jenkinsfile ya incluye `timestamps()`. **No afirmar un ahorro porcentual sin datos.**

1. El administrador instala o confirma el plugin **Pipeline Graph View**, abre el build y selecciona **Pipeline Overview** para observar las cuatro etapas y las ramas paralelas.
2. Si dispone de **Pipeline Stage View**, consultar la duracion por etapa y sus promedios historicos; esta vista puede no representar visualmente las ramas paralelas.
3. Ejecutar dos builds exitosos de `feature/pipeline-optimizacion` sobre el mismo agente Windows para comparar una cache fria y otra caliente. Registrar el tiempo **total** y de las etapas **1, 2, 3 y 4**.
4. Dentro de la etapa 3 registrar por separado **3A (JUnit)** y **3B (Checkstyle/PMD)**. Si ambos se solapan, hay paralelismo.
5. No comparar los tiempos de la tarea 1 (sin analisis de calidad) y la tarea 2 como si hicieran el mismo trabajo.

| Medicion (segundos) | Build 1 | Build 2 |
|---|---:|---:|
| Etapa 1 - GitHub | Por medir | Por medir |
| Etapa 2 - Cache y archivos | Por medir | Por medir |
| Etapa 3A - JUnit | Por medir | Por medir |
| Etapa 3B - Checkstyle/PMD | Por medir | Por medir |
| Etapa 3 - Total del paralelo | Por medir | Por medir |
| Etapa 4 - Resultados | Por medir | Por medir |
| Duracion total | Por medir | Por medir |

## Origen de las librerias y plugins

- **JUnit Jupiter** (`org.junit.jupiter:junit-jupiter:5.11.4`): dependencia de pruebas declarada en `pom.xml`; Maven la obtiene del repositorio de dependencias configurado (normalmente Maven Central).
- **Maven Compiler**: compila Java 17.
- **Maven Surefire**: descubre y ejecuta JUnit.
- **Maven JAR**: empaqueta `gestion-incidencias-1.0.0.jar`.
- **Maven Checkstyle y Maven PMD**: plugins Maven del perfil `calidad`, descargados desde repositorios Maven; producen analisis estatico.
- **Jenkins**: pasos como `git`, `load`, `junit`, `stash/unstash`, `withCredentials` y `powershell` provienen del motor Pipeline y los plugins instalados en Jenkins; no son librerias Java.
- **SonarQube**: NO se utiliza; no hay integracion de Sonar ni dashboard Sonar que demostrar.

## Como reutilizar las funciones desde otro Jenkinsfile

Primero ese otro job debe **descargar el repositorio que contiene el archivo Groovy**. Despues, desde un directorio que contenga `jenkins_ci_udla/`, puede invocarse:

```groovy
script {
    def tareas = load 'jenkins_ci_udla/ci/TareasHelpDesk.groovy'
    tareas.compilarYProbar(env.MAVEN_CACHE_JUNIT)
}
```

El `load` permite reutilizar codigo de otro archivo del workspace, no crea automaticamente una biblioteca global de Jenkins. Para usarlo de forma transversal entre repositorios sin hacer checkout del modulo es necesario configurar una **Jenkins Shared Library**.

## Exigencias aun pendientes

La existencia del codigo no demuestra que funcione en el servidor. Faltan la validacion real de las cuatro etapas, las notificaciones, las mediciones de tiempo, las ramas `docs/*`/`hotfix/*` de demostracion y las capturas para el informe. Comprobar que Jenkins mantenga las credenciales ya utilizadas por la tarea 1 y que el job de la tarea 2 lea la rama correcta.

