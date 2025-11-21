package com.salesianostriana.dam.escuderiagonzalodios.servicios.algoritmos;

import com.salesianostriana.dam.escuderiagonzalodios.models.Componente;
import com.salesianostriana.dam.escuderiagonzalodios.models.Enums.TipoComponente;
import com.salesianostriana.dam.escuderiagonzalodios.servicios.ComponenteService;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.stereotype.Service;

import java.util.*;

@Builder
@AllArgsConstructor
@Service
public class MejoresCoches {

    private final Random random = new Random();
    private final ComponenteService componenteService;

    public List<Componente> generarConfiguracionAleatoria(Map<TipoComponente, List<Componente>> porTipo) {
        List<Componente> configuracion = new ArrayList<>();
        for (List<Componente> componentes : porTipo.values()) {
            Componente elegido = componentes.get(random.nextInt(componentes.size()));
            configuracion.add(elegido);
        }
        return configuracion;
    }

    public double evaluarConfiguraciones(List<Componente> componentes) {
        double potencia = 0;
        double peso = 0;
        double downforce = 0;
        double drag = 0;
        double estado = 0;

        for (Componente c : componentes) {
            potencia += c.getCaballos();
            peso += c.getPeso();
            downforce += c.getDownforce();
            drag += c.getDrag();
            estado += c.getEstado();
        }
        estado /= componentes.size();

        return ((potencia * 0.61) + (downforce * 0.25) + (estado * 0.23)
                - (peso * 0.37) - (drag * 0.3)) * 10;
    }

    public List<Componente> variacionesConfiguracion(List<Componente> original, Map<TipoComponente, List<Componente>> porTipo) {
        List<Componente> copia = new ArrayList<>(original);
        int indexCambiado = (int) (Math.random() * copia.size());
        TipoComponente tipo = copia.get(indexCambiado).getTipo();
        List<Componente> posibles = porTipo.get(tipo);
        Componente nuevo = posibles.get(random.nextInt(posibles.size()));
        copia.set(indexCambiado, nuevo);
        return copia;
    }

    public List<List<Componente>> copiarElite(List<List<Componente>> poblacion, int cantidadElite) {
        List<List<Componente>> elite = new ArrayList<>();
        for (int i = 0; i < cantidadElite; i++) {
            elite.add(new ArrayList<>(poblacion.get(i)));
        }
        return elite;
    }

    private List<List<Componente>> generarHijos(
            List<List<Componente>> elite,
            Map<TipoComponente, List<Componente>> porTipo,
            int cantidad) {

        List<List<Componente>> hijos = new ArrayList<>();

        while (hijos.size() < cantidad) {
            List<Componente> padre = elite.get(random.nextInt(elite.size()));
            hijos.add(variacionesConfiguracion(padre, porTipo));
        }

        return hijos;
    }

    public List<List<Componente>> optimizarConAlgGenetico(
            Map<TipoComponente, List<Componente>> porTipo,
            int generaciones,
            int tamPoblacion,
            int cantidadCoches) {

        List<List<Componente>> poblacion = new ArrayList<>();
        for (int i = 0; i < tamPoblacion * 2; i++) {
            poblacion.add(generarConfiguracionAleatoria(porTipo));
        }

        for (int gen = 0; gen < generaciones; gen++) {
            poblacion.sort(Comparator.comparingDouble(this::evaluarConfiguraciones).reversed());
            int cantidadElite = Math.max(2, tamPoblacion / 5);
            List<List<Componente>> elite = copiarElite(poblacion, cantidadElite);
            List<List<Componente>> nuevaPoblacion = new ArrayList<>(elite);
            int hijosNecesarios = tamPoblacion - cantidadElite;
            nuevaPoblacion.addAll(generarHijos(elite, porTipo, hijosNecesarios));
            poblacion = nuevaPoblacion;
        }

        poblacion.sort(Comparator.comparingDouble(this::evaluarConfiguraciones).reversed());

        List<List<Componente>> cochesUnicos = new ArrayList<>();
        Set<String> configuracionesVistas = new HashSet<>();

        for (List<Componente> coche : poblacion) {
            String hash = generarHashConfiguracion(coche);

            if (!configuracionesVistas.contains(hash)) {
                cochesUnicos.add(coche);
                configuracionesVistas.add(hash);

                if (cochesUnicos.size() >= cantidadCoches) {
                    break;
                }
            }
        }

        while (cochesUnicos.size() < cantidadCoches && cochesUnicos.size() > 0) {
            List<Componente> base = cochesUnicos.get(random.nextInt(cochesUnicos.size()));
            List<Componente> variacion = crearVariacionForzada(base, porTipo);

            String hash = generarHashConfiguracion(variacion);
            if (!configuracionesVistas.contains(hash)) {
                cochesUnicos.add(variacion);
                configuracionesVistas.add(hash);
            }
        }

        return cochesUnicos;
    }

    private List<Componente> crearVariacionForzada(List<Componente> original, Map<TipoComponente, List<Componente>> porTipo) {
        List<Componente> copia = new ArrayList<>(original);
        int mutaciones = 3 + random.nextInt(2);
        Set<Integer> indicesCambiados = new HashSet<>();

        while (indicesCambiados.size() < Math.min(mutaciones, copia.size())) {
            int indexCambiado = random.nextInt(copia.size());

            if (!indicesCambiados.contains(indexCambiado)) {
                TipoComponente tipo = copia.get(indexCambiado).getTipo();
                List<Componente> posibles = porTipo.get(tipo);

                Componente actual = copia.get(indexCambiado);
                List<Componente> alternativas = new ArrayList<>(posibles);
                alternativas.remove(actual);

                if (!alternativas.isEmpty()) {
                    Componente nuevo = alternativas.get(random.nextInt(alternativas.size()));
                    copia.set(indexCambiado, nuevo);
                    indicesCambiados.add(indexCambiado);
                }
            }
        }

        return copia;
    }

    private String generarHashConfiguracion(List<Componente> config) {
        return config.stream()
                .map(c -> String.valueOf(c.getId()))
                .sorted()
                .reduce("", (a, b) -> a + "-" + b);
    }
}