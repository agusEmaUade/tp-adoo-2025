package com.tp.uno.mas.encuentros.deportivos.model;

/**
 * Patrón Observer - Interfaz para observadores de eventos del sistema.
 * Permite notificaciones automáticas cuando ocurren eventos importantes.
 * Principio SOLID: Interface Segregation - interfaz específica para observadores.
 */
public interface IObservador {
    
    /**
     * Método llamado cuando ocurre un evento que el observador debe procesar.
     * 
     * @param evento Tipo de evento ocurrido (ej: "PARTIDO_CREADO", "ESTADO_CAMBIADO")
     * @param datos Datos relacionados con el evento
     */
    void actualizar(String evento, Object datos);
    
    /**
     * Obtiene el tipo de observador para identificación.
     * 
     * @return Tipo del observador (ej: "NOTIFICADOR", "ESTADISTICAS")
     */
    String getTipo();
} 