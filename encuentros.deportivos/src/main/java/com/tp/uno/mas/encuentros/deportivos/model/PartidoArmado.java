package com.tp.uno.mas.encuentros.deportivos.model;

/**
 * Estado inicial del partido - Patrón State.
 * Representa un partido recién creado que necesita jugadores.
 */
public class PartidoArmado implements IEstado {

    @Override
    public void manejar(Partido partido) {
        // Lógica específica para partido armado
        // Por ejemplo, notificar disponibilidad
    }

    @Override
    public String obtenerNombre() {
        return "PARTIDO_ARMADO";
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
        return new NecesitamosJugadores();
    }

    @Override
    public String toString() {
        return "PartidoArmado";
    }
} 