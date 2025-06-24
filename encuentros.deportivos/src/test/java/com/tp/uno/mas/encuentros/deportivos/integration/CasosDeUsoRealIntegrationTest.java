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
 * Tests de integración que simulan casos de uso reales del sistema
 */
class CasosDeUsoRealIntegrationTest {
    
    private GestorPartido gestorPartido;
    private List<Usuario> usuariosDisponibles;
    private Ubicacion ubicacionPalermo;
    private Ubicacion ubicacionVillaCrespo;
    
    @BeforeEach
    void setUp() {
        configurarSistema();
        crearUsuariosDePrueba();
    }
    
    private void configurarSistema() {
        // Configurar sistema completo con todos los componentes
        TestEmailAdapter emailAdapter = new TestEmailAdapter();
        TestPushAdapter pushAdapter = new TestPushAdapter();
        
        NotificacionManager notificacionManager = new NotificacionManager();
        notificacionManager.agregarObserver(new EmailNotificador(emailAdapter));
        notificacionManager.agregarObserver(new PushNotificador(pushAdapter));
        
        Emparejador emparejador = new Emparejador(new EmparejamientoPorNivel());
        gestorPartido = new GestorPartido(notificacionManager, emparejador);
        
        ubicacionPalermo = new Ubicacion(-34.5755f, -58.4000f, 3.0f);
        ubicacionVillaCrespo = new Ubicacion(-34.5998f, -58.4314f, 2.0f);
    }
    
    private void crearUsuariosDePrueba() {
        usuariosDisponibles = new ArrayList<>();
        
        // Usuarios en Palermo
        usuariosDisponibles.add(new Usuario("Ana García", "ana.garcia@email.com", "123", "Fútbol", "intermedio", ubicacionPalermo, 25, "femenino"));
        usuariosDisponibles.add(new Usuario("Carlos López", "carlos.lopez@email.com", "456", "Fútbol", "avanzado", ubicacionPalermo, 30, "masculino"));
        usuariosDisponibles.add(new Usuario("María Rodriguez", "maria.rodriguez@email.com", "789", "Fútbol", "principiante", ubicacionPalermo, 22, "femenino"));
        
        // Usuarios en Villa Crespo
        usuariosDisponibles.add(new Usuario("Juan Pérez", "juan.perez@email.com", "101", "Fútbol", "intermedio", ubicacionVillaCrespo, 28, "masculino"));
        usuariosDisponibles.add(new Usuario("Laura Martín", "laura.martin@email.com", "112", "Fútbol", "avanzado", ubicacionVillaCrespo, 26, "femenino"));
        
        // Usuarios de tenis
        usuariosDisponibles.add(new Usuario("Roberto Silva", "roberto.silva@email.com", "131", "Tenis", "intermedio", ubicacionPalermo, 35, "masculino"));
        usuariosDisponibles.add(new Usuario("Carmen Díaz", "carmen.diaz@email.com", "141", "Tenis", "avanzado", ubicacionPalermo, 29, "femenino"));
        
        // Usuarios de básquet
        usuariosDisponibles.add(new Usuario("Diego Morales", "diego.morales@email.com", "151", "Básquet", "intermedio", ubicacionVillaCrespo, 32, "masculino"));
        usuariosDisponibles.add(new Usuario("Sofía Ruiz", "sofia.ruiz@email.com", "161", "Básquet", "principiante", ubicacionVillaCrespo, 24, "femenino"));
    }
    
    @Test
    void testCasoDeUso_PartidoFutbolCompletoPalermo() {
        // ESCENARIO: Ana organiza un partido de fútbol en Palermo
        Usuario organizadora = usuariosDisponibles.get(0); // Ana
        PartidoFactory futbolFactory = new FutbolFactory();
        
        // 1. Crear partido con criterios específicos
        Partido partido = gestorPartido.crearPartido(futbolFactory, "2024-12-25 16:00", ubicacionPalermo, organizadora);
        
        // 2. Aplicar criterios: solo nivel intermedio-avanzado, mixto, radio 5km
        CriteriosPartido criterios = new CriteriosPartido("intermedio", "avanzado", 20, 40, "mixto", 5.0f);
        partido.aplicarCriterios(criterios);
        
        // 3. Intentar agregar usuarios (solo algunos deberían cumplir criterios)
        List<Usuario> jugadoresAgregados = new ArrayList<>();
        for (Usuario usuario : usuariosDisponibles) {
            if (gestorPartido.agregarJugador(partido, usuario)) {
                jugadoresAgregados.add(usuario);
            }
        }
        
        // 4. Verificar que solo se agregaron usuarios que cumplen criterios
        assertTrue(jugadoresAgregados.size() > 0);
        for (Usuario jugador : jugadoresAgregados) {
            assertTrue(jugador.getNivel().equals("intermedio") || jugador.getNivel().equals("avanzado"));
            assertTrue(jugador.getEdad() >= 20 && jugador.getEdad() <= 40);
        }
        
        // 5. Crear usuarios adicionales que cumplan los criterios para completar el partido
        if (!partido.estaCompleto()) {
            for (int i = jugadoresAgregados.size(); i < partido.getCantJugadoresRequeridos(); i++) {
                Usuario usuarioExtra = new Usuario("Jugador" + i, "jugador" + i + "@test.com", "pass", 
                                                 "Fútbol", "intermedio", ubicacionPalermo, 25, "mixto");
                if (gestorPartido.agregarJugador(partido, usuarioExtra)) {
                    jugadoresAgregados.add(usuarioExtra);
                }
            }
        }
        
        // 6. Completar ciclo del partido
        partido.cambiarEstado(new PartidoArmado());
        assertTrue(gestorPartido.confirmarPartido(partido));
        assertTrue(gestorPartido.iniciarPartido(partido));
        assertTrue(gestorPartido.finalizarPartido(partido));
        
        // 7. Verificar estado final
        assertTrue(partido.getEstadoActual() instanceof Finalizado);
        assertEquals(2, partido.getEquipos().size()); // Fútbol tiene 2 equipos
    }
    
    @Test
    void testCasoDeUso_PartidoTenisConEmparejamientoPorCercania() {
        // ESCENARIO: Roberto organiza un partido de tenis y quiere jugadores cercanos
        Usuario organizador = usuariosDisponibles.get(5); // Roberto
        PartidoFactory tenisFactory = new TenisFactory();
        
        // 1. Cambiar estrategia de emparejamiento a por cercanía
        gestorPartido.getEmparejador().cambiarEstrategia(new EmparejamientoPorCercania());
        
        // 2. Crear partido
        Partido partido = gestorPartido.crearPartido(tenisFactory, "2024-12-26 10:00", ubicacionPalermo, organizador);
        
        // 3. Agregar usuarios - el sistema debería priorizar los más cercanos
        gestorPartido.agregarJugador(partido, organizador); // Roberto (organizador)
        gestorPartido.agregarJugador(partido, usuariosDisponibles.get(6)); // Carmen - también Palermo
        
        // Verificar que el partido está completo con 2 jugadores (1 en cada equipo)
        assertEquals(2, partido.getEquipos().stream().mapToInt(e -> e.cantidadJugadores()).sum());
        
        // 4. Verificar que se formó correctamente y está completo (tenis necesita 2 jugadores)
        assertEquals(2, partido.getCantJugadoresRequeridos());
        assertEquals(2, partido.getEquipos().size());
        assertTrue(partido.estaCompleto(), "El partido de tenis debería estar completo con 2 jugadores");
        
        // 5. Completar partido
        partido.cambiarEstado(new PartidoArmado());
        assertTrue(gestorPartido.confirmarPartido(partido));
        assertTrue(gestorPartido.iniciarPartido(partido));
        assertTrue(gestorPartido.finalizarPartido(partido));
    }
    
    @Test
    void testCasoDeUso_CancelacionPorFaltaDeJugadores() {
        // ESCENARIO: Se crea un partido pero no llegan suficientes jugadores
        Usuario organizador = usuariosDisponibles.get(7); // Diego
        PartidoFactory basquetFactory = new BasquetFactory();
        
        // 1. Crear partido de básquet (necesita 10 jugadores)
        Partido partido = gestorPartido.crearPartido(basquetFactory, "2024-12-27 19:00", ubicacionVillaCrespo, organizador);
        
        // 2. Solo agregar pocos jugadores
        gestorPartido.agregarJugador(partido, usuariosDisponibles.get(8)); // Sofía
        
        // 3. Decidir cancelar por falta de jugadores
        assertTrue(gestorPartido.cancelarPartido(partido));
        assertTrue(partido.getEstadoActual() instanceof Cancelado);
        
        // 4. Verificar que no se pueden hacer más operaciones
        assertFalse(gestorPartido.agregarJugador(partido, usuariosDisponibles.get(0)));
        assertFalse(gestorPartido.confirmarPartido(partido));
    }
    
    @Test
    void testCasoDeUso_CambioDeEstrategiasDuranteEmparejamiento() {
        // ESCENARIO: Probar diferentes estrategias para el mismo conjunto de jugadores
        Usuario organizador = usuariosDisponibles.get(0); // Ana
        PartidoFactory futbolFactory = new FutbolFactory();
        Partido partido = gestorPartido.crearPartido(futbolFactory, "2024-12-28 15:00", ubicacionPalermo, organizador);
        
        // Preparar lista de jugadores para emparejar
        List<Usuario> candidatos = new ArrayList<>();
        candidatos.addAll(usuariosDisponibles.subList(1, 5)); // Carlos, María, Juan, Laura
        
        // 1. Probar emparejamiento por nivel
        gestorPartido.getEmparejador().cambiarEstrategia(new EmparejamientoPorNivel());
        Equipo equipoNivel = gestorPartido.getEmparejador().emparejarJugadores(candidatos, partido);
        assertNotNull(equipoNivel);
        
        // 2. Probar emparejamiento por cercanía
        gestorPartido.getEmparejador().cambiarEstrategia(new EmparejamientoPorCercania());
        Equipo equipoCercania = gestorPartido.getEmparejador().emparejarJugadores(candidatos, partido);
        assertNotNull(equipoCercania);
        
        // 3. Probar emparejamiento mixto
        gestorPartido.getEmparejador().cambiarEstrategia(new EmparejamientoMixto());
        Equipo equipoMixto = gestorPartido.getEmparejador().emparejarJugadores(candidatos, partido);
        assertNotNull(equipoMixto);
        
        // 4. Todos los emparejamientos deberían funcionar
        assertTrue(equipoNivel.cantidadJugadores() > 0);
        assertTrue(equipoCercania.cantidadJugadores() > 0);
        assertTrue(equipoMixto.cantidadJugadores() > 0);
    }
    
    @Test
    void testCasoDeUso_MultiplesPartidosSimultaneos() {
        // ESCENARIO: Gestionar múltiples partidos al mismo tiempo
        
        // 1. Crear partido de fútbol
        Partido partidoFutbol = gestorPartido.crearPartido(
            new FutbolFactory(), 
            "2024-12-29 16:00", 
            ubicacionPalermo, 
            usuariosDisponibles.get(0)
        );
        
        // 2. Crear partido de tenis
        Partido partidoTenis = gestorPartido.crearPartido(
            new TenisFactory(), 
            "2024-12-29 18:00", 
            ubicacionVillaCrespo, 
            usuariosDisponibles.get(5)
        );
        
        // 3. Crear partido de básquet
        Partido partidoBasquet = gestorPartido.crearPartido(
            new BasquetFactory(), 
            "2024-12-29 20:00", 
            ubicacionPalermo, 
            usuariosDisponibles.get(7)
        );
        
        // 4. Agregar jugadores a diferentes partidos
        gestorPartido.agregarJugador(partidoFutbol, usuariosDisponibles.get(1));
        gestorPartido.agregarJugador(partidoTenis, usuariosDisponibles.get(6));
        gestorPartido.agregarJugador(partidoBasquet, usuariosDisponibles.get(8));
        
        // 5. Verificar que cada partido mantiene su estado independientemente
        assertTrue(partidoFutbol.getEstadoActual() instanceof NecesitamosJugadores);
        assertTrue(partidoTenis.getEstadoActual() instanceof NecesitamosJugadores);
        assertTrue(partidoBasquet.getEstadoActual() instanceof NecesitamosJugadores);
        
        // 6. Confirmar uno y cancelar otro
        partidoTenis.cambiarEstado(new PartidoArmado());
        gestorPartido.confirmarPartido(partidoTenis);
        gestorPartido.cancelarPartido(partidoBasquet);
        
        // 7. Verificar estados independientes
        assertTrue(partidoFutbol.getEstadoActual() instanceof NecesitamosJugadores);
        assertTrue(partidoTenis.getEstadoActual() instanceof Confirmado);
        assertTrue(partidoBasquet.getEstadoActual() instanceof Cancelado);
    }
    
    @Test
    void testCasoDeUso_PartidoConCriteriosRestrictivos() {
        // ESCENARIO: Crear un partido muy restrictivo y verificar filtrado
        Usuario organizador = usuariosDisponibles.get(1); // Carlos (avanzado, masculino, 30 años)
        PartidoFactory futbolFactory = new FutbolFactory();
        
        Partido partido = gestorPartido.crearPartido(futbolFactory, "2024-12-30 14:00", ubicacionPalermo, organizador);
        
        // Criterios muy restrictivos: solo masculinos, avanzados, 28-32 años, radio 1km
        CriteriosPartido criteriosRestrictivos = new CriteriosPartido("avanzado", "avanzado", 28, 32, "masculino", 1.0f);
        partido.aplicarCriterios(criteriosRestrictivos);
        
        // Intentar agregar todos los usuarios
        int jugadoresAgregados = 0;
        for (Usuario usuario : usuariosDisponibles) {
            if (gestorPartido.agregarJugador(partido, usuario)) {
                jugadoresAgregados++;
                // Verificar que cumple todos los criterios
                assertEquals("avanzado", usuario.getNivel());
                assertEquals("masculino", usuario.getGenero());
                assertTrue(usuario.getEdad() >= 28 && usuario.getEdad() <= 32);
            }
        }
        
        // Solo Carlos (organizador ya incluido) debería cumplir estos criterios
        // En nuestros datos de prueba, solo Carlos y Laura son avanzados,
        // pero Laura es femenina, así que debería ser muy pocos o ninguno adicional
        assertTrue(jugadoresAgregados <= 1); // Máximo Carlos si no está ya incluido
    }
    
    @Test
    void testCasoDeUso_SistemaCompleto_FlujoPerfecto() {
        // ESCENARIO: Un flujo completo perfecto del sistema
        Usuario organizador = usuariosDisponibles.get(0); // Ana
        
        // 1. Crear partido con factory
        PartidoFactory futbolFactory = new FutbolFactory();
        Partido partido = gestorPartido.crearPartido(futbolFactory, "2024-12-31 16:00", ubicacionPalermo, organizador);
        assertEquals("Fútbol", partido.getDeporte());
        
        // 2. Configurar criterios razonables
        CriteriosPartido criterios = new CriteriosPartido("principiante", "avanzado", 18, 50, "mixto", 10.0f);
        partido.aplicarCriterios(criterios);
        
        // 3. Agregar jugadores que cumplen criterios
        List<Usuario> participantes = new ArrayList<>();
        for (Usuario usuario : usuariosDisponibles) {
            if (usuario.getDeporteFavorito().equals("Fútbol") && gestorPartido.agregarJugador(partido, usuario)) {
                participantes.add(usuario);
            }
        }
        assertTrue(participantes.size() > 0);
        
        // 4. Usar estrategia mixta para emparejamiento
        gestorPartido.getEmparejador().cambiarEstrategia(new EmparejamientoMixto());
        
        // 5. Agregar más jugadores hasta completar el partido si es necesario
        if (!partido.estaCompleto()) {
            // Crear usuarios adicionales para completar el partido
            for (int i = participantes.size(); i < partido.getCantJugadoresRequeridos(); i++) {
                Usuario usuarioExtra = new Usuario("Jugador" + i, "jugador" + i + "@test.com", "pass", 
                                                 "Fútbol", "intermedio", ubicacionPalermo, 25, "mixto");
                gestorPartido.agregarJugador(partido, usuarioExtra);
            }
        }
        
        // 6. Simular llenado y confirmar
        partido.cambiarEstado(new PartidoArmado());
        assertTrue(gestorPartido.confirmarPartido(partido));
        assertTrue(partido.getEstadoActual() instanceof Confirmado);
        
        // 7. Iniciar partido (solo si está completo)
        if (partido.estaCompleto()) {
            assertTrue(gestorPartido.iniciarPartido(partido));
            assertTrue(partido.getEstadoActual() instanceof EnJuego);
        }
        
        // 8. Finalizar partido (solo si está en juego)
        if (partido.getEstadoActual() instanceof EnJuego) {
            assertTrue(gestorPartido.finalizarPartido(partido));
            assertTrue(partido.getEstadoActual() instanceof Finalizado);
        }
        
        // 9. Verificar estructura final
        assertEquals(2, partido.getEquipos().size()); // Fútbol tiene 2 equipos
        assertNotNull(partido.getOrganizador());
        assertNotNull(partido.getUbicacion());
        assertTrue(partido.tieneCriterios());
    }
    
    // Adapters de prueba simplificados
    private static class TestEmailAdapter implements ServicioEmail {
        @Override
        public boolean enviarEmail(String destinatario, String asunto, String mensaje) {
            return true;
        }
    }
    
    private static class TestPushAdapter implements ServicioPush {
        @Override
        public boolean enviarPush(Usuario usuario, String titulo, String mensaje) {
            return true;
        }
    }
} 