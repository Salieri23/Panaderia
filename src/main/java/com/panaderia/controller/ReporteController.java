package com.panaderia.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/reportes")
public class ReporteController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // Endpoint para obtener el total acumulado de ventas
    @GetMapping("/total_ventas")
    public ResponseEntity<Map<String, Object>> getTotalVentas() {
        String sql = """
            SELECT COALESCE(SUM(p.monto), 0) as total_ventas 
            FROM pago p 
            INNER JOIN estado_pago ep ON p.id_estado_pago = ep.id_estado_pago 
            WHERE ep.nombre = 'Completado'
        """;

        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error al obtener total de ventas: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Endpoint para obtener el total de pedidos
    @GetMapping("/total_pedidos")
    public ResponseEntity<Map<String, Object>> getTotalPedidos() {
        String sql = "SELECT COUNT(*) as total_pedidos FROM pedido_cliente";

        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error al obtener total de pedidos: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Endpoint para obtener el número de clientes únicos atendidos
    @GetMapping("/clientes_atendidos")
    public ResponseEntity<Map<String, Object>> getClientesAtendidos() {
        String sql = """
            SELECT COUNT(DISTINCT id_cliente) as clientes_atendidos 
            FROM pedido_cliente
        """;

        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error al obtener clientes atendidos: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }

    // Endpoint para obtener los métodos de pago más usados
    @GetMapping("/metodos_pago")
    public ResponseEntity<List<Map<String, Object>>> getMetodosPago() {
        String sql = """
            SELECT mp.nombre as metodo, COUNT(*) as cantidad_usos 
            FROM pago p 
            INNER JOIN metodo_pago mp ON p.id_metodo_pago = mp.id_metodo_pago 
            GROUP BY mp.nombre 
            ORDER BY cantidad_usos DESC
        """;

        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error al obtener métodos de pago: " + e.getMessage());
            return ResponseEntity.internalServerError().build();
        }
    }
}
