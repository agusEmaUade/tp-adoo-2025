package com.tp.uno.mas.encuentros.deportivos.strategy;

import com.tp.uno.mas.encuentros.deportivos.model.Partido;
import com.tp.uno.mas.encuentros.deportivos.model.Usuario;

import java.util.List;
import java.util.stream.Collectors;

public class BusquedaPorHistorial implements EstrategiaBusqueda {

    @Override
    public List<Partido> buscar(List<Partido> partidos, Usuario usuario) {
        return partidos.stream()
                .filter(partido -> cumpleCriterios(partido, usuario))
                .collect(Collectors.toList());
    }

    @Override
    public boolean cumpleCriterios(Partido partido, Usuario usuario) {
        if (usuario.getHistorialPartidos().isEmpty()) {
            return false; // No tiene historial, no puede tener compañeros en común
        }

        List<Usuario> jugadoresConocidos = usuario.getHistorialPartidos().stream()
                .flatMap(p -> p.getJugadoresActuales().stream())
                .distinct()
                .collect(Collectors.toList());

        return partido.getJugadoresActuales().stream()
                .anyMatch(jugadorEnPartido -> jugadoresConocidos.contains(jugadorEnPartido) && !jugadorEnPartido.equals(usuario));
    }
} 