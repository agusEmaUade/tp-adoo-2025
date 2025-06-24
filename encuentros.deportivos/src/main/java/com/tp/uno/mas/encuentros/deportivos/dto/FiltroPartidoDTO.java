package com.tp.uno.mas.encuentros.deportivos.dto;

import com.tp.uno.mas.encuentros.deportivos.model.NivelDeJuego;
import com.tp.uno.mas.encuentros.deportivos.model.TipoDeporte;
import com.tp.uno.mas.encuentros.deportivos.model.Partido.EstadoEnum;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import java.time.LocalDateTime;

/**
 * DTO para filtros de búsqueda de partidos.
 * Permite aplicar múltiples criterios de filtrado.
 * Principio SOLID: Single Responsibility - solo maneja criterios de filtrado.
 */
public class FiltroPartidoDTO {

    private TipoDeporte tipoDeporte;
    private NivelDeJuego nivelDeJuego;
    private EstadoEnum estado;
    
    private LocalDateTime fechaDesde;
    private LocalDateTime fechaHasta;
    
    // Filtros por ubicación
    private Double latitud;
    private Double longitud;
    
    @DecimalMin(value = "0.1", message = "El radio mínimo es 0.1 km")
    private Double radioKm; // Radio de búsqueda en kilómetros
    
    // Filtros por capacidad
    @Min(value = 1, message = "El mínimo de jugadores debe ser al menos 1")
    private Integer jugadoresMinimo;
    
    @Min(value = 1, message = "El máximo de jugadores debe ser al menos 1")
    private Integer jugadoresMaximo;
    
    // Filtros de disponibilidad
    private Boolean soloDisponibles;
    private Boolean soloConCupo;
    
    // Ordenamiento
    private String ordenarPor; // "fecha", "distancia", "ocupacion"
    private String direccion; // "asc", "desc"
    
    // Paginación
    @Min(value = 0, message = "La página no puede ser negativa")
    private Integer pagina = 0;
    
    @Min(value = 1, message = "El tamaño de página debe ser al menos 1")
    private Integer tamañoPagina = 10;

    // Constructores
    public FiltroPartidoDTO() {}

    // Getters y Setters
    public TipoDeporte getTipoDeporte() {
        return tipoDeporte;
    }

    public void setTipoDeporte(TipoDeporte tipoDeporte) {
        this.tipoDeporte = tipoDeporte;
    }

    public NivelDeJuego getNivelDeJuego() {
        return nivelDeJuego;
    }

    public void setNivelDeJuego(NivelDeJuego nivelDeJuego) {
        this.nivelDeJuego = nivelDeJuego;
    }

    public EstadoEnum getEstado() {
        return estado;
    }

    public void setEstado(EstadoEnum estado) {
        this.estado = estado;
    }

    public LocalDateTime getFechaDesde() {
        return fechaDesde;
    }

    public void setFechaDesde(LocalDateTime fechaDesde) {
        this.fechaDesde = fechaDesde;
    }

    public LocalDateTime getFechaHasta() {
        return fechaHasta;
    }

    public void setFechaHasta(LocalDateTime fechaHasta) {
        this.fechaHasta = fechaHasta;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = latitud;
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = longitud;
    }

    public Double getRadioKm() {
        return radioKm;
    }

    public void setRadioKm(Double radioKm) {
        this.radioKm = radioKm;
    }

    public Integer getJugadoresMinimo() {
        return jugadoresMinimo;
    }

    public void setJugadoresMinimo(Integer jugadoresMinimo) {
        this.jugadoresMinimo = jugadoresMinimo;
    }

    public Integer getJugadoresMaximo() {
        return jugadoresMaximo;
    }

    public void setJugadoresMaximo(Integer jugadoresMaximo) {
        this.jugadoresMaximo = jugadoresMaximo;
    }

    public Boolean getSoloDisponibles() {
        return soloDisponibles;
    }

    public void setSoloDisponibles(Boolean soloDisponibles) {
        this.soloDisponibles = soloDisponibles;
    }

    public Boolean getSoloConCupo() {
        return soloConCupo;
    }

    public void setSoloConCupo(Boolean soloConCupo) {
        this.soloConCupo = soloConCupo;
    }

    public String getOrdenarPor() {
        return ordenarPor;
    }

    public void setOrdenarPor(String ordenarPor) {
        this.ordenarPor = ordenarPor;
    }

    public String getDireccion() {
        return direccion;
    }

    public void setDireccion(String direccion) {
        this.direccion = direccion;
    }

    public Integer getPagina() {
        return pagina;
    }

    public void setPagina(Integer pagina) {
        this.pagina = pagina;
    }

    public Integer getTamañoPagina() {
        return tamañoPagina;
    }

    public void setTamañoPagina(Integer tamañoPagina) {
        this.tamañoPagina = tamañoPagina;
    }

    /**
     * Verifica si hay filtros de ubicación configurados.
     */
    public boolean tieneFiltroPorUbicacion() {
        return latitud != null && longitud != null && radioKm != null;
    }

    /**
     * Verifica si hay filtros de fecha configurados.
     */
    public boolean tieneFiltroPorFecha() {
        return fechaDesde != null || fechaHasta != null;
    }

    /**
     * Verifica si hay filtros de capacidad configurados.
     */
    public boolean tieneFiltroPorCapacidad() {
        return jugadoresMinimo != null || jugadoresMaximo != null;
    }

    @Override
    public String toString() {
        return String.format("FiltroPartidoDTO{tipoDeporte=%s, estado=%s, radioKm=%.2f, pagina=%d}",
                           tipoDeporte, estado, radioKm, pagina);
    }
} 