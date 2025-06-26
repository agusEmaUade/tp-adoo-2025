package com.tp.uno.mas.encuentros.deportivos.strategy;

import com.tp.uno.mas.encuentros.deportivos.model.Equipo;
import com.tp.uno.mas.encuentros.deportivos.model.Partido;
import com.tp.uno.mas.encuentros.deportivos.model.Usuario;

import java.util.List;

public interface EstrategiaEmparejamiento {
    Equipo emparejar(List<Usuario> jugadoresDisponibles, Partido partido);
    boolean esCompatible(Usuario usuario, Partido partido);
} 