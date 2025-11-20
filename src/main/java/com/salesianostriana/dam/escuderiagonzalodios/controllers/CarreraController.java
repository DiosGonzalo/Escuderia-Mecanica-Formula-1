package com.salesianostriana.dam.escuderiagonzalodios.controllers;

import com.salesianostriana.dam.escuderiagonzalodios.models.Carrera;
import com.salesianostriana.dam.escuderiagonzalodios.servicios.CarreraService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

import java.util.List;
import java.util.Map;

@Controller
public class CarreraController {

    CarreraService carreraService;

    public CarreraController(CarreraService carreraService) {
        this.carreraService = carreraService;
    }

    @GetMapping("/carreras")
    public String getMethodName(Model model) {
        List<Carrera> carreras = carreraService.todasCarreras();
        model.addAttribute("carreras", carreras);
        return "carreras";
    }

    @GetMapping("/carreras/{id}")
    public String mostrarDetallesCarrera(Model model, @PathVariable Long id) {

        // Cargar detalles iniciales
        Map<String, Object> detalles = carreraService.obtenerDetallesIniciales(id);

        model.addAttribute("detallesCarrera", detalles); // Poner detalles
        model.addAttribute("carreraId", id); // Poner ID

        // Mostrar vista
        return "detalleCarreras";
    }

    /**
     * Ejecuta la simulación.
     * Genera desgaste y resultados.
     */
    @PostMapping("/carreras/{id}/simular")
    public String simularCarrera(@PathVariable Long id, Model model) {

        // Correr simulación
        Map<String, Object> resultadoSimulacion = carreraService.correrCarrera(id);

        // Poner resultados
        model.addAttribute("resultadoCarrera", resultadoSimulacion);

        // Mostrar vista
        return "detalleCarreras";
    }
}
