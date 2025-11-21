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
    public String simularCarrera(@PathVariable String id, Model model) {

        try {
            // 1. CONVERSIÓN DE TIPO (Crucial)
            Long idCarrera = Long.parseLong(id);

            // 2. Lógica de simulación (pasamos el Long)
            var resultado = carreraService.correrCarrera(idCarrera);
            model.addAttribute("resultadoCarrera", resultado);

            // 3. Recuperar detalles estáticos (pasamos el Long)
            var detalles = carreraService.obtenerDetallesIniciales(idCarrera);
            model.addAttribute("detallesCarrera", detalles);

            model.addAttribute("carreraId", id); // El ID en string para la URL si hace falta

            return "detalleCarreras";

        } catch (NumberFormatException e) {
            // Si el ID no es un número válido
            return "redirect:/carreras?error=id_invalido";
        } catch (Exception e) {
            e.printStackTrace();
            return "error"; // O tu página de error
        }
    }
}
