package com.salesianostriana.dam.escuderiagonzalodios.servicios.clasesExtra;

import com.salesianostriana.dam.escuderiagonzalodios.models.Coche;
import com.salesianostriana.dam.escuderiagonzalodios.models.Componente;
import com.salesianostriana.dam.escuderiagonzalodios.models.Dto.CocheDto;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
public class PerformanceCoche {

    public static final double GRAVEDAD = 9.81;
    public static final double DENSIDAD_AIRE = 1.225;
    public static final double CABALLOS_A_WATTS = 745.7;
    public static final double COEF_FRICCION_ASFALTO = 0.95;
    public static final double MASA_BASE_COCHE = 750.0;

    public static final double FACTOR_DEGRADACION_ESTADO = -0.0005;
    public static final double COEF_FACTOR_CURVAS = -0.8;
    public static final double PENALIZACION_DESBALANCE = 2000.0;
    public static final double LAMBDA_BASE_EVENTOS = 0.0005;

    public CocheDto calcularMetricas(Coche coche) {

        double masaTotal = MASA_BASE_COCHE + coche.getComponentes().stream()
                .mapToDouble(Componente::getPeso)
                .sum();

        double potenciaW = coche.getPotencia() * CABALLOS_A_WATTS;

        double estadoPromedio = coche.getComponentes().stream()
                .mapToDouble(Componente::getEstado)
                .average()
                .orElse(100.0);

        double drag = coche.getComponentes().stream()
                .mapToDouble(Componente::getDrag).sum();
        double downforce = coche.getComponentes().stream()
                .mapToDouble(Componente::getDownforce).sum();

        double areaFrontal = 1.5 + (drag * 0.01);
        double coefDrag = 0.3 + (drag * 0.005);

        return CocheDto.builder()
                .areaFrontal(areaFrontal)
                .coefDrag(coefDrag)
                .downforceTotal(downforce)
                .estadoPromedio(estadoPromedio)
                .masaTotal(masaTotal)
                .potenciaWatts(potenciaW)
                .build();


    }
}








