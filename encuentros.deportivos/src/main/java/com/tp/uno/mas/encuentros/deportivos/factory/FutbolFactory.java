package com.tp.uno.mas.encuentros.deportivos.factory;

import com.tp.uno.mas.encuentros.deportivos.model.Partido;
import com.tp.uno.mas.encuentros.deportivos.model.Ubicacion;
import com.tp.uno.mas.encuentros.deportivos.model.Usuario;

public class FutbolFactory extends PartidoFactory {

    @Override
    public Partido crearPartido(String fecha, Ubicacion ubicacion, Usuario organizador) {
        return new Partido(fecha, "Fútbol", 22, 90, ubicacion, organizador);
    }

    @Override
    public void configurarReglas(Partido partido) {
        // Configuración específica para fútbol
        partido.setCantJugadoresRequeridos(22); // 11 vs 11
        partido.setDuracion(90); // 90 minutos
        
        // Crear equipos
        partido.crearEquipo("Equipo Local", 11);
        partido.crearEquipo("Equipo Visitante", 11);
        
        System.out.println("Configurado partido de fútbol: 22 jugadores, 90 minutos");
    }
} 