package com.tp.uno.mas.encuentros.deportivos.factory;

import com.tp.uno.mas.encuentros.deportivos.model.Partido;
import com.tp.uno.mas.encuentros.deportivos.model.Ubicacion;

public abstract class PartidoFactory {
    
    public abstract Partido crearPartido(String fecha, Ubicacion ubicacion);
    
    public abstract void configurarReglas(Partido partido);
    
    // Método template que define el proceso de creación
    public final Partido crearPartidoCompleto(String fecha, Ubicacion ubicacion) {
        Partido partido = crearPartido(fecha, ubicacion);
        configurarReglas(partido);
        return partido;
    }
} 