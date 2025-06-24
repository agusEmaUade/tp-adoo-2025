package com.tp.uno.mas.encuentros.deportivos.model;

/**
 * Estado cuando el partido está confirmado - Patrón State.
 * El partido tiene suficientes jugadores y está listo para comenzar.
 */
public class Confirmado implements IEstado {

    @Override
    public void manejar(Partido partido) {
        // Lógica específica: confirmar asistencia de jugadores
        // Enviar recordatorios del partido
    }

    @Override
    public String obtenerNombre() {
        return "CONFIRMADO";
    }

    @Override
    public boolean puedeAgregarJugadores() {
        return false; // Ya está completo
    }

    @Override
    public boolean puedeRemoverJugadores() {
        return true; // Pueden salir hasta cierto punto
    }

    @Override
    public IEstado siguienteEstado() {
        return new EnJuego();
    }

    @Override
    public String toString() {
        return "Confirmado";
    }
} 