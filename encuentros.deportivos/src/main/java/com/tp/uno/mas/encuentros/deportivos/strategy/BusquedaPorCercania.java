package com.tp.uno.mas.encuentros.deportivos.strategy;

import com.tp.uno.mas.encuentros.deportivos.model.Partido;
import com.tp.uno.mas.encuentros.deportivos.model.Usuario;
import com.tp.uno.mas.encuentros.deportivos.model.Ubicacion;

import java.util.List;
import java.util.stream.Collectors;

public class BusquedaPorCercania implements EstrategiaBusqueda {
    private final double radioKm;

    public BusquedaPorCercania() {
        // Usar el radio definido en la ubicación del partido o un radio por defecto si no se especifica.
        this.radioKm = -1; // -1 indica que se debe usar el radio del partido.
    }

    public BusquedaPorCercania(double radioKm) {
        this.radioKm = radioKm;
    }

    @Override
    public List<Partido> buscar(List<Partido> partidos, Usuario usuario) {
        return partidos.stream()
                .filter(partido -> cumpleCriterios(partido, usuario))
                .filter(partido -> partido.getEstadoActual().puedeAgregarJugador())
                .sorted((p1, p2) -> {
                    // Ordenar por distancia ascendente
                    double dist1 = calcularDistancia(usuario.getUbicacion(), p1.getUbicacion());
                    double dist2 = calcularDistancia(usuario.getUbicacion(), p2.getUbicacion());
                    return Double.compare(dist1, dist2);
                })
                .collect(Collectors.toList());
    }

    @Override
    public boolean cumpleCriterios(Partido partido, Usuario usuario) {
        double distancia = calcularDistancia(usuario.getUbicacion(), partido.getUbicacion());
        double radioPermitido;

        if (this.radioKm > 0) {
            // Si se especificó un radio en la búsqueda, se usa ese.
            radioPermitido = this.radioKm;
        } else {
            // Si no, se usa el radio definido en el propio partido.
            radioPermitido = partido.getUbicacion().getRadio();
        }
        
        return distancia <= radioPermitido;
    }

    private double calcularDistancia(Ubicacion ubicacion1, Ubicacion ubicacion2) {
        if (ubicacion1 == null || ubicacion2 == null) {
            return Double.MAX_VALUE;
        }
        return ubicacion1.calcularDistancia(ubicacion2);
    }
} 