package com.salesianostriana.dam.escuderiagonzalodios.servicios;

import com.salesianostriana.dam.escuderiagonzalodios.models.Carrera;
import com.salesianostriana.dam.escuderiagonzalodios.models.Coche;
import com.salesianostriana.dam.escuderiagonzalodios.models.Dto.AccidenteDto;
import com.salesianostriana.dam.escuderiagonzalodios.models.Dto.CocheDto;
import com.salesianostriana.dam.escuderiagonzalodios.models.Dto.EstadoCarreraDto;
import com.salesianostriana.dam.escuderiagonzalodios.repositorios.CarreraRepository;
import com.salesianostriana.dam.escuderiagonzalodios.repositorios.CocheRepository;
import com.salesianostriana.dam.escuderiagonzalodios.servicios.clasesExtra.PerformanceCoche;
import com.salesianostriana.dam.escuderiagonzalodios.servicios.clasesExtra.SimuladorCarrera;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CarreraService {

    @Autowired
    PerformanceCoche performanceCoche;

    private CarreraRepository repo;
    private CocheRepository cocheRepo;

    @Autowired
    SimuladorCarrera simulador;

    public CarreraService(CarreraRepository repo, CocheRepository cocheRepo) {
        this.repo = repo;
        this.cocheRepo = cocheRepo;
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

    public Map<String, Object> obtenerDetallesIniciales(Long carreraId) {
        Carrera carrera = repo.findById(carreraId)
                .orElseThrow(() -> new RuntimeException("Carrera no encontrada"));

        Map<String, Object> detalles = new HashMap<>();
        detalles.put("nombre", carrera.getNombre());
        detalles.put("vueltas", carrera.getNumeroVueltas());
        detalles.put("clima", carrera.getClima().name());
        detalles.put("longitudCircuito", carrera.getLongitudCircuito());
        detalles.put("coches", carrera.getCoches().stream()
                .map(Coche::getModelo)
                .collect(Collectors.toList()));
        return detalles;
    }

    private Map<Coche, EstadoCarreraDto> inicializarEstados(List<Coche> coches) {
        return coches.stream()
                .collect(Collectors.toMap(c -> c, EstadoCarreraDto::new));
    }

    private boolean todosRetirados(Map<Coche, EstadoCarreraDto> estados) {
        return estados.values().stream().allMatch(EstadoCarreraDto::isRetirado);
    }

    public Map<String, Object> correrCarrera(Long carreraId){
        Carrera carrera = repo.findById(carreraId).orElseThrow();
        List<Coche> coches = new ArrayList<>(carrera.getCoches());

        Map<Coche, EstadoCarreraDto> estados = inicializarEstados(coches);
        List<AccidenteDto> eventos = new ArrayList<>();

        for (int vuelta = 1; vuelta <= carrera.getNumeroVueltas(); vuelta++) {
            simularVuelta(vuelta, estados, carrera, eventos);
            if (todosRetirados(estados)) break;
        }

        return construirResultadoFinal(carrera, estados, eventos);
    }

    private void simularVuelta(int vuelta, Map<Coche, EstadoCarreraDto> estados,
                               Carrera carrera, List<AccidenteDto> eventos) {

        for (Map.Entry<Coche, EstadoCarreraDto> entry : estados.entrySet()) {
            Coche coche = entry.getKey();
            EstadoCarreraDto estado = entry.getValue();

            if (estado.isRetirado()) continue;

            CocheDto metrics = performanceCoche.calcularMetricas(coche);

            double tiempo = simulador.calcularTiempoVuelta(coche, metrics, carrera, vuelta, estado);

            estado.getTiemposVuelta().add(tiempo);
            estado.setTiempoTotal(estado.getTiempoTotal() + tiempo);
            estado.setVueltasCompletadas(estado.getVueltasCompletadas() + 1);

            simulador.aplicarDesgasteFisico(coche, carrera);
            simulador.verificarFallos(coche, estado, vuelta, eventos);
        }
    }

    private Map<String, Object> construirResultadoFinal(Carrera carrera,
                                                        Map<Coche, EstadoCarreraDto> estados,
                                                        List<AccidenteDto> eventos) {

        List<Map<String, Object>> resultados = estados.entrySet().stream()
                .sorted(Comparator.comparing((Map.Entry<Coche, EstadoCarreraDto> e) -> e.getValue().isRetirado())
                        .thenComparing(e -> e.getValue().getTiempoTotal()))
                .map(e -> construirResultadoCoche(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        Map<String, Object> resultado = new HashMap<>();
        resultado.put("carrera", carrera.getNombre());
        resultado.put("resultados", resultados);
        resultado.put("ganador", resultados.isEmpty() ? null : resultados.get(0));
        resultado.put("eventos", eventos);

        return resultado;
    }

    private Map<String, Object> construirResultadoCoche(Coche coche, EstadoCarreraDto estado) {
        Map<String, Object> r = new HashMap<>();
        r.put("coche", coche.getModelo());
        r.put("piloto", coche.getPiloto());
        r.put("retirado", estado.isRetirado());
        r.put("motivoRetiro", estado.getMotivoRetiro());
        r.put("vueltasCompletadas", estado.getVueltasCompletadas());
        r.put("tiempoTotal", estado.getTiempoTotal()); // Se mantiene para ordenación interna si hace falta

        // Formateo Tiempo Total (Horas)
        r.put("tiempoTotalTexto", formatearTiempoTotal(estado.getTiempoTotal()));

        // Cálculo y Formateo Tiempo Promedio (Minutos)
        double promedio = 0.0;
        if (estado.getVueltasCompletadas() > 0) {
            promedio = estado.getTiempoTotal() / estado.getVueltasCompletadas();
        }
        r.put("tiempoPromedioTexto", formatearTiempoVuelta(promedio));

        return r;
    }

    private String formatearTiempoTotal(double totalSegundos) {
        int horas = (int) (totalSegundos / 3600);
        int minutos = (int) ((totalSegundos % 3600) / 60);
        double segundos = totalSegundos % 60;
        return String.format("%dh %02dm %06.3fs", horas, minutos, segundos);
    }

    private String formatearTiempoVuelta(double segundos) {
        int minutos = (int) (segundos / 60);
        double segs = segundos % 60;
        return String.format("%d:%06.3f", minutos, segs);
    }
}