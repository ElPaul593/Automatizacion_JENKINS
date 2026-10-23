
# HelpDesk UDLA — Gestión de Incidencias con Jenkins CI

Aplicación web desarrollada para **registrar, gestionar y dar seguimiento a incidencias de soporte técnico**, utilizando herramientas de integración continua como **Jenkins, Maven, JUnit 5 y GitHub**, junto con notificaciones automatizadas por Telegram.

---

## 1. Descripción del proyecto

**HelpDesk UDLA** es un sistema de gestión de incidencias que permite registrar solicitudes de soporte técnico, administrar prioridades, actualizar estados y consultar estadísticas.

El proyecto se desarrolló como una aplicación funcional para demostrar el uso de un **pipeline de integración continua (CI)** mediante Jenkins.

### Objetivo

Automatizar la integración del código fuente, la compilación, la ejecución de pruebas y la notificación del resultado mediante un pipeline de tres etapas.

---

## 2. Tecnologías utilizadas

| Tecnología | Descripción |
|---|---|
| **Java 17+** | Desarrollo de la lógica de negocio y servidor HTTP |
| **HTML, CSS y JavaScript** | Interfaz web |
| **Apache Maven** | Gestión del proyecto, compilación y generación del JAR |
| **JUnit 5** | Pruebas unitarias y de integración |
| **Git y GitHub** | Control de versiones y repositorio remoto |
| **Jenkins** | Automatización del pipeline CI |
| **Telegram Bot API** | Notificaciones de éxito o fallo |

---

## 3. Funcionalidades principales

El sistema permite:

- **Registrar incidencias** con título, descripción y prioridad.
- **Consultar incidencias** registradas.
- **Filtrar incidencias** según su estado.
- **Cambiar el estado** de una incidencia.
- **Eliminar incidencias**.
- **Consultar estadísticas** de incidencias abiertas, en progreso y resueltas.
- **Almacenar información localmente**, conservando los registros después de reiniciar la aplicación.

### Prioridades disponibles

- Baja
- Media
- Alta
- Crítica

### Estados disponibles

- Abierto
- En progreso
- Resuelto

---

## 4. Estructura del proyecto

```text
Automatizacion_JENKINS/
└── jenkins_ci_udla/
    ├── Jenkinsfile
    ├── pom.xml
    ├── README.md
    ├── .gitignore
    └── src/
        ├── main/
        │   ├── java/
        │   │   └── edu/udla/helpdesk/
        │   │       ├── Main.java
        │   │       ├── WebServer.java
        │   │       ├── Incidencia.java
        │   │       ├── IncidenciaService.java
        │   │       ├── Estado.java
        │   │       └── Prioridad.java
        │   └── resources/
        │       └── web/
        │           └── index.html
        └── test/
            └── java/
                └── edu/udla/helpdesk/
                    ├── IncidenciaServiceTest.java
                    └── WebServerTest.java
```

### Archivos importantes

| Archivo | Función |
|---|---|
| `Jenkinsfile` | Define las tres etapas del pipeline |
| `pom.xml` | Configura Maven y las dependencias |
| `Main.java` | Punto de entrada de la aplicación |
| `WebServer.java` | Gestiona las solicitudes HTTP |
| `IncidenciaService.java` | Implementa la lógica de incidencias |
| `IncidenciaServiceTest.java` | Pruebas unitarias |
| `WebServerTest.java` | Pruebas de integración HTTP |

---

## 5. Requisitos previos

Antes de ejecutar el proyecto se necesita:

- **JDK 17 o superior**
- **Apache Maven 3.8 o superior**
- **Git**
- **Navegador web**

### Verificar Java

```powershell
java -version
```

### Verificar Maven

```powershell
mvn -version
```

### Verificar Git

```powershell
git --version
```

---

## 6. Instalación y ejecución

### Paso 1. Clonar el repositorio

```powershell
git clone https://github.com/ElPaul593/Automatizacion_JENKINS.git
```

### Paso 2. Acceder al proyecto

```powershell
cd Automatizacion_JENKINS\jenkins_ci_udla
```

### Paso 3. Compilar y ejecutar las pruebas

```powershell
mvn clean verify
```

Este comando:

1. Limpia los archivos de compilaciones anteriores.
2. Compila el código fuente.
3. Ejecuta las pruebas automatizadas.
4. Verifica el proyecto.
5. Genera el archivo JAR.

### Resultado esperado

```text
Tests run: 13, Failures: 0, Errors: 0, Skipped: 0

BUILD SUCCESS
```

**Nota:** el resumen anterior representa el resultado esperado de una ejecución correcta.

### Paso 4. Ejecutar la aplicación

```powershell
java -jar target/gestion-incidencias-1.0.0.jar
```

### Paso 5. Abrir la aplicación

Ingresar desde el navegador:

**http://localhost:8080**

La aplicación mostrará la interfaz **HelpDesk — Gestión de Incidencias**.

Para detener el servidor, presionar `Ctrl + C` en la terminal.

---

## 7. Pruebas automatizadas

El proyecto incorpora **13 pruebas automatizadas con JUnit 5**.

| Tipo de prueba | Cantidad | Propósito |
|---|---:|---|
| Pruebas unitarias | 11 | Verificar la lógica de gestión de incidencias |
| Pruebas de integración HTTP | 2 | Verificar el comportamiento del servidor web |
| **Total** | **13** | **Comprobar el funcionamiento del proyecto** |

### Ejecutar todas las pruebas

```powershell
mvn clean verify
```

### Consultar los reportes

Los resultados de JUnit se generan en:

```text
target/surefire-reports/
```

Para visualizar los resultados desde PowerShell:

```powershell
Get-ChildItem .\target\surefire-reports\*.txt |
Select-String "Tests run:"
```

---

## 8. API HTTP

La aplicación utiliza los siguientes endpoints:

| Método | Endpoint | Función |
|---|---|---|
| `GET` | `/` | Mostrar la interfaz web |
| `GET` | `/api/incidencias` | Consultar incidencias |
| `GET` | `/api/incidencias?estado=ABIERTO` | Filtrar por estado |
| `POST` | `/api/incidencias` | Registrar incidencia |
| `POST` | `/api/incidencias/{id}/estado` | Actualizar estado |
| `DELETE` | `/api/incidencias/{id}` | Eliminar incidencia |
| `GET` | `/api/estadisticas` | Consultar estadísticas |

La aplicación utiliza almacenamiento local y no requiere una base de datos externa.

**Importante:** se trata de un prototipo académico para ejecución local, sin autenticación de usuarios. No está preparado para su exposición directa a Internet.

---

## 9. Integración continua con Jenkins

El proyecto utiliza un pipeline configurado mediante el archivo `Jenkinsfile`.

El pipeline está dividido en **tres etapas principales**.

### Etapa 1. Integración con repositorio privado

Jenkins obtiene el código fuente desde GitHub.

**Repositorio:**

```text
https://github.com/ElPaul593/Automatizacion_JENKINS.git
```

**Configuración:**

- Rama: `main`
- Credencial de Jenkins: `github-private`
- Archivo del pipeline: `jenkins_ci_udla/Jenkinsfile`

Para cumplir el requisito de integración privada, el repositorio debe estar configurado como **Private** y Jenkins debe disponer de los permisos correspondientes.

### Etapa 2. Compilación y pruebas automatizadas

Jenkins ejecuta Maven para compilar el sistema y ejecutar las pruebas JUnit.

**Comando utilizado:**

```bat
mvn -B -ntp -f jenkins_ci_udla/pom.xml clean verify
```

**Actividades automatizadas:**

- Compilación del código Java.
- Ejecución de pruebas unitarias.
- Ejecución de pruebas de integración.
- Generación del archivo JAR.
- Publicación de resultados JUnit.
- Archivado del JAR generado.

**Resultado esperado:** compilación exitosa y 13 pruebas aprobadas.

### Etapa 3. Notificación por Telegram

El pipeline utiliza un bot de Telegram para comunicar el resultado de cada ejecución.

**Tipos de notificación:**

- `EXITOSA`: cuando la ejecución finaliza correctamente.
- `FALLIDA`: cuando se produce un error en el pipeline.

Los mensajes contienen:

- Estado de la ejecución.
- Nombre del trabajo de Jenkins.
- Número de compilación.
- Enlace a la ejecución.

**Credenciales necesarias:**

```text
telegram-bot-token
telegram-chat-id
```

Las credenciales se administran desde Jenkins y no deben almacenarse directamente en el repositorio.

---

## 10. Configuración del pipeline en Jenkins

El Jenkinsfile está preparado para un agente **Windows**, utilizando comandos `bat` y `powershell`.

### Herramientas necesarias en Jenkins

- Git
- Java 17 o superior
- Apache Maven
- PowerShell
- Plugins de Pipeline, Git y JUnit

Maven debe estar configurado en Jenkins con el nombre:

```text
Maven3
```

### Configuración del job

Si se utiliza **Pipeline script from SCM**:

| Campo | Valor |
|---|---|
| Repository URL | `https://github.com/ElPaul593/Automatizacion_JENKINS.git` |
| Credentials | `github-private` |
| Branch | `*/main` |
| Script Path | `jenkins_ci_udla/Jenkinsfile` |

Después, seleccionar **Build Now** para ejecutar el pipeline.

---

## 11. Evidencias de funcionamiento

Para verificar el cumplimiento de la primera tarea se consideran las siguientes evidencias:

### Integración con GitHub

- Repositorio privado.
- Configuración de credenciales.
- Descarga correcta del código desde Jenkins.

### Compilación y pruebas

- Ejecución local de `mvn clean verify`.
- Resultado `BUILD SUCCESS`.
- Reportes de las 13 pruebas JUnit.
- Archivo JAR generado.
- Ejecución de la segunda etapa en Jenkins.

### Notificaciones

- Ejecución exitosa en Jenkins.
- Ejecución fallida controlada.
- Mensajes recibidos mediante Telegram.

**Nota:** las verificaciones locales no sustituyen la ejecución real del pipeline en Jenkins.

---

## 12. Preparación para la segunda tarea

HelpDesk también puede utilizarse para la siguiente práctica:

**Configuración de un pipeline de entrega continua / Estrategias de optimización.**

El proyecto puede ampliarse mediante:

- **Análisis estático de código** con Checkstyle, PMD o SonarQube.
- **Ejecución paralela** de pruebas y verificaciones.
- **Caché de Maven** para optimizar las dependencias.
- **Ejecución selectiva** por ramas y etapas.
- **Pipelines modulares y reutilizables**.
- **Estrategias de branching** para organizar el desarrollo.

Estas funcionalidades corresponden a la segunda tarea y no forman parte de la implementación inicial.

---

## 13. Consideraciones finales

HelpDesk UDLA es una aplicación funcional que permite demostrar cómo Jenkins automatiza procesos de integración continua sobre un proyecto Java real.

Mediante GitHub, Maven, JUnit 5 y Telegram, se integran actividades de control de versiones, compilación, pruebas automatizadas y notificación de resultados.

El proyecto proporciona una base para incorporar posteriormente estrategias avanzadas de optimización de pipelines.

---

**Universidad de Las Américas (UDLA)**  
**Carrera:** Ingeniería de Software  
**Proyecto:** HelpDesk — Gestión de Incidencias  
**Asignatura:** Integración de Sistemas
