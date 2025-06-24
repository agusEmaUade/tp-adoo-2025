package com.tp.uno.mas.encuentros.deportivos.service;

import com.tp.uno.mas.encuentros.deportivos.model.*;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

/**
 * Factory para la creación de estados del partido.
 * Patrón Factory - centraliza la creación de objetos relacionados.
 * Principio SOLID: Single Responsibility - se enfoca únicamente en crear estados.
 */
@Service
public class EstadoFactory {

    /**
     * Crea un estado específico basado en el tipo solicitado.
     * 
     * @param tipoEstado Nombre del estado a crear
     * @return Instancia del estado solicitado
     * @throws IllegalArgumentException si el tipo de estado no es válido
     */
    public IEstado crearEstado(String tipoEstado) {
        Objects.requireNonNull(tipoEstado, "El tipo de estado no puede ser nulo");
        
        return switch (tipoEstado.toUpperCase()) {
            case "NECESITAMOS_JUGADORES" -> new NecesitamosJugadores();
            case "PARTIDO_ARMADO" -> new PartidoArmado();
            case "CONFIRMADO" -> new Confirmado();
            case "EN_JUEGO" -> new EnJuego();
            case "FINALIZADO" -> new Finalizado();
            case "CANCELADO" -> new Cancelado();
            default -> throw new IllegalArgumentException("Tipo de estado no válido: " + tipoEstado);
        };
    }

    /**
     * Crea el estado inicial según el enunciado.
     * 
     * @return Estado inicial "Necesitamos jugadores"
     */
    public IEstado crearEstadoInicial() {
        return new NecesitamosJugadores();
    }

    /**
     * Crea el siguiente estado lógico basado en el estado actual.
     * 
     * @param estadoActual Estado desde el cual transicionar
     * @return Siguiente estado en la secuencia lógica
     */
    public IEstado crearSiguienteEstado(IEstado estadoActual) {
        Objects.requireNonNull(estadoActual, "El estado actual no puede ser nulo");
        
        return estadoActual.siguienteEstado();
    }

    /**
     * Crea un estado basado en las condiciones del partido.
     * 
     * @param partido Partido para evaluar condiciones
     * @return Estado apropiado según las condiciones actuales
     */
    public IEstado crearEstadoSegunCondiciones(Partido partido) {
        Objects.requireNonNull(partido, "El partido no puede ser nulo");
        
        // Lógica para determinar el estado apropiado
        if (partido.getJugadores().size() < partido.getCantidadJugadores()) {
            return new NecesitamosJugadores();
        } else if (partido.getJugadores().size() >= partido.getCantidadJugadores()) {
            // Verificar si ya está confirmado
            if (partido.getEstadoNombre() == Partido.EstadoEnum.CONFIRMADO ||
                partido.getEstadoNombre() == Partido.EstadoEnum.EN_JUEGO ||
                partido.getEstadoNombre() == Partido.EstadoEnum.FINALIZADO) {
                return crearEstado(partido.getEstadoNombre().name());
            }
            return new PartidoArmado();
        }
        
        return new NecesitamosJugadores();
    }

    /**
     * Obtiene la lista de todos los estados disponibles.
     * 
     * @return Lista con los nombres de todos los estados
     */
    public List<String> obtenerEstadosDisponibles() {
        return Arrays.asList(
            "NECESITAMOS_JUGADORES",
            "PARTIDO_ARMADO", 
            "CONFIRMADO",
            "EN_JUEGO",
            "FINALIZADO",
            "CANCELADO"
        );
    }

    /**
     * Valida si un tipo de estado es válido.
     * 
     * @param tipo Nombre del estado a validar
     * @return true si el estado es válido
     */
    public boolean validarTipoEstado(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            return false;
        }
        
        return obtenerEstadosDisponibles().contains(tipo.toUpperCase());
    }

    /**
     * Crea múltiples estados para inicialización.
     * 
     * @param tipos Lista de tipos de estado a crear
     * @return Lista de estados creados
     */
    public List<IEstado> crearEstados(List<String> tipos) {
        Objects.requireNonNull(tipos, "La lista de tipos no puede ser nula");
        
        return tipos.stream()
                .map(this::crearEstado)
                .toList();
    }

    /**
     * Obtiene información sobre las transiciones válidas desde un estado.
     * 
     * @param estadoActual Estado desde el cual verificar transiciones
     * @return Lista de estados a los que se puede transicionar
     */
    public List<String> obtenerTransicionesValidas(String estadoActual) {
        Objects.requireNonNull(estadoActual, "El estado actual no puede ser nulo");
        
        return switch (estadoActual.toUpperCase()) {
            case "NECESITAMOS_JUGADORES" -> Arrays.asList("PARTIDO_ARMADO", "CANCELADO");
            case "PARTIDO_ARMADO" -> Arrays.asList("CONFIRMADO", "NECESITAMOS_JUGADORES", "CANCELADO");
            case "CONFIRMADO" -> Arrays.asList("EN_JUEGO", "CANCELADO");
            case "EN_JUEGO" -> Arrays.asList("FINALIZADO");
            case "FINALIZADO", "CANCELADO" -> Arrays.asList(); // Estados finales
            default -> throw new IllegalArgumentException("Estado no reconocido: " + estadoActual);
        };
    }
} 