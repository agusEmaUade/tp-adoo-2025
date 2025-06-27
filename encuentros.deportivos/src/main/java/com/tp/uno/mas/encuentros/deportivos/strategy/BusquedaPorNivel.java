package com.tp.uno.mas.encuentros.deportivos.strategy;

import com.tp.uno.mas.encuentros.deportivos.model.Partido;
import com.tp.uno.mas.encuentros.deportivos.model.Usuario;
import com.tp.uno.mas.encuentros.deportivos.model.nivel.Nivel;

import java.util.List;
import java.util.stream.Collectors;

public class BusquedaPorNivel implements EstrategiaBusqueda {

    @Override
    public List<Partido> buscar(List<Partido> partidos, Usuario usuario) {
        return partidos.stream()
                .filter(partido -> cumpleCriterios(partido, usuario))
                .filter(partido -> partido.getEstadoActual().puedeAgregarJugador())
                .collect(Collectors.toList());
    }

    @Override
    public boolean cumpleCriterios(Partido partido, Usuario usuario) {
        if (partido.getCriterios() == null) {
            return true; // Si no hay criterios, cualquier nivel es válido
        }

        return validarNivelMinimo(usuario, partido) && 
               validarNivelMaximo(usuario, partido);
    }

    private boolean validarNivelMinimo(Usuario usuario, Partido partido) {
        if (partido.getCriterios().getNivelMinimo() == null) {
            return true;
        }
        
        Nivel nivelUsuario = usuario.getNivel();
        Nivel nivelMinimo = Nivel.desde(partido.getCriterios().getNivelMinimo());
        
        return nivelUsuario.getValor() >= nivelMinimo.getValor();
    }

    private boolean validarNivelMaximo(Usuario usuario, Partido partido) {
        if (partido.getCriterios().getNivelMaximo() == null) {
            return true;
        }
        
        Nivel nivelUsuario = usuario.getNivel();
        Nivel nivelMaximo = Nivel.desde(partido.getCriterios().getNivelMaximo());
        
        return nivelUsuario.getValor() <= nivelMaximo.getValor();
    }
} 