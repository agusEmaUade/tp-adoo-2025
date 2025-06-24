package com.tp.uno.mas.encuentros.deportivos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import java.util.Objects;

/**
 * Entidad que representa una ubicación geográfica.
 * Aplica el principio de Information Expert (GRASP) - conoce sus propios datos y comportamiento.
 */
@Entity
@Table(name = "ubicaciones")
public class Ubicacion {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "La latitud es obligatoria")
    @DecimalMin(value = "-90.0", message = "La latitud debe ser mayor o igual a -90")
    @DecimalMax(value = "90.0", message = "La latitud debe ser menor o igual a 90")
    @Column(nullable = false)
    private Double latitud;

    @NotNull(message = "La longitud es obligatoria")
    @DecimalMin(value = "-180.0", message = "La longitud debe ser mayor o igual a -180")
    @DecimalMax(value = "180.0", message = "La longitud debe ser menor o igual a 180")
    @Column(nullable = false)
    private Double longitud;

    // Constructor por defecto requerido por JPA
    public Ubicacion() {}

    public Ubicacion(Double latitud, Double longitud) {
        this.latitud = Objects.requireNonNull(latitud, "La latitud no puede ser nula");
        this.longitud = Objects.requireNonNull(longitud, "La longitud no puede ser nula");
    }

    /**
     * Calcula la distancia en kilómetros entre esta ubicación y otra usando la fórmula de Haversine.
     * Principio GRASP: Information Expert - la ubicación sabe cómo calcular distancias.
     */
    public Double calcularDistancia(Ubicacion otra) {
        Objects.requireNonNull(otra, "La otra ubicación no puede ser nula");
        
        final double RADIO_TIERRA_KM = 6371.0;
        
        double lat1Rad = Math.toRadians(this.latitud);
        double lat2Rad = Math.toRadians(otra.latitud);
        double deltaLatRad = Math.toRadians(otra.latitud - this.latitud);
        double deltaLonRad = Math.toRadians(otra.longitud - this.longitud);

        double a = Math.sin(deltaLatRad / 2) * Math.sin(deltaLatRad / 2) +
                   Math.cos(lat1Rad) * Math.cos(lat2Rad) *
                   Math.sin(deltaLonRad / 2) * Math.sin(deltaLonRad / 2);
        
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        
        return RADIO_TIERRA_KM * c;
    }

    /**
     * Obtiene una representación textual de la ubicación.
     */
    public String obtenerUbicacion() {
        return String.format("Lat: %.6f, Lon: %.6f", latitud, longitud);
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Double getLatitud() {
        return latitud;
    }

    public void setLatitud(Double latitud) {
        this.latitud = Objects.requireNonNull(latitud, "La latitud no puede ser nula");
    }

    public Double getLongitud() {
        return longitud;
    }

    public void setLongitud(Double longitud) {
        this.longitud = Objects.requireNonNull(longitud, "La longitud no puede ser nula");
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Ubicacion ubicacion = (Ubicacion) o;
        return Objects.equals(latitud, ubicacion.latitud) && 
               Objects.equals(longitud, ubicacion.longitud);
    }

    @Override
    public int hashCode() {
        return Objects.hash(latitud, longitud);
    }

    @Override
    public String toString() {
        return String.format("Ubicacion{id=%d, latitud=%.6f, longitud=%.6f}", 
                           id, latitud, longitud);
    }
} 