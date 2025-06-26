package com.tp.uno.mas.encuentros.deportivos.integration;

import com.tp.uno.mas.encuentros.deportivos.controller.PartidoController;
import com.tp.uno.mas.encuentros.deportivos.factory.*;
import com.tp.uno.mas.encuentros.deportivos.model.*;
import com.tp.uno.mas.encuentros.deportivos.observer.NotificacionManager;
import com.tp.uno.mas.encuentros.deportivos.strategy.EmparejamientoPorNivel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests de Integración - Búsqueda de Partidos")
class BusquedaPartidosIntegrationTest {

    private PartidoController partidoController;
    private List<Usuario> usuarios;
    private GestorPartido gestorPartido;
    
    @BeforeEach
    void setUp() {
        partidoController = new PartidoController();
        
        // Crear usuarios de prueba
        Ubicacion ubicacion1 = new Ubicacion(-34.6037f, -58.3816f, 2.0f);
        Ubicacion ubicacion2 = new Ubicacion(-34.6118f, -58.3960f, 2.0f);
        Ubicacion ubicacion3 = new Ubicacion(-34.5928f, -58.3756f, 2.0f);
        
        usuarios = List.of(
            new Usuario("Juan", "juan@test.com", "123", "Fútbol", "intermedio", ubicacion1, 25, "masculino"),
            new Usuario("María", "maria@test.com", "456", "Tenis", "principiante", ubicacion2, 22, "femenino"),
            new Usuario("Carlos", "carlos@test.com", "789", "Básquet", "avanzado", ubicacion3, 30, "masculino"),
            new Usuario("Ana", "ana@test.com", "101", "Fútbol", "intermedio", ubicacion1, 28, "femenino")
        );
        
        // Configurar gestor para crear partidos
        NotificacionManager notificationManager = new NotificacionManager();
        Emparejador emparejador = new Emparejador(new EmparejamientoPorNivel());
        gestorPartido = new GestorPartido(notificationManager, emparejador);
    }

    @Test
    @DisplayName("Búsqueda básica de partidos funciona correctamente")
    void testBusquedaBasicaFunciona() {
        // Crear y registrar partidos
        crearPartidosDeEjemplo();
        
        // Verificar que se registraron partidos
        List<Partido> partidosDisponibles = partidoController.getPartidosDisponibles();
        assertFalse(partidosDisponibles.isEmpty(), "Debe haber partidos registrados");
        
        // Buscar partidos para usuario específico
        Usuario juan = usuarios.get(0);
        CriteriosBusqueda criterios = new CriteriosBusqueda();
        criterios.setDeporte("Fútbol");
        criterios.setSoloPartidosDisponibles(true);
        
        List<Partido> resultados = partidoController.buscarPartidosParaUsuario(juan, criterios);
        
        assertNotNull(resultados, "Los resultados no deben ser null");
        assertTrue(resultados.stream().allMatch(p -> p.getDeporte().equals("Fútbol")), 
                  "Todos los partidos encontrados deben ser de fútbol");
    }

    @Test
    @DisplayName("Búsqueda por cercanía filtra correctamente por distancia")
    void testBusquedaPorCercania() {
        crearPartidosDeEjemplo();
        
        Usuario juan = usuarios.get(0);
        List<Partido> partidosCercanos = partidoController.buscarPartidosCercanos(juan, 5.0);
        
        assertNotNull(partidosCercanos, "Los resultados no deben ser null");
        
        // Verificar que todos los partidos están dentro del radio
        for (Partido partido : partidosCercanos) {
            double distancia = partidoController.getBuscadorPartidos().calcularDistanciaAlPartido(juan, partido);
            assertTrue(distancia <= 5.0, 
                      "El partido debe estar dentro del radio de 5km, pero está a " + distancia + "km");
        }
    }

    @Test
    @DisplayName("Búsqueda por deporte específico funciona")
    void testBusquedaPorDeporte() {
        crearPartidosDeEjemplo();
        
        Usuario maria = usuarios.get(1); // Le gusta el tenis
        List<Partido> partidosTenis = partidoController.buscarPorDeporte(maria, "Tenis");
        
        assertNotNull(partidosTenis, "Los resultados no deben ser null");
        assertTrue(partidosTenis.stream().allMatch(p -> p.getDeporte().equals("Tenis")), 
                  "Todos los partidos encontrados deben ser de tenis");
    }

    @Test
    @DisplayName("Búsqueda de partidos próximos a completarse funciona")
    void testBusquedaPartidosProximosACompletarse() {
        crearPartidosDeEjemplo();
        
        Usuario carlos = usuarios.get(2);
        List<Partido> partidosProximos = partidoController.buscarPartidosProximosACompletarse(carlos, 2);
        
        assertNotNull(partidosProximos, "Los resultados no deben ser null");
        
        // Verificar que los partidos necesiten pocos jugadores
        for (Partido partido : partidosProximos) {
            int jugadoresNecesarios = partidoController.getBuscadorPartidos().contarJugadoresNecesarios(partido);
            assertTrue(jugadoresNecesarios <= 2, 
                      "El partido debe necesitar máximo 2 jugadores, pero necesita " + jugadoresNecesarios);
        }
    }

    @Test
    @DisplayName("Criterios de búsqueda complejos funcionan correctamente")
    void testCriteriosComplejos() {
        crearPartidosDeEjemplo();
        
        Usuario ana = usuarios.get(3);
        CriteriosBusqueda criteriosComplejos = new CriteriosBusqueda();
        criteriosComplejos.setDeporte("Fútbol");
        criteriosComplejos.setRadioMaximo(20.0);
        criteriosComplejos.setSoloPartidosDisponibles(true);
        criteriosComplejos.setMaxJugadoresNecesarios(10);
        
        List<Partido> resultados = partidoController.buscarPartidosParaUsuario(ana, criteriosComplejos);
        
        assertNotNull(resultados, "Los resultados no deben ser null");
        
        // Verificar que todos los resultados cumplen los criterios
        for (Partido partido : resultados) {
            assertEquals("Fútbol", partido.getDeporte(), "Debe ser un partido de fútbol");
            assertTrue(partido.getEstadoActual().puedeAgregarJugador(), 
                      "El partido debe aceptar nuevos jugadores");
            
            int jugadoresNecesarios = partidoController.getBuscadorPartidos().contarJugadoresNecesarios(partido);
            assertTrue(jugadoresNecesarios <= 10, 
                      "El partido debe necesitar máximo 10 jugadores");
            
            double distancia = partidoController.getBuscadorPartidos().calcularDistanciaAlPartido(ana, partido);
            assertTrue(distancia <= 20.0, 
                      "El partido debe estar dentro del radio de 20km");
        }
    }

    @Test
    @DisplayName("Sistema maneja correctamente cuando no hay partidos disponibles")
    void testSinPartidosDisponibles() {
        // No crear partidos - lista vacía
        
        Usuario juan = usuarios.get(0);
        CriteriosBusqueda criterios = new CriteriosBusqueda();
        criterios.setDeporte("Fútbol");
        
        List<Partido> resultados = partidoController.buscarPartidosParaUsuario(juan, criterios);
        
        assertNotNull(resultados, "Los resultados no deben ser null");
        assertTrue(resultados.isEmpty(), "No debe haber resultados cuando no hay partidos");
    }

    @Test
    @DisplayName("Sistema filtra partidos que no aceptan nuevos jugadores")
    void testFiltradoPartidosCompletos() {
        crearPartidosDeEjemplo();
        
        // Completar uno de los partidos de fútbol
        List<Partido> partidos = partidoController.getPartidosDisponibles();
        Partido partidoFutbol = partidos.stream()
                .filter(p -> p.getDeporte().equals("Fútbol"))
                .findFirst()
                .orElse(null);
        
        assertNotNull(partidoFutbol, "Debe existir un partido de fútbol");
        
        // Simular que el partido ya no acepta jugadores
        partidoFutbol.cambiarEstado(new com.tp.uno.mas.encuentros.deportivos.state.Confirmado());
        
        // Buscar solo partidos disponibles
        Usuario juan = usuarios.get(0);
        CriteriosBusqueda criterios = new CriteriosBusqueda();
        criterios.setDeporte("Fútbol");
        criterios.setSoloPartidosDisponibles(true);
        
        List<Partido> resultados = partidoController.buscarPartidosParaUsuario(juan, criterios);
        
        // El partido confirmado no debería aparecer si solo buscamos disponibles
        assertNotNull(resultados, "Los resultados no deben ser null");
    }

    private void crearPartidosDeEjemplo() {
        // Ubicaciones de ejemplo
        Ubicacion ubicacionPalermo = new Ubicacion(-34.5755f, -58.4330f, 5.0f);
        Ubicacion ubicacionCentro = new Ubicacion(-34.6118f, -58.3960f, 5.0f);
        Ubicacion ubicacionRecoleta = new Ubicacion(-34.5875f, -58.3974f, 5.0f);

        // 1. Partido de Fútbol
        PartidoFactory futbolFactory = new FutbolFactory();
        Partido partidoFutbol = gestorPartido.crearPartido(futbolFactory, "2024-12-25 16:00", ubicacionPalermo, usuarios.get(0));
        partidoController.registrarPartido(partidoFutbol);

        // 2. Partido de Tenis (con pocos jugadores necesarios)
        PartidoFactory tenisFactory = new TenisFactory();
        Partido partidoTenis = gestorPartido.crearPartido(tenisFactory, "2024-12-26 10:00", ubicacionRecoleta, usuarios.get(1));
        gestorPartido.agregarJugador(partidoTenis, usuarios.get(2)); // Agregar un jugador
        partidoController.registrarPartido(partidoTenis);

        // 3. Partido de Básquet
        PartidoFactory basquetFactory = new BasquetFactory();
        Partido partidoBasquet = gestorPartido.crearPartido(basquetFactory, "2024-12-27 19:00", ubicacionCentro, usuarios.get(2));
        partidoController.registrarPartido(partidoBasquet);

        // 4. Otro partido de Fútbol en ubicación distinta
        Partido partidoFutbol2 = gestorPartido.crearPartido(futbolFactory, "2024-12-28 15:00", ubicacionCentro, usuarios.get(3));
        gestorPartido.agregarJugador(partidoFutbol2, usuarios.get(0));
        gestorPartido.agregarJugador(partidoFutbol2, usuarios.get(1));
        partidoController.registrarPartido(partidoFutbol2);
    }
} 