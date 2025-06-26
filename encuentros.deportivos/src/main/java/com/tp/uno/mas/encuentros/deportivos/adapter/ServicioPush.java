package com.tp.uno.mas.encuentros.deportivos.adapter;

import com.tp.uno.mas.encuentros.deportivos.model.Usuario;

public interface ServicioPush {
    boolean enviarPush(Usuario usuario, String titulo, String mensaje);
} 