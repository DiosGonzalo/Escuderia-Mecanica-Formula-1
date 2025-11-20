package com.salesianostriana.dam.escuderiagonzalodios.servicios;


import com.salesianostriana.dam.escuderiagonzalodios.models.Carrera;
import com.salesianostriana.dam.escuderiagonzalodios.models.Coche;
import com.salesianostriana.dam.escuderiagonzalodios.repositorios.CarreraRepository;
import com.salesianostriana.dam.escuderiagonzalodios.repositorios.CocheRepository;
import com.salesianostriana.dam.escuderiagonzalodios.servicios.clasesExtra.PerformanceCoche;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

@Service
public class CarreraService {

    PerformanceCoche performanceCoche;
    private CarreraRepository repo;
    private CocheRepository cocheRepo;
    private static final double GRAVEDAD = 9.81;
    private static final double DENSIDAD_AIRE = 1.225;
    private static final double COEF_FRICCION_ASFALTO = 0.95;





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






}
