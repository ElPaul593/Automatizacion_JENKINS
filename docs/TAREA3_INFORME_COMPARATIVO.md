# INFORME COMPARATIVO - TAREA 3: JENKINS VS CIRCLECI

**Universidad:** Universidad de Las Americas (UDLA)  
**Asignatura:** Procesos de Software  
**Tema:** Comparacion de casos de uso CI/CD con CircleCI  
**Proyecto:** HelpDesk - Sistema de gestion de incidencias  
**Integrantes:** Roberto Guana, Pablo Escobar y Paul Larrea  
**Fecha:** [COLOCAR FECHA DE ENTREGA]

> **Estado del documento:** plantilla de informe lista para recibir las evidencias reales de CircleCI. Los campos entre corchetes NO son mediciones realizadas. No reemplazar por valores inventados.

## 1. Introduccion

En las tareas anteriores se desarrollo HelpDesk UDLA en Java 17, con Maven, 13 pruebas automatizadas JUnit y un pipeline de Jenkins. En la segunda tarea se agregaron Checkstyle, PMD, ejecucion paralela, cache Maven, ejecucion selectiva y un modulo Groovy reutilizable. La tercera tarea consiste en llevar esas practicas a **CircleCI**, observar su ejecucion en una plataforma distinta y comparar resultados sin reemplazar los pipelines originales.

## 2. Objetivo

Configurar el proyecto HelpDesk en CircleCI con diferentes jobs y workflows, conservar las pruebas y analisis de calidad, incorporar notificaciones y comparar frente a Jenkins las estrategias de cache, paralelismo, selectividad, modularidad y tiempo de ejecucion.

## 3. Metodologia

Se creo la rama `feature/circleci-tarea3` como copia inicial de `feature/pipeline-optimizacion`. Las ramas `main` (tarea 1) y `feature/pipeline-optimizacion` (tarea 2) permanecen sin cambios.

**Versiones y entorno de la comparacion:**

| Item | Jenkins | CircleCI |
|---|---|---|
| Job / workflow | `CI-HelpDesk-Optimizacion` | `validacion_completa` |
| Codigo | GitHub, rama `feature/pipeline-optimizacion` | GitHub, rama `feature/circleci-tarea3` |
| Plataforma | Agente Windows administrado por el equipo | Contenedor Linux `cimg/openjdk:17.0` |
| Compilacion y JAR | Maven `clean verify` | Maven `clean verify` |
| Test | JUnit 5, 13 pruebas | JUnit 5, 13 pruebas |
| Calidad | Maven Checkstyle + PMD | Los mismos plugins Maven |
| Notificaciones | Jenkins Credentials + Telegram | Variables secretas CircleCI + Telegram |
| Despliegue productivo | No | No |

## 4. Implementacion del pipeline en CircleCI

### 4.1. Configuracion general

El archivo `.circleci/config.yml` contiene los jobs `pruebas_junit`, `calidad_codigo` y `notificar_exito`. Emplea dos workflows: `validacion_completa` y `validacion_rapida`. Se integra a GitHub y utiliza un contenedor con Java 17.

**[CAPTURA 1: CircleCI - pagina del proyecto, rama de la tarea 3 y workflow completo.]**

### 4.2. Pruebas automatizadas

`pruebas_junit` ejecuta Maven con `clean verify`, guarda XML de Surefire en **Tests** y publica el archivo `gestion-incidencias-1.0.0.jar` como artefacto.

**Resultado real CircleCI:** [INDICAR NUMERO DE TESTS], [FALLOS], [ERRORES], [ESTADO].

**[CAPTURA 2: job pruebas_junit - 13 pruebas, 0 fallos y estado SUCCESS.]**

**[CAPTURA 3: job pruebas_junit - pestana Artifacts con el JAR y XML JUnit.]**

### 4.3. Calidad de codigo

`calidad_codigo` ejecuta el perfil `-Pcalidad -DskipTests`, con Checkstyle y PMD. Los informes HTML y XML se publican como artefactos. El perfil actual **genera informes**, pero no convierte cualquier infraccion en un fallo automatico del job: hay que leer las metricas mostradas.

**Hallazgos Checkstyle:** [NUMERO REAL].  
**Hallazgos PMD:** [NUMERO REAL].

**[CAPTURA 4: job calidad_codigo - pasos de Maven y Artifacts checkstyle.html y pmd.html.]**

### 4.4. Notificaciones

El archivo `.circleci/scripts/telegram.sh` utiliza `TELEGRAM_BOT_TOKEN` y `TELEGRAM_CHAT_ID` configurados en **Project Settings > Environment Variables**. En la configuracion no aparecen valores secretos.

Si falla el job de pruebas o el de calidad, se intenta enviar **FALLIDA** desde el propio job. Si se superan todos los jobs requeridos, `notificar_exito` envia **EXITOSA**.

**[CAPTURA 5: Telegram - mensaje EXITOSA del workflow completo; NO mostrar tokens.]**

**[CAPTURA 6: Telegram - mensaje FALLIDA de una prueba controlada, si se realizo.]**

## 5. Comparacion de las cuatro estrategias de optimizacion

| Estrategia | Implementacion Jenkins | Implementacion CircleCI | Evidencia / observaciones |
|---|---|---|---|
| Cache | Directorios locales persistentes de Maven, separados para pruebas y calidad | `restore_cache/save_cache` de `~/.m2/repository` con checksum del `pom.xml` | [CAPTURA 7: cache miss y posterior cache hit] |
| Paralelismo | `parallel` con 3A JUnit y 3B Checkstyle/PMD | Dos jobs independientes dentro de `validacion_completa` | [CAPTURA 8: jobs superpuestos en el workflow] |
| Selectividad | `when` segun rama `main` o `feature/pipeline-optimizacion` | Dos workflows alternativos controlados por `analisis_completo=true/false` | [CAPTURA 9: workflow rapido sin job calidad_codigo] |
| Modularidad | `TareasHelpDesk.groovy` y `load` | `commands` reutilizables y `telegram.sh` | [CAPTURA 10: fragmentos de ambas configuraciones] |

**Discusion de la selectividad:** las dos plataformas cumplen el objetivo de evitar trabajos no necesarios, pero con mecanismos diferentes: **Jenkins decide por rama**, mientras que **CircleCI decide mediante un parametro booleano**. No presentar ambos mecanismos como identicos.

## 6. Tiempo de ejecucion

Usar builds correctos, preferiblemente el **segundo build con cache** en ambas plataformas. Registrar duraciones reales desde las interfaces.

| Metrica (segundos) | Jenkins | CircleCI | Diferencia y observacion |
|---|---:|---:|---|
| Integracion / preparacion | [ ] | [ ] | [ ] |
| JUnit, compilacion y JAR | [ ] | [ ] | [ ] |
| Checkstyle y PMD | [ ] | [ ] | [ ] |
| Tiempo total del bloque paralelo | [ ] | [ ] | [ ] |
| Publicacion / notificacion | [ ] | [ ] | [ ] |
| **Tiempo total de ejecucion** | **[ ]** | **[ ]** | **[ ]** |

**Formula de diferencia relativa respecto de Jenkins:**

`Diferencia (%) = (T_Jenkins - T_CircleCI) / T_Jenkins x 100`

Si el resultado es positivo, CircleCI fue mas rapido en esas condiciones; si es negativo, fue mas lento. **NO** atribuir toda la diferencia exclusivamente al paralelismo o a la cache: Jenkins corre sobre Windows y CircleCI en un contenedor Linux, con recursos, red, versiones y sobrecarga distintos.

**[CAPTURA 11: ejecucion Jenkins SUCCESS con duracion total y las dos ramas paralelas.]**

**[CAPTURA 12: ejecucion CircleCI SUCCESS con duracion total y duracion por job.]**

### Comparacion de cache fria y caliente en CircleCI

| Dato | Primera ejecucion | Segunda ejecucion |
|---|---|---|
| Cache Maven JUnit | [MISS/HIT] | [MISS/HIT] |
| Cache Maven calidad | [MISS/HIT] | [MISS/HIT] |
| Tiempo `pruebas_junit` | [ ] s | [ ] s |
| Tiempo `calidad_codigo` | [ ] s | [ ] s |
| Duracion workflow completo | [ ] s | [ ] s |

**Interpretacion:** [ESCRIBIR A PARTIR DE RESULTADOS REALES]. La cache acelera o evita descargas de dependencias; `clean verify` mantiene la recompilacion.

## 7. Cumplimiento de la consigna

| Requisito | Estado del codigo | Evidencia operativa |
|---|---|---|
| Proyecto funcional no calculadora | Listo: HelpDesk UDLA | [Pendiente / evidencia] |
| Multiples jobs y workflows en CircleCI | Configurados | [Pendiente / evidencia] |
| Trasladar pipeline Jenkins -> CircleCI | Configurado | [Pendiente / evidencia] |
| Notificaciones de diferentes estados | Configuradas con Telegram | [Pendiente / evidencia] |
| Cache | Configurada | [Pendiente / evidencia] |
| Paralelismo | Configurado | [Pendiente / evidencia] |
| Ejecucion selectiva | Configurada | [Pendiente / evidencia] |
| Modularidad | Configurada | [Pendiente / evidencia] |
| Comparar tiempos | Tabla y metodo preparados | [Pendiente: cifras reales] |
| Presentacion de estructura y ejecucion | Configuracion y guion disponibles | [Pendiente: capturas reales] |

## 8. Observaciones y limitaciones

- El analisis de calidad no usa SonarQube; utiliza **Checkstyle y PMD**.
- La automatizacion es **CI**, no despliegue real a produccion.
- La primera ejecucion puede tardar mas por descargas y configuracion inicial.
- Los tiempos de plataformas distintas no se comparan en condiciones identicas.
- Los secretos de CircleCI deben configurarse de forma independiente a Jenkins.
- Una configuracion YAML en GitHub NO demuestra que CircleCI la haya ejecutado; adjuntar siempre registros reales.

## 9. Conclusiones

[REDACTAR DESPUES DE VER LOS TIEMPOS Y LA EVIDENCIA.]

Sugerencia para el cierre, completar con los resultados:

«Se traslado el proceso de CI de HelpDesk a CircleCI, conservando la compilacion, las 13 pruebas JUnit y el analisis de calidad. Se implementaron cache, trabajos paralelos, ejecucion selectiva y comandos reutilizables. La comparacion de Jenkins y CircleCI muestra [RESULTADO REAL], con las limitaciones propias de ejecutar en entornos diferentes».

## 10. Referencias

- Repositorio del proyecto: https://github.com/ElPaul593/Automatizacion_JENKINS
- Configuracion CircleCI: https://circleci.com/docs/reference/configuration-reference/
- Cache de CircleCI: https://circleci.com/docs/guides/optimize/caching/
- Conexion de proyectos: https://circleci.com/docs/guides/getting-started/create-project/
- Pruebas y artefactos: https://circleci.com/docs/guides/optimize/persist-data/
