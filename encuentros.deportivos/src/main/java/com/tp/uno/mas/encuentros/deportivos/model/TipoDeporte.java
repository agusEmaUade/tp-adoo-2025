package com.tp.uno.mas.encuentros.deportivos.model;

/**
 * Enumeración que define los tipos de deportes disponibles en el sistema.
 * Siguiendo el principio SOLID de Open/Closed, nuevos deportes pueden añadirse fácilmente.
 */
public enum TipoDeporte {
    FUTBOL("Fútbol", 22, 90),
    VOLEY("Voleibol", 12, 60);

    private final String nombre;
    private final int jugadoresMaximos;
    private final int duracionMinutos;

    TipoDeporte(String nombre, int jugadoresMaximos, int duracionMinutos) {
        this.nombre = nombre;
        this.jugadoresMaximos = jugadoresMaximos;
        this.duracionMinutos = duracionMinutos;
    }

    public String getNombre() {
        return nombre;
    }

    public int getJugadoresMaximos() {
        return jugadoresMaximos;
    }

    public int getDuracionMinutos() {
        return duracionMinutos;
    }
} 