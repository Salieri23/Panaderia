package com.panaderia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/tienda") // Ruta base para las vistas de la tienda
public class TiendaViewController {

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @GetMapping("/comprar")
    public String mostrarPaginaCompras(Model model) {
        // 1. Cargar la lista de productos disponibles desde la BD
        String sql = "SELECT id_producto, nombre, precio_base FROM producto WHERE cantidad > 0 ORDER BY nombre;";
        try {
            List<Map<String, Object>> productos = jdbcTemplate.queryForList(sql);
            model.addAttribute("productos", productos);
        } catch (Exception e) {
            model.addAttribute("productos", List.of()); // Enviar lista vacía si hay error
            System.err.println("Error al cargar productos para la compra: " + e.getMessage());
        }

        // 2. Obtener el ID del cliente que está logueado
        // ¡IMPORTANTE! Esto es un ejemplo. Debes adaptarlo a tu sistema de login.
        // Si usas Spring Security con un CustomUserDetailsService, puedes obtenerlo así:
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            // Asumimos que el nombre de usuario (username) es el email o un ID único
            String emailCliente = auth.getName(); 
            
            // Buscamos el ID del cliente en la base de datos
            String sqlCliente = "SELECT id_cliente FROM cliente WHERE email = ?";
            Long idCliente = jdbcTemplate.queryForObject(sqlCliente, Long.class, emailCliente);
            model.addAttribute("idCliente", idCliente);

        } catch (Exception e) {
            // Si no se puede obtener el cliente, ponemos un valor por defecto o manejamos el error
            System.err.println("No se pudo obtener el ID del cliente logueado: " + e.getMessage());
            model.addAttribute("idCliente", null); // O redirigir a login
        }
        
        // Si no usas Spring Security, necesitarás pasar el ID de otra forma (ej: sesión, parámetro, etc.)
        // Por ahora, para pruebas, podrías hardcodearlo:
        // model.addAttribute("idCliente", 1L);

        return "comprar"; // Devuelve la vista comprar.html
    }
}
