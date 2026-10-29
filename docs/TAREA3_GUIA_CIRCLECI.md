# Tarea 3 - Guia paso a paso para ejecutar HelpDesk con CircleCI

**Consigna:** Comparacion de casos de uso CI/CD (CircleCI), 25 % P1.  
**Fecha de entrega indicada por el docente:** 30/10/2026, 18:50 (Ecuador).  
**Proyecto:** HelpDesk UDLA, Java 17, Maven y 13 pruebas JUnit.

## 1. Que se hizo en GitHub

Se creo la rama `feature/circleci-tarea3` **desde `feature/pipeline-optimizacion`**. No se editaron `main` ni `feature/pipeline-optimizacion`.

Los archivos NUEVOS de la tarea 3 son:

| Archivo | Para que sirve |
|---|---|
| `.circleci/config.yml` | Jobs, workflows, paralelismo, caches, reportes y ejecucion selectiva |
| `.circleci/scripts/telegram.sh` | Envio de avisos EXITOSA / FALLIDA a Telegram sin tokens en codigo |
| `docs/TAREA3_GUIA_CIRCLECI.md` | Esta guia para conectar y demostrar CircleCI |
| `docs/TAREA3_INFORME_COMPARATIVO.md` | Informe comparativo con espacios para datos y capturas reales |

**Nota:** El `Jenkinsfile` de la tarea 2 queda copiado en esta nueva rama como antecedente. CircleCI **no lo ejecuta**: usa `.circleci/config.yml`. El `pom.xml` y la aplicacion HelpDesk siguen siendo los mismos.

## 2. Que ejecuta CircleCI

La configuracion `version: 2.1` utiliza el contenedor `cimg/openjdk:17.0`, compatible con el proyecto Java 17.

**Jobs (trabajos):**

1. **`pruebas_junit`:** descarga codigo, recupera cache de Maven, compila, ejecuta las 13 pruebas JUnit, guarda dependencias y publica reportes y JAR.
2. **`calidad_codigo`:** descarga codigo, recupera otra cache, ejecuta Checkstyle y PMD mediante `-Pcalidad -DskipTests`, publica los informes.
3. **`notificar_exito`:** espera a que terminen correctamente los jobs obligatorios y envia una notificacion EXITOSA a Telegram.

Los jobs 1 y 2 **corren en paralelo** porque ninguno depende del otro. Si uno falla, se envia una notificacion **FALLIDA desde el propio job**. Si ambos terminan bien, se activa `notificar_exito`.

**Dos workflows (flujos):**

- **`validacion_completa`** (por defecto): JUnit + Checkstyle/PMD en paralelo + Telegram.
- **`validacion_rapida`** (cuando `analisis_completo=false`): solo JUnit + Telegram.

Esta **ejecucion selectiva por parametro** conserva el objetivo de evitar trabajo innecesario, aunque **no es exactamente la seleccion por rama que configuramos en Jenkins**. En el informe compara esa diferencia con claridad.

**Cuatro estrategias de la consigna:**

- Cache: `restore_cache` y `save_cache`, guardando `~/.m2/repository` por tipo y suma de comprobacion del `pom.xml`.
- Paralelismo: jobs `pruebas_junit` y `calidad_codigo` independientes en el workflow completo.
- Ejecucion selectiva: parametro `analisis_completo` que elige entre dos workflows.
- Modularidad: `commands` reutilizables (`restaurar_cache_maven`, `guardar_cache_maven`, `notificar_fallo`) y script `telegram.sh`.

## 3. Conectar tu cuenta de CircleCI (lo haces tu)

1. Abre **https://app.circleci.com/** y crea una cuenta o inicia sesion con GitHub.
2. En CircleCI, busca **Projects** o **Set up a project**, segun la interfaz.
3. Conecta el repositorio **`ElPaul593/Automatizacion_JENKINS`**.
4. Si aparece **Connect GitHub / GitHub App**, acepta la autorizacion necesaria. **Importante:** tu usuario de GitHub tiene permiso para subir codigo, pero no permiso de **admin** del repositorio. Si GitHub exige autorizacion del propietario para instalar CircleCI, pide a Paul que autorice **solo este repositorio**.
5. Elige usar un archivo **ya existente**, sin generar una configuracion nueva en `main`.
6. **Ruta de configuracion:** `.circleci/config.yml`.
7. **Rama del archivo y del codigo:** `feature/circleci-tarea3`. En interfaces con fuentes separadas, selecciona esa rama tanto en *config source* como en *checkout source*.
8. Si puedes restringir los disparadores automaticos, limita la ejecucion a `feature/circleci-tarea3`, para no ejecutar accidentalmente configuraciones inexistentes de las tareas anteriores.
9. Guarda y abre **Pipelines**. Si el proyecto no se ejecuta automaticamente, usa **Trigger Pipeline** y selecciona la rama `feature/circleci-tarea3`.

**No cambies la rama por defecto `main` ni mezcles la tarea 3 con las anteriores.**

## 4. Configurar Telegram SIN compartir ni subir secretos

Las credenciales guardadas en Jenkins **NO se transfieren automaticamente a CircleCI**.

En **CircleCI > Projects > HelpDesk > Project Settings > Environment Variables**, agrega:

| Nombre exacto | Contenido | Secreto |
|---|---|---|
| `TELEGRAM_BOT_TOKEN` | Token real de tu bot creado en Telegram | Si: nunca subirlo a GitHub ni pegarlo en chats |
| `TELEGRAM_CHAT_ID` | Identificador del chat o grupo receptor | Mantenerlo privado |

Si tienes autorizacion para usar el bot del equipo, puedes reutilizar sus valores. Si no, crea un bot propio con **@BotFather**, envia un mensaje al bot e identifica tu chat. Evita exponer el token en capturas.

El script nuevo `.circleci/scripts/telegram.sh` usa esas dos variables al ejecutarse. **No hay tokens reales ni API keys en los archivos.**

Si faltan estas variables, CircleCI no podra enviar Telegram: la notificacion exitosa devolvera un error claro. Configuralas **antes de hacer la prueba final**.

## 5. Primera ejecucion: workflow COMPLETO

1. Abre CircleCI > **Pipelines** > selecciona el proyecto HelpDesk.
2. Selecciona la rama `feature/circleci-tarea3`.
3. Pulsa **Trigger Pipeline** si necesitas iniciar manualmente.
4. El parametro `analisis_completo` debe estar en **true** (valor por defecto).
5. Debe aparecer el workflow **`validacion_completa`**.
6. Los jobs `pruebas_junit` y `calidad_codigo` deben comenzar sin esperar el uno al otro.
7. En `pruebas_junit` comprueba: **13 pruebas**, **0 fallos**, **0 errores**.
8. Abre la pestana **Tests** o **Test Results**, si la muestra tu interfaz.
9. Abre **Artifacts** en `pruebas_junit`; debe estar `gestion-incidencias-1.0.0.jar`.
10. En `calidad_codigo` confirma los informes **checkstyle.html** y **pmd.html** y revisa el resultado de las reglas configuradas.
11. Cuando terminen los dos jobs, `notificar_exito` debe enviar **EXITOSA** a Telegram.

**Importante:** Los objetivos Maven de Checkstyle y PMD en este proyecto generan informes; no son una puerta de calidad estricta. El hecho de que el job termine verde **no demuestra que todas las reglas tengan cero infracciones**: lee el HTML.

## 6. Segunda ejecucion: demostrar la CACHE

1. Sin cambiar `pom.xml`, lanza de nuevo `validacion_completa` en la misma rama.
2. Abre `pruebas_junit` y `calidad_codigo` > paso **Restore Cache** (o `restore_cache`).
3. Primera ejecucion: normalmente se observa **cache miss** y posteriormente **Save Cache**.
4. Segunda ejecucion: debe observarse **cache hit / restored** para cada job.
5. Anota duraciones del workflow y de cada job en el informe.

La cache se guarda con claves de Maven que incluyen la suma de comprobacion del `pom.xml`. Cambiar ese archivo puede provocar una nueva cache. La cache acelera la recuperacion de dependencias, **no elimina la recompilacion Java** que hace `clean verify`.

## 7. Tercera ejecucion: demostrar SELECTIVIDAD

1. Abre **Trigger Pipeline** para `feature/circleci-tarea3`.
2. En los parametros del pipeline agrega **`analisis_completo`**, tipo **boolean**, valor **false**.
3. Ejecuta. Debe iniciarse `validacion_rapida`.
4. Comprueba que se ejecuta **`pruebas_junit`** pero **no** `calidad_codigo`.
5. Al terminar correctamente, debe llegar Telegram **EXITOSA**.
6. Guarda una captura de la diferencia entre los workflows completo y rapido.

## 8. Demostrar fallo controlado y aviso FALLIDA

Se recomienda **no romper la aplicacion ni editar las ramas anteriores**. Puedes realizar una demostracion de fallo exclusivamente desde una ejecucion controlada en CircleCI, por ejemplo al introducir temporalmente un error de prueba en una rama de experimentacion basada en la tarea 3 y revertirlo al terminar. Este paso es opcional si la rubrica acepta la explicacion de `when: on_fail` y la configuracion de la notificacion.

Si se hace, conservar el log del job fallido y el mensaje FALLIDA recibido por Telegram. Nunca publicar tokens en las capturas.

## 9. Comparacion Jenkins vs CircleCI

En Jenkins tienes evidencias anteriores: job `CI-HelpDesk-Optimizacion`, 4 etapas, 13 JUnit, Checkstyle/PMD, caches locales del agente Windows, funciones Groovy reutilizables y Telegram.

En CircleCI los equivalentes son: 3 jobs en contenedores, 2 workflows seleccionables, `restore_cache/save_cache`, comandos YAML reutilizables, pruebas e informes en las pestanas Tests/Artifacts y Telegram.

Para comparar el tiempo con justicia:

1. Toma un build **SUCCESS** de Jenkins en `feature/pipeline-optimizacion`, su duracion real total y tiempos 3A/3B.
2. Toma un workflow completo **SUCCESS** de CircleCI en `feature/circleci-tarea3` y sus tiempos de `pruebas_junit` y `calidad_codigo`.
3. Usa si es posible ejecuciones de **cache caliente** (segunda ejecucion) y el mismo `pom.xml` y pruebas.
4. Aclara las diferencias entre el Windows donde corre Jenkins y el contenedor Linux de CircleCI; los tiempos NO son perfectamente equivalentes.
5. Formula para comparar tiempos: `(T_Jenkins - T_CircleCI) / T_Jenkins * 100` (si es negativo, CircleCI tardo mas).
6. Adjunta capturas y **NO inventes mediciones**.

Usa el archivo `docs/TAREA3_INFORME_COMPARATIVO.md` para completar el informe con datos reales.

## 10. Problemas frecuentes

| Sintoma | Que revisar |
|---|---|
| CircleCI no encuentra el proyecto GitHub | Permisos de acceso; propietario autoriza la GitHub App |
| No encuentra `.circleci/config.yml` | Elegir rama `feature/circleci-tarea3`, no `main` |
| `mvn: command not found` | Comprobar la imagen `cimg/openjdk:17.0` y el job |
| Java no compila | Revisar log del job y version Java 17 |
| No llegan avisos | Variables `TELEGRAM_BOT_TOKEN` y `TELEGRAM_CHAT_ID` y acceso del bot al chat |
| El workflow completo no corre | Verificar `analisis_completo=true` |
| Calidad no corre en modo rapido | Correcto; es el comportamiento esperado |
| Primera cache no existe | Correcto; debe crearse al completar el job y usarse despues |
| Otra rama Jenkins sigue intacta | Correcto; no mezclar configuraciones |

## 11. Que falta para dar la tarea 3 por terminada

El codigo ya queda preparado en GitHub. Solo podras considerar finalizada la practica cuando **CircleCI haya ejecutado de verdad los workflows**, hayan llegado las notificaciones y hayas completado el informe con **tiempos y capturas reales**.

No se ha desplegado HelpDesk a produccion. El objetivo de esta practica es **trasladar y comparar los pipelines de integracion continua**.

**Fuentes de configuracion:** documentacion oficial de CircleCI: https://circleci.com/docs/reference/configuration-reference/ , https://circleci.com/docs/guides/getting-started/create-project/ y https://circleci.com/docs/guides/optimize/caching/ .
