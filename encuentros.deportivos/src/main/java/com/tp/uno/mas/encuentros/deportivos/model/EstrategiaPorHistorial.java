package com.tp.uno.mas.encuentros.deportivos.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Estrategia de emparejamiento basada en el historial de partidos previos de los jugadores.
 * Patrón Strategy - implementación concreta para compatibilidad por experiencia.
 */
public class EstrategiaPorHistorial implements IEstrategiaEmparejamiento {

    @Override
    public List<Cuenta> encontrarJugadoresCompatibles(Partido partido, List<Cuenta> candidatos) {
        Objects.requireNonNull(partido, "El partido no puede ser nulo");
        Objects.requireNonNull(candidatos, "Los candidatos no pueden ser nulos");

        return candidatos.stream()
                .filter(candidato -> candidato.getDeporteFavorito() == null || 
                                   candidato.getDeporteFavorito().equals(partido.getTipoDeporte()))
                .filter(candidato -> !partido.getJugadores().contains(candidato))
                .sorted((a, b) -> Double.compare(
                    calcularCompatibilidad(b, partido),
                    calcularCompatibilidad(a, partido)
                ))
                .collect(Collectors.toList());
    }

    @Override
    public double calcularCompatibilidad(Cuenta jugador, Partido partido) {
        Objects.requireNonNull(jugador, "El jugador no puede ser nulo");
        Objects.requireNonNull(partido, "El partido no puede ser nulo");

        double compatibilidad = 0.0;

        // Compatibilidad por deporte (20%)
        if (jugador.getDeporteFavorito() != null && 
            jugador.getDeporteFavorito().equals(partido.getTipoDeporte())) {
            compatibilidad += 0.2;
        }

        // Compatibilidad por experiencia en el deporte (40%)
        double experienciaDeporte = calcularExperienciaEnDeporte(jugador, partido.getTipoDeporte());
        compatibilidad += experienciaDeporte * 0.4;

        // Compatibilidad por historial con otros jugadores (40%)
        double historialConJugadores = calcularHistorialConJugadores(jugador, partido);
        compatibilidad += historialConJugadores * 0.4;

        return Math.min(1.0, compatibilidad);
    }

    @Override
    public String obtenerNombre() {
        return "EMPAREJAMIENTO_POR_HISTORIAL";
    }

    /**
     * Calcula la experiencia del jugador en un deporte específico.
     */
    private double calcularExperienciaEnDeporte(Cuenta jugador, TipoDeporte deporte) {
        List<Partido> partidosEnDeporte = jugador.getPartidos().stream()
                .filter(partido -> partido.getTipoDeporte().equals(deporte))
                .collect(Collectors.toList());

        if (partidosEnDeporte.isEmpty()) {
            return 0.0; // Sin experiencia
        }

        // Calcular puntuación basada en cantidad y variedad de partidos
        int totalPartidos = partidosEnDeporte.size();
        long partidosCompletados = partidosEnDeporte.stream()
                .filter(partido -> partido.getEstadoNombre() == Partido.EstadoEnum.FINALIZADO)
                .count();

        // Puntuación: partidos completados / total partidos, con bonus por cantidad
        double tasaCompletados = totalPartidos > 0 ? (double) partidosCompletados / totalPartidos : 0.0;
        double bonusCantidad = Math.min(1.0, totalPartidos / 10.0); // Máximo bonus con 10+ partidos

        return Math.min(1.0, tasaCompletados * 0.7 + bonusCantidad * 0.3);
    }

    /**
     * Calcula la compatibilidad basada en el historial con otros jugadores del partido.
     */
    private double calcularHistorialConJugadores(Cuenta jugador, Partido partido) {
        List<Cuenta> jugadoresEnPartido = partido.getJugadores();
        
        if (jugadoresEnPartido.isEmpty()) {
            return 0.5; // Neutral si no hay otros jugadores
        }

        double sumaCompatibilidad = 0.0;
        int contadorJugadores = 0;

        for (Cuenta otroJugador : jugadoresEnPartido) {
            if (!otroJugador.equals(jugador)) {
                double compatibilidadConJugador = calcularCompatibilidadConJugador(jugador, otroJugador);
                sumaCompatibilidad += compatibilidadConJugador;
                contadorJugadores++;
            }
        }

        return contadorJugadores > 0 ? sumaCompatibilidad / contadorJugadores : 0.5;
    }

    /**
     * Calcula la compatibilidad entre dos jugadores basada en partidos previos juntos.
     */
    private double calcularCompatibilidadConJugador(Cuenta jugador1, Cuenta jugador2) {
        // Encontrar partidos en común
        List<Partido> partidosEnComun = jugador1.getPartidos().stream()
                .filter(partido -> jugador2.getPartidos().contains(partido))
                .collect(Collectors.toList());

        if (partidosEnComun.isEmpty()) {
            return 0.5; // Neutral si no hay historial
        }

        // Evaluar el historial
        long partidosCompletadosJuntos = partidosEnComun.stream()
                .filter(partido -> partido.getEstadoNombre() == Partido.EstadoEnum.FINALIZADO)
                .count();

        long partidosCanceladosJuntos = partidosEnComun.stream()
                .filter(partido -> partido.getEstadoNombre() == Partido.EstadoEnum.CANCELADO)
                .count();

        // Puntuación positiva por partidos completados, negativa por cancelados
        double puntuacion = 0.5; // Base neutral
        
        if (partidosEnComun.size() > 0) {
            double tasaCompletados = (double) partidosCompletadosJuntos / partidosEnComun.size();
            double tasaCancelados = (double) partidosCanceladosJuntos / partidosEnComun.size();
            
            puntuacion += (tasaCompletados * 0.3) - (tasaCancelados * 0.2);
        }

        return Math.max(0.0, Math.min(1.0, puntuacion));
    }

    /**
     * Obtiene estadísticas del historial de un jugador.
     */
    public HistorialEstadisticas obtenerEstadisticas(Cuenta jugador) {
        Objects.requireNonNull(jugador, "El jugador no puede ser nulo");

        int totalPartidos = jugador.getPartidos().size();
        long partidosCompletados = jugador.getPartidos().stream()
                .filter(partido -> partido.getEstadoNombre() == Partido.EstadoEnum.FINALIZADO)
                .count();
        long partidosCancelados = jugador.getPartidos().stream()
                .filter(partido -> partido.getEstadoNombre() == Partido.EstadoEnum.CANCELADO)
                .count();

        return new HistorialEstadisticas(totalPartidos, (int) partidosCompletados, (int) partidosCancelados);
    }

    /**
     * Clase interna para encapsular estadísticas del historial.
     */
    public static class HistorialEstadisticas {
        private final int totalPartidos;
        private final int partidosCompletados;
        private final int partidosCancelados;

        public HistorialEstadisticas(int totalPartidos, int partidosCompletados, int partidosCancelados) {
            this.totalPartidos = totalPartidos;
            this.partidosCompletados = partidosCompletados;
            this.partidosCancelados = partidosCancelados;
        }

        public int getTotalPartidos() { return totalPartidos; }
        public int getPartidosCompletados() { return partidosCompletados; }
        public int getPartidosCancelados() { return partidosCancelados; }
        
        public double getTasaCompletados() {
            return totalPartidos > 0 ? (double) partidosCompletados / totalPartidos : 0.0;
        }

        @Override
        public String toString() {
            return String.format("HistorialEstadisticas{total=%d, completados=%d, cancelados=%d, tasa=%.2f}", 
                               totalPartidos, partidosCompletados, partidosCancelados, getTasaCompletados());
        }
    }
} 