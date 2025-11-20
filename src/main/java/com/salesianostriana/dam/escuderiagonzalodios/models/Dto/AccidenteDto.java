package com.salesianostriana.dam.escuderiagonzalodios.models.Dto;

import com.salesianostriana.dam.escuderiagonzalodios.models.Coche;
import lombok.Data;
import lombok.AllArgsConstructor;

@Data
@AllArgsConstructor
public class AccidenteDto { // DTO de Evento/Accidente

    private String tipo;
    private Coche coche;
    private int vuelta;
    private double probabilidad;
    private String descripcion;
}