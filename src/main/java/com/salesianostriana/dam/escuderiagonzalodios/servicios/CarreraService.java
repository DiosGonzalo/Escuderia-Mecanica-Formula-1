package com.salesianostriana.dam.escuderiagonzalodios.servicios;


import com.salesianostriana.dam.escuderiagonzalodios.models.Carrera;
import com.salesianostriana.dam.escuderiagonzalodios.models.Coche;
import com.salesianostriana.dam.escuderiagonzalodios.repositorios.CarreraRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class CarreraService {

    private CarreraRepository repo;
    private Random rnd = new Random();

    public CarreraService(CarreraRepository repo) {
        this.repo = repo;
    }
    public List<Carrera> todasCarreras(){
        return repo.findAll().stream()
                .collect(Collectors.toList());
    }

    public Carrera buscarPorId(Long id){
        return repo.findById(id).orElse(null);
    }

    public List<Carrera> carrerasCoche(Coche coche){
        return repo.findAll().stream()
                .filter(n->n.getCoches().stream()
                        .filter(c->c.getId() == coche.getId())
                        .count()> 0)
                .toList();
    }


    public List<Double> comprobarTiempo(Carrera carrera){
        double extraLLuvia = 15;
        double desgasteExtra = 0.5;
        double sinExtra = 0;
        List<Double> extras = List.of(extraLLuvia, desgasteExtra);
        List<Double> sinExtras = List.of(sinExtra, 0.0);
        return carrera.getClima().equals("Lluvia") ? extras : sinExtras;
    }

    public String correrCarrera(Long id){
        Carrera carrera = buscarPorId(id);
        int contadorVueltas = 0;
        double porcentajeChoque = 0;
        double extraVuelta = 0;
        double desgasteBase = 0;
        double facil = 4, medio = 8, dificil = 12;
        String rotura = "Se ha averiado un componente de tu coche, la carrera ha terminado en la vuelta: ";
        String choque = "Se ha producido un choque en la vuelta: ";
        Random rnd = new Random();

        switch(carrera.getDificultad().toString()){
            case "Facil":
                desgasteBase = 0.5;
                porcentajeChoque = facil + comprobarTiempo(carrera).getFirst();
                desgasteBase = desgasteBase + comprobarTiempo(carrera).getLast();
                break;
            case "Medio":
                desgasteBase = 1;
                porcentajeChoque = medio + comprobarTiempo(carrera).getFirst();
                desgasteBase = desgasteBase + comprobarTiempo(carrera).getLast();
                break;
            case "Dificil":
                desgasteBase = 1.5;
                porcentajeChoque = dificil + comprobarTiempo(carrera).getFirst();
                desgasteBase = desgasteBase + comprobarTiempo(carrera).getLast();
                break;
        }

        final double desgasteVuelta = desgasteBase;

        while(contadorVueltas <= carrera.getNumeroVueltas()){

            if(contadorVueltas == 0){
                // No hacer nada
            } else if(contadorVueltas == 1){
                extraVuelta = 20;
                porcentajeChoque = porcentajeChoque + extraVuelta;
            } else if(contadorVueltas >= 2 && contadorVueltas <= 5){
                extraVuelta = 5;
                porcentajeChoque = porcentajeChoque + extraVuelta;
            } else if(contadorVueltas >= carrera.getNumeroVueltas() - 10 && contadorVueltas <= carrera.getNumeroVueltas()){
                extraVuelta = 10;
                porcentajeChoque = porcentajeChoque + extraVuelta;
            }

            boolean hayRotura = carrera.getCoches().stream()
                    .flatMap(coche -> coche.getComponentes().stream())
                    .anyMatch(componente -> componente.getEstado() <= 0);

            if(hayRotura){
                return rotura + contadorVueltas;
            }

            double choqueProbabilidad = rnd.nextDouble() * 100;

            if(choqueProbabilidad < porcentajeChoque){
                return choque + contadorVueltas;
            }

            carrera.getCoches().forEach(coche ->
                    coche.getComponentes().forEach(componente ->
                            componente.setEstado(componente.getEstado() - desgasteVuelta)
                    )
            );

            contadorVueltas++;
        }

        return "Carrera terminada sin percances";
    }

}
