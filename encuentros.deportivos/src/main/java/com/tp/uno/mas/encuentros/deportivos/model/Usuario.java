package com.tp.uno.mas.encuentros.deportivos.model;

import com.tp.uno.mas.encuentros.deportivos.model.nivel.Nivel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.util.List;
import java.util.ArrayList;

@Data // Genera getters, setters, toString, equals, hashCode automáticamente
@NoArgsConstructor // Genera constructor sin parámetros
@AllArgsConstructor // Genera constructor con todos los parámetros
public class Usuario {
    private String nombre;
    private String email;
    private String contraseña;
    private String deporteFavorito;
    private Nivel nivel;
    private Ubicacion ubicacion;
    private int edad;
    private String genero;
    private List<Partido> historialPartidos = new ArrayList<>();
    
    // Constructor de conveniencia que acepta String para el nivel
    public Usuario(String nombre, String email, String contraseña, String deporteFavorito, 
                   String nivelString, Ubicacion ubicacion, int edad, String genero) {
        this.nombre = nombre;
        this.email = email;
        this.contraseña = contraseña;
        this.deporteFavorito = deporteFavorito;
        this.nivel = Nivel.desde(nivelString);
        this.ubicacion = ubicacion;
        this.edad = edad;
        this.genero = genero;
        this.historialPartidos = new ArrayList<>();
    }
    
    // Override del getter generado por Lombok para devolver Nivel directamente
    public Nivel getNivel() {
        return nivel != null ? nivel : Nivel.PRINCIPIANTE;
    }
    
    // Setter que acepta String
    public void setNivel(String nivelString) {
        this.nivel = Nivel.desde(nivelString);
    }
    
    public void agregarAPartidoHistorial(Partido partido) {
        if (partido != null && !this.historialPartidos.contains(partido)) {
            this.historialPartidos.add(partido);
        }
    }
} 