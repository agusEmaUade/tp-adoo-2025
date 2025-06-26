package com.tp.uno.mas.encuentros.deportivos.controller;

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

public class PartidoController {


    private BuscadorPartidos buscadorPartidos;
    private List<Partido> partidosDisponibles;

    public PartidoController() {
        this.buscadorPartidos = new BuscadorPartidos();
        this.partidosDisponibles = new ArrayList<>();
    }

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

    public List<Partido> buscarPartidosParaUsuario(Usuario usuario, CriteriosBusqueda criterios) {
        System.out.println("\n--- Búsqueda de Partidos para " + usuario.getNombre() + " ---");
        
        List<Partido> resultados = buscadorPartidos.buscarPartidos(partidosDisponibles, usuario, criterios);
        
        System.out.println("Criterios de búsqueda:");
        imprimirCriterios(criterios);
        
        System.out.println("\nResultados encontrados: " + resultados.size() + " partidos");
        
        for (int i = 0; i < resultados.size(); i++) {
            Partido partido = resultados.get(i);
            imprimirDetallePartido(partido, usuario, i + 1);
        }
        
        return resultados;
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

    public void demostrarBusquedaDePartidos(List<Usuario> usuarios) {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("    BÚSQUEDA DE PARTIDOS:");
        System.out.println("=".repeat(60));

        // Crear partidos de ejemplo que necesitan jugadores
        crearPartidosDeEjemplo(usuarios);

        // Demostrar diferentes tipos de búsqueda para cada usuario
        for (Usuario usuario : usuarios.subList(0, 2)) { // Solo los primeros 2 usuarios para no hacer muy largo el demo
            demostrarBusquedaCompleta(usuario);
        }

        // Caso de uso específico: Usuario busca partido de su deporte favorito
        System.out.println("\n--- CASO DE USO: Búsqueda específica ---");
        Usuario juan = usuarios.get(0);
        CriteriosBusqueda criteriosEspecificos = new CriteriosBusqueda();
        criteriosEspecificos.setDeporte("Fútbol");
        criteriosEspecificos.setRadioMaximo(15.0);
        criteriosEspecificos.setSoloPartidosDisponibles(true);
        criteriosEspecificos.setMaxJugadoresNecesarios(3);

        List<Partido> resultados = buscarPartidosParaUsuario(juan, criteriosEspecificos);
        
        if (!resultados.isEmpty()) {
            System.out.println("✓ " + juan.getNombre() + " encontró " + resultados.size() + " partidos compatibles");
            System.out.println("✓ Funcionalidad de búsqueda implementada correctamente");
        } else {
            System.out.println("ℹ No se encontraron partidos con esos criterios");
        }
    }

    private void demostrarBusquedaCompleta(Usuario usuario) {
        System.out.println("\n=== DEMOSTRACIÓN COMPLETA DE BÚSQUEDA DE PARTIDOS ===");
        System.out.println("Usuario: " + usuario.getNombre() + " (" + usuario.getDeporteFavorito() + ", " + usuario.getNivel() + ")");
        
        // 1. Búsqueda general con criterios personalizados
        CriteriosBusqueda criteriosGenerales = new CriteriosBusqueda();
        criteriosGenerales.setDeporte(usuario.getDeporteFavorito());
        criteriosGenerales.setRadioMaximo(10.0);
        criteriosGenerales.setSoloPartidosDisponibles(true);
        criteriosGenerales.setMaxJugadoresNecesarios(5);
        
        buscarPartidosParaUsuario(usuario, criteriosGenerales);
        
        // 2. Búsqueda por cercanía
        buscarPartidosCercanos(usuario, 5.0);
        
        // 3. Búsqueda por deporte favorito
        buscarPorDeporte(usuario, usuario.getDeporteFavorito());
        
        // 4. Búsqueda de partidos próximos a completarse
        buscarPartidosProximosACompletarse(usuario, 3);
        
        System.out.println("\n=== FIN DE DEMOSTRACIÓN ===\n");
    }

    private void crearPartidosDeEjemplo(List<Usuario> usuarios) {
        // Configurar sistema básico para crear partidos
        NotificacionManager notificationManager = new NotificacionManager();
        Emparejador emparejador = new Emparejador(new EmparejamientoPorNivel());
        GestorPartido gestorPartido = new GestorPartido(notificationManager, emparejador);

        // Ubicaciones de ejemplo
        Ubicacion ubicacionPalermo = new Ubicacion(-34.5755f, -58.4330f, 5.0f);
        Ubicacion ubicacionCentro = new Ubicacion(-34.6118f, -58.3960f, 5.0f);
        Ubicacion ubicacionRecoleta = new Ubicacion(-34.5875f, -58.3974f, 5.0f);

        // Crear diferentes partidos que necesitan jugadores
        
        // 1. Partido de Fútbol (necesita muchos jugadores)
        PartidoFactory futbolFactory = new FutbolFactory();
        Partido partidoFutbol = gestorPartido.crearPartido(futbolFactory, "2024-12-25 16:00", ubicacionPalermo, usuarios.get(0));
        CriteriosPartido criteriosFutbol = new CriteriosPartido("principiante", "avanzado", 18, 40, "mixto", 15.0f);
        partidoFutbol.aplicarCriterios(criteriosFutbol);
        registrarPartido(partidoFutbol);

        // 2. Partido de Tenis (necesita pocos jugadores)
        PartidoFactory tenisFactory = new TenisFactory();
        Partido partidoTenis = gestorPartido.crearPartido(tenisFactory, "2024-12-26 10:00", ubicacionRecoleta, usuarios.get(1));
        // Agregar un jugador al tenis para que solo necesite 1 más
        gestorPartido.agregarJugador(partidoTenis, usuarios.get(2));
        registrarPartido(partidoTenis);

        // 3. Partido de Básquet 
        PartidoFactory basquetFactory = new BasquetFactory();
        Partido partidoBasquet = gestorPartido.crearPartido(basquetFactory, "2024-12-27 19:00", ubicacionCentro, usuarios.get(2));
        CriteriosPartido criteriosBasquet = new CriteriosPartido("intermedio", "avanzado", 20, 35, "masculino", 10.0f);
        partidoBasquet.aplicarCriterios(criteriosBasquet);
        registrarPartido(partidoBasquet);

        // 4. Otro partido de Fútbol en distinta ubicación
        Partido partidoFutbol2 = gestorPartido.crearPartido(futbolFactory, "2024-12-28 15:00", ubicacionCentro, usuarios.get(3));
        // Agregar algunos jugadores para que necesite menos
        gestorPartido.agregarJugador(partidoFutbol2, usuarios.get(0));
        gestorPartido.agregarJugador(partidoFutbol2, usuarios.get(1));
        registrarPartido(partidoFutbol2);

        System.out.println("✓ Creados " + partidosDisponibles.size() + " partidos de ejemplo para búsqueda");
    }

    private void imprimirCriterios(CriteriosBusqueda criterios) {
        if (criterios.getDeporte() != null) {
            System.out.println("- Deporte: " + criterios.getDeporte());
        }
        if (criterios.getRadioMaximo() > 0) {
            System.out.println("- Radio máximo: " + criterios.getRadioMaximo() + " km");
        }
        if (criterios.getMaxJugadoresNecesarios() > 0) {
            System.out.println("- Máximo jugadores necesarios: " + criterios.getMaxJugadoresNecesarios());
        }
        if (criterios.isSoloPartidosDisponibles()) {
            System.out.println("- Solo partidos que acepten nuevos jugadores");
        }
    }

    private void imprimirDetallePartido(Partido partido, Usuario usuario, int numero) {
        int jugadoresNecesarios = buscadorPartidos.contarJugadoresNecesarios(partido);
        double distancia = buscadorPartidos.calcularDistanciaAlPartido(usuario, partido);
        
        System.out.println(numero + ". " + partido.getDeporte() + " - " + partido.getFecha());
        System.out.println("   Estado: " + partido.getEstadoActual().getNombreEstado());
        System.out.println("   Distancia: " + String.format("%.1f km", distancia));
        System.out.println("   Jugadores necesarios: " + jugadoresNecesarios + "/" + partido.getCantJugadoresRequeridos());
        if (partido.getCriterios() != null) {
            System.out.println("   Criterios: Nivel " + partido.getCriterios().getNivelMinimo() + 
                             "-" + partido.getCriterios().getNivelMaximo());
        }
        System.out.println("   Organizador: " + partido.getOrganizador().getNombre());
        System.out.println();
    }

    // Getters para acceso externo
    public List<Partido> getPartidosDisponibles() {
        return new ArrayList<>(partidosDisponibles);
    }

    public BuscadorPartidos getBuscadorPartidos() {
        return buscadorPartidos;
    }
}