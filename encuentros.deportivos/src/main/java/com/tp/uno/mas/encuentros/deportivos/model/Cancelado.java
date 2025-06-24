package com.tp.uno.mas.encuentros.deportivos.model;

/**
 * Estado cuando el partido es cancelado - Patrón State.
 * El partido fue cancelado por alguna razón.
 */
public class Cancelado implements IEstado {

    @Override
    public void manejar(Partido partido) {
        // Lógica específica: notificar cancelación
        // Procesar reembolsos o compensaciones si aplica
    }

    @Override
    public String obtenerNombre() {
        return "CANCELADO";
    }

    @Override
    public boolean puedeAgregarJugadores() {
        return false; // Partido cancelado
    }

    @Override
    public boolean puedeRemoverJugadores() {
        return false; // Partido cancelado
    }

    @Override
    public IEstado siguienteEstado() {
        return null; // Estado final
    }

    @Override
    public String toString() {
        return "Cancelado";
    }
} 