package com.tp.uno.mas.encuentros.deportivos.adapter;

public interface ServicioEmail {
    boolean enviarEmail(String destinatario, String asunto, String mensaje);
} 