package com.tp.uno.mas.encuentros.deportivos.service;

import com.tp.uno.mas.encuentros.deportivos.dto.*;
import com.tp.uno.mas.encuentros.deportivos.model.*;
import com.tp.uno.mas.encuentros.deportivos.repository.CuentaRepository;
import com.tp.uno.mas.encuentros.deportivos.repository.PartidoRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de partidos deportivos.
 * Implementa la lógica de negocio compleja incluyendo manejo de estados y notificaciones.
 * Principio SOLID: Single Responsibility - solo maneja lógica de partidos.
 * Principio GRASP: Controller - coordina las operaciones de dominio.
 */
@Service
@Transactional
public class PartidoService {

    private static final Logger logger = LoggerFactory.getLogger(PartidoService.class);

    private final PartidoRepository partidoRepository;
    private final CuentaRepository cuentaRepository;
    private final Notificador notificador;

    // Nuevas dependencias para patrones implementados
    @Autowired
    private EmparejadorService emparejadorService;
    
    @Autowired
    private EstadoFactory estadoFactory;
    
    @Autowired
    private NotificadorObservador notificadorObservador;
    
    @Autowired
    private EstadisticasObservador estadisticasObservador;

    @Autowired
    public PartidoService(PartidoRepository partidoRepository, 
                         CuentaRepository cuentaRepository,
                         Notificador notificador) {
        this.partidoRepository = Objects.requireNonNull(partidoRepository, "PartidoRepository no puede ser nulo");
        this.cuentaRepository = Objects.requireNonNull(cuentaRepository, "CuentaRepository no puede ser nulo");
        this.notificador = Objects.requireNonNull(notificador, "Notificador no puede ser nulo");
    }

    /**
     * Crea un nuevo partido deportivo.
     */
    public PartidoDTO crearPartido(PartidoDTO partidoDTO) {
        Objects.requireNonNull(partidoDTO, "PartidoDTO no puede ser nulo");
        
        logger.info("Creando nuevo partido de {} para {} jugadores", 
                   partidoDTO.getTipoDeporte(), partidoDTO.getCantidadJugadores());

        // Buscar el creador
        Cuenta creador = cuentaRepository.findById(partidoDTO.getCreadorId())
            .orElseThrow(() -> new IllegalArgumentException("No se encontró cuenta con ID: " + partidoDTO.getCreadorId()));

        // Convertir DTO a entidad
        Partido partido = convertirDTOAEntidad(partidoDTO, creador);
        
        // Configurar estado inicial usando Factory
        IEstado estadoInicial = estadoFactory.crearEstadoInicial();
        partido.setEstado(estadoInicial);
        
        // Agregar observadores para notificaciones automáticas
        partido.agregarObservador(notificadorObservador);
        partido.agregarObservador(estadisticasObservador);
        
        // Guardar en base de datos
        Partido partidoGuardado = partidoRepository.save(partido);
        
        logger.info("Partido creado exitosamente con ID: {}", partidoGuardado.getId());

        // Notificar a observadores sobre creación
        estadisticasObservador.actualizar("PARTIDO_CREADO", partidoGuardado);

        // Enviar notificación
        try {
            String mensaje = String.format(
                "Nuevo partido de %s creado por %s. ¡Únete ahora!", 
                partido.getTipoDeporte().getNombre(), 
                creador.getNombre()
            );
            notificador.enviarNotificacion(mensaje);
        } catch (Exception e) {
            logger.warn("Error al enviar notificación de nuevo partido: {}", e.getMessage());
        }

        return convertirEntidadADTO(partidoGuardado);
    }

    /**
     * Sobrecarga del método crearPartido para uso del Facade.
     */
    public Partido crearPartido(PartidoDTO partidoDTO, Long creadorId) {
        partidoDTO.setCreadorId(creadorId);
        PartidoDTO partidoCreado = crearPartido(partidoDTO);
        
        return partidoRepository.findById(partidoCreado.getId())
            .orElseThrow(() -> new RuntimeException("Error al recuperar partido creado"));
    }

    /**
     * Busca un partido por su ID.
     */
    @Transactional(readOnly = true)
    public Optional<PartidoDTO> buscarPorId(Long id) {
        Objects.requireNonNull(id, "ID no puede ser nulo");
        
        logger.debug("Buscando partido por ID: {}", id);
        
        return partidoRepository.findById(id)
                                .map(this::convertirEntidadADTO);
    }

    /**
     * Busca partidos con filtros complejos.
     */
    @Transactional(readOnly = true)
    public Page<PartidoDTO> buscarPartidos(FiltroPartidoDTO filtro) {
        Objects.requireNonNull(filtro, "FiltroPartidoDTO no puede ser nulo");
        
        logger.debug("Buscando partidos con filtros: {}", filtro);

        // Configurar paginación y ordenamiento
        Sort sort = configurarOrdenamiento(filtro.getOrdenarPor(), filtro.getDireccion());
        Pageable pageable = PageRequest.of(filtro.getPagina(), filtro.getTamañoPagina(), sort);

        Page<Partido> partidos;

        // Elegir el método de búsqueda según los filtros
        if (filtro.tieneFiltroPorUbicacion()) {
            partidos = partidoRepository.findWithFiltersAndLocation(
                filtro.getTipoDeporte() != null ? filtro.getTipoDeporte().name() : null,
                filtro.getEstado() != null ? filtro.getEstado().name() : null,
                filtro.getFechaDesde(),
                filtro.getFechaHasta(),
                filtro.getLatitud(),
                filtro.getLongitud(),
                filtro.getRadioKm(),
                filtro.getOrdenarPor(),
                pageable
            );
        } else {
            partidos = partidoRepository.findWithFilters(
                filtro.getTipoDeporte(),
                filtro.getEstado(),
                filtro.getFechaDesde(),
                filtro.getFechaHasta(),
                filtro.getJugadoresMinimo(),
                filtro.getJugadoresMaximo(),
                filtro.getSoloDisponibles(),
                pageable
            );
        }

        return partidos.map(this::convertirEntidadADTO);
    }

    /**
     * Une un jugador a un partido.
     * Maneja las transiciones de estado automáticamente.
     */
    public PartidoDTO unirseAPartido(Long partidoId, Long cuentaId) {
        Objects.requireNonNull(partidoId, "PartidoId no puede ser nulo");
        Objects.requireNonNull(cuentaId, "CuentaId no puede ser nulo");
        
        logger.info("Uniendo cuenta ID {} al partido ID {}", cuentaId, partidoId);

        Partido partido = partidoRepository.findById(partidoId)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró partido con ID: " + partidoId));

        Cuenta cuenta = cuentaRepository.findById(cuentaId)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró cuenta con ID: " + cuentaId));

        // Intentar agregar jugador (esto maneja estados internamente)
        try {
            partido.agregarJugador(cuenta);
            Partido partidoActualizado = partidoRepository.save(partido);
            
            logger.info("Jugador agregado exitosamente al partido ID: {}", partidoId);

            // Enviar notificación
            try {
                String mensaje = String.format(
                    "%s se ha unido al partido de %s. Jugadores: %d/%d", 
                    cuenta.getNombre(),
                    partido.getTipoDeporte().getNombre(),
                    partido.getJugadores().size(),
                    partido.getCantidadJugadores()
                );
                notificador.enviarNotificacion(mensaje);
            } catch (Exception e) {
                logger.warn("Error al enviar notificación de unión: {}", e.getMessage());
            }

            return convertirEntidadADTO(partidoActualizado);
            
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("Error al unir jugador al partido: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Remueve un jugador de un partido.
     */
    public PartidoDTO salirDePartido(Long partidoId, Long cuentaId) {
        Objects.requireNonNull(partidoId, "PartidoId no puede ser nulo");
        Objects.requireNonNull(cuentaId, "CuentaId no puede ser nulo");
        
        logger.info("Removiendo cuenta ID {} del partido ID {}", cuentaId, partidoId);

        Partido partido = partidoRepository.findById(partidoId)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró partido con ID: " + partidoId));

        Cuenta cuenta = cuentaRepository.findById(cuentaId)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró cuenta con ID: " + cuentaId));

        try {
            partido.removerJugador(cuenta);
            Partido partidoActualizado = partidoRepository.save(partido);
            
            logger.info("Jugador removido exitosamente del partido ID: {}", partidoId);

            // Enviar notificación
            try {
                String mensaje = String.format(
                    "%s ha salido del partido de %s. Jugadores: %d/%d", 
                    cuenta.getNombre(),
                    partido.getTipoDeporte().getNombre(),
                    partido.getJugadores().size(),
                    partido.getCantidadJugadores()
                );
                notificador.enviarNotificacion(mensaje);
            } catch (Exception e) {
                logger.warn("Error al enviar notificación de salida: {}", e.getMessage());
            }

            return convertirEntidadADTO(partidoActualizado);
            
        } catch (IllegalStateException | IllegalArgumentException e) {
            logger.warn("Error al remover jugador del partido: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Cambia manualmente el estado de un partido.
     */
    public PartidoDTO cambiarEstadoPartido(Long partidoId, Partido.EstadoEnum nuevoEstado) {
        Objects.requireNonNull(partidoId, "PartidoId no puede ser nulo");
        Objects.requireNonNull(nuevoEstado, "NuevoEstado no puede ser nulo");
        
        logger.info("Cambiando estado del partido ID {} a {}", partidoId, nuevoEstado);

        Partido partido = partidoRepository.findById(partidoId)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró partido con ID: " + partidoId));

        // Crear instancia del nuevo estado
        IEstado estado = crearInstanciaEstado(nuevoEstado);
        partido.cambiarEstado(estado);
        
        Partido partidoActualizado = partidoRepository.save(partido);
        
        logger.info("Estado cambiado exitosamente para partido ID: {}", partidoId);

        return convertirEntidadADTO(partidoActualizado);
    }

    /**
     * Busca partidos disponibles (con cupo).
     */
    @Transactional(readOnly = true)
    public List<PartidoDTO> buscarPartidosDisponibles() {
        logger.debug("Buscando partidos disponibles");
        
        return partidoRepository.findPartidosDisponibles(LocalDateTime.now())
                                .stream()
                                .map(this::convertirEntidadADTO)
                                .collect(Collectors.toList());
    }

    /**
     * Busca partidos por creador.
     */
    @Transactional(readOnly = true)
    public List<PartidoDTO> buscarPartidosPorCreador(Long creadorId) {
        Objects.requireNonNull(creadorId, "CreadorId no puede ser nulo");
        
        logger.debug("Buscando partidos creados por cuenta ID: {}", creadorId);
        
        return partidoRepository.findByCreadorId(creadorId)
                                .stream()
                                .map(this::convertirEntidadADTO)
                                .collect(Collectors.toList());
    }

    /**
     * Busca partidos en los que participa un usuario.
     */
    @Transactional(readOnly = true)
    public List<PartidoDTO> buscarPartidosPorJugador(Long jugadorId) {
        Objects.requireNonNull(jugadorId, "JugadorId no puede ser nulo");
        
        logger.debug("Buscando partidos donde participa cuenta ID: {}", jugadorId);
        
        return partidoRepository.findByJugadorId(jugadorId)
                                .stream()
                                .map(this::convertirEntidadADTO)
                                .collect(Collectors.toList());
    }

    // Métodos privados de utilidad

    private Sort configurarOrdenamiento(String ordenarPor, String direccion) {
        Sort.Direction dir = "desc".equalsIgnoreCase(direccion) ? Sort.Direction.DESC : Sort.Direction.ASC;
        
        return switch (ordenarPor != null ? ordenarPor.toLowerCase() : "fecha") {
            case "fecha" -> Sort.by(dir, "fechaHora");
            case "ocupacion" -> Sort.by(dir, "cantidadJugadores");
            case "creado" -> Sort.by(dir, "creadoEn");
            default -> Sort.by(dir, "fechaHora");
        };
    }

    private IEstado crearInstanciaEstado(Partido.EstadoEnum estadoEnum) {
        return switch (estadoEnum) {
            case PARTIDO_ARMADO -> new PartidoArmado();
            case NECESITAMOS_JUGADORES -> new NecesitamosJugadores();
            case CONFIRMADO -> new Confirmado();
            case EN_JUEGO -> new EnJuego();
            case FINALIZADO -> new Finalizado();
            case CANCELADO -> new Cancelado();
        };
    }

    // Métodos de conversión

    private Partido convertirDTOAEntidad(PartidoDTO dto, Cuenta creador) {
        Ubicacion ubicacion = new Ubicacion(dto.getUbicacion().getLatitud(), dto.getUbicacion().getLongitud());
        
        return new Partido(
            dto.getTipoDeporte(),
            dto.getCantidadJugadores(),
            dto.getDuracion(),
            dto.getFechaHora(),
            dto.getDescripcion(),
            ubicacion,
            creador
        );
    }

    private PartidoDTO convertirEntidadADTO(Partido partido) {
        PartidoDTO dto = new PartidoDTO();
        dto.setId(partido.getId());
        dto.setTipoDeporte(partido.getTipoDeporte());
        dto.setCantidadJugadores(partido.getCantidadJugadores());
        dto.setDuracion(partido.getDuracion());
        dto.setFechaHora(partido.getFechaHora());
        dto.setDescripcion(partido.getDescripcion());
        dto.setCreadorId(partido.getCreador().getId());
        dto.setEstado(partido.getEstadoNombre());
        dto.setCreadoEn(partido.getCreadoEn());
        dto.setActualizadoEn(partido.getActualizadoEn());
        
        // Convertir ubicación
        UbicacionDTO ubicacionDTO = new UbicacionDTO();
        ubicacionDTO.setId(partido.getUbicacion().getId());
        ubicacionDTO.setLatitud(partido.getUbicacion().getLatitud());
        ubicacionDTO.setLongitud(partido.getUbicacion().getLongitud());
        dto.setUbicacion(ubicacionDTO);
        
        // Convertir jugadores
        List<CuentaDTO> jugadoresDTO = partido.getJugadores().stream()
            .map(this::convertirCuentaADTO)
            .collect(Collectors.toList());
        dto.setJugadores(jugadoresDTO);
        
        // Campos calculados
        dto.setJugadoresActuales(partido.getJugadores().size());
        dto.setPorcentajeOcupacion(partido.obtenerPorcentajeOcupacion());
        dto.setDisponible(partido.estaDisponible());
        
        return dto;
    }

    private CuentaDTO convertirCuentaADTO(Cuenta cuenta) {
        CuentaDTO dto = new CuentaDTO();
        dto.setId(cuenta.getId());
        dto.setNombre(cuenta.getNombre());
        dto.setEmail(cuenta.getEmail());
        dto.setDeporteFavorito(cuenta.getDeporteFavorito());
        dto.setNivel(cuenta.getNivel());
        
        UbicacionDTO ubicacionDTO = new UbicacionDTO();
        ubicacionDTO.setId(cuenta.getUbicacion().getId());
        ubicacionDTO.setLatitud(cuenta.getUbicacion().getLatitud());
        ubicacionDTO.setLongitud(cuenta.getUbicacion().getLongitud());
        dto.setUbicacion(ubicacionDTO);
        
        // No incluir contraseña en DTOs de salida
        dto.setContraseña(null);
        
        return dto;
    }

    // Métodos adicionales para el Facade

    /**
     * Busca un partido por ID y retorna la entidad (para uso interno del Facade).
     */
    public Partido buscarPorId(Long id) {
        return partidoRepository.findById(id).orElse(null);
    }

    /**
     * Guarda un partido directamente (para uso interno del Facade).
     */
    public Partido guardarPartido(Partido partido) {
        return partidoRepository.save(partido);
    }

    /**
     * Agrega un jugador a un partido (para uso interno del Facade).
     */
    public boolean agregarJugador(Long partidoId, Long jugadorId) {
        try {
            Partido partido = buscarPorId(partidoId);
            Cuenta jugador = cuentaRepository.findById(jugadorId).orElse(null);
            
            if (partido == null || jugador == null) {
                return false;
            }
            
            partido.agregarJugador(jugador);
            guardarPartido(partido);
            
            // Notificar a observadores
            java.util.Map<String, Object> datos = new java.util.HashMap<>();
            datos.put("partidoId", partidoId);
            datos.put("jugadorId", jugadorId);
            estadisticasObservador.actualizar("JUGADOR_AGREGADO", datos);
            
            return true;
        } catch (Exception e) {
            logger.error("Error al agregar jugador al partido: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Busca partidos con filtros para el Facade.
     */
    public List<Partido> buscarConFiltros(FiltroPartidoDTO filtros) {
        // Convertir el resultado paginado a lista simple
        Page<Partido> resultado = partidoRepository.findWithFilters(
            filtros.getTipoDeporte(),
            filtros.getEstado(),
            filtros.getFechaDesde(),
            filtros.getFechaHasta(),
            filtros.getJugadoresMinimo(),
            filtros.getJugadoresMaximo(),
            filtros.getSoloDisponibles(),
            PageRequest.of(0, 1000) // Página grande para obtener todos los resultados
        );
        
        return resultado.getContent();
    }

    /**
     * Busca partidos de un jugador específico.
     */
    public List<Partido> buscarPartidosPorJugador(Long jugadorId) {
        return partidoRepository.findByJugadorId(jugadorId);
    }

    /**
     * Busca partidos próximos a iniciar.
     */
    public List<Partido> buscarPartidosProximos(int horasLimite) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.plusHours(horasLimite);
        return partidoRepository.findPartidosProximos(ahora, limite);
    }

    /**
     * Busca partidos urgentes que necesitan jugadores.
     */
    public List<Partido> buscarPartidosUrgentes(int horasLimite) {
        LocalDateTime ahora = LocalDateTime.now();
        LocalDateTime limite = ahora.plusHours(horasLimite);
        return partidoRepository.findPartidosUrgentes(ahora, limite);
    }

    /**
     * Obtiene estadísticas básicas de partidos.
     */
    public java.util.Map<String, Long> obtenerEstadisticasBasicas() {
        java.util.Map<String, Long> estadisticas = new java.util.HashMap<>();
        
        for (Partido.EstadoEnum estado : Partido.EstadoEnum.values()) {
            Long count = partidoRepository.countByEstadoNombre(estado);
            estadisticas.put(estado.name(), count);
        }
        
        return estadisticas;
    }
} 