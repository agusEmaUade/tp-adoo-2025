package com.tp.uno.mas.encuentros.deportivos.service;

import com.tp.uno.mas.encuentros.deportivos.model.IObservador;
import com.tp.uno.mas.encuentros.deportivos.model.Cuenta;
import com.tp.uno.mas.encuentros.deportivos.model.Partido;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Observador especializado en recopilar estadísticas del sistema.
 * Patrón Observer - Reacciona automáticamente a eventos para mantener estadísticas actualizadas.
 * Principio SOLID: Single Responsibility - Se enfoca únicamente en estadísticas.
 */
@Service
public class EstadisticasObservador implements IObservador {

    // Estadísticas globales del sistema
    private final Map<String, Object> estadisticasGlobales = new ConcurrentHashMap<>();
    
    // Estadísticas por usuario
    private final Map<Long, Map<String, Object>> estadisticasPorUsuario = new ConcurrentHashMap<>();
    
    // Estadísticas por partido
    private final Map<Long, Map<String, Object>> estadisticasPorPartido = new ConcurrentHashMap<>();

    public EstadisticasObservador() {
        inicializarEstadisticas();
    }

    @Override
    public void actualizar(String evento, Object datos) {
        switch (evento) {
            case "PARTIDO_CREADO" -> manejarPartidoCreado((Partido) datos);
            case "JUGADOR_AGREGADO" -> manejarJugadorAgregado((Map<String, Object>) datos);
            case "JUGADOR_REMOVIDO" -> manejarJugadorRemovido((Map<String, Object>) datos);
            case "ESTADO_CAMBIADO" -> manejarCambioEstado((Map<String, Object>) datos);
            case "PARTIDO_CONFIRMADO" -> manejarPartidoConfirmado((Partido) datos);
            case "PARTIDO_INICIADO" -> manejarPartidoIniciado((Partido) datos);
            case "PARTIDO_FINALIZADO" -> manejarPartidoFinalizado((Partido) datos);
            case "PARTIDO_CANCELADO" -> manejarPartidoCancelado((Partido) datos);
            case "USUARIO_REGISTRADO" -> manejarUsuarioRegistrado((Cuenta) datos);
            case "USUARIO_ACTUALIZADO" -> manejarUsuarioActualizado((Cuenta) datos);
            default -> registrarEventoGenerico(evento, datos);
        }
        
        // Actualizar timestamp de última actividad
        estadisticasGlobales.put("ultimaActualizacion", LocalDateTime.now());
    }

    /**
     * Maneja estadísticas cuando se crea un partido.
     */
    private void manejarPartidoCreado(Partido partido) {
        // Estadísticas globales
        incrementarContador("partidosCreados");
        incrementarContadorPorDeporte(partido.getTipoDeporte().toString());
        
        // Estadísticas del creador
        Long creadorId = partido.getCreador().getId();
        incrementarContadorUsuario(creadorId, "partidosCreados");
        
        // Inicializar estadísticas del partido
        Map<String, Object> statsPartido = new HashMap<>();
        statsPartido.put("fechaCreacion", LocalDateTime.now());
        statsPartido.put("tipoDeporte", partido.getTipoDeporte());
        statsPartido.put("creadorId", creadorId);
        statsPartido.put("capacidadMaxima", partido.getCantidadJugadores());
        statsPartido.put("jugadoresActuales", 1); // Solo el creador inicialmente
        
        estadisticasPorPartido.put(partido.getId(), statsPartido);
    }

    /**
     * Maneja estadísticas cuando se agrega un jugador.
     */
    private void manejarJugadorAgregado(Map<String, Object> datos) {
        Long partidoId = (Long) datos.get("partidoId");
        Long jugadorId = (Long) datos.get("jugadorId");
        
        // Estadísticas globales
        incrementarContador("jugadoresAgregados");
        
        // Estadísticas del jugador
        incrementarContadorUsuario(jugadorId, "partidosJugados");
        
        // Estadísticas del partido
        if (estadisticasPorPartido.containsKey(partidoId)) {
            Map<String, Object> statsPartido = estadisticasPorPartido.get(partidoId);
            int jugadoresActuales = (Integer) statsPartido.get("jugadoresActuales");
            statsPartido.put("jugadoresActuales", jugadoresActuales + 1);
            
            // Registrar tiempo de llenado del partido
            if (jugadoresActuales + 1 == (Integer) statsPartido.get("capacidadMaxima")) {
                LocalDateTime fechaCreacion = (LocalDateTime) statsPartido.get("fechaCreacion");
                long tiempoLlenado = java.time.Duration.between(fechaCreacion, LocalDateTime.now()).toMinutes();
                statsPartido.put("tiempoLlenadoMinutos", tiempoLlenado);
                
                // Estadística global de tiempo promedio
                actualizarTiempoPromedioLlenado(tiempoLlenado);
            }
        }
    }

    /**
     * Maneja estadísticas cuando se remueve un jugador.
     */
    private void manejarJugadorRemovido(Map<String, Object> datos) {
        Long partidoId = (Long) datos.get("partidoId");
        Long jugadorId = (Long) datos.get("jugadorId");
        
        // Estadísticas globales
        incrementarContador("jugadoresRemovidos");
        
        // Estadísticas del jugador
        incrementarContadorUsuario(jugadorId, "abandonos");
        
        // Estadísticas del partido
        if (estadisticasPorPartido.containsKey(partidoId)) {
            Map<String, Object> statsPartido = estadisticasPorPartido.get(partidoId);
            int jugadoresActuales = (Integer) statsPartido.get("jugadoresActuales");
            statsPartido.put("jugadoresActuales", Math.max(0, jugadoresActuales - 1));
        }
    }

    /**
     * Maneja estadísticas cuando cambia el estado de un partido.
     */
    private void manejarCambioEstado(Map<String, Object> datos) {
        String estadoAnterior = (String) datos.get("estadoAnterior");
        String estadoNuevo = (String) datos.get("estadoNuevo");
        Long partidoId = (Long) datos.get("partidoId");
        
        // Estadísticas de transiciones de estado
        String transicion = estadoAnterior + "_TO_" + estadoNuevo;
        incrementarContador("transiciones." + transicion);
        
        // Actualizar estadísticas del partido
        if (estadisticasPorPartido.containsKey(partidoId)) {
            Map<String, Object> statsPartido = estadisticasPorPartido.get(partidoId);
            statsPartido.put("estadoActual", estadoNuevo);
            statsPartido.put("ultimoCambioEstado", LocalDateTime.now());
        }
    }

    /**
     * Maneja estadísticas cuando se confirma un partido.
     */
    private void manejarPartidoConfirmado(Partido partido) {
        incrementarContador("partidosConfirmados");
        
        // Estadísticas por deporte
        incrementarContador("confirmados." + partido.getTipoDeporte().toString());
        
        // Actualizar estadísticas de todos los jugadores
        for (Cuenta jugador : partido.getJugadores()) {
            incrementarContadorUsuario(jugador.getId(), "partidosConfirmados");
        }
    }

    /**
     * Maneja estadísticas cuando se inicia un partido.
     */
    private void manejarPartidoIniciado(Partido partido) {
        incrementarContador("partidosIniciados");
        
        // Calcular puntualidad
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime horaProgramada = partido.getFechaHora();
        
        if (ahora.isAfter(horaProgramada)) {
            long minutosTarde = java.time.Duration.between(horaProgramada, ahora).toMinutes();
            actualizarPromedioRetraso(minutosTarde);
        }
    }

    /**
     * Maneja estadísticas cuando finaliza un partido.
     */
    private void manejarPartidoFinalizado(Partido partido) {
        incrementarContador("partidosFinalizados");
        
        // Estadísticas de finalización por deporte
        incrementarContador("finalizados." + partido.getTipoDeporte().toString());
        
        // Actualizar estadísticas de todos los jugadores
        for (Cuenta jugador : partido.getJugadores()) {
            incrementarContadorUsuario(jugador.getId(), "partidosCompletados");
        }
        
        // Calcular duración real vs programada si disponible
        if (estadisticasPorPartido.containsKey(partido.getId())) {
            Map<String, Object> statsPartido = estadisticasPorPartido.get(partido.getId());
            statsPartido.put("fechaFinalizacion", LocalDateTime.now());
            statsPartido.put("completado", true);
        }
    }

    /**
     * Maneja estadísticas cuando se cancela un partido.
     */
    private void manejarPartidoCancelado(Partido partido) {
        incrementarContador("partidosCancelados");
        
        // Estadísticas de cancelación por deporte
        incrementarContador("cancelados." + partido.getTipoDeporte().toString());
        
        // Calcular en qué etapa se canceló
        String estadoActual = partido.getEstadoNombre().toString();
        incrementarContador("canceladosEn." + estadoActual);
    }

    /**
     * Maneja estadísticas cuando se registra un usuario.
     */
    private void manejarUsuarioRegistrado(Cuenta usuario) {
        incrementarContador("usuariosRegistrados");
        
        // Estadísticas por deporte favorito si está definido
        if (usuario.getDeporteFavorito() != null) {
            incrementarContador("usuariosPor." + usuario.getDeporteFavorito().toString());
        }
        
        // Estadísticas por nivel si está definido
        if (usuario.getNivel() != null) {
            incrementarContador("usuariosPorNivel." + usuario.getNivel().toString());
        }
        
        // Inicializar estadísticas del usuario
        Map<String, Object> statsUsuario = new HashMap<>();
        statsUsuario.put("fechaRegistro", LocalDateTime.now());
        statsUsuario.put("partidosCreados", 0);
        statsUsuario.put("partidosJugados", 0);
        statsUsuario.put("partidosCompletados", 0);
        statsUsuario.put("abandonos", 0);
        
        estadisticasPorUsuario.put(usuario.getId(), statsUsuario);
    }

    /**
     * Maneja estadísticas cuando se actualiza un usuario.
     */
    private void manejarUsuarioActualizado(Cuenta usuario) {
        incrementarContador("usuariosActualizados");
        
        // Actualizar timestamp de última actividad del usuario
        if (estadisticasPorUsuario.containsKey(usuario.getId())) {
            estadisticasPorUsuario.get(usuario.getId()).put("ultimaActividad", LocalDateTime.now());
        }
    }

    /**
     * Registra eventos no específicos para análisis posterior.
     */
    private void registrarEventoGenerico(String evento, Object datos) {
        incrementarContador("eventosGenericos." + evento);
    }

    // Métodos de utilidad para estadísticas

    private void incrementarContador(String clave) {
        estadisticasGlobales.merge(clave, 1, (existente, nuevo) -> (Integer) existente + 1);
    }

    private void incrementarContadorPorDeporte(String deporte) {
        incrementarContador("partidosPor." + deporte);
    }

    private void incrementarContadorUsuario(Long usuarioId, String metrica) {
        estadisticasPorUsuario.computeIfAbsent(usuarioId, k -> new ConcurrentHashMap<>());
        Map<String, Object> statsUsuario = estadisticasPorUsuario.get(usuarioId);
        statsUsuario.merge(metrica, 1, (existente, nuevo) -> (Integer) existente + 1);
    }

    private void actualizarTiempoPromedioLlenado(long tiempoMinutos) {
        Integer partidosCompletos = (Integer) estadisticasGlobales.getOrDefault("partidosCompletos", 0);
        Long tiempoTotal = (Long) estadisticasGlobales.getOrDefault("tiempoTotalLlenado", 0L);
        
        estadisticasGlobales.put("partidosCompletos", partidosCompletos + 1);
        estadisticasGlobales.put("tiempoTotalLlenado", tiempoTotal + tiempoMinutos);
        estadisticasGlobales.put("tiempoPromedioLlenado", (tiempoTotal + tiempoMinutos) / (partidosCompletos + 1));
    }

    private void actualizarPromedioRetraso(long minutosTarde) {
        Integer partidosConRetraso = (Integer) estadisticasGlobales.getOrDefault("partidosConRetraso", 0);
        Long retrasoTotal = (Long) estadisticasGlobales.getOrDefault("retrasoTotalMinutos", 0L);
        
        estadisticasGlobales.put("partidosConRetraso", partidosConRetraso + 1);
        estadisticasGlobales.put("retrasoTotalMinutos", retrasoTotal + minutosTarde);
        estadisticasGlobales.put("retrasoPromedioMinutos", (retrasoTotal + minutosTarde) / (partidosConRetraso + 1));
    }

    private void inicializarEstadisticas() {
        // Inicializar contadores globales
        estadisticasGlobales.put("partidosCreados", 0);
        estadisticasGlobales.put("partidosConfirmados", 0);
        estadisticasGlobales.put("partidosFinalizados", 0);
        estadisticasGlobales.put("partidosCancelados", 0);
        estadisticasGlobales.put("usuariosRegistrados", 0);
        estadisticasGlobales.put("jugadoresAgregados", 0);
        estadisticasGlobales.put("jugadoresRemovidos", 0);
        estadisticasGlobales.put("inicializacion", LocalDateTime.now());
    }

    // Métodos públicos para consultar estadísticas

    /**
     * Obtiene estadísticas globales del sistema.
     */
    public Map<String, Object> obtenerEstadisticasGlobales() {
        return new HashMap<>(estadisticasGlobales);
    }

    /**
     * Obtiene estadísticas de un usuario específico.
     */
    public Map<String, Object> obtenerEstadisticasUsuario(Long usuarioId) {
        return new HashMap<>(estadisticasPorUsuario.getOrDefault(usuarioId, new HashMap<>()));
    }

    /**
     * Obtiene estadísticas de un partido específico.
     */
    public Map<String, Object> obtenerEstadisticasPartido(Long partidoId) {
        return new HashMap<>(estadisticasPorPartido.getOrDefault(partidoId, new HashMap<>()));
    }

    /**
     * Genera un resumen de estadísticas para reportes.
     */
    public Map<String, Object> generarResumenEstadisticas() {
        Map<String, Object> resumen = new HashMap<>();
        
        // Resumen general
        resumen.put("totalPartidos", estadisticasGlobales.getOrDefault("partidosCreados", 0));
        resumen.put("totalUsuarios", estadisticasGlobales.getOrDefault("usuariosRegistrados", 0));
        resumen.put("tasaFinalizacion", calcularTasaFinalizacion());
        resumen.put("tasaCancelacion", calcularTasaCancelacion());
        resumen.put("deporteMasPopular", obtenerDeporteMasPopular());
        
        return resumen;
    }

    private double calcularTasaFinalizacion() {
        int finalizados = (Integer) estadisticasGlobales.getOrDefault("partidosFinalizados", 0);
        int creados = (Integer) estadisticasGlobales.getOrDefault("partidosCreados", 0);
        return creados > 0 ? (double) finalizados / creados * 100 : 0.0;
    }

    private double calcularTasaCancelacion() {
        int cancelados = (Integer) estadisticasGlobales.getOrDefault("partidosCancelados", 0);
        int creados = (Integer) estadisticasGlobales.getOrDefault("partidosCreados", 0);
        return creados > 0 ? (double) cancelados / creados * 100 : 0.0;
    }

    private String obtenerDeporteMasPopular() {
        return estadisticasGlobales.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith("partidosPor."))
                .max(Map.Entry.comparingByValue())
                .map(entry -> entry.getKey().substring("partidosPor.".length()))
                .orElse("N/A");
    }
} 