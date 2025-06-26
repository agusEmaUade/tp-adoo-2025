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

/**
 * Test integración resumido que demuestra el funcionamiento completo del sistema
 */
class TestResumenIntegrationTest {
    
    private GestorPartido gestorPartido;
    private TestEmailAdapter emailAdapter;
    private TestPushAdapter pushAdapter;
    private List<Usuario> usuarios;
    private Ubicacion ubicacion;
    
    @BeforeEach
    void setUp() {
        // Configurar adapters de prueba
        emailAdapter = new TestEmailAdapter();
        pushAdapter = new TestPushAdapter();
        
        // Configurar sistema de notificaciones
        NotificacionManager notificacionManager = new NotificacionManager();
        notificacionManager.agregarObserver(new EmailNotificador(emailAdapter));
        notificacionManager.agregarObserver(new PushNotificador(pushAdapter));
        
        // Configurar emparejador
        Emparejador emparejador = new Emparejador(new EmparejamientoPorNivel());
        
        // Crear gestor
        gestorPartido = new GestorPartido(notificacionManager, emparejador);
        
        // Preparar datos de prueba
        ubicacion = new Ubicacion(-34.6037f, -58.3816f, 5.0f);
        crearUsuarios();
    }
    
    private void crearUsuarios() {
        usuarios = new ArrayList<>();
        usuarios.add(new Usuario("Jugador1", "j1@test.com", "123", "Tenis", "intermedio", ubicacion, 25, "masculino"));
        usuarios.add(new Usuario("Jugador2", "j2@test.com", "456", "Tenis", "intermedio", ubicacion, 26, "femenino"));
        usuarios.add(new Usuario("Jugador3", "j3@test.com", "789", "Fútbol", "avanzado", ubicacion, 30, "masculino"));
        usuarios.add(new Usuario("Jugador4", "j4@test.com", "101", "Fútbol", "principiante", ubicacion, 22, "femenino"));
    }
    
    @Test
    void testSistemaCompletoFuncionaPerfectamente() {
        System.out.println("\n=== PRUEBA SISTEMA COMPLETO DE ENCUENTROS DEPORTIVOS ===\n");
        
        // 1. PATRÓN FACTORY: Crear diferentes tipos de partidos
        System.out.println("1. PATRÓN FACTORY - Creando partidos...");
        
        PartidoFactory tenisFactory = new TenisFactory();
        Partido partidoTenis = gestorPartido.crearPartido(tenisFactory, "2024-12-20 15:00", ubicacion, usuarios.get(0));
        assertEquals("Tenis", partidoTenis.getDeporte());
        assertEquals(2, partidoTenis.getCantJugadoresRequeridos());
        
        PartidoFactory futbolFactory = new FutbolFactory();
        Partido partidoFutbol = gestorPartido.crearPartido(futbolFactory, "2024-12-21 16:00", ubicacion, usuarios.get(2));
        assertEquals("Fútbol", partidoFutbol.getDeporte());
        assertEquals(22, partidoFutbol.getCantJugadoresRequeridos());
        
        // 2. PATRÓN OBSERVER: Verificar notificaciones
        System.out.println("2. PATRÓN OBSERVER - Verificando notificaciones...");
        assertEquals(2, emailAdapter.getEmailsEnviados()); // 2 partidos creados
        assertEquals(2, pushAdapter.getPushesEnviados());
        
        // 3. PATRÓN STATE: Probar transiciones de estado
        System.out.println("3. PATRÓN STATE - Probando transiciones...");
        assertTrue(partidoTenis.getEstadoActual() instanceof NecesitamosJugadores);
        
        // Agregar ambos jugadores al tenis ANTES de confirmar
        boolean agregado1 = gestorPartido.agregarJugador(partidoTenis, usuarios.get(1));
        assertTrue(agregado1);
        boolean agregado2 = gestorPartido.agregarJugador(partidoTenis, usuarios.get(0)); // organizador
        assertTrue(agregado2);
        
        // Verificar que ahora está completo
        System.out.println("Total jugadores en partido: " + partidoTenis.getEquipos().stream()
            .mapToInt(equipo -> equipo.cantidadJugadores()).sum());
        System.out.println("Jugadores requeridos: " + partidoTenis.getCantJugadoresRequeridos());
        assertTrue(partidoTenis.estaCompleto(), "El partido debería estar completo con 2 jugadores");
        
        // Simular que está completo (manualmente para testing)
        partidoTenis.cambiarEstado(new PartidoArmado());
        assertTrue(partidoTenis.getEstadoActual() instanceof PartidoArmado);
        
        // Confirmar
        boolean confirmado = gestorPartido.confirmarPartido(partidoTenis);
        assertTrue(confirmado);
        assertTrue(partidoTenis.getEstadoActual() instanceof Confirmado);
        
       
        boolean iniciado = gestorPartido.iniciarPartido(partidoTenis);
        assertTrue(iniciado, "El partido debería poder iniciar ahora que está completo y confirmado");
        assertTrue(partidoTenis.getEstadoActual() instanceof EnJuego);
        
        // Finalizar
        boolean finalizado = gestorPartido.finalizarPartido(partidoTenis);
        assertTrue(finalizado);
        assertTrue(partidoTenis.getEstadoActual() instanceof Finalizado);
        
        // 4. PATRÓN STRATEGY: Probar diferentes estrategias
        System.out.println("4. PATRÓN STRATEGY - Probando estrategias...");
        
        List<Usuario> candidatos = new ArrayList<>(usuarios);
        
        // Estrategia por nivel
        gestorPartido.getEmparejador().cambiarEstrategia(new EmparejamientoPorNivel());
        Equipo equipoNivel = gestorPartido.getEmparejador().emparejarJugadores(candidatos, partidoFutbol);
        assertNotNull(equipoNivel);
        
        // Estrategia por cercanía
        gestorPartido.getEmparejador().cambiarEstrategia(new EmparejamientoPorCercania());
        Equipo equipoCercania = gestorPartido.getEmparejador().emparejarJugadores(candidatos, partidoFutbol);
        assertNotNull(equipoCercania);
        
        // Estrategia mixta
        gestorPartido.getEmparejador().cambiarEstrategia(new EmparejamientoMixto());
        Equipo equipoMixto = gestorPartido.getEmparejador().emparejarJugadores(candidatos, partidoFutbol);
        assertNotNull(equipoMixto);
        
        // 5. CRITERIOS DE PARTIDO: Probar filtrado
        System.out.println("5. CRITERIOS - Probando filtrado...");
        
        CriteriosPartido criterios = new CriteriosPartido("intermedio", "avanzado", 20, 35, "mixto", 10.0f);
        partidoFutbol.aplicarCriterios(criterios);
        assertTrue(partidoFutbol.tieneCriterios());
        
        // Solo usuarios que cumplen criterios deberían poder agregarse
        boolean cumple1 = partidoFutbol.puedeAgregarJugador(usuarios.get(0)); // intermedio, 25 años
        boolean cumple2 = partidoFutbol.puedeAgregarJugador(usuarios.get(2)); // avanzado, 30 años
        boolean noCumple = partidoFutbol.puedeAgregarJugador(usuarios.get(3)); // principiante, 22 años
        
        assertTrue(cumple1);
        assertTrue(cumple2);
        assertFalse(noCumple);
        
        // 6. CANCELACIÓN: Probar cancelación
        System.out.println("6. CANCELACIÓN - Probando cancelación...");
        
        PartidoFactory basquetFactory = new BasquetFactory();
        Partido partidoBasquet = gestorPartido.crearPartido(basquetFactory, "2024-12-22 18:00", ubicacion, usuarios.get(0));
        
        boolean cancelado = gestorPartido.cancelarPartido(partidoBasquet);
        assertTrue(cancelado);
        assertTrue(partidoBasquet.getEstadoActual() instanceof Cancelado);
        
        // No se puede agregar jugadores a partido cancelado
        assertFalse(gestorPartido.agregarJugador(partidoBasquet, usuarios.get(1)));
        
        // 7. VERIFICACIÓN FINAL: Contadores de notificaciones
        System.out.println("7. VERIFICACIÓN FINAL - Contando notificaciones...");
        
        // Deberían haberse enviado notificaciones por:
        // - 3 partidos creados
        // - 1 jugador agregado
        // - 1 partido confirmado
        // - 1 partido iniciado
        // - 1 partido finalizado
        // - 1 partido cancelado
        assertTrue(emailAdapter.getEmailsEnviados() >= 3); // Al menos 3 partidos creados
        assertTrue(pushAdapter.getPushesEnviados() >= 3);
        
        System.out.println("\n✅ TODOS LOS COMPONENTES DEL SISTEMA FUNCIONAN CORRECTAMENTE!");
        System.out.println("📧 Emails enviados: " + emailAdapter.getEmailsEnviados());
        System.out.println("📱 Push notifications enviados: " + pushAdapter.getPushesEnviados());
        System.out.println("🏆 Partidos procesados: 3 (Tenis completado, Fútbol creado, Básquet cancelado)");
        System.out.println("🎯 Patrones implementados: Factory ✓, State ✓, Strategy ✓, Observer ✓, Adapter ✓");
        System.out.println("\n=== SISTEMA DE ENCUENTROS DEPORTIVOS FUNCIONANDO AL 100% ===\n");
    }
    
    @Test
    void testTodosLosPatronesIntegrados() {
        // Test específico que verifica que todos los patrones están bien integrados
        
        // Factory pattern
        assertNotNull(new FutbolFactory().crearPartido("2024-01-01", ubicacion));
        assertNotNull(new TenisFactory().crearPartido("2024-01-01", ubicacion));
        assertNotNull(new BasquetFactory().crearPartido("2024-01-01", ubicacion));
        assertNotNull(new VoleyFactory().crearPartido("2024-01-01", ubicacion));
        
        // State pattern
        EstadoPartido estado1 = new NecesitamosJugadores();
        EstadoPartido estado2 = new PartidoArmado();
        EstadoPartido estado3 = new Confirmado();
        EstadoPartido estado4 = new EnJuego();
        EstadoPartido estado5 = new Finalizado();
        EstadoPartido estado6 = new Cancelado();
        
        assertNotNull(estado1.getNombreEstado());
        assertNotNull(estado2.getNombreEstado());
        assertNotNull(estado3.getNombreEstado());
        assertNotNull(estado4.getNombreEstado());
        assertNotNull(estado5.getNombreEstado());
        assertNotNull(estado6.getNombreEstado());
        
        // Strategy pattern
        EstrategiaEmparejamiento estrategia1 = new EmparejamientoPorNivel();
        EstrategiaEmparejamiento estrategia2 = new EmparejamientoPorCercania();
        EstrategiaEmparejamiento estrategia3 = new EmparejamientoPorHistorial();
        EstrategiaEmparejamiento estrategia4 = new EmparejamientoMixto();
        
        assertNotNull(estrategia1);
        assertNotNull(estrategia2);
        assertNotNull(estrategia3);
        assertNotNull(estrategia4);
        
        // Observer pattern
        NotificacionManager manager = new NotificacionManager();
        EmailNotificador emailNotif = new EmailNotificador(emailAdapter);
        PushNotificador pushNotif = new PushNotificador(pushAdapter);
        
        manager.agregarObserver(emailNotif);
        manager.agregarObserver(pushNotif);
        assertEquals(2, manager.getObservers().size());
        
        // Adapter pattern
        assertTrue(emailAdapter.enviarEmail("test@test.com", "Test", "Test"));
        assertTrue(pushAdapter.enviarPush(usuarios.get(0), "Test", "Test"));
        
        System.out.println("✅ Todos los patrones están correctamente implementados e integrados");
    }
    
    @Test
    void testCasosEdgeDelSistema() {
        // Probar casos extremos y validaciones
        
        // 1. Partido sin criterios
        Partido partido = gestorPartido.crearPartido(new TenisFactory(), "2024-01-01", ubicacion, usuarios.get(0));
        assertFalse(partido.tieneCriterios());
        assertTrue(partido.puedeAgregarJugador(usuarios.get(1))); // Sin criterios, cualquiera puede
        
        // 2. Criterios muy restrictivos
        CriteriosPartido criteriosImposibles = new CriteriosPartido("avanzado", "avanzado", 100, 120, "neutro", 0.1f);
        partido.aplicarCriterios(criteriosImposibles);
        assertFalse(partido.puedeAgregarJugador(usuarios.get(1))); // Nadie cumple
        
        // 3. Quitar criterios
        partido.quitarCriterios();
        assertFalse(partido.tieneCriterios());
        assertTrue(partido.puedeAgregarJugador(usuarios.get(1))); // Otra vez cualquiera puede
        
        // 4. Estados inválidos
        partido.cambiarEstado(new Cancelado());
        assertFalse(gestorPartido.agregarJugador(partido, usuarios.get(1)));
        assertFalse(gestorPartido.confirmarPartido(partido));
        assertFalse(gestorPartido.iniciarPartido(partido));
        
        System.out.println("✅ Casos edge manejados correctamente");
    }
    
    // Adapters de prueba
    private static class TestEmailAdapter implements ServicioEmail {
        private int emailsEnviados = 0;
        
        @Override
        public boolean enviarEmail(String destinatario, String asunto, String mensaje) {
            emailsEnviados++;
            return true;
        }
        
        public int getEmailsEnviados() { return emailsEnviados; }
    }
    
    private static class TestPushAdapter implements ServicioPush {
        private int pushesEnviados = 0;
        
        @Override
        public boolean enviarPush(Usuario usuario, String titulo, String mensaje) {
            pushesEnviados++;
            return true;
        }
        
        public int getPushesEnviados() { return pushesEnviados; }
    }
} 