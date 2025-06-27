package com.tp.uno.mas.encuentros.deportivos.state;

import com.tp.uno.mas.encuentros.deportivos.model.Partido;

public class PartidoArmado implements EstadoPartido {
    
    @Override
    public void manejarCambioEstado(Partido partido) {
        // Un partido armado puede cambiar a confirmado o cancelado, pero es una acción manual,
        // no una transición automática por cambio de estado interno.
    }

    @Override
    public boolean puedeAgregarJugador() {
        return false; // Ya está completo
    }

    @Override
    public boolean puedeConfirmar() {
        return true;
    }

    @Override
    public boolean puedeCancelar() {
        return true;
    }

    @Override
    public boolean puedeIniciar() {
        return false;
    }

    @Override
    public boolean puedeFinalizar() {
        return false;
    }

    @Override
    public String getNombreEstado() {
        return "Partido Armado";
    }
} 