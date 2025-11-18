package com.salesianostriana.dam.escuderiagonzalodios.controllers;

import com.salesianostriana.dam.escuderiagonzalodios.models.Carrera;
import com.salesianostriana.dam.escuderiagonzalodios.servicios.CarreraService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

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
}
