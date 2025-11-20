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
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Service
public class CarreraService {

    PerformanceCoche performanceCoche;
    private CarreraRepository repo;
    private CocheRepository cocheRepo;

    SimuladorCarrera simulador;
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

    public Map<String, Object> obtenerDetallesIniciales(Long carreraId) {
        Carrera carrera = repo.findById(carreraId)
                .orElseThrow(() -> new RuntimeException("Carrera no encontrada")); // Buscar carrera

        Map<String, Object> detalles = new HashMap<>();
        detalles.put("nombre", carrera.getNombre()); // Nombre
        detalles.put("vueltas", carrera.getNumeroVueltas()); // Total vueltas
        detalles.put("clima", carrera.getClima().name()); // Clima
        detalles.put("coches", carrera.getCoches().stream()
                .map(Coche::getModelo)
                .collect(Collectors.toList())); // Lista coches
        return detalles;
    }

    private Map<Coche, EstadoCarreraDto> inicializarEstados(List<Coche> coches) {
        // Uso del API Stream de Java 8 para inicializar el estado de cada coche
        return coches.stream()
                .collect(Collectors.toMap(c -> c, EstadoCarreraDto::new));
    }

    private boolean todosRetirados(Map<Coche, EstadoCarreraDto> estados) {
        // Uso del API Stream: verifica si TODOS los valores cumplen la condición
        return estados.values().stream().allMatch(EstadoCarreraDto::isRetirado);
    }

    public Map<String, Object> correrCarrera(Long carreraId){

        Carrera carrera = repo.findById(carreraId).orElseThrow();
        List<Coche> coches = new ArrayList<>(carrera.getCoches());

        // 2. Inicialización del Estado (Asigna EstadoCarrera a cada Coche)
        Map<Coche, EstadoCarreraDto> estados = inicializarEstados(coches);
        List<AccidenteDto> eventos = new ArrayList<>(); // Lista global para registrar incidentes

        // 3. Bucle Principal de Simulación
        for (int vuelta = 1; vuelta <= carrera.getNumeroVueltas(); vuelta++) {

            // Simular la dinámica de la vuelta para cada coche
            simularVuelta(vuelta, estados, carrera, eventos);

            // Si todos los coches han abandonado, terminamos la carrera
            if (todosRetirados(estados)) break;
        }

        // 4. Construir y Devolver Resultados
        return construirResultadoFinal(carrera, estados, eventos);

    }

    private void simularVuelta(int vuelta, Map<Coche, EstadoCarreraDto> estados,
                               Carrera carrera, List<AccidenteDto> eventos) {

        for (Map.Entry<Coche, EstadoCarreraDto> entry : estados.entrySet()) {
            Coche coche = entry.getKey();
            EstadoCarreraDto estado = entry.getValue();

            if (estado.isRetirado()) continue;

            // --- 1. CÁLCULO DE MÉTRICAS Y TIEMPO ---
            CocheDto metrics = performanceCoche.calcularMetricas(coche);

            // Llamada al método de simulación (corregido para usar la instancia 'simulador')
            double tiempo = simulador.calcularTiempoVuelta(coche, metrics, carrera, vuelta, estado);

            // --- 2. ACTUALIZACIÓN DE ESTADO (Corregido el uso del Setter) ---
            estado.getTiemposVuelta().add(tiempo);
            estado.setTiempoTotal(estado.getTiempoTotal() + tiempo);
            estado.setVueltasCompletadas(estado.getVueltasCompletadas() + 1);

            // --- 3. GESTIÓN DE DESGASTE Y FALLOS ---
            simulador.aplicarDesgasteFisico(coche, carrera);
            simulador.verificarFallos(coche, estado, vuelta, eventos);

            // c) Persistir los cambios del coche (estados de los componentes)
            // if (!estado.isRetirado()) {
            //    repo.save(coche); // NECESITAS LA INSTANCIA DE REPO
            // }
        }
    }

    // ----------------------------------------------------
    // METODO AUXILIAR: CONSTRUCCIÓN DEL RESULTADO FINAL
    // ----------------------------------------------------

    private Map<String, Object> construirResultadoFinal(Carrera carrera,
                                                        Map<Coche, EstadoCarreraDto> estados,
                                                        List<AccidenteDto> eventos) {

        // 1. Ordenar los resultados:
        List<Map<String, Object>> resultados = estados.entrySet().stream()
                // Ordenar por retirado (false < true) y luego por tiempoTotal
                .sorted(Comparator.comparing((Map.Entry<Coche, EstadoCarreraDto> e) -> e.getValue().isRetirado())
                        .thenComparing(e -> e.getValue().getTiempoTotal())) // Usar getTiempoTotal()
                .map(e -> construirResultadoCoche(e.getKey(), e.getValue()))
                .collect(Collectors.toList());

        // 2. Construir el objeto final
        Map<String, Object> resultado = new HashMap<>();
        resultado.put("carrera", carrera.getNombre());
        resultado.put("resultados", resultados);
        resultado.put("ganador", resultados.isEmpty() ? null : resultados.get(0));
        resultado.put("eventos", eventos);

        return resultado;
    }

    // ----------------------------------------------------
    // METODO AUXILIAR: CONSTRUCCIÓN DEL RESULTADO POR COCHE
    // ----------------------------------------------------

    private Map<String, Object> construirResultadoCoche(Coche coche, EstadoCarreraDto estado) {
        Map<String, Object> r = new HashMap<>();
        r.put("coche", coche.getModelo());
        r.put("piloto", coche.getPiloto());
        r.put("tiempoTotal", estado.getTiempoTotal()); // Usar getTiempoTotal()
        r.put("retirado", estado.isRetirado());
        r.put("motivoRetiro", estado.getMotivoRetiro());

        // Aquí podrías añadir la lógica para calcular y añadir la 'mejorVuelta' y la 'consistencia'

        return r;
    }





}
