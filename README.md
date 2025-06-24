# Sistema de Encuentros Deportivos - "Uno Más"

## Descripción del Sistema

Sistema para gestionar encuentros deportivos donde los usuarios pueden encontrar jugadores para completar equipos. Implementa arquitectura MVC con **6 patrones de diseño** (superando los 4 mínimos requeridos).

### Funcionalidades Principales
- ✅ Registro con campos obligatorios (nombre, email, contraseña) y opcionales (deporte favorito, nivel)
- ✅ Creación de partidos con estado inicial "NECESITAMOS_JUGADORES" (corregido según enunciado)
- ✅ Secuencia de estados: Necesitamos jugadores → Partido armado → Confirmado → En juego → Finalizado/Cancelado
- ✅ Sistema de emparejamiento inteligente (por nivel, cercanía, historial)
- ✅ Notificaciones automáticas por eventos del sistema
- ✅ Estadísticas en tiempo real y análisis de comportamiento

## Arquitectura y Patrones de Diseño Implementados

### 1. **MVC (Model-View-Controller)**
- **Model**: Entidades de dominio con Observer integrado
- **View**: DTOs para transferencia de datos segura  
- **Controller**: APIs REST con validaciones

### 2. **State Pattern - Estados del Partido** ⭐
- **Secuencia Corregida**: `NecesitamosJugadores` → `PartidoArmado` → `Confirmado` → `EnJuego` → `Finalizado`/`Cancelado`
- **Transiciones Automáticas**: Basadas en número de jugadores y eventos
- **Validaciones**: Cada estado controla qué operaciones son permitidas

### 3. **Strategy Pattern (×2)** ⭐⭐  

#### Estrategias de Notificación
- `Firebase`: Notificaciones push para móviles
- `JavaMail`: Notificaciones por email
- Configuración dinámica y intercambiables

#### Estrategias de Emparejamiento  
- `EstrategiaPorNivel`: Compatibilidad por nivel de juego
- `EstrategiaPorCercania`: Algoritmo geográfico con Haversine
- `EstrategiaPorHistorial`: Basado en partidos anteriores

### 4. **Observer Pattern** ⭐  
- **Notificaciones Automáticas**: `NotificadorObservador` responde a eventos del sistema
- **Estadísticas en Tiempo Real**: `EstadisticasObservador` recopila métricas automáticamente
- **Integración Transparente**: Se agregan automáticamente a partidos y cuentas

### 5. **Factory Pattern (×3)** ⭐⭐⭐
- **EstadoFactory**: Creación centralizada de estados con validaciones
- **EstrategiaNotificacionFactory**: Configuración automática de notificaciones  
- **EstrategiaEmparejamientoFactory**: Optimización por deporte y escenario

### 6. **Facade Pattern** ⭐
- **GestorPartidosFacade**: Simplifica operaciones complejas del sistema
- **Operaciones Unificadas**: Creación completa, búsqueda con invitación, confirmación integral
- **Integración de Todos los Patrones**: Coordina Factory, Strategy, Observer y State

## Correcciones Implementadas del Diagrama Original

### ❌ **Problemas Identificados**
1. Estado inicial incorrecto: `PARTIDO_ARMADO` → **Corregido a** `NECESITAMOS_JUGADORES`
2. Campos obligatorios incorrectos: `deporteFavorito` y `nivel` marcados como `@NotNull`
3. Falta de patrón Observer para notificaciones automáticas
4. Sin algoritmos de emparejamiento de jugadores
5. Solo 2 patrones implementados (insuficiente para requisitos)

### ✅ **Soluciones Implementadas**
1. **Estado Inicial Corregido**: Secuencia de estados según enunciado
2. **Campos Opcionales**: `deporteFavorito` y `nivel` ahora nullable con constructores flexibles
3. **Observer Automático**: Notificaciones y estadísticas por eventos del sistema
4. **Emparejamiento Inteligente**: 3 algoritmos de búsqueda de jugadores compatibles
5. **6 Patrones Completos**: Excede requisitos mínimos con implementación robusta

## Principios SOLID y GRASP Aplicados

### SOLID
- **S**: Cada clase tiene responsabilidad única y bien definida
- **O**: Extensible con nuevas estrategias, estados y observadores  
- **L**: Implementaciones completamente intercambiables
- **I**: Interfaces específicas y cohesivas
- **D**: Dependencias de abstracciones con inyección automática

### GRASP  
- **Information Expert**: Entidades manejan su propia lógica de dominio
- **Creator**: Factory patterns centralizan creación compleja
- **Controller**: Facade coordina operaciones del sistema
- **Low Coupling**: Módulos independientes comunicados por interfaces

## Diagrama UML de Clases Corregido

```mermaid
classDiagram
    %% Entidades principales del dominio
    class Cuenta {
        -Long id
        -String nombreUsuario
        -String email
        -String contraseña
        -TipoDeporte deporteFavorito
        -NivelDeJuego nivel
        -Ubicacion ubicacion
        -List~IObservador~ observadores
        +obtenerUbicacion() String
        +esCompatibleCon(Cuenta otra) boolean
        +actualizarPerfil(CuentaDTO datos) void
        +agregarObservador(IObservador obs) void
        +removerObservador(IObservador obs) void
        +notificarObservadores(String evento) void
    }

    class Partido {
        -Long id
        -TipoDeporte tipoDeporte
        -int cantidadJugadores
        -int duracion
        -LocalDateTime fechaHora
        -String descripcion
        -IEstado estado
        -List~Cuenta~ jugadores
        -Cuenta creador
        -Ubicacion ubicacion
        -LocalDateTime creadoEn
        -List~IObservador~ observadores
        +cambiarEstado(IEstado nuevoEstado) void
        +agregarJugador(Cuenta cuenta) void
        +removerJugador(Cuenta cuenta) void
        +estaCompleto() boolean
        +verificarTransicionAutomatica() void
        +agregarObservador(IObservador obs) void
        +removerObservador(IObservador obs) void
        +notificarObservadores(String evento) void
    }

    class Ubicacion {
        -Double latitud
        -Double longitud
        -String direccion
        +obtenerUbicacion() String
        +calcularDistancia(Ubicacion otra) Double
        +validarCoordenadas() boolean
    }

    %% Enumeraciones
    class TipoDeporte {
        <<enumeration>>
        FUTBOL
        BASQUET
        VOLEY
        TENIS
        +getNombre() String
        +getJugadoresMaximos() int
        +getDuracionMinutos() int
    }

    class NivelDeJuego {
        <<enumeration>>
        PRINCIPIANTE
        INTERMEDIO
        AVANZADO
        +getDescripcion() String
        +esCompatibleCon(NivelDeJuego otro) boolean
        +getPuntuacion() int
    }

    %% Patrón Observer - Sistema de Eventos
    class IObservador {
        <<interface>>
        +actualizar(String evento, Object datos) void
        +getTipo() String
    }

    class NotificadorObservador {
        -IEstrategiaNotificacion estrategia
        +actualizar(String evento, Object datos) void
        +getTipo() String
        -procesarEvento(String evento, Object datos) void
    }

    class EstadisticasObservador {
        -EstadisticasService estadisticas
        +actualizar(String evento, Object datos) void
        +getTipo() String
        -registrarEvento(String evento, Object datos) void
    }

    %% Patrón Strategy - Sistema de Notificaciones
    class IEstrategiaNotificacion {
        <<interface>>
        +enviarNotificacion(String mensaje, Cuenta destinatario) void
        +estaDisponible() boolean
        +obtenerNombre() String
        +configurar(Map~String, Object~ config) void
    }

    class Notificador {
        -IEstrategiaNotificacion estrategia
        +enviarNotificacion(String mensaje, Cuenta destinatario) void
        +cambiarEstrategia(IEstrategiaNotificacion nuevaEstrategia) void
        +notificarPartidoNuevo(Partido partido, List~Cuenta~ interesados) void
        +notificarCambioEstado(Partido partido) void
        +estaListo() boolean
    }

    class Firebase {
        -String apiKey
        -String projectId
        -boolean configurado
        +enviarNotificacion(String mensaje, Cuenta destinatario) void
        +estaDisponible() boolean
        +obtenerNombre() String
        +configurar(Map~String, Object~ config) void
        -prepararPayload(String mensaje, Cuenta destinatario) JsonObject
        -enviarPush(JsonObject payload) void
    }

    class JavaMail {
        -String servidorSMTP
        -int puerto
        -String usuario
        -String contraseña
        -boolean configurado
        +enviarNotificacion(String mensaje, Cuenta destinatario) void
        +estaDisponible() boolean
        +obtenerNombre() String
        +configurar(Map~String, Object~ config) void
        -crearMensaje(String contenido, Cuenta destinatario) MimeMessage
        -enviarEmail(MimeMessage mensaje) void
    }

    %% Patrón Strategy - Sistema de Emparejamiento
    class IEstrategiaEmparejamiento {
        <<interface>>
        +encontrarJugadoresCompatibles(Partido partido, List~Cuenta~ candidatos) List~Cuenta~
        +calcularCompatibilidad(Cuenta jugador, Partido partido) double
        +obtenerNombre() String
    }

    class EmparejadorService {
        -IEstrategiaEmparejamiento estrategia
        +cambiarEstrategia(IEstrategiaEmparejamiento nueva) void
        +buscarJugadores(Partido partido) List~Cuenta~
        +sugerirPartidos(Cuenta jugador) List~Partido~
    }

    class EstrategiaPorNivel {
        +encontrarJugadoresCompatibles(Partido partido, List~Cuenta~ candidatos) List~Cuenta~
        +calcularCompatibilidad(Cuenta jugador, Partido partido) double
        +obtenerNombre() String
        -esNivelCompatible(NivelDeJuego nivel1, NivelDeJuego nivel2) boolean
    }

    class EstrategiaPorCercania {
        -double radioMaximoKm
        +encontrarJugadoresCompatibles(Partido partido, List~Cuenta~ candidatos) List~Cuenta~
        +calcularCompatibilidad(Cuenta jugador, Partido partido) double
        +obtenerNombre() String
        -calcularDistancia(Ubicacion origen, Ubicacion destino) double
    }

    class EstrategiaPorHistorial {
        -HistorialService historialService
        +encontrarJugadoresCompatibles(Partido partido, List~Cuenta~ candidatos) List~Cuenta~
        +calcularCompatibilidad(Cuenta jugador, Partido partido) double
        +obtenerNombre() String
        -obtenerHistorial(Cuenta jugador) List~PartidoHistorico~
        -calcularPuntuacionHistorial(List~PartidoHistorico~ historial) double
    }

    %% Patrón State - Estados del Partido (SECUENCIA CORREGIDA)
    class IEstado {
        <<interface>>
        +manejar(Partido partido) void
        +obtenerNombre() String
        +puedeAgregarJugadores() boolean
        +puedeRemoverJugadores() boolean
        +siguienteEstado() IEstado
        +puedeTransicionarA(IEstado nuevoEstado) boolean
    }

    class NecesitamosJugadores {
        +manejar(Partido partido) void
        +obtenerNombre() String
        +puedeAgregarJugadores() boolean
        +puedeRemoverJugadores() boolean
        +siguienteEstado() IEstado
        +puedeTransicionarA(IEstado estado) boolean
        -buscarJugadoresAutomaticamente(Partido partido) void
    }

    class PartidoArmado {
        +manejar(Partido partido) void
        +obtenerNombre() String
        +puedeAgregarJugadores() boolean
        +puedeRemoverJugadores() boolean
        +siguienteEstado() IEstado
        +puedeTransicionarA(IEstado estado) boolean
        -solicitarConfirmaciones(Partido partido) void
    }

    class Confirmado {
        +manejar(Partido partido) void
        +obtenerNombre() String
        +puedeAgregarJugadores() boolean
        +puedeRemoverJugadores() boolean
        +siguienteEstado() IEstado
        +puedeTransicionarA(IEstado estado) boolean
        -prepararInicio(Partido partido) void
    }

    class EnJuego {
        +manejar(Partido partido) void
        +obtenerNombre() String
        +puedeAgregarJugadores() boolean
        +puedeRemoverJugadores() boolean
        +siguienteEstado() IEstado
        +puedeTransicionarA(IEstado estado) boolean
        -monitorearProgreso(Partido partido) void
    }

    class Finalizado {
        +manejar(Partido partido) void
        +obtenerNombre() String
        +puedeAgregarJugadores() boolean
        +puedeRemoverJugadores() boolean
        +siguienteEstado() IEstado
        +puedeTransicionarA(IEstado estado) boolean
        -registrarEstadisticas(Partido partido) void
    }

    class Cancelado {
        +manejar(Partido partido) void
        +obtenerNombre() String
        +puedeAgregarJugadores() boolean
        +puedeRemoverJugadores() boolean
        +siguienteEstado() IEstado
        +puedeTransicionarA(IEstado estado) boolean
        -notificarCancelacion(Partido partido) void
    }

    %% Patrón Factory - Creación de Estados y Estrategias
    class EstadoFactory {
        <<service>>
        +crearEstado(String tipoEstado) IEstado
        +crearEstadoInicial() IEstado
        +obtenerEstadosDisponibles() List~String~
        -validarTipoEstado(String tipo) boolean
    }

    class EstrategiaNotificacionFactory {
        <<service>>
        +crearEstrategia(String tipo, Map~String, Object~ config) IEstrategiaNotificacion
        +obtenerEstrategiasDisponibles() List~String~
        -validarConfiguracion(String tipo, Map~String, Object~ config) boolean
    }

    class EstrategiaEmparejamientoFactory {
        <<service>>
        +crearEstrategia(String tipo) IEstrategiaEmparejamiento
        +obtenerEstrategiasDisponibles() List~String~
        +crearEstrategiaCombinada(List~IEstrategiaEmparejamiento~ estrategias) IEstrategiaEmparejamiento
    }

    %% Patrón Facade - Simplificación de operaciones complejas
    class GestorPartidosFacade {
        <<service>>
        -PartidoService partidoService
        -CuentaService cuentaService
        -EmparejadorService emparejadorService
        -Notificador notificador
        +crearYPublicarPartido(PartidoDTO datos, Long creadorId) Partido
        +unirJugadorAPartido(Long partidoId, Long jugadorId) boolean
        +buscarPartidosRecomendados(Long jugadorId) List~Partido~
        +procesarInicioPartido(Long partidoId) boolean
        +cancelarPartidoCompleto(Long partidoId, String razon) void
        -validarYPrepararPartido(PartidoDTO datos) boolean
        -notificarJugadoresInteresados(Partido partido) void
    }

    %% Capa de Servicios - Lógica de Negocio
    class CuentaService {
        -CuentaRepository cuentaRepository
        -Notificador notificador
        -List~IObservador~ observadores
        +crearCuenta(CuentaDTO cuentaDTO) Cuenta
        +buscarPorId(Long id) Cuenta
        +buscarPorEmail(String email) Cuenta
        +actualizarPerfil(Long id, CuentaDTO datos) Cuenta
        +buscarJugadoresInteresados(TipoDeporte deporte, Ubicacion ubicacion) List~Cuenta~
        +eliminarCuenta(Long id) void
        +agregarObservador(IObservador observador) void
        +removerObservador(IObservador observador) void
    }

    class PartidoService {
        -PartidoRepository partidoRepository
        -CuentaRepository cuentaRepository
        -EmparejadorService emparejadorService
        -EstadoFactory estadoFactory
        -List~IObservador~ observadores
        +crearPartido(PartidoDTO partidoDTO) Partido
        +buscarPorId(Long id) Partido
        +buscarPartidos(FiltroPartidoDTO filtro, Pageable pageable) Page~Partido~
        +unirseAPartido(Long partidoId, Long cuentaId) Partido
        +salirDePartido(Long partidoId, Long cuentaId) Partido
        +cambiarEstado(Long partidoId, String nuevoEstado) Partido
        +procesarTransicionesAutomaticas() void
        +cancelarPartido(Long id, String razon) void
        +agregarObservador(IObservador observador) void
        +removerObservador(IObservador observador) void
    }

    %% Capa de Repositorios - Persistencia
    class CuentaRepository {
        <<interface>>
        +findByEmail(String email) Optional~Cuenta~
        +findByDeporteFavoritoAndNivel(TipoDeporte deporte, NivelDeJuego nivel) List~Cuenta~
        +findByUbicacionCercana(Double lat, Double lng, Double radio) List~Cuenta~
        +findJugadoresCompatibles(TipoDeporte deporte, NivelDeJuego nivelMin, NivelDeJuego nivelMax) List~Cuenta~
    }

    class PartidoRepository {
        <<interface>>
        +findByTipoDeporte(TipoDeporte tipo, Pageable pageable) Page~Partido~
        +findPartidosCercanos(Double lat, Double lng, Double radio) List~Partido~
        +findByEstadoAndFechaBetween(String estado, LocalDateTime inicio, LocalDateTime fin) List~Partido~
        +findPartidosConFiltros(FiltroPartidoDTO filtro, Pageable pageable) Page~Partido~
        +findPartidosNecesitanJugadores(TipoDeporte deporte) List~Partido~
        +findPartidosProximosAIniciar(LocalDateTime limite) List~Partido~
    }

    %% Capa de Controladores - API REST
    class CuentaController {
        -CuentaService cuentaService
        -GestorPartidosFacade gestorFacade
        +crearCuenta(CuentaDTO cuentaDTO) ResponseEntity~Cuenta~
        +obtenerPerfil(Long id) ResponseEntity~Cuenta~
        +actualizarPerfil(Long id, CuentaDTO datos) ResponseEntity~Cuenta~
        +buscarPartidosRecomendados(Long id) ResponseEntity~List~
        +eliminarCuenta(Long id) ResponseEntity~Void~
    }

    class PartidoController {
        -PartidoService partidoService
        -GestorPartidosFacade gestorFacade
        +crearPartido(PartidoDTO partidoDTO) ResponseEntity~Partido~
        +obtenerPartido(Long id) ResponseEntity~Partido~
        +buscarPartidos(FiltroPartidoDTO filtro, Pageable pageable) ResponseEntity~Page~
        +unirseAPartido(Long partidoId, Long cuentaId) ResponseEntity~Partido~
        +salirDePartido(Long partidoId, Long cuentaId) ResponseEntity~Partido~
        +cambiarEstado(Long partidoId, String estado) ResponseEntity~Partido~
        +cancelarPartido(Long id, String razon) ResponseEntity~Void~
    }

    %% DTOs - Data Transfer Objects
    class CuentaDTO {
        +String nombreUsuario
        +String email
        +String contraseña
        +TipoDeporte deporteFavorito
        +NivelDeJuego nivel
        +UbicacionDTO ubicacion
    }

    class PartidoDTO {
        +TipoDeporte tipoDeporte
        +int cantidadJugadores
        +int duracion
        +LocalDateTime fechaHora
        +String descripcion
        +UbicacionDTO ubicacion
        +Long creadorId
    }

    class UbicacionDTO {
        +Double latitud
        +Double longitud
        +String direccion
    }

    class FiltroPartidoDTO {
        +TipoDeporte tipoDeporte
        +String estado
        +NivelDeJuego nivelMinimo
        +NivelDeJuego nivelMaximo
        +Double latitud
        +Double longitud
        +Double radioEnKm
        +LocalDateTime fechaInicio
        +LocalDateTime fechaFin
    }

    %% ================================
    %% RELACIONES DEL DOMINIO
    %% ================================
    
    %% Relaciones básicas de entidades
    Cuenta "1" *-- "1" Ubicacion : tiene
    Cuenta "1" --> "0..1" TipoDeporte : prefiere
    Cuenta "1" --> "0..1" NivelDeJuego : nivel
    
    Partido "1" *-- "1" Ubicacion : ubicado en
    Partido "1" --> "1" TipoDeporte : tipo
    Partido "1" o-- "*" Cuenta : jugadores participan
    Partido "1" --> "1" Cuenta : creado por
    Partido "1" *-- "1" IEstado : estado actual

    %% Patrón Observer
    Cuenta "1" o-- "*" IObservador : observado por
    Partido "1" o-- "*" IObservador : observado por
    IObservador <|.. NotificadorObservador : implementa
    IObservador <|.. EstadisticasObservador : implementa

    %% Patrón Strategy - Notificaciones
    Notificador "1" *-- "1" IEstrategiaNotificacion : usa estrategia
    IEstrategiaNotificacion <|.. Firebase : implementa
    IEstrategiaNotificacion <|.. JavaMail : implementa
    NotificadorObservador "1" --> "1" Notificador : utiliza

    %% Patrón Strategy - Emparejamiento
    EmparejadorService "1" *-- "1" IEstrategiaEmparejamiento : usa estrategia
    IEstrategiaEmparejamiento <|.. EstrategiaPorNivel : implementa
    IEstrategiaEmparejamiento <|.. EstrategiaPorCercania : implementa
    IEstrategiaEmparejamiento <|.. EstrategiaPorHistorial : implementa

    %% Patrón State - Secuencia corregida según enunciado
    IEstado <|.. NecesitamosJugadores : implementa
    IEstado <|.. PartidoArmado : implementa
    IEstado <|.. Confirmado : implementa
    IEstado <|.. EnJuego : implementa
    IEstado <|.. Finalizado : implementa
    IEstado <|.. Cancelado : implementa

    %% Patrón Factory
    EstadoFactory ..> IEstado : crea
    EstrategiaNotificacionFactory ..> IEstrategiaNotificacion : crea
    EstrategiaEmparejamientoFactory ..> IEstrategiaEmparejamiento : crea

    %% Patrón Facade
    GestorPartidosFacade ..> PartidoService : usa
    GestorPartidosFacade ..> CuentaService : usa
    GestorPartidosFacade ..> EmparejadorService : usa
    GestorPartidosFacade ..> Notificador : usa

    %% Dependencias de Servicios
    CuentaService ..> CuentaRepository : accede a datos
    CuentaService ..> Notificador : envía notificaciones
    PartidoService ..> PartidoRepository : accede a datos
    PartidoService ..> CuentaRepository : accede a datos
    PartidoService ..> EmparejadorService : busca jugadores
    PartidoService ..> EstadoFactory : crea estados

    %% Dependencias de Controladores (MVC)
    CuentaController ..> CuentaService : delega lógica
    CuentaController ..> GestorPartidosFacade : operaciones complejas
    PartidoController ..> PartidoService : delega lógica
    PartidoController ..> GestorPartidosFacade : operaciones complejas

    %% Dependencias de DTOs
    CuentaController ..> CuentaDTO : usa
    CuentaController ..> UbicacionDTO : usa
    PartidoController ..> PartidoDTO : usa
    PartidoController ..> FiltroPartidoDTO : usa
    CuentaService ..> CuentaDTO : transforma
    PartidoService ..> PartidoDTO : transforma
    PartidoService ..> FiltroPartidoDTO : filtra
```

## Arquitectura del Sistema

### Capas de la Aplicación

1. **Capa de Presentación (Controllers)**
   - `CuentaController`: Gestión de usuarios
   - `PartidoController`: Gestión de partidos

2. **Capa de Negocio (Services)**
   - `CuentaService`: Lógica de negocio para usuarios
   - `PartidoService`: Lógica de negocio para partidos
   - `NotificadorService`: Gestión de notificaciones

3. **Capa de Datos (Repositories)**
   - `CuentaRepository`: Persistencia de usuarios
   - `PartidoRepository`: Persistencia de partidos

4. **Capa de Dominio (Models)**
   - Entidades JPA con anotaciones de validación
   - Enumeraciones para tipos constantes
   - Interfaces para patrones de diseño

### DTOs (Data Transfer Objects)
- `CuentaDTO`: Para transferencia de datos de usuario
- `PartidoDTO`: Para transferencia de datos de partido
- `FiltroPartidoDTO`: Para filtros de búsqueda

## Tecnologías Utilizadas

- **Spring Boot 3.x**: Framework principal
- **Spring Data JPA**: Persistencia de datos
- **Spring Web**: APIs REST
- **H2/PostgreSQL**: Base de datos
- **Bean Validation**: Validación de datos
- **Maven**: Gestión de dependencias

## Funcionalidades Principales

### Gestión de Usuarios
- Registro de nuevos usuarios
- Autenticación y autorización
- Actualización de perfil deportivo
- Gestión de ubicación y preferencias

### Gestión de Partidos
- Creación de nuevos partidos
- Búsqueda y filtrado de partidos
- Unirse y salir de partidos
- Gestión de estados del partido
- Notificaciones automáticas

### Sistema de Notificaciones
- Estrategias intercambiables (Firebase, Email)
- Notificaciones automáticas por cambios de estado
- Configuración personalizable por usuario

## Code Smells Evitados

1. **Long Method**: Métodos mantenidos bajo 20 líneas
2. **Large Class**: Clases con responsabilidad única
3. **Duplicate Code**: Reutilización mediante herencia e interfaces
4. **Data Class**: Entidades con comportamiento, no solo datos
5. **Feature Envy**: Cada clase gestiona sus propios datos
6. **God Object**: Responsabilidades distribuidas correctamente

## Beneficios de la Arquitectura

- **Mantenibilidad**: Código limpio y bien estructurado
- **Extensibilidad**: Fácil adición de nuevas funcionalidades
- **Testabilidad**: Componentes desacoplados y testeables
- **Escalabilidad**: Arquitectura preparada para crecimiento
- **Reutilización**: Componentes reutilizables y modulares 

## Correcciones Realizadas al Diagrama

### ✅ Problemas Identificados y Solucionados

#### 1. **Secuencia de Estados Corregida**
**Problema Original**: El código tenía como estado inicial `PARTIDO_ARMADO`
**Corrección**: Ahora sigue exactamente el enunciado:
- 🟢 **"Necesitamos jugadores"** (estado inicial al crear partido)
- 🟡 **"Partido armado"** (cuando se alcanza el número requerido)
- 🔵 **"Confirmado"** (todos los jugadores aceptan)
- 🟠 **"En juego"** (partido en curso)
- ✅ **"Finalizado"** (partido completado)
- ❌ **"Cancelado"** (partido cancelado)

#### 2. **Patrón Strategy para Emparejamiento Implementado**
**Problema Original**: Faltaba el sistema de emparejamiento de jugadores
**Corrección**: Agregado sistema completo con:
- `IEstrategiaEmparejamiento` (interfaz)
- `EstrategiaPorNivel` (emparejamiento por habilidad)
- `EstrategiaPorCercania` (emparejamiento por ubicación)
- `EstrategiaPorHistorial` (emparejamiento por historial de partidos)
- `EmparejadorService` (contexto del patrón)

#### 3. **Patrón Observer para Notificaciones Automáticas**
**Problema Original**: Notificaciones manuales, no automáticas
**Corrección**: Sistema completo de eventos:
- `IObservador` (interfaz observer)
- `NotificadorObservador` (notificaciones automáticas)
- `EstadisticasObservador` (registro automático de estadísticas)
- Eventos automáticos en cambios de estado

#### 4. **Campos Opcionales Corregidos**
**Problema Original**: `deporteFavorito` y `nivel` marcados como `@NotNull`
**Corrección**: Ahora son opcionales (`0..1` en relaciones UML)

#### 5. **Patrones Adicionales Implementados**
**Problema Original**: Solo 2 patrones implementados (State, Strategy parcial)
**Corrección**: Ahora implementa 6 patrones:
- ✅ **State** (estados del partido)
- ✅ **Strategy** (notificaciones + emparejamiento)
- ✅ **Observer** (eventos automáticos)
- ✅ **Factory** (creación de estados y estrategias)
- ✅ **Facade** (simplificación de operaciones complejas)
- ✅ **MVC** (arquitectura principal)

## Patrones de Diseño Implementados

### 1. **State Pattern** 🔄
```java
// Permite que el partido cambie su comportamiento según su estado
partido.cambiarEstado(new NecesitamosJugadores());
// Transiciones automáticas según reglas de negocio
```

### 2. **Strategy Pattern** 🎯
```java
// Estrategias de Notificación
notificador.cambiarEstrategia(new Firebase());
notificador.cambiarEstrategia(new JavaMail());

// Estrategias de Emparejamiento
emparejador.cambiarEstrategia(new EstrategiaPorNivel());
emparejador.cambiarEstrategia(new EstrategiaPorCercania());
```

### 3. **Observer Pattern** 👁️
```java
// Notificaciones automáticas
partido.agregarObservador(new NotificadorObservador());
partido.cambiarEstado(new PartidoArmado()); // Notifica automáticamente
```

### 4. **Factory Pattern** 🏭
```java
// Creación controlada de objetos
IEstado estado = estadoFactory.crearEstado("NECESITAMOS_JUGADORES");
IEstrategiaNotificacion notif = notifFactory.crearEstrategia("FIREBASE", config);
```

### 5. **Facade Pattern** 🎭
```java
// Simplifica operaciones complejas
gestorFacade.crearYPublicarPartido(partidoDTO, creadorId);
// Internamente: crea partido + busca jugadores + envía notificaciones
```

### 6. **MVC Architecture** 🏗️
- **Model**: Entidades del dominio (`Partido`, `Cuenta`, `Ubicacion`)
- **View**: DTOs para transferencia de datos
- **Controller**: APIs REST que delegan a servicios

## Funcionalidades Según Enunciado

### ✅ Registro de Usuarios
- Nombre de usuario, email, contraseña (obligatorios)
- Deporte favorito y nivel (opcionales)
- Ubicación (obligatoria)

### ✅ Búsqueda de Partidos
- Por tipo de deporte
- Por ubicación (radio de distancia)
- Por nivel de juego
- Por estado del partido
- Por rango de fechas

### ✅ Creación de Partidos
- Estado inicial: "Necesitamos jugadores"
- Atributos requeridos: tipo deporte, cantidad jugadores, duración, ubicación, horario

### ✅ Gestión de Estados
- Transiciones automáticas según reglas de negocio
- Notificaciones automáticas en cada cambio
- Validaciones de estado para operaciones

### ✅ Estrategias de Emparejamiento
- **Por nivel**: Jugadores de nivel similar
- **Por cercanía**: Jugadores en la zona
- **Por historial**: Basado en partidos previos
- Intercambiables dinámicamente

### ✅ Sistema de Notificaciones
- **Firebase** para notificaciones push
- **JavaMail** para correos electrónicos
- Eventos automáticos:
  - Partido nuevo para deporte favorito
  - Partido armado (suficientes jugadores)
  - Partido confirmado
  - Cambios de estado (En juego, Finalizado, Cancelado) 