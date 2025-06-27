package com.tp.uno.mas.encuentros.deportivos.model.nivel;


/**
 * Enum para niveles de habilidad de los usuarios
 */
public enum Nivel {
    PRINCIPIANTE("principiante", 1),
    INTERMEDIO("intermedio", 2),
    AVANZADO("avanzado", 3);
    
    private final String nombre;
    private final int valor;
    
    Nivel(String nombre, int valor) {
        this.nombre = nombre;
        this.valor = valor;
    }
    
    // FACTORY METHOD - Crea instancias desde String
    public static Nivel desde(String texto) {
        if (texto == null) return PRINCIPIANTE;
        
        for (Nivel nivel : values()) {
            if (nivel.nombre.equalsIgnoreCase(texto.trim())) {
                return nivel;
            }
        }
        return PRINCIPIANTE; // Por defecto
    }
    
    // MÉTODOS DE NEGOCIO
    
    public boolean esCompatibleCon(Nivel otro) {
        return Math.abs(this.valor - otro.valor) <= 1;
    }
    
    public double calcularCompatibilidad(Nivel otro) {
        int diferencia = Math.abs(this.valor - otro.valor);
        
        switch (diferencia) {
            case 0: return 1.0; // Mismo nivel
            case 1: return 0.7; // Un nivel de diferencia
            case 2: return 0.3; // Dos niveles
            default: return 0.0; // Incompatible
        }
    }
    
    public boolean estaEnRango(Nivel minimo, Nivel maximo) {
        if (minimo != null && this.valor < minimo.valor) {
            return false;
        }
        if (maximo != null && this.valor > maximo.valor) {
            return false;
        }
        return true;
    }
    
    public String obtenerSugerencias() {
        switch (this) {
            case PRINCIPIANTE:
                return "Ideal para jugar con otros principiantes o jugadores intermedios pacientes.";
            case INTERMEDIO:
                return "Nivel versátil, puede jugar con principiantes o avanzados.";
            case AVANZADO:
                return "Busca partidos competitivos con otros avanzados.";
            default:
                return "";
        }
    }
    
    // GETTERS
    
    public String getNombre() {
        return nombre;
    }
    
    public int getValor() {
        return valor;
    }
    
    @Override
    public String toString() {
        return nombre;
    }
} 