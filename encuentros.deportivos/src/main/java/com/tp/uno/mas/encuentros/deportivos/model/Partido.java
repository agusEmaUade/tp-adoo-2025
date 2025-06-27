package com.tp.uno.mas.encuentros.deportivos.model;

import com.tp.uno.mas.encuentros.deportivos.state.EstadoPartido;
import com.tp.uno.mas.encuentros.deportivos.state.NecesitamosJugadores;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Getter
@Setter
public class Partido {
    private String fecha;
    private String deporte;
    private int cantJugadoresRequeridos;
    private int duracion;
    private EstadoPartido estadoActual;
    private List<Equipo> equipos;
    private Ubicacion ubicacion;
    private Usuario organizador;
    private CriteriosPartido criterios;

    public Partido() {
        this.equipos = new ArrayList<>();
        this.estadoActual = new NecesitamosJugadores();
    }

    public Partido(String fecha, String deporte, int cantJugadoresRequeridos,
                   int duracion, Ubicacion ubicacion, Usuario organizador) {
        this.fecha = fecha;
        this.deporte = deporte;
        this.cantJugadoresRequeridos = cantJugadoresRequeridos;
        this.duracion = duracion;
        this.ubicacion = ubicacion;
        this.organizador = organizador;
        this.estadoActual = new NecesitamosJugadores();
        this.equipos = new ArrayList<>();
        this.criterios = null;
    }

    public void cambiarEstado(EstadoPartido nuevoEstado) {
        this.estadoActual = nuevoEstado;
        System.out.println("Estado del partido cambiado a: " + nuevoEstado.getNombreEstado());
        // Invocamos el manejo del estado para permitir transiciones automáticas
        this.estadoActual.manejarCambioEstado(this);
    }

    public boolean agregarJugador(Usuario jugador) {
        if (!estadoActual.puedeAgregarJugador() || estaCompleto()) {
            return false;
        }

        Equipo equipoConMenosJugadores = equipos.stream()
                .min(Comparator.comparingInt(Equipo::cantidadJugadores))
                .orElse(null);

        if (equipoConMenosJugadores != null && equipoConMenosJugadores.puedeAgregarJugador()) {
            equipoConMenosJugadores.agregarJugador(jugador);
            // Después de agregar, el estado podría necesitar cambiar (ej. de NecesitamosJugadores a PartidoArmado)
            this.estadoActual.manejarCambioEstado(this);
            return true;
        }
        return false;
    }

    public boolean estaCompleto() {
        return getJugadoresActuales().size() >= cantJugadoresRequeridos;
    }

    public boolean puedeIniciar() {
        return estadoActual.puedeIniciar() && estaCompleto();
    }

    public boolean tieneCriterios() {
        return criterios != null;
    }

    public void aplicarCriterios(CriteriosPartido criterios) {
        this.criterios = criterios;
    }

    public void crearEquipo(String nombreEquipo, int maxJugadores) {
        equipos.add(new Equipo(nombreEquipo, maxJugadores));
    }

    public List<Usuario> getJugadoresActuales() {
        return equipos.stream()
                .flatMap(equipo -> equipo.getJugadores().stream())
                .collect(Collectors.toList());
    }

    @Override
    public String toString() {
        return "Partido{" +
                "fecha='" + fecha + '\'' +
                ", deporte='" + deporte + '\'' +
                ", estado='" + estadoActual.getNombreEstado() + '\'' +
                ", organizador=" + (organizador != null ? organizador.getNombre() : "N/A") +
                '}';
    }
} 