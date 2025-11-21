package com.salesianostriana.dam.escuderiagonzalodios.servicios;

import com.salesianostriana.dam.escuderiagonzalodios.models.Carrera;
import com.salesianostriana.dam.escuderiagonzalodios.models.Coche;
import com.salesianostriana.dam.escuderiagonzalodios.models.Componente;
import com.salesianostriana.dam.escuderiagonzalodios.models.Enums.TipoComponente;
import com.salesianostriana.dam.escuderiagonzalodios.repositorios.CocheRepository;
import com.salesianostriana.dam.escuderiagonzalodios.servicios.algoritmos.MejoresCoches;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CocheService {

    private final CocheRepository repo;
    private final MejoresCoches mejoresCoches;

    public CocheService(CocheRepository repo, MejoresCoches mejoresCoches) {
        this.repo = repo;
        this.mejoresCoches = mejoresCoches;
    }

    public List<Coche> listaCompleta() {
        return repo.findAll();
    }

    public Coche buscarPorId(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("El coche buscado con ID: " + id + " no existe."));
    }

    public void delete(Long cocheId) {
        Coche coche = repo.findById(cocheId)
                .orElseThrow(() -> new EntityNotFoundException("Coche not found"));

        for (Carrera carrera : new ArrayList<>(coche.getCarreras())) {
            carrera.getCoches().remove(coche);
        }

        repo.delete(coche);
    }

    public String estadoCoche(Long id) {
        List<Componente> componentes = buscarPorId(id).getComponentes();
        double desgasteComponentes = 0;
        double desgasteTotal;

        if (componentes == null || componentes.isEmpty()) {
            return "Sin componentes";
        }

        for (Componente componente : componentes) {
            desgasteComponentes += componente.getEstado();
        }
        desgasteTotal = desgasteComponentes / componentes.size();

        return desgasteTotal <= 30 ? "Mal"
                : desgasteTotal < 60 ? "Regular"
                : "Bueno";
    }

    public void agregarCoche(Coche coche) {
        repo.save(coche);
    }

    public double calcularCaballos(Coche coche) {
        double caballos;

        caballos = coche.getPotencia();
        for (Componente componente : coche.getComponentes()) {
            caballos += componente.getCaballos();
        }
        return caballos;
    }

    public boolean comprobarRepetirComponentes(Coche coche) {
        List<Componente> componentes;
        Set<TipoComponente> tiposUnicos;

        componentes = coche.getComponentes();
        tiposUnicos = componentes.stream()
                .map(Componente::getTipo)
                .collect(Collectors.toSet());

        return tiposUnicos.size() != componentes.size();
    }

    public void guardar(Coche coche) {
        repo.save(coche);
    }

    public void guardarComponentes(List<Componente> componentes, Coche coche) {
        coche.setComponentes(componentes);
    }

    public void reemplazarComponentes(Coche coche, List<Componente> nuevosComponentes) {
        coche.getComponentes().forEach(c -> c.setCoche(null));
        coche.getComponentes().clear();
        nuevosComponentes.forEach(c -> {
            c.setCoche(coche);
            coche.getComponentes().add(c);
        });
    }

    public List<Componente> comprobarNuevosNulo(List<Componente> componentes) {
        if (componentes == null) {
            return new ArrayList<>();
        }
        return componentes.stream()
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }


    //En el readme tambien esta el algoritmo que he usado para las builds, por si no se entiende el codigo
    public List<List<Componente>> sugerirMejoresComponentes(int topN) {
        Map<TipoComponente, List<Componente>> porTipo = repo.findAll().stream()
                .flatMap(coche -> Optional.ofNullable(coche.getComponentes()).orElse(Collections.emptyList()).stream())
                .collect(Collectors.groupingBy(Componente::getTipo));

        return mejoresCoches.optimizarConAlgGenetico(porTipo, 100, 300, topN);
    }

    public List<Double> puntuacionMejoresDeBuilds(List<List<Componente>> mejoresBuilds) {
        return mejoresBuilds.stream()
                .map(build -> mejoresCoches.evaluarConfiguraciones(build))
                .collect(Collectors.toList());
    }
}