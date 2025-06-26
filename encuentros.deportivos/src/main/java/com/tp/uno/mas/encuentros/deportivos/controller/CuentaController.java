package com.tp.uno.mas.encuentros.deportivos.controller;

import com.tp.uno.mas.encuentros.deportivos.model.Ubicacion;
import com.tp.uno.mas.encuentros.deportivos.model.Usuario;

import java.util.ArrayList;
import java.util.List;

public class CuentaController {

    public List<Usuario> crearUsuarios() {
        System.out.println("--- Creando Usuarios ---");

        List<Usuario> usuarios = new ArrayList<>();

        Ubicacion ubicacion1 = new Ubicacion(-34.6037f, -58.3816f, 2.0f);
        Ubicacion ubicacion2 = new Ubicacion(-34.6118f, -58.3960f, 2.0f);
        Ubicacion ubicacion3 = new Ubicacion(-34.5928f, -58.3756f, 2.0f);

        usuarios.add(new Usuario("Juan Pérez", "juan@email.com", "123", "Fútbol", "intermedio", ubicacion1, 25, "masculino"));
        usuarios.add(new Usuario("María García", "maria@email.com", "456", "Fútbol", "principiante", ubicacion2, 22, "femenino"));
        usuarios.add(new Usuario("Carlos López", "carlos@email.com", "789", "Básquet", "avanzado", ubicacion3, 30, "masculino"));
        usuarios.add(new Usuario("Ana Martín", "ana@email.com", "101", "Tenis", "intermedio", ubicacion1, 28, "femenino"));

        System.out.println("✓ " + usuarios.size() + " usuarios creados\n");
        return usuarios;
    }
}