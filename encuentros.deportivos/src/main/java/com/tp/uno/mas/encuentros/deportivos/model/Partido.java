package com.tp.uno.mas.encuentros.deportivos.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Entidad que representa un partido deportivo.
 * Implementa el patrón State para gestionar diferentes estados del partido.
 * Aplica principios SOLID: SRP - gestiona únicamente datos y comportamiento del partido.
 */
@Entity
@Table(name = "partidos")
public class Partido {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull(message = "El tipo de deporte es obligatorio")
    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_deporte", nullable = false)
    private TipoDeporte tipoDeporte;

    @Min(value = 2, message = "Debe haber al menos 2 jugadores")
    @Column(name = "cantidad_jugadores", nullable = false)
    private int cantidadJugadores;

    @Min(value = 15, message = "La duración mínima es 15 minutos")
    @Column(nullable = false)
    private int duracion;

    @NotNull(message = "La fecha y hora son obligatorias")
    @Column(name = "fecha_hora", nullable = false)
    private LocalDateTime fechaHora;

    @NotBlank(message = "La descripción es obligatoria")
    @Size(max = 500, message = "La descripción no puede exceder 500 caracteres")
    @Column(nullable = false, length = 500)
    private String descripcion;

    @NotNull(message = "La ubicación es obligatoria")
    @OneToOne(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JoinColumn(name = "ubicacion_id", nullable = false)
    private Ubicacion ubicacion;

    @ManyToMany(fetch = FetchType.LAZY, cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    @JoinTable(
        name = "partido_jugadores",
        joinColumns = @JoinColumn(name = "partido_id"),
        inverseJoinColumns = @JoinColumn(name = "cuenta_id")
    )
    private List<Cuenta> jugadores = new ArrayList<>();

    @NotNull(message = "El creador del partido es obligatorio")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creador_id", nullable = false)
    private Cuenta creador;

    // Estado actual del partido (Patrón State)
    @Transient
    private IEstado estado;

    @Enumerated(EnumType.STRING)
    @Column(name = "estado_nombre", nullable = false)
    private EstadoEnum estadoNombre = EstadoEnum.NECESITAMOS_JUGADORES;

    @Column(name = "creado_en", nullable = false)
    private LocalDateTime creadoEn;

    @Column(name = "actualizado_en")
    private LocalDateTime actualizadoEn;

    // Patrón Observer - Lista de observadores
    @Transient
    private List<IObservador> observadores = new ArrayList<>();

    // Enumeración para persistir el estado - Secuencia según enunciado
    public enum EstadoEnum {
        NECESITAMOS_JUGADORES,  // Estado inicial
        PARTIDO_ARMADO,         // Suficientes jugadores
        CONFIRMADO,             // Todos confirmaron
        EN_JUEGO,              // Partido en curso
        FINALIZADO,            // Partido completado
        CANCELADO              // Partido cancelado
    }

    // Constructor por defecto requerido por JPA
    public Partido() {
        this.creadoEn = LocalDateTime.now();
        this.actualizadoEn = LocalDateTime.now();
        inicializarEstado();
    }

    public Partido(TipoDeporte tipoDeporte, int cantidadJugadores, int duracion,
                   LocalDateTime fechaHora, String descripcion, Ubicacion ubicacion, Cuenta creador) {
        this();
        this.tipoDeporte = Objects.requireNonNull(tipoDeporte, "El tipo de deporte no puede ser nulo");
        this.cantidadJugadores = cantidadJugadores;
        this.duracion = duracion;
        this.fechaHora = Objects.requireNonNull(fechaHora, "La fecha y hora no pueden ser nulas");
        this.descripcion = Objects.requireNonNull(descripcion, "La descripción no puede ser nula");
        this.ubicacion = Objects.requireNonNull(ubicacion, "La ubicación no puede ser nula");
        this.creador = Objects.requireNonNull(creador, "El creador no puede ser nulo");
        
        // El creador se une automáticamente al partido
        agregarJugador(creador);
        
        // Notificar creación del partido
        notificarObservadores("PARTIDO_CREADO", 
            String.format("Nuevo partido de %s creado por %s", 
                tipoDeporte.name(), creador.getNombre()));
    }

    /**
     * Inicializa el estado basado en el enum persistido.
     */
    @PostLoad
    private void inicializarEstado() {
        switch (estadoNombre) {
            case NECESITAMOS_JUGADORES -> estado = new NecesitamosJugadores();
            case PARTIDO_ARMADO -> estado = new PartidoArmado();
            case CONFIRMADO -> estado = new Confirmado();
            case EN_JUEGO -> estado = new EnJuego();
            case FINALIZADO -> estado = new Finalizado();
            case CANCELADO -> estado = new Cancelado();
            default -> estado = new NecesitamosJugadores(); // Estado inicial por defecto
        }
    }

    /**
     * Cambia el estado del partido.
     * Patrón State - permite cambiar comportamiento dinámicamente.
     */
    public void cambiarEstado(IEstado nuevoEstado) {
        Objects.requireNonNull(nuevoEstado, "El nuevo estado no puede ser nulo");
        IEstado estadoAnterior = this.estado;
        this.estado = nuevoEstado;
        this.estadoNombre = EstadoEnum.valueOf(nuevoEstado.obtenerNombre().toUpperCase());
        this.actualizadoEn = LocalDateTime.now();
        
        // Notificar a observadores del cambio de estado
        notificarObservadores("ESTADO_CAMBIADO", 
            String.format("Estado cambió de %s a %s", 
                estadoAnterior != null ? estadoAnterior.obtenerNombre() : "INICIAL", 
                nuevoEstado.obtenerNombre()));
    }

    /**
     * Agrega un observador al partido.
     * Patrón Observer - gestión de observadores.
     */
    public void agregarObservador(IObservador observador) {
        Objects.requireNonNull(observador, "El observador no puede ser nulo");
        if (!observadores.contains(observador)) {
            observadores.add(observador);
        }
    }

    /**
     * Remueve un observador del partido.
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

    /**
     * Agrega un jugador al partido si es posible según el estado actual.
     * Principio GRASP: Information Expert - el partido gestiona sus jugadores.
     */
    public void agregarJugador(Cuenta cuenta) {
        Objects.requireNonNull(cuenta, "La cuenta no puede ser nula");
        
        if (estado == null) {
            inicializarEstado();
        }
        
        if (!estado.puedeAgregarJugadores()) {
            throw new IllegalStateException("No se pueden agregar jugadores en el estado: " + estado.obtenerNombre());
        }
        
        if (jugadores.contains(cuenta)) {
            throw new IllegalArgumentException("El jugador ya está en el partido");
        }
        
        if (jugadores.size() >= cantidadJugadores) {
            throw new IllegalStateException("El partido ya está completo");
        }
        
        jugadores.add(cuenta);
        cuenta.agregarPartido(this);
        
        // Notificar evento de jugador agregado
        notificarObservadores("JUGADOR_AGREGADO", 
            String.format("Jugador %s se unió al partido. Total: %d/%d", 
                cuenta.getNombre(), jugadores.size(), cantidadJugadores));
        
        // Verificar transición de estado automática
        verificarTransicionEstado();
        
        this.actualizadoEn = LocalDateTime.now();
    }

    /**
     * Remueve un jugador del partido si es posible según el estado actual.
     */
    public void removerJugador(Cuenta cuenta) {
        Objects.requireNonNull(cuenta, "La cuenta no puede ser nula");
        
        if (estado == null) {
            inicializarEstado();
        }
        
        if (!estado.puedeRemoverJugadores()) {
            throw new IllegalStateException("No se pueden remover jugadores en el estado: " + estado.obtenerNombre());
        }
        
        if (!jugadores.contains(cuenta)) {
            throw new IllegalArgumentException("El jugador no está en el partido");
        }
        
        if (cuenta.equals(creador)) {
            throw new IllegalArgumentException("El creador del partido no puede abandonarlo");
        }
        
        jugadores.remove(cuenta);
        cuenta.removerPartido(this);
        
        // Notificar evento de jugador removido
        notificarObservadores("JUGADOR_REMOVIDO", 
            String.format("Jugador %s abandonó el partido. Total: %d/%d", 
                cuenta.getNombre(), jugadores.size(), cantidadJugadores));
        
        // Verificar transición de estado automática
        verificarTransicionEstado();
        
        this.actualizadoEn = LocalDateTime.now();
    }

    /**
     * Verifica si el partido necesita cambiar de estado automáticamente.
     * Sigue la secuencia del enunciado: Necesitamos jugadores -> Partido armado -> Confirmado
     */
    private void verificarTransicionEstado() {
        // Si no hay suficientes jugadores, regresar a "Necesitamos jugadores"
        if (jugadores.size() < cantidadJugadores) {
            if (!(estado instanceof NecesitamosJugadores)) {
                cambiarEstado(new NecesitamosJugadores());
            }
        } 
        // Si se alcanzó el número requerido de jugadores, pasar a "Partido armado"
        else if (jugadores.size() >= cantidadJugadores) {
            if (estado instanceof NecesitamosJugadores) {
                cambiarEstado(new PartidoArmado());
            }
        }
    }

    /**
     * Verifica si el partido está disponible para unirse.
     */
    public boolean estaDisponible() {
        return estado != null && estado.puedeAgregarJugadores() && 
               jugadores.size() < cantidadJugadores &&
               fechaHora.isAfter(LocalDateTime.now());
    }

    /**
     * Obtiene el porcentaje de ocupación del partido.
     */
    public double obtenerPorcentajeOcupacion() {
        return cantidadJugadores > 0 ? (double) jugadores.size() / cantidadJugadores * 100 : 0;
    }

    // Métodos del patrón State delegados
    public void manejar() {
        if (estado != null) {
            estado.manejar(this);
        }
    }

    @PrePersist
    @PreUpdate
    private void actualizarTimestamps() {
        this.actualizadoEn = LocalDateTime.now();
        if (this.creadoEn == null) {
            this.creadoEn = LocalDateTime.now();
        }
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
        this.tipoDeporte = Objects.requireNonNull(tipoDeporte, "El tipo de deporte no puede ser nulo");
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
        this.fechaHora = Objects.requireNonNull(fechaHora, "La fecha y hora no pueden ser nulas");
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = Objects.requireNonNull(descripcion, "La descripción no puede ser nula");
    }

    public Ubicacion getUbicacion() {
        return ubicacion;
    }

    public void setUbicacion(Ubicacion ubicacion) {
        this.ubicacion = Objects.requireNonNull(ubicacion, "La ubicación no puede ser nula");
    }

    public List<Cuenta> getJugadores() {
        return new ArrayList<>(jugadores); // Defensive copy
    }

    public void setJugadores(List<Cuenta> jugadores) {
        this.jugadores = jugadores != null ? new ArrayList<>(jugadores) : new ArrayList<>();
    }

    public Cuenta getCreador() {
        return creador;
    }

    public void setCreador(Cuenta creador) {
        this.creador = Objects.requireNonNull(creador, "El creador no puede ser nulo");
    }

    public IEstado getEstado() {
        if (estado == null) {
            inicializarEstado();
        }
        return estado;
    }

    public EstadoEnum getEstadoNombre() {
        return estadoNombre;
    }

    public void setEstadoNombre(EstadoEnum estadoNombre) {
        this.estadoNombre = estadoNombre;
        inicializarEstado();
    }

    public LocalDateTime getCreadoEn() {
        return creadoEn;
    }

    public LocalDateTime getActualizadoEn() {
        return actualizadoEn;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Partido partido = (Partido) o;
        return Objects.equals(id, partido.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return String.format("Partido{id=%d, tipoDeporte=%s, jugadores=%d/%d, estado=%s, fechaHora=%s}",
                           id, tipoDeporte, jugadores.size(), cantidadJugadores, 
                           estadoNombre, fechaHora);
    }
} 