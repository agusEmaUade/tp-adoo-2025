package com.tp.uno.mas.encuentros.deportivos.integration;

import com.tp.uno.mas.encuentros.deportivos.adapter.*;
import com.tp.uno.mas.encuentros.deportivos.factory.FutbolFactory;
import com.tp.uno.mas.encuentros.deportivos.model.*;
import com.tp.uno.mas.encuentros.deportivos.observer.*;
import com.tp.uno.mas.encuentros.deportivos.strategy.EmparejamientoPorNivel;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.List;

class PatronObserverIntegrationTest {
    
    private NotificacionManager notificacionManager;
    private TestEmailAdapter emailAdapter;
    private TestPushAdapter pushAdapter;
    private GestorPartido gestorPartido;
    private Usuario organizador;
    private Ubicacion ubicacion;
    
    @BeforeEach
    void setUp() {
        // Configurar adapters de prueba
        emailAdapter = new TestEmailAdapter();
        pushAdapter = new TestPushAdapter();
        
        // Configurar sistema de notificaciones
        notificacionManager = new NotificacionManager();
        
        // Configurar gestor
        Emparejador emparejador = new Emparejador(new EmparejamientoPorNivel());
        gestorPartido = new GestorPartido(notificacionManager, emparejador);
        
        // Datos de prueba
        ubicacion = new Ubicacion(-34.6037f, -58.3816f, 5.0f);
        organizador = new Usuario("Organizador", "org@test.com", "123", "Fútbol", "intermedio", ubicacion, 25, "masculino");
    }
    
    @Test
    void testSistemaNotificacionesSinObservers() {
        // Sin observers registrados, no deberían ocurrir errores
        FutbolFactory factory = new FutbolFactory();
        Partido partido = gestorPartido.crearPartido(factory, "2024-12-20 15:00", ubicacion, organizador);
        
        assertNotNull(partido);
        assertEquals(0, emailAdapter.getEmailsEnviados());
        assertEquals(0, pushAdapter.getPushesEnviados());
    }
    
    @Test
    void testRegistroYEliminacionDeObservers() {
        EmailNotificador emailNotificador = new EmailNotificador(emailAdapter);
        PushNotificador pushNotificador = new PushNotificador(pushAdapter);
        
        // Registrar observers
        notificacionManager.agregarObserver(emailNotificador);
        notificacionManager.agregarObserver(pushNotificador);
        assertEquals(2, notificacionManager.getObservers().size());
        
        // Crear partido para verificar notificaciones
        FutbolFactory factory = new FutbolFactory();
        Partido partido = gestorPartido.crearPartido(factory, "2024-12-20 15:00", ubicacion, organizador);
        
        assertEquals(1, emailAdapter.getEmailsEnviados());
        assertEquals(1, pushAdapter.getPushesEnviados());
        
        // Eliminar un observer
        notificacionManager.eliminarObserver(emailNotificador);
        assertEquals(1, notificacionManager.getObservers().size());
        
        // Agregar jugador para verificar que solo push se envía
        Usuario jugador = new Usuario("Jugador", "jugador@test.com", "456", "Fútbol", "intermedio", ubicacion, 26, "masculino");
        gestorPartido.agregarJugador(partido, jugador);
        
        // Email no debería incrementar, push sí
        assertEquals(1, emailAdapter.getEmailsEnviados());
        assertTrue(pushAdapter.getPushesEnviados() > 1);
    }
    
    @Test
    void testNotificacionesPorTipoDeEvento() {
        // Registrar observers
        EmailNotificador emailNotificador = new EmailNotificador(emailAdapter);
        PushNotificador pushNotificador = new PushNotificador(pushAdapter);
        notificacionManager.agregarObserver(emailNotificador);
        notificacionManager.agregarObserver(pushNotificador);
        
        FutbolFactory factory = new FutbolFactory();
        Partido partido = gestorPartido.crearPartido(factory, "2024-12-20 15:00", ubicacion, organizador);
        
        // Verificar evento PARTIDO_CREADO
        assertTrue(emailAdapter.getUltimoEmail().contains("Nuevo partido creado"));
        assertTrue(pushAdapter.getUltimoPush().contains("¡Nuevo partido!"));
        
        // Agregar jugador → evento JUGADOR_UNIDO
        Usuario jugador = new Usuario("Jugador", "jugador@test.com", "456", "Fútbol", "intermedio", ubicacion, 26, "masculino");
        gestorPartido.agregarJugador(partido, jugador);
        
        assertTrue(emailAdapter.getUltimoEmail().contains("Nuevo jugador se unió"));
        assertTrue(pushAdapter.getUltimoPush().contains("Nuevo jugador"));
        
        // Confirmar partido → evento PARTIDO_CONFIRMADO
        partido.cambiarEstado(new com.tp.uno.mas.encuentros.deportivos.state.PartidoArmado());
        gestorPartido.confirmarPartido(partido);
        
        assertTrue(emailAdapter.getUltimoEmail().contains("Partido confirmado"));
        assertTrue(pushAdapter.getUltimoPush().contains("¡Partido confirmado!"));
    }
    
    @Test
    void testNotificacionesATodosLosParticipantes() {
        EmailNotificador emailNotificador = new EmailNotificador(emailAdapter);
        notificacionManager.agregarObserver(emailNotificador);
        
        FutbolFactory factory = new FutbolFactory();
        Partido partido = gestorPartido.crearPartido(factory, "2024-12-20 15:00", ubicacion, organizador);
        
        // Agregar múltiples jugadores
        Usuario jugador1 = new Usuario("Jugador1", "j1@test.com", "456", "Fútbol", "intermedio", ubicacion, 26, "masculino");
        Usuario jugador2 = new Usuario("Jugador2", "j2@test.com", "789", "Fútbol", "intermedio", ubicacion, 27, "femenino");
        
        gestorPartido.agregarJugador(partido, jugador1);
        gestorPartido.agregarJugador(partido, jugador2);
        
        // Al confirmar el partido, todos los participantes deberían recibir notificación
        partido.cambiarEstado(new com.tp.uno.mas.encuentros.deportivos.state.PartidoArmado());
        int emailsAntesConfirmar = emailAdapter.getEmailsEnviados();
        
        gestorPartido.confirmarPartido(partido);
        
        // Debería enviar email al organizador + 2 jugadores = 3 emails adicionales
        int emailsDespuesConfirmar = emailAdapter.getEmailsEnviados();
        assertEquals(3, emailsDespuesConfirmar - emailsAntesConfirmar);
    }
    
    @Test
    void testEventsEnCicloCompletoPartido() {
        EmailNotificador emailNotificador = new EmailNotificador(emailAdapter);
        notificacionManager.agregarObserver(emailNotificador);
        
        FutbolFactory factory = new FutbolFactory();
        Partido partido = gestorPartido.crearPartido(factory, "2024-12-20 15:00", ubicacion, organizador);
        
        int emailsCreacion = emailAdapter.getEmailsEnviados();
        assertTrue(emailAdapter.getUltimoEmail().contains("PARTIDO_CREADO"));
        
        // Agregar jugador
        Usuario jugador = new Usuario("Jugador", "jugador@test.com", "456", "Fútbol", "intermedio", ubicacion, 26, "masculino");
        gestorPartido.agregarJugador(partido, jugador);
        
        assertTrue(emailAdapter.getEmailsEnviados() > emailsCreacion);
        assertTrue(emailAdapter.getUltimoEmail().contains("JUGADOR_UNIDO"));
        
        // Agregar más jugadores para completar el partido
        if (!partido.estaCompleto()) {
            for (int i = 2; i < partido.getCantJugadoresRequeridos(); i++) {
                Usuario usuarioExtra = new Usuario("Jugador" + i, "j" + i + "@test.com", "pass", 
                                                 "Fútbol", "intermedio", ubicacion, 25, "mixto");
                gestorPartido.agregarJugador(partido, usuarioExtra);
            }
        }
        
        // Cambiar estados y verificar eventos
        partido.cambiarEstado(new com.tp.uno.mas.encuentros.deportivos.state.PartidoArmado());
        gestorPartido.confirmarPartido(partido);
        assertTrue(emailAdapter.getUltimoEmail().contains("PARTIDO_CONFIRMADO"));
        
        // Solo iniciar si está completo
        if (partido.estaCompleto()) {
            gestorPartido.iniciarPartido(partido);
            assertTrue(emailAdapter.getUltimoEmail().contains("PARTIDO_EN_JUEGO"));
            
            gestorPartido.finalizarPartido(partido);
            assertTrue(emailAdapter.getUltimoEmail().contains("PARTIDO_FINALIZADO"));
        }
    }
    
    @Test
    void testCancelacionGeneraEventoCorrect() {
        EmailNotificador emailNotificador = new EmailNotificador(emailAdapter);
        notificacionManager.agregarObserver(emailNotificador);
        
        FutbolFactory factory = new FutbolFactory();
        Partido partido = gestorPartido.crearPartido(factory, "2024-12-20 15:00", ubicacion, organizador);
        
        gestorPartido.cancelarPartido(partido);
        
        assertTrue(emailAdapter.getUltimoEmail().contains("PARTIDO_CANCELADO"));
    }
    
    @Test
    void testSuscripcionUsuarios() {
        Usuario usuario1 = new Usuario("Usuario1", "u1@test.com", "123", "Fútbol", "intermedio", ubicacion, 25, "masculino");
        Usuario usuario2 = new Usuario("Usuario2", "u2@test.com", "456", "Básquet", "avanzado", ubicacion, 30, "femenino");
        
        // Suscribir usuarios a tipos de notificaciones
        notificacionManager.suscribirUsuario(usuario1, "email");
        notificacionManager.suscribirUsuario(usuario2, "push");
        
        // Este test verifica que el método no falle, la implementación real
        // manejaría las suscripciones específicas por usuario
        assertNotNull(notificacionManager);
    }
    
    @Test
    void testObserverNoFallaConPartidoSinJugadores() {
        EmailNotificador emailNotificador = new EmailNotificador(emailAdapter);
        notificacionManager.agregarObserver(emailNotificador);
        
        FutbolFactory factory = new FutbolFactory();
        Partido partido = gestorPartido.crearPartido(factory, "2024-12-20 15:00", ubicacion, organizador);
        
        // Cancelar partido sin jugadores
        gestorPartido.cancelarPartido(partido);
        
        // Debería funcionar sin errores
        assertTrue(emailAdapter.getEmailsEnviados() > 0);
        assertTrue(emailAdapter.getUltimoEmail().contains("PARTIDO_CANCELADO"));
    }
    
    // Adapters de prueba
    private static class TestEmailAdapter implements ServicioEmail {
        private int emailsEnviados = 0;
        private String ultimoEmail = "";
        private List<String> todosLosEmails = new ArrayList<>();
        
        @Override
        public boolean enviarEmail(String destinatario, String asunto, String mensaje) {
            emailsEnviados++;
            ultimoEmail = asunto + ": " + mensaje;
            todosLosEmails.add(destinatario + " - " + ultimoEmail);
            return true;
        }
        
        public int getEmailsEnviados() { return emailsEnviados; }
        public String getUltimoEmail() { return ultimoEmail; }
        public List<String> getTodosLosEmails() { return new ArrayList<>(todosLosEmails); }
    }
    
    private static class TestPushAdapter implements ServicioPush {
        private int pushesEnviados = 0;
        private String ultimoPush = "";
        private List<String> todosLosPushes = new ArrayList<>();
        
        @Override
        public boolean enviarPush(Usuario usuario, String titulo, String mensaje) {
            pushesEnviados++;
            ultimoPush = titulo + ": " + mensaje;
            todosLosPushes.add(usuario.getNombre() + " - " + ultimoPush);
            return true;
        }
        
        public int getPushesEnviados() { return pushesEnviados; }
        public String getUltimoPush() { return ultimoPush; }
        public List<String> getTodosLosPushes() { return new ArrayList<>(todosLosPushes); }
    }
} 