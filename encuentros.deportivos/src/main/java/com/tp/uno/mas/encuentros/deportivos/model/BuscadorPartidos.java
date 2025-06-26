package com.tp.uno.mas.encuentros.deportivos.model;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BuscadorPartidos {
    
    public List<Partido> buscarPartidos(List<Partido> partidosDisponibles, 
                                       Usuario usuario, 
                                       CriteriosBusqueda criterios) {
        if (partidosDisponibles == null || partidosDisponibles.isEmpty()) {
            return new ArrayList<>();
        }

        return partidosDisponibles.stream()
                .filter(partido -> criterios.cumpleCriteriosPartido(partido, usuario.getUbicacion()))
                .filter(partido -> puedeUnirseAlPartido(usuario, partido))
                .sorted(crearComparadorPartidos(usuario.getUbicacion()))
                .collect(Collectors.toList());
    }

    public List<Partido> buscarPartidosCercanos(List<Partido> partidosDisponibles, 
                                               Usuario usuario, 
                                               double radioKm) {
        CriteriosBusqueda criterios = new CriteriosBusqueda();
        criterios.setRadioMaximo(radioKm);
        criterios.setSoloPartidosDisponibles(true);
        
        return buscarPartidos(partidosDisponibles, usuario, criterios);
    }

    public List<Partido> buscarPartidosPorDeporte(List<Partido> partidosDisponibles, 
                                                 Usuario usuario, 
                                                 String deporte) {
        CriteriosBusqueda criterios = new CriteriosBusqueda();
        criterios.setDeporte(deporte);
        criterios.setSoloPartidosDisponibles(true);
        
        return buscarPartidos(partidosDisponibles, usuario, criterios);
    }

    public List<Partido> buscarPartidosQueNecesitanPocoJugadores(List<Partido> partidosDisponibles, 
                                                                Usuario usuario, 
                                                                int maxJugadoresNecesarios) {
        CriteriosBusqueda criterios = new CriteriosBusqueda();
        criterios.setMaxJugadoresNecesarios(maxJugadoresNecesarios);
        criterios.setSoloPartidosDisponibles(true);
        
        return buscarPartidos(partidosDisponibles, usuario, criterios);
    }

    public int contarJugadoresNecesarios(Partido partido) {
        int jugadoresActuales = partido.getEquipos().stream()
                .mapToInt(Equipo::cantidadJugadores)
                .sum();
        return Math.max(0, partido.getCantJugadoresRequeridos() - jugadoresActuales);
    }

    public double calcularDistanciaAlPartido(Usuario usuario, Partido partido) {
        if (usuario.getUbicacion() == null || partido.getUbicacion() == null) {
            return Double.MAX_VALUE;
        }
        return usuario.getUbicacion().calcularDistancia(partido.getUbicacion());
    }

    private boolean puedeUnirseAlPartido(Usuario usuario, Partido partido) {
        // Verificar que el partido acepte nuevos jugadores
        if (!partido.getEstadoActual().puedeAgregarJugador()) {
            return false;
        }

        // Verificar que el usuario cumpla los criterios del partido
        if (partido.getCriterios() != null) {
            return partido.getCriterios().cumpleCriterios(usuario);
        }

        return true;
    }

    private Comparator<Partido> crearComparadorPartidos(Ubicacion ubicacionUsuario) {
        return (p1, p2) -> {
            // Priorizar por:
            // 1. Partidos que necesitan menos jugadores (más próximos a completarse)
            int jugadoresNecesarios1 = contarJugadoresNecesarios(p1);
            int jugadoresNecesarios2 = contarJugadoresNecesarios(p2);
            
            if (jugadoresNecesarios1 != jugadoresNecesarios2) {
                return Integer.compare(jugadoresNecesarios1, jugadoresNecesarios2);
            }

            // 2. Distancia (más cercanos primero)
            if (ubicacionUsuario != null) {
                double distancia1 = p1.getUbicacion() != null ? 
                    ubicacionUsuario.calcularDistancia(p1.getUbicacion()) : Double.MAX_VALUE;
                double distancia2 = p2.getUbicacion() != null ? 
                    ubicacionUsuario.calcularDistancia(p2.getUbicacion()) : Double.MAX_VALUE;
                
                return Double.compare(distancia1, distancia2);
            }

            return 0;
        };
    }

    public List<Partido> filtrarPorCompatibilidadNivel(List<Partido> partidos, Usuario usuario) {
        return partidos.stream()
                .filter(partido -> {
                    if (partido.getCriterios() == null) return true;
                    return partido.getCriterios().cumpleCriterios(usuario);
                })
                .collect(Collectors.toList());
    }

    public List<Partido> ordenarPorPrioridad(List<Partido> partidos, Usuario usuario) {
        return partidos.stream()
                .sorted(crearComparadorPartidos(usuario.getUbicacion()))
                .collect(Collectors.toList());
    }
} 