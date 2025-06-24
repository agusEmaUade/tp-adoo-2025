package com.tp.uno.mas.encuentros.deportivos.service;

import com.tp.uno.mas.encuentros.deportivos.model.IEstrategiaNotificacion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import java.util.Objects;

/**
 * Estrategia concreta para envío de notificaciones via Email.
 * Implementa el patrón Strategy.
 * Principio SOLID: Liskov Substitution - puede reemplazar a la interfaz.
 */
@Component
public class JavaMail implements IEstrategiaNotificacion {
    
    private static final Logger logger = LoggerFactory.getLogger(JavaMail.class);
    
    @Override
    public void enviarNotificacion(String mensaje, com.tp.uno.mas.encuentros.deportivos.model.Cuenta destinatario) {
        Objects.requireNonNull(mensaje, "El mensaje no puede ser nulo");
        
        if (mensaje.trim().isEmpty()) {
            throw new IllegalArgumentException("El mensaje no puede estar vacío");
        }
        
        try {
            // Simulación de envío via Email
            String destinatarioEmail = destinatario != null ? destinatario.getEmail() : "admin@encuentrosdeportivos.com";
            logger.info("Enviando notificación por email a {}: {}", destinatarioEmail, mensaje);
            
            // Aquí iría la lógica real de JavaMail
            // MimeMessage message = crearMensaje(mensaje, destinatario);
            // Transport.send(message);
            
            // Simulamos un pequeño delay para hacer más realista
            Thread.sleep(200);
            
            logger.info("Email enviado exitosamente a {}", destinatarioEmail);
            
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new RuntimeException("Error al enviar email", e);
        } catch (Exception e) {
            logger.error("Error al enviar email: {}", e.getMessage());
            throw new RuntimeException("Error al enviar email", e);
        }
    }
    
    @Override
    public String obtenerNombre() {
        return "JavaMail";
    }
    
    @Override
    public boolean estaDisponible() {
        // En un entorno real, verificaríamos la configuración SMTP
        // Por ahora simulamos que siempre está disponible
        return true;
    }
    
    @Override
    public void configurar(java.util.Map<String, Object> configuracion) {
        Objects.requireNonNull(configuracion, "La configuración no puede ser nula");
        
        if (configuracion.containsKey("smtpHost")) {
            String host = (String) configuracion.get("smtpHost");
            logger.info("Configurando servidor SMTP: {}", host);
        }
        
        if (configuracion.containsKey("smtpPort")) {
            Integer puerto = (Integer) configuracion.get("smtpPort");
            logger.info("Configurando puerto SMTP: {}", puerto);
        }
        
        if (configuracion.containsKey("username")) {
            String usuario = (String) configuracion.get("username");
            logger.info("Configurando usuario SMTP: {}", usuario);
        }
        
        logger.info("JavaMail configurado exitosamente");
    }
    
    /**
     * Método específico para configurar el servidor SMTP.
     * Ejemplo de funcionalidad específica de la estrategia.
     */
    public void configurarSMTP(String host, int puerto, String usuario, String contraseña) {
        Objects.requireNonNull(host, "El host no puede ser nulo");
        Objects.requireNonNull(usuario, "El usuario no puede ser nulo");
        Objects.requireNonNull(contraseña, "La contraseña no puede ser nula");
        
        logger.info("Configurando servidor SMTP: {}:{} para usuario: {}", host, puerto, usuario);
        // Lógica para configurar SMTP
    }
    
    /**
     * Método para establecer el remitente por defecto.
     */
    public void establecerRemitente(String email, String nombre) {
        Objects.requireNonNull(email, "El email no puede ser nulo");
        Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
        
        logger.info("Estableciendo remitente: {} <{}>", nombre, email);
        // Lógica para establecer remitente
    }
    
    /**
     * Crea un mensaje de email con información del destinatario.
     */
    private Object crearMensaje(String contenido, com.tp.uno.mas.encuentros.deportivos.model.Cuenta destinatario) {
        // En implementación real, crearía un MimeMessage
        // MimeMessage message = new MimeMessage(session);
        // message.setFrom(new InternetAddress(from));
        // message.setRecipients(Message.RecipientType.TO, InternetAddress.parse(destinatario.getEmail()));
        // message.setSubject("Notificación Encuentros Deportivos");
        // message.setText(contenido);
        return new Object(); // Placeholder
    }
} 