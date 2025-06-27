package com.tp.uno.mas.encuentros.deportivos.factory;

import com.tp.uno.mas.encuentros.deportivos.model.Partido;
import com.tp.uno.mas.encuentros.deportivos.model.Ubicacion;
import com.tp.uno.mas.encuentros.deportivos.model.Usuario;

public class BasquetFactory extends PartidoFactory {

    @Override
    public Partido crearPartido(String fecha, Ubicacion ubicacion, Usuario organizador) {
        return new Partido(fecha, "Básquet", 10, 48, ubicacion, organizador);
    }

    @Override
    public void configurarReglas(Partido partido) {
        // Configuración específica para básquet
        partido.setCantJugadoresRequeridos(10); // 5 vs 5
        partido.setDuracion(48); // 48 minutos (4 cuartos de 12)
        
        // Crear equipos
        partido.crearEquipo("Equipo A", 5);
        partido.crearEquipo("Equipo B", 5);
        
        System.out.println("Configurado partido de básquet: 10 jugadores, 48 minutos");
    }
} 