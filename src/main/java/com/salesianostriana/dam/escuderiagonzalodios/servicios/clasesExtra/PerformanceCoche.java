package com.salesianostriana.dam.escuderiagonzalodios.servicios.clasesExtra;

import com.salesianostriana.dam.escuderiagonzalodios.models.Coche;
import com.salesianostriana.dam.escuderiagonzalodios.models.Dto.CocheDto;

public class PerformanceCoche {


    //Constantes fisicas
    private static final double gravedad = 9.81;
    private static final double densidad_aire = 1.225; // uso la del aire a nivel del mar y $15º porque como me tenga que poner a calcular eso ya me tiro
    private static final double caballosAW = 745.7;
    private static final double coeficiente_friccion = 0.9;
    private static final double masaBaseCoche = 750; //la masa minima del chasis que despues tendre que sumarle la de los componentes

    //Factores que afectan al coche
    //mirar coef curva
    private static final double degradacionComponente = -0.015;
    private static final double coefCurvas = -0.8;
    private static final double desbalance = 2000.0;


    public CocheDto calcularMetricas(Coche coche){

        //que tengo que calcular
        //La masa total, La potencia total en wats porque es la unidad estandar
        //aerodinamica, el promedio de los componentes para la performance del coche, si esta mal se reduce la potencia
        //Area frontal y coef drag es para la resistencia del viento

        double potenciaW,areaFrontal, coefDrag,estadoPromedio, masaTotal, downforce, drag;

        masaTotal = masaBaseCoche + coche.getComponentes().stream()
                .mapToDouble(n -> n.getPeso())
                .sum();

        potenciaW = (coche.getPotencia())*caballosAW;

        estadoPromedio = coche.getComponentes().stream()
                .mapToDouble(c -> c.getEstado())
                .average()
                .orElse(100.0);

        drag = coche.getComponentes().stream()
                .mapToDouble(c->c.getDrag()).sum();
        downforce = coche.getComponentes().stream()
                .mapToDouble(c->c.getDownforce()).sum();


        //formula drag  Fdrag = 0.5 * p *Cd * A *V.mathpoy(2) / al cuadrado para los no programadores
        //Coeficiente drag y area frontal
        // asumo un area base de 1.5
        // el coeficiente de un "buen coche" es de 0.25 a 0.35 por lo que 0.3
        areaFrontal = 1.5 + (drag*0.01);  // A
        coefDrag = 0.3 +(drag*0.005);   //Cd

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
