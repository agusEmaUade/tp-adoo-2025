package com.tp.uno.mas.encuentros.deportivos.model;

import com.tp.uno.mas.encuentros.deportivos.strategy.EstrategiaEmparejamiento;

import java.util.List;

public class Emparejador {
    private EstrategiaEmparejamiento estrategia;

    public Emparejador(EstrategiaEmparejamiento estrategia) {
        this.estrategia = estrategia;
    }

    public Equipo emparejarJugadores(List<Usuario> jugadoresDisponibles, Partido partido) {
        if (estrategia == null) {
            throw new IllegalStateException("No se ha configurado una estrategia de emparejamiento");
        }
        
        return estrategia.emparejar(jugadoresDisponibles, partido);
    }

    public void cambiarEstrategia(EstrategiaEmparejamiento nuevaEstrategia) {
        this.estrategia = nuevaEstrategia;
        System.out.println("Estrategia de emparejamiento cambiada");
    }

    public boolean esCompatible(Usuario usuario, Partido partido) {
        if (estrategia == null) {
            return true; // Sin estrategia, cualquier usuario es compatible
        }
        
        return estrategia.esCompatible(usuario, partido);
    }

    public EstrategiaEmparejamiento getEstrategia() {
        return estrategia;
    }

    public void setEstrategia(EstrategiaEmparejamiento estrategia) {
        this.estrategia = estrategia;
    }
} 