package com.salesianostriana.dam.escuderiagonzalodios.servicios;

import com.salesianostriana.dam.escuderiagonzalodios.models.Coche;
import com.salesianostriana.dam.escuderiagonzalodios.models.Componente;
import com.salesianostriana.dam.escuderiagonzalodios.models.Enums.TipoComponente;
import com.salesianostriana.dam.escuderiagonzalodios.repositorios.ComponenteRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class ComponenteService {

    private final ComponenteRepository repo;

    public ComponenteService(ComponenteRepository repo) {
        this.repo = repo;
    }

    public List<Componente> listaComponentes() {
        return repo.findAll();
    }

    public List<Componente> componentesCoche(Coche coche) {
        List<Componente> componentesCoche = new ArrayList<>();
        Coche c;

        if (coche == null) return componentesCoche;

        for (Componente componente : listaComponentes()) {
            c = componente.getCoche();
            if (c != null && c.equals(coche)) {
                componentesCoche.add(componente);
            }
        }
        return componentesCoche;
    }

    public double desgasteComponente(long id) {
        Componente componenteDevolver = null;

        for (Componente componente : repo.findAll()) {
            if (componente.getId() == id) {
                componenteDevolver = componente;
                break;
            }
        }

        if (componenteDevolver == null) {
            // Manejo de error si no se encuentra el componente
            throw new RuntimeException("Componente con ID " + id + " no encontrado.");
        }

        return componenteDevolver.getEstado();
    }

    public void necesitaCambio(Coche coche) {
        for (Componente componente : componentesCoche(coche)) {
            if (componente.getEstado() < 40) {
                System.out.println(componente + " Necesita cambio");
            } else {
                System.out.println(componente);
            }
        }
    }

    public Componente buscarNombreComponenteCoche(String nombre, Coche coche) {
        return repo.findAll().stream()
                .filter(n -> n.getCoche() != null && n.getCoche().equals(coche))
                .filter(n -> n.getNombre().equals(nombre))
                .findFirst()
                .orElse(null);
    }

    public List<Componente> componentesSinCoche() {
        return repo.findAll().stream()
                .filter(n -> n.getCoche() == null)
                .toList();
    }

    public Componente findById(Long id) {
        return repo.findById(id).orElse(null);
    }

    public List<Componente> crearListaConIds(List<Long> componenteIds) {
        return repo.findAllById(componenteIds);
    }

    public List<Componente> buscarPorNombre(String nombre) {
        return repo.findAll().stream()
                .filter(n -> n.getNombre().toLowerCase().contains(nombre.toLowerCase()))
                .collect(Collectors.toList());
    }

    public List<Componente> filtrarPorTipo(TipoComponente tipo) {
        return repo.findAll().stream()
                .filter(n -> n.getTipo().equals(tipo))
                .toList();
    }

    public List<Componente> ordenarUsosMasMenos() {
        return repo.findAll().stream()
                .sorted(Comparator.comparing(Componente::getVecesUsado).reversed())
                .toList();
    }

    public List<Componente> ordenarUsosMenosMas() {
        return repo.findAll().stream()
                .sorted(Comparator.comparing(Componente::getVecesUsado))
                .toList();
    }

    public List<Componente> ordenarMejores() {
        return repo.findAll().stream()
                .sorted(Comparator.comparing(Componente::getCaballos).reversed()
                        .thenComparing(Componente::getVecesUsado))
                .collect(Collectors.toList());
    }

    public void eliminar(Long id) {
        repo.deleteById(id);
    }
}