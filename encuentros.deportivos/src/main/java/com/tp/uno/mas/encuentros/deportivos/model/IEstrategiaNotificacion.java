package com.tp.uno.mas.encuentros.deportivos.model;

/**
 * Patrón Strategy - Interfaz para diferentes estrategias de notificación.
 * Principio SOLID: Interface Segregation - interfaz específica para notificaciones.
 * Principio SOLID: Open/Closed - abierto para extensión, cerrado para modificación.
 */
public interface IEstrategiaNotificacion {
    
    /**
     * Envía una notificación usando la estrategia específica.
     * 
     * @param mensaje El mensaje a enviar
     * @param destinatario Usuario que recibirá la notificación (puede ser null para broadcast)
     * @throws IllegalArgumentException si el mensaje es nulo o vacío
     */
    void enviarNotificacion(String mensaje, Cuenta destinatario);
    
    /**
     * Obtiene el nombre de la estrategia de notificación.
     * 
     * @return El nombre identificativo de la estrategia
     */
    String obtenerNombre();
    
    /**
     * Indica si la estrategia está disponible para uso.
     * 
     * @return true si la estrategia puede ser utilizada
     */
    boolean estaDisponible();
    
    /**
     * Configura la estrategia con parámetros específicos.
     * 
     * @param configuracion Mapa con configuraciones necesarias para esta estrategia
     */
    void configurar(java.util.Map<String, Object> configuracion);
} 