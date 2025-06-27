package com.tp.uno.mas.encuentros.deportivos.integration;

import com.tp.uno.mas.encuentros.deportivos.factory.TenisFactory;
import com.tp.uno.mas.encuentros.deportivos.model.*;
import com.tp.uno.mas.encuentros.deportivos.observer.NotificacionManager;
import com.tp.uno.mas.encuentros.deportivos.state.Confirmado;
import com.tp.uno.mas.encuentros.deportivos.state.EnJuego;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

@DisplayName("Tests para el Servicio Programador")
class ServicioProgramadorTest {

    private ServicioProgramador servicioProgramador;
    private GestorPartido gestorPartido;
    private Usuario organizador;

    @BeforeEach
    void setUp() {
        gestorPartido = new GestorPartido(new NotificacionManager());
        servicioProgramador = new ServicioProgramador(gestorPartido, new VerificadorHorarios());
        organizador = new Usuario("Test", "test@test.com", "pass", "tenis", "intermedio", new Ubicacion(), 25, "mixto");
    }

    @Test
    @DisplayName("El programador inicia un partido confirmado cuya hora ha llegado")
    void testIniciaPartidoCuandoEsLaHora() {
        // ARRANGE
        String fechaPasada = LocalDateTime.now().minusMinutes(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        Partido partido = gestorPartido.crearPartido(new TenisFactory(), fechaPasada, new Ubicacion(), organizador);
        
        // Completar el partido para que pueda ser iniciado
        Usuario jugador2 = new Usuario("Jugador2", "j2@test.com", "pass", "tenis", "intermedio", new Ubicacion(), 28, "mixto");
        gestorPartido.agregarJugador(partido, jugador2);
        
        partido.cambiarEstado(new Confirmado()); // Simular que está confirmado

        // ACT
        servicioProgramador.verificarYActualizarPartidos(List.of(partido));

        // ASSERT
        assertTrue(partido.getEstadoActual() instanceof EnJuego, "El partido debería haber pasado a estado 'EnJuego'");
    }

    @Test
    @DisplayName("El programador NO inicia un partido cuya hora no ha llegado")
    void testNoIniciaPartidoCuandoNoEsLaHora() {
        // ARRANGE
        String fechaFutura = LocalDateTime.now().plusHours(1).format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
        Partido partido = gestorPartido.crearPartido(new TenisFactory(), fechaFutura, new Ubicacion(), organizador);
        partido.cambiarEstado(new Confirmado());

        // ACT
        servicioProgramador.verificarYActualizarPartidos(List.of(partido));

        // ASSERT
        assertTrue(partido.getEstadoActual() instanceof Confirmado, "El partido debería permanecer en estado 'Confirmado'");
    }
} 