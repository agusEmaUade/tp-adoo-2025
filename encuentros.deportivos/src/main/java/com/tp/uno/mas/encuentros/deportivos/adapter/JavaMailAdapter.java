package com.tp.uno.mas.encuentros.deportivos.adapter;

import java.util.Properties;

public class JavaMailAdapter implements ServicioEmail {
    private Properties session;

    public JavaMailAdapter() {
        this.session = configurarSMTP();
    }

    @Override
    public boolean enviarEmail(String destinatario, String asunto, String mensaje) {
        try {
            // Simulación de envío de email
            System.out.println("Enviando email a: " + destinatario);
            System.out.println("Asunto: " + asunto);
            System.out.println("Mensaje: " + mensaje);
            System.out.println("Email enviado exitosamente.");
            return true;
        } catch (Exception e) {
            System.err.println("Error al enviar email: " + e.getMessage());
            return false;
        }
    }

    private Properties configurarSMTP() {
        Properties props = new Properties();
        props.put("mail.smtp.host", "smtp.gmail.com");
        props.put("mail.smtp.port", "587");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        return props;
    }

    private String crearMensajeEmail(String destinatario, String asunto, String mensaje) {
        return "Para: " + destinatario + "\nAsunto: " + asunto + "\nMensaje: " + mensaje;
    }
} 