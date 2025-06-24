package com.tp.uno.mas.encuentros.deportivos.service;

import com.tp.uno.mas.encuentros.deportivos.dto.*;
import com.tp.uno.mas.encuentros.deportivos.model.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Facade para simplificar las operaciones complejas del sistema.
 * Patrón Facade - Proporciona una interfaz unificada para un conjunto de interfaces.
 * Principio SOLID: Interface Segregation - Proporciona métodos específicos para cada operación.
 */
@Service
public class GestorPartidosFacade {

    @Autowired
    private PartidoService partidoService;
    
    @Autowired
    private CuentaService cuentaService;
    
    @Autowired
    private EmparejadorService emparejadorService;
    
    @Autowired
    private Notificador notificador;
    
    @Autowired
    private NotificadorObservador notificadorObservador;
    
    @Autowired
    private EstadoFactory estadoFactory;
    
    @Autowired
    private EstrategiaNotificacionFactory estrategiaNotificacionFactory;
    
    @Autowired
    private EstrategiaEmparejamientoFactory estrategiaEmparejamientoFactory;

    /**
     * Operación completa para crear un partido con todas las configuraciones.
     * 
     * @param creadorId ID del usuario creador
     * @param partidoDto Datos del partido a crear
     * @param configuracionEmparejamiento Configuración para emparejamiento
     * @return Partido creado con estado inicial
     */
    public PartidoDTO crearPartidoCompleto(Long creadorId, PartidoDTO partidoDto, 
                                         Map<String, Object> configuracionEmparejamiento) {
        Objects.requireNonNull(creadorId, "El ID del creador no puede ser nulo");
        Objects.requireNonNull(partidoDto, "Los datos del partido no pueden ser nulos");

        try {
            // 1. Validar que el creador existe
            Cuenta creador = cuentaService.buscarPorId(creadorId);
            if (creador == null) {
                throw new IllegalArgumentException("El usuario creador no existe");
            }

            // 2. Crear el partido base
            Partido partido = partidoService.crearPartido(partidoDto, creadorId);

            // 3. Configurar estado inicial usando Factory
            IEstado estadoInicial = estadoFactory.crearEstadoInicial();
            partido.setEstado(estadoInicial);

            // 4. Configurar observadores para notificaciones automáticas
            partido.agregarObservador(notificadorObservador);

            // 5. Configurar estrategia de emparejamiento si se especificó
            if (configuracionEmparejamiento != null && !configuracionEmparejamiento.isEmpty()) {
                configurarEmparejamientoAutomatico(partido, configuracionEmparejamiento);
            }

            // 6. Notificar creación del partido
            notificarEventoPartido(partido, "PARTIDO_CREADO", creador);

            // 7. Guardar partido
            Partido partidoGuardado = partidoService.guardarPartido(partido);

            return convertirADTO(partidoGuardado);

        } catch (Exception e) {
            // Log del error y relanzar con mensaje más específico
            String mensaje = "Error al crear partido completo: " + e.getMessage();
            throw new RuntimeException(mensaje, e);
        }
    }

    /**
     * Busca jugadores compatibles y los invita automáticamente.
     * 
     * @param partidoId ID del partido
     * @param estrategiaEmparejamiento Tipo de estrategia ("NIVEL", "CERCANIA", "HISTORIAL")
     * @param configuracion Parámetros adicionales de configuración
     * @return Lista de jugadores encontrados e invitados
     */
    public List<CuentaDTO> buscarEInvitarJugadores(Long partidoId, String estrategiaEmparejamiento, 
                                                 Map<String, Object> configuracion) {
        Objects.requireNonNull(partidoId, "El ID del partido no puede ser nulo");
        Objects.requireNonNull(estrategiaEmparejamiento, "La estrategia no puede ser nula");

        try {
            // 1. Obtener el partido
            Partido partido = partidoService.buscarPorId(partidoId);
            if (partido == null) {
                throw new IllegalArgumentException("El partido no existe");
            }

            // 2. Verificar que el partido necesita jugadores
            if (!puedeAgregarJugadores(partido)) {
                throw new IllegalStateException("El partido no puede agregar más jugadores en su estado actual");
            }

            // 3. Crear estrategia de emparejamiento
            IEstrategiaEmparejamiento estrategia = estrategiaEmparejamientoFactory
                    .crearEstrategia(estrategiaEmparejamiento, configuracion);

            // 4. Buscar jugadores compatibles
            List<Cuenta> candidatos = emparejadorService.buscarCandidatos(partido, estrategia);

            // 5. Calcular cuántos jugadores se necesitan
            int jugadoresNecesarios = calcularJugadoresNecesarios(partido);
            List<Cuenta> jugadoresSeleccionados = candidatos.stream()
                    .limit(jugadoresNecesarios)
                    .collect(Collectors.toList());

            // 6. Invitar a los jugadores seleccionados
            List<Cuenta> jugadoresInvitados = new ArrayList<>();
            for (Cuenta jugador : jugadoresSeleccionados) {
                if (invitarJugador(partido, jugador)) {
                    jugadoresInvitados.add(jugador);
                }
            }

            return jugadoresInvitados.stream()
                    .map(this::convertirADTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            String mensaje = "Error al buscar e invitar jugadores: " + e.getMessage();
            throw new RuntimeException(mensaje, e);
        }
    }

    /**
     * Gestiona la inscripción de un jugador con validaciones y notificaciones.
     * 
     * @param partidoId ID del partido
     * @param jugadorId ID del jugador que se inscribe
     * @return true si la inscripción fue exitosa
     */
    public boolean inscribirJugadorConValidaciones(Long partidoId, Long jugadorId) {
        Objects.requireNonNull(partidoId, "El ID del partido no puede ser nulo");
        Objects.requireNonNull(jugadorId, "El ID del jugador no puede ser nulo");

        try {
            // 1. Validar que el partido y jugador existen
            Partido partido = partidoService.buscarPorId(partidoId);
            Cuenta jugador = cuentaService.buscarPorId(jugadorId);

            if (partido == null || jugador == null) {
                return false;
            }

            // 2. Validar pre-condiciones
            if (!validarInscripcion(partido, jugador)) {
                return false;
            }

            // 3. Agregar jugador al partido
            boolean inscripcionExitosa = partidoService.agregarJugador(partidoId, jugadorId);

            if (inscripcionExitosa) {
                // 4. Verificar si el partido está completo y cambiar estado
                actualizarEstadoSegunCapacidad(partido);

                // 5. Notificar a todos los interesados
                notificarInscripcionJugador(partido, jugador);

                // 6. Configurar observador para el nuevo jugador
                jugador.agregarObservador(notificadorObservador);
            }

            return inscripcionExitosa;

        } catch (Exception e) {
            String mensaje = "Error al inscribir jugador: " + e.getMessage();
            throw new RuntimeException(mensaje, e);
        }
    }

    /**
     * Confirma un partido y prepara todo para el inicio.
     * 
     * @param partidoId ID del partido a confirmar
     * @param configuracionNotificacion Configuración para notificaciones
     * @return true si la confirmación fue exitosa
     */
    public boolean confirmarPartidoCompleto(Long partidoId, Map<String, Object> configuracionNotificacion) {
        Objects.requireNonNull(partidoId, "El ID del partido no puede ser nulo");

        try {
            // 1. Obtener el partido
            Partido partido = partidoService.buscarPorId(partidoId);
            if (partido == null) {
                return false;
            }

            // 2. Validar que el partido puede ser confirmado
            if (!puedeConfirmarPartido(partido)) {
                return false;
            }

            // 3. Cambiar estado a confirmado usando Factory
            IEstado estadoConfirmado = estadoFactory.crearEstado("CONFIRMADO");
            partido.setEstado(estadoConfirmado);

            // 4. Configurar notificaciones especiales para el partido confirmado
            if (configuracionNotificacion != null) {
                configurarNotificacionesConfirmacion(partido, configuracionNotificacion);
            }

            // 5. Notificar confirmación a todos los jugadores
            notificarConfirmacionPartido(partido);

            // 6. Programar recordatorios automáticos
            programarRecordatorios(partido);

            // 7. Guardar cambios
            partidoService.guardarPartido(partido);

            return true;

        } catch (Exception e) {
            String mensaje = "Error al confirmar partido: " + e.getMessage();
            throw new RuntimeException(mensaje, e);
        }
    }

    /**
     * Cancela un partido con todas las notificaciones y limpieza correspondiente.
     * 
     * @param partidoId ID del partido a cancelar
     * @param motivo Motivo de la cancelación
     * @param usuarioId ID del usuario que cancela
     * @return true si la cancelación fue exitosa
     */
    public boolean cancelarPartidoCompleto(Long partidoId, String motivo, Long usuarioId) {
        Objects.requireNonNull(partidoId, "El ID del partido no puede ser nulo");
        Objects.requireNonNull(usuarioId, "El ID del usuario no puede ser nulo");

        try {
            // 1. Obtener partido y usuario
            Partido partido = partidoService.buscarPorId(partidoId);
            Cuenta usuario = cuentaService.buscarPorId(usuarioId);

            if (partido == null || usuario == null) {
                return false;
            }

            // 2. Validar permisos de cancelación
            if (!puedeCancel(partido, usuario)) {
                return false;
            }

            // 3. Cambiar estado a cancelado usando Factory
            IEstado estadoCancelado = estadoFactory.crearEstado("CANCELADO");
            partido.setEstado(estadoCancelado);

            // 4. Notificar cancelación a todos los jugadores
            notificarCancelacionPartido(partido, motivo, usuario);

            // 5. Limpiar recordatorios programados
            cancelarRecordatorios(partido);

            // 6. Actualizar estadísticas de usuarios
            actualizarEstadisticasCancelacion(partido);

            // 7. Guardar cambios
            partidoService.guardarPartido(partido);

            return true;

        } catch (Exception e) {
            String mensaje = "Error al cancelar partido: " + e.getMessage();
            throw new RuntimeException(mensaje, e);
        }
    }

    /**
     * Busca partidos con filtros avanzados y recomendaciones personalizadas.
     * 
     * @param filtros Filtros de búsqueda
     * @param usuarioId ID del usuario para personalizar resultados
     * @return Lista de partidos recomendados
     */
    public List<PartidoDTO> buscarPartidosRecomendados(FiltroPartidoDTO filtros, Long usuarioId) {
        try {
            // 1. Obtener usuario para personalización
            Cuenta usuario = null;
            if (usuarioId != null) {
                usuario = cuentaService.buscarPorId(usuarioId);
            }

            // 2. Buscar partidos con filtros básicos
            List<Partido> partidosBase = partidoService.buscarConFiltros(filtros);

            // 3. Aplicar algoritmo de recomendación si hay usuario
            if (usuario != null) {
                partidosBase = aplicarRecomendacionPersonalizada(partidosBase, usuario);
            }

            // 4. Ordenar por relevancia
            partidosBase = ordenarPorRelevancia(partidosBase, usuario);

            // 5. Convertir a DTOs
            return partidosBase.stream()
                    .map(this::convertirADTO)
                    .collect(Collectors.toList());

        } catch (Exception e) {
            String mensaje = "Error al buscar partidos recomendados: " + e.getMessage();
            throw new RuntimeException(mensaje, e);
        }
    }

    /**
     * Obtiene estadísticas completas de un usuario.
     * 
     * @param usuarioId ID del usuario
     * @return Mapa con estadísticas detalladas
     */
    public Map<String, Object> obtenerEstadisticasCompletas(Long usuarioId) {
        Objects.requireNonNull(usuarioId, "El ID del usuario no puede ser nulo");

        try {
            Map<String, Object> estadisticas = new HashMap<>();

            // 1. Estadísticas básicas del usuario
            Cuenta usuario = cuentaService.buscarPorId(usuarioId);
            if (usuario == null) {
                return estadisticas;
            }

            // 2. Estadísticas de partidos
            List<Partido> partidosJugados = partidoService.buscarPartidosPorJugador(usuarioId);
            estadisticas.put("partidosJugados", partidosJugados.size());
            estadisticas.put("partidosGanados", calcularPartidosGanados(partidosJugados, usuarioId));
            estadisticas.put("deporteFavorito", calcularDeporteFavorito(partidosJugados));

            // 3. Estadísticas de rendimiento
            estadisticas.put("nivelPromedio", calcularNivelPromedio(partidosJugados));
            estadisticas.put("asistencia", calcularPorcentajeAsistencia(partidosJugados, usuarioId));

            // 4. Estadísticas sociales
            estadisticas.put("jugadoresDiferentes", calcularJugadoresDiferentes(partidosJugados, usuarioId));
            estadisticas.put("partidosOrganizados", partidosJugados.stream()
                    .filter(p -> p.getCreador().getId().equals(usuarioId))
                    .count());

            return estadisticas;

        } catch (Exception e) {
            String mensaje = "Error al obtener estadísticas: " + e.getMessage();
            throw new RuntimeException(mensaje, e);
        }
    }

    // Métodos privados de apoyo

    private void configurarEmparejamientoAutomatico(Partido partido, Map<String, Object> configuracion) {
        // Implementar configuración de emparejamiento automático
    }

    private void notificarEventoPartido(Partido partido, String evento, Cuenta usuario) {
        // Usar el sistema de notificaciones para enviar alertas
        IEstrategiaNotificacion estrategia = estrategiaNotificacionFactory.crearEstrategia("FIREBASE");
        notificador.notificar("Evento de partido: " + evento, usuario, estrategia);
    }

    private boolean puedeAgregarJugadores(Partido partido) {
        return partido.getJugadores().size() < partido.getCantidadJugadores() &&
               (partido.getEstadoNombre() == Partido.EstadoEnum.NECESITAMOS_JUGADORES ||
                partido.getEstadoNombre() == Partido.EstadoEnum.PARTIDO_ARMADO);
    }

    private int calcularJugadoresNecesarios(Partido partido) {
        return partido.getCantidadJugadores() - partido.getJugadores().size();
    }

    private boolean invitarJugador(Partido partido, Cuenta jugador) {
        // Implementar lógica de invitación
        return true;
    }

    private boolean validarInscripcion(Partido partido, Cuenta jugador) {
        // Validar que el jugador puede inscribirse
        return puedeAgregarJugadores(partido) && !partido.getJugadores().contains(jugador);
    }

    private void actualizarEstadoSegunCapacidad(Partido partido) {
        if (partido.getJugadores().size() >= partido.getCantidadJugadores()) {
            IEstado estadoArmado = estadoFactory.crearEstado("PARTIDO_ARMADO");
            partido.setEstado(estadoArmado);
        }
    }

    private void notificarInscripcionJugador(Partido partido, Cuenta jugador) {
        // Notificar a todos los jugadores del partido
        for (Cuenta j : partido.getJugadores()) {
            if (!j.equals(jugador)) {
                IEstrategiaNotificacion estrategia = estrategiaNotificacionFactory.crearEstrategia("JAVAMAIL");
                notificador.notificar("Nuevo jugador se unió al partido", j, estrategia);
            }
        }
    }

    private boolean puedeConfirmarPartido(Partido partido) {
        return partido.getEstadoNombre() == Partido.EstadoEnum.PARTIDO_ARMADO &&
               partido.getJugadores().size() >= partido.getCantidadJugadores();
    }

    private void configurarNotificacionesConfirmacion(Partido partido, Map<String, Object> config) {
        // Configurar notificaciones especiales para partidos confirmados
    }

    private void notificarConfirmacionPartido(Partido partido) {
        // Notificar confirmación a todos los jugadores
    }

    private void programarRecordatorios(Partido partido) {
        // Programar recordatorios automáticos
    }

    private boolean puedeCancel(Partido partido, Cuenta usuario) {
        // Validar si el usuario puede cancelar el partido
        return partido.getCreador().equals(usuario) || 
               partido.getJugadores().contains(usuario);
    }

    private void notificarCancelacionPartido(Partido partido, String motivo, Cuenta usuario) {
        // Notificar cancelación
    }

    private void cancelarRecordatorios(Partido partido) {
        // Cancelar recordatorios programados
    }

    private void actualizarEstadisticasCancelacion(Partido partido) {
        // Actualizar estadísticas tras cancelación
    }

    private List<Partido> aplicarRecomendacionPersonalizada(List<Partido> partidos, Cuenta usuario) {
        // Aplicar algoritmo de recomendación personalizada
        return partidos;
    }

    private List<Partido> ordenarPorRelevancia(List<Partido> partidos, Cuenta usuario) {
        // Ordenar partidos por relevancia para el usuario
        return partidos;
    }

    private int calcularPartidosGanados(List<Partido> partidos, Long usuarioId) {
        // Calcular partidos ganados
        return 0;
    }

    private TipoDeporte calcularDeporteFavorito(List<Partido> partidos) {
        // Calcular deporte más jugado
        return partidos.isEmpty() ? null : partidos.get(0).getTipoDeporte();
    }

    private double calcularNivelPromedio(List<Partido> partidos) {
        // Calcular nivel promedio
        return 0.0;
    }

    private double calcularPorcentajeAsistencia(List<Partido> partidos, Long usuarioId) {
        // Calcular porcentaje de asistencia
        return 0.0;
    }

    private int calcularJugadoresDiferentes(List<Partido> partidos, Long usuarioId) {
        // Calcular cantidad de jugadores diferentes con los que jugó
        return 0;
    }

    private PartidoDTO convertirADTO(Partido partido) {
        PartidoDTO dto = new PartidoDTO();
        dto.setId(partido.getId());
        dto.setTitulo(partido.getTitulo());
        dto.setDescripcion(partido.getDescripcion());
        dto.setTipoDeporte(partido.getTipoDeporte());
        dto.setFechaHora(partido.getFechaHora());
        dto.setCantidadJugadores(partido.getCantidadJugadores());
        dto.setEstado(partido.getEstadoNombre());
        return dto;
    }

    private CuentaDTO convertirADTO(Cuenta cuenta) {
        CuentaDTO dto = new CuentaDTO();
        dto.setId(cuenta.getId());
        dto.setNombreUsuario(cuenta.getNombreUsuario());
        dto.setEmail(cuenta.getEmail());
        dto.setDeporteFavorito(cuenta.getDeporteFavorito());
        dto.setNivel(cuenta.getNivel());
        return dto;
    }
} 