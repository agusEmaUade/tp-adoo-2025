package com.tp.uno.mas.encuentros.deportivos.service;

import com.tp.uno.mas.encuentros.deportivos.model.IEstrategiaNotificacion;
import org.springframework.stereotype.Service;
import java.util.Objects;

/**
 * Contexto del patrón Strategy para notificaciones.
 * Permite cambiar dinámicamente la estrategia de envío de notificaciones.
 * Principio SOLID: Open/Closed - abierto para extensión de nuevas estrategias.
 */
@Service
public class Notificador {
    
    private IEstrategiaNotificacion estrategia;
    
    public Notificador() {
        // Estrategia por defecto
        this.estrategia = new Firebase();
    }
    
    public Notificador(IEstrategiaNotificacion estrategia) {
        this.estrategia = Objects.requireNonNull(estrategia, "La estrategia no puede ser nula");
    }
    
    /**
     * Envía una notificación usando la estrategia actual.
     * Principio GRASP: Polymorphism - delega en la estrategia específica.
     */
    public void enviarNotificacion(String mensaje, com.tp.uno.mas.encuentros.deportivos.model.Cuenta destinatario) {
        Objects.requireNonNull(mensaje, "El mensaje no puede ser nulo");
        
        if (mensaje.trim().isEmpty()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío");
        }
        
        if (estrategia == null) {
            throw new IllegalStateException("No hay estrategia de notificación configurada");
        }
        
        if (!estrategia.estaDisponible()) {
            throw new IllegalStateException("La estrategia de notificación no está disponible: " + 
                                          estrategia.obtenerNombre());
        }
        
        estrategia.enviarNotificacion(mensaje, destinatario);
    }
    
    /**
     * Cambia la estrategia de notificación en tiempo de ejecución.
     * Patrón Strategy - permite cambiar algoritmo dinámicamente.
     */
    public void cambiarEstrategia(IEstrategiaNotificacion nuevaEstrategia) {
        this.estrategia = Objects.requireNonNull(nuevaEstrategia, "La nueva estrategia no puede ser nula");
    }
    
    /**
     * Obtiene la estrategia actual.
     */
    public IEstrategiaNotificacion obtenerEstrategia() {
        return estrategia;
    }
    
    /**
     * Verifica si el notificador está listo para enviar notificaciones.
     */
    public boolean estaListo() {
        return estrategia != null && estrategia.estaDisponible();
    }
} 