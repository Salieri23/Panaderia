package com.panaderia.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
@RequestMapping("/reportes") // Esta es la URL que usarás en tu menú
public class ReporteVistaController {

    // Este método se ejecuta cuando alguien visita /reportes
    @GetMapping
    public String mostrarPaginaDeReportes() {
        // Simplemente devuelve el nombre del archivo HTML sin la extensión .html
        // Spring buscará automáticamente en src/main/resources/templates/
        return "reportes"; 
    }
}
