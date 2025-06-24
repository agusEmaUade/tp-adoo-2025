package com.tp.uno.mas.encuentros.deportivos.controller;

import com.tp.uno.mas.encuentros.deportivos.dto.FiltroPartidoDTO;
import com.tp.uno.mas.encuentros.deportivos.dto.PartidoDTO;
import com.tp.uno.mas.encuentros.deportivos.model.IEstado;
import com.tp.uno.mas.encuentros.deportivos.model.Partido;
import com.tp.uno.mas.encuentros.deportivos.model.TipoDeporte;
import com.tp.uno.mas.encuentros.deportivos.service.PartidoService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

/**
 * Controlador REST para la gestión de partidos deportivos.
 * 
 * Implementa el patrón MVC como Controller, delegando la lógica de negocio al PartidoService.
 * Aplica principios SOLID:
 * - SRP: Solo maneja peticiones HTTP relacionadas con partidos
 * - DIP: Depende de la abstracción PartidoService
 * 
 * Maneja transiciones de estado, validaciones y respuestas HTTP apropiadas.
 */
@RestController
@RequestMapping
@Validated
@CrossOrigin(origins = "*")
public class PartidoController {

    private static final Logger LOGGER = LoggerFactory.getLogger(PartidoController.class);
    
    private final PartidoService partidoService;

    @Autowired
    public PartidoController(PartidoService partidoService) {
        this.partidoService = Objects.requireNonNull(partidoService, "PartidoService no puede ser null");
    }

    /**
     * Crea un nuevo partido deportivo.
     * 
     * @param partidoDTO Datos del partido a crear
     * @return ResponseEntity con el partido creado y status 201
     */
    @PostMapping(Path.PARTIDOS)
    public ResponseEntity<Partido> crearPartido(@Valid @RequestBody PartidoDTO partidoDTO) {
        LOGGER.info("Creando nuevo partido de {} en {}", 
                   partidoDTO.getTipoDeporte(), partidoDTO.getHora());
        
        try {
            Partido partido = partidoService.crearPartido(partidoDTO);
            LOGGER.info("Partido creado exitosamente con ID: {}", partido.getId());
            return new ResponseEntity<>(partido, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Error al crear partido: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            LOGGER.error("Error interno al crear partido", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Obtiene un partido por su ID.
     * 
     * @param id ID del partido
     * @return ResponseEntity con el partido encontrado o 404 si no existe
     */
    @GetMapping(Path.PARTIDO_BY_ID)
    public ResponseEntity<Partido> obtenerPartidoPorId(@PathVariable @NotNull @Positive Long id) {
        LOGGER.info("Buscando partido con ID: {}", id);
        
        try {
            Partido partido = partidoService.buscarPorId(id);
            return ResponseEntity.ok(partido);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Partido no encontrado con ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            LOGGER.error("Error al buscar partido por ID", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Busca partidos aplicando filtros múltiples con paginación.
     * 
     * @param filtroDTO Filtros de búsqueda
     * @param pageable Información de paginación
     * @return ResponseEntity con página de partidos filtrados
     */
    @PostMapping(Path.PARTIDOS_BUSCAR)
    public ResponseEntity<Page<Partido>> buscarPartidos(
            @Valid @RequestBody FiltroPartidoDTO filtroDTO,
            Pageable pageable) {
        
        LOGGER.info("Buscando partidos con filtros - Tipo: {}, Estado: {}, Radio: {}km", 
                   filtroDTO.getTipoDeporte(), filtroDTO.getEstado(), filtroDTO.getRadioEnKm());
        
        try {
            Page<Partido> partidos = partidoService.buscarPartidos(filtroDTO, pageable);
            LOGGER.info("Encontrados {} partidos", partidos.getTotalElements());
            return ResponseEntity.ok(partidos);
        } catch (Exception e) {
            LOGGER.error("Error al buscar partidos", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Permite a un usuario unirse a un partido.
     * 
     * @param partidoId ID del partido
     * @param cuentaId ID de la cuenta que se une
     * @return ResponseEntity con el partido actualizado
     */
    @PostMapping(Path.PARTIDO_UNIRSE)
    public ResponseEntity<Partido> unirseAPartido(
            @PathVariable("id") @NotNull @Positive Long partidoId,
            @RequestParam @NotNull @Positive Long cuentaId) {
        
        LOGGER.info("Usuario {} intentando unirse al partido {}", cuentaId, partidoId);
        
        try {
            Partido partido = partidoService.unirseAPartido(partidoId, cuentaId);
            LOGGER.info("Usuario {} se unió exitosamente al partido {}", cuentaId, partidoId);
            return ResponseEntity.ok(partido);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Error al unirse al partido: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            LOGGER.warn("Estado inválido para unirse al partido: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (Exception e) {
            LOGGER.error("Error interno al unirse al partido", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Permite a un usuario salir de un partido.
     * 
     * @param partidoId ID del partido
     * @param cuentaId ID de la cuenta que sale
     * @return ResponseEntity con el partido actualizado
     */
    @DeleteMapping(Path.PARTIDO_SALIR)
    public ResponseEntity<Partido> salirDePartido(
            @PathVariable("id") @NotNull @Positive Long partidoId,
            @RequestParam @NotNull @Positive Long cuentaId) {
        
        LOGGER.info("Usuario {} intentando salir del partido {}", cuentaId, partidoId);
        
        try {
            Partido partido = partidoService.salirDePartido(partidoId, cuentaId);
            LOGGER.info("Usuario {} salió exitosamente del partido {}", cuentaId, partidoId);
            return ResponseEntity.ok(partido);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Error al salir del partido: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (IllegalStateException e) {
            LOGGER.warn("Estado inválido para salir del partido: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.CONFLICT);
        } catch (Exception e) {
            LOGGER.error("Error interno al salir del partido", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Obtiene todos los partidos con paginación.
     * 
     * @param pageable Información de paginación
     * @return ResponseEntity con página de partidos
     */
    @GetMapping(Path.PARTIDOS)
    public ResponseEntity<Page<Partido>> obtenerTodosLosPartidos(Pageable pageable) {
        LOGGER.info("Obteniendo todos los partidos - Página: {}, Tamaño: {}", 
                   pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            Page<Partido> partidos = partidoService.obtenerTodosLosPartidos(pageable);
            return ResponseEntity.ok(partidos);
        } catch (Exception e) {
            LOGGER.error("Error al obtener todos los partidos", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Cancela un partido.
     * 
     * @param id ID del partido a cancelar
     * @return ResponseEntity con status 204 si se canceló correctamente
     */
    @DeleteMapping(Path.PARTIDO_BY_ID)
    public ResponseEntity<Void> cancelarPartido(@PathVariable @NotNull @Positive Long id) {
        LOGGER.info("Cancelando partido con ID: {}", id);
        
        try {
            partidoService.cancelarPartido(id);
            LOGGER.info("Partido cancelado exitosamente con ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Partido no encontrado para cancelar con ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            LOGGER.error("Error al cancelar partido", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
