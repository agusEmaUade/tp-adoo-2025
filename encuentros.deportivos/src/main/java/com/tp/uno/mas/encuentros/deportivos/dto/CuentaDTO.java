package com.tp.uno.mas.encuentros.deportivos.dto;

import com.tp.uno.mas.encuentros.deportivos.model.NivelDeJuego;
import com.tp.uno.mas.encuentros.deportivos.model.TipoDeporte;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * DTO para transferencia de datos de Cuenta.
 * Principio SOLID: Single Responsibility - solo maneja transferencia de datos.
 */
public class CuentaDTO {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 255, message = "La contraseña debe tener al menos 6 caracteres")
    private String contraseña;

    @NotNull(message = "El deporte favorito es obligatorio")
    private TipoDeporte deporteFavorito;

    @NotNull(message = "El nivel de juego es obligatorio")
    private NivelDeJuego nivel;

    @NotNull(message = "La ubicación es obligatoria")
    @Valid
    private UbicacionDTO ubicacion;

    // Constructores
    public CuentaDTO() {}

    public CuentaDTO(String nombre, String email, String contraseña, 
                     TipoDeporte deporteFavorito, NivelDeJuego nivel, UbicacionDTO ubicacion) {
        this.nombre = nombre;
        this.email = email;
        this.contraseña = contraseña;
        this.deporteFavorito = deporteFavorito;
        this.nivel = nivel;
        this.ubicacion = ubicacion;
    }

    // Getters y Setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombre() {
        return nombre;
    }

    public void setNombre(String nombre) {
        this.nombre = nombre;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = contraseña;
    }

    public TipoDeporte getDeporteFavorito() {
        return deporteFavorito;
    }

    public void setDeporteFavorito(TipoDeporte deporteFavorito) {
        this.deporteFavorito = deporteFavorito;
    }

    public NivelDeJuego getNivel() {
        return nivel;
    }

    public void setNivel(NivelDeJuego nivel) {
        this.nivel = nivel;
    }

    public UbicacionDTO getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(UbicacionDTO ubicacion) {
        this.ubicacion = ubicacion;
    }

    @Override
    public String toString() {
        return String.format("CuentaDTO{id=%d, nombre='%s', email='%s', deporteFavorito=%s, nivel=%s}", 
                           id, nombre, email, deporteFavorito, nivel);
    }
} 