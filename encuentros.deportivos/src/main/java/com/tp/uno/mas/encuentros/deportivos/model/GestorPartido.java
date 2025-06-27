package com.tp.uno.mas.encuentros.deportivos.model;

import com.tp.uno.mas.encuentros.deportivos.observer.EventoPartido;
import com.tp.uno.mas.encuentros.deportivos.observer.NotificacionManager;
import com.tp.uno.mas.encuentros.deportivos.state.*;
import com.tp.uno.mas.encuentros.deportivos.factory.PartidoFactory;

public class GestorPartido {
    private NotificacionManager notificacionManager;

    public GestorPartido(NotificacionManager notificacionManager) {
        this.notificacionManager = notificacionManager;
    }

    public Partido crearPartido(PartidoFactory factory, String fecha, Ubicacion ubicacion, Usuario organizador) {
        Partido partido = factory.crearPartidoCompleto(fecha, ubicacion, organizador);
        
        agregarJugador(partido, organizador);

        notificarEvento(partido, EventoPartido.PARTIDO_CREADO);
        return partido;
    }

    public boolean agregarJugador(Partido partido, Usuario jugador) {
        if (partido == null || jugador == null || !partido.getEstadoActual().puedeAgregarJugador()) {
            return false;
        }

        if (partido.getCriterios() != null && !partido.getCriterios().cumpleCriterios(jugador)) {
            return false;
        }

        boolean agregado = partido.agregarJugador(jugador);
        if (agregado) {
            notificarEvento(partido, EventoPartido.JUGADOR_UNIDO);
            // La transición a PartidoArmado es manejada por el estado mismo, pero si se completa
            // disparamos la notificación desde aquí.
            if (partido.getEstadoActual() instanceof PartidoArmado) {
                 notificarEvento(partido, EventoPartido.PARTIDO_ARMADO);
            }
        }
        return agregado;
    }

    public boolean confirmarPartido(Partido partido) {
        if (!validarOperacion(partido, "confirmar")) {
            System.out.println("No se puede confirmar el partido en el estado actual");
            return false;
        }

        partido.cambiarEstado(new Confirmado());
        notificarEvento(partido, EventoPartido.PARTIDO_CONFIRMADO);
        System.out.println("Partido confirmado");
        
        return true;
    }

    public boolean cancelarPartido(Partido partido) {
        if (!validarOperacion(partido, "cancelar")) {
            System.out.println("No se puede cancelar el partido en el estado actual");
            return false;
        }

        partido.cambiarEstado(new Cancelado());
        notificarEvento(partido, EventoPartido.PARTIDO_CANCELADO);
        System.out.println("Partido cancelado");
        
        return true;
    }

    public boolean iniciarPartido(Partido partido) {
        if (!validarOperacion(partido, "iniciar")) {
            System.out.println("No se puede iniciar el partido en el estado actual");
            return false;
        }

        if (!partido.puedeIniciar()) {
            System.out.println("El partido no está listo para iniciar");
            return false;
        }

        partido.cambiarEstado(new EnJuego());
        notificarEvento(partido, EventoPartido.PARTIDO_EN_JUEGO);
        System.out.println("¡Partido iniciado!");
        
        return true;
    }

    public boolean finalizarPartido(Partido partido) {
        if (!validarOperacion(partido, "finalizar")) {
            System.out.println("No se puede finalizar el partido en el estado actual");
            return false;
        }

        partido.cambiarEstado(new Finalizado());
        notificarEvento(partido, EventoPartido.PARTIDO_FINALIZADO);
        System.out.println("Partido finalizado");

        // Actualizar historial de los jugadores
        for (Usuario jugador : partido.getJugadoresActuales()) {
            jugador.agregarAPartidoHistorial(partido);
        }
        
        return true;
    }

    private boolean validarOperacion(Partido partido, String operacion) {
        EstadoPartido estado = partido.getEstadoActual();
        
        switch (operacion) {
            case "agregar_jugador":
                return estado.puedeAgregarJugador();
            case "confirmar":
                return estado.puedeConfirmar();
            case "cancelar":
                return estado.puedeCancelar();
            case "iniciar":
                return estado.puedeIniciar();
            case "finalizar":
                return estado.puedeFinalizar();
            default:
                return false;
        }
    }

    private void notificarEvento(Partido partido, EventoPartido evento) {
        if (notificacionManager != null) {
            notificacionManager.notificarObservers(evento, partido);
        }
    }

    // Getters y Setters
    public NotificacionManager getNotificacionManager() {
        return notificacionManager;
    }

    public void setNotificacionManager(NotificacionManager notificacionManager) {
        this.notificacionManager = notificacionManager;
    }
} 