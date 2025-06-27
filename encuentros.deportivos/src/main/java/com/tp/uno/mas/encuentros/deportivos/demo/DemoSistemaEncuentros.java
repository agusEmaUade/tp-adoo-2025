package com.tp.uno.mas.encuentros.deportivos.demo;

import com.tp.uno.mas.encuentros.deportivos.controller.CuentaController;
import com.tp.uno.mas.encuentros.deportivos.service.ServicioDePartidos;
import com.tp.uno.mas.encuentros.deportivos.factory.*;
import com.tp.uno.mas.encuentros.deportivos.model.*;
import com.tp.uno.mas.encuentros.deportivos.observer.NotificacionManager;

import java.util.List;

public class DemoSistemaEncuentros {

    private final ServicioDePartidos servicioDePartidos;
    private final GestorPartido gestorPartido;
    private final NotificacionManager notificacionManager;
    private final List<Usuario> usuarios;

    public DemoSistemaEncuentros() {
        System.out.println("=== DEMO: Sistema de Encuentros Deportivos ===\n");
        CuentaController cuentaController = new CuentaController();
        this.usuarios = cuentaController.crearUsuarios();
        this.servicioDePartidos = new ServicioDePartidos();
        this.gestorPartido = servicioDePartidos.getGestorPartido();
        this.notificacionManager = servicioDePartidos.getNotificacionManager();
    }

    public void ejecutarDemoCompleta() {
        iniciarSimulacionPartidos();
        demostrarBusquedaDePartidos();
        System.out.println("\n=== FIN DEL DEMO ===");
    }

    private void iniciarSimulacionPartidos() {
        Ubicacion ubicacionCancha = new Ubicacion(-34.6037f, -58.3816f, 5.0f);
        demostrarCreacionPartidos(ubicacionCancha);
    }

    private void demostrarCreacionPartidos(Ubicacion ubicacion) {
        System.out.println("--- Demostrando Creación de Partidos (Patrón Factory) ---");
        Usuario organizador = usuarios.get(0);

        Partido partidoFutbol = gestorPartido.crearPartido(new FutbolFactory(), "2024-12-20 15:00", ubicacion, organizador);
        servicioDePartidos.registrarPartido(partidoFutbol);

        System.out.println("\n--- Agregando Jugadores al Partido de Fútbol ---");
        cicloVidaPartido(partidoFutbol);
    }

    private void cicloVidaPartido(Partido partido) {
        System.out.println("Estado inicial: " + partido.getEstadoActual().getNombreEstado());
        partido.aplicarCriterios(new CriteriosPartido("principiante", "avanzado", 18, 40, "mixto", 10.0f));
        System.out.println("✓ Criterios aplicados al partido");

        for (Usuario u : usuarios) {
            boolean agregado = gestorPartido.agregarJugador(partido, u);
            System.out.println((agregado ? "✓" : "✗") + " " + u.getNombre() + (agregado ? " agregado" : " no pudo ser agregado"));
        }

        if (partido.getEstadoActual().puedeConfirmar()) {
            gestorPartido.confirmarPartido(partido);
        }

        servicioDePartidos.getServicioProgramador().verificarYActualizarPartidos(List.of(partido));

        if (partido.getEstadoActual().puedeFinalizar()) {
            gestorPartido.finalizarPartido(partido);
        }

        System.out.println("Estado final: " + partido.getEstadoActual().getNombreEstado());
    }

    private void demostrarBusquedaDePartidos() {
        System.out.println("\n" + "=".repeat(60));
        System.out.println("    BÚSQUEDA DE PARTIDOS:");
        System.out.println("=".repeat(60));

        crearPartidosDeEjemplo();
        notificarSobreNuevosPartidos();

        for (Usuario usuario : usuarios.subList(0, 2)) {
            demostrarBusquedaCompleta(usuario);
        }

        System.out.println("\n--- CASO DE USO: Búsqueda con criterios mixtos ---");
        Usuario juan = usuarios.get(0);

        List<Partido> resultados = servicioDePartidos.getBuscadorPartidos().buscarPartidosConCriteriosMixtos(
                servicioDePartidos.getPartidosDisponibles(), juan, 2);

        if (!resultados.isEmpty()) {
            System.out.println("✓ " + juan.getNombre() + " encontró " + resultados.size() + " partidos compatibles");
            System.out.println("✓ Funcionalidad de búsqueda implementada correctamente");
        } else {
            System.out.println("ℹ No se encontraron partidos que cumplan los criterios");
        }

        System.out.println("\n=== FIN DE DEMOSTRACIÓN DE BÚSQUEDA ===\n");
    }

    private void demostrarBusquedaCompleta(Usuario usuario) {
        System.out.println("\n=== DEMOSTRACIÓN COMPLETA DE BÚSQUEDA PARA " + usuario.getNombre() + " ===");
        System.out.println("Usuario: " + usuario.getNombre() + " (" + usuario.getDeporteFavorito() + ", " + usuario.getNivel() + ")");

        servicioDePartidos.getBuscadorPartidos().buscarPartidosPorNivel(servicioDePartidos.getPartidosDisponibles(), usuario);
        servicioDePartidos.buscarPartidosCercanos(usuario, 5.0);
        servicioDePartidos.buscarPorDeporte(usuario, usuario.getDeporteFavorito());
        servicioDePartidos.buscarPartidosProximosACompletarse(usuario, 3);
    }

    private void notificarSobreNuevosPartidos() {
        System.out.println("\n--- Notificando a usuarios sobre partidos de su interés ---");
        for (Partido partido : servicioDePartidos.getPartidosDisponibles()) {
            for (Usuario usuario : usuarios) {
                if (partido.getJugadoresActuales().contains(usuario)) {
                    continue;
                }
                if (usuario.getDeporteFavorito().equalsIgnoreCase(partido.getDeporte())) {
                    String titulo = "¡Nuevo partido de " + partido.getDeporte() + "!";
                    String mensaje = "Hay un nuevo partido cerca de ti para el " + partido.getFecha() + ". ¡Únete!";
                    notificacionManager.notificarUsuarioEspecifico(usuario, titulo, mensaje);
                }
            }
        }
    }

    private void crearPartidosDeEjemplo() {
        Ubicacion ubicacionPalermo = new Ubicacion(-34.5755f, -58.4330f, 5.0f);
        Ubicacion ubicacionCentro = new Ubicacion(-34.6118f, -58.3960f, 5.0f);
        Ubicacion ubicacionRecoleta = new Ubicacion(-34.5875f, -58.3974f, 5.0f);

        PartidoFactory futbolFactory = new FutbolFactory();
        Partido partidoFutbol = gestorPartido.crearPartido(futbolFactory, "2024-12-25 16:00", ubicacionPalermo, usuarios.get(0));
        partidoFutbol.aplicarCriterios(new CriteriosPartido("principiante", "avanzado", 18, 40, "mixto", 15.0f));
        servicioDePartidos.registrarPartido(partidoFutbol);

        PartidoFactory tenisFactory = new TenisFactory();
        Partido partidoTenis = gestorPartido.crearPartido(tenisFactory, "2024-12-26 10:00", ubicacionRecoleta, usuarios.get(1));
        gestorPartido.agregarJugador(partidoTenis, usuarios.get(2));
        servicioDePartidos.registrarPartido(partidoTenis);

        PartidoFactory basquetFactory = new BasquetFactory();
        Partido partidoBasquet = gestorPartido.crearPartido(basquetFactory, "2024-12-27 19:00", ubicacionCentro, usuarios.get(2));
        partidoBasquet.aplicarCriterios(new CriteriosPartido("intermedio", "avanzado", 20, 35, "masculino", 10.0f));
        servicioDePartidos.registrarPartido(partidoBasquet);

        Partido partidoFutbol2 = gestorPartido.crearPartido(futbolFactory, "2024-12-28 15:00", ubicacionCentro, usuarios.get(3));
        gestorPartido.agregarJugador(partidoFutbol2, usuarios.get(0));
        gestorPartido.agregarJugador(partidoFutbol2, usuarios.get(1));
        servicioDePartidos.registrarPartido(partidoFutbol2);

        System.out.println("✓ Creados " + servicioDePartidos.getPartidosDisponibles().size() + " partidos de ejemplo para búsqueda");
    }


    public static void main(String[] args) {
        DemoSistemaEncuentros demo = new DemoSistemaEncuentros();
        demo.ejecutarDemoCompleta();
    }
}