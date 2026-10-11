# Informe de práctica: Automatización de tareas en pipeline CI — Jenkins

**Asignatura:** [Completar]  
**Estudiante:** [Completar]  
**Fecha:** [Completar]  
**Repositorio:** [Pegar enlace privado visible al docente, si se le concede acceso]

## Objetivo
Implementar un pipeline de integración continua en Jenkins capaz de acceder de forma segura a un repositorio privado Git, compilar y comprobar automáticamente un proyecto Java y notificar mediante Telegram el resultado final de cada ejecución.

## Herramientas
Jenkins, GitHub (repositorio privado), Git, Java 17, Apache Maven, JUnit 5 y Telegram Bot API.

## Desarrollo

### Etapa 1: integración con repositorio privado
El job se configuró mediante `Pipeline script from SCM` y utiliza credenciales almacenadas en Jenkins para obtener el código de un repositorio privado de GitHub. El Jenkinsfile realiza un checkout explícito en esta etapa y registra el identificador corto del commit.

**Evidencia 1:** [Insertar captura que muestre Private y los archivos].
**Evidencia 2:** [Insertar captura de configuración de SCM, ocultando secretos].

### Etapa 2: compilación y pruebas automatizadas
El comando `mvn -B -ntp clean verify` compila el código Java, ejecuta cuatro pruebas automatizadas con JUnit 5, produce un paquete `.jar` y deja reportes de prueba que Jenkins publica.

**Evidencia 3:** [Insertar captura de las tres etapas].
**Evidencia 4:** [Insertar captura del resultado REAL de JUnit].

### Etapa 3: notificación de resultado
Se integró Telegram mediante el método `sendMessage` de la Bot API. El token y el chat ID se almacenan mediante credenciales `Secret text` en Jenkins. La instrucción `catchError` permite que la etapa de notificación se ejecute incluso cuando la compilación o las pruebas fallan después de haber iniciado el pipeline.

**Evidencia 5:** [Insertar captura del mensaje EXITOSA].
**Evidencia 6:** [Insertar captura del mensaje FALLIDA].

## Resultados experimentales

| Ejecución | Condición | Resultado observado en Jenkins | Notificación recibida |
| --- | --- | --- | --- |
| 1 | Pruebas correctas | [Completar al ejecutar] | [Completar] |
| 2 | Fallo deliberado en una aserción | [Completar al ejecutar] | [Completar] |
| 3 | Prueba restaurada | [Completar al ejecutar] | [Completar] |

## Conclusión propuesta (ajustar a lo observado)
La práctica permite demostrar cómo un pipeline de integración continua automatiza la obtención de código desde un repositorio privado, la validación de la calidad mediante pruebas y la comunicación del estado de ejecución. La gestión de credenciales por identificadores en Jenkins evita introducir secretos directamente en el repositorio. La simulación de un fallo ayuda a comprobar que las notificaciones informan tanto los resultados exitosos como los fallidos.

> Importante: no afirmar una ejecución exitosa ni agregar cifras o evidencias que aún no hayan sido observadas en Jenkins.
