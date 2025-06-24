package com.tp.uno.mas.encuentros.deportivos.model;

import java.util.List;

/**
 * Patrón Strategy - Interfaz para estrategias de emparejamiento de jugadores.
 * Permite diferentes algoritmos de búsqueda y compatibilidad entre jugadores.
 * Principio SOLID: Open/Closed - abierto para extensión de nuevas estrategias.
 */
public interface IEstrategiaEmparejamiento {
    
    /**
     * Encuentra jugadores compatibles para un partido específico.
     * 
     * @param partido El partido que necesita jugadores
     * @param candidatos Lista de jugadores candidatos disponibles
     * @return Lista de jugadores compatibles ordenada por compatibilidad
     */
    List<Cuenta> encontrarJugadoresCompatibles(Partido partido, List<Cuenta> candidatos);
    
    /**
     * Calcula el grado de compatibilidad entre un jugador y un partido.
     * 
     * @param jugador El jugador a evaluar
     * @param partido El partido contra el cual evaluar
     * @return Puntuación de compatibilidad (0.0 = incompatible, 1.0 = perfecta compatibilidad)
     */
    double calcularCompatibilidad(Cuenta jugador, Partido partido);
    
    /**
     * Obtiene el nombre identificativo de la estrategia.
     * 
     * @return Nombre de la estrategia
     */
    String obtenerNombre();
} 