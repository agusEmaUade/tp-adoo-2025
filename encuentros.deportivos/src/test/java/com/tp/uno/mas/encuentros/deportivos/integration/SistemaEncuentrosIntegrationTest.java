package com.tp.uno.mas.encuentros.deportivos.integration;

import com.tp.uno.mas.encuentros.deportivos.adapter.*;
import com.tp.uno.mas.encuentros.deportivos.factory.*;
import com.tp.uno.mas.encuentros.deportivos.model.*;
import com.tp.uno.mas.encuentros.deportivos.observer.*;
import com.tp.uno.mas.encuentros.deportivos.state.*;
import com.tp.uno.mas.encuentros.deportivos.strategy.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

class SistemaEncuentrosIntegrationTest {
    
    private GestorPartido gestorPartido;
    private NotificacionManager notificacionManager;
    private Emparejador emparejador;
    private List<Usuario> usuarios;
    private Ubicacion ubicacionCancha;
    private TestEmailAdapter testEmailAdapter;
    private TestPushAdapter testPushAdapter;
    
    @BeforeEach
    void setUp() {
        // Configurar sistema de notificaciones con adapters de prueba
        testEmailAdapter = new TestEmailAdapter();
        testPushAdapter = new TestPushAdapter();
        
        notificacionManager = new NotificacionManager();
        notificacionManager.agregarObserver(new EmailNotificador(testEmailAdapter));
        notificacionManager.agregarObserver(new PushNotificador(testPushAdapter));
        
        // Configurar emparejador
        emparejador = new Emparejador(new EmparejamientoPorNivel());
        
        // Crear gestor
        gestorPartido = new GestorPartido(notificacionManager, emparejador);
        
        // Preparar datos de prueba
        crearUsuariosDePrueba();
        ubicacionCancha = new Ubicacion(-34.6037f, -58.3816f, 5.0f);
    }
    
    private void crearUsuariosDePrueba() {
        usuarios = new ArrayList<>();
        
        Ubicacion ubicacion1 = new Ubicacion(-34.6037f, -58.3816f, 2.0f);
        Ubicacion ubicacion2 = new Ubicacion(-34.6118f, -58.3960f, 2.0f);
        
        usuarios.add(new Usuario("Juan", "juan@test.com", "123", "Fútbol", "intermedio", ubicacion1, 25, "masculino"));
        usuarios.add(new Usuario("María", "maria@test.com", "456", "Fútbol", "principiante", ubicacion2, 22, "femenino"));
        usuarios.add(new Usuario("Carlos", "carlos@test.com", "789", "Fútbol", "avanzado", ubicacion1, 30, "masculino"));
        usuarios.add(new Usuario("Ana", "ana@test.com", "101", "Fútbol", "intermedio", ubicacion2, 28, "femenino"));
    }
    
    @Test
    void testCicloCompletoPartidoFutbol() {
        // ARRANGE: Crear partido con Factory
        PartidoFactory futbolFactory = new FutbolFactory();
        Usuario organizador = usuarios.get(0);
        
        // ACT & ASSERT: Crear partido
        Partido partido = gestorPartido.crearPartido(futbolFactory, "2024-12-20 15:00", ubicacionCancha, organizador);
        
        // Verificar creación
        assertNotNull(partido);
        assertEquals("Fútbol", partido.getDeporte());
        assertEquals(22, partido.getCantJugadoresRequeridos());
        assertEquals(90, partido.getDuracion());
        assertTrue(partido.getEstadoActual() instanceof NecesitamosJugadores);
        assertEquals(1, testEmailAdapter.getEmailsEnviados());
        assertEquals(1, testPushAdapter.getPushesEnviados());
        
        // ACT: Aplicar criterios
        CriteriosPartido criterios = new CriteriosPartido("principiante", "avanzado", 18, 40, "mixto", 10.0f);
        partido.aplicarCriterios(criterios);
        assertTrue(partido.tieneCriterios());
        
        // ACT: Agregar jugadores
        int emailsIniciales = testEmailAdapter.getEmailsEnviados();
        for (Usuario usuario : usuarios) {
            boolean agregado = gestorPartido.agregarJugador(partido, usuario);
            assertTrue(agregado, "Debería poder agregar usuario: " + usuario.getNombre());
        }
        
        // Verificar notificaciones por cada jugador agregado
        assertTrue(testEmailAdapter.getEmailsEnviados() > emailsIniciales);
        assertTrue(testPushAdapter.getPushesEnviados() > 1);
        
        // Verificar que el partido sigue necesitando jugadores (solo 4 de 22)
        assertTrue(partido.getEstadoActual() instanceof NecesitamosJugadores);
        assertFalse(partido.estaCompleto());
        
        // ACT: Agregar más jugadores para completar el partido
        if (!partido.estaCompleto()) {
            for (int i = 4; i < partido.getCantJugadoresRequeridos(); i++) {
                Usuario usuarioExtra = new Usuario("Jugador" + i, "jugador" + i + "@test.com", "pass", 
                                                 "Fútbol", "intermedio", ubicacionCancha, 25, "mixto");
                gestorPartido.agregarJugador(partido, usuarioExtra);
            }
        }
        
        // ACT: Simular que se completa el partido manualmente
        partido.cambiarEstado(new PartidoArmado());
        
        // ACT: Confirmar partido
        boolean confirmado = gestorPartido.confirmarPartido(partido);
        assertTrue(confirmado);
        assertTrue(partido.getEstadoActual() instanceof Confirmado);
        
        // ACT: Iniciar partido (solo si está completo)
        if (partido.estaCompleto()) {
            boolean iniciado = gestorPartido.iniciarPartido(partido);
            assertTrue(iniciado);
            assertTrue(partido.getEstadoActual() instanceof EnJuego);
        }
        
        // ACT: Finalizar partido (solo si está en juego)
        if (partido.getEstadoActual() instanceof EnJuego) {
            boolean finalizado = gestorPartido.finalizarPartido(partido);
            assertTrue(finalizado);
            assertTrue(partido.getEstadoActual() instanceof Finalizado);
        }
        
        // Verificar que el estado final no permite más cambios
        assertFalse(partido.getEstadoActual().puedeAgregarJugador());
        assertFalse(partido.getEstadoActual().puedeConfirmar());
        assertFalse(partido.getEstadoActual().puedeIniciar());
    }
    
    @Test
    void testPatronFactoryDiferentesDeportes() {
        Usuario organizador = usuarios.get(0);
        
        // Test Fútbol Factory
        PartidoFactory futbolFactory = new FutbolFactory();
        Partido partidoFutbol = gestorPartido.crearPartido(futbolFactory, "2024-12-20 15:00", ubicacionCancha, organizador);
        assertEquals("Fútbol", partidoFutbol.getDeporte());
        assertEquals(22, partidoFutbol.getCantJugadoresRequeridos());
        assertEquals(2, partidoFutbol.getEquipos().size());
        
        // Test Tenis Factory
        PartidoFactory tenisFactory = new TenisFactory();
        Partido partidoTenis = gestorPartido.crearPartido(tenisFactory, "2024-12-21 10:00", ubicacionCancha, organizador);
        assertEquals("Tenis", partidoTenis.getDeporte());
        assertEquals(2, partidoTenis.getCantJugadoresRequeridos());
        assertEquals(2, partidoTenis.getEquipos().size());
        
        // Test Básquet Factory
        PartidoFactory basquetFactory = new BasquetFactory();
        Partido partidoBasquet = gestorPartido.crearPartido(basquetFactory, "2024-12-22 16:00", ubicacionCancha, organizador);
        assertEquals("Básquet", partidoBasquet.getDeporte());
        assertEquals(10, partidoBasquet.getCantJugadoresRequeridos());
        
        // Verificar que se enviaron notificaciones para cada partido creado
        assertEquals(3, testEmailAdapter.getEmailsEnviados());
        assertEquals(3, testPushAdapter.getPushesEnviados());
    }
    
    @Test
    void testPatronStrategyDiferentesEstrategias() {
        // Crear partido de prueba
        PartidoFactory futbolFactory = new FutbolFactory();
        Partido partido = gestorPartido.crearPartido(futbolFactory, "2024-12-20 15:00", ubicacionCancha, usuarios.get(0));
        
        // Test estrategia por nivel
        emparejador.cambiarEstrategia(new EmparejamientoPorNivel());
        Equipo equipoNivel = emparejador.emparejarJugadores(usuarios, partido);
        assertNotNull(equipoNivel);
        assertTrue(equipoNivel.cantidadJugadores() > 0);
        
        // Test estrategia por cercanía
        emparejador.cambiarEstrategia(new EmparejamientoPorCercania());
        Equipo equipoCercania = emparejador.emparejarJugadores(usuarios, partido);
        assertNotNull(equipoCercania);
        assertTrue(equipoCercania.cantidadJugadores() > 0);
        
        // Test estrategia por historial
        emparejador.cambiarEstrategia(new EmparejamientoPorHistorial());
        Equipo equipoHistorial = emparejador.emparejarJugadores(usuarios, partido);
        assertNotNull(equipoHistorial);
        
        // Test estrategia mixta
        emparejador.cambiarEstrategia(new EmparejamientoMixto());
        Equipo equipoMixto = emparejador.emparejarJugadores(usuarios, partido);
        assertNotNull(equipoMixto);
    }
    
    @Test
    void testPatronStateTransiciones() {
        PartidoFactory tenisFactory = new TenisFactory();
        Partido partido = gestorPartido.crearPartido(tenisFactory, "2024-12-20 15:00", ubicacionCancha, usuarios.get(0));
        
        // Estado inicial: NecesitamosJugadores
        assertTrue(partido.getEstadoActual() instanceof NecesitamosJugadores);
        assertTrue(partido.getEstadoActual().puedeAgregarJugador());
        assertFalse(partido.getEstadoActual().puedeConfirmar());
        
        // Agregar jugadores hasta completar (tenis necesita 2)
        gestorPartido.agregarJugador(partido, usuarios.get(0));
        gestorPartido.agregarJugador(partido, usuarios.get(1));
        
        // Cambiar manualmente a PartidoArmado para simular completado
        partido.cambiarEstado(new PartidoArmado());
        assertTrue(partido.getEstadoActual() instanceof PartidoArmado);
        assertFalse(partido.getEstadoActual().puedeAgregarJugador());
        assertTrue(partido.getEstadoActual().puedeConfirmar());
        
        // Confirmar partido
        gestorPartido.confirmarPartido(partido);
        assertTrue(partido.getEstadoActual() instanceof Confirmado);
        assertTrue(partido.getEstadoActual().puedeIniciar());
        assertFalse(partido.getEstadoActual().puedeConfirmar());
        
        // Iniciar partido
        gestorPartido.iniciarPartido(partido);
        assertTrue(partido.getEstadoActual() instanceof EnJuego);
        assertTrue(partido.getEstadoActual().puedeFinalizar());
        assertFalse(partido.getEstadoActual().puedeIniciar());
        
        // Finalizar partido
        gestorPartido.finalizarPartido(partido);
        assertTrue(partido.getEstadoActual() instanceof Finalizado);
        assertFalse(partido.getEstadoActual().puedeFinalizar());
    }
    
    @Test
    void testSistemaNotificacionesCompleto() {
        // Crear partido
        PartidoFactory futbolFactory = new FutbolFactory();
        Partido partido = gestorPartido.crearPartido(futbolFactory, "2024-12-20 15:00", ubicacionCancha, usuarios.get(0));
        
        int emailsInicial = testEmailAdapter.getEmailsEnviados();
        int pushesInicial = testPushAdapter.getPushesEnviados();
        
        // Agregar jugador
        gestorPartido.agregarJugador(partido, usuarios.get(1));
        
        // Verificar que se enviaron notificaciones
        assertTrue(testEmailAdapter.getEmailsEnviados() > emailsInicial);
        assertTrue(testPushAdapter.getPushesEnviados() > pushesInicial);
        
        // Verificar contenido de notificaciones
        assertTrue(testEmailAdapter.getUltimoEmail().contains("Nuevo jugador se unió"));
        assertTrue(testPushAdapter.getUltimoPush().contains("nuevo jugador"));
    }
    
    @Test
    void testCriteriosPartidoIntegracion() {
        PartidoFactory futbolFactory = new FutbolFactory();
        Partido partido = gestorPartido.crearPartido(futbolFactory, "2024-12-20 15:00", ubicacionCancha, usuarios.get(0));
        
        // Aplicar criterios restrictivos
        CriteriosPartido criteriosRestrictivos = new CriteriosPartido("avanzado", "avanzado", 25, 35, "masculino", 1.0f);
        partido.aplicarCriterios(criteriosRestrictivos);
        
        // Solo Carlos debería cumplir estos criterios
        boolean juanAgregado = gestorPartido.agregarJugador(partido, usuarios.get(0)); // Juan - intermedio
        boolean mariaAgregada = gestorPartido.agregarJugador(partido, usuarios.get(1)); // María - principiante, femenino
        boolean carlosAgregado = gestorPartido.agregarJugador(partido, usuarios.get(2)); // Carlos - avanzado, masculino
        
        assertFalse(juanAgregado, "Juan no debería ser agregado (nivel intermedio)");
        assertFalse(mariaAgregada, "María no debería ser agregada (principiante y femenino)");
        assertTrue(carlosAgregado, "Carlos debería ser agregado (cumple todos los criterios)");
    }
    
    @Test
    void testCancelacionPartido() {
        PartidoFactory futbolFactory = new FutbolFactory();
        Partido partido = gestorPartido.crearPartido(futbolFactory, "2024-12-20 15:00", ubicacionCancha, usuarios.get(0));
        
        // Cancelar partido en estado inicial
        boolean cancelado = gestorPartido.cancelarPartido(partido);
        assertTrue(cancelado);
        assertTrue(partido.getEstadoActual() instanceof Cancelado);
        
        // Verificar que no se pueden hacer más operaciones
        assertFalse(gestorPartido.agregarJugador(partido, usuarios.get(1)));
        assertFalse(gestorPartido.confirmarPartido(partido));
        assertFalse(gestorPartido.iniciarPartido(partido));
    }
    
    // Adapters de prueba para verificar notificaciones
    private static class TestEmailAdapter implements ServicioEmail {
        private int emailsEnviados = 0;
        private String ultimoEmail = "";
        
        @Override
        public boolean enviarEmail(String destinatario, String asunto, String mensaje) {
            emailsEnviados++;
            ultimoEmail = asunto + ": " + mensaje;
            return true;
        }
        
        public int getEmailsEnviados() { return emailsEnviados; }
        public String getUltimoEmail() { return ultimoEmail; }
    }
    
    private static class TestPushAdapter implements ServicioPush {
        private int pushesEnviados = 0;
        private String ultimoPush = "";
        
        @Override
        public boolean enviarPush(Usuario usuario, String titulo, String mensaje) {
            pushesEnviados++;
            ultimoPush = titulo + ": " + mensaje;
            return true;
        }
        
        public int getPushesEnviados() { return pushesEnviados; }
        public String getUltimoPush() { return ultimoPush; }
    }
} 