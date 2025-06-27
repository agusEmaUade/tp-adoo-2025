package com.tp.uno.mas.encuentros.deportivos.controller;

import com.tp.uno.mas.encuentros.deportivos.adapter.FirebaseAdapter;
import com.tp.uno.mas.encuentros.deportivos.adapter.JavaMailAdapter;
import com.tp.uno.mas.encuentros.deportivos.factory.PartidoFactory;
import com.tp.uno.mas.encuentros.deportivos.model.*;
import com.tp.uno.mas.encuentros.deportivos.observer.EmailNotificador;
import com.tp.uno.mas.encuentros.deportivos.observer.EventoPartido;
import com.tp.uno.mas.encuentros.deportivos.observer.NotificacionManager;
import com.tp.uno.mas.encuentros.deportivos.observer.PushNotificador;
import com.tp.uno.mas.encuentros.deportivos.state.*;

import java.util.ArrayList;
import java.util.List;

public class PartidoController {

    private BuscadorPartidos buscadorPartidos;
    private List<Partido> partidosDisponibles;
    private NotificacionManager notificacionManager;
    private ServicioProgramador servicioProgramador;

    public PartidoController() {
        this.buscadorPartidos = new BuscadorPartidos();
        this.partidosDisponibles = new ArrayList<>();
        this.notificacionManager = configurarNotificaciones();
        this.servicioProgramador = new ServicioProgramador(this);
    }

    private NotificacionManager configurarNotificaciones() {
        System.out.println("--- Configurando Sistema de Notificaciones ---");

        NotificacionManager manager = new NotificacionManager();
        manager.agregarObserver(new EmailNotificador(new JavaMailAdapter()));
        manager.agregarObserver(new PushNotificador(new FirebaseAdapter()));

        System.out.println("✓ Sistema de notificaciones configurado\n");
        return manager;
    }

    // ===============================================================
    // MÉTODOS DE GESTIÓN DE PARTIDOS (BÚSQUEDA Y REGISTRO)
    // ===============================================================

    public void registrarPartido(Partido partido) {
        if (partido != null && !partidosDisponibles.contains(partido)) {
            partidosDisponibles.add(partido);
            System.out.println("✓ Partido registrado para búsqueda: " + partido.getDeporte() + " en " + partido.getFecha());
        }
    }

    public void eliminarPartido(Partido partido) {
        if (partidosDisponibles.remove(partido)) {
            System.out.println("✓ Partido eliminado de búsqueda: " + partido.getDeporte());
        }
    }

    public List<Partido> buscarPartidosCercanos(Usuario usuario, double radioKm) {
        System.out.println("\n--- Buscando partidos cercanos a " + usuario.getNombre() + " (radio: " + radioKm + "km) ---");
        List<Partido> resultados = buscadorPartidos.buscarPartidosCercanos(partidosDisponibles, usuario, radioKm);
        System.out.println("Partidos encontrados: " + resultados.size());
        resultados.forEach(p -> imprimirDetallePartido(p, usuario, resultados.indexOf(p) + 1));
        return resultados;
    }

    public List<Partido> buscarPorDeporte(Usuario usuario, String deporte) {
        System.out.println("\n--- Buscando partidos de " + deporte + " para " + usuario.getNombre() + " ---");
        List<Partido> resultados = buscadorPartidos.buscarPartidosPorDeporte(partidosDisponibles, usuario, deporte);
        System.out.println("Partidos de " + deporte + " encontrados: " + resultados.size());
        resultados.forEach(p -> imprimirDetallePartido(p, usuario, resultados.indexOf(p) + 1));
        return resultados;
    }

    // ===============================================================
    // MÉTODOS DE CICLO DE VIDA DE UN PARTIDO
    // ===============================================================

    public Partido crearPartido(PartidoFactory factory, String fecha, Ubicacion ubicacion, Usuario organizador) {
        Partido partido = factory.crearPartidoCompleto(fecha, ubicacion, organizador);
        agregarJugador(partido, organizador);
        notificarEvento(partido, EventoPartido.PARTIDO_CREADO);
        return partido;
    }

    public boolean agregarJugador(Partido partido, Usuario jugador) {
        if (partido == null || jugador == null || !partido.getEstadoActual().puedeAgregarJugador()) return false;
        if (partido.getCriterios() != null && !partido.getCriterios().cumpleCriterios(jugador)) return false;

        boolean agregado = partido.agregarJugador(jugador);
        if (agregado) {
            notificarEvento(partido, EventoPartido.JUGADOR_UNIDO);
            if (partido.getEstadoActual() instanceof PartidoArmado) {
                notificarEvento(partido, EventoPartido.PARTIDO_ARMADO);
            }
        }
        return agregado;
    }

    public boolean confirmarPartido(Partido partido) {
        if (!partido.getEstadoActual().puedeConfirmar()) return false;
        partido.cambiarEstado(new Confirmado());
        notificarEvento(partido, EventoPartido.PARTIDO_CONFIRMADO);
        return true;
    }

    public boolean cancelarPartido(Partido partido) {
        if (!partido.getEstadoActual().puedeCancelar()) return false;
        partido.cambiarEstado(new Cancelado());
        notificarEvento(partido, EventoPartido.PARTIDO_CANCELADO);
        return true;
    }

    public boolean iniciarPartido(Partido partido) {
        if (!partido.getEstadoActual().puedeIniciar() || !partido.puedeIniciar()) return false;
        partido.cambiarEstado(new EnJuego());
        notificarEvento(partido, EventoPartido.PARTIDO_EN_JUEGO);
        return true;
    }

    public boolean finalizarPartido(Partido partido) {
        if (!partido.getEstadoActual().puedeFinalizar()) return false;
        partido.cambiarEstado(new Finalizado());
        notificarEvento(partido, EventoPartido.PARTIDO_FINALIZADO);
        partido.getJugadoresActuales().forEach(j -> j.agregarAPartidoHistorial(partido));
        return true;
    }

    private void notificarEvento(Partido partido, EventoPartido evento) {
        if (notificacionManager != null) {
            notificacionManager.notificarObservers(evento, partido);
        }
    }

    private void imprimirDetallePartido(Partido partido, Usuario usuario, int numero) {
        int jugadoresNecesarios = buscadorPartidos.contarJugadoresNecesarios(partido);
        double distancia = buscadorPartidos.calcularDistanciaAlPartido(usuario, partido);
        System.out.println(numero + ". " + partido.getDeporte() + " - " + partido.getFecha() +
                         " - " + String.format("%.1f km", distancia) +
                         " - " + jugadoresNecesarios + " jugadores necesarios");
    }

    // Getters para acceso desde el exterior (ej. la Demo)
    public List<Partido> getPartidosDisponibles() { return new ArrayList<>(partidosDisponibles); }
    public BuscadorPartidos getBuscadorPartidos() { return buscadorPartidos; }
    public NotificacionManager getNotificacionManager() { return notificacionManager; }
    public ServicioProgramador getServicioProgramador() { return servicioProgramador; }
} 