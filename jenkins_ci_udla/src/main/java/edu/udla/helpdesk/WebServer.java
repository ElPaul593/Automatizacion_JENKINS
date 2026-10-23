package edu.udla.helpdesk;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;

/** API HTTP y sitio web para registrar, listar, actualizar y eliminar incidencias. */
public final class WebServer {
    private static final Logger LOGGER = Logger.getLogger(WebServer.class.getName());
    private static final int MAX_CUERPO_BYTES = 65536;
    private static final int LIMITE_CARACTER_CONTROL = 0x20;
    private static final String PARAM_ESTADO = PARAM_ESTADO;
    private static final String TIPO_JSON = TIPO_JSON;
    private final HttpServer server;
    private final IncidenciaService service;
    private final ExecutorService executor;

    public WebServer(IncidenciaService service, int puerto) throws IOException {
        this.service = service;
        this.server = HttpServer.create(new InetSocketAddress(InetAddress.getByAddress(new byte[]{127, 0, 0, 1}), puerto), 0);
        server.createContext("/", this::manejar);
        executor = Executors.newCachedThreadPool();
        server.setExecutor(executor);
    }

    public void iniciar() {
        server.start();
    }

    public void detener() {
        server.stop(0);
        executor.shutdownNow();
    }

    public int puerto() {
        return server.getAddress().getPort();
    }

    private void manejar(HttpExchange ex) throws IOException {
        try (ex) {
            try {
                String ruta = ex.getRequestURI().getPath();
                String metodo = ex.getRequestMethod();
                if ("/".equals(ruta) && "GET".equals(metodo)) {
                    try (InputStream entrada = WebServer.class.getResourceAsStream("/web/index.html")) {
                        if (entrada == null) {
                            enviar(ex, 500, "text/plain", "No se encontro la interfaz");
                        } else {
                            enviar(ex, 200, "text/html", new String(entrada.readAllBytes(), StandardCharsets.UTF_8));
                        }
                    }
                } else if ("/api/incidencias".equals(ruta) && "GET".equals(metodo)) {
                    Map<String, String> q = parametros(ex.getRequestURI().getRawQuery());
                    Estado estado = q.containsKey(PARAM_ESTADO) && !q.get(PARAM_ESTADO).isBlank()
                            ? Estado.valueOf(q.get(PARAM_ESTADO)) : null;
                    List<String> objetos = new ArrayList<>();
                    for (Incidencia i : service.listar(estado)) {
                        objetos.add(jsonIncidencia(i));
                    }
                    enviar(ex, 200, TIPO_JSON, "[" + String.join(",", objetos) + "]");
                } else if ("/api/incidencias".equals(ruta) && "POST".equals(metodo)) {
                    Map<String, String> datos = cuerpoFormulario(ex);
                    Incidencia i = service.crear(datos.get("titulo"), datos.get("descripcion"),
                            Prioridad.valueOf(datos.getOrDefault("prioridad", "MEDIA")));
                    enviar(ex, 201, TIPO_JSON, jsonIncidencia(i));
                } else if ("/api/estadisticas".equals(ruta) && "GET".equals(metodo)) {
                    Map<Estado, Long> c = service.contarPorEstado();
                    String json = "{\"total\":" + service.total()
                            + ",\"abiertas\":" + c.get(Estado.ABIERTO)
                            + ",\"enProgreso\":" + c.get(Estado.EN_PROGRESO)
                            + ",\"resueltas\":" + c.get(Estado.RESUELTO) + "}";
                    enviar(ex, 200, TIPO_JSON, json);
                } else if (ruta.matches("/api/incidencias/[0-9]+") && "DELETE".equals(metodo)) {
                    int id = Integer.parseInt(ruta.substring(ruta.lastIndexOf('/') + 1));
                    service.eliminar(id);
                    enviar(ex, 200, TIPO_JSON, "{\"eliminado\":true}");
                } else if (ruta.matches("/api/incidencias/[0-9]+/estado") && "POST".equals(metodo)) {
                    String[] partes = ruta.split("/");
                    int id = Integer.parseInt(partes[3]);
                    Estado nuevo = Estado.valueOf(parametros(leerCuerpo(ex)).get(PARAM_ESTADO));
                    enviar(ex, 200, TIPO_JSON, jsonIncidencia(service.cambiarEstado(id, nuevo)));
                } else if (ruta.startsWith("/api/")) {
                    enviar(ex, 404, TIPO_JSON, "{\"error\":\"Ruta no encontrada\"}");
                } else {
                    enviar(ex, 404, "text/plain", "No encontrado");
                }
            } catch (IllegalArgumentException | NullPointerException e) {
                enviar(ex, 400, TIPO_JSON, "{\"error\":" + jsonString(e.getMessage() == null
                        ? "Datos invalidos" : e.getMessage()) + "}");
            } catch (NoSuchElementException e) {
                enviar(ex, 404, TIPO_JSON, "{\"error\":" + jsonString(e.getMessage()) + "}");
            } catch (Exception e) {
                if (LOGGER.isLoggable(Level.WARNING)) {
                    LOGGER.warning("Error de servidor: " + e.getMessage());
                }
                enviar(ex, 500, TIPO_JSON, "{\"error\":\"Error interno del servidor\"}");
            }
        }
    }

    private static String leerCuerpo(HttpExchange ex) throws IOException {
        byte[] bytes = ex.getRequestBody().readNBytes(MAX_CUERPO_BYTES + 1);
        if (bytes.length > MAX_CUERPO_BYTES) {
            throw new IllegalArgumentException("Solicitud demasiado grande");
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private static Map<String, String> cuerpoFormulario(HttpExchange ex) throws IOException {
        String tipo = ex.getRequestHeaders().getFirst("Content-Type");
        if (tipo == null || !tipo.startsWith("application/x-www-form-urlencoded")) {
            throw new IllegalArgumentException("Se espera un formulario URL encoded");
        }
        return parametros(leerCuerpo(ex));
    }

    private static Map<String, String> parametros(String query) {
        Map<String, String> datos = new HashMap<>();
        if (query == null || query.isBlank()) {
            return datos;
        }
        for (String parte : query.split("&")) {
            if (parte.isBlank()) {
                continue;
            }
            String[] par = parte.split("=", 2);
            datos.put(URLDecoder.decode(par[0], StandardCharsets.UTF_8),
                    URLDecoder.decode(par.length > 1 ? par[1] : "", StandardCharsets.UTF_8));
        }
        return datos;
    }

    private static String jsonIncidencia(Incidencia i) {
        return "{\"id\":" + i.id() + ",\"titulo\":" + jsonString(i.titulo())
                + ",\"descripcion\":" + jsonString(i.descripcion())
                + ",\"prioridad\":" + jsonString(i.prioridad().name())
                + ",\"estado\":" + jsonString(i.estado().name())
                + ",\"creadaEn\":" + jsonString(i.creadaEn().toString()) + "}";
    }

    private static String jsonString(String texto) {
        String valor = texto == null ? "" : texto;
        StringBuilder b = new StringBuilder("\"");
        for (int k = 0; k < valor.length(); k++) {
            char c = valor.charAt(k);
            switch (c) {
                case '"' -> b.append("\\\"");
                case '\\' -> b.append("\\\\");
                case '\n' -> b.append("\\n");
                case '\r' -> b.append("\\r");
                case '\t' -> b.append("\\t");
                default -> {
                    if (c < LIMITE_CARACTER_CONTROL) {
                        b.append(String.format("\\u%04x", (int) c));
                    } else {
                        b.append(c);
                    }
                }
            }
        }
        return b.append('"').toString();
    }

    private static void enviar(HttpExchange ex, int codigo, String tipo, String contenido) throws IOException {
        byte[] respuesta = contenido.getBytes(StandardCharsets.UTF_8);
        ex.getResponseHeaders().set("Content-Type", tipo + "; charset=UTF-8");
        ex.getResponseHeaders().set("Cache-Control", "no-store");
        ex.getResponseHeaders().set("X-Content-Type-Options", "nosniff");
        ex.sendResponseHeaders(codigo, respuesta.length);
        try (OutputStream out = ex.getResponseBody()) {
            out.write(respuesta);
        }
    }
}
