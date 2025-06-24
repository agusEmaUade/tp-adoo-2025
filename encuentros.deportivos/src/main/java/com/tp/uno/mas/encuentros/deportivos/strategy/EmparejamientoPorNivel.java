package com.tp.uno.mas.encuentros.deportivos.strategy;

import com.tp.uno.mas.encuentros.deportivos.model.Equipo;
import com.tp.uno.mas.encuentros.deportivos.model.Partido;
import com.tp.uno.mas.encuentros.deportivos.model.Usuario;

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
        
        String nivelObjetivo = compatibles.get(0).getNivel();
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
        
        int nivelUsuario = convertirNivelANumero(usuario.getNivel());
        int nivelMinimo = convertirNivelANumero(partido.getCriterios().getNivelMinimo());
        
        return nivelUsuario >= nivelMinimo;
    }

    private boolean validarNivelMaximo(Usuario usuario, Partido partido) {
        if (partido.getCriterios().getNivelMaximo() == null) return true;
        
        int nivelUsuario = convertirNivelANumero(usuario.getNivel());
        int nivelMaximo = convertirNivelANumero(partido.getCriterios().getNivelMaximo());
        
        return nivelUsuario <= nivelMaximo;
    }

    private int convertirNivelANumero(String nivel) {
        switch (nivel.toLowerCase()) {
            case "principiante": return 1;
            case "intermedio": return 2;
            case "avanzado": return 3;
            default: return 1;
        }
    }
} 