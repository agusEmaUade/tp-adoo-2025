package com.tp.uno.mas.encuentros.deportivos.observer;

import com.tp.uno.mas.encuentros.deportivos.adapter.ServicioEmail;
import com.tp.uno.mas.encuentros.deportivos.model.Partido;

public class EmailNotificador implements NotificacionObserver {
    private ServicioEmail servicioEmail;

    public EmailNotificador(ServicioEmail servicioEmail) {
        this.servicioEmail = servicioEmail;
    }

    @Override
    public void notificar(EventoPartido evento, Partido partido) {
        String asunto = construirAsunto(evento);
        String mensaje = construirMensaje(evento, partido);
        
        // Enviar email al organizador
        if (partido.getOrganizador() != null) {
            servicioEmail.enviarEmail(partido.getOrganizador().getEmail(), asunto, mensaje);
        }
        
        // Enviar email a todos los jugadores
        partido.getEquipos().forEach(equipo -> 
            equipo.getJugadores().forEach(jugador -> 
                servicioEmail.enviarEmail(jugador.getEmail(), asunto, mensaje)
            )
        );
    }

    private String construirAsunto(EventoPartido evento) {
        switch (evento) {
            case PARTIDO_CREADO: return "Nuevo partido creado";
            case PARTIDO_ARMADO: return "Partido completado";
            case PARTIDO_CONFIRMADO: return "Partido confirmado";
            case PARTIDO_EN_JUEGO: return "Partido iniciado";
            case PARTIDO_FINALIZADO: return "Partido finalizado";
            case PARTIDO_CANCELADO: return "Partido cancelado";
            case JUGADOR_UNIDO: return "Nuevo jugador se unió";
            default: return "Actualización del partido";
        }
    }

    private String construirMensaje(EventoPartido evento, Partido partido) {
        StringBuilder mensaje = new StringBuilder();
        mensaje.append("Hola,\n\n");
        mensaje.append("Te informamos sobre una actualización en el partido:\n");
        mensaje.append("Deporte: ").append(partido.getDeporte()).append("\n");
        mensaje.append("Fecha: ").append(partido.getFecha()).append("\n");
        mensaje.append("Estado: ").append(partido.getEstadoActual().getNombreEstado()).append("\n");
        mensaje.append("Evento: ").append(evento).append("\n\n");
        mensaje.append("Saludos,\nEquipo de Encuentros Deportivos");
        return mensaje.toString();
    }
} 