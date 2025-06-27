# Sistema de Gestión de Encuentros Deportivos

Este proyecto es una aplicación Java para organizar y unirse a encuentros deportivos. Permite a los usuarios crear partidos, buscar encuentros disponibles, gestionar el ciclo de vida de un partido y recibir notificaciones sobre eventos importantes.

La arquitectura del sistema está diseñada siguiendo principios SOLID y utiliza varios patrones de diseño para garantizar un código modular, flexible y mantenible.

## Patrones de Diseño Utilizados

A continuación se detallan los patrones de diseño implementados en el proyecto:

### 1. Factory Method

-   **Propósito**: Encapsular la creación de objetos complejos. Permite que una clase delegue la instanciación a subclases, decidiendo qué tipo de objeto concreto crear.
-   **Implementación**:
    -   `PartidoFactory`: Es una clase abstracta que define el método `crearPartido(...)`.
    -   `FutbolFactory`, `TenisFactory`, `BasquetFactory`, `VoleyFactory`: Son las implementaciones concretas que crean un `Partido` con la configuración específica de cada deporte (ej. cantidad de jugadores, equipos, etc.).
    -   El `PartidoController` utiliza estas fábricas para crear partidos sin acoplarse a la lógica de instanciación de un deporte específico.

### 2. State

-   **Propósito**: Permitir que un objeto altere su comportamiento cuando su estado interno cambia. El objeto parecerá cambiar su clase.
-   **Implementación**:
    -   `EstadoPartido`: Es la interfaz que define los métodos que varían con el estado (`puedeAgregarJugador`, `puedeConfirmar`, `manejarCambioEstado`, etc.).
    -   `NecesitamosJugadores`, `PartidoArmado`, `Confirmado`, `EnJuego`, `Finalizado`, `Cancelado`: Son las clases concretas que implementan el comportamiento para cada estado del ciclo de vida de un partido.
    -   La clase `Partido` tiene un atributo `estadoActual` y delega las operaciones a este objeto, cambiando de estado según las acciones que ocurren (ej. se completa el número de jugadores).

### 3. Strategy

-   **Propósito**: Definir una familia de algoritmos, encapsular cada uno de ellos y hacerlos intercambiables. Permite que el algoritmo varíe independientemente de los clientes que lo utilizan.
-   **Implementación**:
    -   `EstrategiaBusqueda`: Es la interfaz que define el método `buscar(...)`.
    -   `BusquedaPorCercania`, `BusquedaPorDeporte`, `BusquedaPorNivel`, `BusquedaPorHistorial`: Son las implementaciones concretas que contienen diferentes algoritmos para filtrar y encontrar partidos.
    -   `BuscadorPartidos` utiliza estas estrategias para ofrecer diferentes formas de búsqueda a los usuarios.

### 4. Observer

-   **Propósito**: Definir una dependencia uno a muchos entre objetos, de modo que cuando un objeto cambia de estado, todos sus dependientes son notificados y actualizados automáticamente.
-   **Implementación**:
    -   `NotificacionManager` (Subject): Mantiene una lista de `NotificacionObserver` y tiene un método `notificarObservers(...)` que se invoca cuando ocurre un evento relevante en un partido.
    -   `NotificacionObserver` (Observer): Es la interfaz con el método `notificar(...)`.
    -   `EmailNotificador` y `PushNotificador`: Son los observadores concretos que reaccionan a la notificación, enviando un email o una notificación push, respectivamente.
    -   `EventoPartido`: Un `enum` que define los diferentes tipos de eventos que pueden ocurrir (`PARTIDO_CREADO`, `JUGADOR_UNIDO`, etc.).

### 5. Adapter

-   **Propósito**: Convertir la interfaz de una clase en otra interfaz que los clientes esperan. Permite que clases con interfaces incompatibles trabajen juntas.
-   **Implementación**:
    -   `ServicioEmail` y `ServicioPush`: Son las interfaces que nuestra aplicación conoce y espera.
    -   `JavaMailAdapter`: Implementa `ServicioEmail` y "traduce" la llamada a los métodos de la librería `Resend` para enviar correos.
    -   `FirebaseAdapter`: Implementa `ServicioPush` y simula la "traducción" de la llamada a los métodos de un servicio de notificaciones push como Firebase.
    -   Esto permite que los `Notificadores` (`EmailNotificador`, `PushNotificador`) dependan de nuestras interfaces y no de librerías externas, facilitando el cambio de proveedor en el futuro.

### 6. Facade

-   **Propósito**: Proporcionar una interfaz unificada y simplificada a un conjunto de interfaces en un subsistema.
-   **Implementación**:
    -   `PartidoController`: Actúa como una fachada para el núcleo del sistema. Centraliza y simplifica las operaciones más comunes como `crearPartido`, `agregarJugador`, `buscarPartidos...`, etc. Oculta la complejidad interna de la interacción entre `BuscadorPartidos`, `NotificacionManager`, `ServicioProgramador` y la gestión del estado de los partidos.

## Cumplimiento de Requerimientos

Cada requerimiento se ha implementado de la siguiente manera:

#### 1. Registro de usuarios
-   **Implementación**: La clase `com.tp.uno.mas.encuentros.deportivos.model.Usuario` modela toda la información del usuario, incluyendo nombre, email, contraseña, deporte favorito, nivel (`enum Nivel`), ubicación y otros datos opcionales. La clase `CuentaController` se utiliza en la demo para la creación de usuarios de ejemplo.

#### 2. Búsqueda de partidos
-   **Implementación**: El `PartidoController` expone métodos como `buscarPartidosCercanos(...)` y `buscarPorDeporte(...)`. Internamente, delega esta responsabilidad a `BuscadorPartidos`, que utiliza el **Patrón Strategy** con las distintas implementaciones de `EstrategiaBusqueda` para filtrar los partidos disponibles.

#### 3. Creación de un partido
-   **Implementación**: Se utiliza el **Patrón Factory Method**. El `PartidoController.crearPartido(...)` recibe una `PartidoFactory` (ej. `FutbolFactory`) que se encarga de crear un objeto `Partido` con los atributos correctos para ese deporte (deporte, cantidad de jugadores, etc.). Al crearse, el partido se asigna automáticamente al estado `NecesitamosJugadores`, cumpliendo con el requerimiento.

#### 4. Estado de los partidos
-   **Implementación**: Se gestiona a través del **Patrón State**.
    -   **a. Partido armado**: La lógica dentro de `Partido.agregarJugador(...)` verifica si el partido está completo. Si lo está, llama a `estadoActual.manejarCambioEstado(this)`, y la implementación en `NecesitamosJugadores` cambia el estado del partido a `PartidoArmado`.
    -   **b. Confirmado, Finalizado, Cancelado**: El `PartidoController` tiene métodos (`confirmarPartido`, `finalizarPartido`, `cancelarPartido`) que delegan la acción al estado actual del partido. Cada estado (`PartidoArmado`, `EnJuego`, etc.) decide si la acción es válida.
    -   **c. En juego**: El `ServicioProgramador` verifica periódicamente los partidos. Si un partido está `Confirmado` y su fecha/hora ha llegado, su método `verificarYActualizarPartidos` cambia el estado a `EnJuego`.

#### 5. Estrategia de búsqueda de jugadores
-   **Implementación**: Se aborda con el **Patrón Strategy**.
    -   **a, d. Estrategias**: Se implementaron `BusquedaPorNivel`, `BusquedaPorCercania` y `BusquedaPorHistorial` como estrategias intercambiables.
    -   **b, c. Niveles**: La clase `Usuario` tiene un atributo `Nivel` (enum). La clase `CriteriosPartido` permite definir un `nivelMinimo` y `nivelMaximo` para un partido. La estrategia `BusquedaPorNivel` utiliza estos criterios para filtrar a los jugadores.

#### 6. Notificaciones
-   **Implementación**: Resuelto con los patrones **Observer** y **Adapter**.
    -   **a. Envío de notificaciones**: `NotificacionManager` (Subject) notifica a sus `Observers` (`EmailNotificador`, `PushNotificador`). Estos, a su vez, utilizan `Adapters` (`JavaMailAdapter`, `FirebaseAdapter`) para comunicarse con los servicios externos.
    -   **b. Eventos**: En cada punto clave del flujo (`crearPartido`, `agregarJugador`, cambio de estado), `PartidoController` llama a `notificacionManager.notificarObservers(evento, partido)`, pasando el `EventoPartido` correspondiente (ej. `PARTIDO_CREADO`, `JUGADOR_UNIDO`, `PARTIDO_CONFIRMADO`, etc.), lo que desencadena las notificaciones a todos los participantes.

## Cómo ejecutar el proyecto

### Ejecutar la Demo
Para ver una simulación del sistema en acción, puedes ejecutar la tarea de Gradle:
```shell
./gradlew runDemo
```
### Ejecutar los Tests
Para verificar la integridad del sistema y la correcta implementación de la lógica de negocio, ejecuta la suite de tests:
```shell
./gradlew test
``` 