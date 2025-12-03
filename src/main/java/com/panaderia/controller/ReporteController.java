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

    @GetMapping("/total_ventas")
    public ResponseEntity<Map<String, Object>> getTotalVentas() {
        // Suma los montos de la tabla 'pago' donde el estado del pago es 'Completado'.
        // Asumimos que en tu tabla 'estadopago' existe un registro con descripcion = 'Completado'.
        String sql = """
            SELECT COALESCE(SUM(p.monto), 0) as total_ventas 
            FROM pago p
            INNER JOIN estadopago ep ON p.id_estado_pago = ep.id_estado_pago
            WHERE ep.descripcion = 'Completado'
        """;
        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error al obtener total de ventas: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/total_pedidos")
    public ResponseEntity<Map<String, Object>> getTotalPedidos() {
        // Cuenta todos los registros en la tabla 'pedido_cliente'.
        String sql = "SELECT COUNT(*) as total_pedidos FROM pedido_cliente";
        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error al obtener total de pedidos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/clientes_atendidos")
    public ResponseEntity<Map<String, Object>> getClientesAtendidos() {
        // Cuenta los clientes únicos (sin repetir) que han hecho un pedido.
        String sql = "SELECT COUNT(DISTINCT id_cliente) as clientes_atendidos FROM pedido_cliente";
        try {
            Map<String, Object> result = jdbcTemplate.queryForMap(sql);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error al obtener clientes atendidos: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/metodos_pago")
    public ResponseEntity<List<Map<String, Object>>> getMetodosPago() {
        // Consulta los métodos de pago usados, contando cuántas veces se usa cada uno.
        // Se usan las tablas 'pago' y 'metodopago' con sus nombres exactos.
        String sql = """
            SELECT 
                mp.nombre_metodo as metodo, 
                COUNT(*) as cantidad_usos 
            FROM pago p 
            INNER JOIN metodopago mp ON p.id_metodo_pago = mp.id_metodo_pago 
            GROUP BY mp.nombre_metodo 
            ORDER BY cantidad_usos DESC
        """;

        try {
            List<Map<String, Object>> result = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(result);
        } catch (Exception e) {
            System.err.println("Error al obtener métodos de pago: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
