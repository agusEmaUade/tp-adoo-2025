package com.tp.uno.mas.encuentros.deportivos.dto;

import com.tp.uno.mas.encuentros.deportivos.model.TipoDeporte;
import com.tp.uno.mas.encuentros.deportivos.model.Partido.EstadoEnum;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO para transferencia de datos de Partido.
 * Facilita la comunicación entre capas sin exponer la entidad directamente.
 */
public class PartidoDTO {

    private Long id;

    @NotNull(message = "El tipo de deporte es obligatorio")
    private TipoDeporte tipoDeporte;

    @Min(value = 2, message = "Debe haber al menos 2 jugadores")
    @Max(value = 50, message = "No pueden haber más de 50 jugadores")
    private int cantidadJugadores;

    @Min(value = 15, message = "La duración mínima es 15 minutos")
    @Max(value = 480, message = "La duración máxima es 8 horas")
    private int duracion;

    @NotNull(message = "La fecha y hora son obligatorias")
    @Future(message = "La fecha y hora deben ser futuras")
    private LocalDateTime fechaHora;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    private String descripcion;

    @NotNull(message = "La ubicación es obligatoria")
    @Valid
    private UbicacionDTO ubicacion;

    @NotNull(message = "El creador es obligatorio")
    private Long creadorId;

    private List<CuentaDTO> jugadores;
    private EstadoEnum estado;
    private LocalDateTime creadoEn;
    private LocalDateTime actualizadoEn;

    // Campos calculados
    private int jugadoresActuales;
    private double porcentajeOcupacion;
    private boolean disponible;

    // Constructores
    public PartidoDTO() {}

    public PartidoDTO(TipoDeporte tipoDeporte, int cantidadJugadores, int duracion,
                      LocalDateTime fechaHora, String descripcion, UbicacionDTO ubicacion, Long creadorId) {
        this.tipoDeporte = tipoDeporte;
        this.cantidadJugadores = cantidadJugadores;
        this.duracion = duracion;
        this.fechaHora = fechaHora;
        this.descripcion = descripcion;
        this.ubicacion = ubicacion;
        this.creadorId = creadorId;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public TipoDeporte getTipoDeporte() {
        return tipoDeporte;
    }

    public void setTipoDeporte(TipoDeporte tipoDeporte) {
        this.tipoDeporte = tipoDeporte;
    }

    public int getCantidadJugadores() {
        return cantidadJugadores;
    }

    public void setCantidadJugadores(int cantidadJugadores) {
        this.cantidadJugadores = cantidadJugadores;
    }

    public int getDuracion() {
        return duracion;
    }

    public void setDuracion(int duracion) {
        this.duracion = duracion;
    }

    public LocalDateTime getFechaHora() {
        return fechaHora;
    }

    public void setFechaHora(LocalDateTime fechaHora) {
        this.fechaHora = fechaHora;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public UbicacionDTO getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(UbicacionDTO ubicacion) {
        this.ubicacion = ubicacion;
    }

    public Long getCreadorId() {
        return creadorId;
    }

    public void setCreadorId(Long creadorId) {
        this.creadorId = creadorId;
    }

    public List<CuentaDTO> getJugadores() {
        return jugadores;
    }

    public void setJugadores(List<CuentaDTO> jugadores) {
        this.jugadores = jugadores;
    }

    public EstadoEnum getEstado() {
        return estado;
    }

    public void setEstado(EstadoEnum estado) {
        this.estado = estado;
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public void setCreadoEn(LocalDateTime creadoEn) {
        this.creadoEn = creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    public void setActualizadoEn(LocalDateTime actualizadoEn) {
        this.actualizadoEn = actualizadoEn;
    }

    public int getJugadoresActuales() {
        return jugadoresActuales;
    }

    public void setJugadoresActuales(int jugadoresActuales) {
        this.jugadoresActuales = jugadoresActuales;
    }

    public double getPorcentajeOcupacion() {
        return porcentajeOcupacion;
    }

    public void setPorcentajeOcupacion(double porcentajeOcupacion) {
        this.porcentajeOcupacion = porcentajeOcupacion;
    }

    public boolean isDisponible() {
        return disponible;
    }

    public void setDisponible(boolean disponible) {
        this.disponible = disponible;
    }

    @Override
    public String toString() {
        return String.format("PartidoDTO{id=%d, tipoDeporte=%s, jugadores=%d/%d, estado=%s, fechaHora=%s}",
                           id, tipoDeporte, jugadoresActuales, cantidadJugadores, estado, fechaHora);
    }
} 