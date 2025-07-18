package com.tp.uno.mas.encuentros.deportivos.observer;

import com.tp.uno.mas.encuentros.deportivos.adapter.ServicioPush;
import com.tp.uno.mas.encuentros.deportivos.model.Partido;

public class PushNotificador implements NotificacionObserver {
    private ServicioPush servicioPush;

    public PushNotificador(ServicioPush servicioPush) {
        this.servicioPush = servicioPush;
    }

    @Override
    public void notificar(EventoPartido evento, Partido partido) {
        String titulo = construirTitulo(evento);
        String mensaje = construirMensajePush(evento, partido);
        
        // Enviar push al organizador
        if (partido.getOrganizador() != null) {
            servicioPush.enviarPush(partido.getOrganizador(), titulo, mensaje);
        }
        
        // Enviar push a todos los jugadores
        partido.getEquipos().forEach(equipo -> 
            equipo.getJugadores().forEach(jugador -> 
                servicioPush.enviarPush(jugador, titulo, mensaje)
            )
        );
    }

    private String construirTitulo(EventoPartido evento) {
        switch (evento) {
            case PARTIDO_CREADO: return "¡Nuevo partido!";
            case PARTIDO_ARMADO: return "¡Partido listo!";
            case PARTIDO_CONFIRMADO: return "¡Partido confirmado!";
            case PARTIDO_EN_JUEGO: return "¡A jugar!";
            case PARTIDO_FINALIZADO: return "Partido terminado";
            case PARTIDO_CANCELADO: return "Partido cancelado";
            case JUGADOR_UNIDO: return "Nuevo jugador";
            default: return "Actualización";
        }
    }

    private String construirMensajePush(EventoPartido evento, Partido partido) {
        switch (evento) {
            case PARTIDO_CREADO: 
                return "Se creó un nuevo partido de " + partido.getDeporte();
            case PARTIDO_ARMADO: 
                return "El partido de " + partido.getDeporte() + " está completo";
            case PARTIDO_CONFIRMADO: 
                return "El partido de " + partido.getDeporte() + " fue confirmado";
            case PARTIDO_EN_JUEGO: 
                return "¡El partido de " + partido.getDeporte() + " ha comenzado!";
            case PARTIDO_FINALIZADO: 
                return "El partido de " + partido.getDeporte() + " ha terminado";
            case PARTIDO_CANCELADO: 
                return "El partido de " + partido.getDeporte() + " fue cancelado";
            case JUGADOR_UNIDO: 
                return "Un nuevo jugador se unió al partido";
            default: 
                return "Actualización en tu partido";
        }
    }
} 