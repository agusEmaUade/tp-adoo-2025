package com.tp.uno.mas.encuentros.deportivos.strategy;

import com.tp.uno.mas.encuentros.deportivos.model.Equipo;
import com.tp.uno.mas.encuentros.deportivos.model.Partido;
import com.tp.uno.mas.encuentros.deportivos.model.Usuario;

import java.util.List;
import java.util.stream.Collectors;

public class EmparejamientoMixto implements EstrategiaEmparejamiento {
    
    private EmparejamientoPorNivel estrategiaNivel;
    private EmparejamientoPorCercania estrategiaCercania;
    private EmparejamientoPorHistorial estrategiaHistorial;

    public EmparejamientoMixto() {
        this.estrategiaNivel = new EmparejamientoPorNivel();
        this.estrategiaCercania = new EmparejamientoPorCercania();
        this.estrategiaHistorial = new EmparejamientoPorHistorial();
    }

    @Override
    public Equipo emparejar(List<Usuario> jugadoresDisponibles, Partido partido) {
        List<Usuario> candidatos = combinarCriterios(jugadoresDisponibles, partido);
        
        if (candidatos.isEmpty()) {
            return null;
        }

        // Crear equipo mixto con los mejores candidatos
        Equipo equipo = new Equipo("Equipo Mixto", partido.getCantJugadoresRequeridos() / 2);
        
        for (Usuario usuario : candidatos) {
            if (equipo.puedeAgregarJugador()) {
                equipo.agregarJugador(usuario);
            }
        }

        return equipo;
    }

    @Override
    public boolean esCompatible(Usuario usuario, Partido partido) {
        // Un jugador es compatible si cumple al menos 2 de los 3 criterios
        int criteriosCumplidos = 0;
        
        if (estrategiaNivel.esCompatible(usuario, partido)) {
            criteriosCumplidos++;
        }
        
        if (estrategiaCercania.esCompatible(usuario, partido)) {
            criteriosCumplidos++;
        }
        
        if (estrategiaHistorial.esCompatible(usuario, partido)) {
            criteriosCumplidos++;
        }
        
        return criteriosCumplidos >= 2;
    }

    private List<Usuario> combinarCriterios(List<Usuario> jugadores, Partido partido) {
        return jugadores.stream()
                .filter(usuario -> esCompatible(usuario, partido))
                .sorted((u1, u2) -> {
                    // Ordenar por puntuación de compatibilidad
                    double puntuacion1 = calcularPuntuacionCompatibilidad(u1, partido);
                    double puntuacion2 = calcularPuntuacionCompatibilidad(u2, partido);
                    return Double.compare(puntuacion2, puntuacion1); // Orden descendente
                })
                .collect(Collectors.toList());
    }

    private double calcularPuntuacionCompatibilidad(Usuario usuario, Partido partido) {
        double puntuacion = 0.0;
        
        if (estrategiaNivel.esCompatible(usuario, partido)) {
            puntuacion += 0.4;
        }
        
        if (estrategiaCercania.esCompatible(usuario, partido)) {
            puntuacion += 0.3;
        }
        
        if (estrategiaHistorial.esCompatible(usuario, partido)) {
            puntuacion += 0.3;
        }
        
        return puntuacion;
    }
} 