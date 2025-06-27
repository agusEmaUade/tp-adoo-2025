package com.tp.uno.mas.encuentros.deportivos.factory;

import com.tp.uno.mas.encuentros.deportivos.model.Partido;
import com.tp.uno.mas.encuentros.deportivos.model.Ubicacion;
import com.tp.uno.mas.encuentros.deportivos.model.Usuario;

public class VoleyFactory extends PartidoFactory {

    @Override
    public Partido crearPartido(String fecha, Ubicacion ubicacion, Usuario organizador) {
        return new Partido(fecha, "Vóley", 12, 60, ubicacion, organizador);
    }

    @Override
    public void configurarReglas(Partido partido) {
        // Configuración específica para vóley
        partido.setCantJugadoresRequeridos(12); // 6 vs 6
        partido.setDuracion(60); // 60 minutos aproximadamente
        
        // Crear equipos
        partido.crearEquipo("Equipo 1", 6);
        partido.crearEquipo("Equipo 2", 6);
        
        System.out.println("Configurado partido de vóley: 12 jugadores, 60 minutos");
    }
} 