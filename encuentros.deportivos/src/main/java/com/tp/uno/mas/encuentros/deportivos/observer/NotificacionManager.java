package com.tp.uno.mas.encuentros.deportivos.observer;

import com.tp.uno.mas.encuentros.deportivos.model.Partido;
import com.tp.uno.mas.encuentros.deportivos.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class NotificacionManager {
    private List<NotificacionObserver> observers;

    public NotificacionManager() {
        this.observers = new ArrayList<>();
    }

    public void agregarObserver(NotificacionObserver observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    public void eliminarObserver(NotificacionObserver observer) {
        observers.remove(observer);
    }

    public void notificarObservers(EventoPartido evento, Partido partido) {
        for (NotificacionObserver observer : observers) {
            observer.notificar(evento, partido);
        }
    }

    public void suscribirUsuario(Usuario usuario, String tipoNotificacion) {
        // Lógica para suscribir usuario a tipos específicos de notificaciones
        System.out.println("Usuario " + usuario.getNombre() + " suscrito a notificaciones de tipo: " + tipoNotificacion);
    }

    public List<NotificacionObserver> getObservers() {
        return new ArrayList<>(observers);
    }
} 