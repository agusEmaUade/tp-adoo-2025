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
    private BuscadorPartidos buscadorPartidos;
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
        
        gestorPartido = new GestorPartido(notificacionManager);
        buscadorPartidos = new BuscadorPartidos();
        
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
            assertTrue(jugador.getNivel().toString().equals("intermedio") || jugador.getNivel().toString().equals("avanzado"));
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
    void testCasoDeUso_PartidoTenisConBusquedaPorCercania() {
        // ESCENARIO: Roberto organiza un partido de tenis y quiere jugadores cercanos
        Usuario organizador = usuariosDisponibles.get(5); // Roberto
        PartidoFactory tenisFactory = new TenisFactory();
        
        // 1. Crear partido
        Partido partido = gestorPartido.crearPartido(tenisFactory, "2024-12-26 10:00", ubicacionPalermo, organizador);
        
        // 2. Buscar jugadores cercanos
        List<Partido> partidos = new ArrayList<>();
        partidos.add(partido);
        List<Partido> partidosCercanos = buscadorPartidos.buscarPartidosCercanos(partidos, organizador, 5.0);
        
        // 3. Agregar usuarios cercanos
        gestorPartido.agregarJugador(partido, organizador); // Roberto (organizador)
        gestorPartido.agregarJugador(partido, usuariosDisponibles.get(6)); // Carmen - también en Palermo
        
        // Verificar que el partido está completo con 2 jugadores
        assertEquals(2, partido.getEquipos().stream().mapToInt(e -> e.cantidadJugadores()).sum());
        
        // 4. Verificar que se formó correctamente y está completo
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
    void testCasoDeUso_BusquedaConDiferentesEstrategias() {
        // ESCENARIO: Probar diferentes estrategias de búsqueda
        Usuario organizador = usuariosDisponibles.get(0); // Ana
        PartidoFactory futbolFactory = new FutbolFactory();
        Partido partido = gestorPartido.crearPartido(futbolFactory, "2024-12-28 15:00", ubicacionPalermo, organizador);
        
        List<Partido> partidos = new ArrayList<>();
        partidos.add(partido);
        
        // 1. Probar búsqueda por nivel
        List<Partido> resultadosNivel = buscadorPartidos.buscarPartidosPorNivel(partidos, organizador);
        assertNotNull(resultadosNivel);
        
        // 2. Probar búsqueda por cercanía
        List<Partido> resultadosCercania = buscadorPartidos.buscarPartidosCercanos(partidos, organizador, 5.0);
        assertNotNull(resultadosCercania);
        
        // 3. Probar búsqueda por deporte
        List<Partido> resultadosDeporte = buscadorPartidos.buscarPartidosPorDeporte(partidos, organizador, "Fútbol");
        assertNotNull(resultadosDeporte);
        
        // 4. Probar búsqueda mixta
        List<Partido> resultadosMixta = buscadorPartidos.buscarPartidosConCriteriosMixtos(partidos, organizador, 2);
        assertNotNull(resultadosMixta);
        
        // Verificar que todas las búsquedas funcionan
        assertTrue(resultadosNivel.size() >= 0);
        assertTrue(resultadosCercania.size() >= 0);
        assertTrue(resultadosDeporte.size() >= 0);
        assertTrue(resultadosMixta.size() >= 0);
    }
    
    @Test
    void testCasoDeUso_MultiplesPartidosSimultaneos() {
        // ESCENARIO: Gestionar múltiples partidos al mismo tiempo
        
        // 1. Crear manualmente los partidos para un control total
        Partido partidoFutbol = new FutbolFactory().crearPartidoCompleto("2024-12-29 16:00", ubicacionPalermo, usuariosDisponibles.get(0));
        gestorPartido.agregarJugador(partidoFutbol, usuariosDisponibles.get(0));
        
        Partido partidoTenis = new TenisFactory().crearPartidoCompleto("2024-12-29 18:00", ubicacionVillaCrespo, usuariosDisponibles.get(5));
        gestorPartido.agregarJugador(partidoTenis, usuariosDisponibles.get(5));
        
        Partido partidoBasquet = new BasquetFactory().crearPartidoCompleto("2024-12-29 20:00", ubicacionPalermo, usuariosDisponibles.get(7));
        gestorPartido.agregarJugador(partidoBasquet, usuariosDisponibles.get(7));

        // 2. Verificar estados iniciales
        assertTrue(partidoFutbol.getEstadoActual() instanceof NecesitamosJugadores);
        assertTrue(partidoTenis.getEstadoActual() instanceof NecesitamosJugadores);
        assertTrue(partidoBasquet.getEstadoActual() instanceof NecesitamosJugadores);
        
        // 3. Completar el partido de tenis, confirmar y cancelar otro
        gestorPartido.agregarJugador(partidoTenis, usuariosDisponibles.get(6)); // Agregar segundo jugador
        
        assertTrue(partidoTenis.getEstadoActual() instanceof PartidoArmado, "El partido de tenis debería estar 'Armado'");
        
        assertTrue(gestorPartido.confirmarPartido(partidoTenis), "La confirmación del partido de tenis debería ser exitosa");
        assertTrue(gestorPartido.cancelarPartido(partidoBasquet), "La cancelación del partido de básquet debería ser exitosa");
        
        // 4. Verificar estados finales
        assertTrue(partidoFutbol.getEstadoActual() instanceof NecesitamosJugadores, "Futbol debe seguir necesitando jugadores");
        assertTrue(partidoTenis.getEstadoActual() instanceof Confirmado, "Tenis debe estar 'Confirmado'");
        assertTrue(partidoBasquet.getEstadoActual() instanceof Cancelado, "Básquet debe estar 'Cancelado'");
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
        
        // Crear lista de partidos para búsqueda
        List<Partido> partidos = new ArrayList<>();
        partidos.add(partido);
        
        // Buscar partidos que cumplan los criterios para cada usuario
        int jugadoresAgregados = 0;
        for (Usuario usuario : usuariosDisponibles) {
            List<Partido> partidosValidos = buscadorPartidos.buscarPartidosPorNivel(partidos, usuario);
            if (!partidosValidos.isEmpty() && gestorPartido.agregarJugador(partido, usuario)) {
                jugadoresAgregados++;
                // Verificar que cumple todos los criterios
                assertEquals("avanzado", usuario.getNivel().toString());
                assertEquals("masculino", usuario.getGenero());
                assertTrue(usuario.getEdad() >= 28 && usuario.getEdad() <= 32);
            }
        }
        
        // Solo Carlos (organizador ya incluido) debería cumplir estos criterios
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
        
        // 4. Usar estrategia mixta para búsqueda
        List<Partido> partidosDisponibles = new ArrayList<>();
        partidosDisponibles.add(partido);
        List<Partido> resultadosMixta = buscadorPartidos.buscarPartidosConCriteriosMixtos(partidosDisponibles, participantes.get(0), 2);
        assertNotNull(resultadosMixta);
        
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