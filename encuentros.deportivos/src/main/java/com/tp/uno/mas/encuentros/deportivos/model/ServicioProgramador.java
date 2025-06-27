package com.tp.uno.mas.encuentros.deportivos.model;

import com.tp.uno.mas.encuentros.deportivos.state.Confirmado;
import java.util.List;

public class ServicioProgramador {
    private final GestorPartido gestorPartido;
    private final VerificadorHorarios verificadorHorarios;

    public ServicioProgramador(GestorPartido gestorPartido, VerificadorHorarios verificadorHorarios) {
        this.gestorPartido = gestorPartido;
        this.verificadorHorarios = verificadorHorarios;
    }

    /**
     * Este método simula un proceso que se ejecuta periódicamente (ej. cada minuto).
     * Revisa todos los partidos confirmados y los inicia si ha llegado su hora.
     */
    public void verificarYActualizarPartidos(List<Partido> partidos) {
        System.out.println("\n[SCHEDULER] Verificando partidos para iniciar automáticamente...");
        partidos.stream()
                .filter(p -> p.getEstadoActual() instanceof Confirmado)
                .filter(verificadorHorarios::esHoraDeIniciar)
                .forEach(p -> {
                    System.out.println("  -> Es hora de iniciar el partido de " + p.getDeporte() + ". Iniciando...");
                    gestorPartido.iniciarPartido(p);
                });
    }
} 