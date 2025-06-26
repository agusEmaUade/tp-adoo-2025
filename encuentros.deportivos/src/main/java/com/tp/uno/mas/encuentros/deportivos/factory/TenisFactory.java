package com.tp.uno.mas.encuentros.deportivos.factory;

import com.tp.uno.mas.encuentros.deportivos.model.Partido;
import com.tp.uno.mas.encuentros.deportivos.model.Ubicacion;
import com.tp.uno.mas.encuentros.deportivos.model.Usuario;

public class TenisFactory extends PartidoFactory {

    @Override
    public Partido crearPartido(String fecha, Ubicacion ubicacion) {
        Usuario organizador = new Usuario(); // Se asignaría después
        Partido partido = new Partido(fecha, "Tenis", 2, 90, ubicacion, organizador);
        return partido;
    }

    @Override
    public void configurarReglas(Partido partido) {
        // Configuración específica para tenis
        partido.setCantJugadoresRequeridos(2); // 1 vs 1 (singles)
        partido.setDuracion(90); // 90 minutos aproximadamente
        
        // Crear equipos (jugadores individuales)
        partido.crearEquipo("Jugador 1", 1);
        partido.crearEquipo("Jugador 2", 1);
        
        System.out.println("Configurado partido de tenis: 2 jugadores, 90 minutos");
    }
} 