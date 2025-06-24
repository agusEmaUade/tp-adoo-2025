package com.tp.uno.mas.encuentros.deportivos.model;

/**
 * Estado cuando el partido está en curso - Patrón State.
 * El partido ya comenzó y está siendo jugado.
 */
public class EnJuego implements IEstado {

    @Override
    public void manejar(Partido partido) {
        // Lógica específica: gestionar el partido en curso
        // Puede incluir tracking de tiempo, eventos del partido, etc.
    }

    @Override
    public String obtenerNombre() {
        return "EN_JUEGO";
    }

    @Override
    public boolean puedeAgregarJugadores() {
        return false; // No se pueden agregar jugadores durante el juego
    }

    @Override
    public boolean puedeRemoverJugadores() {
        return false; // No se pueden remover jugadores durante el juego
    }

    @Override
    public IEstado siguienteEstado() {
        return new Finalizado();
    }

    @Override
    public String toString() {
        return "EnJuego";
    }
} 