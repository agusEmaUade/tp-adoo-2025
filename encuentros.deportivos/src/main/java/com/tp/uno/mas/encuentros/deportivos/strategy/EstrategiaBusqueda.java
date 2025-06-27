package com.tp.uno.mas.encuentros.deportivos.strategy;

import com.tp.uno.mas.encuentros.deportivos.model.Partido;
import com.tp.uno.mas.encuentros.deportivos.model.Usuario;
import java.util.List;

public interface EstrategiaBusqueda {
    /**
     * Filtra una lista de partidos según criterios específicos para un usuario.
     * @param partidos Lista de partidos disponibles para filtrar
     * @param usuario Usuario para el cual se filtran los partidos
     * @return Lista filtrada de partidos que cumplen con los criterios
     */
    List<Partido> buscar(List<Partido> partidos, Usuario usuario);
    
    /**
     * Verifica si un partido específico cumple con los criterios para un usuario.
     * @param partido Partido a validar
     * @param usuario Usuario para el cual se valida el partido
     * @return true si el partido cumple los criterios, false en caso contrario
     */
    boolean cumpleCriterios(Partido partido, Usuario usuario);
} 