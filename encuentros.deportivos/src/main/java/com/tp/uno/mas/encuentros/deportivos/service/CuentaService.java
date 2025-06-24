package com.tp.uno.mas.encuentros.deportivos.service;

import com.tp.uno.mas.encuentros.deportivos.dto.CuentaDTO;
import com.tp.uno.mas.encuentros.deportivos.dto.UbicacionDTO;
import com.tp.uno.mas.encuentros.deportivos.model.Cuenta;
import com.tp.uno.mas.encuentros.deportivos.model.NivelDeJuego;
import com.tp.uno.mas.encuentros.deportivos.model.TipoDeporte;
import com.tp.uno.mas.encuentros.deportivos.model.Ubicacion;
import com.tp.uno.mas.encuentros.deportivos.repository.CuentaRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * Servicio para gestión de cuentas de usuario.
 * Implementa la lógica de negocio para operaciones CRUD y funcionalidades específicas.
 * Principio SOLID: Single Responsibility - solo maneja lógica de cuentas.
 * Principio GRASP: Controller - coordina las operaciones de dominio.
 */
@Service
@Transactional
public class CuentaService {

    private static final Logger logger = LoggerFactory.getLogger(CuentaService.class);

    private final CuentaRepository cuentaRepository;
    private final Notificador notificador;

    @Autowired
    public CuentaService(CuentaRepository cuentaRepository, Notificador notificador) {
        this.cuentaRepository = Objects.requireNonNull(cuentaRepository, "CuentaRepository no puede ser nulo");
        this.notificador = Objects.requireNonNull(notificador, "Notificador no puede ser nulo");
    }

    /**
     * Crea una nueva cuenta de usuario.
     * Valida que el email sea único antes de crear.
     */
    public CuentaDTO crearCuenta(CuentaDTO cuentaDTO) {
        Objects.requireNonNull(cuentaDTO, "CuentaDTO no puede ser nulo");
        
        logger.info("Creando nueva cuenta para email: {}", cuentaDTO.getEmail());

        // Validar que el email sea único
        if (cuentaRepository.existsByEmail(cuentaDTO.getEmail())) {
            throw new IllegalArgumentException("Ya existe una cuenta con el email: " + cuentaDTO.getEmail());
        }

        // Convertir DTO a entidad
        Cuenta cuenta = convertirDTOAEntidad(cuentaDTO);
        
        // Guardar en base de datos
        Cuenta cuentaGuardada = cuentaRepository.save(cuenta);
        
        logger.info("Cuenta creada exitosamente con ID: {}", cuentaGuardada.getId());

        // Enviar notificación de bienvenida
        try {
            String mensajeBienvenida = String.format(
                "¡Bienvenido a Encuentros Deportivos, %s! Tu cuenta ha sido creada exitosamente.", 
                cuentaGuardada.getNombre()
            );
            notificador.enviarNotificacion(mensajeBienvenida);
        } catch (Exception e) {
            logger.warn("Error al enviar notificación de bienvenida: {}", e.getMessage());
        }

        return convertirEntidadADTO(cuentaGuardada);
    }

    /**
     * Busca una cuenta por su ID.
     */
    @Transactional(readOnly = true)
    public Optional<CuentaDTO> buscarPorId(Long id) {
        Objects.requireNonNull(id, "ID no puede ser nulo");
        
        logger.debug("Buscando cuenta por ID: {}", id);
        
        return cuentaRepository.findById(id)
                              .map(this::convertirEntidadADTO);
    }

    /**
     * Busca una cuenta por email.
     */
    @Transactional(readOnly = true)
    public Optional<CuentaDTO> buscarPorEmail(String email) {
        Objects.requireNonNull(email, "Email no puede ser nulo");
        
        logger.debug("Buscando cuenta por email: {}", email);
        
        return cuentaRepository.findByEmail(email)
                              .map(this::convertirEntidadADTO);
    }

    /**
     * Actualiza el perfil de una cuenta existente.
     */
    public CuentaDTO actualizarPerfil(Long id, CuentaDTO cuentaDTO) {
        Objects.requireNonNull(id, "ID no puede ser nulo");
        Objects.requireNonNull(cuentaDTO, "CuentaDTO no puede ser nulo");
        
        logger.info("Actualizando perfil de cuenta ID: {}", id);

        Cuenta cuentaExistente = cuentaRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("No se encontró cuenta con ID: " + id));

        // Validar que el email sea único si se está cambiando
        if (!cuentaExistente.getEmail().equals(cuentaDTO.getEmail()) && 
            cuentaRepository.existsByEmail(cuentaDTO.getEmail())) {
            throw new IllegalArgumentException("Ya existe una cuenta con el email: " + cuentaDTO.getEmail());
        }

        // Actualizar campos (excepto ID que no debe cambiar)
        cuentaExistente.setNombre(cuentaDTO.getNombre());
        cuentaExistente.setEmail(cuentaDTO.getEmail());
        if (cuentaDTO.getContraseña() != null && !cuentaDTO.getContraseña().trim().isEmpty()) {
            cuentaExistente.setContraseña(cuentaDTO.getContraseña());
        }
        cuentaExistente.setDeporteFavorito(cuentaDTO.getDeporteFavorito());
        cuentaExistente.setNivel(cuentaDTO.getNivel());
        
        // Actualizar ubicación
        if (cuentaDTO.getUbicacion() != null) {
            Ubicacion ubicacionActualizada = convertirUbicacionDTOAEntidad(cuentaDTO.getUbicacion());
            ubicacionActualizada.setId(cuentaExistente.getUbicacion().getId());
            cuentaExistente.setUbicacion(ubicacionActualizada);
        }

        Cuenta cuentaActualizada = cuentaRepository.save(cuentaExistente);
        
        logger.info("Perfil actualizado exitosamente para cuenta ID: {}", id);
        
        return convertirEntidadADTO(cuentaActualizada);
    }

    /**
     * Busca cuentas por deporte favorito.
     */
    @Transactional(readOnly = true)
    public List<CuentaDTO> buscarPorDeporteFavorito(TipoDeporte tipoDeporte) {
        Objects.requireNonNull(tipoDeporte, "TipoDeporte no puede ser nulo");
        
        logger.debug("Buscando cuentas por deporte favorito: {}", tipoDeporte);
        
        return cuentaRepository.findByDeporteFavorito(tipoDeporte)
                              .stream()
                              .map(this::convertirEntidadADTO)
                              .collect(Collectors.toList());
    }

    /**
     * Busca cuentas compatibles para un partido (mismo deporte y nivel).
     */
    @Transactional(readOnly = true)
    public List<CuentaDTO> buscarCompatiblesParaPartido(TipoDeporte tipoDeporte, NivelDeJuego nivel, Long partidoId) {
        Objects.requireNonNull(tipoDeporte, "TipoDeporte no puede ser nulo");
        Objects.requireNonNull(nivel, "NivelDeJuego no puede ser nulo");
        Objects.requireNonNull(partidoId, "PartidoId no puede ser nulo");
        
        logger.debug("Buscando cuentas compatibles para partido ID: {}, deporte: {}, nivel: {}", 
                    partidoId, tipoDeporte, nivel);
        
        return cuentaRepository.findCompatiblesParaPartido(tipoDeporte, nivel, partidoId)
                              .stream()
                              .map(this::convertirEntidadADTO)
                              .collect(Collectors.toList());
    }

    /**
     * Busca cuentas dentro de un radio geográfico.
     */
    @Transactional(readOnly = true)
    public List<CuentaDTO> buscarPorUbicacion(Double latitud, Double longitud, Double radioKm) {
        Objects.requireNonNull(latitud, "Latitud no puede ser nula");
        Objects.requireNonNull(longitud, "Longitud no puede ser nula");
        Objects.requireNonNull(radioKm, "RadioKm no puede ser nulo");
        
        logger.debug("Buscando cuentas en radio de {} km desde ({}, {})", radioKm, latitud, longitud);
        
        return cuentaRepository.findByUbicacionWithinRadius(latitud, longitud, radioKm)
                              .stream()
                              .map(this::convertirEntidadADTO)
                              .collect(Collectors.toList());
    }

    /**
     * Elimina una cuenta por ID.
     */
    public void eliminarCuenta(Long id) {
        Objects.requireNonNull(id, "ID no puede ser nulo");
        
        logger.info("Eliminando cuenta ID: {}", id);
        
        if (!cuentaRepository.existsById(id)) {
            throw new IllegalArgumentException("No se encontró cuenta con ID: " + id);
        }
        
        cuentaRepository.deleteById(id);
        
        logger.info("Cuenta eliminada exitosamente ID: {}", id);
    }

    /**
     * Obtiene todas las cuentas.
     */
    @Transactional(readOnly = true)
    public List<CuentaDTO> obtenerTodasLasCuentas() {
        logger.debug("Obteniendo todas las cuentas");
        
        return cuentaRepository.findAll()
                              .stream()
                              .map(this::convertirEntidadADTO)
                              .collect(Collectors.toList());
    }

    // Métodos de conversión privados

    private Cuenta convertirDTOAEntidad(CuentaDTO dto) {
        Ubicacion ubicacion = convertirUbicacionDTOAEntidad(dto.getUbicacion());
        
        return new Cuenta(
            dto.getNombre(),
            dto.getEmail(),
            dto.getContraseña(),
            dto.getDeporteFavorito(),
            dto.getNivel(),
            ubicacion
        );
    }

    private CuentaDTO convertirEntidadADTO(Cuenta cuenta) {
        CuentaDTO dto = new CuentaDTO();
        dto.setId(cuenta.getId());
        dto.setNombre(cuenta.getNombre());
        dto.setEmail(cuenta.getEmail());
        dto.setContraseña(cuenta.getContraseña());
        dto.setDeporteFavorito(cuenta.getDeporteFavorito());
        dto.setNivel(cuenta.getNivel());
        dto.setUbicacion(convertirUbicacionEntidadADTO(cuenta.getUbicacion()));
        
        return dto;
    }

    private Ubicacion convertirUbicacionDTOAEntidad(UbicacionDTO dto) {
        return new Ubicacion(dto.getLatitud(), dto.getLongitud());
    }

    private UbicacionDTO convertirUbicacionEntidadADTO(Ubicacion ubicacion) {
        UbicacionDTO dto = new UbicacionDTO();
        dto.setId(ubicacion.getId());
        dto.setLatitud(ubicacion.getLatitud());
        dto.setLongitud(ubicacion.getLongitud());
        
        return dto;
    }
} 