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

    private final CarreraService carreraService;

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
        Map<String, Object> detalles = carreraService.obtenerDetallesIniciales(id);
        model.addAttribute("detallesCarrera", detalles);
        model.addAttribute("carreraId", id);
        return "detalleCarreras";
    }

    @PostMapping("/carreras/{id}/simular")
    public String simularCarrera(@PathVariable String id, Model model) {
        try {
            Long idCarrera = Long.parseLong(id);
            var resultado = carreraService.correrCarrera(idCarrera);
            model.addAttribute("resultadoCarrera", resultado);
            var detalles = carreraService.obtenerDetallesIniciales(idCarrera);
            model.addAttribute("detallesCarrera", detalles);
            model.addAttribute("carreraId", id);
            return "detalleCarreras";
        } catch (NumberFormatException e) {
            return "redirect:/carreras?error=id_invalido";
        } catch (Exception e) {
            e.printStackTrace();
            return "error";
        }
    }
}