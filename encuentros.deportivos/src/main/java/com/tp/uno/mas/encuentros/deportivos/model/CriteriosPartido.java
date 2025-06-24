package com.tp.uno.mas.encuentros.deportivos.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CriteriosPartido {
    private String nivelMinimo;
    private String nivelMaximo;
    private int edadMinima;
    private int edadMaxima;
    private String genero;
    private float radioMaximo;

    public boolean cumpleCriterios(Usuario usuario) {
        return validarNivel(usuario.getNivel()) &&
               validarEdad(usuario.getEdad()) &&
               validarGenero(usuario.getGenero());
    }

    public boolean validarNivel(String nivel) {
        if (nivelMinimo == null || nivelMaximo == null) return true;
        
        int nivelUsuario = convertirNivelANumero(nivel);
        int nivelMin = convertirNivelANumero(nivelMinimo);
        int nivelMax = convertirNivelANumero(nivelMaximo);
        
        return nivelUsuario >= nivelMin && nivelUsuario <= nivelMax;
    }

    public boolean validarEdad(int edad) {
        return edad >= edadMinima && edad <= edadMaxima;
    }

    public boolean validarGenero(String genero) {
        if (this.genero == null || this.genero.equalsIgnoreCase("mixto")) {
            return true;
        }
        return this.genero.equalsIgnoreCase(genero);
    }

    public boolean validarUbicacion(Ubicacion ubicacion, Ubicacion ubicacionPartido) {
        if (radioMaximo <= 0) return true;
        double distancia = ubicacion.calcularDistancia(ubicacionPartido);
        return distancia <= radioMaximo;
    }

    private int convertirNivelANumero(String nivel) {
        switch (nivel.toLowerCase()) {
            case "principiante": return 1;
            case "intermedio": return 2;
            case "avanzado": return 3;
            default: return 1;
        }
    }

    // Getters y Setters generados automáticamente por @Data
} 