package com.tp.uno.mas.encuentros.deportivos.state;

import com.tp.uno.mas.encuentros.deportivos.model.Partido;

public class Confirmado implements EstadoPartido {
    
    @Override
    public void manejarCambioEstado(Partido partido) {
        // Un partido confirmado solo puede iniciar
    }

    @Override
    public boolean puedeAgregarJugador() {
        return false;
    }

    @Override
    public boolean puedeConfirmar() {
        return false;
    }

    @Override
    public boolean puedeCancelar() {
        return true;
    }

    @Override
    public boolean puedeIniciar() {
        return true;
    }

    @Override
    public boolean puedeFinalizar() {
        return false;
    }

    @Override
    public String getNombreEstado() {
        return "Confirmado";
    }
} 