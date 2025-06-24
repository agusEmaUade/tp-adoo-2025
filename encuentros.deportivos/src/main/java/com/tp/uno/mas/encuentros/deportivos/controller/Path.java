package com.tp.uno.mas.encuentros.deportivos.controller;

/**
 * Interfaz que define las constantes de rutas de la API REST.
 * Centraliza todas las rutas para facilitar el mantenimiento y evitar duplicación.
 * 
 * Siguiendo principios SOLID (SRP - Single Responsibility) y DRY (Don't Repeat Yourself).
 */
public interface Path {
    
    // Rutas base
    String API_VERSION = "/api/v1";
    
    // Rutas para Cuentas
    String CUENTAS = API_VERSION + "/cuentas";
    String CUENTA_BY_ID = CUENTAS + "/{id}";
    String CUENTA_BY_EMAIL = CUENTAS + "/email/{email}";
    String CUENTA_PARTIDOS = CUENTA_BY_ID + "/partidos";
    String CUENTA_COMPATIBLES = CUENTA_BY_ID + "/compatibles";
    
    // Rutas para Partidos
    String PARTIDOS = API_VERSION + "/partidos";
    String PARTIDO_BY_ID = PARTIDOS + "/{id}";
    String PARTIDO_UNIRSE = PARTIDO_BY_ID + "/unirse";
    String PARTIDO_SALIR = PARTIDO_BY_ID + "/salir";
    String PARTIDO_CAMBIAR_ESTADO = PARTIDO_BY_ID + "/estado";
    String PARTIDOS_CERCANOS = PARTIDOS + "/cercanos";
    String PARTIDOS_BUSCAR = PARTIDOS + "/buscar";
    
    // Rutas para funcionalidades específicas
    String UBICACION = API_VERSION + "/ubicacion";
    String NOTIFICACIONES = API_VERSION + "/notificaciones";
    String DEPORTES = API_VERSION + "/deportes";
    String NIVELES = API_VERSION + "/niveles";
}
