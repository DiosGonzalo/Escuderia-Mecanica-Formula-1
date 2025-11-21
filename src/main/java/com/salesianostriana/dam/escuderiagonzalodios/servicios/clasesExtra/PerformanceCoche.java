package com.salesianostriana.dam.escuderiagonzalodios.servicios.clasesExtra;

import com.salesianostriana.dam.escuderiagonzalodios.models.Coche;
import com.salesianostriana.dam.escuderiagonzalodios.models.Componente;
import com.salesianostriana.dam.escuderiagonzalodios.models.Dto.CocheDto;
import org.springframework.stereotype.Component;

@Component
public class PerformanceCoche {

    public static final double gravedad = 9.81;
    public static final double densidadAire = 1.225;
    public static final double caballoAW = 745.7;
    public static final double cofFriccion = 0.95;
    public static final double masaBase = 750.0;
    public static final double factorDegradacion = -0.0005;
    public static final double coefFactorCurvas = -0.8;
    public static final double penalizacionBalance = 2000.0;
    public static final double lambdaEventos = 0.0005;

    public CocheDto calcularMetricas(Coche coche) {
        double masaTotal;
        double potenciaW;
        double estadoPromedio;
        double drag;
        double downforce;
        double areaFrontal;
        double coefDrag;

        masaTotal = masaBase + coche.getComponentes().stream()
                .mapToDouble(Componente::getPeso)
                .sum();

        potenciaW = coche.getPotencia() * caballoAW;

        estadoPromedio = coche.getComponentes().stream()
                .mapToDouble(Componente::getEstado)
                .average()
                .orElse(100.0);

        drag = coche.getComponentes().stream()
                .mapToDouble(Componente::getDrag).sum();
        downforce = coche.getComponentes().stream()
                .mapToDouble(Componente::getDownforce).sum();

        areaFrontal = 1.5 + (drag * 0.01);
        coefDrag = 0.3 + (drag * 0.005);

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