package com.tp.uno.mas.encuentros.deportivos.model;

public class Ubicacion {
    private float latitud;
    private float longitud;
    private float radio;

    public Ubicacion() {}

    public Ubicacion(float latitud, float longitud, float radio) {
        this.latitud = latitud;
        this.longitud = longitud;
        this.radio = radio;
    }

    public float getLatitud() { return latitud; }
    public void setLatitud(float latitud) { this.latitud = latitud; }

    public float getLongitud() { return longitud; }
    public void setLongitud(float longitud) { this.longitud = longitud; }

    public float getRadio() { return radio; }
    public void setRadio(float radio) { this.radio = radio; }

    public double calcularDistancia(Ubicacion otra) {
        double radioTierra = 6371; // Radio de la Tierra en km
        double latRad1 = Math.toRadians(this.latitud);
        double latRad2 = Math.toRadians(otra.latitud);
        double deltaLat = Math.toRadians(otra.latitud - this.latitud);
        double deltaLon = Math.toRadians(otra.longitud - this.longitud);

        double a = Math.sin(deltaLat/2) * Math.sin(deltaLat/2) +
                   Math.cos(latRad1) * Math.cos(latRad2) *
                   Math.sin(deltaLon/2) * Math.sin(deltaLon/2);
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));

        return radioTierra * c;
    }

    @Override
    public String toString() {
        return "Ubicacion{" +
                "latitud=" + latitud +
                ", longitud=" + longitud +
                ", radio=" + radio +
                '}';
    }
} 