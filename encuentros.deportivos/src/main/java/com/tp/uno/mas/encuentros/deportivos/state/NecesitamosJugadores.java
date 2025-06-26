package com.tp.uno.mas.encuentros.deportivos.state;

import com.tp.uno.mas.encuentros.deportivos.model.Partido;

public class NecesitamosJugadores implements EstadoPartido {
    
    @Override
    public void manejarCambioEstado(Partido partido) {
        if (partido.estaCompleto()) {
            partido.cambiarEstado(new PartidoArmado());
        }
    }

    @Override
    public boolean puedeAgregarJugador() {
        return true;
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
        return false;
    }

    @Override
    public boolean puedeFinalizar() {
        return false;
    }

    @Override
    public String getNombreEstado() {
        return "Necesitamos Jugadores";
    }
} 