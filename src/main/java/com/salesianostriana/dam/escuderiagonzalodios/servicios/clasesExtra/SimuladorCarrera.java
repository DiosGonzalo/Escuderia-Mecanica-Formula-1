package com.salesianostriana.dam.escuderiagonzalodios.servicios.clasesExtra;

import com.salesianostriana.dam.escuderiagonzalodios.models.Carrera;

import java.util.Random;

public class SimuladorCarrera {
    PerformanceCoche performanceCoche;
    private Random random = new Random();
 //Primero, con las cosas de la carrera seteamos variables

    public static double indiceDificultadSoleado(Carrera carrera){
        return switch (carrera.getDificultad()){
            case MUY_DIFICIL -> 1;
            case DIFICIL -> 0.75;
            case MEDIA -> 0.5;
            case FACIL -> 0.25;
            default -> 0.5;
        };

        public static double obtenerIntensidadLluvia(Carrera carrera) {
            return switch (carrera.getClima()){
                case LLUVIA_INTENSA -> 1.0;
                case LLUVIA -> 0.75;
                case NUBLADO -> 0.3;
                case SECO -> 0.0;
                default -> 0.0;
            };

        }
        // variacion del coeficiente de friccion entre vueltas por el clima
        public static double desviacionCurva(Carrera carrera){
           // base - incertidumbre, dsviacion miniuma 0.08 en region completa de turbulencia, es decir en un dia normal
            //intensidad la intensidad lluvia
            double base = 0.08;
            double intensidad = obtenerIntensidadLluvia(carrera);
            return base * (1 + intensidad *3); //formula para el riesgo

        }

        //Ahora le meto la estoestatica



        //





    }

}
