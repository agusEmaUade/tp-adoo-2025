package com.tp.uno.mas.encuentros.deportivos.model;

/**
 * Patrón State - Interfaz para los diferentes estados del partido.
 * Principio SOLID: Interface Segregation - interfaz específica para estados.
 * Permite que el comportamiento del partido cambie según su estado actual.
 */
public interface IEstado {
    
    /**
     * Maneja la lógica específica del estado actual del partido.
     * 
     * @param partido El partido sobre el cual aplicar la lógica del estado
     */
    void manejar(Partido partido);
    
    /**
     * Obtiene el nombre del estado actual.
     * 
     * @return El nombre identificativo del estado
     */
    String obtenerNombre();
    
    /**
     * Determina si se pueden agregar jugadores en este estado.
     * 
     * @return true si se pueden agregar jugadores
     */
    boolean puedeAgregarJugadores();
    
    /**
     * Determina si se pueden remover jugadores en este estado.
     * 
     * @return true si se pueden remover jugadores
     */
    boolean puedeRemoverJugadores();
    
    /**
     * Obtiene el siguiente estado posible.
     * 
     * @return El siguiente estado o null si no hay transición disponible
     */
    IEstado siguienteEstado();
} 