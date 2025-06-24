package com.tp.uno.mas.encuentros.deportivos.model;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * Estrategia de emparejamiento basada en la cercanía geográfica de los jugadores.
 * Patrón Strategy - implementación concreta para compatibilidad por ubicación.
 */
public class EstrategiaPorCercania implements IEstrategiaEmparejamiento {

    private static final double RADIO_MAXIMO_KM_DEFAULT = 10.0;
    private final double radioMaximoKm;

    public EstrategiaPorCercania() {
        this.radioMaximoKm = RADIO_MAXIMO_KM_DEFAULT;
    }

    public EstrategiaPorCercania(double radioMaximoKm) {
        this.radioMaximoKm = radioMaximoKm > 0 ? radioMaximoKm : RADIO_MAXIMO_KM_DEFAULT;
    }

    @Override
    public List<Cuenta> encontrarJugadoresCompatibles(Partido partido, List<Cuenta> candidatos) {
        Objects.requireNonNull(partido, "El partido no puede ser nulo");
        Objects.requireNonNull(candidatos, "Los candidatos no pueden ser nulos");

        Ubicacion ubicacionPartido = partido.getUbicacion();
        if (ubicacionPartido == null) {
            return candidatos; // Sin filtro si no hay ubicación
        }

        return candidatos.stream()
                .filter(candidato -> candidato.getDeporteFavorito() == null || 
                                   candidato.getDeporteFavorito().equals(partido.getTipoDeporte()))
                .filter(candidato -> !partido.getJugadores().contains(candidato))
                .filter(candidato -> {
                    if (candidato.getUbicacion() == null) {
                        return false; // Excluir si no tiene ubicación
                    }
                    double distancia = calcularDistancia(ubicacionPartido, candidato.getUbicacion());
                    return distancia <= radioMaximoKm;
                })
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

        // Compatibilidad por cercanía geográfica (80%)
        if (jugador.getUbicacion() != null && partido.getUbicacion() != null) {
            double distancia = calcularDistancia(partido.getUbicacion(), jugador.getUbicacion());
            
            if (distancia <= radioMaximoKm) {
                // Inversamente proporcional a la distancia
                double compatibilidadDistancia = Math.max(0.0, 1.0 - (distancia / radioMaximoKm));
                compatibilidad += compatibilidadDistancia * 0.8;
            }
        } else {
            // Si no hay ubicación, compatibilidad baja
            compatibilidad += 0.1;
        }

        return Math.min(1.0, compatibilidad);
    }

    @Override
    public String obtenerNombre() {
        return "EMPAREJAMIENTO_POR_CERCANIA";
    }

    /**
     * Calcula la distancia entre dos ubicaciones usando la fórmula Haversine.
     * 
     * @param ubicacion1 Primera ubicación
     * @param ubicacion2 Segunda ubicación
     * @return Distancia en kilómetros
     */
    private double calcularDistancia(Ubicacion ubicacion1, Ubicacion ubicacion2) {
        if (ubicacion1 == null || ubicacion2 == null) {
            return Double.MAX_VALUE;
        }

        Double lat1 = ubicacion1.getLatitud();
        Double lon1 = ubicacion1.getLongitud();
        Double lat2 = ubicacion2.getLatitud();
        Double lon2 = ubicacion2.getLongitud();

        if (lat1 == null || lon1 == null || lat2 == null || lon2 == null) {
            return Double.MAX_VALUE;
        }

        // Fórmula Haversine para calcular distancia entre coordenadas
        final double RADIO_TIERRA_KM = 6371.0;

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                   Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                   Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        return RADIO_TIERRA_KM * c;
    }

    /**
     * Obtiene el radio máximo de búsqueda en kilómetros.
     */
    public double getRadioMaximoKm() {
        return radioMaximoKm;
    }
} 