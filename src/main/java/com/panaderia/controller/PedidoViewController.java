package com.panaderia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.*;

@Controller
@RequestMapping("/cliente")
public class PedidoViewController {

    @Autowired
    private org.springframework.jdbc.core.JdbcTemplate jdbcTemplate;

    @GetMapping("/pedidos")
    public String mostrarPedidosCliente(Model model) {
        try {
            // 1. Obtener el email del cliente logueado
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            String emailCliente = auth.getName();

            // 2. Buscar el cliente por su email.
            String sqlCliente = "SELECT id_cliente FROM cliente WHERE email = ?";
            Long idCliente = jdbcTemplate.queryForObject(sqlCliente, Long.class, emailCliente);

            if (idCliente == null) {
                System.err.println("Error: No se encontró un cliente con el email: " + emailCliente);
                model.addAttribute("pedidos", new ArrayList<>());
                return "mis-pedidos";
            }

            // 3. UNA SOLA CONSULTA PARA OBTENER TODO: pedidos y sus detalles.
            // El cambio clave está aquí: calculamos el monto_total con una subconsulta.
            String sqlPedidosConDetalles = """
                SELECT
                    pc.id_pedido_cliente,
                    pc.fecha,
                    (
                        SELECT COALESCE(SUM(dpc_sub.subtotal), 0)
                        FROM detalle_pedido_cliente dpc_sub
                        WHERE dpc_sub.id_pedido_cliente = pc.id_pedido_cliente
                    ) AS monto_total, -- Se calcula el monto total aquí
                    p.id_producto AS detalle_id_producto,
                    p.nombre AS detalle_nombre,
                    dpc.cantidad,
                    dpc.precio_unitario,
                    dpc.subtotal
                FROM pedido_cliente pc
                LEFT JOIN detalle_pedido_cliente dpc ON pc.id_pedido_cliente = dpc.id_pedido_cliente
                LEFT JOIN producto p ON dpc.id_producto = p.id_producto
                WHERE pc.id_cliente = ?
                ORDER BY pc.fecha DESC, pc.id_pedido_cliente DESC;
                """;

            List<Map<String, Object>> resultados = jdbcTemplate.queryForList(sqlPedidosConDetalles, idCliente);

            // 4. Procesamos el resultado plano para convertirlo en una lista de pedidos anidados
            Map<Long, Map<String, Object>> pedidosMap = new LinkedHashMap<>();
            for (Map<String, Object> fila : resultados) {
                Long idPedido = (Long) fila.get("id_pedido_cliente");
                Map<String, Object> pedido = pedidosMap.getOrDefault(idPedido, new LinkedHashMap<>());

                // Si el pedido es nuevo, lo añadimos al mapa
                if (pedido.isEmpty()) {
                    pedido.put("id_pedido_cliente", idPedido);
                    pedido.put("fecha", fila.get("fecha"));
                    pedido.put("monto_total", fila.get("monto_total")); // Usamos el valor calculado
                    pedido.put("detalles", new ArrayList<>());
                    pedidosMap.put(idPedido, pedido);
                }

                // Añadimos el detalle a la lista de detalles del pedido
                // Solo si el detalle realmente existe (gracias al LEFT JOIN)
                if (fila.get("detalle_id_producto") != null) {
                    Map<String, Object> detalle = new LinkedHashMap<>();
                    detalle.put("nombre", fila.get("detalle_nombre"));
                    detalle.put("cantidad", fila.get("cantidad"));
                    detalle.put("precio_unitario", fila.get("precio_unitario"));
                    detalle.put("subtotal", fila.get("subtotal"));
                    ((List<Map<String, Object>>) pedido.get("detalles")).add(detalle);
                }
            }

            model.addAttribute("pedidos", new ArrayList<>(pedidosMap.values()));
            System.out.println("Se encontraron " + pedidosMap.size() + " pedidos para el cliente ID: " + idCliente);

        } catch (Exception e) {
            e.printStackTrace();
            System.err.println("Error al cargar los pedidos del cliente: " + e.getMessage());
            model.addAttribute("pedidos", new ArrayList<>());
        }

        return "mis-pedidos";
    }
}
