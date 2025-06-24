package com.tp.uno.mas.encuentros.deportivos.model;

/**
 * Estado cuando el partido necesita más jugadores - Patrón State.
 * Permite agregar jugadores hasta completar el cupo.
 */
public class NecesitamosJugadores implements IEstado {

    @Override
    public void manejar(Partido partido) {
        // Lógica específica: enviar notificaciones para buscar jugadores
        // Verificar si se completó el cupo para cambiar a Confirmado
    }

    @Override
    public String obtenerNombre() {
        return "NECESITAMOS_JUGADORES";
    }

    @Override
    public boolean puedeAgregarJugadores() {
        return true;
    }

    @Override
    public boolean puedeRemoverJugadores() {
        return true;
    }

    @Override
    public IEstado siguienteEstado() {
        return new Confirmado();
    }

    @Override
    public String toString() {
        return "NecesitamosJugadores";
    }
} 