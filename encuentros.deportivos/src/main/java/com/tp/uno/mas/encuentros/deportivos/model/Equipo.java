package com.tp.uno.mas.encuentros.deportivos.model;

import java.util.ArrayList;
import java.util.List;

public class Equipo {
    private String nombre;
    private List<Usuario> jugadores;
    private Usuario capitan;
    private String nivelPromedio;
    private String fechaCreacion;
    private int maxJugadores;

    public Equipo() {
        this.jugadores = new ArrayList<>();
    }

    public Equipo(String nombre, int maxJugadores) {
        this.nombre = nombre;
        this.maxJugadores = maxJugadores;
        this.jugadores = new ArrayList<>();
        this.fechaCreacion = java.time.LocalDateTime.now().toString();
    }

    public void agregarJugador(Usuario jugador) {
        if (puedeAgregarJugador()) {
            jugadores.add(jugador);
            if (capitan == null) {
                asignarCapitan(jugador);
            }
            calcularNivelPromedio();
        }
    }

    public void eliminarJugador(Usuario jugador) {
        jugadores.remove(jugador);
        if (capitan != null && capitan.equals(jugador) && !jugadores.isEmpty()) {
            asignarCapitan(jugadores.get(0));
        }
        calcularNivelPromedio();
    }

    public int cantidadJugadores() {
        return jugadores.size();
    }

    public boolean estaCompleto() {
        return jugadores.size() >= maxJugadores;
    }

    public boolean puedeAgregarJugador() {
        return jugadores.size() < maxJugadores;
    }

    public void asignarCapitan(Usuario usuario) {
        if (jugadores.contains(usuario)) {
            this.capitan = usuario;
        }
    }

    public Usuario obtenerCapitan() {
        return capitan;
    }

    public String calcularNivelPromedio() {
        if (jugadores.isEmpty()) {
            return "principiante";
        }

        int sumaTotal = 0;
        for (Usuario jugador : jugadores) {
            sumaTotal += convertirNivelANumero(jugador.getNivel());
        }

        double promedio = (double) sumaTotal / jugadores.size();
        String nivelCalculado = convertirNumeroANivel(promedio);
        this.nivelPromedio = nivelCalculado;
        return nivelCalculado;
    }

    public List<Usuario> obtenerJugadores() {
        return new ArrayList<>(jugadores);
    }

    public boolean validarCapacidad(int cantidad) {
        return cantidad <= maxJugadores;
    }

    private int convertirNivelANumero(String nivel) {
        switch (nivel.toLowerCase()) {
            case "principiante": return 1;
            case "intermedio": return 2;
            case "avanzado": return 3;
            default: return 1;
        }
    }

    private String convertirNumeroANivel(double numero) {
        if (numero <= 1.3) return "principiante";
        if (numero <= 2.3) return "intermedio";
        return "avanzado";
    }

    // Getters y Setters
    public String getNombre() { return nombre; }
    public void setNombre(String nombre) { this.nombre = nombre; }

    public List<Usuario> getJugadores() { return jugadores; }
    public void setJugadores(List<Usuario> jugadores) { this.jugadores = jugadores; }

    public Usuario getCapitan() { return capitan; }
    public void setCapitan(Usuario capitan) { this.capitan = capitan; }

    public String getNivelPromedio() { return nivelPromedio; }
    public void setNivelPromedio(String nivelPromedio) { this.nivelPromedio = nivelPromedio; }

    public String getFechaCreacion() { return fechaCreacion; }
    public void setFechaCreacion(String fechaCreacion) { this.fechaCreacion = fechaCreacion; }

    public int getMaxJugadores() { return maxJugadores; }
    public void setMaxJugadores(int maxJugadores) { this.maxJugadores = maxJugadores; }

    @Override
    public String toString() {
        return "Equipo{" +
                "nombre='" + nombre + '\'' +
                ", cantidadJugadores=" + jugadores.size() +
                ", capitan=" + (capitan != null ? capitan.getNombre() : "Sin capitán") +
                ", nivelPromedio='" + nivelPromedio + '\'' +
                '}';
    }
} 