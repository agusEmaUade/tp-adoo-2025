package com.tp.uno.mas.encuentros.deportivos.model;

import com.tp.uno.mas.encuentros.deportivos.strategy.*;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class BuscadorPartidos {
    private List<EstrategiaBusqueda> estrategiasActivas;

    public BuscadorPartidos() {
        this.estrategiasActivas = new ArrayList<>();
        // Inicializar con estrategia mixta por defecto
        this.estrategiasActivas.add(new BusquedaMixta());
    }

    public List<Partido> buscarPartidos(List<Partido> partidos, Usuario usuario) {
        if (partidos == null || partidos.isEmpty()) {
            return new ArrayList<>();
        }

        // Si no hay estrategias activas, usar búsqueda mixta por defecto
        if (estrategiasActivas.isEmpty()) {
            estrategiasActivas.add(new BusquedaMixta());
        }

        // Aplicar todas las estrategias activas
        List<Partido> resultados = new ArrayList<>();
        for (EstrategiaBusqueda estrategia : estrategiasActivas) {
            resultados.addAll(estrategia.buscar(partidos, usuario));
        }

        // Eliminar duplicados y ordenar por distancia
        return resultados.stream()
                .distinct()
                .filter(partido -> puedeUnirseAlPartido(usuario, partido))
                .sorted(crearComparadorPartidos(usuario.getUbicacion()))
                .collect(Collectors.toList());
    }

    // Métodos de conveniencia para búsquedas específicas
    public List<Partido> buscarPartidosCercanos(List<Partido> partidosDisponibles, Usuario usuario, double radioKm) {
        return new BusquedaPorCercania(radioKm).buscar(partidosDisponibles, usuario);
    }

    public List<Partido> buscarPartidosPorDeporte(List<Partido> partidosDisponibles, Usuario usuario, String deporte) {
        return new BusquedaPorDeporte(deporte).buscar(partidosDisponibles, usuario);
    }

    public List<Partido> buscarPartidosPorNivel(List<Partido> partidosDisponibles, Usuario usuario) {
        return new BusquedaPorNivel().buscar(partidosDisponibles, usuario);
    }

    public List<Partido> buscarPartidosConCriteriosMixtos(List<Partido> partidosDisponibles, Usuario usuario, int criteriosMinimos) {
        BusquedaMixta estrategiaMixta = new BusquedaMixta();
        estrategiaMixta.setCriteriosMinimosRequeridos(criteriosMinimos);
        List<Partido> resultados = estrategiaMixta.buscar(partidosDisponibles, usuario);
        return resultados != null ? resultados : new ArrayList<>();
    }

    // Nuevo método para buscar partidos que necesitan pocos jugadores
    public List<Partido> buscarPartidosQueNecesitanPocoJugadores(List<Partido> partidosDisponibles, Usuario usuario, int maxJugadoresNecesarios) {
        if (partidosDisponibles == null || partidosDisponibles.isEmpty()) {
            return new ArrayList<>();
        }

        return partidosDisponibles.stream()
                .filter(partido -> puedeUnirseAlPartido(usuario, partido))
                .filter(partido -> contarJugadoresNecesarios(partido) <= maxJugadoresNecesarios)
                .sorted(crearComparadorPartidos(usuario.getUbicacion()))
                .collect(Collectors.toList());
    }

    // Método para calcular la distancia entre un usuario y un partido
    public double calcularDistanciaAlPartido(Usuario usuario, Partido partido) {
        if (usuario == null || usuario.getUbicacion() == null || 
            partido == null || partido.getUbicacion() == null) {
            return Double.MAX_VALUE;
        }
        return usuario.getUbicacion().calcularDistancia(partido.getUbicacion());
    }

    // Método para contar cuántos jugadores faltan para completar un partido
    public int contarJugadoresNecesarios(Partido partido) {
        if (partido == null) {
            return 0;
        }
        return partido.getCantJugadoresRequeridos() - partido.getJugadoresActuales().size();
    }

    // Métodos utilitarios
    private boolean puedeUnirseAlPartido(Usuario usuario, Partido partido) {
        if (partido == null || partido.getEstadoActual() == null || usuario == null) {
            return false;
        }

        // Verificar estado del partido
        if (!partido.getEstadoActual().puedeAgregarJugador()) {
            return false;
        }

        // Verificar si el partido está lleno
        if (partido.estaCompleto()) {
            return false;
        }

        // Verificar criterios del partido si existen
        if (partido.getCriterios() != null) {
            return partido.getCriterios().cumpleCriterios(usuario);
        }

        return true;
    }

    private Comparator<Partido> crearComparadorPartidos(Ubicacion ubicacionUsuario) {
        return (p1, p2) -> {
            if (ubicacionUsuario == null) return 0;
            
            double dist1 = p1.getUbicacion().calcularDistancia(ubicacionUsuario);
            double dist2 = p2.getUbicacion().calcularDistancia(ubicacionUsuario);
            return Double.compare(dist1, dist2);
        };
    }

    // Getters y setters para estrategias
    public void agregarEstrategia(EstrategiaBusqueda estrategia) {
        if (estrategia != null) {
            estrategiasActivas.add(estrategia);
        }
    }

    public void limpiarEstrategias() {
        estrategiasActivas.clear();
    }

    public List<EstrategiaBusqueda> getEstrategiasActivas() {
        return new ArrayList<>(estrategiasActivas);
    }
}

class BusquedaMixta implements EstrategiaBusqueda {

    private int criteriosMinimosRequeridos = 1;

    public void setCriteriosMinimosRequeridos(int i) {
        this.criteriosMinimosRequeridos = i;
    }

    @Override
    public List<Partido> buscar(List<Partido> partidos, Usuario usuario) {
        return partidos.stream()
                .filter(partido -> cumpleCriterios(partido, usuario))
                .collect(Collectors.toList());
    }

    @Override
    public boolean cumpleCriterios(Partido partido, Usuario usuario) {
        int criteriosCumplidos = 0;
        if (new BusquedaPorDeporte().cumpleCriterios(partido, usuario)) {
            criteriosCumplidos++;
        }
        if (new BusquedaPorNivel().cumpleCriterios(partido, usuario)) {
            criteriosCumplidos++;
        }
        if (new BusquedaPorCercania().cumpleCriterios(partido, usuario)) {
            criteriosCumplidos++;
        }
        return criteriosCumplidos >= criteriosMinimosRequeridos;
    }
} 