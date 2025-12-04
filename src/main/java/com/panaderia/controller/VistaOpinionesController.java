package com.panaderia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/opiniones")
public class VistaOpinionesController {

    @GetMapping
    public String mostrarDashboardOpiniones() {
        // Simplemente devuelve el nombre del archivo HTML sin la extensión .html
        // Spring buscará automáticamente en src/main/resources/templates/
        return "dashboard";
    }
}
