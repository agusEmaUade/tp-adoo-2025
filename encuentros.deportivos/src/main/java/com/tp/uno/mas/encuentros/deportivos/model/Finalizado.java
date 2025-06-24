package com.tp.uno.mas.encuentros.deportivos.model;

/**
 * Estado final del partido - Patrón State.
 * El partido ha terminado exitosamente.
 */
public class Finalizado implements IEstado {

    @Override
    public void manejar(Partido partido) {
        // Lógica específica: procesar resultado del partido
        // Actualizar estadísticas, enviar resumen, etc.
    }

    @Override
    public String obtenerNombre() {
        return "FINALIZADO";
    }

    @Override
    public boolean puedeAgregarJugadores() {
        return false; // Partido terminado
    }

    @Override
    public boolean puedeRemoverJugadores() {
        return false; // Partido terminado
    }

    @Override
    public IEstado siguienteEstado() {
        return null; // Estado final
    }

    @Override
    public String toString() {
        return "Finalizado";
    }
} 