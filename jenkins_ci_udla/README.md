# Automatización de tareas en pipeline CI — Jenkins

## Alcance de la tarea
Demostración individual de un pipeline con **exactamente tres etapas**:

1. Integración con un repositorio **privado** de GitHub.
2. Compilación de un proyecto Java con Maven y ejecución de cuatro pruebas JUnit.
3. Notificación automática del estado (éxito o fallo) mediante Telegram.

## Estructura

```
jenkins_ci_udla/
├── Jenkinsfile
├── pom.xml
├── .gitignore
├── src/main/java/edu/udla/Calculadora.java
└── src/test/java/edu/udla/CalculadoraTest.java
```

## Requisitos previos
- Jenkins funcionando, accesible desde el navegador y con un **agente Linux o Unix** disponible.
- Git, JDK 17 o superior compatible con `--release 17`, `curl` y acceso a GitHub/Telegram desde el agente.
- Plugins de Jenkins: Pipeline, Git, Credentials, Credentials Binding, JUnit (y sus dependencias).
- Maven configurado en **Administrar Jenkins > Herramientas**, con nombre **Maven3** (habilita instalación automática si aplica).
- Repositorio PRIVADO en GitHub y token de acceso personal de alcance restringido con acceso de lectura a los contenidos de ese repositorio.
- Bot de Telegram y chat ID.

## 1. Crear repositorio privado
1. GitHub > New repository > nombre `jenkins-ci-udla` > visibilidad `Private` > Create repository.
2. Abre una terminal **dentro de la carpeta `jenkins_ci_udla`**:

```bash
git init
git branch -M main
git add .
git commit -m "feat: pipeline CI Jenkins con Maven y Telegram"
git remote add origin https://github.com/TU_USUARIO/jenkins-ci-udla.git
git push -u origin main
```

3. Edita el valor `GIT_URL` del `Jenkinsfile` con la URL REAL de tu repositorio, guarda el archivo, haz `git add Jenkinsfile && git commit -m "chore: configurar repo CI" && git push`.
4. Verifica en GitHub que el repositorio sigue indicando **Private**.

No subas tokens, contraseñas, ni chat ID dentro de los archivos del repositorio.

## 2. Crear el token de GitHub
- En GitHub, abre Settings > Developer settings > Personal access tokens > Fine-grained tokens.
- Autoriza SOLO tu repositorio privado `jenkins-ci-udla`.
- En Repository permissions asigna a `Contents` el permiso **Read-only**.
- Guarda el token en un lugar seguro. No lo pegues en el código ni en el informe.
- En Jenkins: Administrar Jenkins > Credenciales > Global > Añadir credenciales.
  - Kind: `Username with password`.
  - Username: tu usuario de GitHub.
  - Password: tu personal access token.
  - ID: `github-private`.

## 3. Configurar Telegram
1. En Telegram abre el usuario verificado `@BotFather`, envía `/newbot` y sigue los pasos.
2. Abre el chat del nuevo bot y envíale `/start` o `hola`.
3. En tu TERMINAL, ejecuta el siguiente comando sin compartir el token:

```bash
read -rsp 'Token de Telegram: ' TOKEN; echo
curl -fsS "https://api.telegram.org/bot${TOKEN}/getUpdates"
unset TOKEN
```

4. Identifica `message.chat.id` en el JSON: ese es tu `chat_id` personal. Si sale `result: []`, vuelve a escribirle al bot y repite.
5. Jenkins > Administrar Jenkins > Credenciales > Global > Añadir credenciales:
   - Tipo `Secret text`, ID `telegram-bot-token`, Secret: token del bot.
   - Tipo `Secret text`, ID `telegram-chat-id`, Secret: tu chat ID.
6. Nunca compartas esos valores en la captura ni los subas a GitHub.

## 4. Preparar Maven en Jenkins
- En **Administrar Jenkins > Herramientas**, agrega una instalación Maven cuyo nombre sea exactamente `Maven3`.
- Activa la instalación automática o indica la ruta válida a Maven en el agente.
- Asegúrate de tener instalado un JDK 17 o superior y un ejecutor Linux/Unix.
- Comprueba que `curl` está instalado en el agente.

## 5. Crear job de Pipeline
1. Panel de Jenkins > **Nueva tarea**.
2. Nombre: `CI-Jenkins-UDLA` > tipo **Pipeline**.
3. Desplázate a **Pipeline** > Definition: **Pipeline script from SCM**.
4. SCM: **Git**.
5. Repository URL: `https://github.com/TU_USUARIO/jenkins-ci-udla.git` (URL real).
6. Credentials: selecciona `github-private`.
7. Branch Specifier: `*/main`.
8. Script Path: `Jenkinsfile`.
9. Guardar > **Build Now**.

### Activación automática (mejora opcional)
En la configuración del job, sección **Build Triggers**, activa **Poll SCM** y usa `H/5 * * * *` para que Jenkins compruebe aproximadamente cada cinco minutos si hubo cambios en Git. Esto resulta práctico si Jenkins está en `localhost`, donde un webhook de GitHub normalmente no puede alcanzarlo directamente. No es necesario para completar las tres etapas de la tarea.

**Nota**: Jenkins hace una lectura inicial del `Jenkinsfile` antes de entrar a las etapas. Luego, la etapa 1 hace el checkout explícito que se muestra en el pipeline.

## 6. Ejecutar caso de éxito
- Abre `Build Now` y entra a `Console Output`.
- Debe mostrarse `BUILD SUCCESS` (si no hay problemas de configuración).
- En la etapa 2 deben figurar 4 pruebas ejecutadas sin fallos.
- Telegram debe recibir un mensaje con `EXITOSA`.
- Captura la vista de las tres etapas, el resultado de JUnit y el mensaje de Telegram.

## 7. Ejecutar caso de fallo deliberado (para demostrar la notificación)
- Cambia temporalmente en `src/test/java/edu/udla/CalculadoraTest.java`:

```java
assertEquals(5, Calculadora.sumar(2, 3));
```

por:

```java
assertEquals(999, Calculadora.sumar(2, 3));
```

- Guarda, haz `git add . && git commit -m "test: provocar fallo controlado" && git push`.
- Lanza otro build en Jenkins.
- El estado esperado es `FAILURE`, mientras que la etapa 3 todavía debe ejecutarse y Telegram debe avisar `FALLIDA`.
- Captura el fallo en JUnit y la notificación.
- Restablece la prueba original, haz commit y push, y ejecuta nuevamente hasta terminar en verde.

## 8. Evidencias recomendadas
1. Página de GitHub con indicador **Private**, nombre y archivos del repositorio (sin credenciales).
2. Configuración del Job **Pipeline script from SCM** con URL, branch y `Jenkinsfile`.
3. Vista de las tres etapas del pipeline completado.
4. Logs de compilación y pruebas (`Tests run: 4, Failures: 0`).
5. Mensaje Telegram `EXITOSA`.
6. Capturas del segundo build fallido (`FAILURE`) y mensaje Telegram `FALLIDA`.
7. Opcional: gráfico/reportes JUnit y artefacto `.jar`.

## Problemas habituales
- `No such DSL method 'withCredentials'`: instalar/actualizar Credentials Binding Plugin.
- `Tool type ... maven ... does not have an install of 'Maven3'`: configurar Maven con ese nombre en Jenkins.
- `Authentication failed`: revisar token GitHub, alcance `Contents: Read-only`, usuario y credencial `github-private`.
- `curl: not found`: instalar curl en el agente.
- `403` o `400` al llamar a Telegram: revisar bot token, chat ID y que hayas enviado un mensaje inicial al bot.
- `No tests found`: revisar que los archivos estén en las carpetas Java indicadas y que Maven descargue JUnit.

## Referencias
- https://www.jenkins.io/doc/pipeline/steps/workflow-basic-steps/
- https://www.jenkins.io/doc/pipeline/steps/git/
- https://www.jenkins.io/doc/book/pipeline/jenkinsfile/
- https://www.jenkins.io/doc/pipeline/steps/junit/
- https://core.telegram.org/bots/api
