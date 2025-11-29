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
@RequestMapping("/cliente")
public class EntregaViewController {

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @GetMapping("/entregas")
    public String mostrarEntregasCliente(Model model) {
        try {
            // 1. Obtener el email del cliente logueado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String emailCliente = auth.getName();

            // 2. Buscar el ID del cliente
            String sqlCliente = "SELECT id_cliente FROM cliente WHERE email = ?";
            Long idCliente = jdbcTemplate.queryForObject(sqlCliente, Long.class, emailCliente);

            // 3. Buscar las entregas de ese cliente (unimos tablas)
            String sqlEntregas = """
                SELECT 
                    e.id_entrega,
                    e.id_pedido_cliente,
                    e.fecha_entrega,
                    e.direccion_entrega,
                    e.estado,
                    e.observaciones
                FROM entrega e
                INNER JOIN pedido_cliente pc ON e.id_pedido_cliente = pc.id_pedido_cliente
                WHERE pc.id_cliente = ?
                ORDER BY e.fecha_entrega DESC;
                """;
            
            List<Map<String, Object>> entregas = jdbcTemplate.queryForList(sqlEntregas, idCliente);

            // 4. Añadir la lista de entregas al modelo para que Thymeleaf la use
            model.addAttribute("entregas", entregas);

        } catch (Exception e) {
            // Si hay un error (ej: cliente no encontrado), enviamos una lista vacía
            model.addAttribute("entregas", List.of());
            System.err.println("Error al cargar entregas del cliente: " + e.getMessage());
        }

        return "entregas"; // Devuelve la vista entregas.html
    }
}
