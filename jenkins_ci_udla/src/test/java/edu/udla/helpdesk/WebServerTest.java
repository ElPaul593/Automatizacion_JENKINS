package edu.udla.helpdesk;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Path;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WebServerTest {
    @TempDir Path carpeta;

    @Test void permiteCrearYConsultarIncidenciaViaHttp() throws Exception {
        WebServer server = new WebServer(new IncidenciaService(carpeta.resolve("datos.properties")), 0);
        server.iniciar();
        try {
            String url = "http://127.0.0.1:" + server.puerto();
            HttpClient cliente = HttpClient.newHttpClient();
            String datos = "titulo=" + URLEncoder.encode("Falla de autenticacion", StandardCharsets.UTF_8)
                    + "&descripcion=Usuario+bloqueado&prioridad=ALTA";
            HttpRequest registrar = HttpRequest.newBuilder(URI.create(url + "/api/incidencias"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(datos)).build();
            HttpResponse<String> respuesta = cliente.send(registrar, HttpResponse.BodyHandlers.ofString());
            assertEquals(201, respuesta.statusCode());
            assertTrue(respuesta.body().contains("Falla de autenticacion"));
            HttpRequest listado = HttpRequest.newBuilder(URI.create(url + "/api/incidencias")).GET().build();
            assertTrue(cliente.send(listado, HttpResponse.BodyHandlers.ofString()).body().contains("ALTA"));
        } finally {
            server.detener();
        }
    }

    @Test void rechazaTituloInvalidoViaHttp() throws Exception {
        WebServer server = new WebServer(new IncidenciaService(carpeta.resolve("datos.properties")), 0);
        server.iniciar();
        try {
            HttpRequest registrar = HttpRequest.newBuilder(URI.create("http://127.0.0.1:"
                    + server.puerto() + "/api/incidencias"))
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString("titulo=ab&prioridad=ALTA")).build();
            HttpResponse<String> respuesta = HttpClient.newHttpClient()
                    .send(registrar, HttpResponse.BodyHandlers.ofString());
            assertEquals(400, respuesta.statusCode());
        } finally {
            server.detener();
        }
    }
}
