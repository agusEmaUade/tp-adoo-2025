package com.tp.uno.mas.encuentros.deportivos.model;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class VerificadorHorarios {

    /**
     * Verifica si la fecha y hora de un partido ya ha pasado.
     * @param partido El partido a verificar.
     * @return true si la hora del partido ya pasó, false en caso contrario.
     */
    public boolean esHoraDeIniciar(Partido partido) {
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