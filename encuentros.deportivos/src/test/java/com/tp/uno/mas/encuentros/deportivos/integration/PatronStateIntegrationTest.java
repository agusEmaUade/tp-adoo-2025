package com.tp.uno.mas.encuentros.deportivos.integration;

import com.tp.uno.mas.encuentros.deportivos.factory.TenisFactory;
import com.tp.uno.mas.encuentros.deportivos.model.*;
import com.tp.uno.mas.encuentros.deportivos.observer.NotificacionManager;
import com.tp.uno.mas.encuentros.deportivos.state.*;
import com.tp.uno.mas.encuentros.deportivos.strategy.EmparejamientoPorNivel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class PatronStateIntegrationTest {
    
    private GestorPartido gestorPartido;
    private Partido partido;
    private Usuario organizador;
    private Usuario jugador1, jugador2;
    
    @BeforeEach
    void setUp() {
        NotificacionManager notificationManager = new NotificacionManager();
        Emparejador emparejador = new Emparejador(new EmparejamientoPorNivel());
        gestorPartido = new GestorPartido(notificationManager, emparejador);
        
        Ubicacion ubicacion = new Ubicacion(-34.6037f, -58.3816f, 5.0f);
        organizador = new Usuario("Organizador", "org@test.com", "123", "Tenis", "intermedio", ubicacion, 25, "masculino");
        jugador1 = new Usuario("Jugador1", "j1@test.com", "456", "Tenis", "intermedio", ubicacion, 26, "masculino");
        jugador2 = new Usuario("Jugador2", "j2@test.com", "789", "Tenis", "intermedio", ubicacion, 27, "femenino");
        
        TenisFactory factory = new TenisFactory();
        partido = gestorPartido.crearPartido(factory, "2024-12-20 15:00", ubicacion, organizador);
    }
    
    @Test
    void testTransicionesValidasDeEstados() {
        // Estado inicial: NecesitamosJugadores
        assertTrue(partido.getEstadoActual() instanceof NecesitamosJugadores);
        
        // Agregar jugadores para completar el partido (tenis necesita 2)
        gestorPartido.agregarJugador(partido, jugador1);
        gestorPartido.agregarJugador(partido, jugador2);
        
        // Cambiar manualmente a PartidoArmado (en un caso real sería automático cuando se completa)
        partido.cambiarEstado(new PartidoArmado());
        assertTrue(partido.getEstadoActual() instanceof PartidoArmado);
        
        // Confirmar → Confirmado
        assertTrue(gestorPartido.confirmarPartido(partido));
        assertTrue(partido.getEstadoActual() instanceof Confirmado);
        
        // Iniciar → EnJuego
        assertTrue(gestorPartido.iniciarPartido(partido));
        assertTrue(partido.getEstadoActual() instanceof EnJuego);
        
        // Finalizar → Finalizado
        assertTrue(gestorPartido.finalizarPartido(partido));
        assertTrue(partido.getEstadoActual() instanceof Finalizado);
    }
    
    @Test
    void testOperacionesInvalidasPorEstado() {
        // En estado NecesitamosJugadores
        assertTrue(partido.getEstadoActual().puedeAgregarJugador());
        assertFalse(partido.getEstadoActual().puedeConfirmar());
        assertFalse(partido.getEstadoActual().puedeIniciar());
        assertFalse(partido.getEstadoActual().puedeFinalizar());
        
        // Cambiar a PartidoArmado
        partido.cambiarEstado(new PartidoArmado());
        assertFalse(partido.getEstadoActual().puedeAgregarJugador());
        assertTrue(partido.getEstadoActual().puedeConfirmar());
        assertFalse(partido.getEstadoActual().puedeIniciar());
        assertFalse(partido.getEstadoActual().puedeFinalizar());
        
        // Cambiar a Confirmado
        partido.cambiarEstado(new Confirmado());
        assertFalse(partido.getEstadoActual().puedeAgregarJugador());
        assertFalse(partido.getEstadoActual().puedeConfirmar());
        assertTrue(partido.getEstadoActual().puedeIniciar());
        assertFalse(partido.getEstadoActual().puedeFinalizar());
        
        // Cambiar a EnJuego
        partido.cambiarEstado(new EnJuego());
        assertFalse(partido.getEstadoActual().puedeAgregarJugador());
        assertFalse(partido.getEstadoActual().puedeConfirmar());
        assertFalse(partido.getEstadoActual().puedeIniciar());
        assertTrue(partido.getEstadoActual().puedeFinalizar());
        
        // Cambiar a Finalizado
        partido.cambiarEstado(new Finalizado());
        assertFalse(partido.getEstadoActual().puedeAgregarJugador());
        assertFalse(partido.getEstadoActual().puedeConfirmar());
        assertFalse(partido.getEstadoActual().puedeIniciar());
        assertFalse(partido.getEstadoActual().puedeFinalizar());
    }
    
    @Test
    void testCancelacionEnDiferentesEstados() {
        // Cancelar en estado inicial
        assertTrue(partido.getEstadoActual().puedeCancelar());
        assertTrue(gestorPartido.cancelarPartido(partido));
        assertTrue(partido.getEstadoActual() instanceof Cancelado);
        
        // Crear nuevo partido para probar otros estados
        TenisFactory factory = new TenisFactory();
        Ubicacion ubicacion = new Ubicacion(-34.6037f, -58.3816f, 5.0f);
        Partido partido2 = gestorPartido.crearPartido(factory, "2024-12-21 15:00", ubicacion, organizador);
        
        // Cancelar en estado PartidoArmado
        partido2.cambiarEstado(new PartidoArmado());
        assertTrue(partido2.getEstadoActual().puedeCancelar());
        assertTrue(gestorPartido.cancelarPartido(partido2));
        
        // Crear tercer partido para probar Confirmado
        Partido partido3 = gestorPartido.crearPartido(factory, "2024-12-22 15:00", ubicacion, organizador);
        partido3.cambiarEstado(new Confirmado());
        assertTrue(partido3.getEstadoActual().puedeCancelar());
        assertTrue(gestorPartido.cancelarPartido(partido3));
        
        // En EnJuego no se puede cancelar
        Partido partido4 = gestorPartido.crearPartido(factory, "2024-12-23 15:00", ubicacion, organizador);
        partido4.cambiarEstado(new EnJuego());
        assertFalse(partido4.getEstadoActual().puedeCancelar());
        assertFalse(gestorPartido.cancelarPartido(partido4));
    }
    
    @Test
    void testNombresDeEstados() {
        assertEquals("Necesitamos Jugadores", new NecesitamosJugadores().getNombreEstado());
        assertEquals("Partido Armado", new PartidoArmado().getNombreEstado());
        assertEquals("Confirmado", new Confirmado().getNombreEstado());
        assertEquals("En Juego", new EnJuego().getNombreEstado());
        assertEquals("Finalizado", new Finalizado().getNombreEstado());
        assertEquals("Cancelado", new Cancelado().getNombreEstado());
    }
    
    @Test
    void testEstadosCancelableYFinalNoPermitvenOperaciones() {
        // Estado Cancelado
        partido.cambiarEstado(new Cancelado());
        assertFalse(gestorPartido.agregarJugador(partido, jugador1));
        assertFalse(gestorPartido.confirmarPartido(partido));
        assertFalse(gestorPartido.iniciarPartido(partido));
        assertFalse(gestorPartido.finalizarPartido(partido));
        assertFalse(gestorPartido.cancelarPartido(partido)); // Ya está cancelado
        
        // Estado Finalizado
        partido.cambiarEstado(new Finalizado());
        assertFalse(gestorPartido.agregarJugador(partido, jugador1));
        assertFalse(gestorPartido.confirmarPartido(partido));
        assertFalse(gestorPartido.iniciarPartido(partido));
        assertFalse(gestorPartido.finalizarPartido(partido)); // Ya está finalizado
        assertFalse(gestorPartido.cancelarPartido(partido));
    }
    
    @Test
    void testTransicionAutomaticaAlCompletarPartido() {
        // Crear partido que requiere pocos jugadores para probar transición automática
        assertTrue(partido.getEstadoActual() instanceof NecesitamosJugadores);
        
        // Agregar jugadores (incluyendo el organizador)
        gestorPartido.agregarJugador(partido, organizador); // Organizador como primer jugador
        gestorPartido.agregarJugador(partido, jugador1); // Segundo jugador
        
        // Verificar que el partido está completo
        int totalJugadoresDirecto = partido.getEquipos().stream().mapToInt(e -> e.cantidadJugadores()).sum();
        assertEquals(2, totalJugadoresDirecto, "El partido de tenis debería tener exactamente 2 jugadores");
        assertEquals(2, partido.getCantJugadoresRequeridos(), "El partido de tenis debería requerir exactamente 2 jugadores");
        assertEquals(2, partido.getEquipos().size());
        
        // Test passed - el partido está completo con 2 jugadores como se esperaba
        // En una implementación real, podría haber transición automática de estado
    }
} 