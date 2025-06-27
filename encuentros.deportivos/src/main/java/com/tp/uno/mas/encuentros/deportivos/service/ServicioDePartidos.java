package com.tp.uno.mas.encuentros.deportivos.service;

import com.tp.uno.mas.encuentros.deportivos.adapter.FirebaseAdapter;
import com.tp.uno.mas.encuentros.deportivos.adapter.JavaMailAdapter;
import com.tp.uno.mas.encuentros.deportivos.factory.*;
import com.tp.uno.mas.encuentros.deportivos.model.*;
import com.tp.uno.mas.encuentros.deportivos.observer.EmailNotificador;
import com.tp.uno.mas.encuentros.deportivos.observer.NotificacionManager;
import com.tp.uno.mas.encuentros.deportivos.observer.PushNotificador;
import com.tp.uno.mas.encuentros.deportivos.strategy.*;

import java.util.ArrayList;
import java.util.List;

public class ServicioDePartidos {


    private BuscadorPartidos buscadorPartidos;
    private List<Partido> partidosDisponibles;
    private NotificacionManager notificacionManager;
    private GestorPartido gestorPartido;
    private ServicioProgramador servicioProgramador;

    public ServicioDePartidos() {
        this.buscadorPartidos = new BuscadorPartidos();
        this.partidosDisponibles = new ArrayList<>();
        this.notificacionManager = configurarNotificaciones();
        this.gestorPartido = new GestorPartido(notificacionManager);
        this.servicioProgramador = new ServicioProgramador(this.gestorPartido, new VerificadorHorarios());
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
    // MÉTODOS DE BÚSQUEDA DE PARTIDOS
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
        for (int i = 0; i < resultados.size(); i++) {
            Partido partido = resultados.get(i);
            double distancia = buscadorPartidos.calcularDistanciaAlPartido(usuario, partido);
            System.out.println((i + 1) + ". " + partido.getDeporte() + " - " + 
                             String.format("%.1f km", distancia) + " - " + 
                             buscadorPartidos.contarJugadoresNecesarios(partido) + " jugadores necesarios");
        }
        
        return resultados;
    }

    public List<Partido> buscarPorDeporte(Usuario usuario, String deporte) {
        System.out.println("\n--- Buscando partidos de " + deporte + " para " + usuario.getNombre() + " ---");
        
        List<Partido> resultados = buscadorPartidos.buscarPartidosPorDeporte(partidosDisponibles, usuario, deporte);
        
        System.out.println("Partidos de " + deporte + " encontrados: " + resultados.size());
        for (int i = 0; i < resultados.size(); i++) {
            imprimirDetallePartido(resultados.get(i), usuario, i + 1);
        }
        
        return resultados;
    }

    public List<Partido> buscarPartidosProximosACompletarse(Usuario usuario, int maxJugadoresNecesarios) {
        System.out.println("\n--- Buscando partidos próximos a completarse (máximo " + maxJugadoresNecesarios + " jugadores necesarios) ---");
        
        List<Partido> resultados = buscadorPartidos.buscarPartidosQueNecesitanPocoJugadores(
            partidosDisponibles, usuario, maxJugadoresNecesarios);
        
        System.out.println("Partidos próximos a completarse: " + resultados.size());
        for (int i = 0; i < resultados.size(); i++) {
            imprimirDetallePartido(resultados.get(i), usuario, i + 1);
        }
        
        return resultados;
    }

    private void imprimirDetallePartido(Partido partido, Usuario usuario, int numero) {
        int jugadoresNecesarios = buscadorPartidos.contarJugadoresNecesarios(partido);
        double distancia = buscadorPartidos.calcularDistanciaAlPartido(usuario, partido);
        
        System.out.println(numero + ". " + partido.getDeporte() + " - " + partido.getFecha() +
                         " - " + String.format("%.1f km", distancia) + 
                         " - " + jugadoresNecesarios + " jugadores necesarios");
    }

    // Getters
    public List<Partido> getPartidosDisponibles() {
        return new ArrayList<>(partidosDisponibles);
    }

    public BuscadorPartidos getBuscadorPartidos() {
        return buscadorPartidos;
    }

    public GestorPartido getGestorPartido() {
        return gestorPartido;
    }

    public NotificacionManager getNotificacionManager() {
        return notificacionManager;
    }

    public ServicioProgramador getServicioProgramador() {
        return servicioProgramador;
    }
}