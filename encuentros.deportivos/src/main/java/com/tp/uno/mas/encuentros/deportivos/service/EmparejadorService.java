package com.tp.uno.mas.encuentros.deportivos.service;

import com.tp.uno.mas.encuentros.deportivos.model.*;
import com.tp.uno.mas.encuentros.deportivos.repository.CuentaRepository;
import com.tp.uno.mas.encuentros.deportivos.repository.PartidoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Servicio para el emparejamiento de jugadores usando diferentes estrategias.
 * Patrón Strategy - Contexto que utiliza diferentes algoritmos de emparejamiento.
 * Principio SOLID: Single Responsibility - se enfoca únicamente en emparejamiento.
 */
@Service
public class EmparejadorService {

    private final CuentaRepository cuentaRepository;
    private final PartidoRepository partidoRepository;
    
    // Patrón Strategy - estrategia actual
    private IEstrategiaEmparejamiento estrategia;
    
    // Estrategias disponibles
    private final Map<String, IEstrategiaEmparejamiento> estrategiasDisponibles;

    @Autowired
    public EmparejadorService(CuentaRepository cuentaRepository, PartidoRepository partidoRepository) {
        this.cuentaRepository = cuentaRepository;
        this.partidoRepository = partidoRepository;
        
        // Inicializar estrategias disponibles
        this.estrategiasDisponibles = new HashMap<>();
        this.estrategiasDisponibles.put("NIVEL", new EstrategiaPorNivel());
        this.estrategiasDisponibles.put("CERCANIA", new EstrategiaPorCercania());
        this.estrategiasDisponibles.put("HISTORIAL", new EstrategiaPorHistorial());
        
        // Estrategia por defecto
        this.estrategia = this.estrategiasDisponibles.get("NIVEL");
    }

    /**
     * Cambia la estrategia de emparejamiento en tiempo de ejecución.
     * Patrón Strategy - permite cambiar algoritmo dinámicamente.
     */
    public void cambiarEstrategia(IEstrategiaEmparejamiento nuevaEstrategia) {
        this.estrategia = Objects.requireNonNull(nuevaEstrategia, "La nueva estrategia no puede ser nula");
    }

    /**
     * Cambia la estrategia por nombre.
     */
    public void cambiarEstrategia(String nombreEstrategia) {
        Objects.requireNonNull(nombreEstrategia, "El nombre de la estrategia no puede ser nulo");
        
        IEstrategiaEmparejamiento nuevaEstrategia = estrategiasDisponibles.get(nombreEstrategia.toUpperCase());
        if (nuevaEstrategia == null) {
            throw new IllegalArgumentException("Estrategia no encontrada: " + nombreEstrategia);
        }
        
        this.estrategia = nuevaEstrategia;
    }

    /**
     * Busca jugadores compatibles para un partido específico.
     */
    public List<Cuenta> buscarJugadores(Partido partido) {
        Objects.requireNonNull(partido, "El partido no puede ser nulo");
        
        if (estrategia == null) {
            throw new IllegalStateException("No hay estrategia de emparejamiento configurada");
        }

        // Obtener todos los candidatos disponibles
        List<Cuenta> candidatos = obtenerCandidatosDisponibles(partido);
        
        // Aplicar la estrategia actual
        return estrategia.encontrarJugadoresCompatibles(partido, candidatos);
    }

    /**
     * Busca jugadores compatibles limitando la cantidad de resultados.
     */
    public List<Cuenta> buscarJugadores(Partido partido, int limite) {
        List<Cuenta> todosLosJugadores = buscarJugadores(partido);
        return todosLosJugadores.stream()
                .limit(limite)
                .collect(Collectors.toList());
    }

    /**
     * Sugiere partidos recomendados para un jugador específico.
     */
    public List<Partido> sugerirPartidos(Cuenta jugador) {
        Objects.requireNonNull(jugador, "El jugador no puede ser nulo");

        // Buscar partidos que necesitan jugadores
        List<Partido> partidosDisponibles = partidoRepository.findPartidosNecesitanJugadores(
            jugador.getDeporteFavorito()
        );

        // Filtrar y ordenar por compatibilidad
        return partidosDisponibles.stream()
                .filter(partido -> !partido.getJugadores().contains(jugador))
                .filter(partido -> partido.estaDisponible())
                .sorted((p1, p2) -> Double.compare(
                    estrategia.calcularCompatibilidad(jugador, p2),
                    estrategia.calcularCompatibilidad(jugador, p1)
                ))
                .collect(Collectors.toList());
    }

    /**
     * Sugiere partidos limitando la cantidad de resultados.
     */
    public List<Partido> sugerirPartidos(Cuenta jugador, int limite) {
        List<Partido> todosLosPartidos = sugerirPartidos(jugador);
        return todosLosPartidos.stream()
                .limit(limite)
                .collect(Collectors.toList());
    }

    /**
     * Calcula la compatibilidad entre un jugador y un partido usando la estrategia actual.
     */
    public double calcularCompatibilidad(Cuenta jugador, Partido partido) {
        Objects.requireNonNull(jugador, "El jugador no puede ser nulo");
        Objects.requireNonNull(partido, "El partido no puede ser nulo");
        
        if (estrategia == null) {
            throw new IllegalStateException("No hay estrategia de emparejamiento configurada");
        }

        return estrategia.calcularCompatibilidad(jugador, partido);
    }

    /**
     * Obtiene estadísticas de emparejamiento para un jugador.
     */
    public EstadisticasEmparejamiento obtenerEstadisticas(Cuenta jugador) {
        Objects.requireNonNull(jugador, "El jugador no puede ser nulo");

        int partidosJugados = jugador.getPartidos().size();
        
        // Calcular compatibilidad promedio con partidos pasados
        double compatibilidadPromedio = jugador.getPartidos().stream()
                .mapToDouble(partido -> estrategia.calcularCompatibilidad(jugador, partido))
                .average()
                .orElse(0.0);

        // Obtener partidos sugeridos
        List<Partido> partidosSugeridos = sugerirPartidos(jugador, 5);

        return new EstadisticasEmparejamiento(
            partidosJugados,
            compatibilidadPromedio,
            partidosSugeridos.size(),
            estrategia.obtenerNombre()
        );
    }

    /**
     * Obtiene la estrategia actual.
     */
    public IEstrategiaEmparejamiento getEstrategiaActual() {
        return estrategia;
    }

    /**
     * Obtiene todas las estrategias disponibles.
     */
    public Map<String, IEstrategiaEmparejamiento> getEstrategiasDisponibles() {
        return new HashMap<>(estrategiasDisponibles);
    }

    /**
     * Obtiene candidatos disponibles para un partido.
     */
    private List<Cuenta> obtenerCandidatosDisponibles(Partido partido) {
        // Buscar por deporte favorito y ubicación cercana
        List<Cuenta> candidatosPorDeporte = cuentaRepository.findByDeporteFavoritoAndNivel(
            partido.getTipoDeporte(), null
        );

        // Si el partido tiene ubicación, buscar también por cercanía
        if (partido.getUbicacion() != null && 
            partido.getUbicacion().getLatitud() != null && 
            partido.getUbicacion().getLongitud() != null) {
            
            List<Cuenta> candidatosCercanos = cuentaRepository.findByUbicacionCercana(
                partido.getUbicacion().getLatitud(),
                partido.getUbicacion().getLongitud(),
                10.0 // 10km de radio por defecto
            );

            // Combinar y eliminar duplicados
            Set<Cuenta> todosLosCandidatos = new HashSet<>(candidatosPorDeporte);
            todosLosCandidatos.addAll(candidatosCercanos);
            
            return new ArrayList<>(todosLosCandidatos);
        }

        return candidatosPorDeporte;
    }

    /**
     * Clase para encapsular estadísticas de emparejamiento.
     */
    public static class EstadisticasEmparejamiento {
        private final int partidosJugados;
        private final double compatibilidadPromedio;
        private final int partidosSugeridos;
        private final String estrategiaUsada;

        public EstadisticasEmparejamiento(int partidosJugados, double compatibilidadPromedio, 
                                        int partidosSugeridos, String estrategiaUsada) {
            this.partidosJugados = partidosJugados;
            this.compatibilidadPromedio = compatibilidadPromedio;
            this.partidosSugeridos = partidosSugeridos;
            this.estrategiaUsada = estrategiaUsada;
        }

        // Getters
        public int getPartidosJugados() { return partidosJugados; }
        public double getCompatibilidadPromedio() { return compatibilidadPromedio; }
        public int getPartidosSugeridos() { return partidosSugeridos; }
        public String getEstrategiaUsada() { return estrategiaUsada; }

        @Override
        public String toString() {
            return String.format("EstadisticasEmparejamiento{jugados=%d, compatibilidad=%.2f, sugeridos=%d, estrategia=%s}",
                               partidosJugados, compatibilidadPromedio, partidosSugeridos, estrategiaUsada);
        }
    }
} 