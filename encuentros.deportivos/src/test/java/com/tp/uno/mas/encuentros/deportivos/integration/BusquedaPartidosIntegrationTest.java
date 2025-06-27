package com.tp.uno.mas.encuentros.deportivos.integration;

import com.tp.uno.mas.encuentros.deportivos.service.ServicioDePartidos;
import com.tp.uno.mas.encuentros.deportivos.factory.*;
import com.tp.uno.mas.encuentros.deportivos.model.*;
import com.tp.uno.mas.encuentros.deportivos.observer.NotificacionManager;
import com.tp.uno.mas.encuentros.deportivos.strategy.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("Tests de Integración - Búsqueda de Partidos")
class BusquedaPartidosIntegrationTest {

    private ServicioDePartidos servicioDePartidos;
    private List<Usuario> usuarios;
    private GestorPartido gestorPartido;
    
    @BeforeEach
    void setUp() {
        servicioDePartidos = new ServicioDePartidos();
        
        Ubicacion ubicacion1 = new Ubicacion(-34.6037f, -58.3816f, 2.0f);
        Ubicacion ubicacion2 = new Ubicacion(-34.6118f, -58.3960f, 2.0f);
        Ubicacion ubicacion3 = new Ubicacion(-34.5928f, -58.3756f, 2.0f);
        
        usuarios = List.of(
            new Usuario("Juan", "juan@test.com", "123", "Fútbol", "intermedio", ubicacion1, 25, "masculino"),
            new Usuario("María", "maria@test.com", "456", "Tenis", "principiante", ubicacion2, 22, "femenino"),
            new Usuario("Carlos", "carlos@test.com", "789", "Básquet", "avanzado", ubicacion3, 30, "masculino"),
            new Usuario("Ana", "ana@test.com", "101", "Fútbol", "intermedio", ubicacion1, 28, "femenino")
        );
        
        NotificacionManager notificationManager = new NotificacionManager();
        gestorPartido = new GestorPartido(notificationManager);
        crearPartidosDeEjemplo();
    }

    @Test
    @DisplayName("Búsqueda por cercanía filtra correctamente por distancia")
    void testBusquedaPorCercania() {
        Usuario juan = usuarios.get(0);
        List<Partido> partidosCercanos = servicioDePartidos.buscarPartidosCercanos(juan, 5.0);
        
        assertNotNull(partidosCercanos);
        assertFalse(partidosCercanos.isEmpty());
        
        for (Partido partido : partidosCercanos) {
            double distancia = servicioDePartidos.getBuscadorPartidos().calcularDistanciaAlPartido(juan, partido);
            assertTrue(distancia <= 5.0, "El partido debe estar dentro del radio de 5km, pero está a " + distancia + "km");
        }
    }

    @Test
    @DisplayName("Criterios de búsqueda complejos funcionan correctamente")
    void testCriteriosComplejos() {
        Usuario ana = usuarios.get(3);
        List<Partido> resultados = servicioDePartidos.getBuscadorPartidos().buscarPartidosConCriteriosMixtos(
            servicioDePartidos.getPartidosDisponibles(), ana, 1); // Pedir solo 1 criterio
        
        assertNotNull(resultados);
        assertFalse(resultados.isEmpty(), "La búsqueda mixta debería encontrar partidos para Ana.");
    }

    @Test
    @DisplayName("Búsqueda por historial encuentra partidos con jugadores conocidos")
    void testBusquedaPorHistorial() {
        Usuario userA = usuarios.get(0);
        Usuario userB = usuarios.get(1);
        Usuario userC = usuarios.get(2);

        Partido partidoHistorial = gestorPartido.crearPartido(new TenisFactory(), "2024-01-01 10:00", userA.getUbicacion(), userA);
        gestorPartido.agregarJugador(partidoHistorial, userB);
        
        gestorPartido.confirmarPartido(partidoHistorial);
        gestorPartido.iniciarPartido(partidoHistorial);
        gestorPartido.finalizarPartido(partidoHistorial);

        Partido partidoConConocido = gestorPartido.crearPartido(new FutbolFactory(), "2024-12-30 18:00", userB.getUbicacion(), userB);
        Partido partidoSinConocido = gestorPartido.crearPartido(new FutbolFactory(), "2024-12-30 19:00", userC.getUbicacion(), userC);
        
        List<Partido> partidosDisponibles = List.of(partidoConConocido, partidoSinConocido);
        
        BusquedaPorHistorial busquedaHistorial = new BusquedaPorHistorial();
        List<Partido> resultados = busquedaHistorial.buscar(partidosDisponibles, userA);

        assertNotNull(resultados);
        assertEquals(1, resultados.size());
        assertTrue(resultados.contains(partidoConConocido));
    }

    private void crearPartidosDeEjemplo() {
        Ubicacion ubicacionPalermo = new Ubicacion(-34.5755f, -58.4330f, 5.0f);
        Ubicacion ubicacionCentro = new Ubicacion(-34.6118f, -58.3960f, 5.0f);
        Ubicacion ubicacionRecoleta = new Ubicacion(-34.5875f, -58.3974f, 5.0f);

        PartidoFactory futbolFactory = new FutbolFactory();
        Partido partidoFutbol = gestorPartido.crearPartido(futbolFactory, "2024-12-25 16:00", ubicacionPalermo, usuarios.get(0));
        partidoFutbol.aplicarCriterios(new CriteriosPartido("intermedio", null, 0, 0, null, 0));
        servicioDePartidos.registrarPartido(partidoFutbol);

        PartidoFactory tenisFactory = new TenisFactory();
        Partido partidoTenis = gestorPartido.crearPartido(tenisFactory, "2024-12-26 10:00", ubicacionRecoleta, usuarios.get(1));
        gestorPartido.agregarJugador(partidoTenis, usuarios.get(2));
        servicioDePartidos.registrarPartido(partidoTenis);

        PartidoFactory basquetFactory = new BasquetFactory();
        Partido partidoBasquet = gestorPartido.crearPartido(basquetFactory, "2024-12-27 19:00", ubicacionCentro, usuarios.get(2));
        servicioDePartidos.registrarPartido(partidoBasquet);
    }
} 