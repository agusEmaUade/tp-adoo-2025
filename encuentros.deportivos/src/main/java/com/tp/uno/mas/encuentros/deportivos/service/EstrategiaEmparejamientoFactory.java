package com.tp.uno.mas.encuentros.deportivos.service;

import com.tp.uno.mas.encuentros.deportivos.model.*;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Factory para la creación de estrategias de emparejamiento.
 * Patrón Factory - centraliza la creación de estrategias de emparejamiento.
 * Principio SOLID: Single Responsibility - se enfoca únicamente en crear estrategias.
 */
@Service
public class EstrategiaEmparejamientoFactory {

    /**
     * Crea una estrategia de emparejamiento específica.
     * 
     * @param tipo Tipo de estrategia ("NIVEL", "CERCANIA", "HISTORIAL")
     * @return Instancia de la estrategia solicitada
     * @throws IllegalArgumentException si el tipo no es válido
     */
    public IEstrategiaEmparejamiento crearEstrategia(String tipo) {
        Objects.requireNonNull(tipo, "El tipo de estrategia no puede ser nulo");
        
        return switch (tipo.toUpperCase()) {
            case "NIVEL" -> new EstrategiaPorNivel();
            case "CERCANIA" -> new EstrategiaPorCercania();
            case "HISTORIAL" -> new EstrategiaPorHistorial();
            default -> throw new IllegalArgumentException("Tipo de estrategia no válido: " + tipo);
        };
    }

    /**
     * Crea una estrategia de emparejamiento con configuración específica.
     * 
     * @param tipo Tipo de estrategia
     * @param configuracion Parámetros de configuración
     * @return Estrategia configurada
     */
    public IEstrategiaEmparejamiento crearEstrategia(String tipo, Map<String, Object> configuracion) {
        IEstrategiaEmparejamiento estrategia = crearEstrategia(tipo);
        
        if (configuracion != null && !configuracion.isEmpty()) {
            configurarEstrategia(estrategia, tipo, configuracion);
        }
        
        return estrategia;
    }

    /**
     * Crea la estrategia por nivel con tolerancia específica.
     * 
     * @param toleranciaNivel Tolerancia para la diferencia de niveles
     * @return Estrategia por nivel configurada
     */
    public IEstrategiaEmparejamiento crearPorNivel(int toleranciaNivel) {
        if (toleranciaNivel < 0) {
            throw new IllegalArgumentException("La tolerancia de nivel debe ser no negativa");
        }
        
        Map<String, Object> config = new HashMap<>();
        config.put("tolerancia", toleranciaNivel);
        
        return crearEstrategia("NIVEL", config);
    }

    /**
     * Crea la estrategia por cercanía con radio específico.
     * 
     * @param radioMaximoKm Radio máximo en kilómetros
     * @return Estrategia por cercanía configurada
     */
    public IEstrategiaEmparejamiento crearPorCercania(double radioMaximoKm) {
        if (radioMaximoKm <= 0) {
            throw new IllegalArgumentException("El radio máximo debe ser positivo");
        }
        
        Map<String, Object> config = new HashMap<>();
        config.put("radioMaximo", radioMaximoKm);
        
        return crearEstrategia("CERCANIA", config);
    }

    /**
     * Crea la estrategia por historial con peso específico.
     * 
     * @param pesoHistorial Peso del historial en el cálculo (0.0 - 1.0)
     * @return Estrategia por historial configurada
     */
    public IEstrategiaEmparejamiento crearPorHistorial(double pesoHistorial) {
        if (pesoHistorial < 0.0 || pesoHistorial > 1.0) {
            throw new IllegalArgumentException("El peso del historial debe estar entre 0.0 y 1.0");
        }
        
        Map<String, Object> config = new HashMap<>();
        config.put("peso", pesoHistorial);
        
        return crearEstrategia("HISTORIAL", config);
    }

    /**
     * Crea una estrategia combinada que usa múltiples criterios.
     * 
     * @param estrategias Lista de tipos de estrategia a combinar
     * @param pesos Pesos para cada estrategia (deben sumar 1.0)
     * @return Estrategia combinada
     */
    public IEstrategiaEmparejamiento crearEstrategiaCombinada(List<String> estrategias, List<Double> pesos) {
        Objects.requireNonNull(estrategias, "La lista de estrategias no puede ser nula");
        Objects.requireNonNull(pesos, "Los pesos no pueden ser nulos");
        
        if (estrategias.size() != pesos.size()) {
            throw new IllegalArgumentException("El número de estrategias debe coincidir con el número de pesos");
        }
        
        double sumaPesos = pesos.stream().mapToDouble(Double::doubleValue).sum();
        if (Math.abs(sumaPesos - 1.0) > 0.01) {
            throw new IllegalArgumentException("Los pesos deben sumar 1.0 (suma actual: " + sumaPesos + ")");
        }
        
        // Para este ejemplo, retornamos la primera estrategia
        // En una implementación completa, se podría crear una EstrategiaCombinada
        return crearEstrategia(estrategias.get(0));
    }

    /**
     * Crea una estrategia basada en las preferencias del usuario.
     * 
     * @param cuenta Cuenta del usuario con sus preferencias
     * @return Estrategia más apropiada según el perfil del usuario
     */
    public IEstrategiaEmparejamiento crearSegunPerfil(Cuenta cuenta) {
        Objects.requireNonNull(cuenta, "La cuenta no puede ser nula");
        
        // Lógica para determinar la mejor estrategia según el perfil
        if (cuenta.getNivel() != null) {
            // Si tiene nivel definido, priorizar emparejamiento por nivel
            return crearPorNivel(1); // Tolerancia de 1 nivel
        } else if (cuenta.getUbicacion() != null) {
            // Si no tiene nivel pero sí ubicación, usar cercanía
            return crearPorCercania(10.0); // Radio de 10km
        } else {
            // Por defecto, usar historial
            return crearPorHistorial(0.7);
        }
    }

    /**
     * Crea la estrategia más apropiada para un tipo de deporte.
     * 
     * @param tipoDeporte Deporte para el cual optimizar
     * @return Estrategia optimizada para el deporte
     */
    public IEstrategiaEmparejamiento crearParaDeporte(TipoDeporte tipoDeporte) {
        Objects.requireNonNull(tipoDeporte, "El tipo de deporte no puede ser nulo");
        
        return switch (tipoDeporte) {
            case FUTBOL, BASQUET -> crearPorNivel(1); // Deportes donde el nivel es crítico
            case TENIS, PADEL -> crearPorCercania(5.0); // Deportes más locales
            case VOLEY -> crearPorHistorial(0.8); // Deporte donde importa la química
            default -> crearEstrategia("NIVEL"); // Por defecto, nivel
        };
    }

    /**
     * Obtiene todas las estrategias disponibles.
     * 
     * @return Lista con todas las estrategias disponibles
     */
    public List<IEstrategiaEmparejamiento> obtenerTodasLasEstrategias() {
        List<IEstrategiaEmparejamiento> estrategias = new ArrayList<>();
        
        for (String tipo : obtenerEstrategiasDisponibles()) {
            estrategias.add(crearEstrategia(tipo));
        }
        
        return estrategias;
    }

    /**
     * Obtiene la lista de tipos de estrategia disponibles.
     * 
     * @return Lista con nombres de estrategias disponibles
     */
    public List<String> obtenerEstrategiasDisponibles() {
        return Arrays.asList("NIVEL", "CERCANIA", "HISTORIAL");
    }

    /**
     * Valida si un tipo de estrategia es válido.
     * 
     * @param tipo Nombre de la estrategia a validar
     * @return true si la estrategia es válida
     */
    public boolean validarTipoEstrategia(String tipo) {
        if (tipo == null || tipo.trim().isEmpty()) {
            return false;
        }
        
        return obtenerEstrategiasDisponibles().contains(tipo.toUpperCase());
    }

    /**
     * Crea estrategias optimizadas para diferentes escenarios.
     * 
     * @param escenario Tipo de escenario ("COMPETITIVO", "CASUAL", "SOCIAL")
     * @return Estrategia optimizada para el escenario
     */
    public IEstrategiaEmparejamiento crearParaEscenario(String escenario) {
        Objects.requireNonNull(escenario, "El escenario no puede ser nulo");
        
        return switch (escenario.toUpperCase()) {
            case "COMPETITIVO" -> crearPorNivel(0); // Nivel exacto
            case "CASUAL" -> crearPorCercania(15.0); // Priorizar cercanía
            case "SOCIAL" -> crearPorHistorial(0.9); // Priorizar conocidos
            default -> crearEstrategia("NIVEL"); // Por defecto
        };
    }

    /**
     * Obtiene recomendaciones de estrategia basadas en estadísticas.
     * 
     * @param estadisticas Mapa con estadísticas del usuario
     * @return Estrategia recomendada
     */
    public IEstrategiaEmparejamiento obtenerRecomendacion(Map<String, Object> estadisticas) {
        Objects.requireNonNull(estadisticas, "Las estadísticas no pueden ser nulas");
        
        // Analizar estadísticas para recomendar estrategia
        Object partidosJugados = estadisticas.get("partidosJugados");
        Object nivelPromedio = estadisticas.get("nivelPromedio");
        
        if (partidosJugados instanceof Integer && (Integer) partidosJugados > 10) {
            return crearPorHistorial(0.8); // Usuario experimentado
        } else if (nivelPromedio != null) {
            return crearPorNivel(1); // Usuario con nivel definido
        } else {
            return crearPorCercania(10.0); // Usuario nuevo
        }
    }

    /**
     * Configura una estrategia específica con parámetros.
     */
    private void configurarEstrategia(IEstrategiaEmparejamiento estrategia, String tipo, Map<String, Object> config) {
        // En una implementación completa, cada estrategia tendría métodos de configuración
        // Por ahora, la configuración se maneja en la creación
        
        switch (tipo.toUpperCase()) {
            case "NIVEL" -> {
                // Configuración específica para nivel
                if (config.containsKey("tolerancia")) {
                    // Configurar tolerancia
                }
            }
            case "CERCANIA" -> {
                // Configuración específica para cercanía
                if (config.containsKey("radioMaximo")) {
                    // Configurar radio máximo
                }
            }
            case "HISTORIAL" -> {
                // Configuración específica para historial  
                if (config.containsKey("peso")) {
                    // Configurar peso del historial
                }
            }
        }
    }
} 