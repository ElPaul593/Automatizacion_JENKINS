// Funciones reutilizables para el pipeline de HelpDesk UDLA.
// Cada rama paralela carga este modulo desde su propio workspace.

def ejecutarMaven(String cacheLocal, boolean generarCalidad) {
    if (!cacheLocal?.trim()) {
        error('La ruta del cache Maven no esta configurada.')
    }

    def opcionesCalidad = generarCalidad ? '-Pcalidad -DskipTests ' : ''
    echo "Ejecutando Maven: ${generarCalidad ? 'analisis de calidad' : 'compilacion y JUnit'}"
    echo "Repositorio Maven: ${cacheLocal}"

    bat "mvn -B -ntp \"-Dmaven.repo.local=${cacheLocal}\" ${opcionesCalidad}-f jenkins_ci_udla/pom.xml clean verify"
}

def compilarYProbar(String cacheLocal) {
    ejecutarMaven(cacheLocal, false)
}

def analizarCalidad(String cacheLocal) {
    ejecutarMaven(cacheLocal, true)
}

def comprobarDocumentacion() {
    bat 'if not exist "jenkins_ci_udla\\README.md" exit /b 1'
    echo 'Validacion de README completada.'
}

return this
