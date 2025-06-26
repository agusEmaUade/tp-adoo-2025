package com.tp.uno.mas.encuentros.deportivos.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import com.tp.uno.mas.encuentros.deportivos.model.nivel.Nivel;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriteriosBusqueda {
    private String deporte;
    private String fecha;
    private double radioMaximo; // en km
    private String nivelMinimo;
    private String nivelMaximo;
    private int maxJugadoresNecesarios; // para encontrar partidos que necesiten pocos jugadores
    private boolean soloPartidosDisponibles; // solo partidos que acepten nuevos jugadores

    /**
     * Método mejorado que recibe directamente el Usuario (con objeto Nivel)
     */
    public boolean cumpleCriteriosUsuario(Usuario usuario, Partido partido) {
        // Verificar deporte
        if (deporte != null && !deporte.isEmpty() && !partido.getDeporte().equalsIgnoreCase(deporte)) {
            return false;
        }

        // Verificar fecha
        if (fecha != null && !fecha.isEmpty() && !partido.getFecha().equals(fecha)) {
            return false;
        }

        // Verificar distancia
        if (radioMaximo > 0 && usuario.getUbicacion() != null && partido.getUbicacion() != null) {
            double distancia = usuario.getUbicacion().calcularDistancia(partido.getUbicacion());
            if (distancia > radioMaximo) {
                return false;
            }
        }

        // Verificar que el partido necesite jugadores
        if (soloPartidosDisponibles && !partido.getEstadoActual().puedeAgregarJugador()) {
            return false;
        }

        // Verificar cantidad de jugadores necesarios
        if (maxJugadoresNecesarios > 0) {
            int jugadoresActuales = partido.getEquipos().stream()
                    .mapToInt(Equipo::cantidadJugadores)
                    .sum();
            int jugadoresNecesarios = partido.getCantJugadoresRequeridos() - jugadoresActuales;
            
            if (jugadoresNecesarios > maxJugadoresNecesarios) {
                return false;
            }
        }

        // Verificar nivel
        if (partido.getCriterios() != null) {

            if (!usuario.getNivel().puedeUnirseA(partido)) {
                return false;
            }
        }

        return true;
    }

    /**
     * Método legacy para compatibilidad hacia atrás
     */
    public boolean cumpleCriteriosPartido(Partido partido, Ubicacion ubicacionUsuario) {
        // Verificar deporte
        if (deporte != null && !deporte.isEmpty() && !partido.getDeporte().equalsIgnoreCase(deporte)) {
            return false;
        }

        // Verificar fecha
        if (fecha != null && !fecha.isEmpty() && !partido.getFecha().equals(fecha)) {
            return false;
        }

        // Verificar distancia
        if (radioMaximo > 0 && ubicacionUsuario != null && partido.getUbicacion() != null) {
            double distancia = ubicacionUsuario.calcularDistancia(partido.getUbicacion());
            if (distancia > radioMaximo) {
                return false;
            }
        }

        // Verificar que el partido necesite jugadores
        if (soloPartidosDisponibles && !partido.getEstadoActual().puedeAgregarJugador()) {
            return false;
        }

        // Verificar cantidad de jugadores necesarios
        if (maxJugadoresNecesarios > 0) {
            int jugadoresActuales = partido.getEquipos().stream()
                    .mapToInt(Equipo::cantidadJugadores)
                    .sum();
            int jugadoresNecesarios = partido.getCantJugadoresRequeridos() - jugadoresActuales;
            
            if (jugadoresNecesarios > maxJugadoresNecesarios) {
                return false;
            }
        }

        // Verificar nivel compatible - ahora más simple con objeto Nivel en Usuario
        if (partido.getCriterios() != null && (nivelMinimo != null || nivelMaximo != null)) {
            // Simplificado: verificar directamente desde el criterio de búsqueda contra el partido
            if (nivelMinimo != null) {
                Nivel nivelMin = Nivel.desde(nivelMinimo);
                if (!nivelMin.puedeUnirseA(partido)) {
                    return false;
                }
            }
            
            if (nivelMaximo != null) {
                Nivel nivelMax = Nivel.desde(nivelMaximo);
                if (!nivelMax.puedeUnirseA(partido)) {
                    return false;
                }
            }
        }

        return true;
    }

    // Métodos de conveniencia que usan el nuevo Enum Nivel
    
    /**
     * Calcula compatibilidad de nivel
     */
    public double calcularCompatibilidadNivel(String nivelPartido) {
        if (nivelMinimo == null) return 1.0;
        Nivel nivelMin = Nivel.desde(nivelMinimo);
        Nivel nivelPartidoEnum = Nivel.desde(nivelPartido);
        return nivelMin.calcularCompatibilidad(nivelPartidoEnum);
    }
    
    /**
     * Verifica compatibilidad de nivel
     */
    public boolean esNivelCompatible(String nivelPartido) {
        Nivel nivelPartidoEnum = Nivel.desde(nivelPartido);
        
        if (nivelMinimo != null) {
            Nivel nivelMin = Nivel.desde(nivelMinimo);
            if (!nivelMin.esCompatibleCon(nivelPartidoEnum)) {
                return false;
            }
        }
        
        if (nivelMaximo != null) {
            Nivel nivelMax = Nivel.desde(nivelMaximo);
            if (!nivelMax.esCompatibleCon(nivelPartidoEnum)) {
                return false;
            }
        }
        
        return true;
    }


} 