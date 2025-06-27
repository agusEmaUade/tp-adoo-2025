package com.tp.uno.mas.encuentros.deportivos.observer;

import com.tp.uno.mas.encuentros.deportivos.model.Partido;
import com.tp.uno.mas.encuentros.deportivos.model.Usuario;

public interface NotificacionObserver {
    void notificar(EventoPartido evento, Partido partido);
    void notificarUsuario(Usuario usuario, String titulo, String mensaje);
} 