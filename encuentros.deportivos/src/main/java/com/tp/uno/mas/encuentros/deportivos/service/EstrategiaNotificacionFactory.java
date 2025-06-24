package com.tp.uno.mas.encuentros.deportivos.service;

import com.tp.uno.mas.encuentros.deportivos.model.IEstrategiaNotificacion;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Factory para la creación de estrategias de notificación.
 * Patrón Factory - centraliza la creación de estrategias de notificación.
 * Principio SOLID: Single Responsibility - se enfoca únicamente en crear estrategias.
 */
@Service
public class EstrategiaNotificacionFactory {

    /**
     * Crea una estrategia de notificación específica con configuración.
     * 
     * @param tipo Tipo de estrategia ("FIREBASE", "JAVAMAIL")
     * @param configuracion Mapa con parámetros de configuración
     * @return Instancia configurada de la estrategia
     * @throws IllegalArgumentException si el tipo no es válido
     */
    public IEstrategiaNotificacion crearEstrategia(String tipo, Map<String, Object> configuracion) {
        Objects.requireNonNull(tipo, "El tipo de estrategia no puede ser nulo");
        
        IEstrategiaNotificacion estrategia = switch (tipo.toUpperCase()) {
            case "FIREBASE" -> new Firebase();
            case "JAVAMAIL" -> new JavaMail();
            default -> throw new IllegalArgumentException("Tipo de estrategia no válido: " + tipo);
        };

        // Configurar la estrategia si se proporcionó configuración
        if (configuracion != null && !configuracion.isEmpty()) {
            if (!validarConfiguracion(tipo, configuracion)) {
                throw new IllegalArgumentException("Configuración inválida para estrategia: " + tipo);
            }
            estrategia.configurar(configuracion);
        }

        return estrategia;
    }

    /**
     * Crea una estrategia con configuración por defecto.
     * 
     * @param tipo Tipo de estrategia a crear
     * @return Instancia con configuración por defecto
     */
    public IEstrategiaNotificacion crearEstrategia(String tipo) {
        return crearEstrategia(tipo, obtenerConfiguracionPorDefecto(tipo));
    }

    /**
     * Crea una estrategia Firebase configurada.
     * 
     * @param apiKey Clave de API de Firebase
     * @param projectId ID del proyecto Firebase
     * @return Estrategia Firebase configurada
     */
    public IEstrategiaNotificacion crearFirebase(String apiKey, String projectId) {
        Objects.requireNonNull(apiKey, "La API Key no puede ser nula");
        Objects.requireNonNull(projectId, "El Project ID no puede ser nulo");

        Map<String, Object> config = new HashMap<>();
        config.put("apiKey", apiKey);
        config.put("projectId", projectId);

        return crearEstrategia("FIREBASE", config);
    }

    /**
     * Crea una estrategia JavaMail configurada.
     * 
     * @param smtpHost Servidor SMTP
     * @param smtpPort Puerto SMTP
     * @param username Usuario para autenticación
     * @param password Contraseña para autenticación
     * @return Estrategia JavaMail configurada
     */
    public IEstrategiaNotificacion crearJavaMail(String smtpHost, int smtpPort, 
                                               String username, String password) {
        Objects.requireNonNull(smtpHost, "El host SMTP no puede ser nulo");
        Objects.requireNonNull(username, "El username no puede ser nulo");
        Objects.requireNonNull(password, "La password no puede ser nula");

        Map<String, Object> config = new HashMap<>();
        config.put("smtpHost", smtpHost);
        config.put("smtpPort", smtpPort);
        config.put("username", username);
        config.put("password", password);

        return crearEstrategia("JAVAMAIL", config);
    }

    /**
     * Obtiene todas las estrategias disponibles preconfiguradas.
     * 
     * @return Lista con todas las estrategias disponibles
     */
    public List<IEstrategiaNotificacion> obtenerTodasLasEstrategias() {
        List<IEstrategiaNotificacion> estrategias = new ArrayList<>();
        
        for (String tipo : obtenerEstrategiasDisponibles()) {
            try {
                estrategias.add(crearEstrategia(tipo));
            } catch (Exception e) {
                // Log del error pero continuar con otras estrategias
                System.err.println("Error creando estrategia " + tipo + ": " + e.getMessage());
            }
        }
        
        return estrategias;
    }

    /**
     * Obtiene la lista de tipos de estrategia disponibles.
     * 
     * @return Lista con nombres de estrategias disponibles
     */
    public List<String> obtenerEstrategiasDisponibles() {
        return Arrays.asList("FIREBASE", "JAVAMAIL");
    }

    /**
     * Valida la configuración para un tipo específico de estrategia.
     * 
     * @param tipo Tipo de estrategia
     * @param configuracion Configuración a validar
     * @return true si la configuración es válida
     */
    public boolean validarConfiguracion(String tipo, Map<String, Object> configuracion) {
        if (configuracion == null) {
            return false;
        }

        return switch (tipo.toUpperCase()) {
            case "FIREBASE" -> validarConfiguracionFirebase(configuracion);
            case "JAVAMAIL" -> validarConfiguracionJavaMail(configuracion);
            default -> false;
        };
    }

    /**
     * Crea una estrategia basada en las preferencias del usuario.
     * 
     * @param preferenciasUsuario Preferencias de notificación
     * @return Estrategia más apropiada según preferencias
     */
    public IEstrategiaNotificacion crearEstrategiaSegunPreferencias(Map<String, Object> preferenciasUsuario) {
        Objects.requireNonNull(preferenciasUsuario, "Las preferencias no pueden ser nulas");

        // Lógica para determinar la mejor estrategia según preferencias
        if (preferenciasUsuario.containsKey("push") && 
            Boolean.TRUE.equals(preferenciasUsuario.get("push"))) {
            return crearEstrategia("FIREBASE");
        } else if (preferenciasUsuario.containsKey("email") && 
                   Boolean.TRUE.equals(preferenciasUsuario.get("email"))) {
            return crearEstrategia("JAVAMAIL");
        }

        // Por defecto, retornar Firebase
        return crearEstrategia("FIREBASE");
    }

    /**
     * Obtiene la configuración por defecto para un tipo de estrategia.
     */
    private Map<String, Object> obtenerConfiguracionPorDefecto(String tipo) {
        Map<String, Object> config = new HashMap<>();
        
        switch (tipo.toUpperCase()) {
            case "FIREBASE" -> {
                config.put("apiKey", "default-firebase-key");
                config.put("projectId", "encuentros-deportivos");
            }
            case "JAVAMAIL" -> {
                config.put("smtpHost", "smtp.gmail.com");
                config.put("smtpPort", 587);
                config.put("username", "noreply@encuentrosdeportivos.com");
                config.put("password", "default-password");
            }
        }
        
        return config;
    }

    /**
     * Valida configuración específica de Firebase.
     */
    private boolean validarConfiguracionFirebase(Map<String, Object> config) {
        return config.containsKey("apiKey") && config.get("apiKey") instanceof String &&
               config.containsKey("projectId") && config.get("projectId") instanceof String;
    }

    /**
     * Valida configuración específica de JavaMail.
     */
    private boolean validarConfiguracionJavaMail(Map<String, Object> config) {
        return config.containsKey("smtpHost") && config.get("smtpHost") instanceof String &&
               config.containsKey("smtpPort") && config.get("smtpPort") instanceof Integer &&
               config.containsKey("username") && config.get("username") instanceof String;
    }
} 