package com.tp.uno.mas.encuentros.deportivos.controller;

import com.tp.uno.mas.encuentros.deportivos.dto.CuentaDTO;
import com.tp.uno.mas.encuentros.deportivos.model.Cuenta;
import com.tp.uno.mas.encuentros.deportivos.model.NivelDeJuego;
import com.tp.uno.mas.encuentros.deportivos.model.TipoDeporte;
import com.tp.uno.mas.encuentros.deportivos.service.CuentaService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.util.List;
import java.util.Objects;

/**
 * Controlador REST para la gestión de cuentas de usuario.
 * 
 * Implementa el patrón MVC como Controller, delegando la lógica de negocio al CuentaService.
 * Aplica principios SOLID:
 * - SRP: Solo maneja peticiones HTTP relacionadas con cuentas
 * - DIP: Depende de la abstracción CuentaService
 * 
 * Maneja validaciones, respuestas HTTP apropiadas y logging.
 */
@RestController
@RequestMapping
@Validated
@CrossOrigin(origins = "*")
public class CuentaController {

    private static final Logger LOGGER = LoggerFactory.getLogger(CuentaController.class);
    
    private final CuentaService cuentaService;

    @Autowired
    public CuentaController(CuentaService cuentaService) {
        this.cuentaService = Objects.requireNonNull(cuentaService, "CuentaService no puede ser null");
    }

    /**
     * Crea una nueva cuenta de usuario.
     * 
     * @param cuentaDTO Datos de la cuenta a crear
     * @return ResponseEntity con la cuenta creada y status 201
     */
    @PostMapping(Path.CUENTAS)
    public ResponseEntity<Cuenta> crearCuenta(@Valid @RequestBody CuentaDTO cuentaDTO) {
        LOGGER.info("Creando nueva cuenta para email: {}", cuentaDTO.getEmail());
        
        try {
            Cuenta cuenta = cuentaService.crearCuenta(cuentaDTO);
            LOGGER.info("Cuenta creada exitosamente con ID: {}", cuenta.getId());
            return new ResponseEntity<>(cuenta, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Error al crear cuenta: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            LOGGER.error("Error interno al crear cuenta", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Obtiene una cuenta por su ID.
     * 
     * @param id ID de la cuenta
     * @return ResponseEntity con la cuenta encontrada o 404 si no existe
     */
    @GetMapping(Path.CUENTA_BY_ID)
    public ResponseEntity<Cuenta> obtenerCuentaPorId(@PathVariable @NotNull @Positive Long id) {
        LOGGER.info("Buscando cuenta con ID: {}", id);
        
        try {
            Cuenta cuenta = cuentaService.buscarPorId(id);
            return ResponseEntity.ok(cuenta);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Cuenta no encontrada con ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            LOGGER.error("Error al buscar cuenta por ID", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Obtiene una cuenta por su email.
     * 
     * @param email Email de la cuenta
     * @return ResponseEntity con la cuenta encontrada o 404 si no existe
     */
    @GetMapping(Path.CUENTA_BY_EMAIL)
    public ResponseEntity<Cuenta> obtenerCuentaPorEmail(@PathVariable @Email String email) {
        LOGGER.info("Buscando cuenta con email: {}", email);
        
        try {
            Cuenta cuenta = cuentaService.buscarPorEmail(email);
            return ResponseEntity.ok(cuenta);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Cuenta no encontrada con email: {}", email);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            LOGGER.error("Error al buscar cuenta por email", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Actualiza el perfil de una cuenta existente.
     * 
     * @param id ID de la cuenta a actualizar
     * @param cuentaDTO Nuevos datos de la cuenta
     * @return ResponseEntity con la cuenta actualizada
     */
    @PutMapping(Path.CUENTA_BY_ID)
    public ResponseEntity<Cuenta> actualizarPerfil(
            @PathVariable @NotNull @Positive Long id,
            @Valid @RequestBody CuentaDTO cuentaDTO) {
        
        LOGGER.info("Actualizando perfil de cuenta con ID: {}", id);
        
        try {
            Cuenta cuenta = cuentaService.actualizarPerfil(id, cuentaDTO);
            LOGGER.info("Perfil actualizado exitosamente para cuenta ID: {}", id);
            return ResponseEntity.ok(cuenta);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Error al actualizar perfil: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            LOGGER.error("Error interno al actualizar perfil", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Obtiene todas las cuentas con paginación.
     * 
     * @param pageable Información de paginación
     * @return ResponseEntity con página de cuentas
     */
    @GetMapping(Path.CUENTAS)
    public ResponseEntity<Page<Cuenta>> obtenerTodasLasCuentas(Pageable pageable) {
        LOGGER.info("Obteniendo todas las cuentas - Página: {}, Tamaño: {}", 
                   pageable.getPageNumber(), pageable.getPageSize());
        
        try {
            Page<Cuenta> cuentas = cuentaService.obtenerTodasLasCuentas(pageable);
            return ResponseEntity.ok(cuentas);
        } catch (Exception e) {
            LOGGER.error("Error al obtener todas las cuentas", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Busca cuentas compatibles para jugar con una cuenta específica.
     * 
     * @param id ID de la cuenta de referencia
     * @param tipoDeporte Tipo de deporte para la búsqueda
     * @param radio Radio de búsqueda en kilómetros
     * @return ResponseEntity con lista de cuentas compatibles
     */
    @GetMapping(Path.CUENTA_COMPATIBLES)
    public ResponseEntity<List<Cuenta>> buscarCuentasCompatibles(
            @PathVariable @NotNull @Positive Long id,
            @RequestParam TipoDeporte tipoDeporte,
            @RequestParam(defaultValue = "10.0") Double radio) {
        
        LOGGER.info("Buscando cuentas compatibles para ID: {}, deporte: {}, radio: {}km", 
                   id, tipoDeporte, radio);
        
        try {
            List<Cuenta> cuentasCompatibles = cuentaService.buscarCuentasCompatibles(id, tipoDeporte, radio);
            return ResponseEntity.ok(cuentasCompatibles);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Error en búsqueda de compatibles: {}", e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            LOGGER.error("Error al buscar cuentas compatibles", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Elimina una cuenta (soft delete).
     * 
     * @param id ID de la cuenta a eliminar
     * @return ResponseEntity con status 204 si se eliminó correctamente
     */
    @DeleteMapping(Path.CUENTA_BY_ID)
    public ResponseEntity<Void> eliminarCuenta(@PathVariable @NotNull @Positive Long id) {
        LOGGER.info("Eliminando cuenta con ID: {}", id);
        
        try {
            cuentaService.eliminarCuenta(id);
            LOGGER.info("Cuenta eliminada exitosamente con ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            LOGGER.warn("Cuenta no encontrada para eliminar con ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            LOGGER.error("Error al eliminar cuenta", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Obtiene todos los tipos de deporte disponibles.
     * 
     * @return ResponseEntity con array de tipos de deporte
     */
    @GetMapping(Path.DEPORTES)
    public ResponseEntity<TipoDeporte[]> obtenerTiposDeporte() {
        LOGGER.info("Obteniendo tipos de deporte disponibles");
        return ResponseEntity.ok(TipoDeporte.values());
    }

    /**
     * Obtiene todos los niveles de juego disponibles.
     * 
     * @return ResponseEntity con array de niveles de juego
     */
    @GetMapping(Path.NIVELES)
    public ResponseEntity<NivelDeJuego[]> obtenerNivelesDeJuego() {
        LOGGER.info("Obteniendo niveles de juego disponibles");
        return ResponseEntity.ok(NivelDeJuego.values());
    }
}
