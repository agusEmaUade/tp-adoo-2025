package com.tp.uno.mas.encuentros.deportivos.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data // Genera getters, setters, toString, equals, hashCode automáticamente
@NoArgsConstructor // Genera constructor sin parámetros
@AllArgsConstructor // Genera constructor con todos los parámetros
public class Usuario {
    private String nombre;
    private String email;
    private String contraseña;
    private String deporteFavorito;
    private String nivel; // principiante, intermedio, avanzado
    private Ubicacion ubicacion;
    private int edad;
    private String genero;
} 