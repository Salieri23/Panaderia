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
// CAMBIO: Quitamos la ruta base "/tienda"
@RequestMapping("/") 
public class TiendaViewController {

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @GetMapping("/comprar")
    public String mostrarPaginaCompras(Model model) {
        
        String sql = """
            SELECT 
                p.id_producto, 
                p.nombre, 
                p.precio_base 
            FROM inventario i
            INNER JOIN producto p ON i.id_producto = p.id_producto
            WHERE i.cantidad > 0
            ORDER BY p.nombre;
        """;

        try {
            List<Map<String, Object>> productos = jdbcTemplate.queryForList(sql);
            model.addAttribute("productos", productos);
            System.out.println("Productos encontrados para la tienda: " + productos.size());

        } catch (Exception e) {
            model.addAttribute("productos", List.of());
            System.err.println("Error al cargar productos para la compra: " + e.getMessage());
        }

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String emailCliente = auth.getName(); 
            
            String sqlCliente = "SELECT id_cliente FROM cliente WHERE email = ?";
            Long idCliente = jdbcTemplate.queryForObject(sqlCliente, Long.class, emailCliente);
            model.addAttribute("idCliente", idCliente);

        } catch (Exception e) {
            System.err.println("No se pudo obtener el ID del cliente logueado: " + e.getMessage());
            model.addAttribute("idCliente", null);
        }
        
        return "comprar";
    }
}
