package com.tp.uno.mas.encuentros.deportivos.strategy;

import com.tp.uno.mas.encuentros.deportivos.model.Equipo;
import com.tp.uno.mas.encuentros.deportivos.model.Partido;
import com.tp.uno.mas.encuentros.deportivos.model.Usuario;
import com.tp.uno.mas.encuentros.deportivos.model.nivel.Nivel;

import java.util.List;
import java.util.stream.Collectors;

public class EmparejamientoPorNivel implements EstrategiaEmparejamiento {

    @Override
    public Equipo emparejar(List<Usuario> jugadoresDisponibles, Partido partido) {
        List<Usuario> compatibles = jugadoresDisponibles.stream()
                .filter(usuario -> esCompatible(usuario, partido))
                .collect(Collectors.toList());

        if (compatibles.isEmpty()) {
            return null;
        }

        // Crear equipo con jugadores del mismo nivel
        Equipo equipo = new Equipo("Equipo " + compatibles.get(0).getNivel(), 
                                   partido.getCantJugadoresRequeridos() / 2);
        
        Nivel nivelObjetivo = compatibles.get(0).getNivel();
        for (Usuario usuario : compatibles) {
            if (usuario.getNivel().equals(nivelObjetivo) && equipo.puedeAgregarJugador()) {
                equipo.agregarJugador(usuario);
            }
        }

        return equipo;
    }

    @Override
    public boolean esCompatible(Usuario usuario, Partido partido) {
        if (partido.getCriterios() != null) {
            return validarNivelMinimo(usuario, partido) && 
                   validarNivelMaximo(usuario, partido);
        }
        return true;
    }

    private boolean validarNivelMinimo(Usuario usuario, Partido partido) {
        if (partido.getCriterios().getNivelMinimo() == null) return true;
        
        Nivel nivelUsuario = usuario.getNivel();
        Nivel nivelMinimo = Nivel.desde(partido.getCriterios().getNivelMinimo());
        
        return nivelUsuario.getValor() >= nivelMinimo.getValor();
    }

    private boolean validarNivelMaximo(Usuario usuario, Partido partido) {
        if (partido.getCriterios().getNivelMaximo() == null) return true;
        
        Nivel nivelUsuario = usuario.getNivel();
        Nivel nivelMaximo = Nivel.desde(partido.getCriterios().getNivelMaximo());
        
        return nivelUsuario.getValor() <= nivelMaximo.getValor();
    }
} 