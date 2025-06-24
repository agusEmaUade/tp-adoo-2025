package com.tp.uno.mas.encuentros.deportivos.model;

import com.tp.uno.mas.encuentros.deportivos.state.EstadoPartido;
import com.tp.uno.mas.encuentros.deportivos.state.NecesitamosJugadores;

import java.util.ArrayList;
import java.util.List;

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
        this.equipos = new ArrayList<>();
        this.estadoActual = new NecesitamosJugadores();
    }

    public void cambiarEstado(EstadoPartido nuevoEstado) {
        this.estadoActual = nuevoEstado;
    }

    public EstadoPartido getEstado() {
        return estadoActual;
    }

    public boolean estaCompleto() {
        int totalJugadores = 0;
        for (Equipo equipo : equipos) {
            totalJugadores += equipo.cantidadJugadores();
        }
        return totalJugadores >= cantJugadoresRequeridos;
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

    public void quitarCriterios() {
        this.criterios = null;
    }

    public boolean puedeAgregarJugador(Usuario usuario) {
        if (!estadoActual.puedeAgregarJugador()) {
            return false;
        }
        
        // Verificar criterios si existen
        if (tieneCriterios()) {
            return criterios.cumpleCriterios(usuario) && 
                   criterios.validarUbicacion(usuario.getUbicacion(), this.ubicacion);
        }
        
        return true;
    }

    public void agregarJugadorAEquipo(Usuario usuario, int indiceEquipo) {
        if (puedeAgregarJugador(usuario) && indiceEquipo < equipos.size()) {
            equipos.get(indiceEquipo).agregarJugador(usuario);
            estadoActual.manejarCambioEstado(this);
        }
    }

    public void crearEquipo(String nombreEquipo, int maxJugadores) {
        equipos.add(new Equipo(nombreEquipo, maxJugadores));
    }

    // Getters y Setters
    public String getFecha() { return fecha; }
    public void setFecha(String fecha) { this.fecha = fecha; }

    public String getDeporte() { return deporte; }
    public void setDeporte(String deporte) { this.deporte = deporte; }

    public int getCantJugadoresRequeridos() { return cantJugadoresRequeridos; }
    public void setCantJugadoresRequeridos(int cantJugadoresRequeridos) { 
        this.cantJugadoresRequeridos = cantJugadoresRequeridos; 
    }

    public int getDuracion() { return duracion; }
    public void setDuracion(int duracion) { this.duracion = duracion; }

    public EstadoPartido getEstadoActual() { return estadoActual; }
    public void setEstadoActual(EstadoPartido estadoActual) { this.estadoActual = estadoActual; }

    public List<Equipo> getEquipos() { return equipos; }
    public void setEquipos(List<Equipo> equipos) { this.equipos = equipos; }

    public Ubicacion getUbicacion() { return ubicacion; }
    public void setUbicacion(Ubicacion ubicacion) { this.ubicacion = ubicacion; }

    public Usuario getOrganizador() { return organizador; }
    public void setOrganizador(Usuario organizador) { this.organizador = organizador; }

    public CriteriosPartido getCriterios() { return criterios; }
    public void setCriterios(CriteriosPartido criterios) { this.criterios = criterios; }

    @Override
    public String toString() {
        return "Partido{" +
                "fecha='" + fecha + '\'' +
                ", deporte='" + deporte + '\'' +
                ", cantJugadoresRequeridos=" + cantJugadoresRequeridos +
                ", duracion=" + duracion +
                ", estado='" + estadoActual.getNombreEstado() + '\'' +
                ", organizador=" + (organizador != null ? organizador.getNombre() : "Sin organizador") +
                '}';
    }
} 