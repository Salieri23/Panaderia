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
@RequestMapping("/dashboard")
public class DashboardController {

    @Autowired
    private JdbcTemplate jdbcTemplate;

    /**
     * Endpoint para obtener los datos agregados para el gráfico del dashboard.
     * Agrupa las opiniones por pedido y calcula el promedio de calificación y satisfacción.
     */
    @GetMapping("/opiniones")
    public ResponseEntity<List<Map<String, Object>>> getOpinionesParaGrafico() {
        // NOTA: La opinión está ligada a un 'pedido_cliente', no a un producto específico.
        // Por eso, agrupamos por ID de pedido.
        String sql = """
            SELECT
                'Pedido ' || op.id_pedido_cliente as nombre, -- Creamos una etiqueta legible para el gráfico
                AVG(op.calificacion) as promedio,
                AVG(op.satisfaccion) as satisfaccion
            FROM opinion_pedido op
            GROUP BY op.id_pedido_cliente
            ORDER BY op.id_pedido_cliente
        """;

        try {
            List<Map<String, Object>> resultados = jdbcTemplate.queryForList(sql);
            return ResponseEntity.ok(resultados);
        } catch (Exception e) {
            System.err.println("Error al obtener datos para el gráfico de opiniones: " + e.getMessage());
            e.printStackTrace();
            return ResponseEntity.internalServerError().build();
        }
    }
}
