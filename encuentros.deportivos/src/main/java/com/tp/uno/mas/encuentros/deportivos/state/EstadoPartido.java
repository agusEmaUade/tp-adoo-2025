package com.tp.uno.mas.encuentros.deportivos.state;

import com.tp.uno.mas.encuentros.deportivos.model.Partido;

public interface EstadoPartido {
    void manejarCambioEstado(Partido partido);
    boolean puedeAgregarJugador();
    boolean puedeConfirmar();
    boolean puedeCancelar();
    boolean puedeIniciar();
    boolean puedeFinalizar();
    String getNombreEstado();
} 