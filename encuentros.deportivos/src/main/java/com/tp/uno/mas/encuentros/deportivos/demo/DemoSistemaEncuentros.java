package com.tp.uno.mas.encuentros.deportivos.demo;

import com.tp.uno.mas.encuentros.deportivos.controller.CuentaController;
import com.tp.uno.mas.encuentros.deportivos.controller.PartidoController;
import com.tp.uno.mas.encuentros.deportivos.model.Usuario;

import java.util.List;

public class DemoSistemaEncuentros {

    public static void main(String[] args) {
        System.out.println("=== DEMO: Sistema de Encuentros Deportivos ===\n");

        CuentaController cuentaController = new CuentaController();
        List<Usuario> usuarios = cuentaController.crearUsuarios();

        PartidoController partidoController = new PartidoController();
        partidoController.iniciarSimulacionPartidos(usuarios);

        System.out.println("\n=== FIN DEL DEMO ===");
    }
}