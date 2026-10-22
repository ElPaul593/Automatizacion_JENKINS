package edu.udla.helpdesk;

import java.time.Instant;

/** Entidad de dominio: incidente reportado por un usuario. */
public record Incidencia(int id, String titulo, String descripcion,
                         Prioridad prioridad, Estado estado, Instant creadaEn) {

    public Incidencia conEstado(Estado nuevoEstado) {
        return new Incidencia(id, titulo, descripcion, prioridad, nuevoEstado, creadaEn);
    }
}
