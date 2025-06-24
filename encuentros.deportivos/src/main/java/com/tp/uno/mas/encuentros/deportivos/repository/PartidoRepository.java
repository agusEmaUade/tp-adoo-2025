package com.tp.uno.mas.encuentros.deportivos.repository;

import com.tp.uno.mas.encuentros.deportivos.model.Partido;
import com.tp.uno.mas.encuentros.deportivos.model.TipoDeporte;
import com.tp.uno.mas.encuentros.deportivos.model.Partido.EstadoEnum;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDateTime;
import java.util.List;

/**
 * Repositorio para la entidad Partido.
 * Proporciona métodos de acceso a datos especializados para búsquedas complejas.
 * Principio SOLID: Interface Segregation - interfaz específica para partidos.
 */
@Repository
public interface PartidoRepository extends JpaRepository<Partido, Long> {

    /**
     * Busca partidos por tipo de deporte.
     */
    List<Partido> findByTipoDeporte(TipoDeporte tipoDeporte);

    /**
     * Busca partidos por estado.
     */
    List<Partido> findByEstadoNombre(EstadoEnum estado);

    /**
     * Busca partidos disponibles (con cupo y en estados que permiten unirse).
     */
    @Query("""
        SELECT p FROM Partido p 
        WHERE p.estadoNombre IN ('PARTIDO_ARMADO', 'NECESITAMOS_JUGADORES') 
        AND SIZE(p.jugadores) < p.cantidadJugadores 
        AND p.fechaHora > :ahora
        """)
    List<Partido> findPartidosDisponibles(@Param("ahora") LocalDateTime ahora);

    /**
     * Busca partidos por rango de fechas.
     */
    @Query("SELECT p FROM Partido p WHERE p.fechaHora BETWEEN :desde AND :hasta")
    List<Partido> findByFechaHoraBetween(@Param("desde") LocalDateTime desde, 
                                       @Param("hasta") LocalDateTime hasta);

    /**
     * Busca partidos dentro de un radio geográfico específico.
     * Utiliza la fórmula de Haversine para calcular distancias.
     */
    @Query(value = """
        SELECT p.* FROM partidos p 
        JOIN ubicaciones u ON p.ubicacion_id = u.id 
        WHERE (6371 * acos(cos(radians(:latitud)) * cos(radians(u.latitud)) * 
               cos(radians(u.longitud) - radians(:longitud)) + 
               sin(radians(:latitud)) * sin(radians(u.latitud)))) <= :radioKm
        """, nativeQuery = true)
    List<Partido> findByUbicacionWithinRadius(@Param("latitud") Double latitud, 
                                            @Param("longitud") Double longitud, 
                                            @Param("radioKm") Double radioKm);

    /**
     * Busca partidos creados por un usuario específico.
     */
    @Query("SELECT p FROM Partido p WHERE p.creador.id = :creadorId")
    List<Partido> findByCreadorId(@Param("creadorId") Long creadorId);

    /**
     * Busca partidos en los que participa un usuario específico.
     */
    @Query("SELECT p FROM Partido p JOIN p.jugadores j WHERE j.id = :jugadorId")
    List<Partido> findByJugadorId(@Param("jugadorId") Long jugadorId);

    /**
     * Búsqueda compleja con múltiples filtros usando paginación.
     */
    @Query("""
        SELECT DISTINCT p FROM Partido p 
        JOIN p.ubicacion u 
        WHERE (:tipoDeporte IS NULL OR p.tipoDeporte = :tipoDeporte)
        AND (:estado IS NULL OR p.estadoNombre = :estado)
        AND (:fechaDesde IS NULL OR p.fechaHora >= :fechaDesde)
        AND (:fechaHasta IS NULL OR p.fechaHora <= :fechaHasta)
        AND (:jugadoresMin IS NULL OR p.cantidadJugadores >= :jugadoresMin)
        AND (:jugadoresMax IS NULL OR p.cantidadJugadores <= :jugadoresMax)
        AND (:soloDisponibles = false OR (p.estadoNombre IN ('PARTIDO_ARMADO', 'NECESITAMOS_JUGADORES') 
                                        AND SIZE(p.jugadores) < p.cantidadJugadores 
                                        AND p.fechaHora > CURRENT_TIMESTAMP))
        """)
    Page<Partido> findWithFilters(@Param("tipoDeporte") TipoDeporte tipoDeporte,
                                 @Param("estado") EstadoEnum estado,
                                 @Param("fechaDesde") LocalDateTime fechaDesde,
                                 @Param("fechaHasta") LocalDateTime fechaHasta,
                                 @Param("jugadoresMin") Integer jugadoresMin,
                                 @Param("jugadoresMax") Integer jugadoresMax,
                                 @Param("soloDisponibles") Boolean soloDisponibles,
                                 Pageable pageable);

    /**
     * Búsqueda con filtros y radio geográfico.
     */
    @Query(value = """
        SELECT DISTINCT p.* FROM partidos p 
        JOIN ubicaciones u ON p.ubicacion_id = u.id 
        WHERE (:tipoDeporte IS NULL OR p.tipo_deporte = :tipoDeporte)
        AND (:estado IS NULL OR p.estado_nombre = :estado)
        AND (:fechaDesde IS NULL OR p.fecha_hora >= :fechaDesde)
        AND (:fechaHasta IS NULL OR p.fecha_hora <= :fechaHasta)
        AND (6371 * acos(cos(radians(:latitud)) * cos(radians(u.latitud)) * 
             cos(radians(u.longitud) - radians(:longitud)) + 
             sin(radians(:latitud)) * sin(radians(u.latitud)))) <= :radioKm
        ORDER BY 
            CASE WHEN :ordenarPor = 'distancia' THEN 
                (6371 * acos(cos(radians(:latitud)) * cos(radians(u.latitud)) * 
                 cos(radians(u.longitud) - radians(:longitud)) + 
                 sin(radians(:latitud)) * sin(radians(u.latitud))))
            END ASC,
            CASE WHEN :ordenarPor = 'fecha' THEN p.fecha_hora END ASC
        """, 
        countQuery = """
        SELECT COUNT(DISTINCT p.id) FROM partidos p 
        JOIN ubicaciones u ON p.ubicacion_id = u.id 
        WHERE (:tipoDeporte IS NULL OR p.tipo_deporte = :tipoDeporte)
        AND (:estado IS NULL OR p.estado_nombre = :estado)
        AND (:fechaDesde IS NULL OR p.fecha_hora >= :fechaDesde)
        AND (:fechaHasta IS NULL OR p.fecha_hora <= :fechaHasta)
        AND (6371 * acos(cos(radians(:latitud)) * cos(radians(u.latitud)) * 
             cos(radians(u.longitud) - radians(:longitud)) + 
             sin(radians(:latitud)) * sin(radians(u.latitud)))) <= :radioKm
        """,
        nativeQuery = true)
    Page<Partido> findWithFiltersAndLocation(@Param("tipoDeporte") String tipoDeporte,
                                           @Param("estado") String estado,
                                           @Param("fechaDesde") LocalDateTime fechaDesde,
                                           @Param("fechaHasta") LocalDateTime fechaHasta,
                                           @Param("latitud") Double latitud,
                                           @Param("longitud") Double longitud,
                                           @Param("radioKm") Double radioKm,
                                           @Param("ordenarPor") String ordenarPor,
                                           Pageable pageable);

    /**
     * Cuenta partidos por estado.
     */
    Long countByEstadoNombre(EstadoEnum estado);

    /**
     * Busca partidos próximos (en las próximas horas).
     */
    @Query("""
        SELECT p FROM Partido p 
        WHERE p.fechaHora BETWEEN :ahora AND :limite 
        AND p.estadoNombre = 'CONFIRMADO'
        ORDER BY p.fechaHora ASC
        """)
    List<Partido> findPartidosProximos(@Param("ahora") LocalDateTime ahora, 
                                     @Param("limite") LocalDateTime limite);

    /**
     * Busca partidos que necesitan jugadores urgentemente.
     */
    @Query("""
        SELECT p FROM Partido p 
        WHERE p.estadoNombre = 'NECESITAMOS_JUGADORES' 
        AND p.fechaHora BETWEEN :ahora AND :limite 
        AND SIZE(p.jugadores) < (p.cantidadJugadores / 2)
        ORDER BY p.fechaHora ASC
        """)
    List<Partido> findPartidosUrgentes(@Param("ahora") LocalDateTime ahora, 
                                     @Param("limite") LocalDateTime limite);
} 