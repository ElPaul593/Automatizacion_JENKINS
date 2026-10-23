package edu.udla.helpdesk;

import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class Main {
    private static final Logger LOGGER = Logger.getLogger(Main.class.getName());
    private Main() {}

    public static void main(String[] args) throws Exception {
        int puerto = Integer.parseInt(System.getenv().getOrDefault("HELPDESK_PORT", "8080"));
        Path archivo = Path.of(System.getenv().getOrDefault("HELPDESK_DATA", "data/incidencias.properties"));
        WebServer servidor = new WebServer(new IncidenciaService(archivo), puerto);
        servidor.iniciar();
        if (LOGGER.isLoggable(Level.INFO)) {
            LOGGER.info("Sistema de incidencias disponible en http://localhost:" + servidor.puerto());
        }
    }
}
