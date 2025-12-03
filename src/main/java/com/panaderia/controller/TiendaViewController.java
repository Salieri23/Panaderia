package com.panaderia.controller;

import com.panaderia.entity.Cliente;
import com.panaderia.repository.ClienteRepository;
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
@RequestMapping("/") 
public class TiendaViewController {

    @Autowired
    private ClienteRepository clienteRepository;

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
        } catch (Exception e) {
            model.addAttribute("productos", List.of());
            System.err.println("Error al cargar productos para la compra: " + e.getMessage());
        }

        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String emailCliente = auth.getName(); 
            
            // CORRECCIÓN: Usar el repositorio para buscar al cliente de forma segura
            Cliente cliente = clienteRepository.findByEmail(emailCliente);

            if (cliente != null) {
                // Si el cliente existe, añadimos su ID al modelo
                model.addAttribute("idCliente", cliente.getIdCliente());
            } else {
                // Si no se encuentra, el ID es null
                model.addAttribute("idCliente", null);
            }

        } catch (Exception e) {
            System.err.println("Error al obtener el cliente logueado: " + e.getMessage());
            model.addAttribute("idCliente", null);
        }
        
        return "comprar";
    }
}
