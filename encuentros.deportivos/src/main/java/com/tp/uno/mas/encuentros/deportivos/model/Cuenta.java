package com.tp.uno.mas.encuentros.deportivos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad que representa una cuenta de usuario en el sistema.
 * Aplica principios SOLID: SRP - responsabilidad única de gestionar datos del usuario.
 * Principio GRASP: Information Expert - conoce y gestiona su propia información.
 */
@Entity
@Table(name = "cuentas", uniqueConstraints = {
    @UniqueConstraint(columnNames = "email")
})
public class Cuenta {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 2, max = 100, message = "El nombre debe tener entre 2 y 100 caracteres")
    @Column(nullable = false, length = 100)
    private String nombre;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email debe tener un formato válido")
    @Size(max = 150, message = "El email no puede exceder 150 caracteres")
    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, max = 255, message = "La contraseña debe tener al menos 6 caracteres")
    @Column(nullable = false)
    private String contraseña;

    // Opcional según enunciado
    @Enumerated(EnumType.STRING)
    @Column(name = "deporte_favorito", nullable = true)
    private TipoDeporte deporteFavorito;

    // Opcional según enunciado
    @Enumerated(EnumType.STRING)
    @Column(nullable = true)
    private NivelDeJuego nivel;

    @NotNull(message = "La ubicación es obligatoria")
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id", nullable = false)
    private Ubicacion ubicacion;

    @ManyToMany(mappedBy = "jugadores", fetch = FetchType.LAZY)
    private List<Partido> partidos = new ArrayList<>();

    // Patrón Observer - Lista de observadores
    @Transient
    private List<IObservador> observadores = new ArrayList<>();

    // Constructor por defecto requerido por JPA
    public Cuenta() {}

    public Cuenta(String nombre, String email, String contraseña, 
                  TipoDeporte deporteFavorito, NivelDeJuego nivel, Ubicacion ubicacion) {
        this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
        this.email = Objects.requireNonNull(email, "El email no puede ser nulo");
        this.contraseña = Objects.requireNonNull(contraseña, "La contraseña no puede ser nula");
        this.deporteFavorito = deporteFavorito; // Opcional según enunciado
        this.nivel = nivel; // Opcional según enunciado
        this.ubicacion = Objects.requireNonNull(ubicacion, "La ubicación no puede ser nula");
    }
    
    // Constructor adicional para registro básico (solo campos obligatorios)
    public Cuenta(String nombre, String email, String contraseña, Ubicacion ubicacion) {
        this(nombre, email, contraseña, null, null, ubicacion);
    }

    /**
     * Obtiene la ubicación como string.
     * Principio GRASP: Information Expert - delega en Ubicacion.
     */
    public String obtenerUbicacion() {
        return ubicacion != null ? ubicacion.obtenerUbicacion() : "Sin ubicación";
    }

    /**
     * Verifica si la cuenta está completa para participar en partidos.
     */
    public boolean estaCompleta() {
        return nombre != null && !nombre.trim().isEmpty() &&
               email != null && !email.trim().isEmpty() &&
               contraseña != null && !contraseña.trim().isEmpty() &&
               deporteFavorito != null &&
               nivel != null &&
               ubicacion != null;
    }

    /**
     * Agrega un partido a la lista de partidos del usuario.
     * Principio GRASP: Information Expert - gestiona su propia lista.
     */
    public void agregarPartido(Partido partido) {
        Objects.requireNonNull(partido, "El partido no puede ser nulo");
        if (!partidos.contains(partido)) {
            partidos.add(partido);
        }
    }

    /**
     * Remueve un partido de la lista de partidos del usuario.
     */
    public void removerPartido(Partido partido) {
        Objects.requireNonNull(partido, "El partido no puede ser nulo");
        partidos.remove(partido);
    }

    /**
     * Agrega un observador a la cuenta.
     * Patrón Observer - gestión de observadores.
     */
    public void agregarObservador(IObservador observador) {
        Objects.requireNonNull(observador, "El observador no puede ser nulo");
        if (!observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    /**
     * Remueve un observador de la cuenta.
     */
    public void removerObservador(IObservador observador) {
        Objects.requireNonNull(observador, "El observador no puede ser nulo");
        observadores.remove(observador);
    }

    /**
     * Notifica a todos los observadores sobre un evento.
     */
    public void notificarObservadores(String evento, Object datos) {
        for (IObservador observador : observadores) {
            try {
                observador.actualizar(evento, datos);
            } catch (Exception e) {
                // Log del error pero no interrumpir el proceso principal
                System.err.println("Error notificando observador " + observador.getTipo() + ": " + e.getMessage());
            }
        }
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
        this.nombre = Objects.requireNonNull(nombre, "El nombre no puede ser nulo");
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = Objects.requireNonNull(email, "El email no puede ser nulo");
    }

    public String getContraseña() {
        return contraseña;
    }

    public void setContraseña(String contraseña) {
        this.contraseña = Objects.requireNonNull(contraseña, "La contraseña no puede ser nula");
    }

    public TipoDeporte getDeporteFavorito() {
        return deporteFavorito;
    }

    public void setDeporteFavorito(TipoDeporte deporteFavorito) {
        TipoDeporte anterior = this.deporteFavorito;
        this.deporteFavorito = deporteFavorito; // Ahora puede ser null
        
        // Notificar cambio de deporte favorito
        if (anterior != deporteFavorito) {
            notificarObservadores("DEPORTE_FAVORITO_CAMBIADO", 
                String.format("Deporte favorito cambió de %s a %s", anterior, deporteFavorito));
        }
    }

    public NivelDeJuego getNivel() {
        return nivel;
    }

    public void setNivel(NivelDeJuego nivel) {
        NivelDeJuego anterior = this.nivel;
        this.nivel = nivel; // Ahora puede ser null
        
        // Notificar cambio de nivel
        if (anterior != nivel) {
            notificarObservadores("NIVEL_CAMBIADO", 
                String.format("Nivel cambió de %s a %s", anterior, nivel));
        }
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Ubicacion ubicacion) {
        this.ubicacion = Objects.requireNonNull(ubicacion, "La ubicación no puede ser nula");
    }

    public List<Partido> getPartidos() {
        return new ArrayList<>(partidos); // Defensive copy
    }

    public void setPartidos(List<Partido> partidos) {
        this.partidos = partidos != null ? new ArrayList<>(partidos) : new ArrayList<>();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cuenta cuenta = (Cuenta) o;
        return Objects.equals(email, cuenta.email);
    }

    @Override
    public int hashCode() {
        return Objects.hash(email);
    }

    @Override
    public String toString() {
        return String.format("Cuenta{id=%d, nombre='%s', email='%s', deporteFavorito=%s, nivel=%s}", 
                           id, nombre, email, deporteFavorito, nivel);
    }
} 