package com.tp.uno.mas.encuentros.deportivos.controller;

import com.tp.uno.mas.encuentros.deportivos.adapter.FirebaseAdapter;
import com.tp.uno.mas.encuentros.deportivos.adapter.JavaMailAdapter;
import com.tp.uno.mas.encuentros.deportivos.factory.FutbolFactory;
import com.tp.uno.mas.encuentros.deportivos.factory.PartidoFactory;
import com.tp.uno.mas.encuentros.deportivos.factory.TenisFactory;
import com.tp.uno.mas.encuentros.deportivos.model.*;
import com.tp.uno.mas.encuentros.deportivos.observer.EmailNotificador;
import com.tp.uno.mas.encuentros.deportivos.observer.NotificacionManager;
import com.tp.uno.mas.encuentros.deportivos.observer.PushNotificador;
import com.tp.uno.mas.encuentros.deportivos.strategy.*;

import java.util.List;

public class PartidoController {

    public void iniciarSimulacionPartidos(List<Usuario> usuarios) {
        NotificacionManager notificacionManager = configurarNotificaciones();
        Emparejador emparejador = new Emparejador(new EmparejamientoPorNivel());
        GestorPartido gestorPartido = new GestorPartido(notificacionManager, emparejador);
        Ubicacion ubicacionCancha = new Ubicacion(-34.6037f, -58.3816f, 5.0f);

        demostrarCreacionPartidos(gestorPartido, ubicacionCancha, usuarios);
        demostrarEstrategias(emparejador, usuarios);
    }

    private NotificacionManager configurarNotificaciones() {
        System.out.println("--- Configurando Sistema de Notificaciones ---");

        NotificacionManager manager = new NotificacionManager();
        manager.agregarObserver(new EmailNotificador(new JavaMailAdapter()));
        manager.agregarObserver(new PushNotificador(new FirebaseAdapter()));

        System.out.println("✓ Sistema de notificaciones configurado\n");
        return manager;
    }

    private void demostrarCreacionPartidos(GestorPartido gestor, Ubicacion ubicacion, List<Usuario> usuarios) {
        System.out.println("--- Demostrando Creación de Partidos (Patrón Factory) ---");
        Usuario organizador = usuarios.get(0);

        Partido partidoFutbol = gestor.crearPartido(new FutbolFactory(), "2024-12-20 15:00", ubicacion, organizador);
        Partido partidoTenis = gestor.crearPartido(new TenisFactory(), "2024-12-21 10:00", ubicacion, organizador);

        System.out.println("\n--- Agregando Jugadores al Partido de Fútbol ---");
        cicloVidaPartido(gestor, partidoFutbol, usuarios);
    }

    private void cicloVidaPartido(GestorPartido gestor, Partido partido, List<Usuario> usuarios) {
        System.out.println("Estado inicial: " + partido.getEstadoActual().getNombreEstado());

        partido.aplicarCriterios(new CriteriosPartido("principiante", "avanzado", 18, 40, "mixto", 10.0f));
        System.out.println("✓ Criterios aplicados al partido");

        for (Usuario u : usuarios) {
            boolean agregado = gestor.agregarJugador(partido, u);
            System.out.println((agregado ? "✓" : "✗") + " " + u.getNombre() + (agregado ? " agregado" : " no pudo ser agregado"));
        }

        if (partido.getEstadoActual().puedeConfirmar()) gestor.confirmarPartido(partido);
        if (partido.getEstadoActual().puedeIniciar()) gestor.iniciarPartido(partido);
        if (partido.getEstadoActual().puedeFinalizar()) gestor.finalizarPartido(partido);

        System.out.println("Estado final: " + partido.getEstadoActual().getNombreEstado());
    }

    private void demostrarEstrategias(Emparejador emparejador, List<Usuario> usuarios) {
        System.out.println("\n--- Demostrando Estrategias de Emparejamiento (Patrón Strategy) ---");
        Ubicacion ubicacion = new Ubicacion(-34.6037f, -58.3816f, 5.0f);
        Partido partido = new Partido("2024-12-22 16:00", "Fútbol", 4, 90, ubicacion, usuarios.get(0));

        ejecutarEstrategia(emparejador, new EmparejamientoPorNivel(), usuarios, partido, "por Nivel");
        ejecutarEstrategia(emparejador, new EmparejamientoPorCercania(), usuarios, partido, "por Cercanía");
        ejecutarEstrategia(emparejador, new EmparejamientoPorHistorial(), usuarios, partido, "por Historial");
        ejecutarEstrategia(emparejador, new EmparejamientoMixto(), usuarios, partido, "Mixta");
    }

    private void ejecutarEstrategia(Emparejador emp, EstrategiaEmparejamiento estrategia, List<Usuario> usuarios, Partido partido, String nombre) {
        System.out.println("\nEstrategia " + nombre + ":");
        emp.cambiarEstrategia(estrategia);
        Equipo equipo = emp.emparejarJugadores(usuarios, partido);
        if (equipo != null) System.out.println("✓ Equipo creado: " + equipo);
    }
}