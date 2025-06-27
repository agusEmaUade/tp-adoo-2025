package com.tp.uno.mas.encuentros.deportivos.strategy;

import com.tp.uno.mas.encuentros.deportivos.model.Partido;
import com.tp.uno.mas.encuentros.deportivos.model.Usuario;

import java.util.List;
import java.util.stream.Collectors;

public class BusquedaPorDeporte implements EstrategiaBusqueda {
    private String deporteBuscado;

    public BusquedaPorDeporte() {
        // Si no se especifica deporte, se usará el deporte favorito del usuario
        this.deporteBuscado = null;
    }

    public BusquedaPorDeporte(String deporte) {
        this.deporteBuscado = deporte;
    }

    @Override
    public List<Partido> buscar(List<Partido> partidos, Usuario usuario) {
        return partidos.stream()
                .filter(partido -> cumpleCriterios(partido, usuario))
                .filter(partido -> partido.getEstadoActual().puedeAgregarJugador())
                .collect(Collectors.toList());
    }

    @Override
    public boolean cumpleCriterios(Partido partido, Usuario usuario) {
        String deporteABuscar = deporteBuscado != null ? 
                               deporteBuscado : 
                               usuario.getDeporteFavorito();
        
        return partido.getDeporte().equalsIgnoreCase(deporteABuscar);
    }

    public void setDeporteBuscado(String deporte) {
        this.deporteBuscado = deporte;
    }
} 