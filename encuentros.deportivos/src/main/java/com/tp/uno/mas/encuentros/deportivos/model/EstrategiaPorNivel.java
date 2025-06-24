package com.tp.uno.mas.encuentros.deportivos.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Estrategia de emparejamiento basada en el nivel de juego de los jugadores.
 * Patrón Strategy - implementación concreta para compatibilidad por habilidad.
 */
public class EstrategiaPorNivel implements IEstrategiaEmparejamiento {

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

        // Compatibilidad por deporte (30%)
        if (jugador.getDeporteFavorito() != null && 
            jugador.getDeporteFavorito().equals(partido.getTipoDeporte())) {
            compatibilidad += 0.3;
        }

        // Compatibilidad por nivel de juego (70%)
        if (jugador.getNivel() != null) {
            // Obtener el nivel promedio de los jugadores ya en el partido
            NivelDeJuego nivelPromedio = calcularNivelPromedio(partido);
            
            if (nivelPromedio != null) {
                compatibilidad += calcularCompatibilidadNivel(jugador.getNivel(), nivelPromedio) * 0.7;
            } else {
                // Si no hay nivel promedio definido, cualquier nivel es compatible
                compatibilidad += 0.5;
            }
        } else {
            // Si el jugador no tiene nivel definido, compatibilidad media
            compatibilidad += 0.3;
        }

        return Math.min(1.0, compatibilidad);
    }

    @Override
    public String obtenerNombre() {
        return "EMPAREJAMIENTO_POR_NIVEL";
    }

    /**
     * Calcula la compatibilidad entre dos niveles de juego.
     */
    private double calcularCompatibilidadNivel(NivelDeJuego nivel1, NivelDeJuego nivel2) {
        if (nivel1 == nivel2) {
            return 1.0; // Perfecta compatibilidad
        }
        
        int diferencia = Math.abs(nivel1.ordinal() - nivel2.ordinal());
        return Math.max(0.0, 1.0 - (diferencia * 0.3));
    }

    /**
     * Calcula el nivel promedio de los jugadores ya en el partido.
     */
    private NivelDeJuego calcularNivelPromedio(Partido partido) {
        List<NivelDeJuego> niveles = partido.getJugadores().stream()
                .map(Cuenta::getNivel)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());

        if (niveles.isEmpty()) {
            return null;
        }

        double promedioOrdinal = niveles.stream()
                .mapToInt(Enum::ordinal)
                .average()
                .orElse(0.0);

        // Redondear al nivel más cercano
        int indicePromedio = (int) Math.round(promedioOrdinal);
        NivelDeJuego[] valores = NivelDeJuego.values();
        
        return indicePromedio < valores.length ? valores[indicePromedio] : NivelDeJuego.INTERMEDIO;
    }

    /**
     * Verifica si dos niveles son compatibles.
     */
    private boolean esNivelCompatible(NivelDeJuego nivel1, NivelDeJuego nivel2) {
        if (nivel1 == null || nivel2 == null) {
            return true; // Sin restricciones si alguno es null
        }
        
        // Los niveles son compatibles si la diferencia es máximo 1
        return Math.abs(nivel1.ordinal() - nivel2.ordinal()) <= 1;
    }
} 