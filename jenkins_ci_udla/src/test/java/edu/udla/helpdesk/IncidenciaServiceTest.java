package edu.udla.helpdesk;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.util.NoSuchElementException;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class IncidenciaServiceTest {
    @TempDir Path carpeta;

    private IncidenciaService servicio() throws Exception {
        return new IncidenciaService(carpeta.resolve("datos/incidencias.properties"));
    }

    @Test void registraIncidenciaConEstadoAbierto() throws Exception {
        Incidencia i = servicio().crear("Error al iniciar sesion", "Cuenta bloqueada", Prioridad.ALTA);
        assertEquals(1, i.id());
        assertEquals(Estado.ABIERTO, i.estado());
        assertEquals(Prioridad.ALTA, i.prioridad());
    }

    @Test void asignaIdsConsecutivos() throws Exception {
        IncidenciaService s = servicio();
        assertEquals(1, s.crear("Fallo de impresora", "", Prioridad.MEDIA).id());
        assertEquals(2, s.crear("Pantalla sin imagen", "", Prioridad.BAJA).id());
    }

    @Test void rechazaTituloDemasiadoCorto() throws Exception {
        IncidenciaService s = servicio();
        assertThrows(IllegalArgumentException.class, () -> s.crear("abc", "", Prioridad.MEDIA));
        assertEquals(0, s.total());
    }

    @Test void rechazaPrioridadNula() throws Exception {
        IncidenciaService s = servicio();
        assertThrows(IllegalArgumentException.class, () -> s.crear("Fallo de red", "", null));
    }

    @Test void validaLongitudDeDescripcion() throws Exception {
        IncidenciaService s = servicio();
        assertThrows(IllegalArgumentException.class, () -> s.crear("Fallo de red", "x".repeat(1001), Prioridad.ALTA));
    }

    @Test void actualizaEstadoDeIncidencia() throws Exception {
        IncidenciaService s = servicio();
        int id = s.crear("Servicio caido", "", Prioridad.CRITICA).id();
        assertEquals(Estado.EN_PROGRESO, s.cambiarEstado(id, Estado.EN_PROGRESO).estado());
        assertEquals(Estado.RESUELTO, s.cambiarEstado(id, Estado.RESUELTO).estado());
    }

    @Test void filtraIncidenciasPorEstado() throws Exception {
        IncidenciaService s = servicio();
        s.crear("Fallo de red", "", Prioridad.MEDIA);
        int id = s.crear("Copia de seguridad", "", Prioridad.BAJA).id();
        s.cambiarEstado(id, Estado.RESUELTO);
        assertEquals(1, s.listar(Estado.ABIERTO).size());
        assertEquals(2, s.listar(null).size());
    }

    @Test void eliminaIncidencia() throws Exception {
        IncidenciaService s = servicio();
        int id = s.crear("Fallo de red", "", Prioridad.MEDIA).id();
        s.eliminar(id);
        assertEquals(0, s.total());
        assertThrows(NoSuchElementException.class, () -> s.buscar(id));
    }

    @Test void mantieneDatosTrasReinicio() throws Exception {
        IncidenciaService primero = servicio();
        int id = primero.crear("Servicio caido", "Interrupcion", Prioridad.CRITICA).id();
        primero.cambiarEstado(id, Estado.RESUELTO);
        IncidenciaService reiniciado = servicio();
        assertEquals(Estado.RESUELTO, reiniciado.buscar(id).estado());
        assertEquals("Interrupcion", reiniciado.buscar(id).descripcion());
        assertEquals(id + 1, reiniciado.crear("Fallo de mouse", "", Prioridad.BAJA).id());
    }

    @Test void calculaEstadisticasPorEstado() throws Exception {
        IncidenciaService s = servicio();
        s.crear("Red lenta", "", Prioridad.BAJA);
        int id = s.crear("Disco lleno", "", Prioridad.ALTA).id();
        s.cambiarEstado(id, Estado.EN_PROGRESO);
        assertEquals(1L, s.contarPorEstado().get(Estado.ABIERTO));
        assertEquals(1L, s.contarPorEstado().get(Estado.EN_PROGRESO));
        assertEquals(0L, s.contarPorEstado().get(Estado.RESUELTO));
    }

    @Test void fallaAlModificarIncidenciaInexistente() throws Exception {
        IncidenciaService s = servicio();
        assertThrows(NoSuchElementException.class, () -> s.cambiarEstado(999, Estado.RESUELTO));
    }
}
