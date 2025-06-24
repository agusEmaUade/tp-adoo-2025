package com.tp.uno.mas.encuentros.deportivos.model;

public class Usuario {
    private String nombre;
    private String email;
    private String contraseña;
    private String deporteFavorito;
    private String nivel; // principiante, intermedio, avanzado
    private Ubicacion ubicacion;
    private int edad;
    private String genero;

    public Usuario() {}

    public Usuario(String nombre, String email, String contraseña, String deporteFavorito, 
                   String nivel, Ubicacion ubicacion, int edad, String genero) {
        this.nombre = nombre;
        this.email = email;
        this.contraseña = contraseña;
        this.deporteFavorito = deporteFavorito;
        this.nivel = nivel;
        this.ubicacion = ubicacion;
        this.edad = edad;
        this.genero = genero;
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getContraseña() { return contraseña; }
    public void setContraseña(String contraseña) { this.contraseña = contraseña; }

    public String getDeporteFavorito() { return deporteFavorito; }
    public void setDeporteFavorito(String deporteFavorito) { this.deporteFavorito = deporteFavorito; }

    public String getNivel() { return nivel; }
    public void setNivel(String nivel) { this.nivel = nivel; }

    public Ubicacion getUbicacion() { return ubicacion; }
    public void setUbicacion(Ubicacion ubicacion) { this.ubicacion = ubicacion; }

    public int getEdad() { return edad; }
    public void setEdad(int edad) { this.edad = edad; }

    public String getGenero() { return genero; }
    public void setGenero(String genero) { this.genero = genero; }

    @Override
    public String toString() {
        return "Usuario{" +
                "nombre='" + nombre + '\'' +
                ", email='" + email + '\'' +
                ", deporteFavorito='" + deporteFavorito + '\'' +
                ", nivel='" + nivel + '\'' +
                ", edad=" + edad +
                ", genero='" + genero + '\'' +
                '}';
    }
} 