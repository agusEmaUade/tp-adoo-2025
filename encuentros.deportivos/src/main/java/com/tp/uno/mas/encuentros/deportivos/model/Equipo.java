package com.tp.uno.mas.encuentros.deportivos.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter // Genera todos los getters automáticamente
@Setter // Genera todos los setters automáticamente
@ToString(exclude = {"jugadores"}) // Genera toString excluyendo la lista de jugadores para evitar loops
@NoArgsConstructor // Genera constructor sin parámetros
public class Equipo {
    private String nombre;
    private List<Usuario> jugadores;
    private Usuario capitan;
    private String nivelPromedio;
    private String fechaCreacion;
    private int maxJugadores;

    // Constructor personalizado (el @NoArgsConstructor ya genera el constructor vacío)
    public Equipo(String nombre, int maxJugadores) {
        this.nombre = nombre;
        this.maxJugadores = maxJugadores;
        this.jugadores = new ArrayList<>();
        this.fechaCreacion = java.time.LocalDateTime.now().toString();
    }
    
    // Inicializar la lista en el constructor sin parámetros
    {
        this.jugadores = new ArrayList<>();
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
            sumaTotal += jugador.getNivel().getValor(); // Usar directamente el valor del Enum
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

    // Getters, Setters y toString generados automáticamente por las anotaciones @Getter, @Setter, @ToString
} 