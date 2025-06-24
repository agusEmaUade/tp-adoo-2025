package com.tp.uno.mas.encuentros.deportivos.state;

import com.tp.uno.mas.encuentros.deportivos.model.Partido;

public class PartidoArmado implements EstadoPartido {
    
    @Override
    public void manejarCambioEstado(Partido partido) {
        // El partido armado puede cambiar a confirmado manualmente
        // o puede volver a necesitar jugadores si se van algunos
        if (!partido.estaCompleto()) {
            partido.cambiarEstado(new NecesitamosJugadores());
        }
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