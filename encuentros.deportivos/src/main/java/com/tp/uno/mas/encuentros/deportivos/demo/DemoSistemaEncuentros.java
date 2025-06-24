package com.tp.uno.mas.encuentros.deportivos.demo;

import com.tp.uno.mas.encuentros.deportivos.adapter.*;
import com.tp.uno.mas.encuentros.deportivos.factory.*;
import com.tp.uno.mas.encuentros.deportivos.model.*;
import com.tp.uno.mas.encuentros.deportivos.observer.*;
import com.tp.uno.mas.encuentros.deportivos.strategy.*;

import java.util.ArrayList;
import java.util.List;

public class DemoSistemaEncuentros {
    
    public static void main(String[] args) {
        System.out.println("=== DEMO: Sistema de Encuentros Deportivos ===\n");
        
        // 1. Configurar sistema de notificaciones
        NotificacionManager notificacionManager = configurarNotificaciones();
        
        // 2. Configurar emparejador con estrategia
        Emparejador emparejador = new Emparejador(new EmparejamientoPorNivel());
        
        // 3. Crear gestor de partidos
        GestorPartido gestorPartido = new GestorPartido(notificacionManager, emparejador);
        
        // 4. Crear usuarios
        List<Usuario> usuarios = crearUsuarios();
        
        // 5. Crear ubicaciones
        Ubicacion ubicacionCancha = new Ubicacion(-34.6037f, -58.3816f, 5.0f); // Buenos Aires
        
        // 6. Crear partidos usando Factory
        demostrarCreacionPartidos(gestorPartido, ubicacionCancha, usuarios);
        
        // 7. Demostrar diferentes estrategias de emparejamiento
        demostrarEstrategias(emparejador, usuarios);
        
        System.out.println("\n=== FIN DEL DEMO ===");
    }
    
    private static NotificacionManager configurarNotificaciones() {
        System.out.println("--- Configurando Sistema de Notificaciones ---");
        
        NotificacionManager notificacionManager = new NotificacionManager();
        
        // Configurar servicios de notificación (patrón Adapter)
        ServicioEmail servicioEmail = new JavaMailAdapter();
        ServicioPush servicioPush = new FirebaseAdapter();
        
        // Crear observadores (patrón Observer)
        EmailNotificador emailNotificador = new EmailNotificador(servicioEmail);
        PushNotificador pushNotificador = new PushNotificador(servicioPush);
        
        // Registrar observadores
        notificacionManager.agregarObserver(emailNotificador);
        notificacionManager.agregarObserver(pushNotificador);
        
        System.out.println("✓ Sistema de notificaciones configurado\n");
        return notificacionManager;
    }
    
    private static List<Usuario> crearUsuarios() {
        System.out.println("--- Creando Usuarios ---");
        
        List<Usuario> usuarios = new ArrayList<>();
        
        // Ubicaciones de usuarios
        Ubicacion ubicacion1 = new Ubicacion(-34.6037f, -58.3816f, 2.0f);
        Ubicacion ubicacion2 = new Ubicacion(-34.6118f, -58.3960f, 2.0f);
        Ubicacion ubicacion3 = new Ubicacion(-34.5928f, -58.3756f, 2.0f);
        
        usuarios.add(new Usuario("Juan Pérez", "juan@email.com", "123", "Fútbol", "intermedio", ubicacion1, 25, "masculino"));
        usuarios.add(new Usuario("María García", "maria@email.com", "456", "Fútbol", "principiante", ubicacion2, 22, "femenino"));
        usuarios.add(new Usuario("Carlos López", "carlos@email.com", "789", "Básquet", "avanzado", ubicacion3, 30, "masculino"));
        usuarios.add(new Usuario("Ana Martín", "ana@email.com", "101", "Tenis", "intermedio", ubicacion1, 28, "femenino"));
        
        System.out.println("✓ " + usuarios.size() + " usuarios creados\n");
        return usuarios;
    }
    
    private static void demostrarCreacionPartidos(GestorPartido gestorPartido, Ubicacion ubicacion, List<Usuario> usuarios) {
        System.out.println("--- Demostrando Creación de Partidos (Patrón Factory) ---");
        
        Usuario organizador = usuarios.get(0);
        
        // Crear diferentes tipos de partidos
        PartidoFactory futbolFactory = new FutbolFactory();
        Partido partidoFutbol = gestorPartido.crearPartido(futbolFactory, "2024-12-20 15:00", ubicacion, organizador);
        
        PartidoFactory tenisFactory = new TenisFactory();
        Partido partidoTenis = gestorPartido.crearPartido(tenisFactory, "2024-12-21 10:00", ubicacion, organizador);
        
        System.out.println("\n--- Agregando Jugadores al Partido de Fútbol ---");
        demostrarCicloVidaPartido(gestorPartido, partidoFutbol, usuarios);
    }
    
    private static void demostrarCicloVidaPartido(GestorPartido gestorPartido, Partido partido, List<Usuario> usuarios) {
        System.out.println("Estado inicial: " + partido.getEstadoActual().getNombreEstado());
        
        // Agregar criterios al partido
        CriteriosPartido criterios = new CriteriosPartido("principiante", "avanzado", 18, 40, "mixto", 10.0f);
        partido.aplicarCriterios(criterios);
        System.out.println("✓ Criterios aplicados al partido");
        
        // Intentar agregar jugadores
        for (Usuario usuario : usuarios) {
            boolean agregado = gestorPartido.agregarJugador(partido, usuario);
            if (agregado) {
                System.out.println("✓ " + usuario.getNombre() + " agregado al partido");
            } else {
                System.out.println("✗ " + usuario.getNombre() + " no pudo ser agregado");
            }
        }
        
        System.out.println("\nEstado actual: " + partido.getEstadoActual().getNombreEstado());
        
        // Intentar confirmar partido si está armado
        if (partido.getEstadoActual().puedeConfirmar()) {
            gestorPartido.confirmarPartido(partido);
        }
        
        // Iniciar partido si es posible
        if (partido.getEstadoActual().puedeIniciar()) {
            gestorPartido.iniciarPartido(partido);
        }
        
        // Finalizar partido
        if (partido.getEstadoActual().puedeFinalizar()) {
            gestorPartido.finalizarPartido(partido);
        }
        
        System.out.println("Estado final: " + partido.getEstadoActual().getNombreEstado());
    }
    
    private static void demostrarEstrategias(Emparejador emparejador, List<Usuario> usuarios) {
        System.out.println("\n--- Demostrando Estrategias de Emparejamiento (Patrón Strategy) ---");
        
        // Crear un partido de prueba
        Ubicacion ubicacion = new Ubicacion(-34.6037f, -58.3816f, 5.0f);
        Partido partidoPrueba = new Partido("2024-12-22 16:00", "Fútbol", 4, 90, ubicacion, usuarios.get(0));
        
        // Estrategia por nivel
        System.out.println("\n1. Estrategia por Nivel:");
        emparejador.cambiarEstrategia(new EmparejamientoPorNivel());
        Equipo equipoNivel = emparejador.emparejarJugadores(usuarios, partidoPrueba);
        if (equipoNivel != null) {
            System.out.println("✓ Equipo creado: " + equipoNivel);
        }
        
        // Estrategia por cercanía
        System.out.println("\n2. Estrategia por Cercanía:");
        emparejador.cambiarEstrategia(new EmparejamientoPorCercania());
        Equipo equipoCercania = emparejador.emparejarJugadores(usuarios, partidoPrueba);
        if (equipoCercania != null) {
            System.out.println("✓ Equipo creado: " + equipoCercania);
        }
        
        // Estrategia por historial
        System.out.println("\n3. Estrategia por Historial:");
        emparejador.cambiarEstrategia(new EmparejamientoPorHistorial());
        Equipo equipoHistorial = emparejador.emparejarJugadores(usuarios, partidoPrueba);
        if (equipoHistorial != null) {
            System.out.println("✓ Equipo creado: " + equipoHistorial);
        }
        
        // Estrategia mixta
        System.out.println("\n4. Estrategia Mixta:");
        emparejador.cambiarEstrategia(new EmparejamientoMixto());
        Equipo equipoMixto = emparejador.emparejarJugadores(usuarios, partidoPrueba);
        if (equipoMixto != null) {
            System.out.println("✓ Equipo creado: " + equipoMixto);
        }
    }
} 