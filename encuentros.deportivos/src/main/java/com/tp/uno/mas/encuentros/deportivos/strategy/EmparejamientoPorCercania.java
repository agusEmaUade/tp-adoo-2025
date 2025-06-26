package com.tp.uno.mas.encuentros.deportivos.strategy;

import com.tp.uno.mas.encuentros.deportivos.model.*;

import java.util.List;
import java.util.stream.Collectors;

public class EmparejamientoPorCercania implements EstrategiaEmparejamiento {

    @Override
    public Equipo emparejar(List<Usuario> jugadoresDisponibles, Partido partido) {
        List<Usuario> compatibles = jugadoresDisponibles.stream()
                .filter(usuario -> esCompatible(usuario, partido))
                .collect(Collectors.toList());

        List<Usuario> cercanos = filtrarPorRadio(compatibles, partido.getUbicacion());

        if (cercanos.isEmpty()) {
            return null;
        }

        // Crear equipo con jugadores cercanos
        Equipo equipo = new Equipo("Equipo Local", partido.getCantJugadoresRequeridos() / 2);
        
        for (Usuario usuario : cercanos) {
            if (equipo.puedeAgregarJugador()) {
                equipo.agregarJugador(usuario);
            }
        }

        return equipo;
    }

    @Override
    public boolean esCompatible(Usuario usuario, Partido partido) {
        double distancia = calcularDistancia(usuario.getUbicacion(), partido.getUbicacion());
        double radioPermitido = partido.getUbicacion().getRadio();
        
        if (radioPermitido <= 0) radioPermitido = 10.0; // Radio por defecto 10km
        
        return distancia <= radioPermitido;
    }

    private double calcularDistancia(Ubicacion ubicacion1, Ubicacion ubicacion2) {
        return ubicacion1.calcularDistancia(ubicacion2);
    }

    private List<Usuario> filtrarPorRadio(List<Usuario> jugadores, Ubicacion ubicacion) {
        return jugadores.stream()
                .filter(jugador -> {
                    double distancia = calcularDistancia(jugador.getUbicacion(), ubicacion);
                    double radio = ubicacion.getRadio() > 0 ? ubicacion.getRadio() : 10.0;
                    return distancia <= radio;
                })
                .collect(Collectors.toList());
    }
} 