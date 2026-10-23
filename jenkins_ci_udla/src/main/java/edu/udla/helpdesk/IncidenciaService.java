package edu.udla.helpdesk;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Properties;

/**
 * Reglas de negocio y persistencia local del sistema de incidencias.
 * Se sincronizan las operaciones para proteger el almacenamiento en memoria.
 */
public final class IncidenciaService {
    private static final int MAX_LONGITUD_DESCRIPCION = 1000;
    private static final String PREFIJO_INCIDENTE = "incidente.";
    private final Path archivo;
    private final Map<Integer, Incidencia> incidencias = new LinkedHashMap<>();
    private int siguienteId = 1;

    public IncidenciaService(Path archivo) throws IOException {
        this.archivo = archivo;
        cargar();
    }

    public synchronized Incidencia crear(String titulo, String descripcion, Prioridad prioridad)
            throws IOException {
        String tituloValidado = validarTitulo(titulo);
        String descripcionValidada = validarDescripcion(descripcion);
        if (prioridad == null) {
            throw new IllegalArgumentException("La prioridad es obligatoria");
        }
        Incidencia incidencia = new Incidencia(siguienteId, tituloValidado, descripcionValidada,
                prioridad, Estado.ABIERTO, Instant.now());
        Map<Integer, Incidencia> nuevoMapa = new LinkedHashMap<>(incidencias);
        nuevoMapa.put(incidencia.id(), incidencia);
        guardar(nuevoMapa, siguienteId + 1);
        incidencias.put(incidencia.id(), incidencia);
        siguienteId++;
        return incidencia;
    }

    public synchronized List<Incidencia> listar(Estado filtro) {
        return incidencias.values().stream()
                .filter(i -> filtro == null || i.estado() == filtro)
                .sorted(Comparator.comparingInt(Incidencia::id).reversed())
                .toList();
    }

    public synchronized Incidencia buscar(int id) {
        Incidencia resultado = incidencias.get(id);
        if (resultado == null) {
            throw new NoSuchElementException("No existe la incidencia " + id);
        }
        return resultado;
    }

    public synchronized Incidencia cambiarEstado(int id, Estado nuevoEstado) throws IOException {
        if (nuevoEstado == null) {
            throw new IllegalArgumentException("El estado es obligatorio");
        }
        Incidencia actual = buscar(id);
        Incidencia modificada = actual.conEstado(nuevoEstado);
        Map<Integer, Incidencia> nuevoMapa = new LinkedHashMap<>(incidencias);
        nuevoMapa.put(id, modificada);
        guardar(nuevoMapa, siguienteId);
        incidencias.put(id, modificada);
        return modificada;
    }

    public synchronized void eliminar(int id) throws IOException {
        buscar(id);
        Map<Integer, Incidencia> nuevoMapa = new LinkedHashMap<>(incidencias);
        nuevoMapa.remove(id);
        guardar(nuevoMapa, siguienteId);
        incidencias.remove(id);
    }

    public synchronized Map<Estado, Long> contarPorEstado() {
        Map<Estado, Long> conteo = new LinkedHashMap<>();
        for (Estado e : Estado.values()) {
            conteo.put(e, incidencias.values().stream().filter(i -> i.estado() == e).count());
        }
        return conteo;
    }

    public synchronized int total() {
        return incidencias.size();
    }

    private static String validarTitulo(String titulo) {
        if (titulo == null || titulo.trim().length() < 4 || titulo.trim().length() > 120) {
            throw new IllegalArgumentException("El titulo debe tener entre 4 y 120 caracteres");
        }
        return titulo.trim();
    }

    private static String validarDescripcion(String descripcion) {
        String valor = descripcion == null ? "" : descripcion.trim();
        if (valor.length() > MAX_LONGITUD_DESCRIPCION) {
            throw new IllegalArgumentException("La descripcion no puede exceder 1000 caracteres");
        }
        return valor;
    }

    private void cargar() throws IOException {
        if (!Files.exists(archivo)) {
            return;
        }
        Properties p = new Properties();
        try (InputStream in = Files.newInputStream(archivo)) {
            p.load(in);
        }
        siguienteId = Integer.parseInt(p.getProperty("siguienteId", "1"));
        List<Integer> ids = new ArrayList<>();
        for (String nombre : p.stringPropertyNames()) {
            if (nombre.startsWith(PREFIJO_INCIDENTE) && nombre.endsWith(".titulo")) {
                String id = nombre.substring(PREFIJO_INCIDENTE.length(), nombre.length() - ".titulo".length());
                ids.add(Integer.parseInt(id));
            }
        }
        for (int id : ids) {
            String prefijo = PREFIJO_INCIDENTE + id + ".";
            Incidencia i = new Incidencia(id,
                    p.getProperty(prefijo + "titulo"),
                    p.getProperty(prefijo + "descripcion", ""),
                    Prioridad.valueOf(p.getProperty(prefijo + "prioridad")),
                    Estado.valueOf(p.getProperty(prefijo + "estado")),
                    Instant.parse(p.getProperty(prefijo + "creadaEn")));
            incidencias.put(id, i);
            siguienteId = Math.max(siguienteId, id + 1);
        }
    }

    private void guardar(Map<Integer, Incidencia> elementos, int proximoId) throws IOException {
        Path padre = archivo.toAbsolutePath().getParent();
        Files.createDirectories(padre);
        Properties p = new Properties();
        p.setProperty("siguienteId", String.valueOf(proximoId));
        for (Incidencia i : elementos.values()) {
            String prefijo = PREFIJO_INCIDENTE + i.id() + ".";
            p.setProperty(prefijo + "titulo", i.titulo());
            p.setProperty(prefijo + "descripcion", i.descripcion());
            p.setProperty(prefijo + "prioridad", i.prioridad().name());
            p.setProperty(prefijo + "estado", i.estado().name());
            p.setProperty(prefijo + "creadaEn", i.creadaEn().toString());
        }
        Path temporal = Files.createTempFile(padre, "incidencias-", ".tmp");
        try {
            try (OutputStream out = Files.newOutputStream(temporal)) {
                p.store(out, "Gestion de incidencias - datos locales");
            }
            try {
                Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING,
                        StandardCopyOption.ATOMIC_MOVE);
            } catch (AtomicMoveNotSupportedException e) {
                Files.move(temporal, archivo, StandardCopyOption.REPLACE_EXISTING);
            }
        } finally {
            Files.deleteIfExists(temporal);
        }
    }
}
