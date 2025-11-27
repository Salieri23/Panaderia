package com.panaderia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tienda")
public class TiendaController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Endpoint para procesar el pago del carrito de compras.
     * Recibe el ID del cliente, el método de pago y la lista de productos del carrito.
     */
    @PostMapping("/procesar-pedido")
    @Transactional // ¡MUY IMPORTANTE! Asegura que todas las operaciones se completen o ninguna.
    public ResponseEntity<?> procesarPedido(@RequestBody Map<String, Object> datosPedido) {

        try {
            // 1. Extraer datos del cuerpo de la petición
            Long idCliente = Long.valueOf(datosPedido.get("idCliente").toString());
            Long idMetodoPago = Long.valueOf(datosPedido.get("idMetodoPago").toString());
            @SuppressWarnings("unchecked")
            List<Map<String, Object>> carrito = (List<Map<String, Object>>) datosPedido.get("carrito");

            if (carrito == null || carrito.isEmpty()) {
                return ResponseEntity.badRequest().body("El carrito está vacío.");
            }

            // 2. Crear el pedido principal
            String sqlPedido = """
                INSERT INTO pedido_cliente (fecha, id_cliente) 
                VALUES (NOW(), ?) 
                RETURNING id_pedido_cliente;
                """;
            Long idPedido = jdbcTemplate.queryForObject(sqlPedido, Long.class, idCliente);

            BigDecimal montoTotal = BigDecimal.ZERO;

            // 3. Recorrer cada item del carrito para crear los detalles y actualizar inventario
            for (Map<String, Object> item : carrito) {
                Long idProducto = Long.valueOf(item.get("id").toString());
                Integer cantidad = (Integer) item.get("cantidad");

                // Obtener precio actual del producto para evitar manipulación
                String sqlProducto = "SELECT precio_base FROM producto WHERE id_producto = ?";
                BigDecimal precioUnitario = jdbcTemplate.queryForObject(sqlProducto, BigDecimal.class, idProducto);

                // Calcular subtotal
                BigDecimal subtotal = precioUnitario.multiply(BigDecimal.valueOf(cantidad));
                montoTotal = montoTotal.add(subtotal);

                // Crear el detalle del pedido
                String sqlDetalle = """
                    INSERT INTO detalle_pedido_cliente (id_pedido_cliente, id_producto, cantidad, precio_unitario, subtotal)
                    VALUES (?, ?, ?, ?, ?);
                    """;
                jdbcTemplate.update(sqlDetalle, idPedido, idProducto, cantidad, precioUnitario, subtotal);

                // Actualizar el inventario (restando stock)
                String sqlInventario = "UPDATE inventario SET cantidad = cantidad - ? WHERE id_producto = ?";
                jdbcTemplate.update(sqlInventario, cantidad, idProducto);
            }

            // 4. Crear el registro de pago
            // Buscar el ID del estado "Completado"
            Long idEstadoCompletado = jdbcTemplate.queryForObject(
                "SELECT id_estado_pago FROM estado_pago WHERE nombre = 'Completado'", 
                Long.class
            );

            String sqlPago = """
                INSERT INTO pago (monto, fecha, id_pedido_cliente, id_metodo_pago, id_estado_pago)
                VALUES (?, NOW(), ?, ?, ?);
                """;
            jdbcTemplate.update(sqlPago, montoTotal, idPedido, idMetodoPago, idEstadoCompletado);

            // 5. Retornar una respuesta de éxito
            return ResponseEntity.ok(Map.of(
                "mensaje", "¡Pedido procesado con éxito!",
                "id_pedido", idPedido,
                "monto_total", montoTotal
            ));

        } catch (Exception e) {
            // Si hay cualquier error, la transacción se revertirá automáticamente
            System.err.println("Error al procesar el pedido: " + e.getMessage());
            return ResponseEntity.internalServerError().body(Map.of(
                "error", "Ocurrió un error al procesar tu pedido. Por favor, inténtalo de nuevo."
            ));
        }
    }
}
