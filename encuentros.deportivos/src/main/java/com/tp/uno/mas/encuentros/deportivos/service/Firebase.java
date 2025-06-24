package com.tp.uno.mas.encuentros.deportivos.service;

import com.tp.uno.mas.encuentros.deportivos.model.IEstrategiaNotificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Objects;

/**
 * Estrategia concreta para envío de notificaciones via Firebase.
 * Implementa el patrón Strategy.
 * Principio SOLID: Liskov Substitution - puede reemplazar a la interfaz.
 */
@Component
public class Firebase implements IEstrategiaNotificacion {
    
    private static final Logger logger = LoggerFactory.getLogger(Firebase.class);
    
    @Override
    public void enviarNotificacion(String mensaje, com.tp.uno.mas.encuentros.deportivos.model.Cuenta destinatario) {
        Objects.requireNonNull(mensaje, "El mensaje no puede ser nulo");
        
        if (mensaje.trim().isEmpty()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío");
        }
        
        try {
            // Simulación de envío via Firebase
            String destinatarioInfo = destinatario != null ? destinatario.getEmail() : "BROADCAST";
            logger.info("Enviando notificación push via Firebase a {}: {}", destinatarioInfo, mensaje);
            
            // Aquí iría la lógica real de Firebase
            // FirebaseMessaging.getInstance().send(prepararPayload(mensaje, destinatario));
            
            // Simulamos un pequeño delay para hacer más realista
            Thread.sleep(100);
            
            logger.info("Notificación Firebase enviada exitosamente a {}", destinatarioInfo);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error al enviar notificación Firebase", e);
        } catch (Exception e) {
            logger.error("Error al enviar notificación Firebase: {}", e.getMessage());
            throw new RuntimeException("Error al enviar notificación Firebase", e);
        }
    }
    
    @Override
    public String obtenerNombre() {
        return "Firebase";
    }
    
    @Override
    public boolean estaDisponible() {
        // En un entorno real, verificaríamos la conectividad con Firebase
        // Por ahora simulamos que siempre está disponible
        return true;
    }
    
    @Override
    public void configurar(java.util.Map<String, Object> configuracion) {
        Objects.requireNonNull(configuracion, "La configuración no puede ser nula");
        
        if (configuracion.containsKey("apiKey")) {
            String apiKey = (String) configuracion.get("apiKey");
            logger.info("Configurando API Key Firebase: {}...", apiKey.substring(0, Math.min(10, apiKey.length())));
        }
        
        if (configuracion.containsKey("projectId")) {
            String projectId = (String) configuracion.get("projectId");
            logger.info("Configurando Project ID Firebase: {}", projectId);
        }
        
        logger.info("Firebase configurado exitosamente");
    }

    /**
     * Método específico para configurar el token de Firebase.
     * Ejemplo de funcionalidad específica de la estrategia.
     */
    public void configurarToken(String token) {
        Objects.requireNonNull(token, "El token no puede ser nulo");
        logger.info("Configurando token Firebase: {}", token.substring(0, Math.min(10, token.length())) + "...");
        // Lógica para configurar el token
    }
    
    /**
     * Prepara el payload para Firebase con información del destinatario.
     */
    private Object prepararPayload(String mensaje, com.tp.uno.mas.encuentros.deportivos.model.Cuenta destinatario) {
        // En implementación real, construiría el payload de Firebase
        return new Object(); // Placeholder
    }
} 