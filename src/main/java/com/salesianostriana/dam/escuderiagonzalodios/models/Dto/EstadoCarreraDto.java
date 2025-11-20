package com.salesianostriana.dam.escuderiagonzalodios.models.Dto;

import com.salesianostriana.dam.escuderiagonzalodios.models.Coche;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class EstadoCarreraDto { // Renombrado de EstadoCarrera a Dto según tu código

    // Métricas de tiempo y progreso
    private double tiempoTotal = 0.0;
    private List<Double> tiemposVuelta = new ArrayList<>();
    private int vueltasCompletadas = 0;

    // Estado de la carrera (CRUCIAL)
    private boolean retirado = false;
    private int vueltaRetiro = 0;
    private String motivoRetiro = null;

    public EstadoCarreraDto(Coche coche) {
        // Constructor para inicialización
    }
}