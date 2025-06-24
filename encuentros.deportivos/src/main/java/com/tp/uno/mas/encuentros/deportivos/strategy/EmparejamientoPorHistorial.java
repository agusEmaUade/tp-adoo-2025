package com.tp.uno.mas.encuentros.deportivos.strategy;

import com.tp.uno.mas.encuentros.deportivos.model.Equipo;
import com.tp.uno.mas.encuentros.deportivos.model.Partido;
import com.tp.uno.mas.encuentros.deportivos.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class EmparejamientoPorHistorial implements EstrategiaEmparejamiento {

    @Override
    public Equipo emparejar(List<Usuario> jugadoresDisponibles, Partido partido) {
        List<Usuario> compatibles = new ArrayList<>();
        
        for (Usuario usuario : jugadoresDisponibles) {
            if (esCompatible(usuario, partido)) {
                compatibles.add(usuario);
            }
        }

        if (compatibles.isEmpty()) {
            return null;
        }

        // Crear equipo basado en compatibilidad de historial
        Equipo equipo = new Equipo("Equipo Compatibles", partido.getCantJugadoresRequeridos() / 2);
        
        // Agregar primer jugador
        Usuario primerJugador = compatibles.get(0);
        equipo.agregarJugador(primerJugador);
        
        // Agregar jugadores compatibles con el primero
        for (int i = 1; i < compatibles.size() && equipo.puedeAgregarJugador(); i++) {
            Usuario candidato = compatibles.get(i);
            if (calcularCompatibilidad(primerJugador, candidato) > 0.5) {
                equipo.agregarJugador(candidato);
            }
        }

        return equipo;
    }

    @Override
    public boolean esCompatible(Usuario usuario, Partido partido) {
        // Verificar si tiene historial suficiente
        List<Partido> historial = obtenerHistorial(usuario);
        return historial.size() >= 1; // Al menos 1 partido jugado
    }

    private List<Partido> obtenerHistorial(Usuario usuario) {
        // Simulación de historial - en una implementación real vendría de una base de datos
        List<Partido> historial = new ArrayList<>();
        
        // Simular algunos partidos basados en el nivel del usuario
        int cantPartidos = 0;
        switch (usuario.getNivel().toLowerCase()) {
            case "principiante": cantPartidos = 2; break;
            case "intermedio": cantPartidos = 5; break;
            case "avanzado": cantPartidos = 10; break;
        }
        
        for (int i = 0; i < cantPartidos; i++) {
            // Crear partidos ficticios para el historial
            Partido partidoHistorial = new Partido();
            partidoHistorial.setDeporte(usuario.getDeporteFavorito());
            historial.add(partidoHistorial);
        }
        
        return historial;
    }

    private double calcularCompatibilidad(Usuario usuario1, Usuario usuario2) {
        double compatibilidad = 0.0;
        
        // Compatibilidad por deporte favorito
        if (usuario1.getDeporteFavorito().equals(usuario2.getDeporteFavorito())) {
            compatibilidad += 0.4;
        }
        
        // Compatibilidad por nivel
        if (usuario1.getNivel().equals(usuario2.getNivel())) {
            compatibilidad += 0.3;
        }
        
        // Compatibilidad por edad (diferencia menor a 10 años)
        int diferenciaEdad = Math.abs(usuario1.getEdad() - usuario2.getEdad());
        if (diferenciaEdad <= 10) {
            compatibilidad += 0.3;
        }
        
        return compatibilidad;
    }
} 