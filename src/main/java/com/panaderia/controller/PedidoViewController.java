package com.panaderia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/cliente")
public class PedidoViewController {

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @GetMapping("/pedidos")
    public String mostrarPedidosCliente(Model model) {
        try {
            // 1. Obtener el ID del cliente logueado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String emailCliente = auth.getName();
            String sqlCliente = "SELECT id_cliente FROM cliente WHERE email = ?";
            Long idCliente = jdbcTemplate.queryForObject(sqlCliente, Long.class, emailCliente);

            // 2. Obtener los pedidos principales del cliente
            String sqlPedidos = """
                SELECT 
                    pc.id_pedido_cliente, 
                    pc.fecha, 
                    pc.monto_total
                FROM pedido_cliente pc
                WHERE pc.id_cliente = ?
                ORDER BY pc.fecha DESC;
                """;
            
            List<Map<String, Object>> pedidos = jdbcTemplate.queryForList(sqlPedidos, idCliente);

            // 3. Para cada pedido, obtener sus detalles (productos)
            for (Map<String, Object> pedido : pedidos) {
                Long idPedido = (Long) pedido.get("id_pedido_cliente");

                String sqlDetalles = """
                    SELECT 
                        p.nombre,
                        dp.cantidad,
                        dp.precio_unitario,
                        dp.subtotal
                    FROM detalle_pedido_cliente dp
                    INNER JOIN producto p ON dp.id_producto = p.id_producto
                    WHERE dp.id_pedido_cliente = ?;
                    """;
                
                List<Map<String, Object>> detalles = jdbcTemplate.queryForList(sqlDetalles, idPedido);
                pedido.put("detalles", detalles); // Añadimos la lista de detalles al mapa del pedido
            }

            model.addAttribute("pedidos", pedidos);

        } catch (Exception e) {
            model.addAttribute("pedidos", new ArrayList<>()); // Enviamos lista vacía si hay error
            System.err.println("Error al cargar los pedidos del cliente: " + e.getMessage());
        }

        return "mis-pedidos"; // Renderiza la vista mis-pedidos.html
    }
}
