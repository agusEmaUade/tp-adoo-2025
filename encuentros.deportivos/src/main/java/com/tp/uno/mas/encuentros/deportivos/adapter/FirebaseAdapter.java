package com.tp.uno.mas.encuentros.deportivos.adapter;

import com.tp.uno.mas.encuentros.deportivos.model.Usuario;

public class FirebaseAdapter implements ServicioPush {
    private String firebaseApp;

    public FirebaseAdapter() {
        this.firebaseApp = "EncuentrosDeportivos";
    }

    @Override
    public boolean enviarPush(Usuario usuario, String titulo, String mensaje) {
        try {
            String token = obtenerTokenUsuario(usuario);
            String mensajeFirebase = construirMensajeFirebase(token, titulo, mensaje);
            
            // Simulación de envío push
            System.out.println("Enviando push notification a: " + usuario.getNombre());
            System.out.println("Token: " + token);
            System.out.println("Título: " + titulo);
            System.out.println("Mensaje: " + mensaje);
            System.out.println("Push notification enviada exitosamente.");
            return true;
        } catch (Exception e) {
            System.err.println("Error al enviar push notification: " + e.getMessage());
            return false;
        }
    }

    private String obtenerTokenUsuario(Usuario usuario) {
        // Simulación de obtención de token FCM
        return "fcm_token_" + usuario.getEmail().hashCode();
    }

    private String construirMensajeFirebase(String token, String titulo, String mensaje) {
        return "Token: " + token + ", Título: " + titulo + ", Mensaje: " + mensaje;
    }
} 