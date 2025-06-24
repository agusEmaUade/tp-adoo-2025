package com.tp.uno.mas.encuentros.deportivos.state;

import com.tp.uno.mas.encuentros.deportivos.model.Partido;

public class Finalizado implements EstadoPartido {
    
    @Override
    public void manejarCambioEstado(Partido partido) {
        // Un partido finalizado no puede cambiar de estado
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
        return false;
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
        return "Finalizado";
    }
} 