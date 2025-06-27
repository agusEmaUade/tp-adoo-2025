package com.tp.uno.mas.encuentros.deportivos.model;

import com.tp.uno.mas.encuentros.deportivos.state.Confirmado;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ServicioProgramador {
    private final GestorPartido gestorPartido;

    public ServicioProgramador(GestorPartido gestorPartido) {
        this.gestorPartido = gestorPartido;
    }

    /**
     * Este método simula un proceso que se ejecuta periódicamente (ej. cada minuto).
     * Revisa todos los partidos confirmados y los inicia si ha llegado su hora.
     */
    public void verificarYActualizarPartidos(List<Partido> partidos) {
        System.out.println("\n[SCHEDULER] Verificando partidos para iniciar automáticamente...");
        partidos.stream()
                .filter(p -> p.getEstadoActual() instanceof Confirmado)
                .filter(this::esHoraDeIniciar)
                .forEach(p -> {
                    System.out.println("  -> Es hora de iniciar el partido de " + p.getDeporte() + ". Iniciando...");
                    gestorPartido.iniciarPartido(p);
                });
    }

    /**
     * Verifica si la fecha y hora de un partido ya ha pasado.
     * @param partido El partido a verificar.
     * @return true si la hora del partido ya pasó, false en caso contrario.
     */
    private boolean esHoraDeIniciar(Partido partido) {
        if (partido.getFecha() == null) {
            return false;
        }

        try {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
            LocalDateTime fechaPartido = LocalDateTime.parse(partido.getFecha(), formatter);
            return LocalDateTime.now().isAfter(fechaPartido);
        } catch (Exception e) {
            // En un sistema real, esto se manejaría con un logger.
            System.err.println("ADVERTENCIA: Formato de fecha inválido para el partido. No se puede iniciar automáticamente.");
            return false;
        }
    }
} 