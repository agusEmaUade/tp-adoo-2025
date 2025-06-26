package com.tp.uno.mas.encuentros.deportivos.observer;

import com.tp.uno.mas.encuentros.deportivos.model.Partido;

public interface NotificacionObserver {
    void notificar(EventoPartido evento, Partido partido);
} 