package com.tp.uno.mas.encuentros.deportivos.model;

/**
 * Enumeración que define los niveles de juego disponibles.
 * Permite categorizar a los jugadores según su experiencia.
 */
public enum NivelDeJuego {
    PRINCIPIANTE("Principiante", "Para jugadores que están comenzando"),
    INTERMEDIO("Intermedio", "Para jugadores con experiencia moderada"),
    AVANZADO("Avanzado", "Para jugadores con mucha experiencia");

    private final String nombre;
    private final String descripcion;

    NivelDeJuego(String nombre, String descripcion) {
        this.nombre = nombre;
        this.descripcion = descripcion;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }
} 