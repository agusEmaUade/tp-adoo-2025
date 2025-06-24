package com.tp.uno.mas.encuentros.deportivos.repository;

import com.tp.uno.mas.encuentros.deportivos.model.Cuenta;
import com.tp.uno.mas.encuentros.deportivos.model.NivelDeJuego;
import com.tp.uno.mas.encuentros.deportivos.model.TipoDeporte;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

/**
 * Repositorio para la entidad Cuenta.
 * Proporciona métodos de acceso a datos con Spring Data JPA.
 * Principio SOLID: Interface Segregation - interfaz específica para acceso a datos.
 */
@Repository
public interface CuentaRepository extends JpaRepository<Cuenta, Long> {

    /**
     * Busca una cuenta por email.
     * Útil para autenticación y verificación de unicidad.
     */
    Optional<Cuenta> findByEmail(String email);

    /**
     * Verifica si existe una cuenta con el email dado.
     */
    boolean existsByEmail(String email);

    /**
     * Busca cuentas por deporte favorito.
     */
    List<Cuenta> findByDeporteFavorito(TipoDeporte tipoDeporte);

    /**
     * Busca cuentas por nivel de juego.
     */
    List<Cuenta> findByNivel(NivelDeJuego nivel);

    /**
     * Busca cuentas por deporte favorito y nivel.
     */
    List<Cuenta> findByDeporteFavoritoAndNivel(TipoDeporte tipoDeporte, NivelDeJuego nivel);

    /**
     * Busca cuentas por nombre (búsqueda parcial, case-insensitive).
     */
    @Query("SELECT c FROM Cuenta c WHERE LOWER(c.nombre) LIKE LOWER(CONCAT('%', :nombre, '%'))")
    List<Cuenta> findByNombreContainingIgnoreCase(@Param("nombre") String nombre);

    /**
     * Busca cuentas dentro de un radio geográfico específico.
     * Utiliza la fórmula de Haversine para calcular distancias.
     */
    @Query(value = """
        SELECT c.* FROM cuentas c 
        JOIN ubicaciones u ON c.ubicacion_id = u.id 
        WHERE (6371 * acos(cos(radians(:latitud)) * cos(radians(u.latitud)) * 
               cos(radians(u.longitud) - radians(:longitud)) + 
               sin(radians(:latitud)) * sin(radians(u.latitud)))) <= :radioKm
        """, nativeQuery = true)
    List<Cuenta> findByUbicacionWithinRadius(@Param("latitud") Double latitud, 
                                           @Param("longitud") Double longitud, 
                                           @Param("radioKm") Double radioKm);

    /**
     * Busca cuentas que participan en un partido específico.
     */
    @Query("SELECT c FROM Cuenta c JOIN c.partidos p WHERE p.id = :partidoId")
    List<Cuenta> findByPartidoId(@Param("partidoId") Long partidoId);

    /**
     * Cuenta el número de partidos en los que participa una cuenta.
     */
    @Query("SELECT COUNT(p) FROM Cuenta c JOIN c.partidos p WHERE c.id = :cuentaId")
    Long countPartidosByCuentaId(@Param("cuentaId") Long cuentaId);

    /**
     * Busca cuentas ordenadas por número de partidos (más activos primero).
     */
    @Query("""
        SELECT c FROM Cuenta c 
        LEFT JOIN c.partidos p 
        GROUP BY c.id 
        ORDER BY COUNT(p) DESC
        """)
    List<Cuenta> findAllOrderByPartidosCount();

    /**
     * Busca cuentas compatibles para un tipo de deporte y nivel específicos.
     * Útil para sugerir jugadores para un partido.
     */
    @Query("""
        SELECT c FROM Cuenta c 
        WHERE c.deporteFavorito = :tipoDeporte 
        AND c.nivel = :nivel 
        AND c.id NOT IN (
            SELECT j.id FROM Partido p JOIN p.jugadores j WHERE p.id = :partidoId
        )
        """)
    List<Cuenta> findCompatiblesParaPartido(@Param("tipoDeporte") TipoDeporte tipoDeporte,
                                          @Param("nivel") NivelDeJuego nivel,
                                          @Param("partidoId") Long partidoId);
} 