package com.tp.uno.mas.encuentros.deportivos.service;

import com.tp.uno.mas.encuentros.deportivos.model.IObservador;
import com.tp.uno.mas.encuentros.deportivos.model.IEstrategiaNotificacion;
import com.tp.uno.mas.encuentros.deportivos.model.Cuenta;
import com.tp.uno.mas.encuentros.deportivos.model.Partido;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

/**
 * Observador específico para envío de notificaciones automáticas.
 * Patrón Observer - implementación concreta que reacciona a eventos del sistema.
 * Patrón Strategy - utiliza diferentes estrategias de notificación.
 */
@Service
public class NotificadorObservador implements IObservador {

    private final Notificador notificador;

    @Autowired
    public NotificadorObservador(Notificador notificador) {
        this.notificador = Objects.requireNonNull(notificador, "El notificador no puede ser nulo");
    }

    @Override
    public void actualizar(String evento, Object datos) {
        Objects.requireNonNull(evento, "El evento no puede ser nulo");

        try {
            procesarEvento(evento, datos);
        } catch (Exception e) {
            System.err.println("Error procesando evento " + evento + ": " + e.getMessage());
            // No relanzar la excepción para no interrumpir el flujo principal
        }
    }

    @Override
    public String getTipo() {
        return "NOTIFICADOR";
    }

    /**
     * Procesa diferentes tipos de eventos y envía notificaciones apropiadas.
     */
    private void procesarEvento(String evento, Object datos) {
        String mensaje = construirMensaje(evento, datos);
        
        if (mensaje != null && !mensaje.trim().isEmpty()) {
            // Por ahora enviamos a todos los usuarios, pero se podría filtrar según el evento
            enviarNotificacion(mensaje);
        }
    }

    /**
     * Construye el mensaje de notificación basado en el evento y los datos.
     */
    private String construirMensaje(String evento, Object datos) {
        switch (evento.toUpperCase()) {
            case "PARTIDO_CREADO":
                return construirMensajePartidoCreado(datos);
                
            case "ESTADO_CAMBIADO":
                return construirMensajeEstadoCambiado(datos);
                
            case "JUGADOR_AGREGADO":
                return construirMensajeJugadorAgregado(datos);
                
            case "JUGADOR_REMOVIDO":
                return construirMensajeJugadorRemovido(datos);
                
            case "DEPORTE_FAVORITO_CAMBIADO":
                return construirMensajeDeporteCambiado(datos);
                
            case "NIVEL_CAMBIADO":
                return construirMensajeNivelCambiado(datos);
                
            default:
                return construirMensajeGenerico(evento, datos);
        }
    }

    private String construirMensajePartidoCreado(Object datos) {
        return "🏆 ¡Nuevo partido disponible! " + datos.toString() + 
               "\n¡Únete ahora y disfruta del deporte!";
    }

    private String construirMensajeEstadoCambiado(Object datos) {
        return "📢 Estado del partido actualizado: " + datos.toString();
    }

    private String construirMensajeJugadorAgregado(Object datos) {
        return "👥 " + datos.toString() + 
               "\n¡El partido está llenándose!";
    }

    private String construirMensajeJugadorRemovido(Object datos) {
        return "👤 " + datos.toString() + 
               "\n¡Se necesitan más jugadores!";
    }

    private String construirMensajeDeporteCambiado(Object datos) {
        return "⚽ Preferencia deportiva actualizada: " + datos.toString() + 
               "\n¡Buscaremos partidos de tu nuevo deporte favorito!";
    }

    private String construirMensajeNivelCambiado(Object datos) {
        return "📈 Nivel de juego actualizado: " + datos.toString() + 
               "\n¡Te emparejaremos con jugadores de nivel similar!";
    }

    private String construirMensajeGenerico(String evento, Object datos) {
        return "🔔 Evento: " + evento + 
               (datos != null ? "\nDetalles: " + datos.toString() : "");
    }

    /**
     * Envía la notificación usando el notificador configurado.
     */
    private void enviarNotificacion(String mensaje) {
        try {
            if (notificador.estaListo()) {
                // Por ahora enviamos sin destinatario específico
                // En una implementación real, se determinaría el destinatario según el evento
                notificador.enviarNotificacion(mensaje, null);
            } else {
                System.out.println("Notificador no está listo. Mensaje: " + mensaje);
            }
        } catch (Exception e) {
            System.err.println("Error enviando notificación: " + e.getMessage());
        }
    }

    /**
     * Configurar el tipo de estrategia de notificación para este observador.
     */
    public void configurarEstrategiaNotificacion(IEstrategiaNotificacion estrategia) {
        notificador.cambiarEstrategia(estrategia);
    }

    /**
     * Verifica si el observador está activo y puede enviar notificaciones.
     */
    public boolean estaActivo() {
        return notificador != null && notificador.estaListo();
    }

    /**
     * Obtiene información sobre la estrategia de notificación actual.
     */
    public String getEstrategiaActual() {
        if (notificador != null && notificador.obtenerEstrategia() != null) {
            return notificador.obtenerEstrategia().obtenerNombre();
        }
        return "NO_CONFIGURADA";
    }
} 