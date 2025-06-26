package com.tp.uno.mas.encuentros.deportivos.state;

import com.tp.uno.mas.encuentros.deportivos.model.Partido;

public class EnJuego implements EstadoPartido {
    
    @Override
    public void manejarCambioEstado(Partido partido) {
        // Un partido en juego solo puede finalizar
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
        return true;
    }

    @Override
    public String getNombreEstado() {
        return "En Juego";
    }
} 